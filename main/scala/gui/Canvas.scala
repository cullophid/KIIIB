package gui

import gui.event.ZoomEventHandler

import scala.swing._
import scala.swing.event._

import java.awt.event.{InputEvent,KeyEvent}
import java.beans.{PropertyChangeListener, PropertyChangeEvent}

import java.awt.{Color, BasicStroke, Graphics2D, Rectangle}
import java.awt.geom.{Arc2D, Point2D, Ellipse2D, Line2D, Rectangle2D}

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.event._
import edu.umd.cs.piccolo.nodes.PText

class Canvas(camera0: PCamera) extends Component {
  override lazy val peer = new PCanvas with SuperMixin {
    setCamera(camera0)
    setBackground(new java.awt.Color(0xfff9cf))

    setPanEventHandler(new PPanEventHandler {
      setEventFilter(new PInputEventFilter(InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK))

      setAutopan(false)
    })

    setZoomEventHandler(new ZoomEventHandler)
  }

  def this() = this(CameraFactory.buildCamera(new PCamera with TooltipCamera))

  def camera = peer.getCamera
  def camera_=(c: PCamera): Unit = peer.setCamera(c)
  def root = peer.getRoot
  def layer = peer.getLayer
  def panEventHandler = peer.getPanEventHandler
  def panEventHandler_=(handler: PPanEventHandler): Unit = peer.setPanEventHandler(handler)
  def zoomEventHandler = peer.getZoomEventHandler
  def zoomEventHandler_=(handler: PZoomEventHandler): Unit = peer.setZoomEventHandler(handler)

  val scaleText = new PText("zoom = " + camera.getViewScale.toString.format("0.2f")) {
    setPickable(false)
    setFont(getFont.deriveFont(java.awt.Font.BOLD))
    setPaint(new Color(0x799fcf))
    setTextPaint(Color.WHITE)
    setTransparency(0.7f)
    setOffset(10, 10)
  }
  camera.addChild(scaleText)
  camera.addPropertyChangeListener(PCamera.PROPERTY_VIEW_TRANSFORM, new PropertyChangeListener() {
    def propertyChange(event: PropertyChangeEvent): Unit = scaleText.setText("zoom = %2.1f".format(camera.getViewScale))
  })
}

// vim: set ts=2 sw=2 et:

