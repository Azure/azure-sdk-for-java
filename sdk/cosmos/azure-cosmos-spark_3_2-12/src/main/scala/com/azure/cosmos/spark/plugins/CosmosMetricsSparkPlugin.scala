// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark.plugins

import com.azure.cosmos.spark.{CosmosClientMetrics, CosmosConfigNames, CosmosConstants}
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.codahale.metrics.MetricRegistry
import io.micrometer.core.instrument.MeterRegistry
import org.apache.spark.SparkContext
import org.apache.spark.api.plugin.{DriverPlugin, ExecutorPlugin, PluginContext, SparkPlugin}

import java.util

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

class CosmosMetricsSparkPlugin extends SparkPlugin with BasicLoggingTrait {
  override def driverPlugin(): DriverPlugin = new CosmosMetricsSparkDriverPlugin()

  override def executorPlugin(): ExecutorPlugin = new CosmosMetricsSparkExecutorPlugin()

  private[this] def cleanupMeterRegistry(meterRegistry: Option[MeterRegistry]): Unit = {
    meterRegistry match {
      case Some(existingRegistry) => CosmosClientMetrics.removeMeterRegistry(existingRegistry)
      case None =>
    }
  }

  private class CosmosMetricsSparkDriverPlugin extends DriverPlugin with BasicLoggingTrait {
    private[this] var slf4jReporterEnabled = CosmosConstants.defaultSlf4jMetricReporterEnabled
    private[this] var metricsCollectionIntervalInSeconds = CosmosConstants.defaultMetricsIntervalInSeconds
    private[this] var meterRegistry: Option[MeterRegistry] = None

    override def init
    (
      sc: SparkContext,
      pluginContext: PluginContext
    ): java.util.Map[String, String] = {
      val originalConfig = super.init(sc, pluginContext).asScala

      slf4jReporterEnabled = sc
        .getConf
        .getBoolean(CosmosConfigNames.MetricsEnabledForSlf4j, CosmosConstants.defaultSlf4jMetricReporterEnabled)

      metricsCollectionIntervalInSeconds = sc
        .getConf
        .getInt(CosmosConfigNames.MetricsIntervalInSeconds, CosmosConstants.defaultMetricsIntervalInSeconds)

      logInfo(s"CosmosMetricsSparkDriverPlugin initialized (Slf4JReporter " +
        s"enabled: $slf4jReporterEnabled, metrics interval (seconds): $metricsCollectionIntervalInSeconds)")

      val newConfig = originalConfig +
        (CosmosConfigNames.MetricsEnabledForSlf4j -> slf4jReporterEnabled.toString) +
        (CosmosConfigNames.MetricsIntervalInSeconds -> metricsCollectionIntervalInSeconds.toString)

      newConfig.toMap.asJava
    }

    override def registerMetrics(appId: String, pluginContext: PluginContext): Unit = {
      super.registerMetrics(appId, pluginContext)

      val dropWizardRegistry: MetricRegistry = pluginContext.metricRegistry()
      if (Option(dropWizardRegistry).isDefined) {
        this.meterRegistry = CosmosClientMetrics.registerDropwizardRegistry(
          pluginContext.executorID(),
          dropWizardRegistry,
          this.slf4jReporterEnabled,
          this.metricsCollectionIntervalInSeconds)
        logInfo(s"CosmosMetricsSparkDriverPlugin metrics for application $appId registered (Slf4JReporter " +
          s"enabled: $slf4jReporterEnabled, metrics interval (seconds): $metricsCollectionIntervalInSeconds)")
      }
    }

    override def shutdown(): Unit = {
      super.shutdown()

      cleanupMeterRegistry(this.meterRegistry)

      logInfo("CosmosMetricsSparkDriverPlugin shutdown initiated")
    }
  }

  private class CosmosMetricsSparkExecutorPlugin extends ExecutorPlugin with BasicLoggingTrait {
    private[this] var slf4jReporterEnabled = CosmosConstants.defaultSlf4jMetricReporterEnabled
    private[this] var metricsCollectionIntervalInSeconds = CosmosConstants.defaultMetricsIntervalInSeconds
    private[this] var meterRegistry: Option[MeterRegistry] = None
    override def init(ctx: PluginContext, extraConf: util.Map[String, String]): Unit = {
      super.init(ctx, extraConf)

      if (Option(extraConf).isDefined && extraConf.containsKey(CosmosConfigNames.MetricsEnabledForSlf4j)) {
        slf4jReporterEnabled = extraConf.get(CosmosConfigNames.MetricsEnabledForSlf4j).toBoolean
      }

      if (Option(extraConf).isDefined && extraConf.containsKey(CosmosConfigNames.MetricsIntervalInSeconds)) {
        metricsCollectionIntervalInSeconds = extraConf.get(CosmosConfigNames.MetricsIntervalInSeconds).toInt
      }

      val dropWizardRegistry = ctx.metricRegistry()
      if (Option(dropWizardRegistry).isDefined) {
        this.meterRegistry = CosmosClientMetrics
          .registerDropwizardRegistry(
            ctx.executorID(),
            dropWizardRegistry,
            slf4jReporterEnabled,
            metricsCollectionIntervalInSeconds)

        logInfo(
          s"CosmosMetricsSparkExecutorPlugin metrics registered - (Slf4JReporter " +
            s"enabled: $slf4jReporterEnabled, metrics interval (seconds): $metricsCollectionIntervalInSeconds)")
      }

      logInfo("CosmosMetricsSparkExecutorPlugin initialized")
    }

    override def shutdown(): Unit = {
      super.shutdown()

      cleanupMeterRegistry(this.meterRegistry)

      logInfo("CosmosMetricsSparkExecutorPlugin shutdown initiated")
    }
  }
}
