package advent
import scala.collection.SortedMap

object day8 {

  val dimensions = (25, 6)

  def parseInput(input: String) =
    (toLines _)
      .andThen(toLayers)
      .apply(input)

  def toLines(input: String): List[IndexedSeq[Int]] =
    input.grouped(dimensions._1).map(_.map(_.asDigit)).toList

  def toLayers(lines: List[IndexedSeq[Int]]): SortedMap[Int, List[IndexedSeq[Int]]] =
    SortedMap(lines.grouped(dimensions._2).zipWithIndex.map(_.swap).toList: _*)

  def fewestZeroLayer1x2(input: String): Int = {
    val layers = parseInput(input)

    val layer = layers(
      layers.view.mapValues(_.flatten.count(_ == 0)).toList.minBy(_._2)._1
    )

    layer.flatten.count(_ == 1) * layer.flatten.count(_ == 2)
  }

  val black = 0
  val white = 1

  def mergeLayers(
    over: IndexedSeq[IndexedSeq[Int]],
    below: IndexedSeq[IndexedSeq[Int]]
  ): IndexedSeq[IndexedSeq[Int]] =
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

  def decode(input: String): IndexedSeq[IndexedSeq[Int]] = {
    val layers = parseInput(input).values.map(_.toIndexedSeq).toList

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
