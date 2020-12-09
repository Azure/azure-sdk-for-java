// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import java.util.UUID

import com.azure.cosmos.implementation.TestConfigurations
import com.azure.cosmos.{ConsistencyLevel, CosmosClientBuilder}
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.connector.write.{DataWriter, DataWriterFactory, WriterCommitMessage}
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}

class CosmosDataWriteFactory(userConfig: Map[String, String],
                             inputSchema: StructType) extends DataWriterFactory with CosmosLoggingTrait {
  logInfo(s"Instantiated ${this.getClass.getSimpleName}")

  override def createWriter(i: Int, l: Long): DataWriter[InternalRow] = new CosmosWriter(inputSchema)

  class CosmosWriter(inputSchema: StructType) extends DataWriter[InternalRow] {
    logInfo(s"Instantiated ${this.getClass.getSimpleName}")

    val cosmosAccountConfig = CosmosAccountConfig.parseCosmosAccountConfig(userConfig)
    val cosmosTargetContainerConfig = CosmosContainerConfig.parseCosmosContainerConfig(userConfig)

    // TODO moderakh: this needs to be shared to avoid creating multiple clients
    val client = new CosmosClientBuilder()
      .key(cosmosAccountConfig.key)
      .endpoint(cosmosAccountConfig.endpoint)
      .consistencyLevel(ConsistencyLevel.EVENTUAL)
      .buildAsyncClient();

    override def write(internalRow: InternalRow): Unit = {
      // TODO moderakh: schema is hard coded for now to make end to end TestE2EMain work implement schema inference code
      val objectNode = CosmosRowConverter.internalRowToObjectNode(internalRow, inputSchema)
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
