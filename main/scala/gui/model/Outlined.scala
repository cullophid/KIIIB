package gui.model

import java.awt.Color

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.event._
import edu.umd.cs.piccolo.util._
import edu.umd.cs.piccolo.nodes._

trait Outlined { self: PNode =>
  private var _outlineColor: Color = Color.BLACK

  def outlineColor = _outlineColor
  def outlineColor_=(c: Color) {
    _outlineColor = c

    val children = scala.collection.jcl.Buffer(self.getChildrenReference)
    children foreach {
      case n: Outlined => n.outlineColor = outlineColor
      case _ =>
    }
    // if a parent on repaint calls repaint on children then repaint gets called two times for children
    // but they are buffered so it doesn't matter
    self.repaint
  }

  self.addInputEventListener(new PBasicInputEventHandler {
    override def mouseEntered(e: PInputEvent): Unit = outlineColor = new Color(0xf57900)
    override def mouseExited(e: PInputEvent): Unit = outlineColor = Color.BLACK
  })

}

// vim: set ts=2 sw=2 et:

