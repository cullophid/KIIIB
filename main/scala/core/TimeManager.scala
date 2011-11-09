package core

import core.messages._

import scala.actors._
import scala.actors.Actor._
import scala.actors.Exit

import java.util.{Timer, TimerTask}
import java.text.SimpleDateFormat
import java.util.concurrent._

/** Type representing virtual time. */
class Time(val initialTime: Double, realTimeAtInitialTime: Double, val speed: Double) {
  def currentTime: Double
    = initialTime + speed * (System.currentTimeMillis - realTimeAtInitialTime)
  override def toString = simpleDateFormat.format(currentTime.round.toLong)
}
/** To be sent to interested parties. */

private object simpleDateFormat extends SimpleDateFormat("HH:mm:ss")

class TimeManager extends Actor {
  private var _initialTime: Double = 0
  private var _realTimeAtInitialTime: Double = System.currentTimeMillis
  private var _speed: Double = 0
  private var _pingListeners = Map[Actor, java.util.concurrent.ScheduledFuture[_]]()
  private var _changeListeners = Set[Actor]()

  private val pingExecutor = Executors.newSingleThreadScheduledExecutor(pingThreadFactory)

  private object pingThreadFactory extends ThreadFactory {
    val threadFactory = Executors.defaultThreadFactory()
    def newThread(r: Runnable) : Thread = {
      val t: Thread = threadFactory.newThread(r)
      t.setName("Pinger thread")
      t.setDaemon(true)
      t
    }
  }

  def act {
    trapExit = true
    loop {
    react {
      case RegisterForTimeUpdates(l: Actor, interval: Double) => {
        if (!_pingListeners.keys.contains(l))
          _pingListeners += l -> pingExecutor.scheduleAtFixedRate(new Runnable {
              def run: Unit = {
                synchronized {
                  if (_speed > 0)
                    l ! Ping(new Time(_initialTime, _realTimeAtInitialTime, _speed))
                }
              }
            }, 0, interval.round.toLong, TimeUnit.MILLISECONDS)
      }
      case RegisterForTimeFactorChanges(l: Actor) => {
        _changeListeners += l
        l ! Ping(new Time(_initialTime, _realTimeAtInitialTime, _speed))
      }
      case RemoveFromTimeUpdates(l: Actor) => _pingListeners get l map { future =>
        future cancel true
        _pingListeners -= l
      }

      case SetSpeed(speed0) if speed0 >= 0 => synchronized {
        _initialTime += _speed * (System.currentTimeMillis - _realTimeAtInitialTime)
        _realTimeAtInitialTime = System.currentTimeMillis
        _speed = speed0
        (_pingListeners.keys.toList ++ _changeListeners.toList) foreach {
          _ ! Ping(new Time(_initialTime, _realTimeAtInitialTime, _speed))
        }
      }
      case SetInitialTime(initialTime0) => synchronized {
        _initialTime = initialTime0
        _realTimeAtInitialTime = System.currentTimeMillis
        (_pingListeners.keys.toList ++ _changeListeners.toList) foreach {
          _ ! Ping(new Time(_initialTime, _realTimeAtInitialTime, _speed))
        }
      }
      case Exit(_,_) =>
        _pingListeners.values foreach { _ cancel true }
        pingExecutor.shutdown
        println("TimeManager exitting")
        exit
      
      case _ => ()
    }
  } // loop
  }
}

// vim: set ts=2 sw=2 et:

