package advent

object day2 {

  def op(f: (Int, Int) => Int, at: Int, current: IndexedSeq[Int]) = {
    val x = current(current(at + 1))
    val y = current(current(at + 2))
    current.updated(current(at + 3), f(x, y))
  }

  def program(input: IndexedSeq[Int]): IndexedSeq[Int] = {

    def loop(at: Int, current: IndexedSeq[Int]): IndexedSeq[Int] =
      current(at) match {
        case 1 =>
          val next = op(_ + _, at, current)
          loop(at + 4, next)
        case 2 =>
          val next = op(_ * _, at, current)
          loop(at + 4, next)
        case 99 => current
      }

    loop(0, input)
  }

  def targetSearch(input: IndexedSeq[Int], target: Int): Int = {
    def loop(i: Int, j: Int): Int = {
      val runResult = program(input.updated(1, i).updated(2, j))(0)
      if (runResult == target) 100 * i + j
      else if (i == 99) loop(0, j + 1)
      else loop(i + 1, j)
    }
    loop(0, 0)
  }
}
