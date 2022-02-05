package taxi;

public class MessageTaxiPropose extends MyMessage {
  public double price;
  public long proposalId;

  public MessageTaxiPropose(double price, long proposalId) {
    this.price = price;
    this.proposalId = proposalId;
  }
}
