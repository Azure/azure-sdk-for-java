// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.spark.CosmosClientMetrics.{CosmosClientMetricsDriverPlugin, CosmosClientMetricsExecutorPlugin}
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.codahale.metrics.{MetricRegistry, Slf4jReporter}
import io.micrometer.core.instrument.composite.CompositeMeterRegistry
import io.micrometer.core.instrument.config.NamingConvention
import io.micrometer.core.instrument.{Clock, MeterRegistry}
import io.micrometer.core.instrument.dropwizard.{DropwizardConfig, DropwizardMeterRegistry}
import io.micrometer.core.instrument.util.HierarchicalNameMapper
import org.apache.spark.SparkContext
import org.apache.spark.api.plugin.{DriverPlugin, ExecutorPlugin, PluginContext, SparkPlugin}

import java.util
import java.util.concurrent.TimeUnit

class CosmosClientMetrics extends SparkPlugin with BasicLoggingTrait {
  override def driverPlugin(): DriverPlugin = new CosmosClientMetricsDriverPlugin()

  override def executorPlugin(): ExecutorPlugin = new CosmosClientMetricsExecutorPlugin()
}

private[spark] object CosmosClientMetrics extends BasicLoggingTrait {
  var meterRegistry : Option[MeterRegistry] = None
  var executorId: Option[String] = None;
  var hostName: Option[String] = None;
  var slf4JReporter : Option[Slf4jReporter] = None

  def register
  (
    executorId: String,
    hostname: String,
    dropwizardMetricRegistry: MetricRegistry
  ) : Unit = {

    if (Option(dropwizardMetricRegistry).isDefined) {
      CosmosClientMetrics.executorId = Some(executorId)
      CosmosClientMetrics.hostName = Some(hostname)

      val dropwizardConfig = new DropwizardConfig() {
        override def get(key: String): String = null
        override def prefix = "cosmos"
      }

      val dropWizardMeterRegistry =
        new DropwizardMeterRegistry(
          dropwizardConfig,
          dropwizardMetricRegistry,
          HierarchicalNameMapper.DEFAULT,
          Clock.SYSTEM) {

          override protected def nullGaugeValue: java.lang.Double = Double.NaN
        }

      dropWizardMeterRegistry.config().namingConvention(NamingConvention.dot);

      val compositeMeterRegistry = new CompositeMeterRegistry(Clock.SYSTEM)
      compositeMeterRegistry.add(dropWizardMeterRegistry)

      val reporter = Slf4jReporter
        .forRegistry(dropwizardMetricRegistry)
        .convertRatesTo(TimeUnit.SECONDS)
        .convertDurationsTo(TimeUnit.MILLISECONDS)
        .build
      slf4JReporter = Some(reporter)
      reporter.start(1, TimeUnit.SECONDS)

      CosmosClientMetrics.meterRegistry = Some(compositeMeterRegistry)
    }
  }

  private class CosmosClientMetricsDriverPlugin extends DriverPlugin with BasicLoggingTrait {
    override def init
    (
      sc: SparkContext,
      pluginContext: PluginContext
    ): java.util.Map[String, String] = {
      val result = super.init(sc, pluginContext)

      logInfo("CosmosClientMetricsDriverPlugin initialized")

      result
    }

    override def registerMetrics (appId: String, pluginContext: PluginContext) : Unit = {
      super.registerMetrics(appId, pluginContext)

      val dropWizardRegistry = pluginContext.metricRegistry()
      if (Option(dropWizardRegistry).isDefined) {
        CosmosClientMetrics.register(pluginContext.executorID(), pluginContext.hostname(), dropWizardRegistry)
        logInfo(s"CosmosClientMetricsDriverPlugin metrics for application $appId registered")
      }
    }

    override def shutdown(): Unit = {
      super.shutdown()
      slf4JReporter match {
        case Some(reporter) => reporter.stop()
        case None =>
      }
      logInfo("CosmosClientMetricsDriverPlugin shutdown initiated")
    }
  }

  private class CosmosClientMetricsExecutorPlugin extends ExecutorPlugin with BasicLoggingTrait {
    override def init(ctx: PluginContext, extraConf: util.Map[String, String]): Unit = {
      super.init(ctx, extraConf)

      val dropWizardRegistry = ctx.metricRegistry()
      if (Option(dropWizardRegistry).isDefined) {
        CosmosClientMetrics.register(ctx.executorID(), ctx.hostname(), dropWizardRegistry)

        logInfo("CosmosClientMetricsExecutorPlugin metrics registered")
      }

      logInfo("CosmosClientMetricsExecutorPlugin initialized")
    }

    override def shutdown(): Unit = {
      super.shutdown()

      logInfo("CosmosClientMetricsExecutorPlugin shutdown initiated")
    }
  }
}
