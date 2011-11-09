package utils

import scala.actors._
import java.awt.EventQueue

trait SwingActor extends Actor {
  override val scheduler = new SchedulerAdapter {
    def execute(fun: => Unit) {
      EventQueue.invokeLater {
        new Runnable() { def run() = fun }
      }
    }
  }
}

// vim: set ts=2 sw=2 et:

