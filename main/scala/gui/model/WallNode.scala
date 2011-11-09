package gui.model

import java.awt.geom.Ellipse2D

import edu.umd.cs.piccolo.nodes.PPath

class WallNode extends PPath with OutlinedPath with ConnectedNode with Selectable {

  setPathTo(new Ellipse2D.Double(-2.5, -2.5, 5, 5))
  setPaint(new java.awt.Color(0x888a85))

  override def toString = "(" + getXOffset + "," + getYOffset + ")"
}

// vim: set ts=2 sw=2 et:

