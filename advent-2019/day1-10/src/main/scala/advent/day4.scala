package advent

import scala.util.chaining._

object day4 {

  def digits(i: Int): List[Int] =
    i.toString.split("").map(_.toInt).toList

  def digitsMonotonic(i: Int) =
    digits(i)
      .pipe(x => x zip x.tail)
      .pipe(_.forall { case (a, b) => a <= b })

  def containsTwoAdjacent(i: Int) =
    digits(i)
      .pipe(x => x zip x.tail)
      .pipe(_.exists { case (a, b) => a == b })

  def containsExactlyTwoAdjacent(i: Int) =
    digits(i)
      .pipe { x =>
        val pairs = x zip x.tail
        pairs.exists { case (a, b) => a == b && x.count(_ == a) == 2 }
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
        val d = digits(x)
        d.groupBy(identity).values.map(_.size).toList.contains(2)
      }
      .toList
}
