package advent

import scala.util.chaining._
import cats.data.NonEmptyList
import cats.implicits._

class day12Spec extends BaseSpec {

  import day12._

  describe("day12") {
    it("star 1") {
      assert(
        day12
          .run(
            1000,
            SolarSystem(parse(input), 0)
          )
          .tap(println)
          .pipe(totalEnergy) == 7013
      )
    }

    it("star 2") {
      assert(
        findPeriods(
          SolarSystem(parse(input), 0)
        ).pipe(
            periods => periods.values.toList.toNel.map(_.flatMap(NonEmptyList.of(_)))
          )
          .pipe(_.map(lcm)) == Some(324618307124784L)
      )
    }
  }

  def input = """<x=-7, y=17, z=-11>
  <x=9, y=12, z=5>
  <x=-9, y=0, z=-4>
  <x=4, y=6, z=0>"""

  def example1 = """<x=-1, y=0, z=2>
  <x=2, y=-10, z=-7>
  <x=4, y=-8, z=8>
  <x=3, y=5, z=-1>"""

  def example2 = """<x=-8, y=-10, z=0>
  <x=5, y=5, z=10>
  <x=2, y=-7, z=3>
  <x=9, y=-8, z=-3>"""

  val MoonPattern = raw".*<x=(.*), y=(.*), z=(.*)>".r

  def parse(input: String): List[Moon] =
    input
      .split("\n")
      .map { case MoonPattern(x, y, z) => Moon(Point(x.toInt, y.toInt, z.toInt), Point.empty) }
      .toList
}
