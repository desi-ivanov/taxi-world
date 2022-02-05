package taxi;
public class MessageMyTaxiPosition extends MyMessage {
  public Pos pos;
  public MessageMyTaxiPosition(Pos pos) {
    this.pos = pos;
  }
}
