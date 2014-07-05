package com.test.actors

import akka.actor._
import scala.collection.mutable
import scala.concurrent.duration._
import akka.actor.SupervisorStrategy.Restart

/**
 * Message holding objects
 */
case class Ping()
case class Work(msg: String)
case class WorkDone()

case class Say()

/**
 * Sample actor that contains two references to other actors.
 */
class TestActor extends Actor {
  val lrActor = context.actorOf(Props[LongRunningActor], "long-running-actor")
  // Pass params to actor contructor with classOf, otherwise we'll get AskTimeoutException
  val paramActor = context.actorOf(Props(classOf[ParamActor], "cool name"), "param-actor-cool-name")
  var savedWork = mutable.ArrayBuffer[String]()

  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy(4, 4 seconds) {
    case _: Exception => Restart
  }

  def receive = {
    case Ping =>
      println("Got message!")
      sender ! "pong"
    case work: Work =>
      savedWork += work.msg

      // Forward message to preserve original sender (User request -> LongRunningActor)
      lrActor forward work
    case WorkDone =>
      println(savedWork.mkString(","))
    case say: Say =>
      paramActor forward say
  }

}

/**
 * An example of an actor that replies to two parents.
 */
class LongRunningActor extends Actor {

  def receive = {
    case work: Work =>
      Thread.sleep(1000)
      println("finished work")
      // User Request is sender here.
      sender ! s"got it! Just added ${work.msg} to list."
      // Use context.parent to refer to immediate parent (TestActor)
      context.parent ! WorkDone
  }
}

/**
 * An example of an actor that has a constructor
 * @param name Any name to send to console
 */
class ParamActor(name: String) extends Actor {

  def receive = {
    case Say =>
      println(s"My name is $name")
  }
}
