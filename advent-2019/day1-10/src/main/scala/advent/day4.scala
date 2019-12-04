package advent

import scala.util.chaining._

object day4 {

  def digits(i: Int): List[Int] =
    i.toString.split("").map(_.toInt).toList

  def digitsMonotonic(i: Int) =
    digits(i)
      .pipe(_.sliding(2))
      .pipe(_.forall { case List(a, b) => a <= b })

  def containsTwoAdjacent(i: Int) =
    digits(i)
      .pipe(_.sliding(2))
      .pipe(_.exists { case List(a, b) => a == b })

  // there exists a group of 2 adjacent without there being more than 2 adjacent
  def containsExactlyTwoAdjacent(i: Int) =
    digits(i)
      .pipe { x =>
        x.sliding(2).exists { case List(a, b) => a == b && x.count(_ == a) == 2 }
      }

  def passwords(range: Range) =
    range
      .filter(digitsMonotonic)
      .filter(containsTwoAdjacent)
      .toList

  def passwords2(range: Range) =
    range
      .filter(digitsMonotonic)
      // .filter(containsExactlyTwoAdjacent)
      .filter { x => // since monotonic can do:
        digits(x)
          .groupBy(identity)
          .values
          .exists(_.size == 2)
      }
      .toList
}
