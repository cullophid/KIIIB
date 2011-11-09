package simulation

import scala.util.Random

import core._
import core.devices._
import core.messages._
import simulation.messages._
import simulation.model._
import simulation.model.devices._
import simulation.model.scenario._
import utils.vec._

import scala.actors._
import scala.actors.Actor._

import java.awt.geom._

class Simulator(man: DeviceManager,
                listeners0: List[Actor],
                timeMan: TimeManager) extends Driver {

  val id = "Simulator/Virtual Driver"
  val supportedDevices = MotionSensor :: BinaryLight :: BinarySwitch :: Nil

  override def toString = id

  private var devices = Map[DriverDeviceId, PhysicalDevice]()

  private val random = new Random

  private var walls = List[PhysicalWall]()

  private var avatarActors: Map[Int, AvatarActor] = Map()

  private var listeners: List[Actor] = listeners0

  private var _time: Option[Time] = None

  /** Send a motion event to the device manager from a given local device id */
  private def sendMotionEvent(sender: DriverDeviceId, time: Double) {
    man ! DriverDeviceEventMessage(
      DeviceManager.Mapping(sender, this),
      MotionEvent(time.round.toLong)
    )
  }

  private def makeDriverDeviceList: Map[DriverDeviceId, Device] = devices transform ((_, v) => v.device)

  private def pdist(a: Vec2, s: Vec2, range: Double): Double = {
    val v = range - (a distanceTo s)

    if (v < 0)
      return 0.0

//    // Kumaraswamy distribution, see http://en.wikipedia.org/wiki/Kumaraswamy_distribution
//    val a = 1
//    val b = 3
//    return Math.min(a * b * Math.pow(v, a - 1) * Math.pow(1 - Math.pow(v, a), b - 1), 1.0)

    // Simple linear distribution
    return Math.min(2 * v / range, 1.0)
  }

  def doDriverProtocol : PartialFunction[Any, Unit] = {
    case GetDriverDeviceList=> {
      reply(SetDriverDeviceList(this, makeDriverDeviceList))
    }

    case DriverCommandMessage(recipient: DriverDeviceId, command: DeviceCommand)
      => (devices get recipient, _time) match {

      case (Some(physDev: PhysicalGenericDevice), Some(time)) => (physDev.device, command) match {

        // Binary light
        case (dev0:BinaryLight, TurnOn) => {
          devices = devices + (recipient -> physDev.copy(new BinaryLight(true)))
          man ! DriverDeviceEventMessage(DeviceManager.Mapping(recipient, this), TurnedOn(time.currentTime))
        }
        case (dev0:BinaryLight, TurnOff) => {
          devices = devices + (recipient -> physDev.copy(new BinaryLight(false)))
          man ! DriverDeviceEventMessage(DeviceManager.Mapping(recipient, this), TurnedOff(time.currentTime))
        }

        // Binary switch
        case (dev0:BinarySwitch, TurnOn) => {
          man ! DriverDeviceEventMessage(DeviceManager.Mapping(recipient, this), TurnedOn(time.currentTime))
        }
        case (dev0:BinarySwitch, TurnOff) => {
          man ! DriverDeviceEventMessage(DeviceManager.Mapping(recipient, this), TurnedOff(time.currentTime))
        }

        case _ => println("Unsupported command " + command + " for device " + id)
      }
      
      case (_, Some(_)) => println("No such device " + recipient + " or incompatible command in driver " + id)

      case _ => println(id + " needs to know what time it is before it can send device events")
    }

  }

  def doGuiSetupProtocol : PartialFunction[Any, Unit] = {
    case SetupDevices(devs) => {
      devices = devs
      man ! SetDeviceMappingsMessage(Map() ++ (devices.keys.map { id =>
        (MasterDeviceId(id.value.toInt) -> DeviceManager.Mapping(id, this))
      }))
    }
    case SetupWalls(ws) => walls = ws
  }

  def checkAvatarInSensorRange(a: Avatar, s: PhysicalMotionSensor) = {
    val l = new Line2D.Double(a.pos.x, a.pos.y, s.pos.x, s.pos.y)

    val detectionArc = new Arc2D.Double(s.pos.x - s.device.range,
                                        s.pos.y - s.device.range,
                                        2 * s.device.range,
                                        2 * s.device.range,
                                        -s.angle.toDegrees - s.device.angleSpan / 2,
                                        s.device.angleSpan, Arc2D.PIE)

    detectionArc.contains(a.pos.x, a.pos.y) &&
      ! (walls exists { w => l.intersectsLine(w.segment) } )
  }

  def doGuiProtocol : PartialFunction[Any, Unit] = {
    case msg @ AvatarMoved(_, a, time, _) => {
      listeners foreach (_ ! msg)
      devices.foreach {
        case (devId, physDev: PhysicalMotionSensor)
            if time > physDev.lastCheck + physDev.device.minCheckInterval => {

          val newLastCheck = time.round.toLong
          var newLastDetection = physDev.lastDetection
          if (time > physDev.lastDetection + physDev.device.coolDownMillis &&
              checkAvatarInSensorRange(a, physDev) &&
              random.nextDouble < pdist(a.pos, physDev.pos, physDev.device.range)) {
            newLastDetection = time.round.toLong
            sendMotionEvent(devId, time)
          }
          val physDev0 = physDev.withNewTimes(newLastCheck, newLastDetection)
          devices = devices + (devId -> physDev0)
        }
        case _ => ()
      }
    }
    case msg : NodeConsumed => listeners foreach ( _ ! msg)
  }

  def doScenarioProtocol : PartialFunction[Any, Unit] = {
    case Scenario(_, paths) => {
      avatarActors.values.foreach (_ ! Exit(this, ""))
      avatarActors = Map() ++ (paths map { p =>
        p.index -> new AvatarActor(this, p, timeMan)
      })
      devices = Map() ++ devices.map {
        case (id, physDev) => {(id, physDev.reset)}
      }
    }

    case msg @ messages.AvatarPathMessage(id, _, path) => avatarActors get id match {
      case Some(worker) => worker ! AppendPath(path)
      case None => avatarActors = avatarActors + (id -> new AvatarActor(this, msg, timeMan))
    }
  }

  override def act: Unit = {
    trapExit = true
    timeMan ! RegisterForTimeFactorChanges(this)
    loop {
    react {
      doDriverProtocol orElse
      doGuiSetupProtocol orElse
      doGuiProtocol orElse
      doScenarioProtocol orElse {
        case Ping(time) => _time = Some(time)
        case RegisterListenerMessage(l) => listeners = l :: listeners
        case UnregisterListenerMessage(l) => listeners -= l
        case Exit(_,_) =>
          avatarActors.values.foreach (_ ! Exit(this, "Resetting"))
//           println("Simulator exitting")
          exit
        case _ => ()
      }
    }
    } // loop
  }
}

// vim: set ts=2 sw=2 et:

