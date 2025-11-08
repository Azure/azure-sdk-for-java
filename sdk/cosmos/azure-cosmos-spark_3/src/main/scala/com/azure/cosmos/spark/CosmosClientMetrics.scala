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

  def removeMeterRegistry
  (
    toBeRemovedMeterRegistry: MeterRegistry
  ) : Unit = {
    val shouldShutdown = this.meterRegistry match {
      case Some(existingRegistry) =>
        if (existingRegistry.remove(toBeRemovedMeterRegistry).getRegistries().size() == 0) {
          true
        } else {
          false
        }
      case None => true
    }

    if (shouldShutdown) {
      this.shutdown()
    }
  }

  def registerDropwizardRegistry
  (
    executorId: String,
    dropwizardMetricRegistry: MetricRegistry,
    slf4jReporterEnabled: Boolean,
    metricsCollectionIntervalInSeconds: Integer
  ) : Option[MeterRegistry] = {

    if (Option(dropwizardMetricRegistry).isDefined) {
      CosmosClientMetrics.executorId = Some(executorId)

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

          override protected def close(): Unit = {
            super.close()

            slf4JReporter match {
              case Some(slf4JReporterSnapshot) =>
                slf4JReporterSnapshot.stop()
                slf4JReporterSnapshot.close()
              case None =>
            }
          }
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

      Some(dropWizardMeterRegistry)
    } else {
      None
    }
  }

  private[this] def shutdown(): Unit = {

    val slf4jReporterSnapshot = this.slf4JReporter
    this.slf4JReporter = None

    val meterRegistrySnapshot = this.meterRegistry
    this.meterRegistry = None

    if (slf4jReporterSnapshot.isDefined) {
      slf4jReporterSnapshot.get.stop()
    }

    if (meterRegistrySnapshot.isDefined) {
      meterRegistrySnapshot.get.clear
      meterRegistrySnapshot.get.close()
    }
  }
}
