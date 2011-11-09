package core.devices

case object TurnOn extends DeviceCommand {
  def toXML = <command type="turnon" />
}
case object TurnOff extends DeviceCommand {
  def toXML = <command type="turnoff" />
}


object BinarySwitch extends MetaDevice {
  val shortName = "Binary switch"
}

class  BinarySwitch extends Device {
  val meta = BinarySwitch
  def toXML = <device type="binaryswitch"/>
}


// vim: set ts=2 sw=2 et:
