package codewars

object BinaryDivisionRegex {

  type NodeId = Int

  def addZero(n: Int, remainder: Int): Int = (remainder * 2) % n

  def addOne(n: Int, remainder: Int): Int = (remainder * 2 + 1) % n

  case class Transition(nodeId: NodeId, binary: String)

  // could improve by making the map keyed by (fromNode, toNode)
  // otherwise would need to do string conversion at the end? - buffer
  def removeNode(fsm: Map[NodeId, List[Transition]], nodeId: NodeId): Map[NodeId, List[Transition]] = {
    val removed = fsm(nodeId)
    val (cycle, rest) = removed.partition(_.nodeId == nodeId)
    val cycleBinaryString = cycle.headOption.map(x => s"(${x.binary})*").getOrElse("")

    val removedToNodes = rest.map(x => (x.nodeId, cycleBinaryString + x.binary))

    val removedFromNodes = (fsm - nodeId).collect {
      case (n, xs) if xs.exists(_.nodeId == nodeId) => (n, xs.find(_.nodeId == nodeId).get.binary)
    }

    val newTransitions = for {
      (toNode, toBinStr) <- removedToNodes
      (fromNode, fromBinStr) <- removedFromNodes
    } yield (fromNode, Transition(toNode, fromBinStr + toBinStr))

    newTransitions.foldLeft(fsm - nodeId) {
      case (newFsm, (fromNode, tr)) =>
        val newTranBin = newFsm(fromNode).find(_.nodeId == tr.nodeId).map(x => s"(${x.binary}|${tr.binary})").getOrElse(tr.binary)
        newFsm + (fromNode -> (Transition(tr.nodeId, newTranBin) :: newFsm(fromNode).filterNot(_.nodeId == nodeId)))
    }
  }

  def regexDivisibleBy(n: Int): String = {
    val fsm = (0 until n).map(x => (x, List(
      Transition(addZero(n, x), "0"),
      Transition(addOne(n, x), "1")))).toMap

    (1 until n).foldLeft(fsm)(removeNode)(0).filterNot(_.binary == "0").head.binary + "*"
  }


  def test(n: Int): Unit = {

    println("------")
    //         (0|(1(10)*(0|11)|1(10)*0)(((0(1)*01|0(1)*00(10)*(0|11))|0(1)*00(10)*0))*1)*
    //  val reg = "(0|1(10)*(0|11)(01*01|01*00(10)*(0|11))*1)*"
    val reg = regexDivisibleBy(n)
    //  println(reg)

    println((0 to 100 by n).find(!_.toBinaryString.matches(reg)))
    println((0 to 50).filterNot(_ % n == 0).find(_.toBinaryString.matches(reg)))
  }
}
