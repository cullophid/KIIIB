package utils

import scala.swing.{Swing => ScalaSwing}
import scala.swing.BorderPanel
import scala.swing.Component

object Swing {
  def TitledBorder(inner: javax.swing.border.Border, title: String) = {
    val b = scala.swing.Swing.TitledBorder(inner, title)
    b.setTitleFont(b.getTitleFont.deriveFont(java.awt.Font.BOLD))
    b
  }

  def TitledBorder(title: String): javax.swing.border.Border = TitledBorder(scala.swing.Swing.EmptyBorder, title)

  def buildCanvasBorderPanel(c: Component) = new BorderPanel {
    border = ScalaSwing.CompoundBorder(ScalaSwing.EmptyBorder(5), ScalaSwing.EtchedBorder)
    layout(c) = BorderPanel.Position.Center
  }

}


// vim: set ts=2 sw=2 et:
