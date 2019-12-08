package advent

import cats.implicits._
import cats.data.NonEmptyList
import cats.kernel.Semigroup

import scala.util.chaining._

final case class Cell(value: Int) extends AnyVal

final case class Row(xs: NonEmptyList[Cell])

final case class Layer(xs: NonEmptyList[Row])

object day8Semigroup {

  val dimensions = (25, 6)

  def toLayers(input: String): NonEmptyList[Layer] =
    input
      .grouped(dimensions._1)
      .map(_.toList pipe NonEmptyList.fromListUnsafe)
      .map(_.map(_.asDigit pipe Cell).pipe(Row))
      .grouped(dimensions._2)
      .map(_.toList pipe NonEmptyList.fromListUnsafe pipe Layer)
      .toList pipe NonEmptyList.fromListUnsafe

  val black = Cell(0)
  val white = Cell(1)

  def cellCombine(x: Cell, y: Cell): Cell = x match {
    case `black` => black
    case `white` => white
    case _       => y
  }

  implicit val layerSemigroup = new Semigroup[Layer] {
    override def combine(x: Layer, y: Layer): Layer =
      x.xs.zipWith(y.xs)(
        (r1, r2) => r1.xs.zipWith(r2.xs)(cellCombine) pipe Row
      ) pipe Layer
  }

  def layerPrinter(x: Layer): String =
    x.xs
      .map(
        _.xs
          .map(_.value match {
            case 1 => 'X'
            case 0 => ' '
          })
          .toList
          .mkString
      )
      .toList
      .mkString("\n")

  def decodeWithSemigroup(input: String): Layer =
    toLayers(input).reduce
}
