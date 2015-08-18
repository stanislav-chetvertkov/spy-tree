package com.github.spytree

import akka.actor.Actor.Receive
import akka.actor._
import akka.testkit.TestKit
import org.scalatest.BeforeAndAfterAll

import scala.concurrent.Await

trait DefaultShutdown {
  this: TestKit with BeforeAndAfterAll =>

  override def afterAll() {
    shutdown()
  }
}

class FakeSenderActor(selection:String) extends Actor with ActorLogging {
  override def receive: Receive = {
    case "ping" =>
      log.info("pinging")
      context.system.actorSelection(selection) ! "Ping"
  }
}

object ActorListenersDSL {

  def propByNode(node: NodeBuilder) = {
    val implementation = node.implementation
    implementation match {
      case Some(imp) => CustomImplementationActor.props(node.listener.get, node.children, node.implementation.get)
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
  case class Response(path: String, message: Any)

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

  trait ChildCreator {
    this: Actor =>

    def default: Receive

    def getChildren: List[NodeBuilder]

    val childRefs = getChildren.map(child => context.actorOf(propByNode(child), child.path))

    var childSet: Set[String] = getChildren.map(c => c.path).toSet

    childRefs.foreach(child => child ! GetStatus)

    var respondRef: ActorRef = _

    override def receive: Actor.Receive = {
      case Ready(path) =>
        //        println("Ready:" + path)
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


  /**
   * Silent actor - does not respond to any messages, just a placeholder
   * @param children
   */
  class SilentActor(children: List[NodeBuilder]) extends Actor with ChildCreator with ActorLogging {
    println(akka.serialization.Serialization.serializedActorPath(self))

    override def default: Receive = {
      case message: String => log.info(message)
    }

    override def getChildren: List[NodeBuilder] = children
  }

  object SilentActor {
    def props(children: List[NodeBuilder]) = Props(classOf[SilentActor], children)
  }


  /**
   * Actor with custom Implementation
   * @param children
   * @param implementation
   */
  class CustomImplementationActor(children: List[NodeBuilder], implementation: Receive) extends Actor with ChildCreator with ActorLogging {
    println(akka.serialization.Serialization.serializedActorPath(self))

    override def default: Actor.Receive = implementation

    override def getChildren: List[NodeBuilder] = children
  }

  object CustomImplementationActor {
    def props(listener: ActorRef, children: List[NodeBuilder], implementation: Receive) = Props(classOf[CustomImplementationActor],
      children, implementation)
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
    def props(children: List[NodeBuilder], listener: ActorRef) = Props(classOf[RespondingActor], children, listener)
  }

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
    def >>(f: => List[NodeBuilder]) = copy(children = f)

    /**
     * Concatenate to another NodeBuilder
     * @param that
     * @return
     */
    def ::(that: NodeBuilder) = this :: that :: Nil

    /**
     * Create actors in the actor system based on NodeBuilder
     * @param system - actor system for materialisation
     *
     * Note: this is a blocking call - it returns when the hierarchy is completely initialize
     */
    def materialize(implicit system: ActorSystem): Unit = {
      val rootRef = system.actorOf(propByNode(this), path)
      import akka.pattern.ask
      import akka.util.Timeout

import scala.concurrent.duration._

      implicit val timeout = Timeout(5 seconds)

      Await.ready(rootRef ? GetStatus, Duration.Inf)
    }
  }

  implicit def NodeBuilder2ListOfNodeBuilders(value: NodeBuilder): List[NodeBuilder] = List(value)

  implicit class NodeDomain(underlying: String) {
    def replyTo(listener: ActorRef): NodeBuilder = NodeBuilder(path = underlying, Some(listener), implementation = None)

    def withImplementation(implementation: Receive): NodeBuilder = NodeBuilder(path = underlying, listener = None, Some(implementation))

    def >>(that: List[NodeBuilder]) = NodeBuilder(path = underlying, None, children = that)
  }


}
