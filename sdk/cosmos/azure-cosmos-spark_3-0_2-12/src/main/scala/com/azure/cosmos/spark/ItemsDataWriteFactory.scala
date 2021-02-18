// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, SparkBridgeImplementationInternal}
import com.azure.cosmos.CosmosException
import com.azure.cosmos.implementation.CosmosClientMetadataCachesSnapshot
import com.azure.cosmos.models.{CosmosItemRequestOptions, PartitionKey}
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.connector.write.{DataWriter, DataWriterFactory, WriterCommitMessage}
import org.apache.spark.sql.types.StructType

// scalastyle:off multiple.string.literals
private class ItemsDataWriteFactory(userConfig: Map[String, String],
                            inputSchema: StructType,
                            cosmosClientStateHandle: Broadcast[CosmosClientMetadataCachesSnapshot])
  extends DataWriterFactory
    with CosmosLoggingTrait {
  logInfo(s"Instantiated ${this.getClass.getSimpleName}")

  override def createWriter(i: Int, l: Long): DataWriter[InternalRow] = new CosmosWriter(inputSchema)

  private class CosmosWriter(inputSchema: StructType) extends DataWriter[InternalRow] {
    logInfo(s"Instantiated ${this.getClass.getSimpleName}")

    private val cosmosTargetContainerConfig = CosmosContainerConfig.parseCosmosContainerConfig(userConfig)
    private val cosmosWriteConfig = CosmosWriteConfig.parseWriteConfig(userConfig)

    private val client = CosmosClientCache(CosmosClientConfiguration(userConfig, useEventualConsistency = true), Some(cosmosClientStateHandle))

    private val container = client.getDatabase(cosmosTargetContainerConfig.database)
      .getContainer(cosmosTargetContainerConfig.container)

    private val containerDefinition = container.read().block().getProperties
    private val partitionKeyDefinition = containerDefinition.getPartitionKeyDefinition

    private lazy val bulkWriter = new BulkWriter(container, cosmosWriteConfig)

    override def write(internalRow: InternalRow): Unit = {
      val objectNode = CosmosRowConverter.fromInternalRowToObjectNode(internalRow, inputSchema)

      // TODO moderakh investigate if we should also support point write in non-blocking way
      // TODO moderakh support patch?
      // TODO moderakh bulkWrite in another PR

      val partitionKeyValue = PartitionKeyHelper.getPartitionKeyPath(objectNode, partitionKeyDefinition)

      if (cosmosWriteConfig.bulkEnabled) {
        bulkWriter.scheduleWrite(partitionKeyValue, objectNode)
      } else {
        cosmosWriteConfig.itemWriteStrategy match {
          case ItemWriteStrategy.ItemOverwrite => upsertWithRetry(partitionKeyValue, objectNode)
          case ItemWriteStrategy.ItemAppend => createWithRetry(partitionKeyValue, objectNode)
        }
      }
    }

    // scalastyle:off return
    private def createWithRetry(partitionKeyValue: PartitionKey,
                                objectNode: ObjectNode): Unit = {

      var exceptionOpt = Option.empty[Exception]
      for (attempt <- 1 to cosmosWriteConfig.maxRetryCount + 1) {
        try {
          container.createItem(objectNode, partitionKeyValue, new CosmosItemRequestOptions()).block()
          return
        } catch {
          case e: CosmosException if Exceptions.isResourceExistsException(e) =>
            // TODO: what should we do on unique index violation? should we ignore or throw?
            // TODO moderakh we need to add log messages extract identifier (id, pk) and log
            return
          case e: CosmosException if Exceptions.canBeTransientFailure(e) =>
            logWarning(
              s"create item attempt #$attempt max remaining retries"
                  + s"${cosmosWriteConfig.maxRetryCount + 1 - attempt}, encountered ${e.getMessage}")
            exceptionOpt = Option.apply(e)
        }
      }

      assert(exceptionOpt.isDefined)
      throw exceptionOpt.get
    }

    private def upsertWithRetry(partitionKeyValue: PartitionKey,
                                objectNode: ObjectNode): Unit = {
      var exceptionOpt = Option.empty[Exception]
      for (attempt <- 1 to cosmosWriteConfig.maxRetryCount + 1) {

        try {
          container.upsertItem(objectNode, partitionKeyValue, new CosmosItemRequestOptions()).block()
          return
        } catch {
          case e: CosmosException if Exceptions.canBeTransientFailure(e) =>
            logWarning(
              s"upsert item attempt #$attempt max remaining retries "
                  + s"${cosmosWriteConfig.maxRetryCount + 1 - attempt}, encountered ${e.getMessage}")
            exceptionOpt = Option.apply(e)
        }
      }

      assert(exceptionOpt.isDefined)
      throw exceptionOpt.get
    }
    // scalastyle:on return

    override def commit(): WriterCommitMessage = {
      if (cosmosWriteConfig.bulkEnabled) {
        bulkWriter.flushAndClose()
      }

      new WriterCommitMessage {}
    }

    override def abort(): Unit = {
      if (cosmosWriteConfig.bulkEnabled) {
        bulkWriter.flushAndClose()
      }
    }

    override def close(): Unit = {
      if (cosmosWriteConfig.bulkEnabled) {
        bulkWriter.flushAndClose()
      }
    }
  }
}
// scalastyle:on multiple.string.literals
