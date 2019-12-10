package advent

class day10Spec extends BaseSpec {

  import day10._

  describe("day10") {
    it("star 1") {
      assert(bestLineOfSightCount(input) === 329)
    }

    it("star 2") {
      assert(
        findBestThenVaporize(input, 200).value === ((5, 12))
      )
    }
  }

  def sample1 = """.#..#
  |.....
  |#####
  |....#
  |...##""".stripMargin

  def sample2 = """.#..##.###...#######
  |##.############..##.
  |.#.######.########.#
  |.###.#######.####.#.
  |#####.##.#.##.###.##
  |..#####..#.#########
  |####################
  |#.####....###.#.#.##
  |##.#################
  |#####.##.###..####..
  |..######..##.#######
  |####.##.####...##..#
  |.#####..#.######.###
  |##...#.##########...
  |#.##########.#######
  |.####.#.###.###.#.##
  |....##.##.###..#####
  |.#.#.###########.###
  |#.#.#.#####.####.###
  |###.##.####.##.#..##""".stripMargin

  def input =
    """|....#...####.#.#...........#........
  |#####..#.#.#......#####...#.#...#...
  |##.##..#.#.#.....#.....##.#.#..#....
  |...#..#...#.##........#..#.......#.#
  |#...##...###...###..#...#.....#.....
  |##.......#.....#.........#.#....#.#.
  |..#...#.##.##.....#....##..#......#.
  |..###..##..#..#...#......##...#....#
  |##..##.....#...#.#...#......#.#.#..#
  |...###....#..#.#......#...#.......#.
  |#....#...##.......#..#.......#..#...
  |#...........#.....#.....#.#...#.##.#
  |###..#....####..#.###...#....#..#...
  |##....#.#..#.#......##.......#....#.
  |..#.#....#.#.#..#...#.##.##..#......
  |...#.....#......#.#.#.##.....#..###.
  |..#.#.###.......#..#.#....##.....#..
  |.#.#.#...#..#.#..##.#..........#...#
  |.....#.#.#...#..#..#...###.#...#.#..
  |#..#..#.....#.##..##...##.#.....#...
  |....##....#.##...#..........#.##....
  |...#....###.#...##........##.##..##.
  |#..#....#......#......###...........
  |##...#..#.##.##..##....#..#..##..#.#
  |.#....#..##.....#.#............##...
  |.###.........#....#.##.#..#.#..#.#..
  |#...#..#...#.#.#.....#....#......###
  |#...........##.#....#.##......#.#..#
  |....#...#..#...#.####...#.#..#.##...
  |......####.....#..#....#....#....#.#
  |.##.#..###..####...#.......#.#....#.
  |#.###....#....#..........#.....###.#
  |...#......#....##...##..#..#...###..
  |..#...###.###.........#.#..#.#..#...
  |.#.#.............#.#....#...........
  |..#...#.###...##....##.#.#.#....#.#.""".stripMargin

}
