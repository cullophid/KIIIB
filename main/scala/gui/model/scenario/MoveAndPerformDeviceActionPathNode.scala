package gui.model.scenario

import core.messages.DriverCommandMessage
import utils.vec.Vec2

import edu.umd.cs.piccolo.PLayer

class MoveAndPerformDeviceActionPathNode(x0: Double, y0: Double, speed0: Double, scaling: gui.model.Scaling, storyText0: Option[String], val msg: DriverCommandMessage) extends PathNode(x0, y0, speed0, scaling, storyText0) {
  def this(x1: Double, y1: Double, speed1: Double, scaling: gui.model.Scaling, msg1: DriverCommandMessage) = this(x1, y1, speed1, scaling, None, msg1)
  def this(pos1: Vec2, speed1: Double, scaling: gui.model.Scaling, storyText1: Option[String], msg1: DriverCommandMessage) = this(pos1.x, pos1.y, speed1, scaling, storyText1, msg1)

  posNode.setPathTo(new java.awt.geom.Rectangle2D.Double(-4, -4, 8, 8))
  timeNode.setPathTo(new java.awt.geom.Rectangle2D.Double(-2, -4, 4, 8))

  posNode setPaint new java.awt.Color(0xcc0000)
  timeNode setPaint new java.awt.Color(0xcc0000)

  def toAvatarPathNode = simulation.model.scenario.DeviceActionNode(pos / scaling.pixelsPerMeter, _speed, storyText, msg)

  override protected def storyTextPrefix = Some(msg.cmd.toString + "->" + msg.recipient.toString)
}

// vim: set ts=2 sw=2 et:

