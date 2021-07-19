trait Beans
trait GroundBeans
trait BrownLiquid
trait Coffee

trait imperative_E {

  // library code
  def make_coffee(coffee_beans: Beans): Coffee =
    beans = grind_beans(coffee_beans)
  if (beans == null)
    throw new Error("grinder failed")
  else {
    black_coffee = combine_with_hot_water(beans)
    if (black_coffee == null)
      throw new Error("out of hot water")
    add_milk(black_coffee)
  }

  // client code
  try {
    beans = fetchBeans()
    make_coffee(beans)
  } catch (e) {
    logger.error("failed to make coffee", e.message)
    buy_coffee()
  }

}

trait E {

  def fetch_beans(): Option[Beans]
  def buy_coffee(): Coffee

  def grind_beans(x: Beans): Option[GroundBeans]

  def combine_with_hot_water(ground: GroundBeans): BrownLiquid

  def add_milk(l: BrownLiquid): Coffee

  // library code
  def make_coffee(coffee_beans: Beans): Option[Coffee] =
    grind_beans(coffee_beans)
      .flatMap(combine_with_hot_water)
      .map(add_milk)

  // client code
  fetch_beans()
    .map(make_coffee)
    .getOrElse(_ => buy_coffee())

}

trait E_Either {

  // library code
  def make_coffee(coffee_beans: Beans): Either[Error, Coffee] =
    grind_beans(coffee_beans)
      .flatMap(combine_with_hot_water)
      .map(add_milk)

  // client code
  fetch_beans()
    .map(make_coffee)
    .leftMap(e => logger.error("failed to make coffee", e.message))
    .getOrElse(_ => buy_coffee())
}

trait Imperative_PatternMatch {

  val animal = "dog"
  val climate = "arctic"

  val isTwoLeggedArcticAnimal =
    if (animal == "dog") {
      false
    } else if (animal == "bird") {
      if (climate == "arctic") {
        true
      } else {
        false
      }
    } else if (animal == "polar bear") {
      true
    } else {
      false
    }
}

trait PatternMatch {

  val animal = "dog"
  val climate = "arctic"

  val isTwoLeggedArcticAnimal = (animal, climate) match {
    case ("bird", "arctic") => true
    case ("polar bear", _)  => true
    case (_, _)             => false
  }

}

trait RT {

  val x = "Hello World"
  val r1 = x.reverse
  val r2 = x.reverse

  val r1 = "Hello World".reverse
  val r2 = "Hello World".reverse

  // r1 = "dlroW olleH"
  // r2 = "dlroW olleH"

  val builder = new StringBuilder("Hello")
  val x = builder.append(" World")
  val r1 = x.toString
  val r2 = x.toString

  val r1 = builder.append(" World").toString
  val r2 = builder.append(" World").toString

  // r1 = Hello World
  // r2 = Hello World

  // r2 = Hello World World

}
