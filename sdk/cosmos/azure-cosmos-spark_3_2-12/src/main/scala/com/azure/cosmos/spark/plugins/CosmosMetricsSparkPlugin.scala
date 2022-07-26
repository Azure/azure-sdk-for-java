// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark.plugins

import com.azure.cosmos.spark.CosmosClientMetrics
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import org.apache.spark.SparkContext
import org.apache.spark.api.plugin.{DriverPlugin, ExecutorPlugin, PluginContext, SparkPlugin}

import java.util

class CosmosSparkMetrics extends SparkPlugin with BasicLoggingTrait {
  override def driverPlugin(): DriverPlugin = new CosmosSparkMetricsDriverPlugin()

  override def executorPlugin(): ExecutorPlugin = new CosmosSparkMetricsExecutorPlugin()

  private class CosmosSparkMetricsDriverPlugin extends DriverPlugin with BasicLoggingTrait {
    override def init
    (
      sc: SparkContext,
      pluginContext: PluginContext
    ): java.util.Map[String, String] = {
      val result = super.init(sc, pluginContext)

      logInfo("CosmosSparkMetricsDriverPlugin initialized")

      result
    }

    override def registerMetrics(appId: String, pluginContext: PluginContext): Unit = {
      super.registerMetrics(appId, pluginContext)

      val dropWizardRegistry = pluginContext.metricRegistry()
      if (Option(dropWizardRegistry).isDefined) {
        CosmosClientMetrics.registerDropwizardRegistry(
          pluginContext.executorID(),
          pluginContext.hostname(),
          dropWizardRegistry)
        logInfo(s"CosmosSparkMetricsDriverPlugin metrics for application $appId registered")
      }
    }

    override def shutdown(): Unit = {
      super.shutdown()
      CosmosClientMetrics.shutdown()
    }
  }

  private class CosmosSparkMetricsExecutorPlugin extends ExecutorPlugin with BasicLoggingTrait {
    override def init(ctx: PluginContext, extraConf: util.Map[String, String]): Unit = {
      super.init(ctx, extraConf)

      val dropWizardRegistry = ctx.metricRegistry()
      if (Option(dropWizardRegistry).isDefined) {
        CosmosClientMetrics.register(ctx.executorID(), ctx.hostname(), dropWizardRegistry)

        logInfo("CosmosSparkMetricsExecutorPlugin metrics registered")
      }

      logInfo("CosmosSparkMetricsExecutorPlugin initialized")
    }

    override def shutdown(): Unit = {
      super.shutdown()

      logInfo("CosmosSparkMetricsExecutorPlugin shutdown initiated")
    }
  }
}
