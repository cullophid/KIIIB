package gui

import swing._
import swing.event._

import java.beans.{PropertyChangeListener, PropertyChangeEvent}

import edu.umd.cs.piccolo._

class LayerVisibilityMenu(layers: Map[String, PLayer]) extends Menu("Layers") {
  private class LayerVisibilityMenuItem(name: String, layer: PLayer) extends CheckMenuItem(name) {
    reactions += {
      case ButtonClicked(_) => layer.setVisible(selected)
    }

    layer.addPropertyChangeListener(PNode.PROPERTY_VISIBLE, new PropertyChangeListener() {
        def propertyChange(event: PropertyChangeEvent): Unit = selected = layer.getVisible
    })

    selected = layer.getVisible
  }

  var counter = 1
  layers map {
    case (name, layer) =>
      val menuItem = new LayerVisibilityMenuItem(counter + ".  " + name, layer)
      contents += menuItem
      if (counter <= 10)
        menuItem.peer setMnemonic '0' + (counter % 10)
      counter += 1
  }

  peer setMnemonic 'l'
}

// vim: set ts=2 sw=2 et:

