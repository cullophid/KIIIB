package core

/** Represents an arbitrary connection from one device to another. */
trait Connection {
  /** The id of the device that the connection goes from. */
  val from: MasterDeviceId
  /** The id of the device that the connection goes to. */
  val to: MasterDeviceId
  /** The short, human-readable string name for this type of connection. */
  val shortName: String
  /** Tells the type of this connection and what device ids it goes from and to. */
  override def toString = shortName + " (" + from + " , " + to + ")"
  /** Provides an XML representation of this connection based on device id's */
  def toXML: scala.xml.Node =
    <connection
     from={from.toString}
     to={to.toString} />
}

/** 
 * The abstract Factory for connections.
 *
 * Every concrete connection type will implement a factory that can check if 
 * two devices make a valid connection and create the concrete connection given two MasterDeviceIds
 */
abstract class ConnectionFactory {
  type F <: Device
  type T <: Device
  def isConnValid(from: Device, to: Device): Boolean

  def create(from: MasterDeviceId, to: MasterDeviceId): Connection
}


// vim: set ts=2 sw=2 et:
