// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark.diagnostics

private[spark] trait ILogger {
  // Log methods that take only a String
  def logInfo(msg: => String)

  def logDebug(msg: => String)

  def logTrace(msg: => String)

  def logWarning(msg: => String)

  def logError(msg: => String)

  def logInfo(msg: => String, throwable: Throwable)

  def isDebugLogEnabled: Boolean

  def logDebug(msg: => String, throwable: Throwable)

  def logTrace(msg: => String, throwable: Throwable)

  def logWarning(msg: => String, throwable: Throwable)

  def logError(msg: => String, throwable: Throwable)
}
