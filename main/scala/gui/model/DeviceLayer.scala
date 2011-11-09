package gui.model

import core.MasterDeviceId

import gui.event._
import gui.model.devices._

import scala.swing.Publisher
import scala.collection.mutable._

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.event._
import edu.umd.cs.piccolo.util._
import edu.umd.cs.piccolo.nodes._

object DeviceLayer {

  /**
   * A tool or module can prevent deletion of a device when it is used in
   * some form by implementing this interface and adding the implementation
   * to DeviceLayer#deviceDependencyCheckers
   */
  trait DeviceDependencyChecker {
    def reasonNotToRemoveDevice(d: DeviceNode): Option[String]
  }
}

class DeviceLayer extends PLayer {

  /** A list of dependency checkers that are queried when a device is removed */
  val deviceDependencyCheckers: Buffer[DeviceLayer.DeviceDependencyChecker] = new ArrayBuffer()


  object inventory extends Publisher

  def devices = _devices

  def showMotionSensorRanges(v: Boolean) {
    devices.values foreach {
      case deviceNode: MotionSensorNode => deviceNode.range.setVisible(v)
      case _ => ()
    }
  }
  
  def resetDevices = _devices.values foreach (_ reset)

  override
  def addChild(device: PNode) {
    device match {
      case dev: DeviceNode =>
        super.addChild(dev)
        _devices = _devices + {(dev.id, dev)}
        inventory publish DeviceNodeAdded(dev)

      case _ => require(device.isInstanceOf[DeviceNode])
    }
  }

  def reasonsNotToRemoveChild(device: DeviceNode)
    = deviceDependencyCheckers map (_ reasonNotToRemoveDevice device) filter (_ isDefined)


  override
  def removeChild(device: PNode): PNode = {
    val removed = super.removeChild(device)

    removed match {
      case dev: DeviceNode =>
        inventory publish DeviceNodeRemoved(dev)
        _devices = _devices - dev.id
      case _ => ()
    }

    removed
  }

  def clear {
    removeAllChildren
    _devices = Map()
    _currentDeviceId = 0
  }

  def newDeviceId = {
    val id = MasterDeviceId(_currentDeviceId)
    _currentDeviceId += 1
    id
  }

  def lookup(id: MasterDeviceId) = {
    _devices get id
  }

  def export: List[simulation.model.devices.PhysicalDevice] = _devices.values map (_ export) toList

  def currentDeviceId = _currentDeviceId

  private var _devices: Map[MasterDeviceId, DeviceNode] = Map()
  private var _currentDeviceId = 0
}

// vim: set ts=2 sw=2 et:

