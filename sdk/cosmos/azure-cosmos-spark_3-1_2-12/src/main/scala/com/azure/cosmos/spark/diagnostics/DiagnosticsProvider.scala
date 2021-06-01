// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark.diagnostics

private[spark] trait DiagnosticsProvider {
  def getLogger(classType: Class[_]) : ILogger
}

// minimal slf4j logger
private[spark] class DefaultDiagnostics extends DiagnosticsProvider {
  override def getLogger(classType: Class[_]): ILogger = new DefaultMinimalSlf4jLogger(classType)
}

// only when diagnostics enabled,
// - logs each individual writes success and/or failures with id,pk
// - logs each documentServiceRequest and documentServiceResponse
private[spark] class SimpleDiagnosticsProvider extends DiagnosticsProvider {
  override def getLogger(classType: Class[_]): ILogger = new SimpleDiagnosticsSlf4jLogger(classType)
}
