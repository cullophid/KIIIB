package gui.model.scenario

import java.awt.geom.Ellipse2D

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.activities._
import edu.umd.cs.piccolo.event._
import edu.umd.cs.piccolo.util._
import edu.umd.cs.piccolo.nodes._

/**
 * This class handles the display of avatar paths based on the time the nodes
 * would be reached.
 */
class TimePath(index0: Int, var name: String) {
  override def toString = name
  private var _index = index0

  val timeLayer = new PLayer
  val posLayer = new PLayer
  
  def visible: Boolean = posLayer.getVisible && timeLayer.getVisible
  def visible_=(v: Boolean) = {
    posLayer.setVisible(v)
    timeLayer.setVisible(v)
  }

  def index = _index
  def index_=(v: Int) {
    _index = v
    nodes foreach {node =>
      node.timeNode.setOffset(node.timeNode.getXOffset, (1 + index) * PathNode.pixelsPerSecond)
    }
  }

  def nodes = _nodes

  private var _nodes = List[PathNode]()

  def append(node: PathNode) {
    node.prev = lastOption

    timeLayer.addChild(node.timeNode)
    node.timeNode setParent timeLayer
    node.timeNode.setOffset(node.timeNode.getXOffset, (1 + index) * PathNode.pixelsPerSecond)

    posLayer.addChild(node.posNode)
    node.posNode setParent posLayer

    node.timePath = Some(this)

    node.prev map { prev =>
      val posLine = new NodeEdge.NodeEdgeClass(prev.posNode, node.posNode) {
        setStrokePaint(new java.awt.Color(0x8ae234))
      }
      posLayer.addChild(posLine)
      node.posNode.addEdge(posLine)
      val timeLine = new NodeEdge.NodeEdgeClass(prev.timeNode, node.timeNode) {
        setStrokePaint(new java.awt.Color(0x8ae234))
      }
      timeLayer.addChild(timeLine)
      node.timeNode.addEdge(timeLine)

      // remember to show the node above the line since the line is added after
      node.posNode.moveToFront
      timeLine.moveToBack
    }
    _nodes = _nodes ++ List(node)
  }

  def remove(node: PathNode) {
    _nodes -= node
    node.timeNode.removeFromParent
    node.posNode.removeFromParent
    node.next map remove
    node.prev map (_ next = None)
  }

  def clear {
    nodes foreach { node =>
      _nodes -= node
      node.timeNode.removeFromParent
      node.posNode.removeFromParent
    }
  }

  def firstOption = _nodes.firstOption

  def lastOption = _nodes.lastOption

  def toSimulationType = {
    simulation.messages.AvatarPathMessage(index, name, nodes map (_ toAvatarPathNode))
  }
}

// vim: set ts=2 sw=2 et:

