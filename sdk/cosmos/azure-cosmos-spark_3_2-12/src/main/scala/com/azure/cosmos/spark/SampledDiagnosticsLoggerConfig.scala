// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

private[spark] case class SampledDiagnosticsLoggerConfig
(
  samplingRateMaxCount: Int,
  samplingRateIntervalInSeconds: Int,
  thresholdsPointOperationLatencyInMs: Int,
  thresholdsNonPointOperationLatencyInMs: Int,
  thresholdsRequestCharge: Int,
)
