package advent

import scala.util.chaining._

import cats.syntax.applicative._
import cats.syntax.monoid._

import enumeratum._
import enumeratum.values.IntEnumEntry
import enumeratum.values.IntEnum
import advent.day11.PanelColour.Black
import cats.kernel.Monoid
import monocle.macros.GenLens
import cats.effect.IO
import advent.machine._
import advent.day11.Orientation.North
import advent.day11.Orientation.South
import advent.day11.Orientation.East
import advent.day11.Orientation.West
import advent.day11.PanelColour.White

object day11 {

  final case class Point(x: Int, y: Int)

  object Point {
    def empty = Point(0, 0)

    implicit val pointMonoid = Monoid.instance[Point](
      empty,
      (a, b) => Point(a.x + b.x, a.y + b.y)
    )
  }

  final case class RobotState(
    at: Point,
    facing: Orientation
  )

  object RobotState {
    def start = RobotState(Point(0, 0), North)
  }

  final case class PanelState(
    painted: Map[Point, PanelColour],
    robot: RobotState
  ) {
    def robotColour = painted.getOrElse(robot.at, Black)
  }

  object PanelState {
    def empty = PanelState(Map.empty, RobotState.start)

    val robot = GenLens[PanelState](_.robot)
  }

  sealed abstract class PanelColour(val value: Int) extends IntEnumEntry

  object PanelColour extends IntEnum[PanelColour] {

    val values = findValues

    case object Black extends PanelColour(0)
    case object White extends PanelColour(1)
  }

  sealed abstract class Direction(val value: Int) extends IntEnumEntry

  object Direction extends IntEnum[Direction] {

    val values = findValues

    case object Left extends Direction(0)
    case object Right extends Direction(1)
  }

  sealed trait Orientation extends EnumEntry

  object Orientation extends Enum[Orientation] {

    val values = findValues

    case object North extends Orientation
    case object South extends Orientation
    case object East extends Orientation
    case object West extends Orientation

    val clockwiseOrder = List(North, East, South, West)

    def rotate(direction: Direction, o: Orientation): Orientation = {
      val offset = direction match {
        case Direction.Left  => -1
        case Direction.Right => 1
      }
      clockwiseOrder.indexOf(o).pipe(i => clockwiseOrder((4 + i + offset) % 4))
    }
  }

  trait Robot {
    def paintPanel(c: PanelColour, s: PanelState): PanelState

    def turn(d: Direction, s: PanelState): PanelState

    def paintAndTurn(c: PanelColour, d: Direction): PanelState => PanelState =
      (paintPanel(c, _: PanelState))
        .andThen(turn(d, _))
  }

  object MyRobot extends Robot {
    override def paintPanel(c: PanelColour, panelState: PanelState): PanelState =
      panelState.copy(
        painted = panelState.painted.updated(panelState.robot.at, c)
      )

    override def turn(d: Direction, panelState: PanelState): PanelState = {
      val nextFacing = Orientation.rotate(d, panelState.robot.facing)
      val step = nextFacing match {
        case North => Point(0, 1)
        case South => Point(0, -1)
        case East  => Point(1, 0)
        case West  => Point(-1, 0)
      }

      PanelState.robot
        .modify(r => r.copy(at = r.at |+| step, facing = nextFacing))
        .apply(panelState)
    }
  }

  def machineRun(machine: SyncIntMachine): IO[(RunResult, SyncIntMachine)] =
    machine.run.map(result => (result, machine.copy(state = result.state)))

  // 1.Run Intcode with input 0
  // 2.outputs 2#.
  // 3.update panelstate
  // 1. give new input according to panel state, recurse until halt
  // return num painted from panel state
  def loop(machine: SyncIntMachine, panelState: PanelState): IO[PanelState] =
    for {
      (result1, machine1) <- machineRun(machine)
      (result2, machine2) <- machineRun(machine1)
      panelState <- (result1, result2) match {
        case (RunResult.Output(x1, _), RunResult.Output(x2, _)) =>
          val nextPanelState = MyRobot
            .paintAndTurn(PanelColour.withValue(x1.toInt), Direction.withValue(x2.toInt))
            .apply(panelState)
          loop(
            machine2.copy(getInput = nextPanelState.robotColour.value.pure[IO]),
            nextPanelState
          )
        case _ =>
          panelState.pure[IO]
      }
    } yield panelState

  def star1(memory: Memory): IO[Int] = {
    val machine = SyncIntMachine(Black.value.pure[IO], State(memory, Position(0), 0))
    val panelState = PanelState.empty

    loop(machine, panelState)
      .map(_.painted.size)
  }

  def star2(memory: Memory): IO[PanelState] = {
    val machine = SyncIntMachine(White.value.pure[IO], State(memory, Position(0), 0))
    val panelState = PanelState.empty.copy(painted = Map(RobotState.start.at -> White))
    loop(machine, panelState)
  }

  def printPanel(painted: Map[Point, PanelColour]) = {
    val xRange = painted.keys.pipe(points => (points.minBy(_.x).x to points.maxBy(_.x).x))
    val yRange = painted.keys.pipe(points => (points.minBy(_.y).y to points.maxBy(_.y).y))

    for {
      y <- yRange.reverse
      line = xRange
        .map(x => painted.getOrElse(Point(x, y), Black))
        .map {
          case Black => ' '
          case White => 'X'
        }
        .mkString
      _ = println(line)
    } yield ()
  }
}
