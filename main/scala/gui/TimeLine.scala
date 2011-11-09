package gui

import core._
import gui.event._
import gui.model.scenario._
import simulation._
import utils.vec._

import java.awt.event.InputEvent
import java.awt.geom.Ellipse2D

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.activities._
import edu.umd.cs.piccolo.event._
import edu.umd.cs.piccolo.util._
import edu.umd.cs.piccolo.nodes._

object TimeLine {
  val dateFormat = new java.text.SimpleDateFormat("HH:mm:ss")

  def gridProperties = new DrawsGrid.Properties {
    val numCells: Int = 60
    val pixelsPerUnit: Double = numCells * PathNode.pixelsPerSecond

    override val offsetX: Double = 0.0
    override val offsetY: Double = PathNode.pixelsPerSecond / 2
  }

  def buildCamera = CameraFactory.buildCamera(new PCamera with DrawsGrid with TooltipCamera {
    val properties = gridProperties
  })
}
class TimeLine(val allowEditing: Boolean, selection: gui.model.NodeSelection, relatedCanvas: Canvas) extends gui.Canvas(TimeLine.buildCamera) {
  border = scala.swing.Swing.EtchedBorder

  def clear {
    layer.removeAllChildren
    layer.addChild(infoText)
  }

  private val infoText = new PText {
    setVisible(false)
    setFont(getFont.deriveFont(java.awt.Font.BOLD))
    setTextPaint(java.awt.Color.WHITE)
    setPaint(new java.awt.Color(0x799fcf))
    setTransparency(0.7f)
  }
  layer.addChild(infoText)
  camera removeChild scaleText

  panEventHandler = new PPanEventHandler {
    setEventFilter(new PInputEventFilter(InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK, InputEvent.CTRL_MASK))

    override def pan(event: PInputEvent) {
      var bounds = camera.getViewBounds
      bounds.setOrigin(bounds.getX - event.getDelta.getWidth, 0)
      camera.setViewBounds(bounds)
    }

    override def mouseReleased(event: PInputEvent) {
      var bounds = camera.getViewBounds
      bounds.setOrigin(bounds.getX - event.getDelta.getWidth, 0)
      camera.setViewBounds(bounds)
    }

    setAutopan(false)
  }
  zoomEventHandler = null
  
  camera.addInputEventListener(new PBasicInputEventHandler {
    override def mouseClicked(event: PInputEvent) {
      if (event.isLeftMouseButton && event.getPickedNode != event.getTopCamera) {
        event.getPickedNode match {
          case node: PathNode.TimeNode =>
            val activity =
              relatedCanvas.camera.animateViewToCenterBounds(
                node.owner.posNode.getGlobalBounds,
                false,
                500
              )
            relatedCanvas.camera.addActivity(activity)
          
          case _ => ()
        }
      }    
    }
  })

  if (allowEditing) {
    camera.addInputEventListener(new PBasicInputEventHandler {
      override def mouseClicked(event: PInputEvent) {
        super.mouseClicked(event)
        if (event.isMiddleMouseButton && event.getPickedNode != event.getTopCamera) {
          event.getPickedNode match {
            case node: PathNode.TimeNode =>
              selection.selectedNode = node.owner.prev map (_.posNode)
              node.owner.timePath map (_ remove node.owner)

            case _ => ()
          }
        }
      }
    })

    camera.addInputEventListener(new ContextMenuEventHandler {
      setEventFilter(new PInputEventFilter(java.awt.event.InputEvent.BUTTON3_MASK))
    })

    camera.addInputEventListener(new TimeLineSnappedDragHandler(TimeLine.gridProperties, infoText))
  }
}

// vim: set ts=2 sw=2 et:

