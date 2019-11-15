package fp

import org.scalatest.funspec.AnyFunSpec

class ApplicativeSpec extends AnyFunSpec {

  describe("product should compose 2 applicatives") {
    val OptAndList = Applicative.opt.product(Applicative.list)

    it("unit") {
      assert(OptAndList.unit(1) == ((Some(1), List(1))))
    }
    it("map2") {
      assert(
        OptAndList
          .map2((Some("a"), List("b")), (Some(1), List.empty[Int]))(_ + _) == (
          (
            Some("a1"),
            List()
          )
        )
      )
    }
  }

  describe("compose should compose the other applicative inside this one") {

    val ListOpt = Applicative.list.compose(Applicative.opt)

    it("unit") {
      assert(ListOpt.unit(1) == List(Some(1)))
    }

    it("map2") {
      assert(ListOpt.map2(ListOpt.unit(1), ListOpt.unit(2))(_ + _) == List(Some(3)))
      assert(ListOpt.map2(ListOpt.unit(1), List(Option.empty[Int]))(_ + _) == List(None))
    }
  }

  describe("sequenceMap") {
    it("should pull the whole map into the applicative just like sequence on list") {
      assert(
        Applicative.opt
          .sequenceMap(Map("a" -> Some(1), "b" -> None)) == None
      )
      assert(
        Applicative.opt
          .sequenceMap(Map("a" -> Some(1), "b" -> Some(2))) == Some(Map("a" -> 1, "b" -> 2))
      )
    }
  }

}
