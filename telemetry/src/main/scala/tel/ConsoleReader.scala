package tel
import scala.util.Try

object ConsoleReader extends Reader {
  override def read(): Try[Either[Double, Avg]] =
    Try(parse(scala.io.StdIn.readLine()))

  val avgRequestPattern = "avg: (\\d)+".r

  def parse(request: String): Either[Double, Avg] = request match {
    case avgRequestPattern(n) => Right(Avg(n.toInt))
    case d => Left(d.toDouble)
  }
}
