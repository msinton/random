package advent

import cats.syntax.foldable._
import cats.instances.int._
import cats.instances.list._

import scala.annotation.tailrec

object day1 {

  def calcFuel(i: Int): Int =
    (i / 3d).toInt - 2

  def calcFuels(input: List[Int]) =
    input.foldMap(calcFuel)

  // star 2
  def calcFuelRec(i: Int): Int = {
    @tailrec
    def loop(sum: Int, fuel: Int): Int =
      if (fuel <= 0) sum else loop(sum + fuel, calcFuel(fuel))

    loop(0, calcFuel(i))
  }

  def calcFuels2(input: List[Int]) =
    input.foldMap(calcFuelRec)
}
