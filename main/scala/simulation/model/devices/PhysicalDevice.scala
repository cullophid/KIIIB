package simulation.model.devices

import core.Device
import core.devices._
import utils.vec.Vec2

object PhysicalDevice {
  def fromXML(node: scala.xml.Node): PhysicalDevice = {
    val device = (node \ "device" \ "@type").first.text match {
      case "binaryswitch" => new BinarySwitch
      case "binarylight"  => new BinaryLight(false)
      case "motionsensor" => MotionSensor.fromXML((node \ "device").first)
    }

    val angle = (node \ "@angle").first.text.toDouble
    val pos   = utils.vec.Vec2.fromXML((node \ "pos").first)

    device match {
      case dev: MotionSensor => new PhysicalMotionSensor(dev, pos, angle)
      case dev: BinaryLight  => new PhysicalBinaryLight(dev, pos, angle)
      case _                 => new PhysicalGenericDevice(device, pos, angle)
    }
  }
}
trait PhysicalDevice extends PhysicalProperties {

  type DeviceType <: Device
  val device: DeviceType

  def copy(dev: DeviceType): PhysicalDevice
  
  def reset: PhysicalDevice
}

// vim: set ts=2 sw=2 et:

