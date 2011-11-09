package gui.event

import scala.swing.event.Event

import gui.model.devices.DeviceNode

sealed abstract class DeviceNodeEvent extends Event
case class DeviceNodeAdded(node: DeviceNode) extends DeviceNodeEvent
case class DeviceNodeRemoved(node: DeviceNode) extends DeviceNodeEvent
case class DeviceNodeUpdated(node: DeviceNode) extends DeviceNodeEvent

// vim: set ts=2 sw=2 et:

