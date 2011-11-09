package ai

import core._
import core.devices._
import core.messages._


trait AiController {
  def connectionsFrom(id: MasterDeviceId): List[Connection]
  def connections: List[Connection]
  def masterList: Map[MasterDeviceId, Device]
  def sendDeviceCommand(devId: MasterDeviceId, cmd: DeviceCommand): Unit
  def scheduleDeviceCommand(id: MasterDeviceId, cmd: DeviceCommand, delay: Double): Unit
  def eventLog: List[DeviceEventMessage]
}

// vim: set ts=2 sw=2 et:

