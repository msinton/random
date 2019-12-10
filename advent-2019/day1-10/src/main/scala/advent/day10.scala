package advent

import cats.implicits._

import scala.util.chaining._
import scala.annotation.tailrec
import scala.collection.immutable.Nil

object day10 {

  final case class Radians(value: Double) extends AnyVal

  final case class Visible(
    at: (Int, Int),
    radians: Radians,
    distance: Int
  )

  type Coord = (Int, Int)

  def parse(input: String): Set[Coord] =
    input
      .split("\n")
      .toList
      .zipWithIndex
      .flatten { case (s, y) => s.zipWithIndex.collect { case ('#', x) => (x, y) } }
      .toSet

  def asteroidsLineOfSight(input: String): Map[Coord, Set[Visible]] =
    (parse _)
      .andThen(xs => xs.toList.foldMap(x => Map(x -> visible(x, xs - x))))
      .apply(input)

  def bestLineOfSight(asteroids: Map[Coord, Set[Visible]]) =
    asteroids.view
      .mapValues(reduceByLineOfSight)
      .pipe(_.maxBy(_._2.size))

  def bestLineOfSightCount(input: String) =
    asteroidsLineOfSight(input).pipe(bestLineOfSight)._2.size

  def visible(a: Coord, xs: Set[Coord]): Set[Visible] =
    xs.map(b => Visible(b, radians(a, b), distance(a, b)))

  def radians(a: Coord, b: Coord): Radians =
    Radians(math.atan2(b._1 - a._1.toDouble, b._2 - a._2.toDouble)) // x, y backwards to flip axis for star 2

  def distance(x: Coord, y: Coord): Int =
    math.abs(x._1 - y._1) + math.abs(x._2 - y._2)

  def reduceByLineOfSight(xs: Set[Visible]): Set[Visible] =
    xs.groupBy(_.radians).values.map(_.minBy(_.distance)).toSet

  // star 2
  def findBestThenVaporize(input: String, count: Int) = {
    val asteroids = asteroidsLineOfSight(input)
    val best = bestLineOfSight(asteroids)._1

    vaporize(asteroids(best), count)
  }

  def vaporize(xs: Set[Visible], count: Int): Option[Coord] = {
    val sorted = xs
      .groupBy(_.radians.value)
      .toList
      .sortBy(-_._1)
      .map(_._2.toList.sortBy(_.distance))

    @tailrec
    def loop(xs: List[List[Visible]], n: Int): Option[Coord] =
      xs match {
        case Nil => none
        case x :: _ if n === 1 =>
          x.headOption.map(_.at)
        case x :: xss =>
          val next = if (x.tail.isEmpty) xss else xss :+ x.tail
          loop(next, n - 1)
      }
    loop(sorted, count)
  }

}
