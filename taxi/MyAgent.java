package taxi;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class MyAgent extends jade.core.Agent {
  AtomicInteger idCounter = new AtomicInteger(0);
  ConcurrentHashMap<Integer, Consumer<Pair<ACLMessage, ? extends MyMessage>>> subscribers = new ConcurrentHashMap<>();

  public void log(Object s) {
    System.out.println(getLocalName() + ": " + s.toString());
  }

  @Override
  protected void setup() {
    super.setup();

    addBehaviour(new ReceiveCalls((op) -> {
      subscribers.values().forEach(subscriber -> subscriber.accept(op));
    }));
  }

  List<DFAgentDescription> findEnvironmentObservers() {
    ServiceDescription sd = new ServiceDescription();
    sd.setName("TaxiWorld");
    sd.setType(EnvironmentObserver.SERVICE_NAME);
    DFAgentDescription dfd = new DFAgentDescription();
    dfd.addServices(sd);
    try {
      return Arrays.asList(DFService.search(this, dfd));
    } catch (FIPAException e) {
      e.printStackTrace();
      return new LinkedList<>();
    }
  }

  void notifyEnvironmentObserver(MyMessage msg) {
    findEnvironmentObservers()
        .forEach(obs -> {
          ACLMessage acl = new ACLMessage(ACLMessage.INFORM);
          acl.addReceiver(obs.getName());
          try {
            acl.setContentObject(msg);
            send(acl);
          } catch (Exception e) {
            e.printStackTrace();
          }
        });
  }

  public <T extends MyMessage> void once(Class<T> klass, Consumer<Pair<ACLMessage, T>> c) {
    int id = idCounter.getAndIncrement();
    Consumer<Pair<ACLMessage, ? extends MyMessage>> wrp = (m -> {
      if (klass.isInstance(m.snd)) {
        c.accept(new Pair<>(m.fst, klass.cast(m.snd)));
      }
      this.subscribers.remove(id);
    });
    this.subscribers.put(id, wrp);
  }

  public <T extends MyMessage> void on(Class<T> klass, Consumer<Pair<ACLMessage, T>> c) {
    int id = idCounter.getAndIncrement();
    Consumer<Pair<ACLMessage, ? extends MyMessage>> wrp = (m -> {
      if (klass.isInstance(m.snd)) {
        c.accept(new Pair<>(m.fst, klass.cast(m.snd)));
      }
    });
    this.subscribers.put(id, wrp);
  }

  private class ReceiveCalls extends CyclicBehaviour {
    Consumer<Pair<ACLMessage, MyMessage>> onMessage;

    ReceiveCalls(Consumer<Pair<ACLMessage, MyMessage>> onMessage) {
      super();
      this.onMessage = onMessage;
    }

    public void action() {
      ACLMessage msg = myAgent.receive();
      if (msg != null) {
        try {

          MyMessage m = (MyMessage) msg.getContentObject();
          onMessage.accept(new Pair<>(msg, m));
        } catch (Exception e) {
          e.printStackTrace();
        }
      } else {
        block();
      }
    }
  }
}
