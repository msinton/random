package tel
import scala.util.Try

trait Reader {

  def read(): Try[Either[Double, Avg]]
}

