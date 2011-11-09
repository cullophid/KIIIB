package simulation.model.devices

import core.devices.BinaryLight
import utils.vec.Vec2

class PhysicalBinaryLight(val device: BinaryLight,
                          val pos: Vec2,
                          val angle: Double) extends PhysicalDevice {
  type DeviceType = BinaryLight

  def copy(dev: BinaryLight) = new PhysicalBinaryLight(dev, pos, angle)
  
  def reset = copy(new BinaryLight(false))
}

// vim: set ts=2 sw=2 et:

