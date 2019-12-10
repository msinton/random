package advent

object day9 {
  def boostTest(input: IndexedSeq[Long]): List[Long] =
    IntCodeMachine(input).runToEnd(List(1))

  def boost(input: IndexedSeq[Long]): List[Long] =
    IntCodeMachine(input).runToEnd(List(2))

}
