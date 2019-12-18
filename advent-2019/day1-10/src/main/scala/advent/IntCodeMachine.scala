package advent

import cats.implicits._
import monocle.function.all._
import monocle.macros.GenLens
import enumeratum.values.IntEnum
import enumeratum.values.IntEnumEntry

import scala.annotation.tailrec
import scala.util.chaining._

sealed abstract class ParamMode(val value: Int) extends IntEnumEntry
object ParamMode extends IntEnum[ParamMode] {

  val values = findValues

  case object Position extends ParamMode(0)
  case object Immediate extends ParamMode(1)
  case object Relative extends ParamMode(2)
}

sealed abstract class Instr(val value: Int, val params: Int) extends IntEnumEntry

object Instr extends IntEnum[Instr] {

  val values = findValues

  case object Add extends Instr(1, params = 3)
  case object Mult extends Instr(2, params = 3)
  case object SaveInput extends Instr(3, params = 1)
  case object Output extends Instr(4, params = 1)
  case object JumpIfTrue extends Instr(5, params = 2)
  case object JumpIfFalse extends Instr(6, params = 2)
  case object LessThan extends Instr(7, params = 3)
  case object Equals extends Instr(8, params = 3)
  case object RelativeBase extends Instr(9, params = 1)
  case object Halt extends Instr(99, params = 0)
}

final case class Instruction(value: Instr, paramModes: Map[Int, ParamMode])

final case class Position(value: Long) extends AnyVal {

  def +(x: Int) = Position(value + x)
  def +(x: Long) = Position(value + x)
}

sealed trait RunResult
object RunResult {
  final case class Output(value: Long, memory: Memory, position: Position, relativeBase: Long)
      extends RunResult
  object Stop extends RunResult
}

final case class Memory(value: Map[Position, Long])

final case class IntCodeMachine(memory: Memory, position: Position) {

  import IntCodeMachine._

  def runWith(inputs: List[Long]): RunResult = {

    @tailrec
    def loop(
      prevState: State
    ): RunResult = {
      val instruction = parseInstr(memory.value(prevState.position))

      applyInstr(instruction, prevState) match {
        case Left(value) =>
          println(s"Run output $value")
          value
        case Right(nextState) => loop(nextState)
      }
    }

    loop(State(memory, position, inputs, 0))
  }

  def runToEnd(inputs: List[Long]): List[Long] = {
    @tailrec
    def loop(
      prevState: State,
      outputs: List[Long]
    ): List[Long] = {
      val instruction = parseInstr(memory.value(prevState.position))
      // println(instruction.toString + s" at ${prevState.position}")
      // println(prevState.inputs)

      applyInstr(instruction, prevState) match {
        case Left(RunResult.Output(value, memory, position, relativeBase)) =>
          loop(
            prevState.copy(memory = memory, position = position, relativeBase = relativeBase),
            outputs :+ value
          )
        case Left(RunResult.Stop) => outputs
        case Right(nextState)     => loop(nextState, outputs)
      }
    }

    loop(State(memory, position, inputs, 0), Nil)
  }
}

final case class ConnectedMachines(xs: List[IntCodeMachine]) {

  def runFeedback(phases: List[Long]): Long = {

    def initialRun(index: Int, input: Long) =
      xs(index).runWith(List(phases(index), input))

    @tailrec
    def initialLoop(
      index: Int,
      input: Long,
      machines: List[IntCodeMachine]
    ): (Long, List[IntCodeMachine]) =
      if (index >= machines.length) (input, machines)
      else
        initialRun(index, input) match {
          case RunResult.Output(value, memory, position, _) => // TODO relative
            initialLoop(index + 1, value, machines.updated(index, IntCodeMachine(memory, position)))
          case RunResult.Stop => throw new Exception("program error")
        }

    @tailrec
    def mainLoop(
      index: Int,
      input: Long,
      xs: List[IntCodeMachine]
    ): Long =
      xs(index).runWith(List(input)) match {
        case RunResult.Output(value, memory, position, _) => // TODO relative
          mainLoop(
            (index + 1) % xs.size,
            value,
            xs.updated(index, IntCodeMachine(memory, position))
          )
        case RunResult.Stop =>
          //   println(s"$input Halting on index $index")
          input
      }

    val (output, machines) = initialLoop(0, 0, xs)

    mainLoop(0, output, machines)
  }
}

final case class State(
  memory: Memory,
  position: Position,
  inputs: List[Long],
  relativeBase: Long
)

object State {
  val memory = GenLens[State](_.memory.value)

  def mem(position: Position, value: Long): State => State =
    (memory composeLens at(position)).set(Some(value))

  val inputs = GenLens[State](_.inputs)

  def position = GenLens[State](_.position)
}

object Memory {
  def on(position: Position): Memory => Long =
    _.value.getOrElse(position, 0)

  def relative(position: Position): Memory => Long =
    memory => on(position)(memory).pipe(Position).pipe(on)(memory)

  def relativeBase(position: Position, offset: Long): Memory => Long =
    memory => on(position)(memory).pipe(Position(_) + offset).pipe(on)(memory)

}

object IntCodeMachine {

  def apply(memory: IndexedSeq[Int], position: Position): IntCodeMachine =
    new IntCodeMachine(
      Memory(
        memory.zipWithIndex
          .map(_.bimap(_.toLong, _.toLong))
          .map(_.swap)
          .map(_.leftMap(Position))
          .toMap
      ),
      position
    )

  def apply(memory: IndexedSeq[Long]): IntCodeMachine =
    new IntCodeMachine(
      Memory(
        memory.zipWithIndex
          .map(_.map(_.toLong.pipe(Position)))
          .map(_.swap)
          .toMap
      ),
      Position(0)
    )

  def parseParamMode(paramInstr: String): Map[Int, ParamMode] =
    paramInstr.reverse
      .map(_.asDigit)
      .map(ParamMode.withValue)
      .zip(1 to paramInstr.length)
      .map(_.swap)
      .toMap

  def getParam(memory: Memory, at: Position, mode: ParamMode, relativeBase: Long): Long =
    (mode match {
      case ParamMode.Immediate => Memory.on(at)
      case ParamMode.Position  => Memory.relative(at)
      case ParamMode.Relative  => Memory.relativeBase(at, relativeBase)
    })(memory)

  def getLiteralParam(memory: Memory, at: Position, mode: ParamMode, relativeBase: Long): Position =
    (mode match {
      case ParamMode.Immediate | ParamMode.Position => Memory.on(at)(memory)
      case ParamMode.Relative                       => Memory.on(at)(memory) + relativeBase
    }).pipe(Position)

  def parseInstr(value: Long): Instruction = {
    val valueString = value.toString
    val (paramInstr, instrStr) = valueString.splitAt(valueString.length - 2)
    Instruction(
      Instr.withValue(instrStr.toInt),
      parseParamMode(paramInstr)
    )
  }

  import Instr._

  def applyInstr(
    instr: Instruction,
    state: State
  ): Either[RunResult, State] = {

    // println(state)
    val State(memory, position, inputs, relativeBase) = state

    val positionInc = State.position.modify(_ + instr.value.params + 1)

    def continue(
      next: Memory = memory,
      newAt: Position = positionInc(state).position,
      inputs: List[Long] = inputs
    ) =
      State(next, newAt, inputs, relativeBase).asRight[RunResult]

    def paramFetch(paramIndex: Int) =
      getParam(
        memory,
        position + paramIndex,
        instr.paramModes.getOrElse(paramIndex, ParamMode.Position),
        relativeBase
      )

    def literal(paramIndex: Int) = getLiteralParam(
      memory,
      position + paramIndex,
      instr.paramModes.getOrElse(paramIndex, ParamMode.Position),
      relativeBase
    )

    instr.value match {
      case Add =>
        State
          .mem(literal(3), paramFetch(1) + paramFetch(2))
          .andThen(positionInc)
          .apply(state)
          .asRight

      case Mult =>
        State
          .mem(literal(3), paramFetch(1) * paramFetch(2))
          .andThen(positionInc)
          .apply(state)
          .asRight

      case SaveInput =>
        State
          .mem(literal(1), inputs.head)
          .andThen(positionInc)
          .andThen(State.inputs.set(inputs.tail))
          .apply(state)
          .asRight

      case Output =>
        RunResult
          .Output(value = paramFetch(1), memory = memory, positionInc(state).position, relativeBase)
          .asLeft

      case JumpIfTrue =>
        val first = paramFetch(1)
        if (first =!= 0) continue(newAt = Position(paramFetch(2)))
        else continue()

      case JumpIfFalse =>
        val first = paramFetch(1)
        if (first === 0) continue(newAt = Position(paramFetch(2)))
        else continue()

      case LessThan =>
        val first = paramFetch(1)
        val second = paramFetch(2)
        val store = if (first < second) 1L else 0L
        State
          .mem(literal(3), store)
          .andThen(positionInc)
          .apply(state)
          .asRight

      case Equals =>
        val first = paramFetch(1)
        val second = paramFetch(2)
        val store = if (first === second) 1L else 0L
        State
          .mem(literal(3), store)
          .andThen(positionInc)
          .apply(state)
          .asRight

      case RelativeBase =>
        val baseInc = paramFetch(1)
        positionInc(state.copy(relativeBase = relativeBase + baseInc)).asRight

      case Halt =>
        RunResult.Stop.asLeft
    }
  }

}
