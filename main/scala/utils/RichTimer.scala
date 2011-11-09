package utils

import java.util.{Timer, TimerTask}

class RichTimer(timer: Timer) {
  def scheduleOnce(after: Long)(f: => Unit) {
    timer.schedule(new TimerTask {
      override def run = f
    }, after)
  }

  def scheduleRepeatedly(after: Long, interval: Long)(f: => Unit) = {
    val task = new TimerTask {
      override def run = f
    }
    timer.schedule(task, after, interval)
    task
  }

  def scheduleMultiple(after: Long, interval: Long, n: Int)(f: => Unit) {
    var c: Int = 0

    timer.schedule(new TimerTask {
      override def run = {
        if (c < n)
          f
        else
          cancel
        c += 1
      }
    }, after, interval)
  }

  def scheduleUntil(after: Long, interval: Long, until: Long)(f: => Unit) {
    val untilTime: Long = System.currentTimeMillis + until

    timer.schedule(new TimerTask {
      override def run = {
        if (scheduledExecutionTime < untilTime)
          f
        else
          cancel
      }
    }, after, interval)
  }
}

object RichTimer {
  implicit def timer2rich(t: Timer) = new RichTimer(t)
}

// vim: set ts=2 sw=2 et:

