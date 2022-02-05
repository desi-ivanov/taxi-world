package taxi;

public class TravelProposal {
  double price;
  Pos source;
  Pos destination;
  
  TravelProposal(double price, Pos source, Pos destination) {
    this.price = price;
    this.source = source;
    this.destination = destination;
  }
  
}
