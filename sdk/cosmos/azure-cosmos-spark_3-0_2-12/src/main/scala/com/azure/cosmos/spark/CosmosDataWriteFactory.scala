// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import java.util.UUID

import com.azure.cosmos.implementation.TestConfigurations
import com.azure.cosmos.{ConsistencyLevel, CosmosClientBuilder}
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.connector.write.{DataWriter, DataWriterFactory, WriterCommitMessage}
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}

class CosmosDataWriteFactory extends DataWriterFactory with CosmosLoggingTrait {
  logInfo(s"Instantiated ${this.getClass.getSimpleName}")

  override def createWriter(i: Int, l: Long): DataWriter[InternalRow] = new CosmosWriter()

  class CosmosWriter() extends DataWriter[InternalRow] {
    logInfo(s"Instantiated ${this.getClass.getSimpleName}")

    // TODO moderakh account config and databaseName, containerName need to passed down from the user
    val client = new CosmosClientBuilder()
      .key(TestConfigurations.MASTER_KEY)
      .endpoint(TestConfigurations.HOST)
      .consistencyLevel(ConsistencyLevel.EVENTUAL)
      .buildAsyncClient();
    val databaseName = "testDB"
    val containerName = "testContainer"

    override def write(internalRow: InternalRow): Unit = {
      // TODO moderakh: schema is hard coded for now to make end to end TestE2EMain work implement schema inference code
      val userProvidedSchema = StructType(Seq(StructField("number", IntegerType), StructField("word", StringType)))

      val objectNode = CosmosRowConverter.internalRowToObjectNode(internalRow, userProvidedSchema)
      // TODO: moderakh how should we handle absence of id?
      if (!objectNode.has("id")) {
        objectNode.put("id", UUID.randomUUID().toString)
      }
      client.getDatabase(databaseName)
        .getContainer(containerName)
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
