package taxi;
public class MessageCallTaxi extends MyMessage {
  public Pos from;
  public Pos to;
  MessageCallTaxi(Pos from, Pos to) {
    this.from = from;
    this.to = to;
  }
}
