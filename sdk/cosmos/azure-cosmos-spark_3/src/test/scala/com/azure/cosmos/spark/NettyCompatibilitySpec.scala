// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdLoopNIO
import io.netty.channel.MultiThreadIoEventLoopGroup
import io.netty.util.Version
import io.netty.util.concurrent.DefaultThreadFactory

import java.util.concurrent.{Callable, TimeUnit}
import scala.collection.JavaConverters._

class NettyCompatibilitySpec extends UnitSpec {
  private val timeoutInSeconds = 10

  it should "resolve a consistent Netty 4.2 dependency stack" in {
    val versions = Version.identify().values().asScala
      .filterNot(_.artifactId().startsWith("netty-tcnative"))
      .map(_.artifactVersion()).toSeq
    versions should not be empty
    all(versions) should startWith("4.2.")
  }

  it should "create NIO event loops for the Cosmos transport" in {
    val threadName = "cosmos-spark-netty-test"
    val eventLoops = new RntbdLoopNIO().newEventLoopGroup(1, new DefaultThreadFactory(threadName))
    try {
      eventLoops shouldBe a[MultiThreadIoEventLoopGroup]
      eventLoops.next().submit(new Callable[String] {
        override def call(): String = Thread.currentThread().getName
      }).get(timeoutInSeconds, TimeUnit.SECONDS) should startWith(threadName)
    } finally {
      eventLoops.shutdownGracefully(0, timeoutInSeconds, TimeUnit.SECONDS).sync()
    }
  }
}
