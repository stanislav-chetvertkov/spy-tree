package com.github.spytree.actors

import akka.actor.Actor._
import akka.actor.{Props, ActorLogging, Actor, ActorRef}
import akka.contrib.pattern.ReceivePipeline
import com.github.spytree.ActorListenersDSL.Response
import com.github.spytree.{ChildCreator, NodeBuilder}

/**
  * Actor with custom Implementation
  * @param children - list of children
  * @param implementation - custom implementation
  */
class CustomImplementationActor(children: List[NodeBuilder], implementation: Receive, listener: Option[ActorRef]) extends Actor
with ChildCreator with ActorLogging with ReceivePipeline {

  log.debug(akka.serialization.Serialization.serializedActorPath(self))

  private def respond: Actor.Receive = {
    case message: Any =>
      val path = akka.serialization.Serialization.serializedActorPath(self)
      listener.get ! Response(path, message)
  }

  override def default: Actor.Receive = {
    implementation(())
    listener match {
      case Some(ref) => respond andThen implementation
      case None => implementation
    }

  }

  override def getChildren: List[NodeBuilder] = children

}

object CustomImplementationActor {
  def props(listener: ActorRef, children: List[NodeBuilder], implementation: Receive):Props =
    Props(classOf[CustomImplementationActor],children, implementation, Option(listener))
}