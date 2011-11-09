package gui

import core.DriverDeviceId
import core.messages._
import gui.model._
import gui.model.devices._
import gui.model.scenario._
import gui.event.SnappedSelectionDragEventHandler
import simulation._
import utils.vec._

import scala.swing.Swing

import java.awt.geom.Ellipse2D
import java.awt.event.InputEvent

import edu.umd.cs.piccolo.PLayer
import edu.umd.cs.piccolo.event._
import edu.umd.cs.piccolo.nodes._
import edu.umd.cs.piccolo.util.PPaintContext

class ScenarioCanvas(view: ScenarioView, deviceLayer: DeviceLayer, scaling: Scaling, selection: NodeSelection) extends Canvas {
  val nodeLayer = new PLayer
  layer.addChild(nodeLayer)

  var speed = 1.0

  def clear {
    nodeLayer.removeAllChildren
    nodeLayer.moveToFront
  }

  camera.addInputEventListener(new PBasicInputEventHandler {
    override def mouseClicked(event: PInputEvent) {
      super.mouseClicked(event)
      if (event.isMiddleMouseButton && event.getPickedNode != event.getTopCamera)
        event.getPickedNode match {
          case node: PathNode.PosNode =>
            selection.selectedNode = node.owner.prev map (_.posNode)
            node.owner.timePath map (_ remove node.owner)
          
          case _ => ()
        }
    }
  })

  camera.addInputEventListener(new PBasicInputEventHandler {
    setEventFilter(new PInputEventFilter(InputEvent.BUTTON1_MASK, InputEvent.SHIFT_MASK))

    override def mouseClicked(event: PInputEvent) {
      val pos = event.getPosition
      var nodeOpt: Option[PathNode] = None
      val pickedNode = event.getPickedNode
      if ((event.getModifiers & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK) {
        nodeOpt = Some(new MoveAndDelayPathNode(pos.getX, pos.getY, speed, scaling, 5))
      } else if (pickedNode.getParent != null
          && pickedNode.getParent.isInstanceOf[BinarySwitchNode]) {
        val bsn = event.getPickedNode.getParent.asInstanceOf[BinarySwitchNode]
        if (pickedNode != bsn.core) {
          val cmd = if (pickedNode == bsn.btn1) core.devices.TurnOn else core.devices.TurnOff
          val driverId = DriverDeviceId(bsn.id.toString)
          nodeOpt = Some(new MoveAndPerformDeviceActionPathNode(pos.getX, pos.getY, speed, scaling, new DriverCommandMessage(driverId, cmd)))
        }
      } else {
        nodeOpt = Some(new MovePathNode(pos.getX, pos.getY, speed, scaling))
      }

      nodeOpt map { node =>
        node.posNodeChanged // updates time node x offset
        view.appendNode(node)
      }
    }
  })
  camera.addInputEventListener(new SnappedSelectionDragEventHandler(selection, scaling) {
    setEventFilter(new PInputEventFilter(InputEvent.BUTTON1_MASK, InputEvent.SHIFT_MASK))

    override def shouldStartDragInteraction(event: PInputEvent) = {
      super.shouldStartDragInteraction(event) && event.getPickedNode.isInstanceOf[PathNode.PosNode]
    }
  })

  camera.addInputEventListener(new ContextMenuEventHandler {
    setEventFilter(new PInputEventFilter(java.awt.event.InputEvent.BUTTON3_MASK))
  })

  camera.addChild(new RealScalePText(camera, scaling) {
    setOffset(10, 24)
  })
}

// vim: set ts=2 sw=2 et:

