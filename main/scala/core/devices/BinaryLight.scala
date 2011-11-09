package core.devices

case class TurnedOn(timestamp: Double) extends DeviceEvent {
  def toXML: scala.xml.Node = <event type="turnedon" timestamp={timestamp.toString}/>
}
case class TurnedOff(timestamp: Double) extends DeviceEvent {
  def toXML: scala.xml.Node = <event type="turnedoff" timestamp={timestamp.toString}/>
}

object BinaryLight extends MetaDevice {
  val shortName = "Binary light"
}
class  BinaryLight(val turnedOn:Boolean) extends Device {
  val meta = BinaryLight

  def toXML = <device type="binarylight"/>
}

// vim: set et sw=2 ts=2:

