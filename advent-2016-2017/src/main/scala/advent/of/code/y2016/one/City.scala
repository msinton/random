package advent.of.code.y2016.one

import scala.collection.mutable

object City {

  sealed trait Orientation

  case object Up extends Orientation

  case object Down extends Orientation

  case object Left extends Orientation

  case object Right extends Orientation

  case class Point(x: Int, y: Int)

  case class Position(point: Point, orientation: Orientation) {

    def moveForward(steps: Int): Position = {
      orientation match {
        case Up =>    Position(Point(point.x, point.y + steps), orientation)
        case Right => Position(Point(point.x + steps, point.y), orientation)
        case Down =>  Position(Point(point.x, point.y - steps), orientation)
        case Left =>  Position(Point(point.x - steps, point.y), orientation)
      }
    }

    def move(steps: Int, rotateRight: Boolean): Position = {
      orientation match {
        case Up if rotateRight => Position(Point(point.x + steps, point.y), Right)
        case Up =>                Position(Point(point.x - steps, point.y), Left)
        case Right if rotateRight => Position(Point(point.x, point.y - steps), Down)
        case Right =>               Position(Point(point.x, point.y + steps), Up)
        case Down if rotateRight => Position(Point(point.x - steps, point.y), Left)
        case Down =>                Position(Point(point.x + steps, point.y), Right)
        case Left if rotateRight => Position(Point(point.x, point.y + steps), Up)
        case Left =>                Position(Point(point.x, point.y - steps), Down)
      }
    }

    def allMoves(steps: Int, rotateRight: Boolean): (Seq[Point], Position) = {
      val rotated = move(0, rotateRight)
      val positions = (1 to steps).map(rotated.moveForward)
      (positions.map(_.point), positions.last)
    }
  }

  val leftMatch = "L(.*)".r
  val rightMatch = "R(.*)".r

  def addDirection(d: String, from: Position): Position = {
    d match {
      case leftMatch(n) => from.move(n.toInt, rotateRight = false)
      case rightMatch(n) => from.move(n.toInt, rotateRight = true)
    }
  }

  def addDirections(d: String, from: Position): (Seq[Point], Position) = {
    d match {
      case leftMatch(n) => from.allMoves(n.toInt, rotateRight = false)
      case rightMatch(n) => from.allMoves(n.toInt, rotateRight = true)
    }
  }

  def coordinate(directions: List[String]): Int = {

    val Point(x, y) = directions.foldLeft(Position(Point(0, 0), Up))((p, direction) => addDirection(direction, p)).point

    Math.abs(x) + Math.abs(y)
  }

  def firstCoordVisitedTwice(directions: List[String]): Option[Point] = {

    var position = Position(Point(0, 0), Up)
    val set = mutable.Set[Point]()

    for (direction <- directions) {
      val (points, p) = addDirections(direction, position)
      position = p
      for (point <- points) {
        if (!set.add(point)) return Option(point)
      }
    }
    None
  }
}
