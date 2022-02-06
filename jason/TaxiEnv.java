
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Stream;

import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Structure;
import jason.environment.Environment;
import taxi.EnvironmentModel;
import taxi.EnvironmentView;
import taxi.Pos;

public class TaxiEnv extends Environment {
  Random random = new Random(System.currentTimeMillis());
  static Logger logger = Logger.getLogger(TaxiEnv.class.getName());
  private EnvironmentModel model = new EnvironmentModel();

  @Override
  public void init(String[] args) {
    int taxiNum = Integer.parseInt(args[0]);
    int pedsNum = Integer.parseInt(args[1]);
    Stream.concat(
        Stream.iterate(1, i -> i + 1)
            .limit(taxiNum)
            .map(i -> taxiNum == 1 ? "taxi" : ("taxi" + i)),
        Stream.iterate(1, i -> i + 1)
            .limit(pedsNum)
            .map(i -> pedsNum == 1 ? "pedestrian" : ("pedestrian" + i)))
        .forEach(name -> {
          logger.info("Adding agent " + name);
          int x = random.nextInt(EnvironmentView.GRID_SZ);
          int y = random.nextInt(EnvironmentView.GRID_SZ);
          addPercept(name, Literal.parseLiteral("current_pos(" + x + "," + y + ")"));
          addPercept(name, Literal.parseLiteral("world_size(" + EnvironmentView.GRID_SZ + ")"));
          if (name.contains("pedestrian"))
            model.updatePedestrianPosition(name, new Pos(x, y));
          else if (name.contains("taxi"))
            model.updateTaxiPosition(name, new Pos(x, y));
        });
  }

  @Override
  public boolean executeAction(String ag, Structure action) {
    logger.info(ag + " doing: " + action);
    try {
      if (action.getFunctor().equals("move_at")) {
        int x = (int) ((NumberTerm) action.getTerm(0)).solve();
        int y = (int) ((NumberTerm) action.getTerm(1)).solve();
        if (ag.contains("taxi")) {
          model.updateTaxiPosition(ag, new Pos(x, y));
          if (model.carrying.containsKey(ag)) {
            removePerceptsByUnif(model.carrying.get(ag), Literal.parseLiteral("current_pos(X,Y)"));
            addPercept(model.carrying.get(ag), Literal.parseLiteral("current_pos(" + x + "," + y + ")"));
          }
        } else if (ag.contains("ped"))
          model.updatePedestrianPosition(ag, new Pos(x, y));
      } else if (action.getFunctor().equals("carry")) {
        model.updatePedestrianPickedUp(ag, action.getTerm(0).toString());
      } else if (action.getFunctor().equals("drop")) {
        model.updatePedDrop(ag);
      } else if (action.getFunctor().equals("desire_position")) {
        int x = (int) ((NumberTerm) action.getTerm(0)).solve();
        int y = (int) ((NumberTerm) action.getTerm(1)).solve();
        model.updatePedestrianDestination(ag, new Pos(x, y));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return true;
  }
}
