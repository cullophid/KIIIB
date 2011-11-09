package core.messages

import core._
import core.devices._

import utils.SwingActor

import scala.actors.Actor

/**
 * A command message sent to a device identified by its MasterDeviceId.
 * @param recipient
 *   The MasterDeviceId of the recipient.
 * @param cmd
 *   The command meant to be given to the recipient.
 */
case class DeviceCommandMessage(recipient: MasterDeviceId, cmd: DeviceCommand)

object DriverCommandMessage {
  def fromXML(node: scala.xml.Node): DriverCommandMessage =
    DriverCommandMessage(DriverDeviceId((node \ "@recipient").first.text),
                         DeviceCommand.fromXML((node \ "command").first))
}
/**
 * A command message sent to a device identified by its DriverDeviceId.
 * @param recipient
 *   The DriverDeviceId of the recipient.
 * @param cmd
 *   The command meant to be given to the recipient.
 */
case class DriverCommandMessage(recipient: DriverDeviceId, cmd: DeviceCommand) {
  def toXML = <msg recipient={recipient.value}> { cmd.toXML } </msg>
}

/**
 * An event message related to a device specified by a MasterDeviceId.
 * @param id
 *   The MasterDeviceId of the related device.
 * @param cmd
 *   The event related to that device.
 */
case class DeviceEventMessage(id: MasterDeviceId, e: DeviceEvent) {
  def toXML: xml.Node =
    <device-event-message id={id.toString}>
      {e.toXML}
    </device-event-message>
}
/**
 * An event message related to a device specified by a DriverDeviceId.
 * @param id
 *   The DriverDeviceId of the related device.
 * @param cmd
 *   The event related to that device.
 */
case class DriverDeviceEventMessage(sender: DeviceManager.Mapping, e: DeviceEvent)

/** A message which is sent to request a device list from a driver. */
case object GetDriverDeviceList
/**
 * A message containing a driver and the related DriverDeviceIds and devices.
 * @param driver
 *   The relevant driver.
 * @param m
 *   A map containing the driver's DriverDeviceIds and corresponding device
 *   objects.
 */
case class SetDriverDeviceList(driver: Driver, m: Map[DriverDeviceId, Device])

/**
 * A message containing the MasterDeviceIds and related device objects to be
 * sent to listeners when the current mapping is changed.
 * @param masterList
 *   The aforementioned MasterDeviceIds and related device objects.
 */
case class SetMasterDeviceListMessage(masterList: Map[MasterDeviceId, Device])

/**
 * A message containing a new map of mappings to be used by the DeviceManager.
 * @param mappings
 *   The mappings in a map from MasterDeviceId to Mapping.
 */
case class SetDeviceMappingsMessage(mappings: DeviceManager.Mappings)

/**
 * A message to let the DeviceManager know that the specified actor wishes to
 * receive updates from the DeviceManager when changes occur in mappings, etc.
 * @todo Find out what exactly "etc." means.
 * @param l
 *   The actor to register as a listener.
 */
case class RegisterListenerMessage(l:Actor)

case class UnregisterListenerMessage(l:Actor)

/**
 * A message containing a new list of connections between devices. To be used by
 * the AI component and other interested parties.
 * @param conns
 *   The new list of connections.
 */
case class SetDeviceConnectionsMessage(conns: List[Connection])

sealed abstract class TimeManagerMessage
case class RegisterForTimeUpdates(l: Actor, interval: Double) extends TimeManagerMessage
case class RegisterForTimeFactorChanges(l: Actor) extends TimeManagerMessage
case class RemoveFromTimeUpdates(l: Actor) extends TimeManagerMessage
case class SetSpeed(speed: Double) extends TimeManagerMessage
case class SetInitialTime(initialTime: Double) extends TimeManagerMessage
case class Ping(time: Time)

// vim: set ts=2 sw=2 et:

