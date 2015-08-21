package com.github.spytree

import akka.actor._
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}


class DSLTestSpec extends TestKit(ActorSystem("actorSystem"))
with DefaultTimeout with ImplicitSender
with WordSpecLike with Matchers with BeforeAndAfterAll with DefaultShutdown {


  "The DSL" must {
    "work with simple hierarchies" in {

      import com.github.spytree.ActorListenersDSL._

      ("generatorManager" >> {"sipRouter" replyTo self}).materialize

      val fakeSender: ActorRef = system.actorOf(FakeSenderActor.props("/user/generatorManager/sipRouter", "Ping"))

      fakeSender ! Activate

      expectMsgPF() {
        case Response(path, message) =>
          path.contains("/generatorManager/sipRouter") should be
          message should be("Ping")
      }

    }

    "2 repliers case" in {

      import com.github.spytree.ActorListenersDSL._

      ("root" >> {
        "parent" >> {
          ("child-1".replyTo(self) >> {"grand-child-1" replyTo self}) ::
          ("child-2" replyTo self)
        }
      }).materialize


      val fakeSender: ActorRef = system.actorOf(FakeSenderActor.props("/user/root/parent/child-1/grand-child-1", "Ping"))

      fakeSender ! Activate

      expectMsgPF() {
        case Response(path, message) =>
          path.contains("/root/parent/grand-child-1") should be
          message should be("Ping")
      }

    }

    "handle custom implementation" in {
      import com.github.spytree.ActorListenersDSL._

      val tree = "parent" >> {
        "child" replyTo self withImplementation {
          case message: Any =>
            self ! "PONG"
        }
      }
      tree.materialize

      val fakeSender = system.actorOf(FakeSenderActor.props("/user/parent/child", "Ping"))

      fakeSender ! Activate

      var gotSpyReply:Boolean = false
      var gotCustomReply:Boolean = false

      expectMsgPF() {
        case Response(path, message) =>
          path.contains("/parent/child") should be
          message should be("Ping")
          gotSpyReply = true
        case "PONG" =>
          println("PONG")
          gotCustomReply = true
      }

      expectMsgPF() {
        case Response(path, message) =>
          path.contains("/parent/child") should be
          message should be("Ping")
          gotSpyReply = true
        case "PONG" =>
          println("PONG")
          gotCustomReply = true
      }

      gotCustomReply shouldBe true
      gotSpyReply shouldBe true


    }

  }

}
