package utils.vec

object Vec {
  implicit def mutableVec2ToImmutableVec2(v: mutable.Vec2) = Vec2(v.x, v.y)
  implicit def immutableVec2ToMutableVec2(v: Vec2) = mutable.Vec2(v.x, v.y)
  implicit def point2D2ImmutableVec2(v: java.awt.geom.Point2D) = Vec2(v.getX, v.getY)
  implicit def mmutableVec2ToPoint2D(v: Vec2) = new java.awt.geom.Point2D.Double(v.x, v.y)
}

object Vec2 {
  def fromXML(node: scala.xml.Node): Vec2 =
    Vec2((node \ "@x").first.text.toDouble,
         (node \ "@y").first.text.toDouble)
}
case class Vec2(val x: Double, val y: Double) {
  def setX(value: Double) = Vec2(value, y)
  def setY(value: Double) = Vec2(x, value)
  def incrX(value: Double) = Vec2(x + value, y)
  def incrY(value: Double) = Vec2(x, y + value)

  def + (v: Vec2) = Vec2(x + v.x, y + v.y)
  def - (v: Vec2) = Vec2(x - v.x, y - v.y)
  def / (k: Double) = Vec2(x / k, y / k)
  def * (k: Double) = Vec2(x * k, y * k)

  def dot(v: Vec2) = x * v.x + y * v.y

  lazy val length2 = x * x + y * y
  lazy val length = Math.sqrt(length2)

  def normalized = if (length > 0) Vec2(x / length, y / length) else Vec2(0, 0)

  def distanceTo (v: Vec2) = (v - this).length

  def directionTo(v: Vec2) = Math.atan2(v.x-x, v.y-y)

  override def toString = "Vec2(%1.3f,%1.3f)".format(x, y)

  def toXML =
    <pos x={x.toString} y={y.toString}/>
}

package mutable {
  case class Vec2(var x: Double, var y: Double) {
    def + (v: Vec2)   = Vec2(x + v.x, y + v.y)
    def - (v: Vec2)   = Vec2(x - v.x, y - v.y)
    def / (k: Double) = Vec2(x / k, y / k)
    def * (k: Double) = Vec2(x * k, y * k)

    def +=(v: Vec2)   = { x += v.x; y += v.y; }
    def -=(v: Vec2)   = { x -= v.x; y -= v.y; }
    def /=(k: Double) = { x /= k; y /= k; }
    def *=(k: Double) = { x *= k; y *= k; }

    def dot(v: Vec2)  = x * v.x + y * v.y

    def length2 = x * x + y * y
    def length = Math.sqrt(length2)

    def normalized = if (length > 0) Vec2(x / length, y / length) else Vec2(0, 0)

    def distanceTo (v: Vec2) = (v - this).length

    def directionTo(v: Vec2) = Math.atan2(v.x-x, v.y-y)
  }

}

// vim: set ts=2 sw=2 et:

