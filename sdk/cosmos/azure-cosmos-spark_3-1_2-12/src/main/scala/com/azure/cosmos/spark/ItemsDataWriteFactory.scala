// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark


import com.azure.cosmos.implementation.CosmosClientMetadataCachesSnapshot
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.connector.write.streaming.StreamingDataWriterFactory
import org.apache.spark.sql.connector.write.{DataWriter, DataWriterFactory, WriterCommitMessage}
import org.apache.spark.sql.types.StructType

// scalastyle:off multiple.string.literals
private class ItemsDataWriteFactory(userConfig: Map[String, String],
                            inputSchema: StructType,
                            cosmosClientStateHandle: Broadcast[CosmosClientMetadataCachesSnapshot])
  extends DataWriterFactory
    with StreamingDataWriterFactory
    with CosmosLoggingTrait {
  logInfo(s"Instantiated ${this.getClass.getSimpleName}")

  /**
   * Returns a data writer to do the actual writing work. Note that, Spark will reuse the same data
   * object instance when sending data to the data writer, for better performance. Data writers
   * are responsible for defensive copies if necessary, e.g. copy the data before buffer it in a
   * list.
   *
   * If this method fails (by throwing an exception), the corresponding Spark write task would fail
   * and get retried until hitting the maximum retry times.
   *
   * @param partitionId A unique id of the RDD partition that the returned writer will process.
   *                    Usually Spark processes many RDD partitions at the same time,
   *                    implementations should use the partition id to distinguish writers for
   *                    different partitions.
   * @param taskId      The task id returned by `TaskContext# taskAttemptId ( )`. Spark may run
   *                    multiple tasks for the same partition (due to speculation or task failures,
   *                    for example).
   */
  override def createWriter(partitionId: Int, taskId: Long): DataWriter[InternalRow] =
    new CosmosWriter(inputSchema)

  /**
   * Returns a data writer to do the actual writing work. Note that, Spark will reuse the same data
   * object instance when sending data to the data writer, for better performance. Data writers
   * are responsible for defensive copies if necessary, e.g. copy the data before buffer it in a
   * list.
   *
   * If this method fails (by throwing an exception), the corresponding Spark write task would fail
   * and get retried until hitting the maximum retry times.
   *
   * @param  partitionId A unique id of the RDD partition that the returned writer will process.
   *                     Usually Spark processes many RDD partitions at the same time,
   *                     implementations should use the partition id to distinguish writers for
   *                     different partitions.
   * @param taskId       The task id returned by `TaskContext# taskAttemptId ( )`. Spark may run
   *                     multiple tasks for the same partition (due to speculation or task failures,
   *                     for example).
   * @param epochId      A monotonically increasing id for streaming queries that are split in to
   *                     discrete periods of execution.
   */
  override def createWriter(partitionId: Int, taskId: Long, epochId: Long): DataWriter[InternalRow] =
    new CosmosWriter(inputSchema)

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

      require(objectNode.has(CosmosConstants.Properties.Id) &&
        objectNode.get(CosmosConstants.Properties.Id).isTextual,
        s"${CosmosConstants.Properties.Id} is a mandatory field. " +
          s"But it is missing or it is not a string. Json: ${SparkUtils.objectNodeToJson(objectNode)}")

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
