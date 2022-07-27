// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark.plugins

import com.azure.cosmos.spark.{CosmosClientMetrics, CosmosConfigNames}
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import org.apache.spark.SparkContext
import org.apache.spark.api.plugin.{DriverPlugin, ExecutorPlugin, PluginContext, SparkPlugin}

import java.util

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

class CosmosMetricsSparkPlugin extends SparkPlugin with BasicLoggingTrait {
  override def driverPlugin(): DriverPlugin = new CosmosSparkMetricsDriverPlugin()

  override def executorPlugin(): ExecutorPlugin = new CosmosSparkMetricsExecutorPlugin()

  private class CosmosSparkMetricsDriverPlugin extends DriverPlugin with BasicLoggingTrait {
    private[this] var slf4jReporterEnabled = true

    override def init
    (
      sc: SparkContext,
      pluginContext: PluginContext
    ): java.util.Map[String, String] = {
      val originalConfig = super.init(sc, pluginContext).asScala

      slf4jReporterEnabled = sc
        .getConf
        .getBoolean(CosmosConfigNames.MetricsEnabledForSlf4j, defaultValue = true)

      logInfo(s"CosmosSparkMetricsDriverPlugin initialized - Console enabled $slf4jReporterEnabled")

      val newConfig = originalConfig + (CosmosConfigNames.MetricsEnabledForSlf4j -> slf4jReporterEnabled.toString)

      newConfig.toMap.asJava
    }

    override def registerMetrics(appId: String, pluginContext: PluginContext): Unit = {
      super.registerMetrics(appId, pluginContext)

      val dropWizardRegistry = pluginContext.metricRegistry()
      if (Option(dropWizardRegistry).isDefined) {
        CosmosClientMetrics.registerDropwizardRegistry(
          pluginContext.executorID(),
          pluginContext.hostname(),
          dropWizardRegistry,
          this.slf4jReporterEnabled)
        logInfo(s"CosmosSparkMetricsDriverPlugin metrics for application $appId registered")
      }
    }

    override def shutdown(): Unit = {
      super.shutdown()
      CosmosClientMetrics.shutdown()
    }
  }

  private class CosmosSparkMetricsExecutorPlugin extends ExecutorPlugin with BasicLoggingTrait {
    private[this] var slf4jReporterEnabled = true

    override def init(ctx: PluginContext, extraConf: util.Map[String, String]): Unit = {
      super.init(ctx, extraConf)

      if (Option(extraConf).isDefined && extraConf.containsKey(CosmosConfigNames.MetricsEnabledForSlf4j)) {
        slf4jReporterEnabled = extraConf.get(CosmosConfigNames.MetricsEnabledForSlf4j).toBoolean
      }

      val dropWizardRegistry = ctx.metricRegistry()
      if (Option(dropWizardRegistry).isDefined) {
        CosmosClientMetrics
          .registerDropwizardRegistry(ctx.executorID(), ctx.hostname(), dropWizardRegistry, slf4jReporterEnabled)

        logInfo(
          s"CosmosSparkMetricsExecutorPlugin metrics registered - Slf4jReporterEnable: $slf4jReporterEnabled")
      }

      logInfo("CosmosSparkMetricsExecutorPlugin initialized")
    }

    override def shutdown(): Unit = {
      super.shutdown()

      logInfo("CosmosSparkMetricsExecutorPlugin shutdown initiated")
    }
  }
}
