package core.devices

object MotionSensor extends MetaDevice {
  val shortName = "Motion sensor"

  def fromXML(node: scala.xml.Node) =
    new MotionSensor((node \ "@range").first.text.toDouble,
                     (node \ "@anglespan").first.text.toDouble,
                     (node \ "@cooldownmillis").first.text.toLong,
                     (node \ "@mincheckinterval").first.text.toLong)
}

class MotionSensor(val range: Double,
                   val angleSpan: Double,
                   val coolDownMillis: Long,
                   val minCheckInterval: Long) extends Device {

  val meta = MotionSensor

  def this(range: Double, angleSpan: Double) = this(range, angleSpan, 2000, 250)
  def this(range: Double) = this(range, 2 * Math.Pi / 3)

  def copyNewRange(range1: Double) = new MotionSensor(range1, angleSpan, coolDownMillis, minCheckInterval)

  def copyNewAngleSpan(angleSpan1: Double) = new MotionSensor(range, angleSpan1, coolDownMillis, minCheckInterval)
  def copyNewCoolDownMillis(coolDownMillis0: Long) = new MotionSensor(range, angleSpan, coolDownMillis0, minCheckInterval)
  def copyNewMinCheckInterval(minCheckInterval0: Long) = new MotionSensor(range, angleSpan, coolDownMillis, minCheckInterval0)

  def toXML =
    <device
     type="motionsensor"
     range={range.toString}
     anglespan={angleSpan.toString}
     cooldownmillis={coolDownMillis.toString}
     mincheckinterval={minCheckInterval.toString} />
}
case class MotionEvent(timestamp: Double) extends DeviceEvent {
  def toXML: scala.xml.Node = <event type="motionevent" timestamp={timestamp.toString}/>
}


// vim: set ts=2 sw=2 et:
