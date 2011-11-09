package gui.model.scenario

import utils.vec.Vec2

import edu.umd.cs.piccolo.PLayer

class MoveAndDelayPathNode(x0: Double, y0: Double, speed0: Double, scaling: gui.model.Scaling, storyText0: Option[String], val delay0: Double) extends PathNode(x0, y0, speed0, scaling, storyText0) {
  def this(x1: Double, y1: Double, speed1: Double, scaling: gui.model.Scaling, delay1: Double) = this(x1, y1, speed1, scaling, None, delay1)
  def this(pos1: Vec2, speed1: Double, scaling: gui.model.Scaling, storyText1: Option[String], delay1: Double) = this(pos1.x, pos1.y, speed1, scaling, storyText1, delay1)

  def delay = _delay
  def delay_=(v: Double) {
    _delay = if (v >= 0) v else 0
    timeNode.setPathTo(new java.awt.geom.Rectangle2D.Double(-1, -4, 2 + delay * PathNode.pixelsPerSecond, 8))
    posNodeChanged
  }
  private var _delay = delay0

  posNode.setPathTo(new java.awt.geom.Rectangle2D.Double(-4, -4, 8, 8))
  timeNode.setPathTo(new java.awt.geom.Rectangle2D.Double(-1, -4, 2 + delay * PathNode.pixelsPerSecond, 8))

  posNode setPaint new java.awt.Color(0x3465a4)
  timeNode setPaint new java.awt.Color(0x3465a4)

  override def endTime = startTime + delay

  def toAvatarPathNode = simulation.model.scenario.DelayNode(pos / scaling.pixelsPerMeter, _speed, storyText, delay * 1000)

  override protected def storyTextPrefix = Some("%1.1f s wait".format(delay))
}

// vim: set ts=2 sw=2 et:

