package gui.model.scenario

import utils.vec.Vec2

import edu.umd.cs.piccolo.PLayer

class MovePathNode(x0: Double, y0: Double, speed0: Double, scaling: gui.model.Scaling, storyText0: Option[String]) extends PathNode(x0, y0, speed0, scaling, storyText0) {
  def this(x1: Double, y1: Double, speed1: Double, scaling: gui.model.Scaling) = this(x1, y1, speed1, scaling, None)
  def this(speed1: Double, scaling: gui.model.Scaling) = this(0, 0, speed1, scaling)
  def this(pos1: Vec2, speed1: Double, scaling: gui.model.Scaling, storyText1: Option[String]) = this(pos1.x, pos1.y, speed1, scaling, storyText1)

  posNode setPaint new java.awt.Color(0x73d216)
  timeNode setPaint new java.awt.Color(0x73d216)

  def toAvatarPathNode = simulation.model.scenario.MoveNode(pos / scaling.pixelsPerMeter, _speed, storyText)
}

// vim: set ts=2 sw=2 et:

