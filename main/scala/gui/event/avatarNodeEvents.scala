package gui.event

import scala.swing.event.Event

import gui.model.AvatarNode

sealed abstract class AvatarNodeEvent extends Event
case class AvatarNodeRemoved(node: AvatarNode) extends AvatarNodeEvent

// vim: set ts=2 sw=2 et:

