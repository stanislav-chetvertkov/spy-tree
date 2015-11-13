package com.github.spytree.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.contrib.pattern.ReceivePipeline
import com.github.spytree.ActorListenersDSL.Response
import com.github.spytree.{ChildCreator, NodeBuilder}

class ProxyToActor(children: List[NodeBuilder], proxyTo: ActorRef, listener: ActorRef) extends Actor
with ChildCreator with ActorLogging with ReceivePipeline {

  log.debug(akka.serialization.Serialization.serializedActorPath(self))

  override def default: Actor.Receive = {
    case message =>
      val path = akka.serialization.Serialization.serializedActorPath(self)
      listener ! Response(path, message)
      proxyTo.forward(message)
  }

  override def getChildren: List[NodeBuilder] = children
}

object ProxyToActor {
  def props(children: List[NodeBuilder], proxyTo: ActorRef, listener: ActorRef): Props =
    Props(classOf[ProxyToActor], children, proxyTo, listener)
}