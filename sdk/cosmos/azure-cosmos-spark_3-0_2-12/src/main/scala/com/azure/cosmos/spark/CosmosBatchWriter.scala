// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import org.apache.spark.sql.connector.write.{BatchWrite, DataWriterFactory, PhysicalWriteInfo, WriterCommitMessage}

class CosmosBatchWriter extends BatchWrite with CosmosLoggingTrait {
  logInfo(s"Instantiated ${this.getClass.getSimpleName}")

  override def createBatchWriterFactory(physicalWriteInfo: PhysicalWriteInfo): DataWriterFactory = new CosmosDataWriteFactory()

  override def commit(writerCommitMessages: Array[WriterCommitMessage]): Unit = {
    // TODO
  }

  override def abort(writerCommitMessages: Array[WriterCommitMessage]): Unit = {
    // TODO
  }
}