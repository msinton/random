package tel

object Alert {

  def apply(input: Seq[Double]): Boolean = {
    input match {
      case x1 :: x2 :: xs => if (isInvalid(x1, x2)) true else apply(x2 :: xs)
      case _ => false
    }
  }

  def isInvalid(x1: Double, x2: Double): Boolean =
    (x1 > 50 && x2 > 50) || (x1 < 50 && x2 < 50)
}
