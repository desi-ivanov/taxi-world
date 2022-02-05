package taxi;

import jade.core.AID;

public class MessagePickedUpAgent extends MyMessage {
  public AID client;

  public MessagePickedUpAgent(AID client) {
    this.client = client;
  }
}
  