// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.spark.diagnostics.{DiagnosticsProvider, ILogger}
import org.apache.commons.io.FileUtils

import java.io.{File, IOException}
import java.nio.file.Files
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
  FileUtils.forceDeleteOnExit(folder)

  def getOrCreateSingletonLoggerInstance(classType: Class[_]): SimpleFileDiagnosticsSlf4jLogger = {
    if (!folder.exists()) {
      folder.mkdirs()
    }

    singletonInstances.applyOrElse(
      classType,
      ct => new SimpleFileDiagnosticsSlf4jLogger(ct))
  }

  def getLogFile(classType: Class[_]): File = {
    folder.createNewFile()
    val logFile = new File(
      folder.getAbsolutePath()
      + File.separator
      + classType.getSimpleName().replace("$", "") + ".txt")

    FileUtils.forceDeleteOnExit(logFile)
    logFile
  }

  def reset(): Unit = {
    try {
      FileUtils.deleteDirectory(folder)
    } catch {
      case _: IOException =>
    }
  }
}

