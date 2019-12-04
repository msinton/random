package advent

import scala.util.chaining._

object day3 {

  def path(current: (Int, Int), instr: String): IndexedSeq[(Int, Int)] = {
    val digits = instr.substring(1).toInt
    (instr(0) match {
      case 'L' =>
        (current._1 - digits to current._1).map((_, current._2)).reverse
      case 'R' =>
        (current._1 to current._1 + digits).map((_, current._2))
      case 'D' =>
        (current._2 - digits to current._2).map((current._1, _)).reverse
      case 'U' =>
        (current._2 to current._2 + digits).map((current._1, _))
    }).drop(1)
  }

  val start = (0, 0)

  def constructPointsVisited(wire: List[String]): List[(Int, Int)] =
    wire
      .foldLeft((List.empty[(Int, Int)], start)) {
        case ((result, current), instr) =>
          val next = path(current, instr)
          (result ++ next, next.lastOption.getOrElse(current))
      }
      ._1

  def findClosestIntersection(wire1: List[String], wire2: List[String]): Option[Int] = {

    val path1 = constructPointsVisited(wire1).toSet

    def loop(xs: List[String], current: (Int, Int), found: List[Int]): List[Int] =
      xs match {
        case Nil => found
        case x :: xss =>
          val points = path(current, x)
          val newFound = points.toSet
            .intersect(path1)
            .map(x => x._1 + x._2)
            .pipe(found ++ _)

          loop(xss, points.lastOption.getOrElse(current), newFound)
      }

    loop(wire2, (0, 0), Nil).sorted.headOption
  }

  def shortestIntersection(wire1: List[String], wire2: List[String]): Option[Int] = {

    val path1 = (start +: constructPointsVisited(wire1)).zipWithIndex.toMap

    def loop(xs: List[String], current: (Int, Int), currentInd: Int, found: List[Int]): List[Int] =
      xs match {
        case Nil => found
        case x :: xss =>
          val points = path(current, x)
          val newFound = points.zipWithIndex
            .map(
              x =>
                path1.get(x._1).map { y =>
                  x._2 + y + currentInd + 1
                }
            )
            .collect { case Some(distance) => distance }
            .pipe(found ++ _)
          loop(xss, points.lastOption.getOrElse(current), currentInd + points.length, newFound)
      }

    val result = loop(wire2, (0, 0), 0, Nil)

    result.sorted.headOption
  }
}
