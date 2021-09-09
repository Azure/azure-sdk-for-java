// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.spark.diagnostics.SimpleDiagnosticsSlf4jLogger
import com.nimbusds.jose.util.StandardCharset

import java.io.{BufferedReader, File, FileOutputStream, FileReader}
import java.nio.charset.StandardCharsets
import scala.collection.mutable.ListBuffer

// scalastyle:off multiple.string.literals

private[spark] final class SimpleFileDiagnosticsSlf4jLogger(classType: Class[_])
  extends SimpleDiagnosticsSlf4jLogger(classType: Class[_]) {

  private val thisLock = new Object()
  private val file =SimpleFileDiagnosticsProvider.getLogFile(classType)

  if (!file.exists()) {
    file.createNewFile()
  }

  private val fos = new FileOutputStream(file, true)

  override def logInfo(msg: => String): Unit = {
    super.logInfo(msg)
    val line = msg + "|||"
    writeToFile(line)
  }

  override def logInfo(msg: => String, throwable: Throwable): Unit = {
    super.logInfo(msg, throwable)
    val line = if (Option.apply(throwable).isDefined) {
      msg + "|||" + throwable.toString.filter(_ >= ' ')
    } else {
      msg + "|||"
    }

    writeToFile(line)
  }

  private def writeToFile(line: String) {
    thisLock.synchronized {
      val fileLock = fos.getChannel.lock()
      try {
        fos.write(line.getBytes(StandardCharsets.UTF_8))
        fos.write(System.getProperty("line.separator").getBytes())
        fos.flush()
        fileLock.release()
      } catch {
        case t: Throwable => fileLock.release()
      }
    }
  }

  def getMessages(): ListBuffer[(String, Option[String])] = {
    val result = new ListBuffer[(String, Option[String])]
    thisLock.synchronized {
      val reader = new BufferedReader(new FileReader(file))
      var continue = true
      while (continue) {
        val line = reader.readLine()
        if (line == null) {
          continue = false
        } else {
          val columns = line.split("\\|\\|\\|")
          if (columns.size == 2 && columns(1) != null && columns(1).length > 0) {
            result.append((columns(0), Some(columns(1))))
          } else {
            result.append((columns(0), None))
          }
        }
      }

    }
    result
  }
}
// // scalastyle:on multiple.string.literals

