package simulation.model.devices

import core.devices.MotionSensor
import utils.vec.Vec2

class PhysicalMotionSensor(val device: MotionSensor,
                           val pos: Vec2,
                           val angle: Double,
                           val lastCheck: Long,
                           val lastDetection: Long) extends PhysicalDevice {

  def this(device1: MotionSensor, pos1: Vec2, angle1: Double) = this(device1, pos1, angle1, Math.MIN_LONG, Math.MIN_LONG)

  type DeviceType = MotionSensor

  def copy(device1: MotionSensor)
    = new PhysicalMotionSensor(device1, pos, angle, lastCheck, lastDetection)

  def withNewTimes(newLastCheck: Long, newLastDetection: Long)
    = new PhysicalMotionSensor(device, pos, angle, newLastCheck, newLastDetection)

  override def toString = "PhysicalMotionSensor(device="+device + ", pos=" + pos +
                          ", angle="+angle + ", lastCheck="+lastCheck+", lastDetection="+lastDetection+")"
                          
  def reset = withNewTimes(Math.MIN_LONG, Math.MIN_LONG)
}

// vim: set ts=2 sw=2 et:

