package taxi;

import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class EnvironmentObserver extends MyAgent {
  static String SERVICE_NAME = "EnvironmentObserver";
  EnvironmentModel model = new EnvironmentModel();

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

    on(MessageMyTaxiPosition.class, op -> model.updateTaxiPosition(op.fst.getSender().getLocalName(), op.snd.pos));
    on(MessageMyDesiredPosition.class, op ->  model.updatePedestrianDestination(op.fst.getSender().getLocalName(), op.snd.pos));
    on(MessageMyPedestrianPosition.class, op -> model.updatePedestrianPosition(op.fst.getSender().getLocalName(), op.snd.pos));
    on(MessagePickedUpAgent.class, op -> model.updatePedestrianPickedUp(op.fst.getSender().getLocalName(), op.snd.client.getLocalName()));
    on(MessageDrop.class, op -> model.updatePedDrop(op.fst.getSender().getLocalName()));
  }
}
