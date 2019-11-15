package advent.of.code.y2017

object Day17 {

  val stepSize = 335

  private var arr = 1

  private var head = 0

  def moveHead(steps: Int): Unit = {
    head = (head + steps) % arr
  }

  def next(n: Int): Unit = {
    moveHead(stepSize)
    arr += 1
    if (head == 0) println(n, " - ")
    moveHead(1)
  }

  def apply(turns: Int) = {
    (1 to turns).foreach(next)
  }
}
