// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.CosmosClientState
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.connector.write.{BatchWrite, DataWriterFactory, PhysicalWriteInfo, WriterCommitMessage}
import org.apache.spark.sql.types.StructType

class CosmosBatchWriter(userConfig: Map[String, String], inputSchema: StructType, cosmosClientStateHandle: Broadcast[CosmosClientState])
  extends BatchWrite
    with CosmosLoggingTrait {
  logInfo(s"Instantiated ${this.getClass.getSimpleName}")

  override def createBatchWriterFactory(physicalWriteInfo: PhysicalWriteInfo): DataWriterFactory = {
    new CosmosDataWriteFactory(userConfig, inputSchema, cosmosClientStateHandle)
  }

  override def commit(writerCommitMessages: Array[WriterCommitMessage]): Unit = {
    // TODO
  }

  override def abort(writerCommitMessages: Array[WriterCommitMessage]): Unit = {
    // TODO
  }
}
