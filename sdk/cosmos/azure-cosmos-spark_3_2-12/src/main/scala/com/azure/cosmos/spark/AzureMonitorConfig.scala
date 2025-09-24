// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import java.util.concurrent.atomic.AtomicReference

private[spark] case class AzureMonitorConfig
(
  enabled: Boolean,
  connectionString: String,
  authEnabled: Boolean,
  authConfig: Option[CosmosAuthConfig],
  liveMetricsEnabled: Boolean,
  samplingRate: Float,
  samplingRateMaxCount: Int,
  samplingRateIntervalInSeconds: Int,
  metricCollectionIntervalInSeconds: Int
)

private[spark] object AzureMonitorConfig {
  private[this] val configHolder = new AtomicReference[Option[AzureMonitorConfig]](None)

  private[spark] def resetForUsageInTest() : Unit = {
    configHolder.set(None)
  }

  private[spark] def validateConfigUniqueness(azureMonitorConfig: AzureMonitorConfig) : Unit = {
    val configCandidate = Option(azureMonitorConfig)

    if (configCandidate.isDefined) {
      if (!configHolder.compareAndSet(None, configCandidate)
        && (configHolder.get() != configCandidate)) {

        throw new IllegalStateException(
          "The Azure Monitor configuration in 'spark.cosmos.diagnostics.azureMonitor.*' "
        + "must only result in at most one unique non-empty set per JVM process. If you want to change the "
        + "configuration, please restart you cluster first.")
      }
    }
  }
}
