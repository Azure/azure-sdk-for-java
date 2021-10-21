// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.CosmosAsyncContainer
import com.azure.cosmos.spark.diagnostics.ILogger
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

private object SparkUtils {

  private val mapper = new ObjectMapper()

  def objectNodeToJson(node: ObjectNode): String = {
    mapper.writeValueAsString(node)
  }

  def daemonThreadFactory(): ThreadFactory = {
    new DaemonThreadFactory()
  }

  def getNumberOfHostCPUCores: Int = {
    Runtime.getRuntime.availableProcessors
  }

  def safeOpenConnectionInitCaches(container: CosmosAsyncContainer, logger: ILogger): Unit = {
    safeOpenConnectionInitCaches(container, (msg, exception) => logger.logWarning(msg, exception))
  }

  def safeOpenConnectionInitCaches(container: CosmosAsyncContainer, logger: (String, Exception) => Unit): Unit = {
    try {
      container.openConnectionsAndInitCaches().block()
    } catch {
      case e: Exception => {
        logger("ignoring openConnectionsAndInitCaches failure", e)
      }
    }
  }

  private object DaemonThreadFactory {
    val poolNumber = new AtomicInteger(1)
  }

  private class DaemonThreadFactory extends ThreadFactory {
    private val threadNumber = new AtomicInteger(1)
    private val namePrefix = "cosmos-spark-daemon-pool-" + DaemonThreadFactory.poolNumber.getAndIncrement + "-thread-"

    override def newThread(r: Runnable): Thread = {
      val t = new Thread(r, namePrefix + threadNumber.getAndIncrement)
      if (!t.isDaemon) t.setDaemon(true)
      if (t.getPriority != Thread.NORM_PRIORITY) t.setPriority(Thread.NORM_PRIORITY)
      t
    }
  }
}
