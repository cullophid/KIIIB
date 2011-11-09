package gui.tool

import gui.model._

import scala.swing._
import scala.swing.event._

class ToolPanel(canvas: Canvas, tools0: Seq[Tool]) extends BorderPanel {

  def tools = tools0

  private val group = new ButtonGroup

  private val toolRadioButtons = new BoxPanel(Orientation.Vertical) {
    border = utils.Swing.TitledBorder(Swing.EmptyBorder, "Tools")
  }

  private val settings = new BorderPanel {
    border = utils.Swing.TitledBorder(Swing.EmptyBorder, "Tool settings")
  }

  private def activateTool (th: Option[Tool]) {
    _currentTool map { th =>
      canvas.camera.removeInputEventListener(th.eventHandler)
      th.toolLeave
    }
    _currentTool = th
    _currentTool map { tool =>
      canvas.camera.addInputEventListener(tool.eventHandler)
      tool.toolEnter
      settings.layout.clear
      settings.layout(tool.settingsPanel) = BorderPanel.Position.North
      settings.revalidate
      settings.repaint // somehow it wont repaint after setting an empty panel
    }
  }

  private var _currentTool: Option[Tool] = None


  tools foreach { tool =>
    val btn = new RadioButton(tool.name) {
      reactions += {
        case ButtonClicked(_) => activateTool(Some(tool))
      }
    }
    toolRadioButtons.contents += btn
    group.buttons += btn
  }

  activateTool(tools.firstOption)
  toolRadioButtons.contents.firstOption map {
    case c: RadioButton => c.selected = true
    case _ => ()
  }

  layout(toolRadioButtons) = BorderPanel.Position.North
  layout(settings) = BorderPanel.Position.Center

}

// vim: set ts=2 sw=2 et:

