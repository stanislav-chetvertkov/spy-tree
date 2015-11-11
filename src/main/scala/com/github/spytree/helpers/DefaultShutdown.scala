package com.github.spytree.helpers

import akka.testkit.TestKit
import org.scalatest.BeforeAndAfterAll

/**
  * Adding shutdown implementation for TestKit
  */
trait DefaultShutdown {
  this: TestKit with BeforeAndAfterAll =>

  override def afterAll() {
    shutdown()
  }
}
