package gui.tool

import edu.umd.cs.piccolo.event.PInputEvent
import scala.swing.FlowPanel

class SelectTool(val controller: ToolController) extends Tool {
  val name = "Select"
  val settingsPanel = new FlowPanel
  val eventHandler = new DefaultToolControllerCallbackHandler {
    override def mouseEntered(e: PInputEvent): Unit = controller.statusMessage = "Click to select device/wall; click and drag to move; Hold ctrl to snap to grid"
  }

}

// vim: set ts=2 sw=2 et:
