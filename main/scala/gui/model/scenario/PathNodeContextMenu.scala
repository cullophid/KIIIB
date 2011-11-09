package gui.model.scenario

import gui.model._
import gui.model.scenario._
import simulation.model.scenario.AvatarPathNode
import utils.vec.Vec2

import swing._

import java.awt.event.InputEvent

import edu.umd.cs.piccolo.PNode
import edu.umd.cs.piccolo.event._
import edu.umd.cs.piccolo.nodes.PText

class PathNodeContextMenu(val node: PathNode.AbstractPathNode) extends ContextMenu {
  type NodeType = PathNode.AbstractPathNode

  def buildPopup(node: PathNode.AbstractPathNode) = {
    val menu = new javax.swing.JPopupMenu with Reactor

    val alterStoryTextMenu = new MenuItem("Alter story text (" + (node.owner.storyText getOrElse("None")) + ")")

    menu.listenTo(alterStoryTextMenu)
    menu.reactions += {
      case event.ButtonClicked(`alterStoryTextMenu`) => {
        val newStoryText = Dialog.showInput(alterStoryTextMenu,
                               "Choose story text - type None not to alter previous",
                               "Alter story text",
                               Dialog.Message.Plain, null, Seq(), node.owner.storyText getOrElse("None"))

        newStoryText match {
          case Some(text) if text == "None" =>
            node.owner.storyText = None
          
          case Some(text) =>
            node.owner.storyText = Some(text)

          case _ => ()
        }
      }
    }

    menu.add(alterStoryTextMenu.peer)

    menu
  }
}

// vim: set ts=2 sw=2 et:

