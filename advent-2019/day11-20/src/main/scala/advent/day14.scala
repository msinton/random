package advent

import scala.util.chaining._

import cats.implicits._
import cats.kernel.Monoid
import cats.kernel.Semigroup

object day14 {

  def gcd(a: Int, b: Int): Int =
    if (b == 0) a else gcd(b, a % b)

  final case class ElementQuantity(element: String, quantity: Int) {
    def *(i: Int) = ElementQuantity(element, quantity * i)
  }

  final case class Reaction(inputs: List[ElementQuantity], output: ElementQuantity)

  final case class Fraction(numerator: Int, denominator: Int) {
    def ceil = math.ceil(numerator.toDouble / denominator).toInt
  }

  object Fraction {
    implicit val fractionSemi: Semigroup[Fraction] = Semigroup.instance { (a, b) =>
      val num = a.numerator * b.denominator + b.numerator * a.denominator
      val denom = a.denominator * b.denominator
      val gcdValue = gcd(num, denom)
      Fraction(num / gcdValue, denom / gcdValue)
    }
  }

  final case class ReactionFractions(value: Map[String, Fraction])

  object ReactionFractions {
    val empty = ReactionFractions(Map.empty)
    implicit val reactionFractionSemi: Monoid[ReactionFractions] = Monoid.instance(
      empty,
      (a, b) => ReactionFractions(a.value |+| b.value)
    )
  }

  def parse(input: Iterable[String]): List[Reaction] = {
    val ElementQuantityPattern = raw"\s*(\d+) (.+)".r
    val ReactionPattern = raw"\s*(.+) => (.+)".r
    input.map {
      case ReactionPattern(ins, out) =>
        val inputs = ins
          .split(",")
          .map {
            case ElementQuantityPattern(quantity, element) =>
              ElementQuantity(element, quantity.toInt)
          }
          .toList
        out match {
          case ElementQuantityPattern(quantity, element) =>
            Reaction(inputs, ElementQuantity(element, quantity.toInt))
        }
    }.toList
  }

  def recipe(reactions: List[Reaction]): Long = {

    val outputMap =
      reactions
        .groupBy(_.output.element)
        .tap(xs => assert(xs.values.toList.forall(_.size == 1)))
        .pipe(_.view.mapValues(_.head).toMap)

    def calculateOre(parts: ReactionFractions, outputs: List[ElementQuantity]) = {

      // Fuel, require 1, produced as 1
      // AB, require is in input quantity, produced is from outputMap
      // don't forget to multiply by current ceil(fraction)

      val x = outputs.flatMap(x => outputMap(x.element).inputs)
      ???
    }

    // calculateOre(List(ElementQuantity("FUEL", 1)), Map.empty).ore
    ???
  }
}
/**
  *
  *
  *
  *
  final case class Result(ore: Long, surplus: Map[String, Int], required: Map[String, Int])

  final case class ElementQuantities(value: Map[String, Int])

  object ElementQuantities {
    val empty = ElementQuantities(Map.empty)
    implicit val qsMonoid = Monoid.instance[ElementQuantities](
      empty,
      (a, b) => ElementQuantities(a.value |+| b.value)
    )
  }

  final case class Reagents(required: ElementQuantities, surplus: ElementQuantities)

  object Reagents {
    val empty = Reagents(ElementQuantities.empty, ElementQuantities.empty)

    implicit val reagentsMonoid = Monoid.instance[Reagents](
      empty,
      (a, b) => Reagents(a.required |+| b.required, a.surplus |+| b.surplus)
    )
  }

  object Result {
    val empty = Result(0, Map.empty, Map.empty)

    implicit val resultMonoid: Monoid[Result] = Monoid.instance(
      empty,
      (a, b) => Result(a.ore + b.ore, a.surplus |+| b.surplus, a.required |+| b.required)
    )
  }

    // def calculateOre(reaction: Reaction, require: Int): Result = {
    //   val multiplier = math.ceil(require / reaction.output.quantity.toDouble).toInt
    //   val surplus = Map(
    //     reaction.output.element -> (multiplier * reaction.output.quantity - require)
    //   )

    //   reaction.inputs.foldLeft(Result(0, surplus)) { (result, input) =>
    //     val surplusInput = result.surplus.getOrElse(input.element, 0)
    //     // val require = math.max(0, multiplier * input.quantity - surplusInput)

    //     val inputSurplus =
    //       if (surplusInput <= (multiplier * input.quantity))
    //         Map.empty[String, Int]
    //       else Map(input.element -> (surplusInput - multiplier * input.quantity))

    //     (input.element match {
    //       case "ORE" =>
    //         outputMap(input.element).output
    //           .pipe(out => Result(0, Map.empty, Map(out.element -> require * out.quantity)))
    //       case _ => calculateOre(outputMap(input.element), multiplier) // was require?
    //     }) |+| Result(0, inputSurplus) |+| result
    //   }
    // }

    // build full tree of requirements and then evaluate?
    //

    def calculateOre(requirements: List[ElementQuantity], acc: Map[String, Int]): Result =
      // * I calc how many times I need to perform the reaction to get each input. This creates a new set of requirements
      // * For each of these, if they are not from ORE then calculate the requirements again
      // * The trick is to accumulate reaction requirements for all paths that require that chemical
      // * when the quantity is known proceed to the next
      // * but how can I know a chemicals requirements have all been accumulated?
      if (requirements.isEmpty)
        Result(
          acc.toList
            .map(x => ElementQuantity(x._1, x._2))
            .flatMap(getRequirements)
            .foldMap(_.quantity.toLong),
          Map.empty,
          acc
        )
      else {

        val (rawElements, others) = requirements.partition(eq => createFromOre(eq.element))
        val nextReq = others.flatMap(getRequirements)

        calculateOre(nextReq, rawElements.foldMap(eq => Map(eq.element -> eq.quantity)) |+| acc)
      }

    def getRequirements(elementQuantity: ElementQuantity): List[ElementQuantity] =
      outputMap(elementQuantity.element).pipe { reaction =>
        val multiplier =
          math.ceil(elementQuantity.quantity.toDouble / reaction.output.quantity).toInt
        reaction.inputs.map(_ * multiplier)
      }

    def createFromOre(element: String): Boolean =
      outputMap(element).inputs.exists(_.element == "ORE")

    // def reverseReactions(result: Result): Result =
    //   result.surplus
    //     .map {
    //       case (element, quantity) =>
    //         reactions.find(r => r.output.element == element && r.output.quantity <= quantity)
    //     }
    //     .collectFirst {
    //       case Some(reversibleReaction) =>
    //         reversibleReaction.inputs
    //           .foldLeft(result.surplus)(
    //             (surplus, in) =>
    //               surplus.updatedWith(in.element)(_.map(_ + in.quantity).orElse(Some(in.quantity)))
    //           )
    //           .updatedWith(reversibleReaction.output.element)(
    //             _.map(_ - reversibleReaction.output.quantity)
    //           )
    //           .pipe(sur => Result(result.ore - sur.getOrElse("ORE", 0), sur.removed("ORE")))
    //     }
    //     .map(reverseReactions)
    //     .getOrElse(result)

    // // breadth first
    // def calculateReagents(reactions: List[Reaction], require: Int, acc: Reagents): Reagents = {

    //   def reactionRequirements(reaction: Reaction): Reagents = {
    //     val multiplier = math.ceil(require / reaction.output.quantity.toDouble).toInt
    //     Reagents(
    //       required = ElementQuantities(Map("TODO" -> multiplier)), // TODO each input
    //       ElementQuantities(
    //         Map(
    //           reaction.output.element -> (multiplier * reaction.output.quantity - require)
    //         )
    //       )
    //     )
    //   }
    //   // the thing I need to figure out:
    //   // how to collect up the total required for an element and then calculate surplus (is surplus even needed then?!)

    //   val nextReagents = reactions.foldMap(reactionRequirements) |+| acc

    //   // add surplus to acc,
    //   // map each input to their required quantities and add to acc
    //   // continue with the next reactions:
    //   // these are the reactions for the inputs which are not in the old acc
    //   val inputs = nextReagents.required.value.keySet -- acc.required.value.keySet
    //   ???
    // }

    // def oreFromReagents(reagents: Reagents): Long =
    //   // val fromOre = outputMap()
    //   ???

    // resolve the equations simultaneously?
    // substitution?

    // reactions
    //   .find(_.output.element == "FUEL")
    //   .map(fuelReaction => calculateReagents(List(fuelReaction), 1, Reagents.empty))
    //   .map(oreFromReagents)
    //   .getOrElse(0)

  */
