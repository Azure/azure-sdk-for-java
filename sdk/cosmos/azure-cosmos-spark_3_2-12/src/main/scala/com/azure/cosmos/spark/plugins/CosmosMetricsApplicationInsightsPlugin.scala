// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark.plugins

import com.azure.cosmos.implementation.Strings
import com.azure.cosmos.spark.{CosmosClientMetrics, CosmosConfigNames, CosmosConstants}
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.microsoft.applicationinsights.TelemetryConfiguration
import io.micrometer.azuremonitor.{AzureMonitorConfig, AzureMonitorMeterRegistry}
import io.micrometer.core.instrument.{Clock, MeterRegistry}
import org.apache.spark.SparkContext
import org.apache.spark.api.plugin.{DriverPlugin, ExecutorPlugin, PluginContext, SparkPlugin}

import java.time.Duration
import java.util

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

class CosmosMetricsApplicationInsightsPlugin extends SparkPlugin with BasicLoggingTrait {
  override def driverPlugin(): DriverPlugin = new CosmosMetricsApplicationInsightsDriverPlugin()

  override def executorPlugin(): ExecutorPlugin = new CosmosMetricsApplicationInsightsExecutorPlugin()

  private[this] def createAndAddRegistry(connectionString: String, metricsCollectionIntervalInSeconds: Long): MeterRegistry = {
    val config = new DefaultAzureMonitorConfig(metricsCollectionIntervalInSeconds)

    val telemetryConfig = TelemetryConfiguration
      .createDefault()
    telemetryConfig.setConnectionString(connectionString)

    val azureMonitorRegistry = AzureMonitorMeterRegistry
      .builder(config)
      .clock(Clock.SYSTEM)
      .telemetryConfiguration(telemetryConfig)
      .build()

    CosmosClientMetrics.addMeterRegistry(azureMonitorRegistry)

    azureMonitorRegistry
  }

  private[this] def cleanupMeterRegistry(meterRegistry: Option[MeterRegistry]): Unit = {
    meterRegistry match {
      case Some(existingRegistry) => CosmosClientMetrics.removeMeterRegistry(existingRegistry)
      case None =>
    }
  }

  private class CosmosMetricsApplicationInsightsDriverPlugin
    extends DriverPlugin
      with BasicLoggingTrait {

    private[this] var connectionString: String = _
    private[this] var metricsCollectionIntervalInSeconds = CosmosConstants.defaultMetricsIntervalInSeconds
    private[this] var meterRegistry: Option[MeterRegistry] = None

    override def init
    (
      sc: SparkContext,
      pluginContext: PluginContext
    ): java.util.Map[String, String] = {
      val originalConfig = super.init(sc, pluginContext).asScala

      metricsCollectionIntervalInSeconds = sc
        .getConf
        .getInt(CosmosConfigNames.MetricsIntervalInSeconds, CosmosConstants.defaultMetricsIntervalInSeconds)

      connectionString = sc
        .getConf
        .get(CosmosConfigNames.MetricsAzureMonitorConnectionString, null)

      if (Strings.isNullOrWhiteSpace(connectionString)) {
        throw new IllegalArgumentException(
          s"Azure monitor instrumentation key provided in '" +
            s"${CosmosConfigNames.MetricsAzureMonitorConnectionString}' is null or only " +
            s"contains whitespaces.'"
        )
      }

      logInfo(s"CosmosMetricsApplicationInsightsDriverPlugin initialized - metrics interval " +
        s"(seconds): $metricsCollectionIntervalInSeconds)")

      val newConfig = originalConfig +
        (CosmosConfigNames.MetricsAzureMonitorConnectionString -> connectionString) +
        (CosmosConfigNames.MetricsIntervalInSeconds -> metricsCollectionIntervalInSeconds.toString)

      newConfig.toMap.asJava
    }

    override def registerMetrics(appId: String, pluginContext: PluginContext): Unit = {
      super.registerMetrics(appId, pluginContext)

      this.meterRegistry = Some(createAndAddRegistry(connectionString, metricsCollectionIntervalInSeconds))

      logInfo(s"CosmosMetricsApplicationInsightsDriverPlugin metrics for application $appId " +
        s"registered (metrics interval (seconds): $metricsCollectionIntervalInSeconds)")
    }

    override def shutdown(): Unit = {
      super.shutdown()

      cleanupMeterRegistry(this.meterRegistry)

      logInfo("CosmosMetricsApplicationInsightsDriverPlugin shutdown initiated")
    }
  }

  private class CosmosMetricsApplicationInsightsExecutorPlugin
    extends ExecutorPlugin
      with BasicLoggingTrait {

    private[this] var connectionString: String = _
    private[this] var metricsCollectionIntervalInSeconds = CosmosConstants.defaultMetricsIntervalInSeconds
    private[this] var meterRegistry: Option[MeterRegistry] = None

    override def init(ctx: PluginContext, extraConf: util.Map[String, String]): Unit = {
      super.init(ctx, extraConf)

      if (Option(extraConf).isDefined &&
        extraConf.containsKey(CosmosConfigNames.MetricsAzureMonitorConnectionString)) {

        connectionString = extraConf.get(CosmosConfigNames.MetricsAzureMonitorConnectionString)
      }

      if (Option(extraConf).isDefined &&
        extraConf.containsKey(CosmosConfigNames.MetricsIntervalInSeconds)) {

        metricsCollectionIntervalInSeconds = extraConf.get(CosmosConfigNames.MetricsIntervalInSeconds).toInt
      }

      this.meterRegistry = Some(
        createAndAddRegistry(connectionString, metricsCollectionIntervalInSeconds))

      logInfo(s"CosmosMetricsApplicationInsightsExecutorPlugin metrics " +
        s"registered (metrics interval (seconds): $metricsCollectionIntervalInSeconds)")
    }

    override def shutdown(): Unit = {
      super.shutdown()

      cleanupMeterRegistry(this.meterRegistry)
      logInfo("CosmosMetricsApplicationInsightsExecutorPlugin shutdown initiated")
    }
  }

  private class DefaultAzureMonitorConfig
  (
    val intervalInSeconds: Long
  ) extends AzureMonitorConfig {

    override def get(s: String): String = {
      null
    }

    override def step(): Duration = {
      Duration.ofSeconds(intervalInSeconds)
    }

    override def instrumentationKey(): String = {
      null
    }

    override def enabled(): Boolean = {
      true
    }
  }
}
