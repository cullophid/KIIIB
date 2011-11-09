package gui.model

import core._
import core.devices.connections._
import gui.model._
import gui.model.devices.DeviceNode

import scala.swing.Publisher
import edu.umd.cs.piccolo.PLayer

class DeviceConnectionLayer(val selection: NodeSelection,
                            connFactories: List[ConnectionFactory]) extends PLayer with Publisher {
  def clear {
    connections = Map()
    removeAllChildren
  }

  def connect(from: DeviceNode, to: DeviceNode) {
    if (!connExists(from.id, to.id)) {
      connFactories find (_.isConnValid(from.device, to.device)) map {
        fact => {
          val conn = fact.create(from.id, to.id)
          val edge = new ConnectionNodeEdge(from, to)
          connections = connections + {(conn, edge)}
          publish(ConnectionAdded(conn))
          addChild(edge)
        }
      }
    }
  }

  def remove(conn: Connection) {
    connections get conn map (removeChild _)
    connections -= conn
  }

  def export: List[Connection] = connections.keys toList

  private def connExists(from: MasterDeviceId, to: MasterDeviceId)
    = connections.keys.exists(c => c.from == from && c.to == to)

  private var connections: Map[Connection, ConnectionNodeEdge] = Map()

  val dependencyChecker = new DeviceLayer.DeviceDependencyChecker {
    def reasonNotToRemoveDevice(n: DeviceNode) = {
      connections.keys.find(c => c.from == n.id || c.to == n.id) match {
        case Some(conn) => Some("It is part of the connection " + conn)
        case None       => None
      }
    }
  }
}

// vim: set ts=2 sw=2 et:

