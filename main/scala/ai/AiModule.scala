package ai

import core._
import core.devices._
import core.messages._

import scala.actors.Actor
import scala.actors.Actor.loop
import scala.actors.Exit

case class  SetAi(ai: Option[AiListener])
case object TurnOnRecording
case object TurnOffRecording
case object GetEventLog
case class  SetEventLog(log: List[DeviceEventMessage])
case class  EventLog(log: List[DeviceEventMessage])
/**
 * Represents the artificial intelligence component, which is responsible for
 * reacting to DeviceEventMessages.
*/
class AiModule(man: DeviceManager, timeMan: TimeManager) extends Actor {
  private var masterList: Map[MasterDeviceId, Device] = _
  private var conns: List[Connection] = Nil

  private var scheduledCommands = Map[Tuple2[MasterDeviceId, DeviceCommand], Double]()

  private val _aiControllerDelegate = new AiController {
    def connections: List[Connection] = conns
    def connectionsFrom(id: MasterDeviceId): List[Connection] = conns filter (_.from == id)
    def eventLog = _eventLog
    def masterList: Map[MasterDeviceId, Device] = masterList
    def sendDeviceCommand(devId: MasterDeviceId, cmd: DeviceCommand): Unit
      = man ! DeviceCommandMessage(devId, cmd)
    def scheduleDeviceCommand(id: MasterDeviceId, cmd: DeviceCommand, time: Double) =
      scheduledCommands = scheduledCommands + {((id, cmd), time)}
  }

  private var _aiListener: Option[AiListener] = None

  private var _eventLog: List[DeviceEventMessage] = Nil
  private var _isRecording = false

  import utils.RichTimer._

  // Register in DeviceManager
  man ! RegisterListenerMessage(this)
  timeMan ! RegisterForTimeUpdates(this, 250)

  /**
   * Handles incoming information from the DeviceManager according to the
   * current AI protocol.
   * @todo Delegate actions to separate AIProtocol class.
  */
  def act = {
    trapExit = true
    loop {

    react {
      case SetDeviceConnectionsMessage(conns) => this.conns = conns
      case msg @ DeviceEventMessage(id, event: DeviceEvent) => {
        if (_isRecording) _eventLog = msg :: _eventLog
        _aiListener map { ai =>
          masterList get id map {
            dev => ai.deviceEventReceived(_aiControllerDelegate, id, dev, event)
          }
        }
      }

      case TurnOnRecording  => _isRecording = true
      case TurnOffRecording => _isRecording = false
      case SetMasterDeviceListMessage(list) => masterList = list
      case SetAi(ai) => _aiListener = ai

      case GetEventLog      => reply(EventLog(_eventLog))
      case SetEventLog(log) => _eventLog = log

      case Ping(time) => {
        val now = time.currentTime
        scheduledCommands filter {
          case ((_, _), timestamp) if timestamp < now => true
          case _                                 => false
        } foreach {
          case ((id, cmd), _) => {
            _aiControllerDelegate.sendDeviceCommand(id, cmd)
            scheduledCommands = scheduledCommands - {(id, cmd)}
          }
        }
      }
      case Exit(_,_) =>
        timeMan ! RemoveFromTimeUpdates(this)
//         println("AiModule exitting")
        exit

      case _ => ()
    }
    } // loop
  }
}

// vim: set ts=2 sw=2 et:

