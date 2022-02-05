package taxi;

public class MessageAcceptProposal extends MyMessage {
  public long proposalId;

  MessageAcceptProposal(long proposalId) {
    this.proposalId = proposalId;
  }

}
