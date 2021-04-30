// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark.diagnostics

import com.azure.cosmos.implementation.spark.OperationListener

private[spark] trait DiagnosticsProvider {
  def getLogger(classType: Class[_]) : ILogger

  def getOperationListener() : Option[OperationListener] = Option.empty
}

private[spark] class DefaultDiagnostics extends DiagnosticsProvider {
  override def getLogger(classType: Class[_]): ILogger = new DefaultSlf4jLogger(classType)
}

private[spark] class SimpleDiagnosticsProvider extends DiagnosticsProvider {
  override def getLogger(classType: Class[_]): ILogger = new DefaultSlf4jLogger(classType)
  override def getOperationListener() : Option[OperationListener] = Option.apply(new SimpleOperationListener)
}
