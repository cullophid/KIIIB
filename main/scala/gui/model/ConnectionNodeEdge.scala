package gui.model

import gui.model.devices.DeviceNode

class ConnectionNodeEdge(val p1: DeviceNode, val p2: DeviceNode) extends NodeEdge with ShouldNotDrag {
  setStroke(new java.awt.BasicStroke(0))
}

// vim: set ts=2 sw=2 et:

