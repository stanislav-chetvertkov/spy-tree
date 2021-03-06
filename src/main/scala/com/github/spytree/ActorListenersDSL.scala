package com.github.spytree

import akka.actor.Actor.Receive
import akka.actor._
import akka.contrib.pattern.ReceivePipeline
import akka.testkit.{TestKitBase, TestProbe, TestKit}
import com.github.spytree.actors._

import scala.language.implicitConversions

object ActorListenersDSL {

  implicit class ExpectOps[A <: TestKitBase](it: A) {
    def expectResponse[T]: Response[T] = it.expectMsgClass(classOf[Response[T]])

    import scala.reflect.runtime.universe._
    def expectResponse[T: TypeTag](from: String): Option[Response[T]] = it.expectMsgPF() {
      case response: Response[T@unchecked] if typeOf[T] =:= typeOf[T] =>
        if (response.path.contains(from)) Some(response) else None
      case x => None
    }
  }


  implicit class NBList(it: List[NodeBuilder]) {
    def ~(that: NodeBuilder): List[NodeBuilder] = that +: it
  }

  def propByNode(node: NodeBuilder): Props = {
    if (node.proxyTo.isDefined && node.listener.isDefined) {
      ProxyToActor.props(node.children, node.proxyTo.get, node.listener.get)
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

  /**
    * Extension class that allows to create node domain from string
    * @param it
    */
  implicit class NodeDomain(it: String) {
    def replyTo(listener: ActorRef): NodeBuilder =
      NodeBuilder(path = it, Some(listener), implementation = None)

    def withImplementation(implementation: Receive): NodeBuilder =
      NodeBuilder(path = it, listener = None, Some(implementation))

    def /(that: List[NodeBuilder]): NodeBuilder = NodeBuilder(path = it, None, children = that)
  }

}
