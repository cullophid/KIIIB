package ai.impl

import core._
import core.devices._
import core.messages._

import java.util.Timer
import utils.RichTimer._

class SimpleAi extends AiListener {

  def aiStarted(ctrl: AiController): Unit = { }
  def aiStopped: Unit = { }

  def deviceEventReceived(c: AiController,
                          id: MasterDeviceId,
                          device: Device,
                          event: DeviceEvent): Unit = {
    (device, event) match {
      case (_: BinarySwitch, TurnedOn(time)) => c.connectionsFrom(id) foreach {
        x => c.sendDeviceCommand(x.to, TurnOn)
      }
      case (_: BinarySwitch, TurnedOff(time)) => c.connectionsFrom(id) foreach {
        x => c.sendDeviceCommand(x.to, TurnOff)
      }
      case (_: MotionSensor, MotionEvent(time)) => c.connectionsFrom(id) foreach {
        x => {
          c.sendDeviceCommand(x.to, TurnOn)
          c.scheduleDeviceCommand(x.to, TurnOff, time + 5000)
        }
      }
      case _ => ()
    }
  }
}

// vim: set ts=2 sw=2 et:

