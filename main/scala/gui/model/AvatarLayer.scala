package gui.model

import gui.event._

import scala.swing.Publisher

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.event._
import edu.umd.cs.piccolo.util._
import edu.umd.cs.piccolo.nodes._

class AvatarLayer extends PLayer {
  object inventory extends Publisher

  override def addChild(node: PNode) {
    node match {
      case a: AvatarNode => {
        super.addChild(a)
        _avatars = _avatars + ((a.id, a))
      }
      case _ => require(node.isInstanceOf[AvatarNode])
    }
  }

  override def removeChild(node: PNode) = {
    val removedNode = super.removeChild(node)

    node match {
      case a: AvatarNode => {
        _avatars = _avatars - a.id
        inventory publish AvatarNodeRemoved(a)
      }
      case _ => ()
    }

    removedNode
  }

  override def removeAllChildren = {
    super.removeAllChildren

    _avatars.values map (a => inventory publish AvatarNodeRemoved(a))
  }

  def avatars = _avatars
  private var _avatars = Map[Int, AvatarNode]()

  def export: List[simulation.model.Avatar] = {
    val avatars = scala.collection.jcl.Buffer(getChildrenReference)
    avatars flatMap {
      case n: AvatarNode => Seq(n.export)
      case _ => Nil
    } toList
  }
}

// vim: set ts=2 sw=2 et:

