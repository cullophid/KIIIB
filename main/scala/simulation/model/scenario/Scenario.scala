package simulation.model.scenario

import simulation.messages._

object Scenario {
  def fromXML(node: scala.xml.Node): Scenario = {
    val id = (node \ "@id").first.text
    val paths = node \ "paths" \ "path" map AvatarPathMessage.fromXML toList

    Scenario(id, paths)
  }
}
case class Scenario(id: String, paths: List[AvatarPathMessage]) {
  override def toString = id

  def toXML: scala.xml.Node =
    <scenario id={id}>
      <paths>
        { paths map (_.toXML) }
      </paths>
    </scenario>

}

// vim: set ts=2 sw=2 et:

