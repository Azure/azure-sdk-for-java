// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, SparkBridgeInternal}
import com.azure.cosmos.models.{CosmosItemRequestOptions, PartitionKey}
import com.azure.cosmos.{ConsistencyLevel, CosmosClientBuilder, CosmosException}
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.connector.write.{DataWriter, DataWriterFactory, WriterCommitMessage}
import org.apache.spark.sql.types.StructType

import java.util.UUID

class CosmosDataWriteFactory(userConfig: Map[String, String],
                             inputSchema: StructType,
                             cosmosClientStateHandle: Broadcast[CosmosClientMetadataCachesSnapshot])
  extends DataWriterFactory
    with CosmosLoggingTrait {
  logInfo(s"Instantiated ${this.getClass.getSimpleName}")

  override def createWriter(i: Int, l: Long): DataWriter[InternalRow] = new CosmosWriter(inputSchema)

  class CosmosWriter(inputSchema: StructType) extends DataWriter[InternalRow] {
    logInfo(s"Instantiated ${this.getClass.getSimpleName}")

    val cosmosAccountConfig = CosmosAccountConfig.parseCosmosAccountConfig(userConfig)
    val cosmosTargetContainerConfig = CosmosContainerConfig.parseCosmosContainerConfig(userConfig)
    val cosmosWriteConfig = CosmosWriteConfig.parseWriteConfig(userConfig)

    // TODO moderakh: this needs to be shared to avoid creating multiple clients
    val builder = new CosmosClientBuilder()
      .key(cosmosAccountConfig.key)
      .endpoint(cosmosAccountConfig.endpoint)
      .consistencyLevel(ConsistencyLevel.EVENTUAL)

    SparkBridgeInternal.setMetadataCacheSnapshot(builder, cosmosClientStateHandle.value)
    val client = builder.buildAsyncClient();

    val container = client.getDatabase(cosmosTargetContainerConfig.database)
      .getContainer(cosmosTargetContainerConfig.container)

    val containerDefinition = container.read().block().getProperties
    val partitionKeyDefinition = containerDefinition.getPartitionKeyDefinition

    override def write(internalRow: InternalRow): Unit = {
      val objectNode = CosmosRowConverter.fromInternalRowToObjectNode(internalRow, inputSchema)
      ensureHasId(objectNode)

      // TODO modreakh investigate if we should also support point write in non-blocking way
      // TODO moderakh support patch?
      // TODO moderakh bulkWrite in another PR

      val partitionKeyValue = PartitionKeyHelper.getPartitionKeyPath(objectNode, partitionKeyDefinition)

      if (cosmosWriteConfig.upsertEnabled) {
        upsertWithRetry(partitionKeyValue, objectNode, cosmosWriteConfig.maxRetryCount + 1)
      } else {
        createWithRetry(partitionKeyValue, objectNode, cosmosWriteConfig.maxRetryCount + 1)
      }
    }

    private def createWithRetry(partitionKeyValue: PartitionKey,
                                objectNode: ObjectNode,
                                remainingAttempts: Int,
                                exceptionToThrow: Option[Exception] = Option.empty): Unit = {
      if (remainingAttempts == 0) {
        assert(exceptionToThrow.isDefined)
        throw exceptionToThrow.get
      }
      try {
        container.createItem(objectNode, partitionKeyValue, new CosmosItemRequestOptions()).block()
      } catch {
        case e: CosmosException if Exceptions.isResourceExistsException(e) => {
          // TODO: what should we do on unique index violation? should we ignore or throw?
          // DONE
        }
        case e: CosmosException if Exceptions.canBeTransientFailure(e) => {
          createWithRetry(partitionKeyValue, objectNode, remainingAttempts - 1, exceptionToThrow)
        }
      }
    }

    private def upsertWithRetry(partitionKeyValue: PartitionKey,
                                objectNode: ObjectNode,
                                remainingAttempts: Int,
                                exceptionToThrow: Option[Exception] = Option.empty): Unit = {
      if (remainingAttempts == 0) {
        assert(exceptionToThrow.isDefined)
        throw exceptionToThrow.get
      }
      try {
        container.upsertItem(objectNode, partitionKeyValue, new CosmosItemRequestOptions()).block()
      } catch {
        case e: CosmosException if Exceptions.canBeTransientFailure(e) =>
          upsertWithRetry(partitionKeyValue, objectNode, remainingAttempts - 1, Option.apply(e))
        case e: Exception => throw e // unexpected failure
      }
    }

    private def ensureHasId(objectNode: ObjectNode): Unit = {
      // ensures the id has id otherwise it will autogenerate one
      if (!objectNode.has(CosmosConstants.CosmosIdFieldName)) {
        objectNode.put(CosmosConstants.CosmosIdFieldName, UUID.randomUUID().toString)
      }
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
