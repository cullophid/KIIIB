package ai

import core.MasterDeviceId
import core.Device
import core.DeviceEvent


trait AiListener {
  def aiStarted(ctrl: AiController)
  def aiStopped

  def deviceEventReceived(c: AiController, id: MasterDeviceId, device: Device, event: DeviceEvent)
}

// vim: set ts=2 sw=2 et:

