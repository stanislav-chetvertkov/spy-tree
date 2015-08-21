package com.github.spytree

import akka.actor.{Props, ActorLogging, Actor}

case object Activate

/**
 * Helper actor for sending messages for specific actor selection
 * @param selection - selection to send a test message
 */
class FakeSenderActor(selection: String, message:Any) extends Actor with ActorLogging {
  override def receive: Receive = {
    case Activate =>
      log.info("pinging")
      context.system.actorSelection(selection) ! message
  }
}

object FakeSenderActor{
  def props(path:String, message:Any) =
    Props(classOf[FakeSenderActor],path, message)
}