package fp

import org.scalatest.funspec.AnyFunSpec

class TraverseSpec extends AnyFunSpec {
  import Traverse._
  import Applicative._

  describe("traverse") {
    it("fuse should perform two independent traversals as one, returning both") {

      def multBy2(i: Int) = Option(i * 2)

      def negate(i: Int) = List(-i)

      val result = Traverse[List].fuse(List.range(1, 5))(multBy2, negate)
      assert(result == Tuple2(Some(List(2, 4, 6, 8)), List(List(-1, -2, -3, -4))))
    }
  }

  describe("test out traverse") {

    it("list") {

      val xs = List.range(1, 5)

      def transfromLess4(i: Int): Option[String] = if (i < 4) Some(s"$i-s") else None
      def transfromLess5(i: Int): Option[String] = if (i < 5) Some(s"$i-s") else None

      assert(Traverse[List].traverse(xs)(transfromLess4) == None)
      assert(Traverse[List].traverse(xs)(transfromLess5) == Some(List("1-s", "2-s", "3-s", "4-s")))
    }

    it("option of list") {
      val opt = Option("1234")

      def transform(s: String): List[Int] = s.map(_.asDigit).toList
      def transformGenEven(s: String): List[Int] =
        s.filter(_ % 2 == 0).map(x => List(x.asDigit, x.asDigit)).flatten.toList

      assert(Traverse[Option].traverse(opt)(transform) == List(Some(1), Some(2), Some(3), Some(4)))
      assert(Traverse[Option].traverse(opt)(transformGenEven) == List(Some(2), Some(2), Some(4), Some(4)))
      assert(Traverse[Option].traverse(None)(transform) == List(None))
    }

    it("option and tree") {

      val opt = Option("123456")

      def transform(s: String): Tree[Int] =
        if (s.size <= 2)
          Tree(s.head.asDigit, s.tail.headOption.fold(List.empty[Tree[Int]])(a => List(Tree(a.asDigit, List.empty))))
        else
          Tree(
            s.head.asDigit,
            s.tail.splitAt(s.tail.size / 2) match {
              case (s1, s2) => List(transform(s1), transform(s2))
            }
          )

      println(opt.map(transform))

      println(Traverse[Option].traverse(opt)(transform)) // seems suprising but when we bring option inside a tree, should it know how to traverse through the values?
    }
  }
}
