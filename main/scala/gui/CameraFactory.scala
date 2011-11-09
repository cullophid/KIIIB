package gui

import edu.umd.cs.piccolo._

object CameraFactory {
  def buildCamera(camera: PCamera) = {
    val r = new PRoot
    val l = new PLayer

    r.addChild(camera)
    r.addChild(l)
    camera.addLayer(l)

    camera
  }
}


// vim: set ts=2 sw=2 et:
