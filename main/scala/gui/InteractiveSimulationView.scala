package gui

import ai._
import core._
import core.devices._
import core.messages._
import gui.model._
import gui.model.devices._
import gui.model.scenario._
import simulation._
import simulation.messages._
import simulation.model.scenario._
import utils.SwingActor

import actors._
import actors.Actor._

import swing._
import swing.event._

import java.awt.event.InputEvent

import edu.umd.cs.piccolo.PLayer
import edu.umd.cs.piccolo.event._
import edu.umd.cs.piccolo.nodes._

class InteractiveSimulationView(man: DeviceManager,
                                sim: Simulator,
                                timeMan: TimeManager,
                                ai: AiModule,
                                scaling: gui.model.Scaling,
                                deviceLayer: DeviceLayer,
                                wallLayer: WallLayer,
                                deviceConnectionLayer: DeviceConnectionLayer) extends BorderPanel with SwingActor {

  def onEnter {
    resetView

    sim ! SetupDevices(Map() ++ (deviceLayer.devices map {
      case (core.MasterDeviceId(id), deviceNode) => core.DriverDeviceId(id.toString) -> deviceNode.export
    }))
    sim ! SetupWalls(wallLayer.export)
    man ! SetDeviceConnectionsMessage(deviceConnectionLayer.export)
  }

  def onLeave {
    pause
  }

  def resetView {
    canvas.clear
    avatarNode setVisible false

    settingsPanel.dateTextField.enabled = true
    settingsPanel.timeTextField.enabled = true
    settingsPanel.setTimeButton.enabled = true
    settingsPanel.playToggleButton.selected = false

    lastNodeOpt = None
    nodeMap = Map()
    timeMan ! SetSpeed(0)
    sim ! Scenario("Interactive scenario", Nil)

    val initialTime = dateTimeFormat.parse(settingsPanel.dateTextField.text + " " + settingsPanel.timeTextField.text).getTime
    timeMan ! SetInitialTime(initialTime)
  }

  private def play {
    sim ! RegisterListenerMessage(this)
    timeMan ! SetSpeed(if (settingsPanel.ffwToggleButton.selected) 5 else 1)

    settingsPanel.dateTextField.enabled = false
    settingsPanel.timeTextField.enabled = false
    settingsPanel.setTimeButton.enabled = false
  }

  private def pause {
    sim ! UnregisterListenerMessage(this)
    timeMan ! SetSpeed(0)

    settingsPanel.dateTextField.enabled = true
    settingsPanel.timeTextField.enabled = true
    settingsPanel.setTimeButton.enabled = true
  }

  private val _timePath: TimePath = new TimePath(0, "Interactive path")

  val canvas = new Canvas {
    layer.addChild(_timePath.posLayer)

    var speed = 1.0

    def clear: Unit = {
      _timePath.clear
      _timePath.posLayer.moveToFront
      avatarNode.moveToFront
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

    camera.addInputEventListener(new PBasicInputEventHandler {
      setEventFilter(new PInputEventFilter(InputEvent.BUTTON1_MASK, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK))

      override def mouseClicked(event: PInputEvent) {
        val pos = event.getPosition
        var nodeOpt: Option[PathNode] = None
        val pickedNode = event.getPickedNode
        if (pickedNode.getParent != null
            && pickedNode.getParent.isInstanceOf[BinarySwitchNode]) {
          val bsn = event.getPickedNode.getParent.asInstanceOf[BinarySwitchNode]
          if (pickedNode != bsn.core) {
            val cmd = if (pickedNode == bsn.btn1) TurnOn else TurnOff
            val driverId = DriverDeviceId(bsn.id.toString)
            nodeOpt = Some(new MoveAndPerformDeviceActionPathNode(pos.getX, pos.getY, speed, scaling, new DriverCommandMessage(driverId, cmd)))
          }
        } else {
          nodeOpt = Some(new MovePathNode(pos.getX, pos.getY, speed, scaling))
        }

        nodeOpt map { node =>
          _timePath.append(node)
          val avatarPathNode = node.toAvatarPathNode
          sim ! AvatarPathMessage(0, "Interactive path", List(avatarPathNode))
          nodeMap = nodeMap + {(avatarPathNode, node)}
        }
      }
    })
  }

  private var nodeMap = Map[AvatarPathNode, PathNode]()

  val dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd")
  val timeFormat = new java.text.SimpleDateFormat("HH:mm:ss")
  val dateTimeFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  val settingsPanel = new BoxPanel(Orientation.Vertical) {
    border = utils.Swing.TitledBorder("Settings")

    val dateTextField = new TextField(dateFormat.format(new java.util.Date))
    val timeTextField = new TextField(timeFormat.format(new java.util.Date)) {
      columns = 8
      preferredSize = new java.awt.Dimension(50,0)
    }

    val setTimeButton = new Button("Reset") { peer setMnemonic 'r' }
    val playToggleButton = new ToggleButton("Play") { peer setMnemonic 'p' }
    val ffwToggleButton = new ToggleButton("Fast forward") { peer setMnemonic 'a' }
    val recordToggleButton = new ToggleButton("Record events in AI") { peer setMnemonic 'e' }

    contents += dateTextField
    contents += new BoxPanel(Orientation.Horizontal) {
      contents += timeTextField
      contents += setTimeButton
    }
    contents += new BoxPanel(Orientation.Horizontal) {
      contents += playToggleButton
      contents += ffwToggleButton
    }
    contents += new FlowPanel {
      contents += recordToggleButton
    }

    listenTo(
      setTimeButton,
      playToggleButton,
      recordToggleButton
    )

    reactions += {
      case ButtonClicked(`setTimeButton`) =>
        try {
          resetView
        } catch {
          case _ => ()
        }

      case ButtonClicked(`playToggleButton`) if playToggleButton.selected => play

      case ButtonClicked(`playToggleButton`) if !playToggleButton.selected => pause
      
      case ButtonClicked(`playToggleButton`) if playToggleButton.selected =>
        timeMan ! SetSpeed(if (ffwToggleButton.selected) 5 else 1)

      case ButtonClicked(`recordToggleButton`) if recordToggleButton.selected =>
        ai ! TurnOnRecording

      case ButtonClicked(`recordToggleButton`) if !recordToggleButton.selected =>
        ai ! TurnOffRecording
    }
  }

  layout(utils.Swing.buildCanvasBorderPanel(canvas)) = BorderPanel.Position.Center
  layout(new BorderPanel {
    border = scala.swing.Swing.EmptyBorder(5, 0, 5, 0)
    layout(settingsPanel) = BorderPanel.Position.North
  }) = BorderPanel.Position.East

  val avatarNode = new AvatarNode(0, utils.vec.Vec2(0, 0), scaling)
  avatarNode setVisible false
  canvas.layer.addChild(avatarNode)

  private var lastNodeOpt: Option[AvatarPathNode] = None

  def act = {
    trapExit = true;
    timeMan ! RegisterForTimeUpdates(this, 200)
    loop {

    react {
      case AvatarMoved(0, avatar, _, _) => {
        avatarNode setVisible true
        avatarNode.setOffset(avatar.pos.x * scaling.pixelsPerMeter,
                             avatar.pos.y * scaling.pixelsPerMeter)
        avatarNode.bodyNode.setRotation(avatar.angle)
        lastNodeOpt map { lastNode =>
          nodeMap get lastNode map {last =>
            last.posNode.setOffset(avatarNode.getOffset)
            last.posNode setVisible false
          }
        }
      }
      case NodeConsumed(0, n) => {
        lastNodeOpt map { lastNode =>
          nodeMap get lastNode map { lastPathNode =>
            lastPathNode.prev = None
            lastPathNode.next = None
            _timePath remove lastPathNode
          }
          nodeMap = nodeMap - lastNode
        }
        lastNodeOpt = Some(n)
      }
      case Ping(time) => canvas.timeText = dateTimeFormat format time.currentTime.toLong
      case Exit(_,_) =>
//         println("InteractiveSimulationView exitting")
        exit
    }
    } // loop
  }

  start
}

// vim: set ts=2 sw=2 et:

