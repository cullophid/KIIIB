package gui.model.devices

import core.MasterDeviceId
import core.devices._
import gui.ContextMenu
import simulation.model.devices._

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.event.PInputEvent
import edu.umd.cs.piccolo.event.PBasicInputEventHandler
import java.beans.{PropertyChangeListener, PropertyChangeEvent}
import java.awt.Color

/** Factory class for construction device nodes from PhysicalDevices */
class DeviceNodeFactory(scaling: Scaling) {
  def createFromPhysical(physDev: PhysicalDevice, id: MasterDeviceId) = physDev.device match {
    case dev: BinarySwitch => new BinarySwitchNode(id, physDev.pos.x, physDev.pos.y, dev, scaling){ setRotation(physDev.angle) }
    case dev: BinaryLight  => new BinaryLightNode(id, physDev.pos.x, physDev.pos.y, dev, scaling){ setRotation(physDev.angle) }
    case dev: MotionSensor => new MotionSensorNode(id, physDev.pos.x, physDev.pos.y, dev, scaling){ setRotation(physDev.angle) }
  }
}

/**
 * The base gui Device node class that visualises a core.Device in the gui.
 *
 * By default it shows the master device id.
 */
abstract class DeviceNode extends PNode with Outlined with ConnectedNode with Selectable with HasTooltip {
  type DeviceType <: core.Device
  var device: DeviceType
  val id: MasterDeviceId

  def export: PhysicalDevice

  def tooltip = device.shortName + " (" + id.value.toString + ")"

  def reset {}
}

abstract class DeviceContextMenu extends ContextMenu {
  type NodeType <: DeviceNode
}

// vim: set ts=2 sw=2 et:

