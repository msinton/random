package advent.of.code.y2016.four

object CheckSum {

  val roomMatcher = """^(.+)-(\d+)\[(.*)\]$""".r

  def apply(input: Seq[String]): Int = {

    def checkSum(roomStr: String): Int = {
      roomStr match {
        case roomMatcher(alpha, code, check) =>
          val charCountWithIndex = alpha.replaceAll("-", "").zipWithIndex.groupBy(_._1).mapValues(x => (x.length, x.head._2))

          val top5 = charCountWithIndex.toList.sortBy(x => (-x._2._1, x._1, x._2._2)).take(5).map(_._1)

          if (top5.mkString("").sorted == check.sorted) {
            code.toInt
          } else
            0
      }
    }

    input.map(checkSum).sum
  }
}
