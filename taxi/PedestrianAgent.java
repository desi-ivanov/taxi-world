package taxi;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class PedestrianAgent extends MyAgent {
  static int SLEEP_DURATION = 300;
  Pos currentPos = new Pos(0, 0);
  Pos destination = new Pos(0, 0);
  Random random = new Random(System.currentTimeMillis() + System.nanoTime());

  @Override
  protected void setup() {
    super.setup();
    updateCurrentPos(new Pos(random.nextInt(EnvironmentView.GRID_SZ), random.nextInt(EnvironmentView.GRID_SZ)));
    pickRandomDestination();

    this.on(MessageArrived.class, (c) -> {
      updateCurrentPos(destination);
      destination = new Pos(random.nextInt(EnvironmentView.GRID_SZ), random.nextInt(EnvironmentView.GRID_SZ));
      pickRandomDestination();
      addBehaviour(new WakerBehaviour(this, SLEEP_DURATION) {
        @Override
        protected void onWake() {
          addBehaviour(new TravelBehaviour());
        }
      });
    });
    this.on(MessageAlreadyOccupied.class, (c) -> {
      log("Whoops... Taxi was not available anymore. I'll try again later.");
      addBehaviour(new WakerBehaviour(this, SLEEP_DURATION) {
        @Override
        protected void onWake() {
          addBehaviour(new TravelBehaviour());
        }
      });
    });

    addBehaviour(new TravelBehaviour());
  }

  private void updateCurrentPos(Pos pos) {
    this.currentPos = pos;
    notifyEnvironmentObserver(new MessageMyPedestrianPosition(pos));
  }

  private void pickRandomDestination() {
    destination = new Pos(random.nextInt(EnvironmentView.GRID_SZ), random.nextInt(EnvironmentView.GRID_SZ));
    notifyEnvironmentObserver(new MessageMyDesiredPosition(destination));
  }

  private class TravelBehaviour extends OneShotBehaviour {
    TravelBehaviour() {
      super();
    }

    public void action() {
      searchTaxi();
      awaitResponse();
    }

    private void searchTaxi() {
      log("Searching taxi");
      ServiceDescription sd = new ServiceDescription();
      sd.setType(TaxiAgent.SERVICE_NAME);
      sd.setName("TaxiWorld");
      DFAgentDescription dfd = new DFAgentDescription();
      dfd.addServices(sd);
      try {
        Arrays.asList(DFService.search(myAgent, dfd))
            .forEach(this::sendRequest);
      } catch (FIPAException e) {
        e.printStackTrace();
      }
    }

    private void awaitResponse() {
      myAgent.addBehaviour(
          new AwaitTaxiResponseBehaviour(
              xs -> {
                log(
                    "Taxi offers: " + xs.stream().map(x -> x.fst.getSender().getLocalName() + ": $" + x.snd.price + "")
                        .collect(Collectors.joining(";")));
                xs.stream().sorted((a, v) -> (int) (a.snd.price - v.snd.price))
                    .findFirst()
                    .ifPresentOrElse((best) -> {
                      log("Accepting propose from " + best.fst.getSender().getLocalName());
                      acceptProposal(best.fst.getSender(), best.snd.proposalId);
                    }, () -> {
                      log("No taxi currently available... I'll try again later");
                      myAgent.addBehaviour(new TravelBehaviour());
                    });
              },
              SLEEP_DURATION));
    }

    private void sendRequest(DFAgentDescription dfa) {
      log("Requesting price from taxi: " + dfa.getName().getLocalName());
      PedestrianAgent ag = (PedestrianAgent) myAgent;
      ACLMessage msg = new ACLMessage(ACLMessage.CFP);
      msg.addReceiver(dfa.getName());
      try {
        msg.setContentObject(new MessageCallTaxi(ag.currentPos, ag.destination));
        myAgent.send(msg);
      } catch (Exception e) {
        e.printStackTrace();
      }

    }

    private void acceptProposal(AID taxi, long propId) {
      ACLMessage msg = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
      msg.addReceiver(taxi);
      try {
        msg.setContentObject(new MessageAcceptProposal(propId));
        myAgent.send(msg);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

  }

  private class AwaitTaxiResponseBehaviour extends OneShotBehaviour {
    Consumer<List<Pair<ACLMessage, MessageTaxiPropose>>> onResult;
    long timeout;

    AwaitTaxiResponseBehaviour(
        Consumer<List<Pair<ACLMessage, MessageTaxiPropose>>> onResult,
        long timeout) {
      super();
      this.onResult = onResult;
      this.timeout = timeout;
    }

    public void action() {
      List<Pair<ACLMessage, MessageTaxiPropose>> proposals = new LinkedList<>();
      ((PedestrianAgent) myAgent).once(MessageTaxiPropose.class, proposals::add);
      myAgent.addBehaviour(new WakerBehaviour(myAgent, timeout) {
        @Override
        protected void onWake() {
          onResult.accept(proposals);
        }
      });
    }
  }

}
