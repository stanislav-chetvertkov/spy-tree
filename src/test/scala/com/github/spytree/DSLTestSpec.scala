package com.github.spytree

import akka.actor._
import akka.testkit._
import com.github.spytree.helpers.{GracefulShutdown, DefaultShutdown}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._

class DSLTestSpec extends TestKit(ActorSystem("actorSystem"))
with DefaultTimeout with ImplicitSender
with WordSpecLike with Matchers with BeforeAndAfterAll with DefaultShutdown with GracefulShutdown {

  import com.github.spytree.ActorListenersDSL._

  "The DSL" must {
    "work with simple hierarchies" in {
      val rootRef = ("generatorManager" / {
        "sipRouter" replyTo self
      }).materialize

      loan(rootRef) {
        val fakeSender: ActorRef = system.actorOf(FakeSenderActor.props("/user/generatorManager/sipRouter", "Ping"))

        fakeSender ! Activate

        val response = this.expectResponse[String]
        response.path.contains("/generatorManager/sipRouter") should be
        response.message should be("Ping")
      }
    }

    "2 repliers case" in {
      import scala.concurrent.duration._

      val grandChildListener = TestProbe()
      expectNoMsg(2.second) // allow the probe to initialize

      val rootRef = ("root" / {
        "parent" / {
          ("child-1".replyTo(self) / {
            "grand-child-1" replyTo grandChildListener.ref
          }) ~ ("child-2" replyTo self) ~ ("child-3" replyTo self)
        }
      }).materialize

      loan(rootRef) {
        val fakeSender: ActorRef = system.actorOf(FakeSenderActor.props("/user/root/parent/child-1/grand-child-1", "Ping"))
        fakeSender ! Activate

        grandChildListener.expectResponse[String]("/root/parent/child-1/grand-child-1").get.message shouldBe "Ping"
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

    val rootRef = ("parent" / {
      "child" replyTo self proxyTo incrementActor
    }).materialize

    loan(rootRef) {
      system.actorSelection("/user/parent/child") ! "hello"

      val message = this.expectResponse[String]

      message.message shouldBe "hello"

      val message2 = expectMsgClass(classOf[Int])
    }
  }

  "handle custom implementation" in {

    val listener = TestProbe()
    expectNoMsg(1.second)

    val tree = ("parent" / {
      "child" replyTo listener.ref withImplementation {
        case message: Any =>
          self ! "PONG"
      }
    }).materialize

    val fakeSender = system.actorOf(FakeSenderActor.props("/user/parent/child", "Ping"))

    fakeSender ! Activate

    listener.expectResponse[String].message shouldBe "Ping"
    expectMsg("PONG")
  }


}
