package gui.model

import edu.umd.cs.piccolo.PNode

trait ConnectedNode extends PNode {

  def connectionPNode: PNode = this

  override
  def removeFromParent {
    super.removeFromParent
    _edges foreach (_.removeFromParent)
  }

  def removeEdge(e: NodeEdge) {
    _edges = _edges - e
  }

  def addEdge(e: NodeEdge) {
    _edges = e :: _edges
  }

  private var _edges: List[NodeEdge] = Nil
}


// vim: set ts=2 sw=2 et:

