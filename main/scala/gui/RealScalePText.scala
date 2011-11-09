package gui

import gui.model.{Scaling, ScalingChange}

import java.beans.{PropertyChangeListener, PropertyChangeEvent}

import edu.umd.cs.piccolo.PCamera
import edu.umd.cs.piccolo.nodes.PText

class RealScalePText(camera: PCamera, scaling: Scaling) extends PText {

  val dpi = java.awt.Toolkit.getDefaultToolkit().getScreenResolution()

  def update {
    setText("scale = 1:" + (dpi / (scaling.pixelsPerMeter * camera.getViewScale * 0.0254)).round.toInt)
  }

  camera.addPropertyChangeListener(PCamera.PROPERTY_VIEW_TRANSFORM, new PropertyChangeListener() {
    def propertyChange(event: PropertyChangeEvent): Unit = update
  })

  private val reactor = new swing.Reactor {
    listenTo(scaling)
    reactions += {
      case ScalingChange(_) => update
    }
  }

  setPickable(false)
  setFont(getFont.deriveFont(java.awt.Font.BOLD))
  setPaint(new java.awt.Color(0x799fcf))
  setTextPaint(java.awt.Color.WHITE)
  setTransparency(0.7f)

  update
}

// vim: set ts=2 sw=2 et:

