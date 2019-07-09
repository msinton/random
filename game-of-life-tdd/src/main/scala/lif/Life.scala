package lif

case class Bounds(x0: Int, x1: Int, y0: Int, y1: Int) {
  val within: Cell => Boolean = {
    case (x, y) => x >= x0 && x <= x1 && y >= y0 && y <= y1
  }
}

case class Life(cells: Cells, neighbours: Map[Cell, Int], bounds: Bounds)

object Life {

  def apply(cells: Cells, width: Int, height: Int): Life = {
    val bounds = Bounds(0, width, 0, height)
    Life(
      cells = cells,
      neighbours = allNeighbours(grid(bounds), cells).filterNot(_._2 == 0),
      bounds = bounds
    )
  }

  val surrounding: Cell => Cells = cell =>
    cell match {
      case (x, y) =>
        (for {
          i <- -1 to 1
          j <- -1 to 1
        } yield (x + i, y + j)).toSet - cell
    }

  private def allNeighbours(cells: Seq[Cell], alive: Cells): Map[Cell, Int] =
    cells
      .map(c => (c, surrounding(c).count(alive.contains)))
      .toMap

  def underPopulated(neighbours: Map[Cell, Int]): Cells =
    neighbours.filter(_._2 < 2).keySet

  def overpopulated(neighbours: Map[Cell, Int]): Cells =
    neighbours.filter(_._2 > 3).keySet

  def grid(bounds: Bounds): Seq[Cell] = {
    val Bounds(x0, x1, y0, y1) = bounds
    for {
      i <- x0 to x1
      j <- y0 to y1
    } yield (i, j)
  }

  def birth(neighbours: Map[Cell, Int]): Cells =
    neighbours.filter(_._2 == 3).keySet

}

object Game {

  import cats._
  import cats.implicits._

  def tick(
    alive: Cells,
    neighbours: Map[Cell, Int],
    bounds: Bounds,
    died: Cells,
    born: Cells
  ): Life = {

    def adjustment(cell: Cell): Int =
      Life.surrounding(cell).count(born.contains) - Life.surrounding(cell).count(died.contains)

    val newNeighboursToCheck = born
      .flatMap(Life.surrounding)
      .filter(bounds.within)
      .toSet -- neighbours.keySet

    Life(
      cells = alive -- died ++ born,
      neighbours = Monoid.combineAll(
        List(
          neighbours.map {
            case (c, x) => (c, x + adjustment(c))
          },
          newNeighboursToCheck.map(c => (c, Life.surrounding(c).count(born.contains))).toMap
        )
      ),
      bounds = bounds
    )
  }

  def loop(life: Life): Life = {
    import life._
    import Life._
    tick(
      alive = cells,
      neighbours = neighbours,
      bounds = bounds,
      died = cells.intersect(underPopulated(life.neighbours) ++ overpopulated(life.neighbours)),
      born = birth(life.neighbours -- cells)
    )
  }
}
