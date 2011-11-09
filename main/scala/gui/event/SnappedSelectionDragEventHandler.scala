package gui.event

import gui.model.NodeSelection

import edu.umd.cs.piccolo.event._
import edu.umd.cs.piccolo.PNode

class SnappedSelectionDragEventHandler(selection: NodeSelection,
                                       gridProperties: DrawsGrid.Properties,
                                       orientation: Option[swing.Orientation.Value]) extends PDragSequenceEventHandler {
  def this(selection: NodeSelection, gridProperties: DrawsGrid.Properties) = this(selection, gridProperties, None)

  private var draggedNode: Option[PNode] = None

  override def mouseClicked(event: PInputEvent) {
    super.mouseClicked(event)
    event.getPickedNode match {
      case node: Selectable => selection.selectedNode = Some(node)
      case _ => selection.selectedNode = None
    }
  }

  protected var axisFixPoint: Double = _
  protected var xDiff = 0.0
  protected var yDiff = 0.0

  override def startDrag(event: PInputEvent) {
    super.startDrag(event)
    draggedNode = Some(event.getPickedNode)
    xDiff = event.getPickedNode.getXOffset - event.getPosition.getX
    yDiff = event.getPickedNode.getYOffset - event.getPosition.getY
    orientation map {
      _ match {
        case swing.Orientation.Horizontal => axisFixPoint = event.getPickedNode.getYOffset
        case swing.Orientation.Vertical => axisFixPoint = event.getPickedNode.getXOffset
      }
    }
  }

  override def endDrag(event: PInputEvent) {
    super.endDrag(event)
    draggedNode = None
  }

  override def drag(event: PInputEvent) {
    super.drag(event)
    draggedNode map { dnode =>
      val pos = event.getPosition
      val (snappedX, snappedY) = if (event.isControlDown) snapCoordinate(pos.getX + xDiff, pos.getY + yDiff) else (pos.getX + xDiff, pos.getY + yDiff)

      dnode setOffset (orientation match {
        case Some(swing.Orientation.Horizontal) =>
          new java.awt.geom.Point2D.Double(snappedX, axisFixPoint)
        case Some(swing.Orientation.Vertical) =>
          new java.awt.geom.Point2D.Double(axisFixPoint, snappedY)
        case None =>
          new java.awt.geom.Point2D.Double(snappedX, snappedY)
      })
    }
  }

  def snapCoordinate(x: Double, y: Double): (Double, Double) = {
    val size = gridProperties.pixelsPerUnit / gridProperties.numCells

    ( Math.round(x/size) * size
    , Math.round(y/size) * size )
  }

  override def shouldStartDragInteraction(e: PInputEvent) =
    super.shouldStartDragInteraction(e) &&
      e.getPickedNode != e.getTopCamera &&
      !e.getPickedNode.isInstanceOf[ShouldNotDrag]
}

// vim: set ts=2 sw=2 et:

