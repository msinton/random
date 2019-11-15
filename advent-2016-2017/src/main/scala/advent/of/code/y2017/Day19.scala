package advent.of.code.y2017

object Day19 {

  type Coord = (Int, Int)

  trait Mover {
    def apply(coord: Coord): Coord
    def turn(coord: Coord, toggle: Boolean): (Coord, Mover)
  }

  trait HorizontalTurner {
    def turn(coord: Coord, toggle: Boolean): (Coord, Mover) =
      if (toggle)
        ((coord._1 + 1, coord._2), GoesEast)
      else
        ((coord._1 - 1, coord._2), GoesWest)
  }

  trait VerticalTurner {
    def turn(coord: Coord, toggle: Boolean): (Coord, Mover) =
      if (toggle)
        ((coord._1, coord._2 + 1), GoesSouth)
      else
        ((coord._1, coord._2 - 1), GoesNorth)
  }

  case object GoesNorth extends Mover with HorizontalTurner {
    def apply(coord: Coord): Coord = (coord._1, coord._2 - 1)
  }

  case object GoesSouth extends Mover with HorizontalTurner {
    def apply(coord: Coord): Coord = (coord._1, coord._2 + 1)

  }

  case object GoesEast extends Mover with VerticalTurner {
    def apply(coord: Coord): Coord = (coord._1 + 1, coord._2)
  }

  case object GoesWest extends Mover with VerticalTurner {
    def apply(coord: Coord): Coord = (coord._1 - 1, coord._2)
  }


  def findStart(s: Map[Int, Char]): Int = s.find(x => x._2 == '|').get._1


  val letterReg = """([a-zA-Z])""".r
  val pipeReg = """[\|\-]""".r

  def apply(lines: Map[Int, Map[Int, Char]]): String = {
    val x = findStart(lines(0))

    var steps = 0

    def loop(fn: Mover, coord: Coord, result: String): String = {

      steps += 1
      lines(coord._2)(coord._1) match {
        case ' ' =>
          println(steps)
          result

        case letterReg(c) =>
          loop(fn, fn(coord), result + c)

        case pipeReg() =>
          loop(fn, fn(coord), result)

        case '+' =>
          val (newCoord, newMover) = fn.turn(coord, toggle = true)
          if (lines(newCoord._2)(newCoord._1) != ' ')
            loop(newMover, newCoord, result)
          else {
            val (newCoord, newMover) = fn.turn(coord, toggle = false)
            loop(newMover, newCoord, result)
          }
      }
    }

    loop(GoesSouth, (x, 1), "")
  }
}
