Remove the val?

```scala
val x = "Hello World"
val r1 = x.reverse
val r2 = x.reverse
```

```scala
val r1 = "Hello World".reverse
val r2 = "Hello World".reverse
```

```scala
val x = new StringBuilder("Hello")
val y = x.append(" World")
val r1 = y.toString
val r2 = y.toString
```

Can I make this visually easy to see the replacements we are doing?

```scala
val x = new StringBuilder("Hello")
val r1 = x.append(" World").toString
val r2 = x.append(" World").toString
```

```scala
def make_coffee(coffee_beans: Beans) =
  grind_beans(coffee_beans)
	  .flatMap(combine_with_hot_water)
	  .map(add_milk)
```

// if grinder is not working returns empty
