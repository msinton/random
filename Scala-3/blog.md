
# Scala 3 - and what it means to me

## Why read this?
There are a whole bunch of changes that come with Scala 3. This is my opinionated selection of the noteworthy changes and how I feel about them! 
If you (and your team) are anything like me (and mine) then hopefully this will be relevant to you!


# Introduction
First of all I want to say a huge thank you to the folks who have been working hard on Scala 3 and that I’m excited to start enjoying the benefits in my everyday work.

Scala 3 is built on a new foundation called **Dotty**. The name **Dotty** comes from [Dependent Object Types (DOT)](http://lampwww.epfl.ch/~amin/dot/fool.pdf) which is the calculus for path-dependent types. The important take-away is that after 8 years of experience refining Scala, the team behind the language have specifically designed Dotty to model a Scala-like language and become a strong new foundation that will enable Scala to be the language they want it to be. DOT is also **simpler** than the previous foundation, and places emphasis on accessibility and safety.

For more information checkout the [Dotty docs](http://dotty.epfl.ch/docs/index.html)

# Dropped and changed features - some "warts" removed

## Automatic Eta Expansion

This makes me so happy! 😄

A fundamental requirement for a functional language is that you can pass around functions in just the same way as data. 
Now, because scala makes the distinction between *methods* (which are not objects) and *functions* (which are), there are certain situations 
where you need to tell the compiler you want to convert a *method* to a *function* so that it can be passed around. 

_Scala 2:_

``` scala
def foo(a: Int) = a + 1         // This is a method

val fooFunction = foo _         // This is a function
```

The requirement to add an `_` is awkward and confusing for new Scala developers. Thankfully that requirement has gone.

_Scala 3:_

``` scala
val fooFunction = foo           // This is a function
```

You can read more about this **wart** in Lihaoyi's excellent blog [Scala Warts - Weak Eta Expansion](http://www.lihaoyi.com/post/WartsoftheScalaProgrammingLanguage.html#weak-eta-expansion)

[Auto Eta Expansion - detailed reference](https://dotty.epfl.ch/docs/reference/changed-features/eta-expansion-spec.html)


## Bye-bye package object

Package objects are no longer needed since all kinds of definitions can now be written at the top-level. 

``` scala
package helloworld

def add(x: Iny, y: Iny) = x + y

final case class Person(name: String, age: Int)

val bob = Person("bob", 42)
```

This also makes me happy. 😄

The restriction that everything you write needs to live inside an object, class or trait was not needed, and 
package objects were clearly just a special case of object that attempted to alleviate this problem. 
This is nice, more warts removed.

### 🕊️ To migrate 🕊️

You will not be required to migrate initially, in Scala 3.0 package objects are only deprecated.
That said, it shouldn't be too hard!

``` scala
package fish

package object nemo {
    val friend = "Dory"
}
```

becomes

``` scala
package fish
package nemo

val friend = "Dory"
```

I expect there will be tools to do this automagically. 

[Dropped: Package Objects - reference details](https://dotty.epfl.ch/docs/reference/dropped-features/package-objects.html)


## Limit 22

For most people reaching this limit has been a rare thing and often easy to workaround - though my team has encountered this problem (in config loading). 

This makes me smile. 🙂

In dotty we can go beyond `Tuple22` and `Function22`. So we can all start writing our apps like this

``` scala
def makeEarth(
    init:           Planet,
    mountains:      Planet => Planet,
    oceans:         Planet => Planet,
    trees:          Planet => Planet,
    grass:          Planet => Planet,
    insects:        Planet => Planet,
    fish:           Planet => Planet,
    bears:          Planet => Planet,
    badgers:        Planet => Planet,
    snakes:         Planet => Planet,
    tigers:         Planet => Planet,
    caves:          Planet => Planet,
    lakes:          Planet => Planet,
    volcanoes:      Planet => Planet,
    geysers:        Planet => Planet,
    deserts:        Planet => Planet,
    crystals:       Planet => Planet,
    buriedTreasure: Planet => Planet,
    coal:           Planet => Planet,
    people:         Planet => Planet,
    food:           Planet => Planet,
    roads:          Planet => Planet,
    swimmingPools:  Planet => Planet,
    sheep:          Planet => Planet,
    dragons:        Planet => Planet,
    pillows:        Planet => Planet
): Planet = ???
```

[Dropped: Limit 22 - reference](https://dotty.epfl.ch/docs/reference/dropped-features/limit22.html)

## Tuples concat!

``` scala
(1, "thing", 3.14, false) ++ ("more", "and", 2, "more")
> (1, "thing", 3.14, false, "more", "and", 2, "more)
```

Enough said! ❤️


## XML literals

Deprecated, to be replaced with xml string interpolation:

_Scala 2:_

```scala
val people = 
  <people>
		<person firstName="John" lastName="Smith" age={{john.age}} gender="M" />
		<person firstName="Ada" lastName="Lovelace" age={{ada.age}} gender="F" />
  </people>
```

_Scala 3:_

``` scala
val people = xml"""
  <people>
		<person firstName="John" lastName="Smith" age=${john.age} gender="M" />
		<person firstName="Ada" lastName="Lovelace" age=${ada.age} gender="F" />
  </people>"""
```

For those who use XML literals it might come as a blow. It seems the reason to drop this is for simplification - including
XML literals in the language places a great burden on the language that some feel is not justified, and instead string interpolation
can achieve the same end result, albeit without some of the compile-time safety.

For me and my team, this has no impact, but it is certainly noteworthy. See this [discussion on the original proposal](https://contributors.scala-lang.org/t/proposal-to-remove-xml-literals-from-the-language/2146/81). 
The TLDR is that it has been shown that the **Lift** framework still compiles after a re-write and using
a third party interpolator. Though there is still some way to go to prove that it all still works correctly.

[The official Dotty XML interpolator](https://github.com/lampepfl/xml-interpolator/)

## No more Auto-Application

This is now an error

```scala
def next(): Fish = ...
next // no longer expanded by the compiler to "next()"
```

I like this. 👍

I like it because code will become more consistent. The parens `()` should be reserved for side-effecting code. So typically methods that return Unit.
This conforms to the *uniform access principle* that allows properties to be either fields `val x` or methods `def x` without affecting the calling code.
Therefore, side-effects are not properties and should not be permitted to be accessed as properties.

```scala
object Thing {
    def iChangeTheWorld(): Unit
}

Thing.iChangeTheWorld() // Not possible to write: "Thing.iChangeTheWorld" So the parens () tell us that this code has an effect
```

With this restriction in place the decision to write your methods with or without `()` is something that will become natural and make code easier to read.

In fact, there are even mistakes in the Scala 2 libraries - in particular `def toInt()` !

### 🕊️ To Migrate 🕊️

Your code can still be compiled in Dotty under -language:Scala2Compat. 

When paired with the `-rewrite` option, the code will be automatically rewritten to conform to Dotty's stricter checking.

[Dropped: Auto-Application - reference](https://dotty.epfl.ch/docs/reference/dropped-features/auto-apply.html)

# 2 New features!

## Types - intersection and union

### Intersection types

```scala
trait HasAge { def age: Int }
trait Named { def name: String }

def introduce(ab: HasAge & Named): String =  {
    s"My name is ${ab.name}, I am ${ab.age} years old"
}
```

Well, we sort of already have this. Whenever you write `with` you create a **Compound Type**.

```scala
trait CooksBread { def cook(bread: Bread): Toast }
trait Pops { def pop: Sound }

trait Toaster extends CooksBread with Pops
```

But as it stands, **Compound Types** do not obey some properties, notably the `commutativity` law. 

What this means is that these are not always the same:

```scala
trait AB extends A with B
trait BA extends B with A
// AB != BA    :(
```

It's not typically a deal breaker, but you can appreciate that fixing this is a nice simplification. 👍

The problem comes when A and B share the same member (e.g. both have a `def children`). 
Scala 3 solves this by making
1) the new member's return type the intersection of its parent's types (see `children`)
2) forcing you to override conflicting members (see `name`)

```scala
trait A {
  def children: List[A] 
  def name: String = "A"
}
trait B {
  def children: List[B] 
  def name: String = "B"
}
class C extends A with B {
  def children: List[A & B] = List.empty
  override def name: String = "C"
}
val x: A & B = new C
val ys: List[A & B] = x.children
x.name // "C"
```

Since List is covariant the intersection of `List[A]` and `List[B]` is `List[A & B]`.  Pretty nifty 

[Intersection types - reference](https://dotty.epfl.ch/docs/reference/new-types/intersection-types.html)

### Union Types

This follows a similar story to the intersection types above. In short, we can provide unions with `|`

```scala
val eitherStringOrInt: String | Int = if (condition) "Fish" else 0
val eitherManWomanOrChild: Man | Woman | Child = if (age <  18) Child() else if (mansplaining) Man() else Woman()

eitherManWomanOrChild match {
  case x: Man => x.eat(toast)
  case x: Woman => x.eat(toast)
  case x: Child => x.eat(toast)
} // everyone can eat toast ;) but you have to know who you are to do it ¯\_(ツ)_/¯
```

Unions Types are similar to `Either` or `Coproduct` in that they allow you to handle more than one distinct possible types.
An Either is right-biased and has a Monad, so provides different uses and is more powerful. 
I'm sure union have their place, however, as yet I am unsure where I would use them - perhaps when following the [Principle of Least Power](http://www.lihaoyi.com/post/StrategicScalaStylePrincipleofLeastPower.html), 
when I don't require the added benefits of an `Either`.

## Enums

The `Enumeration` type in Scala 2 is problematic to the point that nobody uses it. Instead we use `sealed trait` defined ADTs and rely
on libraries like `enumeratum` to get nice enum behaviour. 

Current problems:
1) Enumerations have the same type after erasure
2) There’s no exhaustive compile-time matching check
3) They don’t inter-operate with Java’s enum

Essentially it looks like Scala 3 implements `enum` in much the same way as `enumeratum` as a sealed trait, but making this
a first class language feature is a nice win as a developer. 

I spend less time writing boilerplate, adding dependencies and explaining to newcomers why we even need to do it in the first place. 

Libraries should also provide more consistent support - it becomes less of an "optional" thing to provide support for `enumeratum`.

```scala
enum Color {
  case Red, Green, Blue
}
```

We can use enum for ADTs and Generalized Algebraic Data Types (GADTs).

```scala
enum Option[+T] {
  case Some(x: T)
  case None
}
```

Awesome sauce 👍👍👍

[Enum - reference](https://dotty.epfl.ch/docs/reference/enums/enums.html)

## Implicits are bad, mkay

So implicits are really a mixed bag. Also known as term inference, the construct is present in other languages like Haskell, Rust and Swift and may be coming to Kotlin, C# and F#.
Scala wouldn't be as popular as it is today without them.

So what's the big deal? 

### Summary of current issues with Scala implicits
1. Being very powerful, implicits are easily over-used and mis-used
2. Over-reliance on implicit imports leads to inscrutable type errors
3. The keyword `implicit` is used for a large number of language constructs, requiring the reader to decipher what the intent is in each case
4. Implicit parameters are awkward - e.g. they must have a name, even though in many cases that name is never referenced
5. Implicits pose challenges for tooling. The set of available implicits depends on context, so command completion has to take context into account.

If you've been writing Scala for a while you may have learnt the common use cases for implicits and are happy with them as they are, 
but for newcomers, learning Scala can become a mountain to climb.

This section could easily be a whole blog post by itself, for that reason I will only give a brief summary of some of the new syntax.

### `given` and `with`

A `given` instance is conceptually the same as an implicit value.

We use the keyword `with` to specify that we require a `given` instance.

```scala
def max[T](x: T, y: T) with (ord: Ord[T]): T =
  if (ord.compare(x, y) < 0) y else x
```

This max function can be read as: 
"the `max` of 2 arguments `x` and `y` having type `T`, `with` an Ordering for `T`, is found by comparing `x` and `y`"

We can define the `given` instance for the ordering of ints:

```scala
given intOrd as Ord[Int] {
  def compare(x: Int, y: Int) =
    if (x < y) -1 else if (x > y) +1 else 0
}
```

and use it in either of 2 ways:
```scala
package instances.int

given intOrd as Ord[Int] ...
max(2, 3)
max(2, 3).with(intOrd) // less common
```

importing given instances must be done like so:

```scala
import instances.int.{given Ord[Int]} // imports only intOrd (from above) by referencing it's type
import instances.int.{given _} // imports all the given instances in the package
```

Typically when importing "given instances" it makes more sense to import them by their type than by their name. 
The name of a given instance is often unimportant, since only the correct type is required for its application.
For the same reason, it is not required for given instances to have a name at all.

```scala
given Ord[Int] {
  ...
}
```

#### 🕊️ To Migrate 🕊️

Old style implicits can still be imported as they currently are, however they will also be imported through the new `given` style of imports.
Gradually this old style will be deprecated (Scala 3.1) and then in a later version will give a compiler error.
This will give libraries and users the chance to migrate smoothly.

### `summon`

Simply summons a `given` instance.

_Scala 2_
```scala
val mySummonedIntOrd = implicitly[Ord[Int]]
```

_Scala 3_
```scala
val mySummonedIntOrd = summon[Ord[Int]]
```

## Extension methods - say goodbye to boilerplate

Extension methods allow one to add methods to a type after the type is defined. Example:

```scala
case class Person(name: String, age: Int)

def (p: Person).description: String = s"${p.name} is ${p.age} years old"

// usage
val bob = Person("Bob", 40)
bob.description // "Bob is 40 years old"
```

If you are familiar with Typeclasses (which are an important mechanism for libraries such as Cats), you will know that
we often need to define Syntax classes with serve to make writing code easier by converting ordinary values to a partially applied typeclass instance.

Extension methods make this easier

_Extension method to add compare as an infix method. Requires a given instance of Ord, and is generic on type T_
```scala
object syntax {
    @infix def [T](x: T) compare (y: T) with (ord: Ord[T]): Int = ord.compare(x, y)
}

import syntax._
import instances.int.{given _}

1 compare 5 // -1
```

In a nutshell this has become much easier to do. 👍👍

## Typeclasses 

If you're not sure what a typeclass is, don't be put off. This is actually a fairly simple pattern
to define behaviour for a single type. We've already seen it in previous examples.

_Scala 2_

```scala
// typeclass
trait Ord[T] {
    def compare(x: T, y: T): Int
}
// a typeclass instance
implicit val intOrd: Ord[Int] = new Ord[Int] {
  def compare(x: Int, y: Int) =
    if (x < y) -1 else if (x > y) +1 else 0
}
```

_Scala 3_

```scala
// typeclass
trait Ord[T] {
    def compare(x: T, y: T): Int
}
// a typeclass instance
given Ord[Int] {
  def compare(x: Int, y: Int) =
    if (x < y) -1 else if (x > y) +1 else 0
}
```

Essentially there is not a lot of change here. 
Traits already did a good job of representing a typeclass.

Declaring typeclass instances involves less boilerplate now. 👍


## main

Scala 3 introduces a new way to declare an entry point for a program with `@main` annotation.

```scala
@main def programEntyPoint(): Unit = {
  println("Hello World")
}
```

## Optional Braces using `with`

You may have heard that braces are becoming optional in Scala 3. As a result the keyword `with` has a new role
so that curly braces are not required by class bodies and similar constructs.

_Scala 2_
```scala
class NoBraces() {
  def delete(braces: String): String
}
```

_Scala 3_
```scala
class NoBraces() with
  def delete(braces: String): String
```

Personally I like this. 🙂

What I don't like is now I have a new problem - which should we use in our team's production code and will everyone agree?

# Conclusion

Scala 3.0 is coming! 

Dotty became Feature Complete December 2019, this paves the way for Scala 3.0. 
I have struggled to find conclusive indication of when that will be, one source says this Autumn. 
First of all scala 2.14 will be released with the intention to enable smoother migration to Scala 3.0.

You can start by trying out [Dotty](https://dotty.epfl.ch/) now, it's super simple to download and have a play in the REPL - and there are example Dotty projects to check out too.

Migration looks to be simple, with a focus on providing auto-re-write tooling for the few breaking changes.

Developer experience will improve, the language will be more streamlined and less quirky. 
I expect certain features will open the door to better libraries too. 

I for one will try to do what I can to help the community, which has given so much to me, without expecting anything in return.


