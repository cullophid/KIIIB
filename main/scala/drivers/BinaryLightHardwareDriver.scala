package drivers

import core._
import core.devices._
import core.messages._

import scala.actors.Actor.loop
import scala.actors.Exit

import java.util.Timer
import utils.RichTimer._

//import zwapi._
//import zwapi.nodes._

class BinaryLightHardwareDriver(man: DeviceManager) extends Driver {
  man ! RegisterListenerMessage(this)
  val supportedDevices = BinaryLight :: Nil
  val id = "ZWave light"
  private val devices = Map[DriverDeviceId, Device](
//      DriverDeviceId("Light #2") -> new BinaryLight(false),
      DriverDeviceId("13") -> new BinaryLight(false),
      DriverDeviceId("17") -> new BinaryLight(false),
      DriverDeviceId("18") -> new BinaryLight(false),
      DriverDeviceId("19") -> new BinaryLight(false)
  )
  
/*  
  private var zwController = new ZWNetworkController(25, "") 
  private var zwNetwork = new ZWNetwork(zwController)
  private var zwLamps = Map[DriverDeviceId, ZWLampModule]()
  
  devices foreach { 
    case (key: DriverDeviceId, _) => {
      val lamp = new ZWLampModule(key.toString.toInt)
      zwLamps += (key -> lamp)
      zwNetwork addNode lamp
      lamp off
    }
  }
  println(zwLamps)
*/
  override def act:Unit = {    
    trapExit = true
        
    loop {
      react {
        case GetDriverDeviceList => reply(SetDriverDeviceList(this, devices))
       
/*        case DeviceEventMessage(id, e) => {
          man getMapping(id, this.id) match {
            case Some(lamp) => {
              zwLamps get lamp match {
                case Some(zwLamp) => {
		          e match {
		  	        case TurnedOn(t) => {
		  	          println("lamp on " + lamp)
		  	          zwLamp on
		  	        }
		  	        case TurnedOff(t) => {
		  	          println("lamp off " + lamp)
		  	          zwLamp off
		  	        }
		  	        case _ => {
		  	          //println("other")
		  	        }
		          }   
	            }
                case None => { }
              }

            }
            case None => { }
          }
        }
*/
        case DriverCommandMessage(recipient: DriverDeviceId, command: DeviceCommand) => { 
          println("stuff happened")
        }
        case Exit(_,_) => 
          println("BinaryLightHardwareDriver exitting")
          exit
        case m => {
//          println("BinaryLightHardwareDriver does not understand " + m)
        }
      } // react
    } // loop
    
  } // act
  
} // class