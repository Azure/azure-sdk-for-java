// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark.diagnostics

import com.azure.cosmos.implementation.spark.OperationContext
import com.azure.cosmos.implementation.{RxDocumentServiceRequest, RxDocumentServiceResponse}
import org.slf4j.{Logger, LoggerFactory}

private[spark] class DefaultMinimalSlf4jLogger(classType: Class[_])
  extends ILogger {
  // Make the log field transient so that objects with Logging can
  // be serialized and used on another machine
  @transient private lazy val log: Logger = LoggerFactory.getLogger(logName)

  // Method to get the logger name for this object
  protected def logName: String = {
    // Ignore trailing $'s in the class names for Scala objects
    classType.getName.stripSuffix("$")
  }

  // Log methods that take only a String
  def logInfo(msg: => String) {
    if (log.isInfoEnabled) log.info(msg)
  }

  def logDebug(msg: => String) {
    if (log.isDebugEnabled) log.debug(msg)
  }

  def logTrace(msg: => String) {
    if (log.isTraceEnabled) log.trace(msg)
  }

  def logWarning(msg: => String) {
    if (log.isWarnEnabled) log.warn(msg)
  }

  def logError(msg: => String) {
    if (log.isErrorEnabled) log.error(msg)
  }

  // Log methods that take Throwables (Exceptions/Errors) too
  def logInfo(msg: => String, throwable: Throwable) {
    if (log.isInfoEnabled) log.info(msg, throwable)
  }

  def isDebugLogEnabled: Boolean = {
    log.isDebugEnabled()
  }

  def logDebug(msg: => String, throwable: Throwable) {
    if (log.isDebugEnabled) log.debug(msg, throwable)
  }

  def logTrace(msg: => String, throwable: Throwable) {
    if (log.isTraceEnabled) log.trace(msg, throwable)
  }

  def logWarning(msg: => String, throwable: Throwable) {
    if (log.isWarnEnabled) log.warn(msg, throwable)
  }

  def logError(msg: => String, throwable: Throwable) {
    if (log.isErrorEnabled) log.error(msg, throwable)
  }

  override def logItemWriteCompletion(writeOperation: WriteOperation): Unit = {
  }

  override def logItemWriteSkipped(writeOperation: WriteOperation, detail: => String): Unit = {
  }

  override def logItemWriteFailure(writeOperation: WriteOperation): Unit = {
  }

  override def logItemWriteFailure(writeOperation: WriteOperation, throwable: Throwable): Unit = {
  }

  override def logItemWriteDetails(writeOperation: WriteOperation, detail: => String): Unit = {
  }

  override def requestListener(context: OperationContext, request: RxDocumentServiceRequest): Unit = {
  }

  override def responseListener(context: OperationContext, response: RxDocumentServiceResponse): Unit = {
  }

  override def exceptionListener(context: OperationContext, exception: Throwable): Unit = {
  }
}
