package gui.tool

import scala.swing._
import scala.swing.event._

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.event._
import edu.umd.cs.piccolo.util._
import edu.umd.cs.piccolo.nodes._

import java.awt.geom._
import java.awt.event.InputEvent

import gui.model._
import gui.model.devices._

class RotateDeviceTool(val controller: ToolController, selection: NodeSelection) extends Tool {
  val name = "Rotate devices"

  val settingsPanel = new FlowPanel
  val eventHandler = new PDragSequenceEventHandler with DefaultToolControllerCallbackHandler {
    override def mouseEntered(e: PInputEvent): Unit
      = controller.statusMessage = "Select a device, click and drag on empty canvas to change the rotation"

    override protected
    def drag(event: PInputEvent) {
      super.drag(event)

      def sub (p1: Point2D, p2: Point2D) = new Point2D.Double(p2.getX - p1.getX, p2.getY - p1.getY)

      (selection.selectedNode, _origin) match {
        case (Some(node: DeviceNode), Some(origin)) =>
          val p = sub(event.getPosition, origin)
          val angle = Math.atan2(p.x, -p.y) + Math.Pi / 2
          node.setRotation(angle)
        case _ => ()
      }
    }

    override protected
    def startDrag(event: PInputEvent) {
      super.startDrag(event)
      selection.selectedNode map { node => _origin = Some(node.getOffset) }
    }

    override protected
    def endDrag(event: PInputEvent) {
      super.endDrag(event)
      _origin     = None
    }

    override protected
    def shouldStartDragInteraction(event: PInputEvent) = event.getPickedNode == event.getTopCamera

    private var _origin: Option[Point2D] = None

    setEventFilter(new PInputEventFilter(InputEvent.BUTTON1_MASK, InputEvent.SHIFT_MASK))
  }

}

// vim: set ts=2 sw=2 et:

