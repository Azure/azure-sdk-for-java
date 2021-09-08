// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.spark.diagnostics.{DiagnosticsProvider, ILogger, SimpleDiagnosticsSlf4jLogger}

import java.io.File
import scala.collection.concurrent.TrieMap

// only when diagnostics enabled,
// - logs each individual writes success and/or failures with id,pk
// - logs each documentServiceRequest and documentServiceResponse
private[spark] class SimpleFileDiagnosticsProvider extends DiagnosticsProvider {
  override def getLogger(classType: Class[_]): ILogger =
    SimpleFileDiagnosticsProvider.getOrCreateSingletonLoggerInstance(classType)
}

private[spark] object SimpleFileDiagnosticsProvider {
  val singletonInstances: TrieMap[Class[_], SimpleFileDiagnosticsSlf4jLogger] =
    new TrieMap[Class[_], SimpleFileDiagnosticsSlf4jLogger]()

  private val folderName = System.getProperty("java.io.tmpdir") + "/SimpleFileDiagnostics"
  private val folder = new File(folderName)

  def getOrCreateSingletonLoggerInstance(classType: Class[_]): SimpleFileDiagnosticsSlf4jLogger = {
    if (!folder.exists()) {
      folder.mkdirs()
    }

    singletonInstances.applyOrElse(
      classType,
      (ct) => new SimpleFileDiagnosticsSlf4jLogger(ct))
  }

  def getLogFileName(classType: Class[_]): String = {
    folderName + "/" + classType.getSimpleName().replace("$", "") + ".txt"
  }

  def reset() = {
    folder.delete()
  }
}

