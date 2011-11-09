package gui.model.scenario

import simulation.model.scenario.Scenario

object MutableScenario {
  def fromScenario(s: Scenario, scaling: gui.model.Scaling): MutableScenario =
    new MutableScenario(s.id, Map() ++ s.paths.zipWithIndex.map {
      case (k, v) => (v, k.toTimePath(scaling))
    })
}
class MutableScenario(var id: String, var paths: Map[Int, TimePath]) {

  override def toString = id

  def export: Scenario =
    Scenario(id, paths.values map (_ toSimulationType) toList)
}

// vim: set ts=2 sw=2 et:

