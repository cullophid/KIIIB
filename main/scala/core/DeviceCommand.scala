package core

/** A command that is supposed to be sent through the actors system. */
abstract class DeviceCommand {
  def toXML: scala.xml.Node
}

object DeviceCommand {
  def fromXML(node: scala.xml.Node): DeviceCommand =
    (node \ "@type").first.text match {
      case "turnon" => devices.TurnOn
      case "turnoff" => devices.TurnOff
      case other => error("Dont know how to handle DeviceCommand type \"" + other + "\"")
    }
}


// vim: set ts=2 sw=2 et:
