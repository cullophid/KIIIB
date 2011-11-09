package ai.impl

import core._
import core.devices._
import core.messages._

import java.util.Timer
import utils.RichTimer._
class SimpleAi extends AiListener {
    
    def aiStarted(ctrl: AiController): Unit = { }
    def aiStopped: Unit = { }

    def deviceEventReceived(c: AiController,id: MasterDeviceId,device: Device,event: DeviceEvent): Unit = {
        (device, event) match {
            case (_: BinarySwitch, TurnedOn(time)) => 
            println("switched on "+id)

            c.connectionsFrom(id) foreach {
                x => c.sendDeviceCommand(x.to, TurnOn)
            }
            case (_: BinarySwitch, TurnedOff(time)) => 
                println("switched off "+id)
                c.connectionsFrom(id) foreach {
                    x => c.sendDeviceCommand(x.to, TurnOff)
                }
            case (_: MotionSensor, MotionEvent(time)) => 
                println("Sensor id :"+id)
            case _ => ()
        }
    }
}

// vim: set ts=2 sw=2 et:

