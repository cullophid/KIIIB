/*
* Main file for the KIIIB project
* Authors: Andreas Møller, David Emil Lemvigh
*/

package ai.impl

import core._
import core.devices._
import core.messages._
import java.util.Timer
import utils.RichTimer._
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Connection
import java.sql.Statement
//import scala.collection.JavaConversions._
import collection.jcl.Conversions._
import collection.mutable.{Map => MMap}
import java.util.Collections._
import smarthouse.AI
import smarthouse.SmartHouse

class Ai extends AiListener with AI {
    def aiStarted(ctrl: AiController): Unit = { }
    def aiStopped: Unit = { }
    var conn:Connection = null
    var smarthouse:SmartHouse = new SmartHouse(this)
//    var smarthouse:SmartHouse = new SmartHouse()
    try {
        Class.forName("com.mysql.jdbc.Driver")//load the mysql driver
        conn = DriverManager.getConnection("jdbc:mysql://localhost/kiiib?user=KIIIB&password=42")//connect to the database
     }
     catch {
        case sqle:SQLException =>
        println("SQLException: " + sqle.getMessage())
        println("SQLState: " + sqle.getSQLState())
        println("VendorError: " + sqle.getErrorCode())
        case e:Exception =>
           e.printStackTrace()
       }

    var cached: Option[AiController] = None;
    def triggerSwitch(id: Int, state: Boolean) {
        cached match {
            case Some(c) => {
                c.connectionsFrom(MasterDeviceId(id)) foreach {
                    if (state) {
                        x => c.sendDeviceCommand(x.to, TurnOn)
                    } else {
                        x => c.sendDeviceCommand(x.to, TurnOff)
                    }
              }
          }
          case None => {
              println("couldn't trigger switch - no ai controller in cache");
          }    
        }
    }
    /*
    *deviceEventRecieved handles events monitored by the AI
    */
    def deviceEventReceived(c: AiController,id: MasterDeviceId,device: Device,event: DeviceEvent): Unit = {
        cached = Some(c);
      
        (device, event) match {
            case (_: BinarySwitch, TurnedOn(time)) => //switch turned on 
                smarthouse.switchEvent(id.value,1)
//                on(id.value)
            case (_: BinarySwitch, TurnedOff(time)) => // switch turned off
                smarthouse.switchEvent(id.value,0)
//                off(id.value)
            case (_: MotionSensor, MotionEvent(time)) => {// motion sensor events
                smarthouse.sensorEvent(id.value)
            }
            case _ => ()
            
        }
    }
    
    def on(id: Int) {
      println("turn on switch: " + id)
      triggerSwitch(id, true)
    }
    
    def off(id: Int) {
      println("turn off lamp: " + id)
      triggerSwitch(id, false)
    }
    

}

// vim: set ts=2 sw=2 et:


// vim: set ts=4 sw=4 et:
