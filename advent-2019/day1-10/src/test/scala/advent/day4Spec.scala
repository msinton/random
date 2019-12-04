package advent

class day4Spec extends BaseSpec {

  import day4._

  val input = 256310 to 732736

  describe("day4") {
    it("star 1") {
      println(passwords(input).length)
    }

    it("star 2") {
      println(passwords2(input).length)
    }
  }
}
