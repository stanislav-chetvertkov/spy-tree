package com.github.spytree

import akka.actor._
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}


class FakeSenderActor(selection:String) extends Actor with ActorLogging {
  override def receive: Receive = {
    case "ping" =>
      log.info("pinging")
      context.system.actorSelection(selection) ! "Ping"
  }
}

class DSLTestSpec extends TestKit(ActorSystem("actorSystem"))
with DefaultTimeout with ImplicitSender
with WordSpecLike with Matchers with BeforeAndAfterAll with DefaultShutdown {


  "The DSL" must {
    "be awesome" in {

      import com.github.spytree.ActorListenersDSL._

      ("generatorManager" >> {"sipRouter" replyTo self}).materialize

      val fakeSender: ActorRef = system.actorOf(Props(classOf[FakeSenderActor],"/user/generatorManager/sipRouter"))

      fakeSender ! "ping"

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

      val fakeSender: ActorRef = system.actorOf(Props(classOf[FakeSenderActor],"/user/root/parent/child-1/grand-child-1"))

      fakeSender ! "ping"

      expectMsgPF() {
        case Response(path, message) =>
          path.contains("/root/parent/grand-child-1") should be
          message should be("Ping")
      }

    }

    "handle custom implementation" in {
      //TODO
    }

  }

}
