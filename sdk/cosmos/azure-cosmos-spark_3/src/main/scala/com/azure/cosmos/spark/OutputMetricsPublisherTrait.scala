// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.CosmosDiagnosticsContext

private[spark] trait OutputMetricsPublisherTrait {
  def trackWriteOperation(recordCount: Long, diagnostics: Option[CosmosDiagnosticsContext]): Unit

  /**
   * Tracks an operation the write strategy intentionally treated as a no-op (for example a patch whose
   * filter predicate did not match, or a not-found delete). RU/byte accounting is identical to
   * [[trackWriteOperation]]; the records are counted as skipped rather than written.
   */
  def trackSkippedOperation(recordCount: Long, diagnostics: Option[CosmosDiagnosticsContext]): Unit
}


