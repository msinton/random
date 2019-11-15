package advent.of.code.y2017

object Day21 {

  import Day21.Grids._

  def parse(lines: Seq[String]): Seq[Rule] = {

    val ruleReg = """(.+) => (.+)""".r

    lines.map {
      case ruleReg(from, to) => Rule(Grids.parse(from), Grids.parse(to))
    }
  }

  case class Rule(from: Grid, to: Grid)

  object Grids {

    def parse(string: String): Grid = {
      val splits = string.split("/")
      val active = splits.zipWithIndex.flatMap {
        case (s, rowIdx) => s.zipWithIndex.filter(_._1 == '#').unzip._2.map(c => (c, rowIdx))
      }
      Grid(active.toSet, splits.size)
    }

    case class Grid(active: Set[(Int, Int)], size: Int) {

      require(active.size <= (size * size), "invalid grid - must be square")

      override def toString: String = {
        (0 until size)
          .map(y => (0 until size)
            .map(x => if (active.contains((x, y))) '#' else '.').mkString)
          .mkString("\n")
      }

      def flipY(): Grid = {
        Grid(active.map(p => (size - p._1 - 1, p._2)), size)
      }

      def flipX(): Grid = {
        Grid(active.map(p => (p._1, size - p._2 - 1)), size)
      }

      def rotate90(): Grid = {
        Grid(active.map(p => (
          (size * 2 - 1 - p._2) % size,
          (size * 2 + p._1) % size)
        ), size)
      }

      /* rotation:
        (x,y)

        x: 0  1  2
        y
        ..
        0  0  1  2
        1  3  4  5
        2  6  7  8

        rotate90 ->

           6  3  0
           7  4  1
           8  5  2

        Plus mod size:
          (2,)(1,1)(,2)
          (1,2)(,)(2,1)
          (,1)(2,2)(1,)

     */
    }
  }

  def rotations(grid: Grid): Set[Grid] = {
    (1 to 3).foldLeft(List(grid))((rs, _) => rs.head.rotate90() :: rs).toSet
  }

  def allMatches(grid: Grid): Set[Grid] = {
    rotations(grid) ++ rotations(grid.flipX()) ++ rotations(grid.flipY())
  }

  def getDivision(grid: Grid): Int = if (grid.size % 2 == 0) 2 else 3

  def modulo(p: (Int, Int), m: Int) = (p._1 % m, p._2 % m)

  def divide(grid: Grid, partSize: Int): Map[(Int, Int), Grid] = {
      grid.active
        .groupBy { case (x, y) => ((x - (x % partSize)) / partSize, (y - (y % partSize)) / partSize) }
        .mapValues(v => Grid(v.map(modulo(_, partSize)), partSize))
        .withDefaultValue(Grid(Set(), partSize))
  }

  def joinGrids(grids: Map[(Int, Int), Grid], gridsLength: Int): Grid = {
    val sectionSize = grids((0, 0)).size
    val newSize = sectionSize * gridsLength

    val active = for {
      x <- 0 until gridsLength
      y <- 0 until gridsLength
      anActive <- grids(x, y).active.map { case (i, j) => (x * sectionSize + i, y * sectionSize + j) }
    } yield anActive

    Grid(active.toSet, newSize)
  }

  val initial = Grids.parse(".#./..#/###")

  def apply(rules: Seq[Rule], iterations: Int): Int = {

    val rulesForAllMatches: Map[Grid, Grid] =
      (for {
        rule <- rules
        grid <- allMatches(rule.from)
      } yield (grid, rule.to)).toMap

    def nextGrids(grid: Grid, divideInto: Int) = {
      val divided = divide(grid, divideInto)
      (for {
        x <- 0 until grid.size
        y <- 0 until grid.size
      } yield ((x, y), rulesForAllMatches(divided((x, y))))).toMap
        .withDefaultValue(Grid(Set(), divideInto))
    }

    def update(grid: Grid): Grid = {
      val divideInto = getDivision(grid)
      val grids = nextGrids(grid, divideInto)
      joinGrids(grids, gridsLength = grid.size / divideInto)
    }

    val result = (1 to iterations).foldLeft(initial)((prev, _) => update(prev))
//    println(result)

    result.active.size
  }


}


// Idea: define an ordering on grids, so that it reduces the comparisons required.
/*

 # . .
 . # #
 . . .
 score: 0 + 4 + 5 = 9

 - the ordering requires that the On (#) are as early as possible
 - first by row, then by column
 - so the above example cannot be preceded by any of its reflected/rotated versions:

 # . .
 . # .
 . # .
 score: 0 + 4 + 7 = 11

 So we can do this by soring - where the grid

 */