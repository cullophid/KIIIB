package gui

import gui.model.scenario._

import swing._

object ScenarioComboBox {
  private val model = new javax.swing.DefaultComboBoxModel
}
class ScenarioComboBox extends ComboBox[MutableScenario](Seq(new MutableScenario("", Map()))) {
  import ScenarioComboBox.model
  peer.setModel(model)
  private val dataModel = model.asInstanceOf[javax.swing.DefaultComboBoxModel]

  def contents = {
    var list: List[MutableScenario] = Nil
    val size = dataModel.getSize
    (0 to size - 1).foreach { index =>
      list = list ::: List(dataModel.getElementAt(index).asInstanceOf[MutableScenario])
    }
    list
  }
  def contents_=(v: List[MutableScenario]) {
    dataModel.removeAllElements
    v map dataModel.addElement
  }

  def selectedItem = dataModel.getSelectedItem.asInstanceOf[MutableScenario] match {
    case null => None
    case s    => Some(s)
  }

  def selectedItem_=(v: Option[MutableScenario]) {
    v match {
      case Some(s) => dataModel setSelectedItem s
      case None    => dataModel setSelectedItem null
    }
    selection.publish(swing.event.SelectionChanged(this))
  }

  listenTo(selection)
}

// vim: set ts=2 sw=2 et:

