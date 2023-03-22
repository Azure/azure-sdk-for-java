// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.scheduler.{SparkListener, SparkListenerExecutorAdded, SparkListenerExecutorRemoved}
import org.apache.spark.sql.SparkSession

private[spark] case class CosmosSparkExecutorCountListener()
    extends SparkListener
        with BasicLoggingTrait {

    private var executorCountBroadcast: Broadcast[Integer] = initialize()

    def getExecutorCount(): Integer = {
        this.executorCountBroadcast.value
    }

    override def onExecutorAdded(executorAdded: SparkListenerExecutorAdded): Unit = {
        val sparkSession = SparkSession.active

        // getExecutorInfo will return information for both driver and executor
        // and what we really want is the executor count, so -1
        val currentExecutorCount = sparkSession.sparkContext.statusTracker.getExecutorInfos.size - 1

        logInfo(s"Executor is added. Before [${executorCountBroadcast.value}], after [$currentExecutorCount]")
        executorCountBroadcast.unpersist()
        executorCountBroadcast = sparkSession.sparkContext.broadcast(currentExecutorCount)
    }


    override def onExecutorRemoved(executorRemoved: SparkListenerExecutorRemoved): Unit = {
        val sparkSession = SparkSession.active
        val currentExecutorCount = sparkSession.sparkContext.statusTracker.getExecutorInfos.size - 1

        logInfo(s"Executor is removed. Before [${executorCountBroadcast.value}], after [$currentExecutorCount]")
        executorCountBroadcast.unpersist()
        executorCountBroadcast = sparkSession.sparkContext.broadcast(currentExecutorCount)
    }

    private[spark] def initialize(): Broadcast[Integer] = {
        val sparkSession = SparkSession.active
        val currentExecutorCount = sparkSession.sparkContext.statusTracker.getExecutorInfos.size - 1
        logInfo(s"Initialize ExecutorCountBroadcastWrapper. Current executor count [$currentExecutorCount]")

        sparkSession.sparkContext.broadcast(currentExecutorCount)
    }
}
