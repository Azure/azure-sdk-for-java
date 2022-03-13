// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark.diagnostics

import com.azure.cosmos.implementation.spark.OperationListener

private[spark] trait ILogger extends OperationListener {

  def logItemWriteCompletion(writeOperation: WriteOperation)
  def logItemWriteSkipped(writeOperation: WriteOperation, detail: => String)

  def logItemWriteFailure(writeOperation: WriteOperation)
  def logItemWriteFailure(writeOperation: WriteOperation, throwable: Throwable)

  def logItemWriteDetails(writeOperation: WriteOperation, detail: => String)

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
