package advent.of.code.y2017

object Day20 {

  case class Position(x: Long, y: Long, z: Long)

  case class Velocity(x: Long, y: Long, z: Long)

  case class Acceleration(x: Long, y: Long, z: Long)

  case class Particle(p: Position, v: Velocity, a: Acceleration) {

    def update: Particle = {
      val vNew = Velocity(v.x + a.x, v.y + a.y, v.z + a.z)
      Particle(Position(p.x + vNew.x, p.y + vNew.y, p.z + vNew.z), vNew, a)
    }
  }

  def parse(lines: IndexedSeq[String]): IndexedSeq[Particle] = {

    val parseReg = """p=<(.+),(.+),(.+)>, v=<(.+),(.+),(.+)>, a=<(.+),(.+),(.+)>""".r

    lines.map({
      case parseReg(x, y, z, vx, vy, vz, ax, ay, az) => Particle(
        Position(x.toLong, y.toLong, z.toLong),
        Velocity(vx.toLong, vy.toLong, vz.toLong),
        Acceleration(ax.toLong, ay.toLong, az.toLong)
      )
    })
  }

  val updateAll: IndexedSeq[Particle] => IndexedSeq[Particle] = _.map(_.update)

  val updateIterable: Iterable[Particle] => Iterable[Particle] = _.map(_.update)

  def filterCollisions(ps: Iterable[Particle]): Iterable[Particle] = {
    ps.groupBy(_.p).filter(_._2.size == 1).values.flatten
  }

  def distance(p: Position): Long = {
    Math.abs(p.x) + Math.abs(p.y) + Math.abs(p.z)
  }
  
  // if acceleration large than will be further away as time tends to infinity

  def apply(particles: IndexedSeq[Particle]): Int = {
    val ps = (1 to 5000).foldLeft(particles)((ps,_) => updateAll(ps))
    val closest = ps.minBy(x => distance(x.p))
    ps.indexOf(closest)
  }

  def reduction(particles: Iterable[Particle]): Int = {
    val reduced = (1 to 7000).foldLeft(particles)((ps, _) => updateIterable.andThen(filterCollisions)(ps))
    reduced.size
  }
}
