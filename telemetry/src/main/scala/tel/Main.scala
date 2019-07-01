package tel

object Main extends App {

  val reader: Reader = ConsoleReader
  val writer: Writer = ConsoleWriter

  var allInput = List.empty[Double]
  var inputs = List.empty[Double]

  while (true) {
    val input = reader.read()
    input.fold(
      { _ =>
        writer.write("bad input")
      }, {
        case Left(tel) =>
          inputs = tel :: inputs
          allInput = tel :: allInput
          if (Alert(inputs)) {
            writer.write("Alert!")
            inputs = List.empty[Double]
          }

        case Right(Avg(n)) =>
          writer.write(avg(allInput.take(n)) + "")
      }
    )

  }

  def avg(values: Seq[Double]): Double = if (values.isEmpty) 0 else values.sum / values.size
}
