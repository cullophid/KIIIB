package simulation.model.scenario

import core.messages._
import gui.model.scenario._

import utils.vec.Vec2
import utils.vec.Vec2._

object AvatarPathNode {
  def fromXML(node: scala.xml.Node): AvatarPathNode =
    (node \ "@type").first.text match {
      case "move" => MoveNode.fromXML(node)
      case "delay" => DelayNode.fromXML(node)
      case "deviceaction" => DeviceActionNode.fromXML(node)
    }
}
trait AvatarPathNode {
  val typeString: String

  val pos: Vec2
  val speed: Double
  val storyText: Option[String]

  def toPathNode(scaling: gui.model.Scaling): PathNode

  def storyXML: scala.xml.NodeSeq =
    {storyText match {
      case Some(text) =>
        <story-text>
          {text}
        </story-text>
      case None       => scala.xml.NodeSeq.Empty
    }}

  def innerXML: scala.xml.NodeSeq = scala.xml.NodeSeq.Empty
  def toXML: scala.xml.Node =
    <node type={typeString} speed={speed.toString}>
      {pos.toXML}
      {storyXML}
      {innerXML}
    </node>
}

object MoveNode {
  def fromXML(node: scala.xml.Node): AvatarPathNode =
    MoveNode(Vec2.fromXML((node \ "pos").first), (node \ "@speed").first.text.toDouble, (node \ "story-text").firstOption map (_ text))
}
case class MoveNode(pos: Vec2, speed: Double, storyText: Option[String]) extends AvatarPathNode {
  val typeString = "move"

  def toPathNode(scaling: gui.model.Scaling) = new MovePathNode(pos * scaling.pixelsPerMeter, speed, scaling, storyText)
}
object DelayNode {
  def fromXML(node: scala.xml.Node): AvatarPathNode =
    DelayNode(Vec2.fromXML((node \ "pos").first), (node \ "@speed").first.text.toDouble, (node \ "story-text").firstOption map (_ text), (node \ "@delay").first.text.toDouble)
}
case class DelayNode(pos: Vec2, speed: Double, storyText: Option[String], delay: Double) extends AvatarPathNode {
  val typeString = "delay"

  def toPathNode(scaling: gui.model.Scaling) = new MoveAndDelayPathNode(pos * scaling.pixelsPerMeter, speed, scaling, storyText, delay / 1000.0)

  override def toXML: scala.xml.Node =
        <node type="delay" speed={speed.toString} delay={delay.toString}>
          {pos.toXML}
          {storyXML}
        </node>
}
object DeviceActionNode {
  def fromXML(node: scala.xml.Node): AvatarPathNode =
    DeviceActionNode(Vec2.fromXML((node \ "pos").first), (node \ "@speed").first.text.toDouble, (node \ "story-text").firstOption map (_ text), DriverCommandMessage.fromXML((node \ "msg").first))
}
case class DeviceActionNode(pos: Vec2, speed: Double, storyText: Option[String], msg: DriverCommandMessage) extends AvatarPathNode {
  val typeString="deviceaction"

  def toPathNode(scaling: gui.model.Scaling) = new MoveAndPerformDeviceActionPathNode(pos * scaling.pixelsPerMeter, speed, scaling, storyText, msg)

  override def innerXML: scala.xml.NodeSeq = msg.toXML
}

// vim: set ts=2 sw=2 et:

