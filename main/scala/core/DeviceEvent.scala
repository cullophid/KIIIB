package core

/**
 * An event that has occurred and is supposed to be sent through the actors
 * system.
 */
abstract class DeviceEvent {
  val timestamp: Double
  def toXML: scala.xml.Node
}


// vim: set ts=2 sw=2 et:
