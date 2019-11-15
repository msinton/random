package advent.of.code.y2017

import scala.annotation.tailrec

object Day15 {

  val divisor = 2147483647

  val mask = Math.pow(2, 16).intValue()

  case class Generator(initial: Int, multiplier: Int, factorCondition: Int) {

    var current: Long = initial

    def doStep(): Unit = {
      current = (current * multiplier) % divisor
    }

    def step(): Long = {
      doStep()
      while (current % factorCondition != 0) {
        doStep()
      }
      current
    }
  }

  def isMatch(a: Long, b: Long): Boolean = {
    a % mask == b % mask
  }

  def process(genA: Generator, genB: Generator, numSteps: Int): Int = {

    @tailrec
    def loop(counter: Int, matches: Int): Int = {
      if (counter == 0)
        matches
      else {
        val nextMatches = if (isMatch(genA.step(), genB.step())) matches + 1 else matches
        loop(counter - 1, nextMatches)
      }
    }

    loop(numSteps, 0)
  }

  def apply(): Int = {
    process(Generator(783, 16807, 4), Generator(325, 48271, 8), 5*1000000)
  }

}
