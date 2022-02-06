package taxi;

import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class EnvironmentModel {

  EnvironmentView view = new EnvironmentView("TaxiWorld", 800);
  ConcurrentHashMap<String, Pos> taxiPositions = new ConcurrentHashMap<>();
  ConcurrentHashMap<String, Pos> pedestrianPositions = new ConcurrentHashMap<>();
  ConcurrentHashMap<String, Pos> destionationPositions = new ConcurrentHashMap<>();
  public ConcurrentHashMap<String, String> carrying = new ConcurrentHashMap<>();

  public void updatePedestrianPickedUp(String taxi, String ped) {
    carrying.put(taxi, ped);
    flush();
  }

  public void updatePedDrop(String taxi) {
    if(carrying.containsKey(taxi)) {
      pedestrianPositions.put(carrying.get(taxi), taxiPositions.get(taxi));
      carrying.remove(taxi);
    }
    flush();
  }

  public void updateTaxiPosition(String id, Pos pos) {
    taxiPositions.put(id, pos);
    if (carrying.containsKey(id)) {
      pedestrianPositions.put(carrying.get(id), pos);
    }
    flush();
  }

  public void updatePedestrianPosition(String id, Pos pos) {
    pedestrianPositions.put(id, pos);
    flush();
  }

  public void updatePedestrianDestination(String id, Pos pos) {
    destionationPositions.put(id, pos);
    flush();
  }

  public void flush() {
    view
        .flush(
            Stream.concat(
                Stream.concat(
                    destionationPositions.entrySet().stream()
                        .map(e -> new Pair<>(e, ElType.DESIRED_POS)),
                    taxiPositions.entrySet().stream()
                        .map(e -> new Pair<>(e, ElType.TAXI))),
                pedestrianPositions.entrySet().stream()
                    .map(e -> new Pair<>(e, ElType.PEDESTRIAN)))
                .map(x -> new Pair<>(x.fst.getValue(), new Pair<>(x.fst.getKey(), x.snd)))
                .toList());
  }
}
