package taxi;

import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class EnvironmentObserver extends MyAgent {
  static String SERVICE_NAME = "EnvironmentObserver";
  EnvironmentModel environmentModel = new EnvironmentModel();

  @Override
  protected void setup() {
    super.setup();
    ServiceDescription sd = new ServiceDescription();
    sd.setName("TaxiWorld");
    sd.setType(SERVICE_NAME);

    DFAgentDescription dfd = new DFAgentDescription();
    dfd.setName(getAID());
    dfd.addServices(sd);
    try {
      DFService.register(this, dfd);
    } catch (FIPAException fe) {
      fe.printStackTrace();
    }

    on(MessageMyTaxiPosition.class, environmentModel::updateTaxiPosition);
    on(MessageMyDesiredPosition.class, environmentModel::updatePedestrianDestination);
    on(MessageMyPedestrianPosition.class, environmentModel::updatePedestrianPosition);
    on(MessagePickedUpAgent.class, environmentModel::updatePedestrianPickedUp);

  }

}
