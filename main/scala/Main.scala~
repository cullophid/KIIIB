import ai._
import core.devices.MotionSensor
import core.MasterDeviceId
import core.DeviceManager
import core.devices.connections._
import core.TimeManager
import gui._
import gui.event._
import gui.model._
import gui.tool._
import simulation.Simulator

import scala.swing._
import scala.swing.event._

import utils.Swing

import java.awt.event.{InputEvent,KeyEvent}
import java.beans.{PropertyChangeListener, PropertyChangeEvent}

import java.awt.{Color, BasicStroke, Graphics2D, Rectangle}
import java.awt.geom.{Arc2D, Point2D, Ellipse2D, Line2D, Rectangle2D}

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.event._
import edu.umd.cs.piccolo.util._
import edu.umd.cs.piccolo.nodes._

class Layers(val deviceLayer: DeviceLayer,
             val wallLayer: WallLayer,
             val blueprintLayer: BlueprintLayer,
             val deviceConnectionLayer: DeviceConnectionLayer) {

  def assignHomeLayersTo(layer: PLayer) {
    layer.addChild(blueprintLayer)
    layer.addChild(wallLayer)
    layer.addChild(deviceConnectionLayer)
    layer.addChild(deviceLayer)
  }
}

import scala.actors.Actor
import scala.actors.Actor.loop


case object Quit
case class  LinkActor(a: Actor)

class Coordinator extends Actor {
  def act = loop {
    react {
      case LinkActor(a) => link(a)
      case Quit => exit()
    }
  }
}

object Main extends SimpleGUIApplication {
  try {
    import javax.swing.UIManager
    // Get the native look and feel class name
    val nativeLF = UIManager.getSystemLookAndFeelClassName
    // Install the look and feel
    UIManager.setLookAndFeel(nativeLF);
  } catch {
    case e: Exception => println(e.getMessage)
  }

  val prettyPrinter: xml.PrettyPrinter = new scala.xml.PrettyPrinter(80, 2)

  val scaling = new Scaling
  val selection = new NodeSelection
  val deviceLayer = new DeviceLayer
  val wallLayer = new WallLayer(scaling)
  val blueprintLayer = new BlueprintLayer {
    loadBlueprintImage("./blueprint.png")
  }
  val deviceConnectionLayer = new DeviceConnectionLayer(selection, List(new BinarySwitchToBinaryLightConnFactory, new MotionSensorToBinaryLightConnFactory))


  val layers = new Layers(deviceLayer, wallLayer, blueprintLayer, deviceConnectionLayer)


  val layerMenu = new LayerVisibilityMenu(
    Map("Blueprint"   -> blueprintLayer,
        "Connections" -> deviceConnectionLayer,
        "Devices"     -> deviceLayer,
        "Walls"       -> wallLayer)
  )

  val coordinator = new Coordinator
  coordinator.start

  val man = new DeviceManager
  man.start
  coordinator ! LinkActor(man)

  val timeMan = new TimeManager
  timeMan.start
  coordinator ! LinkActor(timeMan)

  val sim = new Simulator(man, Nil, timeMan)
  sim.start
  coordinator ! LinkActor(sim)

  val ai = new AiModule(man, timeMan)
  ai.start
  coordinator ! LinkActor(ai)

  ai ! SetAi(Some(new _root_.ai.impl.SimpleAi))


  val deviceNodeActor = new DeviceNodeActor(man, deviceLayer)
  deviceNodeActor.start
  coordinator ! LinkActor(deviceNodeActor)

  val motionSensorHardwareDriver = new drivers.MotionSensorHardwareDriver(man)
  motionSensorHardwareDriver.start
  coordinator ! LinkActor(motionSensorHardwareDriver)
    
//  val binarySensorHardwareDriver = new drivers.BinaryLightHardwareDriver(man)
//  binarySensorHardwareDriver.start
//  coordinator ! LinkActor(binarySensorHardwareDriver)

  val driverDeviceToDeviceListViewActor = new DriverDeviceToDeviceListViewActor(Seq(motionSensorHardwareDriver))
//  val driverDeviceToDeviceListViewActor = new DriverDeviceToDeviceListViewActor(Seq(binarySensorHardwareDriver))
  driverDeviceToDeviceListViewActor.start
  coordinator ! LinkActor(driverDeviceToDeviceListViewActor)


  val statusBar = new Label("")

  val toolController = new ToolController {
    def statusMessage = statusBar.text
    def statusMessage_=(msg: String): Unit = statusBar.text = msg
  }
  
  val deviceNodeFactories = Seq(new MotionSensorNodeFactory(scaling),
                                new BinaryLightNodeFactory(scaling),
                                new BinarySwitchNodeFactory(scaling))
  val deviceNodeFactory = new gui.model.devices.DeviceNodeFactory(scaling)

  val tools = Seq(
    new SelectTool(toolController),
    new BlueprintTool(toolController, blueprintLayer, scaling),
    new AddDeviceTool(toolController, deviceLayer, selection, deviceNodeFactories, deviceNodeFactory),
    new DeviceConnectionTool(toolController, deviceLayer, deviceConnectionLayer),
    new RotateDeviceTool(toolController, selection),
    new MappingTool(toolController, selection, man, driverDeviceToDeviceListViewActor),
    new WallEditorTool(toolController, selection, wallLayer, scaling)
  )

  val homeView = new HomeView(selection, wallLayer, deviceLayer, deviceConnectionLayer, scaling, tools)

  val scenarioView = new ScenarioView(deviceLayer, scaling, selection)

  val simView = new SimulationView(man, sim, timeMan, ai, deviceLayer, wallLayer, deviceConnectionLayer, scaling, selection)
  simView.start

  coordinator ! LinkActor(simView)

  val interactiveSimView = new InteractiveSimulationView(man, sim, timeMan, ai, scaling, deviceLayer, wallLayer, deviceConnectionLayer)

  coordinator ! LinkActor(interactiveSimView)

  val scenarioPane = new ViewPage("Scenario editor", scenarioView, "Edit your scenarios!") {
    def viewEnter {
      layers.assignHomeLayersTo(scenarioView.canvas.layer)
      deviceLayer.resetDevices
      scenarioView.onEnter
    }
    def viewLeave { selection.selectedNode = None }
  }

  val simPane = new ViewPage("Scenario simulator", simView, "Simulate your scenarios!") {
    def viewEnter {
      layers.assignHomeLayersTo(simView.canvas.layer)
      deviceLayer.resetDevices
      simView.onEnter
    }
    def viewLeave { simView.onLeave }
  }

  val interactiveSimPane = new ViewPage("Interactive simulator", interactiveSimView, "Have fun!") {
    def viewEnter {
      layers.assignHomeLayersTo(interactiveSimView.canvas.layer)
      deviceLayer.resetDevices
      interactiveSimView.onEnter
    }
    def viewLeave { interactiveSimView.onLeave }
  }

  val views = Seq(
    new ViewPage("Home builder", homeView, "Build your home!") {
      def viewEnter {
        layers.assignHomeLayersTo(homeView.canvas.layer)
        deviceLayer.resetDevices
      }
      def viewLeave { selection.selectedNode = None }
    },

    scenarioPane,
    simPane,
    interactiveSimPane
  )

  val viewPane = new ViewPane(views)

  override val top = new Frame {
    title = "SaVoIL"

    val statusPanel = new BorderPanel {
      layout(new Label("Status: ")) = BorderPanel.Position.West
      layout(statusBar)             = BorderPanel.Position.Center
    }

    contents = new BorderPanel{
      layout(viewPane) = BorderPanel.Position.Center
      layout(statusPanel) = BorderPanel.Position.South
    }

    minimumSize = new java.awt.Dimension(900, 600)

    val menuFileNew = new MenuItem("New setup") { peer.setMnemonic('n') }
    val menuFileLoad = new MenuItem("Open setup...") { peer.setMnemonic('o') }
    val menuFileSave = new MenuItem("Save setup") { peer.setMnemonic('s') }
    val menuFileSaveAs = new MenuItem("Save setup as...") { peer.setMnemonic('a') }
    val menuFileQuit = new MenuItem("Quit") { peer.setMnemonic('q') }

    val menuViewShowSensorRanges = new MenuItem("Show all motion sensor ranges") { peer setMnemonic 's' }
    val menuViewHideSensorRanges = new MenuItem("Hide all motion sensor ranges") { peer setMnemonic 'h' }

    val sep = new Separator

    menuBar = buildMenuBar(loadRecentList)
    
    def buildMenuBar(recentList: List[String]): MenuBar = new MenuBar {
      contents += new Menu("File") {
        contents += menuFileNew
        contents += menuFileLoad
        contents += sep
        contents += menuFileSave
        contents += menuFileSaveAs
        
        if (recentList != Nil)
          contents +=sep

        var counter = 1
        recentList take 10 foreach { x =>
          contents += new MenuItem(counter + ".  " + x.substring(x.lastIndexOf("/") + 1)) {
            if (counter <= 10)
              peer setMnemonic ('0' + (counter % 10))
            reactions += {
              case ButtonClicked(_) =>
                loadSetup(x)
                workingFile = Some(x)
            }
          }
          counter += 1
        }
        
        contents += sep
        contents += menuFileQuit

        peer setMnemonic 'f'
      }
      contents += new Menu("View") {
        contents += layerMenu
        contents += sep
        contents += menuViewShowSensorRanges
        contents += menuViewHideSensorRanges

        peer setMnemonic 'v'
      }
    }

    listenTo(menuFileNew,
             menuFileLoad,
             menuFileSave,
             menuFileSaveAs,
             menuFileQuit,
             menuViewShowSensorRanges,
             menuViewHideSensorRanges)

    reactions += {
      case ButtonClicked(`menuFileNew`) =>
        newSetup
        workingFile = None

      case ButtonClicked(`menuFileLoad`) =>
        val chooser = new ConfigFileChooser
        chooser.title = "Open setup..."
        chooser.showOpenDialog(menuFileLoad) match {
          case FileChooser.Result.Approve => {
            val shouldLoad: Boolean = workingFile match {
              case Some(fileName) if fileName == chooser.selectedFile.getCanonicalPath => {
                Dialog.showConfirmation(menuFileLoad,
                                        "This will revert '" + fileName +
                                        "' to its last saved state. " +
                                        "Do you want to do this?",
                                        "Revert setup?",
                                        Dialog.Options.YesNo) match {
                  case Dialog.Result.Yes => true

                  case Dialog.Result.No
                      |Dialog.Result.Cancel => false
                }
              }

              case Some(fileName) => {
                Dialog.showConfirmation(menuFileLoad,
                                        "Do you want to save '" + fileName
                                        + "' before loading '" +
                                        chooser.selectedFile.getCanonicalPath
                                        + "'?", "Save existing setup?",
                                        Dialog.Options.YesNoCancel) match {
                  case Dialog.Result.Cancel
                      |Dialog.Result.Closed => false

                  case Dialog.Result.Yes =>
                    saveSetup(fileName)
                    true

                  case Dialog.Result.No => true
                }
              }

              case None => true
            }

            if (shouldLoad) {
              loadSetup(chooser.selectedFile.getCanonicalPath)
              workingFile = Some(chooser.selectedFile.getCanonicalPath)
            }
          }

          case _ => ()
        }

      case ButtonClicked(`menuFileSave`) => {
        workingFile match {
          case Some(fileName) => saveSetup(fileName)

          case None => {
            val chooser = new ConfigFileChooser
            chooser.title = "Save setup..."
            chooser.showSaveDialog(menuFileSave) match {
              case FileChooser.Result.Approve =>
                saveSetup(chooser.selectedFile.getCanonicalPath)
                workingFile = Some(chooser.selectedFile.getCanonicalPath)
              case _ => ()
            }
          }
        }
      }

      case ButtonClicked(`menuFileSaveAs`) => {
        val chooser = new ConfigFileChooser
        chooser.title = "Save setup..."
        chooser.showSaveDialog(menuFileSaveAs) match {
          case FileChooser.Result.Approve =>
            saveSetup(chooser.selectedFile.getCanonicalPath)
            workingFile = Some(chooser.selectedFile.getCanonicalPath)

          case _ => ()
        }
      }

      case ButtonClicked(`menuFileQuit`) =>
        if (shouldQuit) {
          coordinator ! Quit
          System.exit(0)
        }

      case ButtonClicked(`menuViewShowSensorRanges`) =>
        deviceLayer.showMotionSensorRanges(true)

      case ButtonClicked(`menuViewHideSensorRanges`) =>
        deviceLayer.showMotionSensorRanges(false)

      case WindowClosing(_) =>
        if (shouldQuit) {
          coordinator ! Quit
          System.exit(0)
        }
    }
  }


  var workingFile: Option[String] = None

  class ConfigFileChooser extends FileChooser {
    fileSelectionMode = FileChooser.SelectionMode.FilesOnly
    peer setMultiSelectionEnabled false
    fileFilter = new javax.swing.filechooser.FileNameExtensionFilter("XML configuration file", "xml")
  }

  def shouldQuit: Boolean = workingFile match {
    case Some(fileName) => {
      Dialog.showConfirmation(top.menuFileQuit,
                              "Do you want to save '" + fileName
                              + "' before quitting?",
                              "Save existing setup?",
                              Dialog.Options.YesNo) match {
        case Dialog.Result.Cancel
            |Dialog.Result.Closed => false

        case Dialog.Result.Yes =>
          val chooser = new ConfigFileChooser
          chooser.title = "Save setup..."
          chooser.showSaveDialog(top.menuFileQuit) match {
            case FileChooser.Result.Approve =>
              saveSetup(chooser.selectedFile.getCanonicalPath)
              true

            case _ => false
          }

        case Dialog.Result.No => true
      }
    }

    case None => {
      Dialog.showConfirmation(top.menuFileQuit,
                              "Do you want to save your setup " +
                              "before quitting?",
                              "Save existing setup?",
                              Dialog.Options.YesNo) match {
        case Dialog.Result.Cancel
            |Dialog.Result.Closed => false

        case Dialog.Result.Yes =>
          val chooser = new ConfigFileChooser
          chooser.title = "Save setup..."
          chooser.showSaveDialog(top.menuFileQuit) match {
            case FileChooser.Result.Approve =>
              saveSetup(chooser.selectedFile.getCanonicalPath)
              true

            case _ => false
          }

        case Dialog.Result.No => true
      }
    }
  }

  def newSetup {
    import gui.model.scenario._
    // Clear scenarios
    scenarioView.clearScenario
    scenarioView.scenarioList.contents = List(new MutableScenario("Scenario 1", Map(0 -> new TimePath(0, "Path 1"))))
    simView.clearScenario
    interactiveSimView.resetView

    if (viewPane.selection.page == simPane)
      simView.onEnter
    else if (viewPane.selection.page == interactiveSimPane)
      interactiveSimView.onEnter

    // Clear tool settings
    homeView.toolPanel.tools foreach (_ toolClear)

    // Clear AI log
    aiClear

    top.title = "SaVoIL"
  }

  def saveSetup(uri: String) {
    val xml =
<setup>
  <views>
    <home
     panx={homeView.canvas.camera.getViewBounds.getOrigin.getX.toString}
     pany={homeView.canvas.camera.getViewBounds.getOrigin.getY.toString}
     scale={homeView.canvas.camera.getViewScale.toString}/>
    <scenario
     panx={scenarioView.canvas.camera.getViewBounds.getOrigin.getX.toString}
     pany={scenarioView.canvas.camera.getViewBounds.getOrigin.getY.toString}
     scale={scenarioView.canvas.camera.getViewScale.toString}/>
    <simulation
     panx={simView.canvas.camera.getViewBounds.getOrigin.getX.toString}
     pany={simView.canvas.camera.getViewBounds.getOrigin.getY.toString}
     scale={simView.canvas.camera.getViewScale.toString}/>
    <interactive-simulation
     panx={interactiveSimView.canvas.camera.getViewBounds.getOrigin.getX.toString}
     pany={interactiveSimView.canvas.camera.getViewBounds.getOrigin.getY.toString}
     scale={interactiveSimView.canvas.camera.getViewScale.toString}/>
  </views>
  {homeView.toolPanel.tools map (tool => tool.toolSave)}
  <scenarios>
    {scenarioView.scenarioList.contents.map(_.export.toXML)}
  </scenarios>
  {aiSave}
</setup>

    val writer = new java.io.PrintWriter(new java.io.FileOutputStream(uri))
    writer.println(prettyPrinter.format(xml))
    writer.close
    top.title = "SaVoIL - " + new java.io.File(uri).getName
  }

  def loadSetup(uri: String) {
    try {
      val src = scala.io.Source.fromFile(uri)
      val cpa = scala.xml.parsing.ConstructingParser.fromSource(src, false) // fromSource initializes automatically
      val doc = cpa.document
      val ele = doc.docElem

      def callWithIfDefined(fun: Double => Unit, nodeSeq: scala.xml.NodeSeq) =
        nodeSeq.firstOption map ( node => fun((node).text.toDouble) )

      def callWithIfBothDefined(fun: (Double, Double) => Unit, nodeSeqs: Tuple2[scala.xml.NodeSeq, scala.xml.NodeSeq]) = {
        (nodeSeqs._1.firstOption, nodeSeqs._2.firstOption) match {
          case (Some(node1), Some(node2)) => fun(node1.text.toDouble, node2.text.toDouble)
          case _ => ()
        }
      }

      // Load view settings
      callWithIfDefined(homeView.canvas.camera.setViewScale _, ele \ "views" \ "home" \ "@scale")
      callWithIfDefined(scenarioView.canvas.camera.setViewScale _, ele \ "views" \ "scenario" \ "@scale")
      callWithIfDefined(simView.canvas.camera.setViewScale _, ele \ "views" \ "simulation" \ "@scale")
      callWithIfDefined(simView.canvas.camera.setViewScale _, ele \ "views" \ "interactive-simulation" \ "@scale")

      val homeBounds = homeView.canvas.camera.getViewBounds
      callWithIfBothDefined(homeBounds.setOrigin _, (ele \ "views" \ "home" \ "@panx", ele \ "views" \ "home" \ "@pany"))
      homeView.canvas.camera setViewBounds homeBounds

      val scenarioBounds = scenarioView.canvas.camera.getViewBounds
      callWithIfBothDefined(scenarioBounds.setOrigin _, (ele \ "views" \ "scenario" \ "@panx", ele \ "views" \ "scenario" \ "@pany"))
      scenarioView.canvas.camera setViewBounds scenarioBounds

      val simBounds = simView.canvas.camera.getViewBounds
      callWithIfBothDefined(simBounds.setOrigin _, (ele \ "views" \ "simulation" \ "@panx", ele \ "views" \ "simulation" \ "@pany"))
      simView.canvas.camera setViewBounds simBounds

      val interSimBounds = interactiveSimView.canvas.camera.getViewBounds
      callWithIfBothDefined(interSimBounds.setOrigin _, (ele \ "views" \ "interactive-simulation" \ "@panx", ele \ "views" \ "interactive-simulation" \ "@pany"))
      interactiveSimView.canvas.camera setViewBounds interSimBounds

      // Load tool settings
      homeView.toolPanel.tools foreach (_ toolLoad(ele))

      // Load scenarios
      val scenarios = (ele \ "scenarios" \ "scenario") map (simulation.model.scenario.Scenario.fromXML _)
      val mutables = scenarios.map (s => gui.model.scenario.MutableScenario.fromScenario(s, scaling)).toList
      scenarioView.loadScenarios(mutables)
      if (viewPane.selection.page == simPane)
        simView.onEnter
      else if (viewPane.selection.page == interactiveSimPane)
        interactiveSimView.onEnter

      // Load AI log
      aiLoad(ele)

      var recentList = loadRecentList
      recentList = uri :: (recentList - uri) take 10
      saveRecentList(recentList)
      top.menuBar = top.buildMenuBar(recentList)
      top.title = "SaVoIL - " + new java.io.File(uri).getName
    } catch {
      case e: java.io.FileNotFoundException => Console.err.println("Could not find file '" + uri + "'.")
      case e: Exception => e.printStackTrace
    }
  }
  
  def loadRecentList: List[String] = {
    val configFile = new java.io.File(System.getProperty("user.home") + "/.simulation-and-visualization-of-intelligent-lighting/recent.xml")
    if (configFile.exists && configFile.isFile && configFile.length > 0) {
      val cpa = scala.xml.parsing.ConstructingParser.fromFile(configFile, false) // fromSource initializes automatically
      val doc = cpa.document
      val ele = doc.docElem

      (ele \ "file").map {fileNode =>
        (fileNode \ "@path").first.text
      }.toList
    } else {
      saveRecentList(Nil)
      Nil
    }
  }
  
  def saveRecentList(recentList: List[String]) {
    val recentNode = 
      <recent>
        {
          recentList.map { x =>
            <file path={x}/>
          }
        }
      </recent>

    val configDir = System.getProperty("user.home") + "/.simulation-and-visualization-of-intelligent-lighting"
    val configFile = new java.io.File(configDir)
    if (configFile.exists && configFile.isDirectory || configFile.mkdirs) {
      val writer = new java.io.PrintWriter(new java.io.FileOutputStream(System.getProperty("user.home") + "/.simulation-and-visualization-of-intelligent-lighting/recent.xml"))
      writer.println(prettyPrinter.format(recentNode))
      writer.close
    }
  }

  def aiLoad(ele: xml.Node): Unit = {
    import core.devices._
    ai ! SetEventLog ((ele \ "ai-log" \ "device-event-message").map { eventNode =>
      val id = MasterDeviceId((eventNode \ "@id").first.text.toInt)
      val timestamp = (eventNode \ "event" \ "@timestamp").first.text.toDouble

      core.messages.DeviceEventMessage(id, (eventNode \ "event" \ "@type").first.text match {
        case "motionevent" => new MotionEvent(timestamp)
        case "turnedon"    => new TurnedOn(timestamp)
        case "turnedoff"   => new TurnedOff(timestamp)
      })
    }.toList)
  }

  def aiSave: xml.NodeSeq = {
    ai !? GetEventLog match {
      case EventLog(log) =>
        <ai-log>
          {log map (_ toXML)}
        </ai-log>
      case _ => xml.NodeSeq.Empty
    }
  }

  def aiClear = ai ! SetEventLog(Nil)
}

// vim: set ts=2 sw=2 et:

