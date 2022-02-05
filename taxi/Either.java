package taxi;
import java.util.function.Function;

public class Either<L, R> {
  boolean isLeft;
  L left;
  R right;
  static <T, U> Either<T, U> fromLeft(T left) {
    Either<T, U> either = new Either<>();
    either.isLeft = true;
    either.left = left;
    return either;
  }
  static <T, U> Either<T, U> fromRight(U right) {
    Either<T, U> either = new Either<>();
    either.isLeft = false;
    either.right = right;
    return either;
  }
  
  public <V> V match(Function<L, V> caseLeft, Function<R, V> caseRight) {
    if (isLeft) {
      return caseLeft.apply(left);
    } else {
      return caseRight.apply(right);
    }
  }

  
}
