package advent.machine

import cats._
import cats.implicits._
import monocle.function.all.at
import monocle.macros.GenLens

import scala.util.chaining._
import cats.effect.IO

final case class Position(value: Long) extends AnyVal {

  def +(x: Int) = Position(value + x)
  def +(x: Long) = Position(value + x)
}

final case class Memory(value: Map[Position, Long]) {

  def update(at: Position, x: Long): Memory =
    Memory(value.updated(at, x))

  def get(at: Position): Long =
    value.getOrElse(at, 0)
}

object Memory {
  def apply(input: IndexedSeq[Long]): Memory =
    new Memory(
      input.zipWithIndex
        .map(_.map(_.toLong.pipe(Position)))
        .map(_.swap)
        .toMap
    )

  implicit val memoryMonoid: Monoid[Memory] = Monoid.instance(
    new Memory(Map.empty),
    (m1, m2) => new Memory(m1.value |+| m2.value)
  )
}

final case class State(
  memory: Memory,
  position: Position,
  relativeOffset: Long
) {

  def literalParam(index: Int, modes: ParameterModes): Long =
    modes.get(index) match {
      case ParamMode.Position | ParamMode.Immediate => memory.get(position + index)
      case ParamMode.Relative                       => memory.get(position + index) + relativeOffset
    }

  def param(index: Int, modes: ParameterModes): Long =
    literalParam(index, modes).pipe { literal =>
      modes.get(index) match {
        case ParamMode.Immediate => literal
        case _                   => memory.get(Position(literal))
      }
    }

}

object State {
  val memory = GenLens[State](_.memory.value)

  def memoryUpdate(position: Position, value: Long): State => State =
    (memory composeLens at(position)).set(Some(value))

  val position = GenLens[State](_.position)

  val relativeOffset = GenLens[State](_.relativeOffset)
}

sealed trait RunResult {
  def state: State
}

object RunResult {

  final case class Output(
    value: Long,
    state: State
  ) extends RunResult

  final case class Stop(state: State) extends RunResult
}

trait IntMachine[F[_]] {

  import Operation._

  def execute(getInput: F[Int], state: State)(implicit f: Monad[F]): F[RunResult] = {

    val instruction = Instruction.parse(state.memory.get(state.position))

    def literal(index: Int) = state.literalParam(index, instruction.paramModes)
    def param(index: Int) = state.param(index, instruction.paramModes)
    val positionInc = State.position.modify(_ + instruction.operation.params + 1)

    val stateUpdates: PartialFunction[Operation, State => State] = {
      val stateOps: PartialFunction[Operation, State => State] = {
        case Add =>
          State.memoryUpdate(Position(literal(3)), param(1) + param(2))

        case Mult =>
          State.memoryUpdate(Position(literal(3)), param(1) * param(2))

        case LessThan =>
          val store = if (param(1) < param(2)) 1L else 0L
          State.memoryUpdate(Position(literal(3)), store)

        case Equals =>
          val store = if (param(1) === param(2)) 1L else 0L
          State.memoryUpdate(Position(literal(3)), store)

        case RelativeOffset =>
          State.relativeOffset.modify(_ + param(1))
      }

      stateOps
        .andThen(_.andThen(positionInc))
        .orElse {
          case JumpIfTrue =>
            if (param(1) =!= 0) State.position.set(Position(param(2)))
            else positionInc

          case JumpIfFalse =>
            if (param(1) === 0) State.position.set(Position(param(2)))
            else positionInc
        }
    }

    val syncUpdates: PartialFunction[Operation, State => F[State]] = {
      case SaveInput =>
        state =>
          getInput.map { input =>
            State
              .memoryUpdate(Position(literal(1)), input.toLong)
              .andThen(positionInc)
              .apply(state)
          }
    }

    val resultUpdates: PartialFunction[Operation, State => RunResult] = {
      case Output =>
        positionInc
          .andThen(RunResult.Output(value = param(1), _))

      case Halt =>
        RunResult.Stop
    }

    stateUpdates
      .andThen(_.andThen(_.pure[F]))
      .orElse(syncUpdates)
      .andThen(_.andThen(_.flatMap(execute(getInput, _))))
      .orElse(resultUpdates.andThen(_.andThen(_.pure[F])))
      .apply(instruction.operation)
      .apply(state)
  }
}

object IOMachine extends IntMachine[IO]

object PureMachine extends IntMachine[Id]

final case class SyncIntMachine(getInput: IO[Int], state: State) {

  def run = IOMachine.execute(getInput, state)
}
