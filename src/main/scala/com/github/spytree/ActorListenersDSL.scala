package com.github.spytree

import akka.actor.Actor.Receive
import akka.actor._
import akka.contrib.pattern.ReceivePipeline
import akka.testkit.TestKit

object ActorListenersDSL {

  def propByNode(node: NodeBuilder):Props = {
    val implementation = node.implementation
    implementation match {
      case Some(imp) =>
        CustomImplementationActor.props(node.listener.get, node.children, node.implementation.get)
      case None => node.listener match {
        case Some(l) => RespondingActor.props(node.children, l)
        case None => SilentActor.props(node.children)
      }
    }
  }

  /**
   * Response from test actor
   * Should be used for validating listener
   *
   * @param path - path in the actor system
   * @param message - message that actor received
   */
  case class Response[T](path: String, message: T)


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
      implementation()
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


  /**
   * Actor echoes received messages to the listener
   * @param children - children to create
   * @param listener - listener to respond to
   */
  class RespondingActor(children: List[NodeBuilder], listener: ActorRef) extends Actor with ChildCreator with ActorLogging {
    println(akka.serialization.Serialization.serializedActorPath(self))

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


  implicit def NodeBuilder2ListOfNodeBuilders(value: NodeBuilder): List[NodeBuilder] = List(value)

  implicit class NodeDomain(underlying: String) {
    def replyTo(listener: ActorRef): NodeBuilder =
      NodeBuilder(path = underlying, Some(listener), implementation = None)

    def withImplementation(implementation: Receive): NodeBuilder =
      NodeBuilder(path = underlying, listener = None, Some(implementation))

    def \(that: List[NodeBuilder]):NodeBuilder = NodeBuilder(path = underlying, None, children = that)
  }

  /**
    * Allows to shutdown actor gracefully
    */
  trait GracefulShutdown {
    this:TestKit =>

    def shutdownGracefully(ref:ActorRef):Terminated = {
      watch(ref)
      ref ! PoisonPill
      expectTerminated(ref)
    }
  }

}
