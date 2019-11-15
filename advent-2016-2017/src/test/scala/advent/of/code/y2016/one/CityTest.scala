package advent.of.code.y2016.one

import org.scalatest.FunSuite

class CityTest extends FunSuite {

  val input = "R3, L5, R2, L2, R1, L3, R1, R3, L4, R3, L1, L1, R1, L3, R2, L3, L2, R1, R1, L1, R4, L1, L4, R3, L2, L2, R1, L1, R5, R4, R2, L5, L2, R5, R5, L2, R3, R1, R1, L3, R1, L4, L4, L190, L5, L2, R4, L5, R4, R5, L4, R1, R2, L5, R50, L2, R1, R73, R1, L2, R191, R2, L4, R1, L5, L5, R5, L3, L5, L4, R4, R5, L4, R4, R4, R5, L2, L5, R3, L4, L4, L5, R2, R2, R2, R4, L3, R4, R5, L3, R5, L2, R3, L1, R2, R2, L3, L1, R5, L3, L5, R2, R4, R1, L1, L5, R3, R2, L3, L4, L5, L1, R3, L5, L2, R2, L3, L4, L1, R1, R4, R2, R2, R4, R2, R2, L3, L3, L4, R4, L4, L4, R1, L4, L4, R1, L2, R5, R2, R3, R3, L2, L5, R3, L3, R5, L2, R3, R2, L4, L3, L1, R2, L2, L3, L5, R3, L1, L3, L4, L3"


  test("testCoordinate") {
    assert(291 === City.coordinate(input.split(", ").toList))
  }

  test("testCoordinate2") {

    assert(2 === City.coordinate("R2, R2, R2".split(", ").toList))
  }

  test("testCoordinate3") {

    assert(12 === City.coordinate("R5, L5, R5, R3".split(", ").toList))

  }

  test("part 2") {
    val result = City.firstCoordVisitedTwice(input.split(", ").toList)

    assert(159 === Math.abs(result.get.x) + Math.abs(result.get.y))
  }

  test("part 2-2") {
    val in2 = "R8, R4, R4, R8"
    val result = City.firstCoordVisitedTwice(in2.split(", ").toList)
    assert(4 === Math.abs(result.get.x) + Math.abs(result.get.y))
  }
}
