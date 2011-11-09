package gui.model

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.nodes.{PPath, PImage}

import java.beans.{PropertyChangeListener, PropertyChangeEvent}
import java.awt.geom.Ellipse2D
import java.awt.Color

class BlueprintLayer extends PLayer {

  private var _fileName = ""
  def fileName = _fileName

  def loadBlueprintImage(fileName: String): Unit = {
    fileName match {
      case "" =>
        bgImage setVisible false
        _fileName = ""

      case str =>
        bgImage setVisible true
        bgImage.setImage(fileName)
        _fileName = str
    }
  }

  def setBlueprintOffset(x: Double, y: Double) {
    bgImage.setOffset(x, y)
  }


  val anchor1 = makeAnchor
  val anchor2 = makeAnchor
  val anchorLine = makeLine(anchor1, anchor2)
  val bgImage = new PImage { setPickable(false) }

  def anchorLength = {
    import utils.vec.Vec._

    val p1 = anchor1.getOffset
    val p2 = anchor2.getOffset

    (p2 - p1).length
  }


  private def makeAnchor = new PPath with ConnectedNode {
    setPathTo(new Ellipse2D.Double(-4, -4, 8, 8))
    setVisible(false)
    setPaint(new Color(0xad7fa8))
  }

  private def makeLine(a1: ConnectedNode, a2: ConnectedNode) = new PPath with NodeEdge {
    val p1 = a1
    val p2 = a2
    setVisible(false)
    setPickable(false)
    setStroke(new java.awt.BasicStroke(4))
    setStrokePaint(new Color(0xad7fa8))
  }

  addChild(bgImage)
  addChild(anchorLine)
  addChild(anchor1)
  addChild(anchor2)

}

// vim: set ts=2 sw=2 et:

