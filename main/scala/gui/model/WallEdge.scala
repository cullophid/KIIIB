package gui.model

import gui.ContextMenu

import java.awt.event.InputEvent
import java.awt.geom.Line2D

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.event._
import edu.umd.cs.piccolo.util._
import edu.umd.cs.piccolo.nodes._

object WallEdge {
  def fromPhysical(physWall: simulation.model.PhysicalWall, scaling: Scaling) =
    new WallEdge(new WallNode{ setOffset(physWall.segment.getP1) },
                 new WallNode{ setOffset(physWall.segment.getP2) },
                 physWall.thickness, scaling)
}

class WallEdge(val p1: WallNode,
               val p2: WallNode,
               thickness0: Double,
               scaling: Scaling) extends PPath with OutlinedPath
                                               with NodeEdge
                                               with HasContextMenu
                                               with ShouldNotDrag {

  private var _thickness = thickness0
  def thickness = _thickness
  def thickness_=(v: Double) {
    _thickness = v
    setStroke(new java.awt.BasicStroke((v * scaling.pixelsPerMeter).toFloat))
  }

  def export = new simulation.model.PhysicalWall(
    new Line2D.Double(p1.getXOffset / scaling.pixelsPerMeter,
                      p1.getYOffset / scaling.pixelsPerMeter,
                      p2.getXOffset / scaling.pixelsPerMeter,
                      p2.getYOffset / scaling.pixelsPerMeter),
    thickness
  )

  def buildContextMenu = new WallEdgeContextMenu(WallEdge.this)

  private val reactor = new swing.Reactor {
    listenTo(scaling)
    reactions += {
      case ScalingChange(scaling) => setStroke(new java.awt.BasicStroke((thickness * scaling.pixelsPerMeter).toFloat))
    }
  }

  setStroke(new java.awt.BasicStroke((thickness * scaling.pixelsPerMeter).toFloat))

}

class WallEdgeContextMenu(val node: WallEdge) extends ContextMenu {
  type NodeType = WallEdge

  import javax.swing.JPopupMenu

  protected def buildPopup(edge: WallEdge): JPopupMenu = {
    val menu = new JPopupMenu with swing.Reactor

    val thicknessMenu = new swing.MenuItem("Thickness (" + edge.thickness + ")")

    menu.listenTo(thicknessMenu)
    menu.reactions += {
      case swing.event.ButtonClicked(`thicknessMenu`) => {
        val newRange = swing.Dialog.showInput(thicknessMenu,
                               "Choose new range",
                               "Alter motion sensor range",
                               swing.Dialog.Message.Plain, null, Seq(),
                               edge.thickness.toString)

        newRange map { t =>
          edge.thickness = t.toDouble
        }
      }
    }

    menu.add(thicknessMenu.peer)

    menu
  }
}

// vim: set ts=2 sw=2 et:

