package com.github.spytree

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
