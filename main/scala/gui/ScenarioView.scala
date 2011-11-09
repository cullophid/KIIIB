package gui

import core._
import gui.model._
import gui.model.scenario._
import simulation.model.scenario._

import scala.swing._
import scala.swing.event._

import edu.umd.cs.piccolo.nodes.PPath

class ScenarioView(deviceLayer: DeviceLayer, scaling: Scaling, selection: NodeSelection) extends BorderPanel {
  val canvas     = new ScenarioCanvas(this, deviceLayer, scaling, selection)
  val timeLine   = new TimeLine(true, selection, canvas)

  private var paths = 0

  def onEnter = scenarioList.selectedItem map loadScenario

  private var scenarioCounter = 1
  val addScenarioButton = new Button("Add") { peer setMnemonic 'a' }
  val removeScenarioButton: Button = new Button("Delete") {
    enabled = false
    peer setMnemonic 'd'
  }
  val renameScenarioButton = new Button("Rename") { peer setMnemonic 'r' }

  def loadScenarios(scenarios: List[MutableScenario]) {
    scenarioList.contents = scenarios.toList
    scenarioCounter = scenarios.size + 1
    removeScenarioButton.enabled = scenarios.size > 1
  }

  def revalidateTimeLine {
    timeLine.preferredSize = (100, (1 + paths) * PathNode.pixelsPerSecond)
    timeLine.revalidate
  }

  def clearScenario {
    pathList.listData = Nil
    canvas.clear
    timeLine.clear
    paths = 0
  }
  private def loadScenario(s: MutableScenario) {
    clearScenario
    s.paths.toList.sort((a,b) => a._1 < b._1).map(_._2) foreach { timePath =>
      addPath(timePath)
      timePath.lastOption map { last =>
        selection.selectedNode = Some(last.posNode)
      }
    }
  }
  private def addPath(timePath: TimePath) {
    paths += 1

    timeLine.layer.addChild(timePath.timeLayer)
    canvas.nodeLayer.addChild(timePath.posLayer)
    canvas.nodeLayer.moveToFront
    timePath.posLayer.moveToFront
    pathList.listData = pathList.listData ++ Seq(timePath)
    pathList.selection.selectIndices(pathList.listData.size - 1)

    revalidateTimeLine
  }
  private def removePath(timePath: TimePath) {
    paths -= 1

    canvas.nodeLayer.removeChild(timePath.posLayer)
    timeLine.layer.removeChild(timePath.timeLayer)
    pathList.listData = pathList.listData.filter(_ != timePath)
    pathList.listData foreach { timePath =>
      timePath.index = pathList.listData.indexOf(timePath)
    }
    pathList.selection.selectIndices(pathList.listData.size - 1)
    selection.selectedNode = pathList.selection.items.firstOption match {
      case Some(path) => path.lastOption match {
        case Some(pathNode) => Some(pathNode.posNode)
        case None           => None
      }
      case None       => None
    }

    revalidateTimeLine
  }
  def appendNode(node: PathNode) {
    pathList.selection.items.firstOption map { timePath =>
      timePath.append(node)
    }
    selection.selectedNode = Some(node.posNode)
  }

  private val pathList = new ListView[TimePath] {
    selection.intervalMode = ListView.IntervalMode.Single
  }

  val scenarioList = new ScenarioComboBox {
    reactions += {
      case SelectionChanged(_) => selectedItem map loadScenario
    }
    contents = List(new MutableScenario("Scenario " + scenarioCounter,
                                        Map(0 -> new TimePath(0, "Path 1"))))
  }

  val addPathButton = new Button("Add") { peer setMnemonic 'd' }
  val deletePathButton = new Button("Delete") { peer setMnemonic 'e' }
  val renamePathButton = new Button("Rename") { peer setMnemonic 'n' }
  
  val showOnlySelectedPathRadioButton = new RadioButton("Show only selected") { peer setMnemonic 'o' }
  val showAllPathsRadioButton = new RadioButton("Show all") { peer setMnemonic 'l' }
  
  val pathVisibilityButtonGroup = new ButtonGroup(showOnlySelectedPathRadioButton, showAllPathsRadioButton) {
    select(showAllPathsRadioButton)
  }
  

  val controls = new BorderPanel {
    val scenarioPanel = new BoxPanel(Orientation.Vertical) {
      border = utils.Swing.TitledBorder("Scenarios")
      contents += scenarioList
      contents += new BoxPanel(Orientation.Horizontal) {
        contents += addScenarioButton
        contents += removeScenarioButton
        contents += renameScenarioButton
      }
    }
    val pathPanel = new BoxPanel(Orientation.Vertical) {
      border = utils.Swing.TitledBorder("Paths")
      contents += new ScrollPane(pathList)
      contents += new BoxPanel(Orientation.Vertical) {
        contents += new BoxPanel(Orientation.Horizontal) {
          contents += addPathButton
          contents += deletePathButton
          contents += renamePathButton
        }
        contents += new BoxPanel(Orientation.Horizontal) {
          contents += showOnlySelectedPathRadioButton
          contents += showAllPathsRadioButton
        }
      }
    }

    border = scala.swing.Swing.EmptyBorder(5, 0, 5, 0)
    layout(scenarioPanel) = BorderPanel.Position.North
    layout(pathPanel) = BorderPanel.Position.Center
  }

  layout(utils.Swing.buildCanvasBorderPanel(canvas))    = BorderPanel.Position.Center
  layout(new BorderPanel {
    border = utils.Swing.TitledBorder("Time line")
    layout(timeLine) = BorderPanel.Position.Center
  }               ) = BorderPanel.Position.South
  layout(controls)  = BorderPanel.Position.East

  listenTo(
    addScenarioButton,
    removeScenarioButton,
    renameScenarioButton,
    addPathButton,
    deletePathButton,
    renamePathButton,
    showOnlySelectedPathRadioButton,
    showAllPathsRadioButton,
    pathList.selection
  )

  reactions += {
    case ButtonClicked(`addScenarioButton`) => {
      scenarioCounter += 1
      val p = new TimePath(0, "Path 1")
      val s = new MutableScenario("Scenario " + scenarioCounter, Map(0 -> p))
      scenarioList.contents = scenarioList.contents ++ Seq(s)
      removeScenarioButton.enabled = scenarioList.contents.size > 1
      scenarioList.selectedItem = Some(s)
    }
    case ButtonClicked(`renameScenarioButton`) => scenarioList.selectedItem map { s =>
      val name =  s.id
      val newName = Dialog.showInput(renameScenarioButton,
                                     "Choose new name",
                                     "Rename scenario",
                                     Dialog.Message.Plain, null, Seq(), name)

      newName map { n =>
        s.id = n
        scenarioList.repaint
      }
    }
    case ButtonClicked(`removeScenarioButton`) => {
      val oldSize = scenarioList.contents.size
      scenarioList.selectedItem map (scenarioList.contents -= _)
      removeScenarioButton.enabled = scenarioList.contents.size > 1
    }
    case ButtonClicked(`addPathButton`) => {
      val timePath = new TimePath(paths, "Path " + (paths + 1))
      addPath(timePath)
      scenarioList.selectedItem map { s =>
        s.paths =
          Map() ++ (pathList.listData map { path => (pathList.listData.indexOf(path) -> path) })
      }
    }
    case ButtonClicked(`deletePathButton`) => {
      pathList.selection.items.firstOption map { selected =>
        removePath(selected)
        scenarioList.selectedItem map (_.paths =
          Map() ++ (pathList.listData map { path => (pathList.listData.indexOf(path) -> path) }))
      }
    }
    case ButtonClicked(`renamePathButton`) => pathList.selection.items.firstOption map { p =>
      val name = p.name
      val newName = Dialog.showInput(renameScenarioButton,
                                     "Choose new name",
                                     "Rename scenario",
                                     Dialog.Message.Plain, null, Seq(), name)

      newName map { n =>
        p.name = n
        pathList.repaint
      }
    }
    case ButtonClicked(`showAllPathsRadioButton`) => {
      pathList.listData foreach (_.visible = true)
    }
    case ButtonClicked(`showOnlySelectedPathRadioButton`) => {
      pathList.listData foreach {path =>
        path.visible = pathList.selection.items contains path
      }
    }
    case ListSelectionChanged(`pathList`, _, false) => {
      selection.selectedNode = pathList.selection.items.firstOption match {
        case Some(path) => path.lastOption match {
          case Some(pathNode) => {
            val activity =
              canvas.camera.animateViewToCenterBounds(
                pathNode.posNode.getGlobalBounds,
                false,
                500
              )
            canvas.camera.addActivity(activity)

            Some(pathNode.posNode)
          }
          case None           => None
        }
        case None       => None
      }
      pathVisibilityButtonGroup.selected match {
        case Some(`showOnlySelectedPathRadioButton`) => 
          pathList.listData foreach {path =>
            path.visible = pathList.selection.items contains path
          }
        case _ => ()
      }
    }
  }

  private val dependencyChecker = new DeviceLayer.DeviceDependencyChecker {
    def reasonNotToRemoveDevice(n: gui.model.devices.DeviceNode): Option[String] = {
      scenarioList.contents find { scenario =>
        scenario.paths.values exists { path =>
          path.nodes exists {
            case node: MoveAndPerformDeviceActionPathNode =>
              node.msg.recipient.value == n.id.value.toString
            case _ => false
          }
        }
      } match {
        case Some(scenario: MutableScenario) => Some("The scenario \"" + scenario.id + "\" has a path with an action node referring to the device.")
        case None => None
      }
    }
  }

  deviceLayer.deviceDependencyCheckers += dependencyChecker
}

// vim: set ts=2 sw=2 et:

