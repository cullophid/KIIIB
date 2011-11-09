package gui.tool

import gui.model.NodeSelection
import gui.model.devices.DeviceNode

import scala.swing._
import scala.swing.event._

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.event._
import edu.umd.cs.piccolo.util._
import edu.umd.cs.piccolo.nodes._



case class DeviceListItem(id: core.DriverDeviceId,
                          device: core.Device,
                          driver: core.Driver,
                          var masterId: Option[core.MasterDeviceId])

class DeviceListView extends ListView[DeviceListItem] {
  renderer = new ListView.Renderer[DeviceListItem] {
    def componentFor(list: ListView[_],
                     isSelected: Boolean,
                     hasFocus: Boolean,
                     a: DeviceListItem,
                     index: Int): Component = new Label {

      val mappedId = a.masterId map { " (" + _.value + ")" } getOrElse ("")
      text = a.driver.id + ": " + a.id.value + mappedId
      tooltip = text + " (" + a.device.shortName + ")"
      peer.setOpaque(true) // scala.swing doesn't yet map opaque?
      horizontalAlignment = Alignment.Left
      border = Swing.EmptyBorder(0, 5, 0, 5)
      background = if (isSelected) list.selectionBackground else list.background
      foreground = if (isSelected) list.selectionForeground else list.foreground
    }
  }
}

import scala.actors.Actor.loop
import scala.actors.Exit
import core.messages._

case object QueryDrivers
class DriverDeviceToDeviceListViewActor(drivers: Seq[core.Driver]) extends utils.SwingActor {
  private var _counter = 0
  private var _devices: Map[core.Driver, Seq[(core.DriverDeviceId, core.Device)]] = Map()
  private var _deviceList: Option[DeviceListView] = None

  def act = {
    trapExit = true;
    this ! QueryDrivers

    loop {
      react {
        case SetDriverDeviceList(driver, devices) if _deviceList.isDefined =>
          _devices = _devices + {(driver, devices.toList)}
          _counter -= 1
          if (_counter < 1) {
            val list: Seq[DeviceListItem] = _devices flatMap {
              case (driver, mapping) => mapping map { case (id, dev) => DeviceListItem(id, dev, driver, None) } toList
            } toList

            _deviceList.get.listData = list
          }

        case QueryDrivers => queryDrivers
        case Exit(_,_) =>
//           println("DriverDeviceToDeviceListViewActor exitting")
          exit
        case c: DeviceListView => _deviceList = Some(c)
      }
    }
  }

  private def queryDrivers {
    _counter = _counter + drivers.length
    drivers foreach { _ ! GetDriverDeviceList }
  }

}

class MappingTool(val controller: ToolController,
                  selection: NodeSelection,
                  man: core.DeviceManager,
                  listenerActor: DriverDeviceToDeviceListViewActor) extends Tool {

  private var _mappings: core.DeviceManager.Mappings = Map()

  val name = "Edit mappings"
  
  override def toolClear {
    _mappings = Map()
  }

  private def addNewMapping(masterId: core.MasterDeviceId,
                            driverDeviceId: core.DriverDeviceId,
                            driver: core.Driver) {
    _mappings = _mappings + {(masterId, core.DeviceManager.Mapping(driverDeviceId, driver))}

    val list = settingsPanel.devices.listData

    val oldMappings = list.filter {
      case DeviceListItem(_, _, _, Some(id)) if id == masterId => true
      case _ => false
    }

    oldMappings foreach { _.masterId = None }

    val itemIndex = list.findIndexOf (x => x.id == driverDeviceId && x.driver == driver)
    assert(itemIndex != -1)

    val item = list(itemIndex)
    item.masterId = Some(masterId)

    settingsPanel.devices.repaint
  }

  val settingsPanel = new BorderPanel {
    val devices = new DeviceListView {
//      fixedCellWidth = 100
    }

    val mapButton = new Button("Map to selection") { peer setMnemonic 'm' }
    val activateMappingBtn = new Button("Activate")

    val ctrlPanel = new BoxPanel(Orientation.Horizontal) {
      contents += mapButton
      contents += activateMappingBtn
    }

    layout(new ScrollPane(devices)) = BorderPanel.Position.Center
    layout(ctrlPanel) = BorderPanel.Position.South

    listenTo(mapButton, activateMappingBtn)
    reactions += {
      case ButtonClicked(`activateMappingBtn`) => man ! SetDeviceMappingsMessage(_mappings)
      case ButtonClicked(`mapButton`) =>
        ( devices.selection.items.firstOption
        , selection.selectedNode ) match {
          case ( Some(item)
               , Some(n: DeviceNode) ) =>
            if (item.device.meta == n.device.meta)
              addNewMapping(n.id, item.id, item.driver)
            else {
              Dialog.showMessage(mapButton, n.device.shortName + " is not compatible with the " +
                item.device.shortName + " device from the " + item.driver.id + "  driver.")
            }
          case _ => ()
        }
    }
  }

  val eventHandler = new PBasicInputEventHandler with DefaultToolControllerCallbackHandler {
    override def mouseEntered(e: PInputEvent): Unit =
      controller.statusMessage = "Click to select a device and map it to a hardware device"

    override def mouseClicked(e: PInputEvent): Unit = e.getPickedNode match {
      case d: DeviceNode => selection.selectedNode = Some(d)
      case _ => selection.selectedNode = None
    }
  }

  // send the device list view to the listener actor
  listenerActor ! settingsPanel.devices
}

// vim: set ts=2 sw=2 et:

