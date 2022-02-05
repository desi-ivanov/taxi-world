package taxi;

import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class EnvironmentObserver extends MyAgent {
  static String SERVICE_NAME = "EnvironmentObserver";
  EnvironmentView view = new EnvironmentView("TaxiWorld", 800);
  ConcurrentHashMap<String, Pos> taxiPositions = new ConcurrentHashMap<>();
  ConcurrentHashMap<String, Pos> pedestrianPositions = new ConcurrentHashMap<>();
  ConcurrentHashMap<String, Pos> destionationPositions = new ConcurrentHashMap<>();

  @Override
  protected void setup() {
    super.setup();
    ServiceDescription sd = new ServiceDescription();
    sd.setName("TaxiWorld");
    sd.setType(SERVICE_NAME);

    DFAgentDescription dfd = new DFAgentDescription();
    dfd.setName(getAID());
    dfd.addServices(sd);
    try {
      DFService.register(this, dfd);
    } catch (FIPAException fe) {
      fe.printStackTrace();
    }

    on(MessageMyTaxiPosition.class, this::updateTaxiPosition);
    on(MessageMyDesiredPosition.class, this::updatePedestrianDestination);
    on(MessageMyPedestrianPosition.class, this::updatePedestrianPosition);
    on(MessagePickedUpAgent.class, this::updatePedestrianPickedUp);

  }

  void updatePedestrianPickedUp(Pair<ACLMessage, MessagePickedUpAgent> op) {
    AID client = op.snd.client;
    pedestrianPositions.remove(client.getLocalName());
    flush();
  }

  void updateTaxiPosition(Pair<ACLMessage, MessageMyTaxiPosition> op) {
    taxiPositions.put(op.fst.getSender().getLocalName(), op.snd.pos);
    flush();
  }

  void updatePedestrianPosition(Pair<ACLMessage, MessageMyPedestrianPosition> op) {
    pedestrianPositions.put(op.fst.getSender().getLocalName(), op.snd.pos);
    flush();
  }

  void updatePedestrianDestination(Pair<ACLMessage, MessageMyDesiredPosition> op) {
    destionationPositions.put(op.fst.getSender().getLocalName(), op.snd.pos);
    flush();
  }

  void flush() {
    view
        .flush(
            Stream.concat(
                Stream.concat(
                    destionationPositions.entrySet().stream()
                        .map(e -> new Pair<>(e, ElType.DESIRED_POS)),
                    pedestrianPositions.entrySet().stream()
                        .map(e -> new Pair<>(e, ElType.PEDESTRIAN))),
                taxiPositions.entrySet().stream()
                    .map(e -> new Pair<>(e, ElType.TAXI)))
                .map(x -> new Pair<>(x.fst.getValue(), new Pair<>(x.fst.getKey(), x.snd)))
                .toList());
  }
}
