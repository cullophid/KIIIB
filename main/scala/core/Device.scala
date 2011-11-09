package core

/** Meta trait for device companion objects (to be able to match devices on type) */
trait MetaDevice {
  /** The short name of the device type. */
  val shortName: String
  override val toString = shortName
}

/** Base device type. */
abstract class Device {

  val meta: MetaDevice

  val shortName = meta.shortName

  def toXML: scala.xml.Node
}


// vim: set ts=2 sw=2 et:
