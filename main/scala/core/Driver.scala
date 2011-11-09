package core

import scala.actors.Actor

/** Base driver type. */
trait Driver extends Actor {
  /** The identifying string of this driver. */
  val id: String
  /** A list of supported device types. */
  val supportedDevices: List[MetaDevice]
  /**
   * Returns the driver's id.
   * @return The driver's id.
   */
  override def toString = id
}


// vim: set ts=2 sw=2 et:
