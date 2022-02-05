package taxi;

public class MessageMyPedestrianPosition extends MyMessage {
  public Pos pos;
  public MessageMyPedestrianPosition(Pos pos) {
    this.pos = pos;
  }
  
}
