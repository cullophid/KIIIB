package gui.tool

import core.MasterDeviceId
import core.devices._
import gui.model._
import gui.model.devices._
import utils.vec.Vec2

import scala.swing._

import java.awt.geom.Point2D

class MotionSensorNodeFactory(scaling: Scaling) extends DeviceNodeFactory {
  var range = 3.0 // meter
  var angleSpan = 115.0 // deg

  val propertiesPanel = new BoxPanel(Orientation.Vertical) {
    // TODO: add validation (input > 0)
    val rangeTxt = new TextField(range.toString, 5)
    val angleSpanTxt = new TextField(angleSpan.toString, 5)

    contents += new Label("Range (m)") {
      peer setDisplayedMnemonic 'r'
      peer setLabelFor rangeTxt.peer
    }
    contents += rangeTxt
    contents += new Label("Angle span (°)") {
      peer setDisplayedMnemonic 'a'
      peer setLabelFor angleSpanTxt.peer
    }
    contents += angleSpanTxt
  }

  def createDevice(id: MasterDeviceId, position: Point2D) = {
    val range = propertiesPanel.rangeTxt.text.toDouble
    val angleSpan = propertiesPanel.angleSpanTxt.text.toDouble
    new MotionSensorNode(id,
                         position.getX,
                         position.getY,
                         new MotionSensor(range, angleSpan),
                         scaling)
  }

  val toolTitle = "Motion sensor"
}

// vim: set ts=2 sw=2 et:

