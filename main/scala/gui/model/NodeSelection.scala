package gui.model

import gui.Selectable

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.util._
import edu.umd.cs.piccolo.nodes._

import java.awt.geom.Ellipse2D

class NodeSelection {

  class RingNode(owner: PNode with Selectable) extends PPath {
    setPathTo(new Ellipse2D.Double(-10, -10, 20, 20))
    setPickable(false)
    setStrokePaint(new java.awt.Color(0x4e9a06))
    setStroke(new java.awt.BasicStroke(2.0f))
  }


  def selectedNode = _selectedNode
  def selectedNode_=(node: Option[PNode with Selectable]) {
    _selectedNode map { case n: PPath => n.setStrokePaint(java.awt.Color.BLACK); case _ => () }
    _selectedNode map { n =>
      _ring map (_.removeFromParent)
      _ring = None
    }
    _selectedNode = node
    _selectedNode map { n =>
      _ring = Some(new RingNode(n))
      n.addChild(_ring.get)
    }
    _selectedNode map { case n: PPath => n.setStrokePaint(new java.awt.Color(0x4e9a06)); case _ => () }
  }

  private var _selectedNode: Option[PNode with Selectable] = None

  private var _ring: Option[RingNode] = None
}

// vim: set ts=2 sw=2 et:

