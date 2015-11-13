package com.github.spytree.actors

import akka.actor.Actor._
import akka.actor.{Props, ActorLogging, Actor, ActorRef}
import akka.contrib.pattern.ReceivePipeline
import com.github.spytree.ActorListenersDSL.Response
import com.github.spytree.{ChildCreator, NodeBuilder}

class WrapperForPropsActor(children: List[NodeBuilder], props: Props, listener: ActorRef) extends Actor
with ChildCreator with ActorLogging with ReceivePipeline {

  var proxied: ActorRef = _

  override def preStart(): Unit = {
    proxied = context.actorOf(props, "proxied")
  }

  log.debug(akka.serialization.Serialization.serializedActorPath(self))

  override def default: Actor.Receive = {
    case message =>
      val path = akka.serialization.Serialization.serializedActorPath(self)
      listener ! Response(path, message)
      proxied.forward(message)
  }

  override def getChildren: List[NodeBuilder] = children
}

object WrapperForPropsActor {
  def props(children: List[NodeBuilder], props: Props, listener: ActorRef): Props =
    Props(classOf[WrapperForPropsActor], props, listener, listener)
}