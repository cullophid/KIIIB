package core

/**
 * Internal device id used by the drivers.
 * @param value
 *   The driver's id for the device.
 */
case class DriverDeviceId(value: String) {
  /**
   * Returns the id value.
   * @return the id value.
   */
  override def toString = value
}


// vim: set ts=2 sw=2 et:
