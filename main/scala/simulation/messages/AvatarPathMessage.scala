package simulation.messages

import simulation.model.scenario.AvatarPathNode

object AvatarPathMessage {
  def fromXML(node: scala.xml.Node): AvatarPathMessage =
    AvatarPathMessage((node \ "@index").first.text.toInt, (node \ "@name").first.text, node \ "node" map AvatarPathNode.fromXML toList)
}
case class AvatarPathMessage(index: Int, name: String, nodes: List[AvatarPathNode]) {
  def toTimePath(scaling: gui.model.Scaling) = {
    val timePath = new gui.model.scenario.TimePath(index, name)
    nodes foreach { node =>
      timePath append node.toPathNode(scaling)
    }
    timePath
  }

  def toXML: scala.xml.Node =
      <path index={index.toString} name={name}>
          { nodes map (_.toXML) }
      </path>

}

// vim: set ts=2 sw=2 et:

