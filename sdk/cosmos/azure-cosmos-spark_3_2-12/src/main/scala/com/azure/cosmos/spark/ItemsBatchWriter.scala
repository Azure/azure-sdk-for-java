// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.CosmosClientMetadataCachesSnapshot
import com.azure.cosmos.spark.diagnostics.LoggerHelper
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.connector.write.streaming.{StreamingDataWriterFactory, StreamingWrite}
import org.apache.spark.sql.connector.write.{BatchWrite, DataWriterFactory, PhysicalWriteInfo, WriterCommitMessage}
import org.apache.spark.sql.types.StructType

private class ItemsBatchWriter
(
  userConfig: Map[String, String],
  inputSchema: StructType,
  cosmosClientStateHandle: Broadcast[CosmosClientMetadataCachesSnapshot],
  diagnosticsConfig: DiagnosticsConfig
)
  extends BatchWrite
    with StreamingWrite {

  @transient private lazy val log = LoggerHelper.getLogger(diagnosticsConfig, this.getClass)
  log.logInfo(s"Instantiated ${this.getClass.getSimpleName}")

  override def createBatchWriterFactory(physicalWriteInfo: PhysicalWriteInfo): DataWriterFactory = {
    new ItemsDataWriteFactory(userConfig, inputSchema, cosmosClientStateHandle, diagnosticsConfig)
  }

  override def createStreamingWriterFactory(physicalWriteInfo: PhysicalWriteInfo): StreamingDataWriterFactory = {
    new ItemsDataWriteFactory(userConfig, inputSchema, cosmosClientStateHandle, diagnosticsConfig)
  }

  override def commit(writerCommitMessages: Array[WriterCommitMessage]): Unit = {
    // TODO
  }

  override def commit(epochId: Long, writerCommitMessages: Array[WriterCommitMessage]): Unit = {
    // TODO
  }

  override def abort(writerCommitMessages: Array[WriterCommitMessage]): Unit = {
    // TODO
  }

  override def abort(epochId: Long, writerCommitMessages: Array[WriterCommitMessage]): Unit = {
    // TODO
  }
}
