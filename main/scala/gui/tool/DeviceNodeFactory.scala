package gui.tool

import core.MasterDeviceId
import gui.model.devices.DeviceNode
import utils.vec.Vec2

import scala.swing.Panel

import java.awt.geom.Point2D

abstract class DeviceNodeFactory {
  def createDevice(id: MasterDeviceId, position: Point2D): DeviceNode

  val propertiesPanel: Panel

  val toolTitle: String
}

// vim: set ts=2 sw=2 et:

