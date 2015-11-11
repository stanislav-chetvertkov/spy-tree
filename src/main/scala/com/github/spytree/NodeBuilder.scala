package com.github.spytree

import akka.actor.Actor._
import akka.actor.{ActorSystem, ActorRef}

import scala.concurrent.Await

/**
  * @param path - path the node has
  * @param listener
  * @param implementation - custom receive implementation
  * @param children
  */
case class NodeBuilder(path: String, listener: Option[ActorRef] = None,
                       implementation: Option[Receive] = None,
                       children: List[NodeBuilder] = List()) {

  def withListener(listener: ActorRef): NodeBuilder = copy(listener = Some(listener))

  def withImplementation(implementation: Receive): NodeBuilder = copy(implementation = Some(implementation))

  /**
    * Specify children of the actor in the provided block
    * @param f
    * @return
    */
  def \(f: => List[NodeBuilder]):NodeBuilder = copy(children = f)

  /**
    * Concatenate to another NodeBuilder
    * @param that
    * @return
    */
  def ::(that: NodeBuilder):List[NodeBuilder] = this :: that :: Nil

  /**
    * Create actors in the actor system based on NodeBuilder
    * @param system - actor system for materialisation
    *
    * Note: this is a blocking call - it returns when the hierarchy is completely initialize
    *
    * returns hierarchy's root ActorRef
    */
  def materialize(implicit system: ActorSystem): ActorRef = {
    val rootRef = system.actorOf(ActorListenersDSL.propByNode(this), path)
    import akka.pattern.ask
    import akka.util.Timeout

    import scala.concurrent.duration._
    implicit val timeout = Timeout(5.seconds)

    Await.ready(rootRef ? GetStatus, Duration.Inf)
    rootRef
  }
}
