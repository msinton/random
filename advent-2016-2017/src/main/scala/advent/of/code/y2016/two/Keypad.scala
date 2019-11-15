package advent.of.code.y2016.two

object Keypad {

  sealed trait Direction
  case object Up extends Direction
  case object Down extends Direction
  case object Left extends Direction
  case object Right extends Direction

  object Grid {

    val positions = IndexedSeq(
      IndexedSeq("1", "2", "3"),
      IndexedSeq("4", "5", "6"),
      IndexedSeq("7", "8", "9"),
    )

    def move(at: (Int, Int), direction: Direction): (Int, Int) = {
      direction match {
        case Up     => (at._1, Math.max(at._2 - 1, 0))
        case Down   => (at._1, Math.min(at._2 + 1, 2))
        case Left   => (Math.max(at._1 - 1, 0), at._2)
        case Right  => (Math.min(at._1 + 1, 2), at._2)
      }
    }
  }

  object Grid2 {

    val positions = Map(
                                  (3,-1) -> "1",
                    (2,0) -> "2", (3,0) -> "3", (4,0) -> "4",
      (1,1) -> "5", (2,1) -> "6", (3,1) -> "7", (4,1) -> "8", (5,1) -> "9",
                    (2,2) -> "A", (3,2) -> "B", (4,2) -> "C",
                                  (3,3) -> "D",
    )

    def move(at: (Int, Int), direction: Direction): (Int, Int) = {
      val newAt = direction match {
        case Up     => (at._1, at._2 - 1)
        case Down   => (at._1, at._2 + 1)
        case Left   => (at._1 - 1, at._2)
        case Right  => (at._1 + 1, at._2)
      }
      if (positions.isDefinedAt(newAt)) newAt else at
    }
  }

  def decipherCode(instructions: Seq[String]): String = {

    def loopLine(position: (Int, Int), instruction: String): ((Int, Int), String) = {
      instruction.headOption match {
        case Some('U') => loopLine(Grid2.move(position, Up), instruction.drop(1))
        case Some('D') => loopLine(Grid2.move(position, Down), instruction.drop(1))
        case Some('L') => loopLine(Grid2.move(position, Left), instruction.drop(1))
        case Some('R') => loopLine(Grid2.move(position, Right), instruction.drop(1))
        case _ => (position, Grid2.positions(position))
      }
    }

    def loop(position: (Int, Int), instructions: Seq[String]): String = {
      instructions match {
        case Nil => ""
        case h :: t =>
          val (newPosition, code) = loopLine(position, h)
          code + loop(newPosition, t)
      }
    }

    loop((1,1), instructions)
  }

}
