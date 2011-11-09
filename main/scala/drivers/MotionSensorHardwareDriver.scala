package drivers

import core._
import core.devices._
import core.messages._

import scala.actors.Actor.loop
import scala.actors.Exit

import java.util.Timer
import utils.RichTimer._

case object Poll

class MotionSensorHardwareDriver(man: DeviceManager) extends Driver {
  val supportedDevices = MotionSensor :: Nil
  val id = "Dummy sensor: "
  private val devices = Map[DriverDeviceId, Device](
    DriverDeviceId("#0") -> new MotionSensor(20.0),
    DriverDeviceId("#1") -> new MotionSensor(30.0),
    DriverDeviceId("#2") -> new MotionSensor(20.0)
  )

  private val random = new java.util.Random

  override def act:Unit = {
    trapExit = true

    val timer = new Timer

    val pollTask = timer.scheduleRepeatedly(0, 1000) {
      this ! Poll
    }


    loop {
    react {
      case Poll => {
        // TODO: poll database. Poll message is sent by PollActorsomething?
    	print("poll")
        //if (random.nextInt(5) == 0) {
    	if (true) {
          val idx = random.nextInt(devices.size)
          print("\tidx:"+idx)
          val id = devices.keys.drop(idx).next
          print("\tid:"+id)
          man ! DriverDeviceEventMessage(DeviceManager.Mapping(id, this), MotionEvent(System.currentTimeMillis))
        }
    	println
      }
      case GetDriverDeviceList=>reply(SetDriverDeviceList(this, devices))

      case DriverCommandMessage(recipient: DriverDeviceId, command: DeviceCommand) => { }
      case Exit(_,_) =>
        println("MotionSensorHardwareDriver exitting")
        pollTask.cancel
        timer.cancel
        exit
      case m => println("MotionSensorHardwareDriver does not understand " + m)

    }
    } // loop
  }
}

// vim: set et sw=2 ts=2:

