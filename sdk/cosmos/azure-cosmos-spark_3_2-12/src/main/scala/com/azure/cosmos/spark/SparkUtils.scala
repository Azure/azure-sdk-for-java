// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.CosmosAsyncContainer
import com.azure.cosmos.models.{CosmosQueryRequestOptions, SqlParameter, SqlQuerySpec}
import com.azure.cosmos.spark.diagnostics.ILogger
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode

import java.util.UUID
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

    // TODO @fabianm - uncomment and fix or completely remove this code
    // the version below can cause perf problems whenever there is data available in
    // a container already and when the container does not index all properties
    // because the query below will result in a full table scan (vs. index lookup only)
    /*
    try {

      // this results in a cross partition query with one single query plan request
      // resulting in warming up all caches and connections
      // once container.openConnectionsAndInitCaches() is fixed we can switch back.
      val sqlQuery = new SqlQuerySpec(s"SELECT * FROM r WHERE r['${UUID.randomUUID().toString}'] = @param",
        new SqlParameter("@param", UUID.randomUUID().toString)
      )

      container.queryItems(sqlQuery,
        new CosmosQueryRequestOptions(),
        classOf[ObjectNode])
        .collectList()
        .block()

    } catch {
      case e: Exception => {
        logger("ignoring openConnectionsAndInitCaches failure", e)
      }
    }
    */
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
