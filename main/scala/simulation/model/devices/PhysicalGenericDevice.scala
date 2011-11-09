package simulation.model.devices

import core.Device
import utils.vec.Vec2

class PhysicalGenericDevice(val device: Device,
                            val pos: Vec2,
                            val angle: Double) extends PhysicalDevice {
  type DeviceType = Device

  def copy(dev: Device) = new PhysicalGenericDevice(dev, pos, angle)
  
  def reset = copy(device)
}

// vim: set ts=2 sw=2 et:

