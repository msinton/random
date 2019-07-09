package lif
import org.scalatest.WordSpec

class LifeTest extends WordSpec {

  import Life._

  "under-population" should {
    "return cells with fewer than 2 neighbours" in {
      val cells = Map((0, 0) -> 1, (0, 1) -> 0)

      val result = underPopulated(cells)
      assert(result == cells.keySet)
    }
    "Not affect cells with at least 2 neighbours" in {
      val cells = Map((0, 0) -> 2, (0, 1) -> 3)

      val result = underPopulated(cells)
      assert(result.isEmpty)
    }
  }

  "over-population" should {
    "return cells with more than 3 neighbours" in {
      val cells = Map((0, 0) -> 4, (0, 1) -> 5)

      val result = overpopulated(cells)
      assert(result == cells.keySet)
    }
    "Not affect cells with less than 3 neighbours" in {
      val cells = Map((0, 0) -> 2, (0, 1) -> 1)

      val result = overpopulated(cells)
      assert(result.isEmpty)
    }
  }

  "birth" should {
    "cause empty (dead) cells with exactly 3 neighbours come to life" in {
      val cells = Map((1, 1) -> 3)

      val result = birth(cells)
      assert(result == cells.keySet)
    }

    "Not affect cells with neighbours not equal to 3" in {
      val cells = Map((1, 1) -> 1, (1, 1) -> 2, (1, 1) -> 4, (1, 1) -> 5)

      val result = birth(cells)
      assert(result.isEmpty)
    }
  }

  "game tick" should {
    import Game._
    "return the updated cells that are now alive" in {

      val Life(alive, _, _) = tick(
        neighbours = Map(
          (0, 1) -> 1,
          (1, 1) -> 2,
          (2, 2) -> 3,
          (0, 3) -> 4
        ),
        alive = Set((0, 1), (1, 1), (2, 2), (0, 3)),
        died = Set((0, 1)),
        born = Set((2, 3)),
        bounds = Bounds(x0 = 0, x1 = 3, y0 = 0, y1 = 3)
      )

      assert(
        alive == Set(
          (1, 1),
          (2, 2),
          (0, 3),
          (2, 3)
        )
      )
    }

    "return the updated neighbours" in {
      val Life(_, neighbours, _) = tick(
        neighbours = Map(
          (0, 1) -> 1,
          (1, 1) -> 2,
          (2, 2) -> 3,
          (2, 3) -> 1,
          (0, 3) -> 4
        ),
        alive = Set((0, 1), (1, 1), (2, 2), (0, 3)),
        died = Set((0, 1)),
        born = Set((2, 3)),
        bounds = Bounds(x0 = 0, x1 = 3, y0 = 0, y1 = 3)
      )

      val expected = Map(
        (0, 1) -> 1,
        (1, 1) -> 1,
        (2, 2) -> 4,
        (0, 3) -> 4,
        (2, 3) -> 1,
        (1, 2) -> 1,
        (1, 3) -> 1,
        (3, 2) -> 1,
        (3, 3) -> 1
      )

      assert(
        neighbours == expected
      )
    }
  }

  def printer(life: Life) = {
    import life._
    val repr = (bounds.y0 to bounds.y1)
      .map(
        y =>
          (bounds.x0 to bounds.x1)
            .map(x => if (cells.contains((x, y))) "x" else " ")
            .mkString(" ")
      )
      .mkString("\n")

    println(repr)
    println("-----------------------------")
  }

  "game loop should return the new game state - 1" in {

    val initial = Life(
      cells = Set((0, 0), (0, 1), (1, 0), (1, 1), (2, 2), (2, 0)),
      width = 6,
      height = 5
    )
    //   val initial = Life(Set((0, 0), (0,1), (0, 2), (1, 0), (1, 1), (1,2)))

    import Game._

    println()
    (0 to 3).foldLeft(initial) { (n, _) =>
      printer(n)
      loop(n)
    }
    assert(true)
  }

  "game loop should return the new game state - 2" in {

    val initial = Life(
      cells = Set((0, 0), (1, 1), (1, 2), (2, 1), (0, 2)),
      width = 8,
      height = 8
    )

    import Game._

    (0 to 8).foldLeft(initial) { (n, _) =>
      printer(n)
      loop(n)
    }
    assert(true)
  }

}
