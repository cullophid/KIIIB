package core.devices.connections

case class BinarySwitchToBinaryLightConn(val from: MasterDeviceId,
                                         val to: MasterDeviceId) extends Connection {
  val shortName = "Binary Switch -> Binary Light conn"
}

class BinarySwitchToBinaryLightConnFactory extends ConnectionFactory {
  type F = BinarySwitch
  type T = BinaryLight
  def isConnValid(from: Device, to: Device) = from.isInstanceOf[F] && to.isInstanceOf[T]
  def create(from: MasterDeviceId, to: MasterDeviceId) = BinarySwitchToBinaryLightConn(from, to)
}

case class MotionSensorToBinaryLightConn(val from: MasterDeviceId,
                                         val to: MasterDeviceId) extends Connection {
  val shortName = "Motion Sensor -> Binary Light conn"
}

class MotionSensorToBinaryLightConnFactory extends ConnectionFactory {
  type F = MotionSensor
  type T = BinaryLight

  def isConnValid(from: Device, to: Device) = from.isInstanceOf[F] && to.isInstanceOf[T]
  def create(from: MasterDeviceId, to: MasterDeviceId) = MotionSensorToBinaryLightConn(from, to)
}

case class MotionSensorToDimmerLightConn(val from: MasterDeviceId,
                                         val to: MasterDeviceId) extends Connection {
  val shortName = "Motion Sensor -> Dimmer Light conn"
}

// vim: set ts=2 sw=2 et:

