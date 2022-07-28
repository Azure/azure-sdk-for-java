// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.codahale.metrics.{MetricRegistry, Slf4jReporter}
import io.micrometer.core.instrument.composite.CompositeMeterRegistry
import io.micrometer.core.instrument.config.NamingConvention
import io.micrometer.core.instrument.{Clock, MeterRegistry}
import io.micrometer.core.instrument.dropwizard.{DropwizardConfig, DropwizardMeterRegistry}
import io.micrometer.core.instrument.util.HierarchicalNameMapper

import java.util.concurrent.TimeUnit

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

private[spark] object CosmosClientMetrics extends BasicLoggingTrait {
  var meterRegistry: Option[CompositeMeterRegistry] = None
  var executorId: Option[String] = None
  var hostName: Option[String] = None
  var slf4JReporter : Option[Slf4jReporter] = None

  def addMeterRegistry
  (
    newMeterRegistry: MeterRegistry
  ) : Unit = {
    this.meterRegistry match {
      case Some(existingRegistry) => existingRegistry.add(newMeterRegistry)
      case None =>
        this.meterRegistry = Some(new CompositeMeterRegistry(
          Clock.SYSTEM,
          Iterable.apply(newMeterRegistry).asJava))

    }
  }
  def registerDropwizardRegistry
  (
    executorId: String,
    hostname: String,
    dropwizardMetricRegistry: MetricRegistry,
    slf4jReporterEnabled: Boolean,
    metricsCollectionIntervalInSeconds: Integer
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

      dropWizardMeterRegistry.config().namingConvention(NamingConvention.dot)

      if (slf4jReporterEnabled) {
        val reporter = Slf4jReporter
          .forRegistry(dropwizardMetricRegistry)
          .convertRatesTo(TimeUnit.SECONDS)
          .convertDurationsTo(TimeUnit.MILLISECONDS)
          .build
        slf4JReporter = Some(reporter)
        reporter.start(metricsCollectionIntervalInSeconds.toLong, TimeUnit.SECONDS)
      }

      addMeterRegistry(dropWizardMeterRegistry)
    }
  }

  def shutdown(): Unit = {
    if (slf4JReporter.isDefined) {
      slf4JReporter.get.stop()
    }
  }
}
