package gui.model

import java.awt.geom.{Ellipse2D, Line2D}

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.nodes._

class AvatarNode(val id: Int,
                 pos: utils.vec.Vec2,
                 scaling: Scaling) extends PPath with OutlinedPath {
  val bodyNode = new PPath {
    setPickable(false)
  }
  addChild(bodyNode)

  private val storyPText = new PText {
    setPickable(false)
    setPaint(new java.awt.Color(0x799fcf))
//    setTransparency(0.7f)
    setTextPaint(java.awt.Color.WHITE)
    setFont(getFont.deriveFont(20.0f))
  }
  addChild(storyPText)

  setOffset(pos.x, pos.y)

  def storyText = storyPText getText
  def storyText_=(v: String) = storyPText setText v

  private val reactor = new swing.Reactor {
    listenTo(scaling)
    reactions += {
      case ScalingChange(`scaling`) => updatePath
    }
  }

  def updatePath = {
    var path = new java.awt.geom.Path2D.Double
    val r = 0.25 * scaling.pixelsPerMeter // meter

    path.append(new Ellipse2D.Double(-r, -r / 1.75, 2 * r, r / 2 * 1.75).getPathIterator(null), false)
    path.append(new Line2D.Double(0, -r / 4, 0, -r / 1.25).getPathIterator(null), false)

    bodyNode.setPathTo(path)
    bodyNode.setPaint(new java.awt.Color(0xd3d7cf))

    storyPText.centerBoundsOnPoint(bodyNode.getOffset.getX, bodyNode.getOffset.getY + r + 2)

    path
  }

  updatePath

  def export = new simulation.model.Avatar(
    utils.vec.Vec2(getXOffset / scaling.pixelsPerMeter,
                   getYOffset / scaling.pixelsPerMeter),
    getRotation
  )
}

// vim: set ts=2 sw=2 et:

