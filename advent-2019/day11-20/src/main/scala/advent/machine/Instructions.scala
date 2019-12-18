package advent.machine

import enumeratum.values.IntEnum
import enumeratum.values.IntEnumEntry

sealed abstract class ParamMode(val value: Int) extends IntEnumEntry

object ParamMode extends IntEnum[ParamMode] {

  val values = findValues

  case object Position extends ParamMode(0)
  case object Immediate extends ParamMode(1)
  case object Relative extends ParamMode(2)
}

sealed abstract class Operation(val value: Int, val params: Int) extends IntEnumEntry

object Operation extends IntEnum[Operation] {

  val values = findValues

  case object Add extends Operation(1, params = 3)
  case object Mult extends Operation(2, params = 3)
  case object SaveInput extends Operation(3, params = 1)
  case object Output extends Operation(4, params = 1)
  case object JumpIfTrue extends Operation(5, params = 2)
  case object JumpIfFalse extends Operation(6, params = 2)
  case object LessThan extends Operation(7, params = 3)
  case object Equals extends Operation(8, params = 3)
  case object RelativeOffset extends Operation(9, params = 1)
  case object Halt extends Operation(99, params = 0)
}

final case class ParameterModes(value: Map[Int, ParamMode]) {

  def get(index: Int): ParamMode =
    value.getOrElse(index, ParamMode.Position)
}

object ParameterModes {

  def parse(paramInstr: String): ParameterModes =
    ParameterModes(
      paramInstr.reverse
        .map(_.asDigit)
        .map(ParamMode.withValue)
        .zip(1 to paramInstr.length)
        .map(_.swap)
        .toMap
    )
}

// renamed to operation from value
final case class Instruction(operation: Operation, paramModes: ParameterModes)

object Instruction {

  def parse(value: Long): Instruction = {
    val valueString = value.toString
    val (paramInstr, instrStr) = valueString.splitAt(valueString.length - 2)
    Instruction(
      Operation.withValue(instrStr.toInt),
      ParameterModes.parse(paramInstr)
    )
  }
}
