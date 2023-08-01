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

// only when diagnostics enabled,
// - logs each FeedResponse when it is retrieved and processed
private[spark] class FeedDiagnosticsProvider extends DiagnosticsProvider {
  override def getLogger(classType: Class[_]): ILogger = new FeedDiagnosticsSlf4jLogger(classType, false)
}

// only when diagnostics enabled,
// - logs each FeedResponse when it is retrieved and processed including list of pk and id values
private[spark] class DetailedFeedDiagnosticsProvider extends DiagnosticsProvider {
  override def getLogger(classType: Class[_]): ILogger = new FeedDiagnosticsSlf4jLogger(classType, true)
}
