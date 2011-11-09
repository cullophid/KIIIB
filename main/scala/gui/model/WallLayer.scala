package gui.model

import utils.vec.mutable.Vec2

import java.awt.geom.Line2D

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.util._
import edu.umd.cs.piccolo.nodes._

class WallLayer(scaling: Scaling) extends PLayer {
  def addNode(x: Double, y: Double): WallNode = {
    val n = new WallNode
    n.setOffset(x, y)
    nodeLayer.addChild(n)
    n
  }

  def addEdge(p1: WallNode, p2: WallNode, thickness: Double) {
    edgeLayer.addChild(new WallEdge(p1, p2, thickness, scaling))
  }

  def splitEdge(edge: WallEdge, x: Double, y: Double): WallNode = {
    val p1 = edge.p1
    val p2 = edge.p2
    val p1vec = Vec2(p1.getXOffset, p1.getYOffset)
    val p2vec = Vec2(p2.getXOffset, p2.getYOffset)
    val dir = (p2vec - p1vec).normalized
    val relativeCCW = Line2D.relativeCCW(p1vec.x, p1vec.y, p2vec.x, p2vec.y, x, y)
    val ortho = Vec2(-relativeCCW * dir.y, relativeCCW * dir.x)
    val intersectionPoint = Vec2(x, y) + (ortho * Line2D.ptLineDist(p1vec.x, p1vec.y, p2vec.x, p2vec.y, x, y))
    val n = addNode(intersectionPoint.x.toFloat, intersectionPoint.y.toFloat)
    edge.removeFromParent
    addEdge(p1, n, edge.thickness)
    addEdge(n, p2, edge.thickness)
    n
  }

  def clear {
    edgeLayer.removeAllChildren
    nodeLayer.removeAllChildren
  }

  val edgeLayer = new PLayer()
  val nodeLayer = new PLayer()

  addChild(edgeLayer)
  addChild(nodeLayer)

  def export: List[simulation.model.PhysicalWall] = {
    val edges = scala.collection.jcl.Buffer(edgeLayer.getChildrenReference)
    edges flatMap {
      case n: WallEdge => Seq(n.export)
      case _ => Nil
    } toList
  }
}

// vim: set ts=2 sw=2 et:

