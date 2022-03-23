// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark.diagnostics

import com.azure.cosmos.spark.DiagnosticsConfig

private[spark] object LoggerHelper {
  def getLogger(diagnosticsConfig: DiagnosticsConfig, classType: Class[_]): ILogger = {
    DiagnosticsLoader.getDiagnosticsProvider(diagnosticsConfig).getLogger(classType)
  }
}
