package advent.of.code.y2017

import scala.collection.immutable.Queue

object Day18 {

  var sentCount = 0L

  case class State(
                    registers: Map[String, Long] = Map.empty[String, Long].withDefaultValue(0L),
                    waiting: Boolean = false,
                    jumpTo: Option[Int] = None,
                    queue: Queue[Long] = Queue.empty,
                  ) {
  }


  sealed trait Command {
    def execute(s: State, otherState: State): (State, State)
  }

  trait IsolatedCommand extends Command {
    def executeOne(s: State): State
    def execute(s: State, otherState: State): (State, State) = (executeOne(s), otherState)
  }

  case class Set(register: String, x: Long) extends IsolatedCommand {
    override def executeOne(s: State): State = {
      val registers = s.registers + (register -> x)
      State(registers, queue = s.queue)
    }
  }
  case class SetAs(register: String, other: String) extends IsolatedCommand {
    override def executeOne(s: State): State = {
      val registers = s.registers + (register -> s.registers.getOrElse(other, 0L))
      State(registers, queue = s.queue)
    }
  }

  case class Add(register: String, x: Long) extends IsolatedCommand {
    override def executeOne(s: State): State =
      State(s.registers + (register -> (s.registers(register) + x)), queue = s.queue)
  }

  case class AddAs(register: String, other: String) extends IsolatedCommand {
    override def executeOne(s: State): State =
      State(s.registers + (register -> (s.registers(register) + s.registers(other))), queue = s.queue)
  }

  case class Multiply(register: String, x: Long) extends IsolatedCommand {
    override def executeOne(s: State): State =
      State(s.registers + (register -> (s.registers(register) * x)), queue = s.queue)
  }

  case class MultiplyAs(register: String, other: String) extends IsolatedCommand {
    override def executeOne(s: State): State =
      State(s.registers + (register -> (s.registers(register) * s.registers(other))), queue = s.queue)
  }

  case class Modulo(register: String, x: Long) extends IsolatedCommand {
    override def executeOne(s: State): State = {
      val r = s.registers(register)
      val newVal = if (x == 0) 0 else r % x
      State(s.registers + (register -> newVal), queue = s.queue)
    }
  }

  case class ModuloAs(register: String, other: String) extends IsolatedCommand {
    override def executeOne(s: State): State = {
      val r = s.registers(register)
      val x = s.registers(other)
      val newVal = if (x == 0) 0 else r % x
      State(s.registers + (register -> newVal), queue = s.queue)
    }
  }

  case class Jump(register: String, x: Int) extends IsolatedCommand {
    override def executeOne(s: State): State =
      if (s.registers.getOrElse(register, 0L) <= 0)
        s
      else
        State(s.registers, jumpTo = Option(x), queue = s.queue)
  }

  case class JumpAs(register: String, other: String) extends IsolatedCommand {
    override def executeOne(s: State): State =
      if (s.registers.getOrElse(register, 0L) <= 0)
        s
      else
        State(s.registers, jumpTo = s.registers.get(other).map(_.toInt), queue = s.queue)
  }

  case class JumpR(r: Int, x: Int) extends IsolatedCommand {
    override def executeOne(s: State): State =
      if (r <= 0)
        s
      else
        State(s.registers, jumpTo = Option(x), queue = s.queue)
  }

  case class Receive(register: String) extends IsolatedCommand {
    override def executeOne(s: State): State =
      if (s.queue.isEmpty) {
//        println("waiting")
        State(s.registers, waiting = true, queue = s.queue, jumpTo = Some(0))
      } else {
        val (v, q) = s.queue.dequeue
//        println("got value " + v)
        State(s.registers + (register -> v), queue = q)
      }
  }

  case class Send(x: Long) extends Command {
    override def execute(s: State, otherState: State): (State, State) = {
      (State(s.registers, queue = s.queue),
        State(otherState.registers, queue = otherState.queue.enqueue(x), waiting = otherState.waiting, jumpTo = otherState.jumpTo))
    }
  }

  case class SendAs(register: String) extends Command {
    override def execute(s: State, otherState: State): (State, State) = {
      (State(s.registers, queue = s.queue),
        State(otherState.registers, queue = otherState.queue.enqueue(s.registers(register)), waiting = otherState.waiting, jumpTo = otherState.jumpTo))
    }
  }


  def parse(lines: Seq[String]): Seq[Command] = {

    val setReg = """set (.+) (.+)""".r
    val addReg = """add (.+) (.+)""".r
    val multiplyReg = """mul (.+) (.+)""".r
    val moduloReg = """mod (.+) (.+)""".r
    val jumpReg = """jgz (.+) (.+)""".r
    val recoverReg = """rcv (.+)""".r
    val soundReg = """snd (.+)""".r

    val digits = """-*\d+"""

    lines.map {
      case setReg(x, y) if y.matches(digits) => Set(x, y.toLong)
      case setReg(x, y) => SetAs(x, y)
      case addReg(x, y) if y.matches(digits) => Add(x, y.toLong)
      case addReg(x, y) => AddAs(x, y)
      case multiplyReg(x, y) if y.matches(digits) => Multiply(x, y.toLong)
      case multiplyReg(x, y) => MultiplyAs(x, y)
      case moduloReg(x, y) if y.matches(digits) => Modulo(x, y.toLong)
      case moduloReg(x, y) => ModuloAs(x, y)
      case jumpReg(x, y) if x.matches(digits) => JumpR(x.toInt, y.toInt)
      case jumpReg(x, y) if y.matches(digits) => Jump(x, y.toInt)
      case jumpReg(x, y) => JumpAs(x, y)
      case recoverReg(x) => Receive(x)
      case soundReg(x) if x.matches(digits) => Send(x.toLong)
      case soundReg(x) => SendAs(x)
    }
  }

  def countSentBy(aCommand: Command): Unit = {
    aCommand match {
      case _: Send => sentCount += 1
      case _: SendAs => sentCount += 1
      case _ =>
    }
  }

  def apply(commands: IndexedSeq[Command]): Long = {

    var i = 0
    var j = 0
    var stateA = State(Map("p" -> 0))
    var stateB = State(Map("p" -> 1))
    var aTerminated = false
    var bTerminated = false

    while (!(stateA.waiting && stateB.waiting) && (!aTerminated && !bTerminated)) {

      if (i < commands.size && i >= 0) {
        val next = commands(i).execute(stateA, stateB)
        stateA = next._1
        stateB = next._2
      } else {
        aTerminated = true
      }

      if (j < commands.size && j >= 0) {
        countSentBy(commands(j))

        val next = commands(j).execute(stateB, stateA)
        stateB = next._1
        stateA = next._2
      } else {
        bTerminated = true
      }
//
//      println("a", i, commands(i), stateA)
//      println("b", j, commands(j), stateB)

      i += stateA.jumpTo.getOrElse(1)
      j += stateB.jumpTo.getOrElse(1)
    }

    sentCount
  }
}
