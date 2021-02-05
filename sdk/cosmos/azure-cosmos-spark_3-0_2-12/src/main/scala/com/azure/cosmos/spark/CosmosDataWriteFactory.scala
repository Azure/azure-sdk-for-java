// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import java.util.UUID

import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, SparkBridgeInternal}
import com.azure.cosmos.{ConsistencyLevel, CosmosClientBuilder}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.connector.write.{DataWriter, DataWriterFactory, WriterCommitMessage}
import org.apache.spark.sql.types.StructType

class CosmosDataWriteFactory(userConfig: Map[String, String],
                             inputSchema: StructType,
                             cosmosClientStateHandle: Broadcast[CosmosClientMetadataCachesSnapshot])
  extends DataWriterFactory
    with CosmosLoggingTrait {
  logInfo(s"Instantiated ${this.getClass.getSimpleName}")

  override def createWriter(i: Int, l: Long): DataWriter[InternalRow] = new CosmosWriter(inputSchema)

  class CosmosWriter(inputSchema: StructType) extends DataWriter[InternalRow] {
    logInfo(s"Instantiated ${this.getClass.getSimpleName}")

    val cosmosTargetContainerConfig = CosmosContainerConfig.parseCosmosContainerConfig(userConfig)

    val client = CosmosClientCache(CosmosClientConfiguration(userConfig, useEventualConsistency = true), Some(cosmosClientStateHandle))

    override def write(internalRow: InternalRow): Unit = {
      // TODO moderakh: schema is hard coded for now to make end to end TestE2EMain work implement schema inference code
      val objectNode = CosmosRowConverter.fromInternalRowToObjectNode(internalRow, inputSchema)
      // TODO: moderakh how should we handle absence of id?
      if (!objectNode.has("id")) {
        objectNode.put("id", UUID.randomUUID().toString)
      }
      client.getDatabase(cosmosTargetContainerConfig.database)
        .getContainer(cosmosTargetContainerConfig.container)
        .createItem(objectNode)
        .block()
    }

    override def commit(): WriterCommitMessage = {
      new WriterCommitMessage {}
    }

    override def abort(): Unit = {
      // TODO
    }

    override def close(): Unit = {
      // TODO
    }
  }
}
