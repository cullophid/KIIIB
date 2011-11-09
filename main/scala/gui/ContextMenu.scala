package gui

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.event._

/**
 * A base class for node context popup menus
 */
abstract class ContextMenu {
  type NodeType <: PNode

  val node: NodeType

  /**
   * Concrete context menus will implement this method to build the menu
   */
  protected def buildPopup(node: NodeType): javax.swing.JPopupMenu
 
  /**
   * Build and show the context menu based on the PInputEvent
   *
   * The popup is owned by the canvas component and placed at mouse position
   */
  def show(inputEvent: PInputEvent) {
    val menu = buildPopup(node)
    // HACK: convert PComponent (which is an interface with no connection to JComponent)
    //       but! in our case getComponent is always PCanvas which *is* a JComponent :)
    val c = inputEvent.getComponent.asInstanceOf[javax.swing.JComponent]

//    menu.show(c, inputEvent.getPosition.getX, inputEvent.getPosition.getY)
    val (x: Int, y: Int) = inputEvent.getSourceSwingEvent match {
      case e: java.awt.event.MouseEvent => (e.getPoint.getX.toInt,
                                            e.getPoint.getY.toInt)
      case _ => (0, 0)
    }
    menu.show(c, x, y)
  }
}

trait HasContextMenu extends PNode {
  def buildContextMenu: ContextMenu
}

/**
 * The global input event handler nodes that implements HasContextMenu
 */
class ContextMenuEventHandler extends PBasicInputEventHandler {
  override def mouseClicked(e: PInputEvent) {
    e.getPickedNode match {
      case n: HasContextMenu =>
        val context = n.buildContextMenu
        context.show(e)
      case _ => ( )
    }
  }
}




// vim: set ts=2 sw=2 et:

