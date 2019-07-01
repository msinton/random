package tel
import org.scalatest.WordSpec

class AlertTest extends WordSpec {

  "alert" should {
    "return true" when {
      "input is 50.1 50.2" in {
        val input = Seq(50.1, 50.2)

        assert(Alert(input))
      }
      "input is 49.9 49.9" in {
        val input = Seq(49.9, 49.9)

        assert(Alert(input))
      }
    }
    "return false" when {
      "input is 50 50" in {
        val input = Seq(50D, 50D)

        assert(Alert(input) === false)
      }
      "input is 50" in {
        val input = Seq(50D)

        assert(Alert(input) === false)
      }
      "input is empty" in {
        val input = Seq.empty[Double]

        assert(Alert(input) === false)
      }
    }
  }
}
