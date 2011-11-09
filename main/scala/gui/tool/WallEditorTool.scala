package gui.tool

import gui.model._

import scala.swing._

import gui.model.NodeSelection

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.event._
import edu.umd.cs.piccolo.util._
import edu.umd.cs.piccolo.nodes._

class WallEditorTool(val controller: ToolController,
                     selection: NodeSelection,
                     wallLayer: WallLayer,
                     scaling: gui.model.Scaling) extends Tool {

  val name = "Add/Edit walls"

  override def toolEnter { eventHandler.lastNode = None }
  override def toolLeave { super.toolLeave; eventHandler.lastNode = None }

  val settingsPanel = new BoxPanel(Orientation.Vertical) {
    // TODO: add validation (input > 0)
    val thicknessTxt = new TextField("0.15", 5)

    contents += new Label("Thickness (m)")
    contents += thicknessTxt
  }

  val eventHandler = new PBasicInputEventHandler with DefaultToolControllerCallbackHandler {
    override def mouseEntered(e: PInputEvent): Unit
      = controller.statusMessage = "Left mouse button: Place a wall point, next point will be connected; Middle or Ctrl + Left mouse button: delete wall point or wall segment"

    private var _lastNode: Option[WallNode] = None

    def lastNode = _lastNode
    def lastNode_=(node: Option[WallNode]) {
      _lastNode map (_ setPaint java.awt.Color.BLACK)
      _lastNode = node
      _lastNode map (_ setPaint new java.awt.Color(0x3465a4))
      selection.selectedNode = node
    }


    override def mouseClicked(e: PInputEvent) {

      if (e.isRightMouseButton) lastNode = None
      else if (e.isMiddleMouseButton || (e.isLeftMouseButton() && e.isControlDown())) e.getPickedNode match {
        case n: WallNode => {
          n.removeFromParent
          lastNode map { last => if (last eq n) lastNode = None }
        }
        case n: WallEdge => {
          n.removeFromParent
          lastNode map { last => if ((last eq n.p1) || (last eq n.p2)) lastNode = None }
        }
        case _ => ()
      } else if (e.isLeftMouseButton) e.getPickedNode match {
        case n: WallNode if lastNode.isEmpty => lastNode = Some(n)
        case n: WallNode => lastNode map { last => wallLayer.addEdge(last, n, settingsPanel.thicknessTxt.text.toDouble) }
        case n: WallEdge => {
          val splitNode = wallLayer.splitEdge(n, e.getPosition.getX, e.getPosition.getY)
          lastNode = Some(splitNode)
        }
        case _ => {
          val n = wallLayer.addNode(e.getPosition.getX, e.getPosition.getY)

          lastNode map { last => wallLayer.addEdge(last, n, settingsPanel.thicknessTxt.text.toDouble) }
          lastNode = Some(n)
        }
      }
      super.mouseClicked(e)
    }

    setEventFilter(new PInputEventFilter)
  }


  override def toolLoad(node: scala.xml.NodeSeq) {
    wallLayer.clear
    (node \ "walls" \ "@default-thickness").firstOption map (first => settingsPanel.thicknessTxt.text = first.text)
    val nodeMap = Map() ++ ((node \ "walls" \ "node") map { node =>
      (node \ "@id").first.text.toInt -> (new WallNode {
        setOffset((node \ "@x").first.text.toDouble, (node \ "@y").first.text.toDouble)
      })
    })
    nodeMap.values foreach (wallLayer.nodeLayer addChild _)
    (node \ "walls" \ "edge") map { node =>
      wallLayer.edgeLayer addChild new WallEdge(nodeMap get (node \ "@from").first.text.toInt get, nodeMap get (node \ "@to").first.text.toInt get, (node \ "@thickness").first.text.toDouble, scaling)
    }
  }

  override def toolSave: scala.xml.NodeSeq = {
    val nodes = scala.collection.jcl.Buffer(wallLayer.nodeLayer.getChildrenReference)
    val nodeMap = Map() ++ (nodes.toList.zipWithIndex)

    <walls default-thickness={settingsPanel.thicknessTxt.text}>
      { nodeMap map {
          case (node: WallNode, index: Int) =>
            <node id={index.toString} x={node.getXOffset.toString} y={node.getYOffset.toString}/>
        }
      }
      {
        scala.collection.jcl.Buffer(wallLayer.edgeLayer.getChildrenReference) flatMap {
          case edge: WallEdge => {
            <edge from={(nodeMap get edge.p1).get.toString} to={(nodeMap get edge.p2).get.toString} thickness={edge.thickness.toString}/>
          }
        }
      }
    </walls>
  }

  override def toolClear = wallLayer.clear
}

// vim: set ts=2 sw=2 et:

