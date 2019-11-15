package codewars

import java.math.BigInteger

import scala.annotation.tailrec

object FabregeFormula {

  def height(n: BigInteger, m: BigInteger): BigInteger = {
    height(n.intValue(), m.intValue()).bigInteger
  }

  def height(n: Int, m: Int): BigInt = {

    val eggs = Math.min(n, m)

    @tailrec
    def loop(bi: Int, previous: BigInt, result: BigInt): BigInt = {
      if (bi == eggs) result else {
        val next = previous * (m - bi) / (bi + 1)
        loop(bi + 1, next, result + next)
      }
    }

    (eggs, m) match {
      case (0, _) => 0
      case (_, 0) => 0
      case (1, tries) => tries
      case _ => loop(1, m, m)
    }
  }

    //  def height(n: BigInteger, d: BigInteger): BigInteger = {
    //
    //
    //    val eggs = Math.min(n.intValue(), m.intValue())
    //
    //    (eggs, m.intValue()) match {
    //      case (0, _) => ZERO
    //      case (_, 0) => ZERO
    //      case (1, tries) => BigInteger.valueOf(tries)
    //      case (_, tries) => height(eggs, tries).bigInteger
    //    }
    //  }

//    def height(n: BigInt, m: BigInt): BigInt = {
//
//      var bi = BigInt(1)
//      var result = m
//      var prev = result
//
//      while (bi != n) {
//        val curr = prev * (m - bi) / (bi + 1)
//        println(curr)
//        result += curr
//        println("==", result)
//        bi += 1
//        prev = curr
//      }
//      result
//
//    }

    //  def height(n: Int, m: Int): BigInt = {
    //
    //    val d = m - n
    //    if (d == 0) return BigInt(2).pow(n) - 1
    //
    //    if (n == 1) return BigInt(m + 1) * (m + 2) / 2
    //    if (d < 4) return formulas(n, d)
    //
    //    val sum = getPowerOfTwoSum(n, m)
    //
    //    var start = System.currentTimeMillis()
    //
    //    val upperPart = factorialDiv(m - 3, n - 2, m - n - 1) - 1
    //    System.out.println("upper\t\t\t", System.currentTimeMillis() - start)
    //
    //    start = System.currentTimeMillis()
    //    val finalS = finalSum(n, m)
    //    System.out.println("finalS\t\t\t", System.currentTimeMillis() - start)
    //
    //    val result = sum + upperPart + finalS
    //    println(result)
    //    result
    //  }

    def factorialDiv(start: Int, stop: Int, divStart: Int): BigInt = {
      if (divStart > stop) {
        factorial(start, divStart) / factorial(stop)
      } else {
        factorial(start, stop) / factorial(divStart)
      }
    }

    def formulas(eggs: Int, extraTries: Int): BigInt = {
      extraTries match {
        case 0 => BigInt(2).pow(eggs) - 1
        case 1 => BigInt(2).pow(eggs + 1) - 2
        case 2 => BigInt(2).pow(eggs + 2) - eggs - 4
        case 3 => BigInt(2).pow(eggs + 3) - 7 * (eggs - 1) - sumOfSeries(eggs - 4) - 9
      }
    }

    def sumOfSeries(n: BigInt): BigInt = {
      n * (n + 1) / 2
    }

    def getPowerOfTwoSum(n: Int, m: Int): BigInt = {

      val d = m - n

      val start = System.currentTimeMillis()
      if (n > 8) {
        val result = sumPowerFactors(n, d)
        println("sum power fac \t\t", System.currentTimeMillis() - start)
        result
      } else {
        var sum = BigInt(0)
        var i = 0
        while(i < n - 2) {
          val p = addPower(n, d, i)
          sum += p
          i += 1
        }
        println("old way \t\t", System.currentTimeMillis() - start)
        sum
      }
    }

    def finalSum(n: Int, m: Int): BigInt = {
      val d = m - n

      var i = 0

      var ff = factorial(n - 2, n - 3)
      var dTerm = BigInt((d + 3) * (d + 2) / 2)
      var sum = dTerm

      if (i < d - 1) {
        dTerm = (d + 1) * dTerm / (d + 3)
        sum += ff * dTerm
      }
      i += 1


      while (i < d - 1) {

        ff = (n - 2 + i) * ff / (1 + i)
        dTerm = (d + 1 - i) * dTerm / (d + 3 - i)

        sum += ff * dTerm
        i += 1
      }

      sum
    }


    def factorial(i: BigInt, stop: BigInt = BigInt(0)): BigInt = {
      @tailrec
      def loop(i: BigInt, result: BigInt): BigInt = {
        if (i == stop) {
          result
        } else
          loop(i - 1, i * result)
      }
      if (i < stop) 1 else loop(i, 1)
    }

    def addPower(n: Int, length: Int, seqNum: Int): BigInt = {
      if (seqNum == 0) BigInt(2).pow(n)
      else {
        val factSeq = factorial(seqNum + length - 1, seqNum) / factorial(length - 1)
        factSeq * BigInt(2).pow(n - seqNum)
      }
    }


    def sumPowerFactors(n: Int, d: Int): BigInt = {

      val firstTerm: BigInt = {

        var term = factorial(d, d - 1)
        var sum = 1 + term

        for (i <- 2 until n - 2) {
          term = term * (d - i + 1) / i
          sum += term
        }
        sum * BigInt(2).pow(n)
      }

      var x = factorial(d + n - 4, d - 1) / factorial(n - 3)
      var sum = x
      var power = 0
      for (i <- 1 until n - 3) {
        x = (d - i) * x / (d + n - 3 - i)
        power += 1
        sum += (x * BigInt(2).pow(power))
      }

      firstTerm - (sum * BigInt(2).pow(3))
    }

  }
