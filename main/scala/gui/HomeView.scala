package gui

import model._
import tool._
import event._

import java.awt.event.InputEvent

import scala.swing._

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.event._
import edu.umd.cs.piccolo.util._
import edu.umd.cs.piccolo.nodes._

class HomeView (
  selection: NodeSelection,
  wallLayer: WallLayer,
  deviceLayer: DeviceLayer,
  deviceConnectionLayer: DeviceConnectionLayer,
  scaling0: Scaling,
  tools: Seq[Tool]
) extends BorderPanel {

  private def buildCanvas = {
    val camera = CameraFactory.buildCamera(new PCamera with DrawsGrid with TooltipCamera {
      val properties = new DrawsGrid.Properties {
        def pixelsPerUnit = scaling0.pixelsPerMeter
        def numCells = 10
      }
    })
    new Canvas(camera) {
      camera.addInputEventListener(new SnappedSelectionDragEventHandler(selection, scaling0) {
        setEventFilter(new PInputEventFilter(InputEvent.BUTTON1_MASK,
                                             InputEvent.SHIFT_MASK))
      })

      camera.addInputEventListener(new ContextMenuEventHandler {
        setEventFilter(new PInputEventFilter(java.awt.event.InputEvent.BUTTON3_MASK))
      })

      panEventHandler = new PPanEventHandler {
        setEventFilter(new PInputEventFilter(InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK))
      }

      camera.addChild(new RealScalePText(camera, scaling0) { setOffset(10, 24) })
    }
  }

  val canvas = buildCanvas
  val toolPanel: ToolPanel = new ToolPanel(canvas, tools) {
    border = scala.swing.Swing.EmptyBorder(5, 0, 5, 0)
  }

  layout(utils.Swing.buildCanvasBorderPanel(canvas)) = BorderPanel.Position.Center
  layout(toolPanel)                      = BorderPanel.Position.East

}


// vim: set ts=2 sw=2 et:
