package com.github.spytree

import akka.actor.Actor.Receive
import akka.actor._
import akka.contrib.pattern.ReceivePipeline
import akka.testkit.TestKit
import com.github.spytree.actors.{WrapperForPropsActor, SilentActor, RespondingActor, CustomImplementationActor}

import scala.language.implicitConversions

object ActorListenersDSL {

  def propByNode(node: NodeBuilder):Props = {
    if (node.props.isDefined && node.listener.isDefined){
      WrapperForPropsActor.props(node.children, node.props.get, node.listener.get)
    } else {
      node.implementation match {
        case Some(imp) =>
          CustomImplementationActor.props(node.listener.get, node.children, node.implementation.get)
        case None => node.listener match {
          case Some(l) => RespondingActor.props(node.children, l)
          case None => SilentActor.props(node.children)
        }
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

  implicit def NodeBuilder2ListOfNodeBuilders(value: NodeBuilder): List[NodeBuilder] = List(value)

  implicit class NodeDomain(underlying: String) {
    def replyTo(listener: ActorRef): NodeBuilder =
      NodeBuilder(path = underlying, Some(listener), implementation = None)

    def withImplementation(implementation: Receive): NodeBuilder =
      NodeBuilder(path = underlying, listener = None, Some(implementation))

    def \(that: List[NodeBuilder]):NodeBuilder = NodeBuilder(path = underlying, None, children = that)
  }



}
