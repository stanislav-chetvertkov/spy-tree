package com.github.spytree

import akka.actor.{ActorRef, Actor}

/**
 * used for requesting test hierarchy readiness status
 */
case object GetStatus

/**
 * Used for creating a test hierarchy
 * Signal from child that it is completely initialized
 * means that child itself and all its children are ready
 * @param path
 */
case class Ready(path: String)

/**
 * Trait that is responsible creation of the actor's children
 */
trait ChildCreator {
  this: Actor =>

  /**
   * Default implementation - switches to it when initialized
   */
  def default: Receive

  /**
   * @return Get children of the current actor
   */
  def getChildren: List[NodeBuilder]

  private val childRefs = getChildren.map(child => context.actorOf(ActorListenersDSL.propByNode(child), child.path))

  private var childSet: Set[String] = getChildren.map(c => c.path).toSet

  childRefs.foreach(child => child ! GetStatus)

  private var respondRef: ActorRef = _

  override def receive: Actor.Receive = {
    case Ready(path) =>
      childSet -= path
      if (childSet.isEmpty) {
        respondRef ! Ready(self.path.name)
        context.become(default)
      }
    case GetStatus =>
      respondRef = sender()
      if (childSet.isEmpty) {
        respondRef ! Ready(self.path.name)
        context.become(default)
      }
  }
}
