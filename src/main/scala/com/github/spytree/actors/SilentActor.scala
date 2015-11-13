package com.github.spytree.actors

import akka.actor.{Props, ActorLogging, Actor}
import com.github.spytree.{ChildCreator, NodeBuilder}

/**
  * Silent actor - does not respond to any messages, just a placeholder
  * @param children
  */
class SilentActor(children: List[NodeBuilder]) extends Actor with ChildCreator with ActorLogging {
  log.debug(akka.serialization.Serialization.serializedActorPath(self))

  override def default: Receive = {
    case message: String => log.info(message)
  }

  override def getChildren: List[NodeBuilder] = children
}

object SilentActor {
  def props(children: List[NodeBuilder]):Props = Props(classOf[SilentActor], children)
}
