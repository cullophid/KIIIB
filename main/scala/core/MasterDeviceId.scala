package core

/**
 * Id for a device in the master list (can be mapped to different device types).
 * @param value
 *   A unique id value.
 */
case class MasterDeviceId(value: Int) {
  /**
   * Returns the id value.
   * @return the id value.
   */
  override def toString = "" + value
}


// vim: set ts=2 sw=2 et:
