package gui.event

import gui.model.scenario._

import edu.umd.cs.piccolo.PNode
import edu.umd.cs.piccolo.event._
import edu.umd.cs.piccolo.nodes.PText

class TimeLineSnappedDragHandler(gridProperties: DrawsGrid.Properties, infoText: PText) extends PDragSequenceEventHandler {
  private var draggedNode: Option[PNode] = None

  private var axisFixPoint: Double = _
  private var xDiff = 0.0

  override def shouldStartDragInteraction(event: PInputEvent) = {
    super.shouldStartDragInteraction(event) && event.getPickedNode.isInstanceOf[PathNode.TimeNode]
  }

  override def startDrag(event: PInputEvent) {
    super.startDrag(event)
    axisFixPoint = event.getPickedNode.getYOffset

    xDiff = event.getPickedNode.getXOffset - event.getPosition.getX

    event.getPickedNode.asInstanceOf[PathNode.TimeNode].owner match {
      case owner: MoveAndDelayPathNode if event.isRightMouseButton => xDiff += owner.delay * PathNode.pixelsPerSecond
      case _ => ()
    }
    draggedNode = Some(event.getPickedNode)
  }

  override def endDrag(event: PInputEvent) {
    super.endDrag(event)
    draggedNode = None
    infoText setVisible false
    infoText.moveToBack
  }

  override def drag(event: PInputEvent) {
    super.drag(event)
    draggedNode map { dnode =>
      val pos = event.getPosition
      val snappedX = if (event.isControlDown) snapX(pos.getX + xDiff) else pos.getX + xDiff

      var newTime = snappedX / PathNode.pixelsPerSecond
      val timeNode = event.getPickedNode.asInstanceOf[PathNode.TimeNode]
      val pathNode = timeNode.owner
      (pathNode.prev, pathNode.next) match {
        case (Some(prev), _) if prev.endTime > newTime => newTime = prev.endTime + 1.7E-308
        case _ => ()
      }
      pathNode match {
        case owner: MoveAndDelayPathNode if event.isRightMouseButton => {
          val delay = newTime - owner.startTime
          owner.delay = if (delay >= 0) delay else 0
          newTime = pathNode.endTime
        }
        case owner: PathNode if event.isLeftMouseButton => owner.startTime = newTime
        case _ => return
      }
      val timeToDisplay = pathNode match {
        case owner: MoveAndDelayPathNode if event.isRightMouseButton => owner.delay
        case _ => pathNode.startTime
      }
      infoText.setOffset(newTime * PathNode.pixelsPerSecond + 4, axisFixPoint.toFloat - 4)
      infoText setText (TimeLine.dateFormat.format((timeToDisplay * 1000 - java.util.TimeZone.getDefault.getRawOffset).toLong))
      infoText setVisible true
      infoText.moveToFront
    }
  }

  def snapX(x: Double): Double = {
    val size = gridProperties.pixelsPerUnit / gridProperties.numCells

    Math.round(x/size) * size
  }
}
