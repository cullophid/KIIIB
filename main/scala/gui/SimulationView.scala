package gui

import ai._
import core._
import core.messages._
import gui.model._
import gui.model.scenario._
import simulation.messages._
import simulation.model.scenario._
import utils.SwingActor

import scala.actors._
import scala.actors.Actor._

import scala.swing._
import scala.swing.event._

import edu.umd.cs.piccolo.PLayer
import edu.umd.cs.piccolo.nodes.{PPath, PText}

class SimulationView(man: core.DeviceManager,
                     sim: simulation.Simulator,
                     timeMan: TimeManager,
                     ai: AiModule,
                     deviceLayer: DeviceLayer,
                     wallLayer: WallLayer,
                     deviceConnectionLayer: DeviceConnectionLayer,
                     scaling: Scaling,
                     selection: NodeSelection) extends BorderPanel with SwingActor {

  private val avatarLayer = new AvatarLayer

  val dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd")
  val timeFormat = new java.text.SimpleDateFormat("HH:mm:ss")
  val dateTimeFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  val canvas = new Canvas {
    val nodeLayer = new PLayer
    layer addChild nodeLayer
    layer addChild avatarLayer

    def clear {
      nodeLayer.removeAllChildren
      avatarLayer.removeAllChildren
      nodeLayer.moveToFront
      avatarLayer.moveToFront
    }

    val timePText = new PText {
      setPickable(false)
      setFont(getFont.deriveFont(java.awt.Font.BOLD))
      setPaint(new java.awt.Color(0x799fcf))
      setTextPaint(java.awt.Color.WHITE)
      setTransparency(0.7f)
      setOffset(10, 24)
    }
    camera.addChild(timePText)

    def timeText_=(string: String) = timePText setText string
    def timeText = timePText getText
  }

  val dateTextField = new TextField(dateFormat.format(new java.util.Date))
  val timeTextField = new TextField(timeFormat.format(new java.util.Date)) {
    columns = 8
    preferredSize = new java.awt.Dimension(50,0)
  }
  val setTimeButton = new Button("Reset") { peer setMnemonic 'r' }
  val playToggleButton = new ToggleButton("Play") { peer setMnemonic 'p' }
  val ffwToggleButton = new ToggleButton("Fast forward") { peer setMnemonic 'a' }
  val recordToggleButton = new ToggleButton("Record events in AI") { peer setMnemonic 'e' }

  val scenarioList = new ScenarioComboBox

  def play {
    sim ! RegisterListenerMessage(this)
    timeMan ! SetSpeed(if (ffwToggleButton.selected) 5 else 1)

    dateTextField.enabled = false
    timeTextField.enabled = false
    setTimeButton.enabled = false
  }

  def pause {
    timeMan ! SetSpeed(0)
    sim ! UnregisterListenerMessage(this)

    timeMan ! RemoveFromTimeUpdates(this)

    dateTextField.enabled = true
    timeTextField.enabled = true
    setTimeButton.enabled = true
  }

  listenTo(
    playToggleButton,
    ffwToggleButton,
    setTimeButton,
    recordToggleButton,
    scenarioList.selection
  )

  reactions += {
    case ButtonClicked(`setTimeButton`) =>
      deviceLayer.resetDevices
      scenarioList.selectedItem map loadScenario

    case ButtonClicked(`playToggleButton`) if playToggleButton.selected => play
    
    case ButtonClicked(`ffwToggleButton`) if playToggleButton.selected =>
      timeMan ! SetSpeed(if (ffwToggleButton.selected) 5 else 1)

    case ButtonClicked(`playToggleButton`) if !playToggleButton.selected => pause

    case ButtonClicked(`recordToggleButton`) if recordToggleButton.selected =>
      ai ! TurnOnRecording

    case ButtonClicked(`recordToggleButton`) if !recordToggleButton.selected =>
      ai ! TurnOffRecording

    case SelectionChanged(`scenarioList`) =>
      scenarioList.selectedItem map loadScenario
  }

  def clearScenario {
    canvas.clear
    timeLine.clear
    avatarLayer.removeAllChildren
  }
  def loadScenario(s: MutableScenario) {
    clearScenario
    var numberOfPaths = 0
    s.paths.values foreach { timePath =>
      timePath.index = numberOfPaths
      addPath(timePath)
      timePath.firstOption match {
        case Some(first) => {
          val aNode = new AvatarNode(timePath.index, first.pos, scaling)
          avatarLayer.addChild(aNode)
        }
        case None        => ()
      }
      numberOfPaths += 1
    }
    timeLine.preferredSize = (100, (1 + numberOfPaths) * PathNode.pixelsPerSecond)
    timeLine.revalidate

    reset(s)
  }
  def addPath(timePath: TimePath) {
    timeLine.layer.addChild(timePath.timeLayer)
    canvas.nodeLayer.addChild(timePath.posLayer)
    canvas.nodeLayer.moveToFront
    timePath.posLayer.moveToFront
    avatarLayer.moveToFront
  }

  def onEnter {
    playToggleButton.selected = false
    setTimeButton.enabled = true

    dateTextField.enabled = scenarioList.selectedItem.isDefined
    timeTextField.enabled = scenarioList.selectedItem.isDefined
    setTimeButton.enabled = scenarioList.selectedItem.isDefined
    playToggleButton.enabled = scenarioList.selectedItem.isDefined
    
    scenarioList.selectedItem map loadScenario

    sim ! SetupDevices(Map() ++ (deviceLayer.devices map {
      case (core.MasterDeviceId(id), deviceNode) => core.DriverDeviceId(id.toString) -> deviceNode.export
    }))
    sim ! SetupWalls(wallLayer.export)
    man ! SetDeviceConnectionsMessage(deviceConnectionLayer.export)
    
    scenarioList.selectedItem map reset
  }

  def reset(s: MutableScenario) {
    sim ! s.export
    s.paths.values.foreach (_.visible = true)
  }

  def onLeave {
    pause
  }

  val timeLine = new TimeLine(false, selection, canvas)

  layout(utils.Swing.buildCanvasBorderPanel(canvas)) = BorderPanel.Position.Center
  layout(new BorderPanel {
    val p0 = new BorderPanel {
      border = utils.Swing.TitledBorder("Scenarios")
      layout(new BorderPanel { layout(new BoxPanel(Orientation.Vertical) {
        contents += scenarioList
        contents += new BoxPanel(Orientation.Horizontal) {
          border = Swing.EmptyBorder
          contents += dateTextField
          contents += timeTextField
          contents += setTimeButton
        }
        contents += new BoxPanel(Orientation.Horizontal) {
          contents += playToggleButton
          contents += ffwToggleButton
          contents += recordToggleButton
        }
      }) = BorderPanel.Position.North }) = BorderPanel.Position.West
    }
    val p1 = new BorderPanel {
      border = utils.Swing.TitledBorder("Time line")
      layout(timeLine) = BorderPanel.Position.Center
    }
    layout(p0) = BorderPanel.Position.West
    layout(p1) = BorderPanel.Position.Center
  }) = BorderPanel.Position.South

  def act = {
    trapExit = true
    timeMan ! RegisterForTimeUpdates(this, 200)
    sim ! RegisterListenerMessage(this)
    loop {
      react {
        case AvatarMoved(id, avatar, _, n) => {
          avatarLayer.avatars get id map { avatarNode =>
            avatarNode.setOffset(avatar.pos.x * scaling.pixelsPerMeter,
                                 avatar.pos.y * scaling.pixelsPerMeter)
            avatarNode.bodyNode.setRotation(avatar.angle)
            n map (_.storyText map (avatarNode.storyText = _))
          }
        }
        case Ping(time) => canvas.timeText = dateTimeFormat format time.currentTime.toLong
        case Exit(_,_) =>
//           println("SimulationView exitting")
          exit
      }
    } // loop 
  }

}

// vim: set ts=2 sw=2 et:

