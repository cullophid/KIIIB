package simulation.model

import core.TimeManager
import core.messages._
import simulation.messages._
import simulation.model.scenario._
import utils.vec._

import scala.actors.Actor
import scala.actors.Actor._
import scala.actors.Exit

class AvatarActor(sim: Simulator, pathMsg: AvatarPathMessage, timeMan: TimeManager) extends Actor {
  var nodes: List[AvatarPathNode] = pathMsg.nodes
  var avatarOpt: Option[Avatar] = None

  var lastTimeOpt: Option[Double] = None

  timeMan ! RegisterForTimeUpdates(this, 25)

  def act {
    trapExit = true
    loop {
    react {
      case AppendPath(nodes0) => {
        nodes = nodes ::: nodes0
      }
      case Ping(time) => {
        val now = time.currentTime
        lastTimeOpt map { lastTime =>
          val delta = (now - lastTime)
          var deltaLeft = delta
          while (deltaLeft > 0 && nodes != Nil) {
            nodes match {
              case Nil => ()
              case n :: ns => {
                if (avatarOpt.isEmpty)
                  avatarOpt = Some(new Avatar(n.pos, 0))
                avatarOpt map { a =>
                  val nodeDist = a.pos distanceTo n.pos
                  val moveDist = deltaLeft * n.speed / 1000.0
                  val dir = (n.pos - a.pos).normalized
                  var newPos = a.pos
                  var newAngle = a.angle
                  if (moveDist == 0 || nodeDist < moveDist) {
                    sim ! NodeConsumed(pathMsg.index, n)
                    nodes = ns
                    newPos = n.pos
                    n match {
                      case DelayNode(pos, _, _, delay) => {
                        val newDelay = delay - deltaLeft
                        if (newDelay > 0)
                          nodes = DelayNode(pos, 0, None, newDelay) :: nodes
                        deltaLeft -= delay
                      }
                      case DeviceActionNode(_, _, _, msg) => sim ! msg
                      case _ => ()
                    }
                    if (moveDist > 0)
                      deltaLeft -= deltaLeft * (moveDist - nodeDist) / moveDist
                  } else {
                    newPos += dir * moveDist
                    deltaLeft = 0
                    newAngle = Vec2(a.pos.x, -a.pos.y) directionTo Vec2(n.pos.x, -n.pos.y)
                  }

                  val newAvatar = new Avatar(newPos, newAngle)
                  avatarOpt = Some(newAvatar)
                  sim ! AvatarMoved(pathMsg.index, newAvatar, now - deltaLeft, nodes.firstOption)
                }
              }
            }
          }
        }
        lastTimeOpt = Some(now)
      }
      case Exit(_,_) =>
        timeMan ! RemoveFromTimeUpdates(this)
//         println("AvatarActor exitting")
        exit
    }
    } // loop
  }

  start
}

// vim: set ts=2 sw=2 et:

