package com.github.spytree.actors

import akka.actor.{Props, ActorLogging, Actor, ActorRef}
import com.github.spytree.ActorListenersDSL.Response
import com.github.spytree.{ChildCreator, NodeBuilder}

/**
  * Actor echoes received messages to the listener
  * @param children - children to create
  * @param listener - listener to respond to
  */
class RespondingActor(children: List[NodeBuilder], listener: ActorRef) extends Actor with ChildCreator with ActorLogging {
  log.info(akka.serialization.Serialization.serializedActorPath(self))

  override def default: Actor.Receive = {
    case message: Any =>
      val path = akka.serialization.Serialization.serializedActorPath(self)
      listener ! Response(path, message)
  }

  override def getChildren: List[NodeBuilder] = children
}

object RespondingActor {
  def props(children: List[NodeBuilder], listener: ActorRef):Props =
    Props(classOf[RespondingActor], children, listener)
}