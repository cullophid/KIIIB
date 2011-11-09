package core
import messages._

import scala.actors.Actor
import scala.actors.Actor._
import scala.actors.Exit

/** The DeviceManager's companion object. */
object DeviceManager {
  /** A mapping from a driver's id to the relevant driver. */
  case class Mapping (id: DriverDeviceId, driver: Driver)

  /** Type specifying a map of Mapping's. */
  type Mappings = Map[MasterDeviceId, Mapping]
  /** Type specifying an inverse map of Mapping's. */
  type InverseMappings = Map[Mapping, MasterDeviceId]
}
/**
 * Works as a bus between drivers and other components such as the AI.
 * Maintains mappings from the general MasterDeviceIds to the more specific
 * pairs of DriverDeviceIds and corresponding drivers.
 */
class DeviceManager extends Actor {
  import DeviceManager._

  private var activeMappingSet: Mappings = Map()
  private var inverseMappings: InverseMappings = Map()
  private var allMappings: Map[String, Mappings] = Map()

  private var listeners = Set[Actor]()

  private var nonSimMappingSet: Mappings = Map()

  private var masterDeviceList : Map[MasterDeviceId, Device] = Map()
  private var remainingDrivers: List[Driver] = Nil
  private var connections: List[Connection] = Nil

  private def buildInverseMappings(ms : Mappings): Map[Mapping, MasterDeviceId]
    = Map() ++ (ms map {
      case (k, v) => (v, k)
    })

  def getMapping(id: MasterDeviceId, driver: String): Option[DriverDeviceId] = {
    allMappings get driver match {
      case Some(ms) => {
        ms get id match {
          case Some(m) => return Some(m id)
          case None => return None
        }
      }
      case None => None
    }
  }
  /**
   * Replace master device list devices from driver with new.
   */
  private def buildMasterDeviceList(driver: Driver, devices: Map[DriverDeviceId, Device])
    : Map[MasterDeviceId, Device] = {

    // find previously added devices from that driver
    val remkeys = for {
      deviceId <- devices.keys.toList
      masterId <- inverseMappings get Mapping(deviceId, driver)
    } yield masterId


    // remove all mappings from device
    val masterList = masterDeviceList -- remkeys

    // add the new mappings
    // TODO: does not test if the devices have a master!
    masterList ++ (devices filter {
      case (k, _) => (inverseMappings contains Mapping(k, driver))
    } map {
      case (k, v) => (inverseMappings(Mapping(k, driver)), v)
    })
  }
  
  /**
   * Receives information from drivers, passes it on to registered listeners and
   * provides mappings from MasterDeviceIds to device objects.
   */
  def act {
    trapExit = true;
    loop {

    react {
      case SetDeviceMappingsMessage(mappings) => {
        activeMappingSet = mappings
        inverseMappings = buildInverseMappings(mappings)
        // request local driver device lists
        remainingDrivers = activeMappingSet.values.toList.map(s => s.driver).removeDuplicates
        remainingDrivers foreach (_ ! GetDriverDeviceList)

        if (mappings isEmpty)
          listeners foreach (_ ! SetMasterDeviceListMessage(masterDeviceList))
        
        mappings foreach {
          case (key, value) => {
        	val driver: String = value.driver.id
            allMappings get driver match {
              case Some(mapping) => {
                val m = mapping + (key -> value)
                allMappings += (driver -> m)
              }
              case None => {
                val m: Mappings = Map(key -> value)
                allMappings += (driver -> m)
              }
            }
          }
        }
      }

      case SetDriverDeviceList(driver, deviceMap) => {
        masterDeviceList = buildMasterDeviceList(driver, deviceMap)

        remainingDrivers = remainingDrivers - driver

        if (remainingDrivers isEmpty)
          listeners foreach (_ ! SetMasterDeviceListMessage(masterDeviceList))
      }

      case RegisterListenerMessage(listener) => 
        if (remainingDrivers.isEmpty) listener ! SetMasterDeviceListMessage(masterDeviceList)
        listener ! SetDeviceConnectionsMessage(connections)
        listeners += listener

      // Command from controller (simulator, ai) is translated and forwarded to driver
      case DeviceCommandMessage(recipient: MasterDeviceId, cmd: DeviceCommand) => {
        activeMappingSet get recipient match {
          case Some(mapping) => mapping.driver ! DriverCommandMessage(mapping.id, cmd)
          case None          => println("local id does not correspond to master device")
        }
      }

      // A device event from a driver is translated and forwarded to listeners
      case DriverDeviceEventMessage(sender: Mapping, e: DeviceEvent) => {
        inverseMappings get sender match {
          case Some(id) => {
            listeners foreach (_ ! DeviceEventMessage(id, e))
            //println(sender.id + " -> " + id)
          }
          case None     => println(sender.id + " does not correspond to any master device. Go fish.")
        }
      }

      case msg@SetDeviceConnectionsMessage(conns) => 
        connections = conns
        listeners foreach (_! msg)

      case Exit(_,_) =>
//           println("DeviceManager exitting")
          exit

      case msg => println("Unhandled message type: " + msg + ".")
    }
  }
  } // loop
}


// vim: set ts=2 sw=2 et:
