// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.CosmosDiagnosticsContext

private[spark] trait OutputMetricsPublisherTrait {
  def trackWriteOperation(recordCount: Long, diagnostics: Option[CosmosDiagnosticsContext]): Unit
}


