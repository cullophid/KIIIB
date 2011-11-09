package gui.model.devices

import gui.model._

import core.devices.MotionSensor

import java.awt.Color
import java.awt.geom._
import java.awt.event.{ActionListener, ActionEvent}
import javax.swing.{JPopupMenu, JMenuItem, JCheckBoxMenuItem}

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.activities._
import edu.umd.cs.piccolo.event._
import edu.umd.cs.piccolo.nodes._
import edu.umd.cs.piccolo.util._

import scala.swing._
import scala.swing.event._

class MotionSensorNode(val id: core.MasterDeviceId,
                       x: Double,
                       y: Double,
                       device0: MotionSensor,
                       scaling: Scaling) extends DeviceNode with HasContextMenu {
  type DeviceType = MotionSensor

  private var _device = device0
  def device = _device
  def device_=(v: MotionSensor) {
    _device = v
    range.setPathTo(makeRangeArc(scaling.pixelsPerMeter))
  }

  val core  = makeCoreNode
  var range = makeRangeNode

  setOffset(x, y)

  addChild(range)
  addChild(core)

  private val reactor = new Reactor {
    listenTo(scaling)
    reactions += {
      case ScalingChange(scaling) => range.setPathTo(makeRangeArc(scaling.pixelsPerMeter))
    }
  }

  def export = new simulation.model.devices.PhysicalMotionSensor(
    _device,
    utils.vec.Vec2(getXOffset / scaling.pixelsPerMeter,
                   getYOffset / scaling.pixelsPerMeter),
    getRotation
  )

  override def intersects(bounds: Rectangle2D) = core.intersects(bounds)

  override protected def layoutChildren {
    core.setX(getX - 5)
    core.setY(getY - 5)
    range.setX(getX)
    range.setY(getY - range.getHeight / 2)
  }

  private def makeCoreNode = {

    val node = new PPath(new Rectangle2D.Double(0, 0, 10, 10)) with OutlinedPath
    node.setPaint(new Color(0xcc0000))
    node.setPickable(false)
    node
  }

  private def makeRangeArc(pixelsPerMeter: Double) = {
    val range = _device.range * pixelsPerMeter
    new Arc2D.Double(-range, -range, 2 * range, 2 * range,
                     -_device.angleSpan / 2, _device.angleSpan, Arc2D.PIE)
  }

  private def makeRangeNode = new PPath(makeRangeArc(scaling.pixelsPerMeter)) with OutlinedPath {
    setPaint(null)
    setPickable(false)
  }
  
  private var currentActivity: Option[PActivity] = None

  def animateUponDetection {
    currentActivity match {
      case Some(activity) if activity.isStepping =>
        activity.terminate
      
      case _ => currentActivity = None
    }
    core.setPaint(new Color(0x73d216))
    val activity = core.animateToColor(new Color(0xcc0000), _device.coolDownMillis)
    currentActivity = Some(activity)
    getRoot.addActivity(activity)
  }

  override def connectionPNode = core

  def buildContextMenu = new MotionSensorContextMenu(this)
}

class MotionSensorContextMenu(val node: MotionSensorNode) extends DeviceContextMenu {
  type NodeType = MotionSensorNode

  protected def buildPopup(device: MotionSensorNode): JPopupMenu = {
    val menu = new JPopupMenu with Reactor

    val rangeMenu     = new MenuItem("Range (" + device.device.range + " m)")
    val angleSpanMenu = new MenuItem("Angle span (" + device.device.angleSpan + " deg)")
    val visibility    = new CheckMenuItem("Range visible") { selected = device.range.getVisible }
    val coolDown      = new MenuItem("Detection cooldown period (" + device.device.coolDownMillis +" ms)")
    val checkInterval = new MenuItem("Detection check interval (" + device.device.minCheckInterval +" ms)")

    menu.listenTo(
      rangeMenu,
      angleSpanMenu,
      coolDown,
      checkInterval,
      visibility
    )

    menu.reactions += {
      case ButtonClicked(`rangeMenu`) => {
        val newRange = Dialog.showInput(rangeMenu,
                               "Choose new range",
                               "Alter motion sensor property",
                               Dialog.Message.Plain, null, Seq(), device.device.range.toString)

        newRange map { r =>
          device.device = device.device.copyNewRange(r.toDouble)
        }
      }

      case ButtonClicked(`angleSpanMenu`) => {
        val newRange = Dialog.showInput(angleSpanMenu,
                               "Choose new angle span",
                               "Alter motion sensor property",
                               Dialog.Message.Plain, null, Seq(), device.device.angleSpan.toString)

        newRange map { as =>
          device.device = device.device.copyNewAngleSpan(as.toDouble)
        }
      }

      case ButtonClicked(`visibility`) => device.range.setVisible(visibility.selected)

      case ButtonClicked(`coolDown`) => {
        val newRange = Dialog.showInput(coolDown,
                               "Choose new sensor detection cooldown period",
                               "Alter motion sensor property",
                               Dialog.Message.Plain, null, Seq(), device.device.coolDownMillis.toString)
        newRange map { as =>
          device.device = device.device.copyNewCoolDownMillis(as.toLong)
        }
      }
      case ButtonClicked(`checkInterval`) => {
        val newRange = Dialog.showInput(coolDown,
                               "Choose new sensor detection check interval ",
                               "Alter motion sensor property",
                               Dialog.Message.Plain, null, Seq(), device.device.minCheckInterval.toString)
        newRange map { as =>
          device.device = device.device.copyNewMinCheckInterval(as.toLong)
        }
      }


    }

    menu.add(rangeMenu.peer)
    menu.add(angleSpanMenu.peer)
    menu.add(coolDown.peer)
    menu.add(checkInterval.peer)
    menu.add(visibility.peer)

    menu
  }
}

// vim: set ts=2 sw=2 et:

