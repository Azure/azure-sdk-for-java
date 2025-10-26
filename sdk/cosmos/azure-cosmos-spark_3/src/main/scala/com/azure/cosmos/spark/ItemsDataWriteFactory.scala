// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.spark.diagnostics.LoggerHelper
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.connector.write.streaming.StreamingDataWriterFactory
import org.apache.spark.sql.connector.write.{DataWriter, DataWriterFactory}
import org.apache.spark.sql.types.StructType

// scalastyle:off multiple.string.literals
private class ItemsDataWriteFactory(userConfig: Map[String, String],
                                    inputSchema: StructType,
                                    cosmosClientStateHandles: Broadcast[CosmosClientMetadataCachesSnapshots],
                                    diagnosticsConfig: DiagnosticsConfig,
                                    sparkEnvironmentInfo: String)
  extends DataWriterFactory
    with StreamingDataWriterFactory {

  @transient private lazy val log = LoggerHelper.getLogger(diagnosticsConfig, this.getClass)

  log.logTrace(s"Instantiated ${this.getClass.getSimpleName}")

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
    new CosmosWriter(
      userConfig,
      cosmosClientStateHandles,
      diagnosticsConfig,
      inputSchema,
      partitionId,
      taskId,
      None,
      sparkEnvironmentInfo)

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
    new CosmosWriter(
      userConfig,
      cosmosClientStateHandles,
      diagnosticsConfig,
      inputSchema,
      partitionId,
      taskId,
      Some(epochId),
      sparkEnvironmentInfo)
}
// scalastyle:on multiple.string.literals
