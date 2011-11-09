package gui.tool

import gui.model.{ConnectionAdded, DeviceConnectionLayer, DeviceLayer}
import gui.model.devices.DeviceNode

import scala.swing._
import scala.swing.event.ButtonClicked

import edu.umd.cs.piccolo.event._

class ConnectionListView extends ListView[core.Connection] {
  renderer = new ListView.Renderer[core.Connection] {
    def componentFor(list: ListView[_],
                     isSelected: Boolean,
                     hasFocus: Boolean,
                     a: core.Connection,
                     index: Int): Component = new Label {
      text = a.from.value + " -> " + a.to.value
      tooltip = a.toString
      peer.setOpaque(true) // scala.swing doesn't yet map opaque?
      horizontalAlignment = Alignment.Left
      border = Swing.EmptyBorder(0, 5, 0, 5)
      background = if (isSelected) list.selectionBackground else list.background
      foreground = if (isSelected) list.selectionForeground else list.foreground
    }
  }
}


class DeviceConnectionTool(val controller: ToolController,
                           deviceLayer: DeviceLayer,
                           deviceConnectionLayer: DeviceConnectionLayer) extends Tool with Reactor {

  val name = "Connect devices"

  listenTo(deviceConnectionLayer)
  reactions += {
    case ConnectionAdded(conn) =>
      val list = (settingsPanel.connList.listData.toList ++ Seq(conn)) sort ((a, b) => a.from.value < b.from.value)
      settingsPanel.connList.listData = list
  }

  val settingsPanel = new BorderPanel {
    val connList = new ConnectionListView { fixedCellWidth = 10 }
    val deleteConnectionButton = new Button("Remove") { peer setMnemonic 'r' }

    layout(new ScrollPane(connList)) = BorderPanel.Position.Center
    layout(deleteConnectionButton)   = BorderPanel.Position.South

    listenTo(deleteConnectionButton)
    reactions += {
      case ButtonClicked(`deleteConnectionButton`) => {
        connList.selection.items.firstOption map {first =>
          deviceConnectionLayer remove first
          connList.listData = connList.listData.toList remove (_ == first)
        }
      }
    }
  }

  val eventHandler = new PBasicInputEventHandler with DefaultToolControllerCallbackHandler {
    override def mouseEntered(e: PInputEvent): Unit
      = controller.statusMessage = "Click to connect compatible devices"

    override
    def mouseClicked(e: PInputEvent): Unit =
      (deviceConnectionLayer.selection.selectedNode, e.getPickedNode) match {
        case (Some(from: DeviceNode), to: DeviceNode) => deviceConnectionLayer.connect(from, to)
        case (_, select: DeviceNode) => deviceConnectionLayer.selection.selectedNode = Some(select)
        case _ => deviceConnectionLayer.selection.selectedNode = None
      }
  }

  def clear {
    deviceConnectionLayer.clear
    settingsPanel.connList.listData = Nil
  }

  override def toolSave =
    <connections>
      { deviceConnectionLayer.export.map(_.toXML) }
    </connections>

  override def toolLoad(node: scala.xml.NodeSeq) = {
    clear

    (node \ "connections" \ "connection") map { node =>
      val fromOpt = deviceLayer.devices get core.MasterDeviceId((node \ "@from").first.text.toInt)
      val toOpt   = deviceLayer.devices get core.MasterDeviceId((node \ "@to").first.text.toInt)
      (fromOpt, toOpt) match {
        case (Some(from: DeviceNode), Some(to: DeviceNode)) => deviceConnectionLayer.connect(from, to)
        case _ => ()
      }
    }
  }

  override def toolClear = clear

  override def toolEnter: Unit = {
    deviceConnectionLayer.selection.selectedNode = None
  }

  // add a device dependency checker to device layer that prevents removal of connected nodes
  deviceLayer.deviceDependencyCheckers += deviceConnectionLayer.dependencyChecker
}

// vim: set ts=2 sw=2 et:

