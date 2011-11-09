package gui.tool

import java.awt.event.InputEvent

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.event._

import gui.model.BlueprintLayer
import gui.model.Scaling

import scala.swing._
import scala.swing.event._

import java.beans.{PropertyChangeListener, PropertyChangeEvent}
import java.awt.Dimension

class BlueprintTool(val controller: ToolController, bpLayer: BlueprintLayer, scaling: Scaling) extends Tool {

  val name = "Blueprint"

  val settingsPanel = new GridPanel(0, 2) {
    val scaleText = new TextField("1", 4) {
      minimumSize = new Dimension(50, 0)
      tooltip = "Length of the scaling bar in meters"
    }
    val visibleRadio = new CheckBox { selected = true }
    val loadBlueprintButton  = new Button("Load")
    val clearBlueprintButton = new Button("Clear")
    val offsetXText = new TextField("0", 3) { minimumSize = new Dimension(50, 0) }
    val offsetYText = new TextField("0", 3) { minimumSize = new Dimension(50, 0) }

    contents += new Label("Length (m)") {
      peer setDisplayedMnemonic 'l'
      peer setLabelFor scaleText.peer
    }
    contents += scaleText
    contents += new Label("Show ") {
      peer setDisplayedMnemonic 'r'
      peer setLabelFor visibleRadio.peer
    }
    contents += visibleRadio
    contents += new Label("X-offset: ") {
      peer setDisplayedMnemonic 'x'
      peer setLabelFor offsetXText.peer
    }
    contents += offsetXText
    contents += new Label("Y-offset: ") {
      peer setDisplayedMnemonic 'y'
      peer setLabelFor offsetYText.peer
    }
    contents += offsetYText

    contents += loadBlueprintButton
    contents += clearBlueprintButton

    listenTo(
      visibleRadio,
      loadBlueprintButton,
      clearBlueprintButton,
      scaleText,
      offsetXText,
      offsetYText
    )

    reactions += {
      case ButtonClicked(`visibleRadio`) => bpLayer.setVisible(visibleRadio.selected)
      case ButtonClicked(`loadBlueprintButton`) => {
        val fc = new FileChooser(new java.io.File("."))
        fc.showOpenDialog(this) match {
          case FileChooser.Result.Approve => {
            bpLayer.loadBlueprintImage(fc.selectedFile.getPath)
          }
          case _ => { }
        }
      }
      case ButtonClicked(`clearBlueprintButton`) => bpLayer.loadBlueprintImage("")
      case EditDone(`offsetXText`) => updateOffset
      case EditDone(`offsetYText`) => updateOffset

      case EditDone(`scaleText`) => update
    }

  }

  private def updateOffset {
    // TODO validate input

    val x = settingsPanel.offsetXText.text.toDouble
    val y = settingsPanel.offsetYText.text.toDouble

    bpLayer.setBlueprintOffset(x, y)
  }


  private val changeListener = new PropertyChangeListener() {
    def propertyChange(event: PropertyChangeEvent): Unit = update
  }

  bpLayer.anchor1.addPropertyChangeListener(PNode.PROPERTY_FULL_BOUNDS, changeListener)
  bpLayer.anchor2.addPropertyChangeListener(PNode.PROPERTY_FULL_BOUNDS, changeListener)

  val eventHandler = new PDragSequenceEventHandler with DefaultToolControllerCallbackHandler {
    override def mouseEntered(e: PInputEvent): Unit
      = controller.statusMessage = "Click and drag to place a scaling bar that defines the metric units"

    override protected
    def drag(event: PInputEvent) {
      super.drag(event)
      val pos = event.getPosition
      pos.setLocation(pos.getX - 4, pos.getY - 4)
      bpLayer.anchor2.setOffset(pos)

      update
    }

    override protected
    def startDrag(event: PInputEvent) {
      super.startDrag(event)
      val pos = event.getPosition
      pos.setLocation(pos.getX - 4, pos.getY - 4)
      bpLayer.anchor1.setOffset(pos)
      bpLayer.anchor1.setVisible(true)
      bpLayer.anchor2.setOffset(pos)
      bpLayer.anchor2.setVisible(true)
      bpLayer.anchorLine.setVisible(true)
    }

    override protected
    def endDrag(event: PInputEvent) {
      super.endDrag(event)

      settingsPanel.scaleText.requestFocus
    }

    override protected
    def shouldStartDragInteraction(event: PInputEvent) = event.getPickedNode == event.getTopCamera ||
                                                         event.getPickedNode == bpLayer.bgImage

    setEventFilter(new PInputEventFilter(InputEvent.BUTTON1_MASK, InputEvent.SHIFT_MASK))
  }

  private def update {
    val anchorPhysicalLength = settingsPanel.scaleText.text.toDouble
    val len = bpLayer.anchorLength
    if (len > 0) scaling.pixelsPerMeter = len / anchorPhysicalLength
  }

  override def toolSave = {
    update

    <blueprint filename={bpLayer.fileName}
     x1={bpLayer.anchor1.getXOffset.toString}
     y1={bpLayer.anchor1.getYOffset.toString}
     x2={bpLayer.anchor2.getXOffset.toString}
     y2={bpLayer.anchor2.getYOffset.toString}
     ox={settingsPanel.offsetXText.text}
     oy={settingsPanel.offsetYText.text}
     scaling={settingsPanel.scaleText.text}/>
  }

  override def toolLoad(node: xml.NodeSeq) {
    bpLayer.loadBlueprintImage((node \ "blueprint" \ "@filename").first.text)

    try {
      val x1 = (node \ "blueprint" \ "@x1").first.text.toDouble
      val y1 = (node \ "blueprint" \ "@y1").first.text.toDouble
      val x2 = (node \ "blueprint" \ "@x2").first.text.toDouble
      val y2 = (node \ "blueprint" \ "@y2").first.text.toDouble

      bpLayer.anchor1 setOffset (x1, y1)
      bpLayer.anchor1 setVisible true
      bpLayer.anchor2 setOffset (x2, y2)
      bpLayer.anchor2 setVisible true
      bpLayer.anchorLine setVisible true

      settingsPanel.scaleText.text = (node \ "blueprint" \ "@scaling").first.text

      update

      settingsPanel.offsetXText.text = (node \ "blueprint" \ "@ox").first.text
      settingsPanel.offsetYText.text = (node \ "blueprint" \ "@oy").first.text

      updateOffset
    } catch {
      case _: NumberFormatException => println("Couldn't read blueprint scale point coordinates from file")
      case _: NoSuchElementException => println("Couldn't find blueprint scale points in file")
    }
  }

  override def toolClear {
    bpLayer.anchor1.setOffset(0, 0)
    bpLayer.anchor2.setOffset(20, 0)
    update

    bpLayer.loadBlueprintImage("./blueprint.png")

    settingsPanel.offsetXText.text = "0"
    settingsPanel.offsetYText.text = "0"
    updateOffset
  }
}

// vim: set ts=2 sw=2 et:

