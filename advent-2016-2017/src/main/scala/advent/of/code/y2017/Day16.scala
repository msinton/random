package advent.of.code.y2017

import scala.collection.mutable

object Day16 {

  sealed trait Command

  case class Spin(n: Int) extends Command

  case class Exchange(a: Int, b: Int) extends Command

  case class Partner(a: Char, b: Char) extends Command

  case class DosyDo(start: Char, rest: List[Char]) extends Command

  val startSequence = 'a' to 'p'


  def parse(input: String): Seq[Command] = {

    val spinRegex = """s(.+)""".r
    val exchangeRegex = """x(.+)\/(.+)""".r
    val partnerRegex = """p(.)\/(.)""".r
    val dosyDoRegex = """(.)->(.*)""".r

    input.split(",").map({
      case spinRegex(n) => Spin(n.toInt)
      case exchangeRegex(a, b) => Exchange(a.toInt, b.toInt)
      case partnerRegex(a, b) => Partner(a.head, b.head)
      case dosyDoRegex(start, rest) => DosyDo(start.head, rest.toCharArray.toList)
    })
  }

  def replaceChar(i: Int, c: Char, sequence: mutable.Buffer[Char]): mutable.Buffer[Char] = {
    sequence(i) = c
    sequence
  }

  def processCommand(command: Command, sequence: mutable.Buffer[Char]): mutable.Buffer[Char] = {
    command match {
      case Spin(n) =>
        val (end, beginning) = sequence.splitAt(sequence.length - n)
        beginning ++ end

      case Exchange(i, j) =>
        val atI = sequence(i)
        val atJ = sequence(j)

        sequence(i) = atJ
        sequence(j) = atI
        sequence

      case Partner(a, b) =>
        val i = sequence.indexOf(a)
        val j = sequence.indexOf(b)
        replaceChar(i, b, sequence)
        replaceChar(j, a, sequence)

      case DosyDo(start, rest) =>
        val i = sequence.indexOf(start)
        val restIndexes = rest.map(sequence.indexOf(_))
        ((restIndexes ++ Seq(i)) zip (start :: rest)).foldLeft(sequence){case (acc, (ind, c)) => replaceChar(ind, c, acc)}
    }
  }

  def apply(initial: mutable.Buffer[Char], commands: Seq[Command]): String = {
    commands.foldLeft(initial)((nextSeq, command) => processCommand(command, nextSeq)).mkString
  }
}
