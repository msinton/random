package advent

import Instr._

import cats.implicits._
import scala.annotation.tailrec

sealed trait RunResult
object RunResult {
  final case class Output(value: Int, memory: IndexedSeq[Int], position: Position) extends RunResult
  object Stop extends RunResult
}

import IntCodeMachine._

final case class IntCodeMachine(memory: IndexedSeq[Int], position: Position) {

  def runWith(inputs: List[Int]): RunResult = {

    @tailrec
    def loop(
      at: Position,
      memory: IndexedSeq[Int],
      inputs: List[Int]
    ): RunResult = {
      val instruction = parseInstr(memory(at.value))

      applyInstr(instruction, at, memory, inputs) match {
        case Left(value) => value
        case Right((next, nextInputs, position)) =>
          loop(position, next, nextInputs)
      }
    }

    loop(position, memory, inputs)
  }
}

final case class ConnectedMachines(xs: List[IntCodeMachine]) {

  def runFeedback(phases: List[Int]): Int = {
    // println(phases)

    def initialRun(index: Int, input: Int) =
      xs(index).runWith(List(phases(index), input))

    @tailrec
    def initialLoop(
      index: Int,
      input: Int,
      machines: List[IntCodeMachine]
    ): (Int, List[IntCodeMachine]) =
      if (index >= machines.length) (input, machines)
      else
        initialRun(index, input) match {
          case RunResult.Output(value, memory, position) =>
            initialLoop(index + 1, value, machines.updated(index, IntCodeMachine(memory, position)))
          case RunResult.Stop => throw new Exception("program error")
        }

    @tailrec
    def mainLoop(
      index: Int,
      input: Int,
      xs: List[IntCodeMachine]
    ): Int =
      xs(index).runWith(List(input)) match {
        case RunResult.Output(value, memory, position) =>
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

object IntCodeMachine {

  def parseParamMode(paramInstr: String): Map[Int, ParamMode] =
    paramInstr.reverse
      .map(_.asDigit)
      .map(ParamMode.withValue)
      .zip(1 to paramInstr.length)
      .map(_.swap)
      .toMap

  def getParam(memory: IndexedSeq[Int], at: Position, mode: ParamMode): Int =
    mode match {
      case ParamMode.Immediate => memory(at.value)
      case ParamMode.Position  => memory(memory(at.value))
    }

  def parseInstr(value: Int): Instruction = {
    val valueString = value.toString
    val (paramInstr, instrStr) = valueString.splitAt(valueString.length - 2)
    Instruction(
      Instr.withValue(instrStr.toInt),
      parseParamMode(paramInstr)
    )
  }

  def incPosition(instr: Instruction, position: Position): Position =
    Position(position.value + instr.value.params + 1)

  def applyInstr(
    instr: Instruction,
    position: Position,
    memory: IndexedSeq[Int],
    inputs: List[Int]
  ): Either[RunResult, (IndexedSeq[Int], List[Int], Position)] = {

    val at = position.value

    def continue(
      next: IndexedSeq[Int],
      newAt: Position = incPosition(instr, position),
      inputs: List[Int] = inputs
    ) =
      (next, inputs, newAt).asRight[RunResult]

    def paramFetch(relativeIndex: Int) =
      getParam(
        memory,
        Position(at + relativeIndex),
        instr.paramModes.getOrElse(relativeIndex, ParamMode.Position)
      )

    instr.value match {
      case Add =>
        continue(memory.updated(memory(at + 3), paramFetch(1) + paramFetch(2)))

      case Mult =>
        continue(memory.updated(memory(at + 3), paramFetch(1) * paramFetch(2)))

      case SaveInput =>
        continue(memory.updated(memory(at + 1), inputs.head), inputs = inputs.tail)

      case Output =>
        RunResult
          .Output(value = paramFetch(1), memory = memory, incPosition(instr, position))
          .asLeft

      case JumpIfTrue =>
        val first = paramFetch(1)
        if (first =!= 0) continue(memory, newAt = Position(paramFetch(2)))
        else continue(memory)

      case JumpIfFalse =>
        val first = paramFetch(1)
        if (first === 0) continue(memory, newAt = Position(paramFetch(2)))
        else continue(memory)

      case LessThan =>
        val first = paramFetch(1)
        val second = paramFetch(2)
        val store = if (first < second) 1 else 0
        continue(memory.updated(memory(at + 3), store))

      case Equals =>
        val first = paramFetch(1)
        val second = paramFetch(2)
        val store = if (first === second) 1 else 0
        continue(memory.updated(memory(at + 3), store))

      case Halt =>
        RunResult.Stop.asLeft
    }
  }

}
