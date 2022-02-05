package taxi;


import jade.core.AID;
public class TaxiClient {
  public AID aid;
  public double price;
  public Pos source;
  public Pos destination;
  TaxiClient(AID aid, double price, Pos source, Pos destination) {
    this.aid = aid;
    this.price = price;
    this.source = source;
    this.destination = destination;
  }
  @Override
  public String toString() {
    return "TaxiClient{" +
        "aid=" + aid +
        ", price=" + price +
        ", source=" + source +
        ", destination=" + destination +
        '}';
  }
}
