package gui.model.devices


import gui.model._

import core.devices.BinaryLight

import java.awt.geom._

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.util._
import edu.umd.cs.piccolo.nodes._

class BinaryLightNode(val id: core.MasterDeviceId,
                      x: Double,
                      y: Double,
                      device0: BinaryLight,
                      scaling: Scaling) extends DeviceNode {
  type DeviceType = BinaryLight

  private var _device = device0

  val core  = makeCoreNode
  val range = makeRangeNode

  def device = _device
  def device_=(d: BinaryLight): Unit = {
    _device = d
    val c = if (d.turnedOn) new java.awt.Color(0xfce94f) else new java.awt.Color(0xeeeeec)
    range.setPaint(c)
  }


  setOffset(x, y)

  addChild(core)
  addChild(range)

  def export = new simulation.model.devices.PhysicalGenericDevice(
    device,
    utils.vec.Vec2(getXOffset / scaling.pixelsPerMeter,
                   getYOffset / scaling.pixelsPerMeter),
    getRotation
  )

  override def intersects(bounds: Rectangle2D) = core.intersects(bounds) || range.intersects(bounds)

  override protected def layoutChildren {
    core.setX(getX - 2)
    core.setY(getY - 3)
    range.setX(getX - 5)
    range.setY(getY)
//    range.setY(getY - range.getHeight / 2)
  }

  private def makeCoreNode = new PPath(new Rectangle2D.Double(0, 0, 4, 7)) with OutlinedPath {
    setPaint(new java.awt.Color(0x888a85))
    setPickable(false)
  }

  private def makeRangeNode = new PPath(new Ellipse2D.Double(5,10, 10, 10)) with OutlinedPath {
    val c = if (_device.turnedOn) new java.awt.Color(0xfce94f) else new java.awt.Color(0xeeeeec)
    setPaint(c)
    setPickable(false)
  }

  override def connectionPNode = range
  
  override def reset = device = new BinaryLight(false)
}

// vim: set ts=2 sw=2 et:

