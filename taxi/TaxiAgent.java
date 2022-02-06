package taxi;

import java.util.HashMap;

import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class TaxiAgent extends MyAgent {
  static int SPEED = 5;
  static String SERVICE_NAME = "TaxiTransport";
  HashMap<Long, TravelProposal> proposals = new HashMap<>();
  boolean available = true;
  Pos currentPos = new Pos(0, 0);
  double money = 0;

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

    this.on(MessageCallTaxi.class, op -> {
      if (available) {
        propose(op.fst, op.snd);
      }
    });
    this.on(MessageAcceptProposal.class, op -> handleProposalAccepted(op.fst, op.snd));
  }

  double dist(Pos a, Pos b) {
    return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
  }

  void handleProposalAccepted(ACLMessage msg, MessageAcceptProposal cp) {
    if (available) {
      available = false;
      TravelProposal p = proposals.get(cp.proposalId);
      addBehaviour(new ServeClientBehaviour(new TaxiClient(
          msg.getSender(),
          p.price,
          p.source,
          p.destination)));
    } else {
      informAlreadyOccupied(msg);
    }
  }

  void informAlreadyOccupied(ACLMessage msg) {
    ACLMessage reply = new ACLMessage(ACLMessage.FAILURE);
    reply.addReceiver(msg.getSender());
    reply.setPerformative(ACLMessage.FAILURE);
    try {
      reply.setContentObject(new MessageAlreadyOccupied());
      send(reply);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  void propose(ACLMessage src, MessageCallTaxi call) {
    ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
    msg.addReceiver(src.getSender());
    try {
      TravelProposal prop = new TravelProposal(((dist(currentPos, call.from) + dist(call.from, call.to)) * 10),
          call.from, call.to);
      log("Got a new request! Proposing price: " + prop.price);
      long propId = System.currentTimeMillis();
      proposals.put(propId, prop);
      msg.setContentObject(new MessageTaxiPropose(prop.price, propId));
      send(msg);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  void stepTowawrds(Pos p) {
    int dx = currentPos.x > p.x ? -1 : currentPos.x < p.x ? 1 : 0;
    int dy = currentPos.y > p.y ? -1 : currentPos.y < p.y ? 1 : 0;
    currentPos.x += dx;
    currentPos.y += dy;
    notifyEnvironmentObserver(new MessageMyTaxiPosition(currentPos));
  }

  private class ServeClientBehaviour extends OneShotBehaviour {
    private TaxiClient client;

    ServeClientBehaviour(TaxiClient c) {
      super();
      this.client = c;
    }

    public void action() {
      log("Serving client " + client.aid.getLocalName());
      myAgent.addBehaviour(
          new GotoPosition(client, client.source, () -> myAgent.addBehaviour(
              new PickUpBehaviour(client, () -> myAgent.addBehaviour(
                  new GotoPosition(client, client.destination, () -> myAgent.addBehaviour(
                      new DropClientBehaviour(client, () -> ((TaxiAgent) myAgent).available = true))))))));
    }
  }

  private class GotoPosition extends OneShotBehaviour {
    Pos dest;
    Runnable andThen;
    TaxiClient client;

    GotoPosition(TaxiClient client, Pos d, Runnable t) {
      super();
      this.client = client;
      this.dest = d;
      this.andThen = t;
    }

    public void action() {
      TaxiAgent ta = (TaxiAgent) myAgent;
      ta.stepTowawrds(dest);
      log("Serving " + this.client.aid.getLocalName() + " (" + ta.currentPos.x + " " + ta.currentPos.y + ") -> ("
          + dest.x + " " + dest.y + ")");
      myAgent.addBehaviour(
          new WakerBehaviour(myAgent, 1000 / SPEED) {
            @Override
            public void onWake() {
              if (dest.x == ta.currentPos.x && dest.y == ta.currentPos.y) {
                andThen.run();
              } else {
                myAgent.addBehaviour(new GotoPosition(client, dest, andThen));

              }
            }
          });
    }
  }

  private class PickUpBehaviour extends OneShotBehaviour {
    TaxiClient c;
    Runnable andThen;

    PickUpBehaviour(TaxiClient c, Runnable cons) {
      super();
      this.c = c;
      this.andThen = cons;
    }

    public void action() {
      log("Taxi: Picked up client " + c.aid.getLocalName());
      andThen.run();
      notifyEnvironmentObserver(new MessagePickedUpAgent(c.aid));
    }
  }

  private class DropClientBehaviour extends OneShotBehaviour {
    TaxiClient c;
    Runnable andThen;

    DropClientBehaviour(TaxiClient c, Runnable andThen) {
      super();
      this.c = c;
      this.andThen = andThen;
    }

    public void action() {
      log("Delivered " + c.aid.getLocalName() + " to destination!");
      ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
      msg.addReceiver(c.aid);
      try {
        msg.setContentObject(new MessageDrop());
      } catch (Exception e) {
        e.printStackTrace();
      }
      myAgent.send(msg);
      andThen.run();
      ((TaxiAgent) myAgent).money += c.price;
      log("Balance: $" + c.price);
      notifyEnvironmentObserver(new MessageDrop());
    }
  }

}
