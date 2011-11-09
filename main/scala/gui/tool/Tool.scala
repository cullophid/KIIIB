package gui.tool

import gui.model.NodeSelection

import scala.swing.Panel

import edu.umd.cs.piccolo.event.{PInputEvent, PBasicInputEventHandler}

abstract class ToolController {
  var statusMessage: String
}

abstract class Tool {
  val settingsPanel: Panel
  val eventHandler: PBasicInputEventHandler

  val name: String

  val controller: ToolController

  def toolEnter: Unit = { }
  def toolLeave: Unit = { controller.statusMessage = "" }
  def toolInit: Unit = { }
  def toolClear: Unit = { }
  def toolSave: scala.xml.NodeSeq = scala.xml.NodeSeq.Empty
  def toolLoad(node: scala.xml.NodeSeq): Unit = { }

  trait DefaultToolControllerCallbackHandler extends PBasicInputEventHandler {
    override def mouseEntered(e: PInputEvent): Unit = controller.statusMessage = ""
    override def mouseExited(e: PInputEvent): Unit = controller.statusMessage = ""
  }

}

// vim: set ts=2 sw=2 et:

