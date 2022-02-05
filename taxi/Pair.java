package taxi;
public class Pair<T, U> {
  T fst;
  U snd;
  public Pair(T fst, U snd) {
    this.fst = fst;
    this.snd = snd;
  }
  @Override
  public String toString() {
    return "Pair{" +
        "fst=" + fst +
        ", snd=" + snd +
        '}';
  }
}
