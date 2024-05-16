// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import org.apache.commons.io.IOUtils
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.execution.streaming.{HDFSMetadataLog, MetadataVersionUtil}

import java.io.{BufferedWriter, InputStream, InputStreamReader, OutputStream, OutputStreamWriter}
import java.nio.charset.StandardCharsets

private class ChangeFeedInitialOffsetWriter
(
  sparkSession: SparkSession,
  metadataPath: String
) extends HDFSMetadataLog[String](sparkSession, metadataPath) {

  val VERSION = 1

  override def serialize(offsetJson: String, out: OutputStream): Unit = {
    val writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))
    writer.write(s"v$VERSION\n")
    writer.write(offsetJson)
    writer.flush()
  }

  override def deserialize(in: InputStream): String = {
    val content = IOUtils.toString(new InputStreamReader(in, StandardCharsets.UTF_8))
    // HDFSMetadataLog would never create a partial file.
    require(content.nonEmpty)
    val indexOfNewLine = content.indexOf("\n")
    if (content(0) != 'v' || indexOfNewLine < 0) {
      throw new IllegalStateException(
        "Log file was malformed: failed to detect the log file version line.")
    }

    MetadataVersionUtil.validateVersion(content.substring(0, indexOfNewLine), VERSION)
    content.substring(indexOfNewLine + 1)
  }
}
