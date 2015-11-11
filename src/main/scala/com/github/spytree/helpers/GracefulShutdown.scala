package com.github.spytree.helpers

import akka.actor.{PoisonPill, Terminated, ActorRef}
import akka.testkit.TestKit

/**
  * Allows to shutdown actor gracefully
  */
trait GracefulShutdown {
  this:TestKit =>

  /**
    * Kill an actor, make sure it was done successfully
    * @param ref - actorRef to kill
    * @return
    */
  def shutdownGracefully(ref:ActorRef):Terminated = {
    watch(ref)
    ref ! PoisonPill
    expectTerminated(ref)
  }
}