package gui.model

import scala.swing.Publisher
import scala.swing.event.Event

case class ScalingChange(scaling: Scaling) extends Event

/**
 * The gui operates with two metics. The dimensionless pixels (wall and
 * device positions) and physical dimensions such as motion sensor range, avatar speed.
 *
 * The Scaling class holds the conversion ratio between the physical dimensions and the dimensionless gui positions
 */
class Scaling extends Publisher with DrawsGrid.Properties {

  def pixelsPerUnit = pixelsPerMeter
  val numCells = 10
  def pixelsPerMeter: Double = _pixelsPerMeter

  def pixelsPerMeter_=(ppm: Double) {
    require(ppm > 0)
    _pixelsPerMeter = ppm

    publish(ScalingChange(this))
  }


  /** The snapping grid cell size in meters */
  def snapGridSize = _snapGridSize
  def snapGridSize_=(size: Double) {
    require(size > 0)
    _snapGridSize = size
  }

  private var _pixelsPerMeter: Double = 20.0
  private var _snapGridSize: Double = 0.10 // in meters



}

// vim: set ts=2 sw=2 et:

