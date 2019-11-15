package advent.of.code.y2017

object Day22 {

  sealed trait Direction {
    val right: Direction
    val left: Direction
  }

  case object Up extends Direction {
    override val right: Direction = East
    override val left: Direction = West
  }

  case object Down extends Direction {
    override val right: Direction = West
    override val left: Direction = East
  }

  case object West extends Direction {
    override val right: Direction = Up
    override val left: Direction = Down
  }

  case object East extends Direction {
    override val right: Direction = Down
    override val left: Direction = Up
  }

  case class Point(x: Int, y: Int) {

    def move(direction: Direction): Point = {
      direction match {
        case Up => Point(x, y - 1)
        case Down => Point(x, y + 1)
        case West => Point(x - 1, y)
        case East => Point(x + 1, y)
      }
    }
  }

  sealed trait Infection {
    val nextState: Infection
  }
  case object Clean extends Infection {
    override val nextState: Infection = Weakened
  }
  case object Weakened extends Infection {
    override val nextState: Infection = Infected
  }
  case object Infected extends Infection {
    override val nextState: Infection = Flagged
  }
  case object Flagged extends Infection {
    override val nextState: Infection = Clean
  }

  type Grid = Map[Point, Infection]

  case class Virus(position: Point = Point(0,0), direction: Direction = Up, infected: Int = 0) {

    def flipAndMove(grid: Grid): (Grid, Virus) = {
      val (newDirection, infecting) =
        if (grid(position) == Infected)
          (direction.right, 0)
        else
          (direction.left, 1)

      val newGrid = grid + (position -> (if (grid(position) == Infected) Clean else Infected))

      val newPosition = position.move(newDirection)

      (newGrid, Virus(newPosition, newDirection, infected + infecting))
    }

    def evolvedBurst(grid: Grid): (Grid, Virus) = {

      val newDirection = grid(position) match {
        case Clean => direction.left
        case Weakened => direction
        case Infected => direction.right
        case Flagged => direction.left.left
      }

      val nextState = grid(position).nextState

      val infecting = if (nextState == Infected) 1 else 0

      val newGrid = grid + (position -> nextState)
      val newPosition = position.move(newDirection)

      (newGrid, Virus(newPosition, newDirection, infected + infecting))
    }
  }

  def parse(lines: Seq[String]): Grid = (
    for {
      lineWithIdx <- lines.zipWithIndex
      (line, y) = lineWithIdx
      (c, x) <- line.zipWithIndex
      if c == '#'
    } yield Point(x - line.length / 2, y - lines.size / 2) -> Infected)
    .toMap
    .withDefaultValue(Clean)

  def simpleVirus(initial: Grid): Int = {
    (1 to 10000).foldLeft((initial, Virus())){ case ((state, virus), _) => virus.flipAndMove(state) }
      ._2.infected
  }

  def evolvedVirus(initial: Grid): Int = {
    (1 to 10000000).foldLeft((initial, Virus())){ case ((state, virus), _) => virus.evolvedBurst(state) }
      ._2.infected
  }
}
