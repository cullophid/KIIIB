package gui.model

import edu.umd.cs.piccolo.PNode
import edu.umd.cs.piccolo.nodes.PPath

import java.beans.{PropertyChangeListener, PropertyChangeEvent}
import java.awt.geom.Line2D

object NodeEdge {
  class NodeEdgeClass(val p1: ConnectedNode, val p2: ConnectedNode) extends PPath with NodeEdge {
    setPickable(false)
    setStrokePaint(new java.awt.Color(0x888a85))
  }
  def makeLine(p1: ConnectedNode, p2: ConnectedNode) = new NodeEdgeClass(p1, p2)
}
trait NodeEdge extends PPath {
  val p1: ConnectedNode
  val p2: ConnectedNode

  override def removeFromParent {
    super.removeFromParent
    p1.removeEdge(this)
    p1.removePropertyChangeListener(changeListener)

    p2.removeEdge(this)
    p2.removePropertyChangeListener(changeListener)
  }

  private val changeListener = new PropertyChangeListener() {
    def propertyChange(event: PropertyChangeEvent): Unit = update
  }

  private def update {
    val start = p1.connectionPNode.getGlobalBounds.getCenter2D
    val end   = p2.connectionPNode.getGlobalBounds.getCenter2D
    val (width, height) = (end.getX - start.getX, end.getY - start.getY)
    val line  = new Line2D.Double(-width/2, -height/2, width/2, height/2);
    setOffset(start.getX + width/2, start.getY + height/2)
    setPathTo(line)
  }

  p1.addEdge(this)
  p2.addEdge(this)

  p1.addPropertyChangeListener(PNode.PROPERTY_FULL_BOUNDS, changeListener)
  p2.addPropertyChangeListener(PNode.PROPERTY_FULL_BOUNDS, changeListener)

  update
}

// vim: set ts=2 sw=2 et:

