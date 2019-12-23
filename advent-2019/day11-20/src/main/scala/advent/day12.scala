package advent

import cats.kernel.Monoid
import cats.implicits._

import scala.util.chaining._
import scala.annotation.tailrec
import cats.data.NonEmptyList
import cats.kernel.Eq
import enumeratum._

object day12 {

  sealed trait Axis extends EnumEntry
  object Axis extends Enum[Axis] {

    val values = findValues

    case object X extends Axis
    case object Y extends Axis
    case object Z extends Axis
  }

  final case class Point(x: Int, y: Int, z: Int) {
    def sumAbs = math.abs(x) + math.abs(y) + math.abs(z)

    def zipWith[A, B](f: (Int, Int) => A)(g: (A, A, A) => B, that: Point): B =
      g(f(x, that.x), f(y, that.y), f(z, that.z))

    def axis(axis: Axis): Int = axis match {
      case Axis.X => x
      case Axis.Y => y
      case Axis.Z => z
    }
  }

  object Point {
    def empty = Point(0, 0, 0)

    implicit val pointMonoid = Monoid.instance[Point](
      empty,
      (a, b) => Point(a.x + b.x, a.y + b.y, a.z + b.z)
    )

    implicit val eq = Eq.fromUniversalEquals[Point]
  }

  final case class Moon(at: Point, velocity: Point)

  final case class SolarSystem(moons: List[Moon], steps: Long)

  def calcVelocity(a: Int, b: Int): (Int, Int) =
    if (a < b) (1, -1) else if (a > b) (-1, 1) else (0, 0)

  final case class GravityUpdate(moonKey: Int, value: Point)

  def gravity(state: SolarSystem): SolarSystem = {
    val moons = state.moons.zipWithIndex
    moons
      .combinations(2)
      .flatMap {
        case List((m1, m1Key), (m2, m2Key)) =>
          m1.at.zipWith(calcVelocity)({
            case ((dx1, dx2), (dy1, dy2), (dz1, dz2)) =>
              List(
                GravityUpdate(m1Key, Point(dx1, dy1, dz1)),
                GravityUpdate(m2Key, Point(dx2, dy2, dz2))
              )
          }, m2.at)
      }
      .pipe { updates =>
        val moonUpdates = updates.toList.foldMap(x => Map(x.moonKey -> x.value))
        moons.map { case (moon, key) => moon.copy(velocity = moon.velocity |+| moonUpdates(key)) }
      }
      .pipe(ms => state.copy(moons = ms))
  }

  def move(moon: Moon): Moon =
    moon.copy(at = moon.at |+| moon.velocity)

  def move(state: SolarSystem): SolarSystem =
    SolarSystem(moons = state.moons.map(move), steps = state.steps + 1)

  val update: SolarSystem => SolarSystem =
    (gravity _)
      .andThen(move)

  def run(steps: Int, solarSystem: SolarSystem): SolarSystem = {
    @tailrec
    def loop(solarSystem: SolarSystem): SolarSystem =
      if (solarSystem.steps >= steps) solarSystem
      else loop(update(solarSystem))
    loop(solarSystem)
  }

  def totalEnergy: SolarSystem => Int =
    _.moons
      .map(m => m.at.sumAbs * m.velocity.sumAbs)
      .sum

  final case class MoonIndex(value: Int) extends AnyVal

  final case class AxisProperty(position: Int, velocity: Int)

  final case class InitialAxis(value: Map[Axis, List[AxisProperty]])

  final case class PeriodState(
    periodsFound: Map[Axis, Long],
    initial: InitialAxis,
    system: SolarSystem
  ) {

    private def findPeriods: Map[Axis, Long] =
      Axis.values
        .filterNot(periodsFound.contains)
        .map(
          axis =>
            axis -> system.moons
              .map(moon => AxisProperty(moon.at.axis(axis), moon.velocity.axis(axis)))
        )
        .filter { case (axis, properties) => initial.value(axis) == properties }
        .map(_.as(system.steps))
        .toMap

    def updatePeriods: PeriodState = {
      val periods = findPeriods
      if (periods.nonEmpty) {
        println(periods)
      }
      this.copy(
        periodsFound = periodsFound |+| periods
      )
    }

    def updateSystem =
      this.copy(system = update(system))
  }

  def update(periodState: PeriodState): PeriodState =
    periodState.updateSystem.updatePeriods

  def findPeriods(system: SolarSystem): Map[Axis, Long] = {

    @tailrec
    def loop(state: PeriodState): Map[Axis, Long] =
      if (state.periodsFound.size === 3)
        state.periodsFound
      else loop(update(state))

    val state =
      PeriodState(
        periodsFound = Map.empty,
        initial = InitialAxis(
          Axis.values
            .map(
              axis =>
                axis -> system.moons
                  .map(moon => AxisProperty(moon.at.axis(axis), moon.velocity.axis(axis)))
            )
            .toMap
        ),
        system = system
      )

    loop(state)
  }

  def gcd(a: Long, b: Long): Long =
    if (b == 0) a else gcd(b, a % b)

  def lcm(xs: NonEmptyList[Long]): Long =
    xs.reduceLeft((acc, x) => acc * x / gcd(acc, x))

}
