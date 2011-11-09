package gui.model

import scala.actors.Actor
import scala.actors.Actor.loop
import scala.actors.Exit

import core._

import core.devices._

import core.messages._

import gui.model.devices._

class DeviceNodeActor(man: DeviceManager, deviceLayer: DeviceLayer) extends utils.SwingActor {

  override def act = {
    man ! RegisterListenerMessage(this)
    trapExit = true
    loop {
      react {
        case DeviceEventMessage(id, e) => (e, deviceLayer lookup id) match {
          case (MotionEvent(millis),
                Some(dev: MotionSensorNode)) => {
            dev.animateUponDetection
          }
          case (TurnedOn(timestamp),
                Some(dev: BinaryLightNode)) => dev.device = new BinaryLight(true)
          case (TurnedOff(timestamp),
                Some(dev: BinaryLightNode)) => dev.device = new BinaryLight(false)
          case _ => ()
        }
        case Exit(_,_) =>
//           println("DeviceNodeActor exitting")
          exit

        case _ => ()
      }
    }
  }
}

// vim: set ts=2 sw=2 et:

