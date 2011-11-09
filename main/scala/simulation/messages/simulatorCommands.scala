package simulation.messages

import core.DriverDeviceId
import simulation.model.devices.PhysicalDevice
import simulation.model.PhysicalWall

sealed abstract class SimulatorCommand

case class SetupDevices(devices: Map[DriverDeviceId, PhysicalDevice]) extends SimulatorCommand
case class SetupWalls(walls: List[PhysicalWall]) extends SimulatorCommand

// vim: set ts=2 sw=2 et:

