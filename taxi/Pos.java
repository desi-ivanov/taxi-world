package taxi;
import java.io.Serializable;
public class Pos implements Serializable {
  public int x = 0;
  public int y = 0;
  public Pos(int x, int y) {
    this.x = x;
    this.y = y;
  }
  @Override
  public String toString() {
    return "Pos{" +
        "x=" + x +
        ", y=" + y +
        '}';
  }
}
