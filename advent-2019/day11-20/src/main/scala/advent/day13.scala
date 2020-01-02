package advent

import advent.machine._

import cats._
import cats.implicits._
import cats.kernel.Monoid
import enumeratum.values._
import advent.machine.Memory
import scala.annotation.tailrec
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.ExitCode
import cats.effect.concurrent.Ref

object day13 {

  final case class Point(x: Int, y: Int)

  object Point {
    def empty = Point(0, 0)

    implicit val pointMonoid = Monoid.instance[Point](
      empty,
      (a, b) => Point(a.x + b.x, a.y + b.y)
    )
  }

  sealed abstract class Tile(val value: Int) extends IntEnumEntry

  object Tile extends IntEnum[Tile] {

    val values = findValues

    case object Empty extends Tile(0)
    case object Wall extends Tile(1)
    case object Block extends Tile(2)
    case object HorizontalPaddle extends Tile(3)
    case object Ball extends Tile(4)
  }

  final case class Game(value: Map[Point, Tile], score: Int) {

    private def update(at: Point, tile: Tile): Game =
      this.copy(value = this.value.updated(at, tile))

    /**
      * score X=-1, Y=0, Z=score
      */
    def update(x: Int, y: Int, z: Int): Game =
      if (x == -1 && y == 0) this.copy(score = z)
      else this.update(Point(x, y), Tile.withValue(z))

  }

  final case class Machine(state: State) {
    def run = PureMachine.execute(0, state)
  }

  object Game {
    val empty: Game = Game(Map.empty, 0)
  }

  def runGame(input: IndexedSeq[Long]) = game(Memory(input))

  def game(memory: Memory): Game = {
    @tailrec
    def loop(machine: Machine, game: Game, out: List[Int]): Game =
      if (out.size == 3)
        loop(
          machine,
          game.update(out(0), out(1), out(2)),
          Nil
        )
      else
        machine.run match {
          case RunResult.Output(value, state) =>
            loop(machine.copy(state = state), game, out :+ value.toInt)
          case _ => game
        }

    loop(Machine(State(memory, Position(0), 0)), Game.empty, Nil)
  }

  def robotInput(ref: Ref[IO, Game]): IO[Int] =
    ref.get.map { game =>
      game.value
        .find(_._2 == Tile.Ball)
        .map2(
          game.value.find(_._2 == Tile.HorizontalPaddle)
        ) { (ball, paddle) =>
          ball._1.x.compare(paddle._1.x)
        }
        .getOrElse(0)
    }

  final case class JoystickMachine(state: State, ref: Ref[IO, Game]) {
    def run: IO[RunResult] = IOMachine.execute(robotInput(ref), state)
  }

  /**
    * If the joystick is in the neutral position, provide 0.
    * If the joystick is tilted to the left, provide -1.
    * If the joystick is tilted to the right, provide 1
    */
  def star2(input: IndexedSeq[Long]) = {
    val memory = Memory(input).update(Position(0), 2)

    def loop(
      machine: JoystickMachine,
      gameRef: Ref[IO, Game],
      out: List[Int]
    ): IO[Game] =
      if (out.size == 3)
        gameRef.update(_.update(out(0), out(1), out(2))) >>
          loop(
            machine,
            gameRef,
            Nil
          )
      else {
        gameRef.get.flatMap { game =>
          // printGame(game) >>
          machine.run.flatMap {
            case RunResult.Output(value, state) =>
              loop(
                machine.copy(state = state),
                gameRef,
                out :+ value.toInt
              )
            case _ =>
              IO.pure(game)
          }
        }
      }

    Ref[IO].of(Game.empty).flatMap { ref =>
      loop(JoystickMachine(State(memory, Position(0), 0), ref), ref, Nil)
    }
  }

  import Tile._
  def printGame(game: Game): IO[Unit] =
    if (game.value.isEmpty) IO.unit
    else
      IO {
        println(s"Score: ${game.score}")

        val yMax = game.value.maxBy(_._1.y)._1.y
        val xMax = game.value.maxBy(_._1.x)._1.x

        for {
          y <- 0 to yMax
          line = (0 to xMax)
            .map(
              x =>
                game.value.getOrElse(Point(x, y), Empty) match {
                  case HorizontalPaddle => "P"
                  case Wall             => "|"
                  case Empty            => " "
                  case Ball             => "o"
                  case Block            => "X"
                }
            )
            .mkString
        } yield println(line)
      }.as(())

}
