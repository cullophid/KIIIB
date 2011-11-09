package gui.event

import edu.umd.cs.piccolo.activities.PTransformActivity
import edu.umd.cs.piccolo.event._
import edu.umd.cs.piccolo.util.PAffineTransform

class ZoomEventHandler extends PZoomEventHandler {
  setMinScale(0.1)
  setMaxScale(2.0)

  var targetScale = 1.0
  var currentActivity: Option[PTransformActivity] = None
  
  val scrollZoomFactor = 1.5

  override def mouseWheelRotated(event: PInputEvent) {
    var amount = event.getWheelRotation match {
      case value if value < 0 => -value * scrollZoomFactor
      case value if value > 0 => value / scrollZoomFactor
      case _                  => 1.0
    }

    currentActivity match {
      case Some(activity) if activity.isStepping => {
        var newTransform = new PAffineTransform(activity.getDestinationTransform)
        if (amount * newTransform.getScale < 0.1)
          amount = 0.1 / newTransform.getScale
        if (amount * newTransform.getScale > 2.0)
          amount = 2.0 / newTransform.getScale

        if (amount != 1.0) {
          newTransform.scaleAboutPoint(amount, event.getPosition.getX, event.getPosition.getY)
          var matrixDest = new Array[Double](6)
          newTransform.getMatrix(matrixDest)
          activity.setDestinationTransform(matrixDest)
        }
      }

      case _ => {
        if (amount * event.getCamera.getViewScale < 0.1)
          amount = 0.1 / event.getCamera.getViewScale
        if (amount * event.getCamera.getViewScale > 2.0)
          amount = 2.0 / event.getCamera.getViewScale

        if (amount != 1.0) {
          var newTransform = event.getCamera.getViewTransform
          newTransform.scaleAboutPoint(amount, event.getPosition.getX, event.getPosition.getY)
          val activity = event.getCamera.animateViewToTransform(newTransform, 100)
          activity setSlowInSlowOut false
          event.getCamera.addActivity(activity)
          currentActivity = Some(activity)
        }
      }
    }

  }
}

// vim: set ts=2 sw=2 et:

