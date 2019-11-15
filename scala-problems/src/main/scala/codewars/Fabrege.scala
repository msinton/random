package codewars

import java.math.BigInteger
import java.math.BigInteger._

import scala.annotation.tailrec


object Fabrege {

  def height(n: BigInteger, m: BigInteger): BigInteger = {
    if (n.compareTo(ZERO) <= 0 || m.compareTo(ZERO) <= 0)
      ZERO
    else
      heightNonZeroImp(BigInt(n), BigInt(m)).bigInteger
  }

  def heightNonZero(eggz: BigInt, tries: BigInt): BigInt = {

    val eggs = if (eggz > tries) tries else eggz

    @tailrec
    def loop(counter: Int, results: Seq[BigInt]): Seq[BigInt] = {
      if (counter == 0)
        results
      else {
        loop(counter - 1, results.scanLeft(results.head)(_ + _ + 1).drop(1))
      }
    }

    if (eggs == 1)
      tries
    else {
      val resultSeqForTwoEggs = (BigInt(3) to tries - eggs + 2).scanLeft(BigInt(3))(_ + _)
      loop(eggs.intValue - 2, resultSeqForTwoEggs).last
    }
  }


  def heightNonZeroImp(eggz: BigInt, tries: BigInt): BigInt = {

    val eggs = if (eggz > tries) tries else eggz


    if (eggs == 1)
      tries
    else if (eggs == tries) {
      var i = 2
      var result = BigInt(3)
      while (i < eggs) {
        result += result + 1
        i += 1
      }
      result
    } else {

      val mutRes = (BigInt(2) to tries - eggs + 1).scanLeft(BigInt(3))(_ + _ + 1).drop(1).toBuffer
      println(mutRes.size)
      var i = 2

      while (i < eggs.intValue()) {
        mutRes.update(0, mutRes.head * 2 + 2)
        (1 until mutRes.size).foreach(i => mutRes(i) += mutRes(i - 1) + 1)
        i += 1
      }
      mutRes.last
    }
  }

}
