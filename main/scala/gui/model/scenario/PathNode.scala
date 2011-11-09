package gui.model.scenario

import utils.vec.Vec2

import java.beans.{PropertyChangeListener, PropertyChangeEvent}

import edu.umd.cs.piccolo.PNode
import edu.umd.cs.piccolo.nodes.{PPath, PText}

object PathNode {
  abstract class AbstractPathNode extends PPath with HasContextMenu with HasTooltip {
    val owner: PathNode
    def buildContextMenu = new PathNodeContextMenu(this)

    def tooltip: String = {
      (owner.storyTextPrefix, owner.storyTextToDisplay) match {
        case (Some(prefix), Some(text)) if text.trim == "" => prefix
        case (Some(prefix), Some(text))    => prefix + ": " + text
        case (Some(prefix), None)          => prefix
        case (None, Some(text))            => text
        case (None, None)                  => ""
      }
    }
  }

  class PosNode(val owner: PathNode) extends AbstractPathNode
                                     with OutlinedPath
                                     with ConnectedNode
                                     with Selectable
  class TimeNode(val owner: PathNode) extends AbstractPathNode
                                      with OutlinedPath
                                      with ConnectedNode

  val pixelsPerSecond = 20
}
abstract class PathNode(x0: Double, y0: Double, speed0: Double, scaling: gui.model.Scaling, storyText0: Option[String]) {
  def this(x1: Double, y1: Double, speed1: Double, scaling: gui.model.Scaling) = this(x1, y1, speed1, scaling, None)
  def this(pos1: Vec2, speed1: Double, scaling: gui.model.Scaling) = this(pos1.x, pos1.y, speed1, scaling)
  import PathNode._

  def toAvatarPathNode: simulation.model.scenario.AvatarPathNode

  def prev = _prev
  def next = _next

  protected var _prev: Option[PathNode] = None
  protected var _next: Option[PathNode] = None
  protected var _speed: Double = speed0
  def prev_=(v: Option[PathNode]): Unit = {
    _prev = v
    v map (_ _next = Some(this))
    if (v.isDefined)
      posNodeChanged
  }
  def next_=(v: Option[PathNode]): Unit = {
    _next = v
    v map (_ _prev = Some(this))
    v map (_ posNodeChanged)
  }

  var timePath: Option[TimePath] = None

  def pos: Vec2 = Vec2(posNode.getXOffset, posNode.getYOffset)
  def startTime_=(v: Double) = timeNode.setOffset(v * pixelsPerSecond, timeNode.getYOffset)
  def startTime: Double = timeNode.getXOffset / pixelsPerSecond
  def endTime: Double = startTime

  val timeNode = new TimeNode(this) {
    setPathTo(new java.awt.geom.Ellipse2D.Double(-2, -4, 4, 8))
    setStroke(new java.awt.BasicStroke(0))
    addPropertyChangeListener(PNode.PROPERTY_FULL_BOUNDS,
                              new PropertyChangeListener() {
      def propertyChange(event: PropertyChangeEvent): Unit = timeNodeChanged
    })
  }
  val posNode = new PosNode(this) {
    setPathTo(new java.awt.geom.Rectangle2D.Double(-4, -4, 8, 8))
    setStroke(new java.awt.BasicStroke(0))
    addPropertyChangeListener(PNode.PROPERTY_FULL_BOUNDS,
                              new PropertyChangeListener() {
      def propertyChange(event: PropertyChangeEvent): Unit = posNodeChanged
    })
  }
  
  protected def storyTextPrefix: Option[String] = None

  protected def storyTextToDisplay: Option[String] =
    storyText match {
      case Some(text) => Some(text)
      case None       =>
        _prev map (_.storyTextToDisplay) getOrElse(Some(""))
    }

  var storyText: Option[String] = storyText0

  posNode.setOffset(x0, y0)

  private val reactor = new swing.Reactor {
    listenTo(scaling)
    reactions += {
      case ScalingChange(`scaling`) => timeNodeChanged
    }
  }

  def timeNodeChanged: Unit = {
    _prev match {
      case Some(prev) =>
        val physicalDistance = (prev.pos distanceTo pos) / scaling.pixelsPerMeter
        if ((startTime - prev.endTime) >= 1.7E-308)
          _speed = physicalDistance / (startTime - prev.endTime)
        else if (physicalDistance >= 1.7E-308)
          _speed = physicalDistance / 1.7E-308
        else
          _speed = 1

      case None => startTime = 0
    }
    _next map (_ posNodeChanged)
  }
  def posNodeChanged: Unit = {
    _prev map { prev =>
      val physicalDistance = (prev.pos distanceTo pos) / scaling.pixelsPerMeter
      if (physicalDistance < 1.7E-308) {
        startTime = prev.endTime + 1.7E-308
        _speed = physicalDistance / 1.7E-308
      } else {
        startTime = prev.endTime + (physicalDistance / _speed)
      }
    }
    _next map (_ posNodeChanged)
  }
}

// vim: set ts=2 sw=2 et:

