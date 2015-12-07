package com.github.spytree

import akka.actor._
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.github.spytree.helpers.{GracefulShutdown, DefaultShutdown}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}


class DSLTestSpec extends TestKit(ActorSystem("actorSystem"))
with DefaultTimeout with ImplicitSender
with WordSpecLike with Matchers with BeforeAndAfterAll with DefaultShutdown with GracefulShutdown {

  import com.github.spytree.ActorListenersDSL._

  "The DSL" must {
    "work with simple hierarchies" in {

      val rootRef = ("generatorManager" / {"sipRouter" replyTo self}).materialize

      loan(rootRef){
        val fakeSender: ActorRef = system.actorOf(FakeSenderActor.props("/user/generatorManager/sipRouter", "Ping"))

        fakeSender ! Activate

        expectMsgPF() {
          case Response(path, message) =>
            path.contains("/generatorManager/sipRouter") should be
            message should be("Ping")
        }

      }
    }

    "2 repliers case" in {

      val rootRef = ("root" / {
        "parent" / {
          ("child-1".replyTo(self) / {
            "grand-child-1" replyTo self
          }) ~ ("child-2" replyTo self) ~ ("child-3" replyTo self)
        }
      }).materialize

      loan(rootRef){
        val fakeSender: ActorRef = system.actorOf(FakeSenderActor.props("/user/root/parent/child-1/grand-child-1", "Ping"))

        fakeSender ! Activate

        expectMsgPF() {
          case Response(path, message) =>
            path.contains("/root/parent/grand-child-1") should be
            message should be("Ping")
        }
      }
    }

    "handle custom implementation with state" in {
      import akka.actor.ActorDSL._

      val incrementActor = actor(new Act {
        var counter: Int = 0
        become {
          case "hello" =>
            counter += 1
            sender() ! counter
        }
      })

      val rootRef = ("parent" / {"child" replyTo self proxyTo incrementActor}).materialize

      loan(rootRef){
        system.actorSelection("/user/parent/child") ! "hello"

        val message = expectMsgClass(classOf[Response[String]])

        message.message shouldBe "hello"

        val message2 = expectMsgClass(classOf[Int])
      }
    }

    "handle custom implementation" in {


      val tree = "parent" / {
        "child" replyTo self withImplementation {
          case message: Any =>
            self ! "PONG"
        }
      }
      tree.materialize

      val fakeSender = system.actorOf(FakeSenderActor.props("/user/parent/child", "Ping"))

      fakeSender ! Activate

      var gotSpyReply: Boolean = false
      var gotCustomReply: Boolean = false

      expectMsgPF() {
        case Response(path, message) =>
          path.contains("/parent/child") should be
          message should be("Ping")
          gotSpyReply = true
        case "PONG" =>
          gotCustomReply = true
      }

      expectMsgPF() {
        case Response(path, message) =>
          path.contains("/parent/child") should be
          message should be("Ping")
          gotSpyReply = true
        case "PONG" =>
          gotCustomReply = true
      }

      gotCustomReply shouldBe true
      gotSpyReply shouldBe true
    }

  }

}
