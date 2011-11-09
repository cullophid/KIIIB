package simulation.model

import java.awt.geom.Line2D

object PhysicalWall {
  def fromXML(node: scala.xml.Node) = {
    val x1 = (node \ "@x1").first.text.toDouble
    val y1 = (node \ "@y1").first.text.toDouble
    val x2 = (node \ "@x2").first.text.toDouble
    val y2 = (node \ "@y2").first.text.toDouble
    val thickness = (node \ "@thickness").first.text.toDouble

    new PhysicalWall(new Line2D.Double(x1, y1, x2, y2), thickness)
  }
}
class PhysicalWall(val segment: Line2D, val thickness: Double) {
  def toXML: scala.xml.Node =
    <walledge x1={segment.getX1.toString} y1={segment.getY1.toString} x2={segment.getX2.toString} y2={segment.getY2.toString} thickness={thickness.toString} />

}

// vim: set ts=2 sw=2 et:

