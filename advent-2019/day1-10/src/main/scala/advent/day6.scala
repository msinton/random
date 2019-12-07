package advent

import cats.implicits._
import scala.util.chaining._

object day6 {

  sealed trait Tree {
    def name: String
  }
  final case class Node(depth: Int, children: List[Tree], name: String) extends Tree {
    def toLeaf = Leaf(depth, name)
  }
  final case class Leaf(depth: Int, name: String) extends Tree

  val root = "COM"

  def parse(input: List[String]): Map[String, Set[String]] =
    input
      .map(_.split("\\)"))
      .foldMap(arr => Map(arr(0) -> Set(arr(1))))

  def buildOrbits(input: List[String]) = {
    val orbits = parse(input)

    def buildTree(start: String, depth: Int): Tree =
      orbits.get(start) match {
        case None        => Leaf(depth, start)
        case Some(value) => Node(depth, value.toList.map(buildTree(_, depth + 1)), start)
      }
    buildTree(root, 0)
  }

  def totalOrbits(input: List[String]) =
    buildOrbits(input)
      .pipe(count)

  def count(tree: Tree): Int =
    tree match {
      case Node(depth, children, _) => depth + children.map(count).sum
      case Leaf(depth, _)           => depth
    }

  def collectAncestors(a: String, tree: Tree, ancestors: List[Leaf]): List[Leaf] =
    tree match {
      case x if x.name === a =>
        ancestors

      case n: Node =>
        n.children.flatMap(collectAncestors(a, _, ancestors :+ n.toLeaf))

      case _ => Nil
    }

  def totalTransfers(input: List[String]): Int = {
    val orbits = buildOrbits(input)

    val youAncestors = collectAncestors("YOU", orbits, Nil)
    val sanAncestors = collectAncestors("SAN", orbits, Nil)

    println(youAncestors)
    println(sanAncestors)
    println(
      youAncestors.toSet
        .intersect(
          sanAncestors.toSet
        )
    )

    val closestAncestorDepth = youAncestors.toSet
      .intersect(
        sanAncestors.toSet
      )
      .toList
      .map(_.depth)
      .max

    youAncestors.last.depth + sanAncestors.last.depth - 2 * closestAncestorDepth
  }
}
