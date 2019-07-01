package tel

trait Writer {
  def write(s: String): Unit
}

object ConsoleWriter extends Writer {

  def write(s: String): Unit = println(s)
}
