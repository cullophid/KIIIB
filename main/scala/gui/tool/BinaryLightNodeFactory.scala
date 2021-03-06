package gui.tool

import core.MasterDeviceId
import core.devices._
import gui.model._
import gui.model.devices._
import utils.vec.Vec2

import scala.swing._

import java.awt.geom.Point2D

class BinaryLightNodeFactory(scaling: Scaling) extends DeviceNodeFactory {
  val propertiesPanel = new FlowPanel
  def createDevice(id: MasterDeviceId, position: Point2D)
    = new BinaryLightNode(id,
                          position.getX,
                          position.getY,
                          new BinaryLight(false),
                          scaling)

  val toolTitle = "Binary light"
}

// vim: set ts=2 sw=2 et:

