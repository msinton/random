package advent

import cats.implicits._
import scala.annotation.tailrec

object day5 {

  import Instr._

  final case class Position(value: Int)

  def parseParamMode(paramInstr: String): Map[Int, ParamMode] =
    paramInstr.reverse
      .map(_.asDigit)
      .map(ParamMode.withValue)
      .zip(1 to paramInstr.length)
      .map(_.swap)
      .toMap

  def getParam(memory: IndexedSeq[Int], at: Position, mode: ParamMode, relativeBase: Int): Int =
    mode match {
      case ParamMode.Immediate => memory(at.value)
      case ParamMode.Position  => memory(memory(at.value))
      case ParamMode.Relative  => memory(memory(at.value + relativeBase))
    }

  def parseInstr(value: Int): Instruction = {
    val valueString = value.toString
    val (paramInstr, instrStr) = valueString.splitAt(valueString.length - 2)
    Instruction(
      Instr.withValue(instrStr.toInt),
      parseParamMode(paramInstr)
    )
  }

  def applyInstr(
    instr: Instruction,
    position: Position,
    memory: IndexedSeq[Int],
    inputs: List[Int],
    outputs: List[Int]
  ): Either[Int, (IndexedSeq[Int], List[Int], Position, List[Int])] = {

    val at = position.value

    def continue(
      next: IndexedSeq[Int],
      newAt: Int = at + instr.value.params + 1,
      inputs: List[Int] = inputs,
      outputs: List[Int] = outputs
    ) =
      (next, inputs, Position(newAt), outputs).asRight[Int]

    def paramFetch(relativeIndex: Int) =
      getParam(
        memory,
        Position(at + relativeIndex),
        instr.paramModes.getOrElse(relativeIndex, ParamMode.Position),
        0
      )

    instr.value match {
      case Add =>
        continue(memory.updated(memory(at + 3), paramFetch(1) + paramFetch(2)))

      case Mult =>
        continue(memory.updated(memory(at + 3), paramFetch(1) * paramFetch(2)))

      case SaveInput =>
        continue(memory.updated(memory(at + 1), inputs.head), inputs = inputs.tail)

      case Output =>
        continue(memory, outputs = outputs :+ paramFetch(1))

      case JumpIfTrue =>
        val first = paramFetch(1)
        if (first =!= 0) continue(memory, newAt = paramFetch(2))
        else continue(memory)

      case JumpIfFalse =>
        val first = paramFetch(1)
        if (first === 0) continue(memory, newAt = paramFetch(2))
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

      case RelativeBase => ???

      case Halt =>
        outputs.last.asLeft
    }
  }

  def run(memory: IndexedSeq[Int], inputs: List[Int]): Int = {
    @tailrec
    def loop(at: Position, memory: IndexedSeq[Int], inputs: List[Int], outputs: List[Int]): Int = {
      val instruction = parseInstr(memory(at.value))

      applyInstr(instruction, at, memory, inputs, outputs) match {
        case Left(value) => value
        case Right((next, nextInputs, position, nextOutputs)) =>
          loop(position, next, nextInputs, nextOutputs)
      }
    }
    loop(Position(0), memory, inputs, List.empty)
  }

  def diagnostic(memory: IndexedSeq[Int], input: Int): Int =
    run(memory, List(input))

}
