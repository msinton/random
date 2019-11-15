package advent.of.code.y2016.three

object Triangles {

  def isTriangle(a: Int, b: Int, c: Int): Boolean = {
    (a < b + c) && (b < a + c) && (c < a + b)
  }
}
