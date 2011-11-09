package gui.model.devices

import gui.model._

import core.devices.BinarySwitch

import java.awt.geom._

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.util._
import edu.umd.cs.piccolo.nodes._

class BinarySwitchNode(val id: core.MasterDeviceId,
                       x: Double,
                       y: Double,
                       var device: BinarySwitch,
                       scaling: Scaling) extends DeviceNode {
  type DeviceType = BinarySwitch

  val core = makeCoreNode
  val btn1 = makeButtonNode(new java.awt.Color(0x73d216), "Turn on")
  val btn2 = makeButtonNode(new java.awt.Color(0xcc0000), "Turn off")

  setOffset(x, y)

  addChild(core)
  addChild(btn1)
  addChild(btn2)

  def export = new simulation.model.devices.PhysicalGenericDevice(
    device,
    utils.vec.Vec2(getXOffset / scaling.pixelsPerMeter,
                   getYOffset / scaling.pixelsPerMeter),
    getRotation
  )

  override def intersects(bounds: Rectangle2D) = core.intersects(bounds)

  override protected def layoutChildren {
    core.setX(getX - 10)
    core.setY(getY - 5)
    btn1.setX(getX - 8)
    btn1.setY(getY - 3)
    btn2.setX(getX + 2)
    btn2.setY(getY - 3)
  }

  private def makeCoreNode = new PPath(new Rectangle2D.Double(0, 0, 20, 10)) with OutlinedPath {
    setPaint(new java.awt.Color(0xd3d7cf))
    setPickable(false)
  }

  private def makeButtonNode(color: java.awt.Color, tooltip0: String) = new PPath(new Rectangle2D.Double(0, 0, 5, 6))
                                                          with OutlinedPath
                                                          with ShouldNotDrag
                                                          with HasTooltip {
    def tooltip = BinarySwitchNode.this.tooltip + ": " + tooltip0

    setPaint(color)
    setPickable(true)
  }

  override val connectionPNode = core
}

// vim: set ts=2 sw=2 et:

