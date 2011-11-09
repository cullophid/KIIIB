package gui.tool

import core.MasterDeviceId
import core.devices._
import gui.model._
import gui.model.devices._
import utils.vec.Vec2

import scala.swing._
import scala.swing.event._

import java.awt.geom._
import java.awt.event.InputEvent

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.event._
import edu.umd.cs.piccolo.util._
import edu.umd.cs.piccolo.nodes._

class AddDeviceTool(val controller: ToolController,
                    deviceLayer: DeviceLayer,
                    selection: NodeSelection,
                    deviceNodeFactories: Seq[DeviceNodeFactory],
                    deviceNodeFactory: gui.model.devices.DeviceNodeFactory) extends Tool {
  private var _factory: Option[DeviceNodeFactory] = None
  private val _group = new ButtonGroup
  private val _propertiesPanel = new BorderPanel

  val name = "Add devices"

  private def addDeviceType(name: String, factory: DeviceNodeFactory): RadioButton = {
    val btn = new RadioButton(name) {
      reactions += {
        case ButtonClicked(source) => {
          _factory = Some(factory)
          _propertiesPanel.layout.clear
          _propertiesPanel.layout(factory.propertiesPanel) = BorderPanel.Position.North
          _propertiesPanel.revalidate
          _propertiesPanel.repaint // somehow it wont repaint after setting an empty panel
        }
      }
    }
    _group.buttons += btn
    _group.select(btn)
    _factory = Some(factory)
    btn
  }


  // TODO: Load factories from a list instead of statically adding device types
  val settingsPanel = new BorderPanel {
    val devices = new BoxPanel(Orientation.Vertical) {
      deviceNodeFactories foreach { factory =>
        contents += addDeviceType(factory.toolTitle, factory)
      }
    }

    layout(devices) = BorderPanel.Position.North
    layout(_propertiesPanel) = BorderPanel.Position.Center
  }

  val eventHandler = new PBasicInputEventHandler with DefaultToolControllerCallbackHandler {
    override def mouseEntered(e: PInputEvent) = controller.statusMessage = "Left mouse button: place device; Middle or Ctrl + Left mouse button: delete device"

    override def mouseClicked(e: PInputEvent) {
      super.mouseClicked(e)

      if (e.isMiddleMouseButton || (e.isLeftMouseButton() && e.isControlDown())) e.getPickedNode match {
        case n: DeviceNode if !deviceLayer.reasonsNotToRemoveChild(n).isEmpty => {
          val sb = new StringBuilder("The device cannot be removed because:")
          deviceLayer.reasonsNotToRemoveChild(n) map {
            case Some(reason) => sb.append("\n· " + reason)
            case None         => ()
          }
          Dialog.showMessage(settingsPanel, sb.toString)
        }

        case n: DeviceNode => deviceLayer removeChild n
        case _ => ()
      }
      else if (e.isLeftMouseButton) _factory map { f =>
        val device = f.createDevice(deviceLayer.newDeviceId, e.getPosition)
        deviceLayer.addChild(device)
        selection.selectedNode = Some(device) // TODO: this gets overwritten by the SnappedSelectionDragEventHandler
      }
    }
  }

  override def toolLoad(node: scala.xml.NodeSeq) {
    deviceLayer.clear
    val physicalDeviceList = Map() ++ ((node \ "devices" \ "physicaldevice") map { node =>
      val id    = MasterDeviceId((node \ "@id").first.text.toInt)

      (id -> simulation.model.devices.PhysicalDevice.fromXML(node))
    })
    physicalDeviceList transform {(id, physDev) =>
      deviceLayer.addChild(deviceNodeFactory.createFromPhysical(physDev, id))
    }

    (0 to (node \ "devices" \ "@last-id").first.text.toInt) foreach {
      _ => deviceLayer.newDeviceId
    }
  }

  override def toolSave =
    <devices last-id={deviceLayer.currentDeviceId.toString}>
      {deviceLayer.devices.map {
        case (id, deviceNode) =>
        <physicaldevice
         id={id.toString}
         angle={deviceNode.getRotation.toString}>
          <pos
           x={deviceNode.getXOffset.toString}
           y={deviceNode.getYOffset.toString}/>
          {deviceNode.device.toXML}
        </physicaldevice>
      }}
    </devices>

  override def toolClear =
    deviceLayer.clear
}

// vim: set ts=2 sw=2 et:

