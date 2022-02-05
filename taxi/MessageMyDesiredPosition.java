package taxi;
public class MessageMyDesiredPosition extends MyMessage {
  public Pos pos;
  public MessageMyDesiredPosition(Pos pos) {
    this.pos = pos;
  }

}
