import codewars.Fabrege

val tries = 3
val eggs = 5

var res = (3 to tries - eggs + 2).scanLeft(3)(_ + _)
var i = 2

while (i < eggs) {
  res = res.scanLeft(res(0))((acc, v) => acc + v + 1).drop(1)
  println(res)
  i += 1
}

res.last

// number of values
10 * 4

31 * 2 + 1

9948 - 6475 - 1

(9948 - 40) / Math.pow(2, 7)

val numberFromMaths = Math.pow(2, 10) * 6

numberFromMaths - 9948

i = 2
res = (3 to tries - eggs + 2).scanLeft(3)(_ + _).drop(1)
while (i < eggs) {
  res = res.tail.scanLeft(res.head * 2 + seriesSum(i-1) + 1)((acc, v) => acc + v + 1).drop(1)
  println(i, res)
  i += 1
}

def seriesSum(limit: Int) = {
  (limit + 1) * limit / 2
}

seriesSum(2)

seriesSum((tries - eggs + 1) * 4)

2509 - 1485

Fabrege.height(BigInt(2).bigInteger, BigInt(8).bigInteger)

val start = 3
val multiplier = tries

(3 * 12) * 3 + seriesSum(6)

val first = start * multiplier * 2 + seriesSum(eggs) - 1

// formula for the second in terms of the first?
(first - seriesSum(eggs) + 1) / 2 * 3 + seriesSum(eggs)
val second = start * multiplier * 3 + seriesSum(eggs)

first * 2 + 2 * seriesSum(eggs)

val next2 = first * 2

seriesSum(eggs + 1)


def formula(first: Int, n: Int): Int = {
  (first - seriesSum(n-1) + 1) / 2 * 3 + seriesSum(n)
}

formula(63, 4)


formula(162, 4)

def formulaForX(ys: Seq[Int], n: Int) = {
  ys.head + ys.take(n).sum + n + 1
}

val ys = (3 to tries - eggs + 2).scanLeft(3)(_ + _).drop(1)
formulaForX(ys, 3)
formulaForX(ys, 4)


//def form(ys: Seq[Int], row: Int, col: Int) = {
//  (3 * seriesSum(row) - 1) * ys(1) +
//}

