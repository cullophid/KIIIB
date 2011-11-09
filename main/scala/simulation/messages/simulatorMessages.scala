package simulation.messages

import simulation.model.Avatar
import simulation.model.scenario.AvatarPathNode

sealed abstract class SimulatorMessage

case class AppendPath(path: List[AvatarPathNode]) extends SimulatorMessage
case class AvatarMoved(id: Int, avatar: Avatar, time: Double, nextNode: Option[AvatarPathNode]) extends SimulatorMessage
case class NodeConsumed(id: Int, node: AvatarPathNode) extends SimulatorMessage

// vim: set ts=2 sw=2 et:

