package gui

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.nodes.PText
import edu.umd.cs.piccolo.event.PBasicInputEventHandler
import edu.umd.cs.piccolo.event.PInputEvent

import java.awt.Color

trait HasTooltip {
  def tooltip: String
}

trait TooltipCamera { self: PCamera =>
  private val tooltipNode = new PText {
    setPickable(false)
    setPaint(new Color(0x799fcf))
    setTextPaint(Color.WHITE)
    setFont(getFont.deriveFont(20.0f))
  }

  self.addChild(tooltipNode)

  self.addInputEventListener(new PBasicInputEventHandler {
    override def mouseMoved(event: PInputEvent): Unit = updateToolTip(event)

    override def mouseDragged(event: PInputEvent): Unit = updateToolTip(event)

    def updateToolTip(event: PInputEvent) {
      event.getInputManager.getMouseOver.getPickedNode match {
        case n: HasTooltip =>
          val p = event.getCanvasPosition
          tooltipNode.setVisible(true)

          event.getPath.canvasToLocal(p, self)

          tooltipNode.setText(n.tooltip)
          tooltipNode.setOffset(p.getX + 8, p.getY - 8)
        case _ =>
          tooltipNode.setVisible(false)
      }
    }
  })

}
// vim: set ts=2 sw=2 et:
