// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark


import com.azure.cosmos.implementation.CosmosClientMetadataCachesSnapshot
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

    private val container = ThroughputControlHelper.getContainer(userConfig, cosmosTargetContainerConfig, client)

    private val containerDefinition = container.read().block().getProperties
    private val partitionKeyDefinition = containerDefinition.getPartitionKeyDefinition

    private val writer = if (cosmosWriteConfig.bulkEnabled) {
      new BulkWriter(container, cosmosWriteConfig)
    } else {
      new PointWriter(container, cosmosWriteConfig)
    }

    override def write(internalRow: InternalRow): Unit = {
      val objectNode = CosmosRowConverter.fromInternalRowToObjectNode(internalRow, inputSchema)

      // TODO moderakh investigate if we should also support point write in non-blocking way
      // TODO moderakh support patch?
      // TODO moderakh bulkWrite in another PR

      if (!objectNode.has(CosmosConstants.Properties.Id) ||
        !objectNode.get(CosmosConstants.Properties.Id).isTextual) {
        logError(s"${CosmosConstants.Properties.Id} is a mandatory field. " +
          s"But it is missing or it is not a string")
      }
      val partitionKeyValue = PartitionKeyHelper.getPartitionKeyPath(objectNode, partitionKeyDefinition)
      writer.scheduleWrite(partitionKeyValue, objectNode)
    }

    override def commit(): WriterCommitMessage = {
      logInfo("commit invoked!!!")
      writer.flushAndClose()

      new WriterCommitMessage {}
    }

    override def abort(): Unit = {
      logInfo("abort invoked!!!")
      writer.flushAndClose()
    }

    override def close(): Unit = {
      logInfo("close invoked!!!")
      writer.flushAndClose()
    }
  }
}
// scalastyle:on multiple.string.literals
