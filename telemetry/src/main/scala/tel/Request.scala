package tel

sealed trait Request

// TODO use
case class Tel(d: Double) extends Request
case object Noop extends Request
case class Avg(n: Int) extends Request
