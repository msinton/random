package advent

import cats.data._
import cats.implicits._

object day7 {

  def maxThrusterOutput(memory: IndexedSeq[Int]) = {
    val range = (0 to 4).toSet

    (for { // TODO simplify
      phase1 <- range
      phase2 <- range
      phase3 <- range
      phase4 <- range
      phase5 <- range
      phases = List(phase1, phase2, phase3, phase4, phase5)
      if phases.toSet.size == range.size
    } yield phases)
      .map(
        _.foldLeft(0)((input, phase) => thrusterOutput(memory, input, phase))
      )
      .max
  }

  def star2(memory: IndexedSeq[Int]) = {

    val range = (5L to 9L).toList
    val machines = ConnectedMachines(List.fill(range.size)(IntCodeMachine(memory, Position(0))))

    (for {
      phase1 <- range
      phase2 <- range
      phase3 <- range
      phase4 <- range
      phase5 <- range
      phases = List(phase1, phase2, phase3, phase4, phase5)
      if phases.toSet.size == range.size
    } yield phases)
      .map(machines.runFeedback)
      .max
  }

  // TODO
  val nextPhase = State[List[Int], Int](s => (s.tail :+ s.head, s.head))

  def without(xs: List[Int]): State[List[Int], Int] =
    nextPhase
      .flatMap(e => if (xs.contains(e)) without(xs) else State.pure(e))

  def doit(from: Int, end: Int, num: Int) = {

    val initialStates = List.fill(num)(from to end).map(_.toList)

    def run[A](inputStates: List[List[Int]], f: List[Int] => A, results: List[A]): List[A] = {
      val (nextStates, result) = (0 until num)
        .foldLeft((List.empty[List[Int]], List.empty[Int])) {
          case ((states, values), i) =>
            println(s"ss $values, $i")
            without(Nil).run(inputStates(i)).value.bimap(_ :: states, _ :: values)
        }
        .map(f)

      val nextResults = result :: results
      if (nextStates == initialStates) nextResults else run(nextStates, f, nextResults)
    }

    run(initialStates, println, List.empty)
  }

  def thrusterOutput(memory: IndexedSeq[Int], input: Int, phase: Int): Int =
    IntCodeMachine(memory, Position(0)).runWith(List(phase.toLong, input.toLong)) match {
      case x: RunResult.Output =>
        x.value.toInt
      case RunResult.Stop => throw new Exception("program error")
    }

}
