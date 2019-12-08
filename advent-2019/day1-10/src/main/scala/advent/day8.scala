package advent

import scala.util.chaining._

object day8 {

  val dimensions = (25, 6)

  def parseInput(input: String) =
    (toLines _)
      .andThen(toLayers)
      .apply(input)

  def toLines(input: String): List[IndexedSeq[Int]] =
    input.grouped(dimensions._1).map(_.map(_.asDigit)).toList

  def toLayers(lines: List[IndexedSeq[Int]]): Seq[List[IndexedSeq[Int]]] =
    lines.grouped(dimensions._2).toSeq

  def fewestZeroLayer1x2(input: String): Int =
    parseInput(input)
      .pipe(_.minBy(_.flatten.count(_ == 0)))
      .flatten
      .pipe(x => x.count(_ == 1) * x.count(_ == 2))

  val black = 0
  val white = 1

  def mergeLayers(
    over: List[IndexedSeq[Int]],
    below: List[IndexedSeq[Int]]
  ): List[IndexedSeq[Int]] =
    (over zip below).map {
      case (a, b) => (a zip b).map(Function.tupled(mergeCell))
    }

  def mergeCell(over: Int, below: Int): Int =
    over match {
      case `black` => black
      case `white` => white
      case _       => below
    }

  def printer(x: Iterable[Iterable[Int]]): String =
    x.map(_.mkString)
      .map(_.map {
        case '1' => 'X'
        case '0' => ' '
        case '2' => '.'
      })
      .mkString("\n")

  def decode(input: String): List[IndexedSeq[Int]] = {
    val layers = parseInput(input).toList

    layers.tail.foldLeft(layers.head)(mergeLayers)
  }

  def decode2(input: String): IndexedSeq[IndexedSeq[Int]] = {
    val layers = input.grouped(25 * 6).map(_.map(_.asDigit)).toList

    layers.tail
      .foldLeft(layers.head)(
        (a, b) => (a zip b).map(Function.tupled(mergeCell))
      )
      .grouped(25)
      .toIndexedSeq
  }

}
