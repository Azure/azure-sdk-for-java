// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.CosmosClientMetadataCachesSnapshot
import com.azure.cosmos.spark.diagnostics.LoggerHelper
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.connector.read.streaming.MicroBatchStream
import org.apache.spark.sql.connector.read.{Batch, Scan}
import org.apache.spark.sql.types.StructType

private case class ChangeFeedScan
(
  session: SparkSession,
  schema: StructType,
  config: Map[String, String],
  cosmosClientStateHandle: Broadcast[CosmosClientMetadataCachesSnapshot],
  diagnosticsConfig: DiagnosticsConfig
)
  extends Scan {

  @transient private lazy val log = LoggerHelper.getLogger(diagnosticsConfig, this.getClass)

  log.logTrace(s"Instantiated ${this.getClass.getSimpleName}")
  private lazy val containerConfig = CosmosContainerConfig.parseCosmosContainerConfig(config)

  /**
   * Returns the actual schema of this data source scan, which may be different from the physical
   * schema of the underlying storage, as column pruning or other optimizations may happen.
   */
  override def readSchema(): StructType = {
    schema
  }

  override def description(): String = {
    s"""Cosmos ChangeFeedScan: ${containerConfig.database}.${containerConfig.container}""".stripMargin
  }

  /**
   * Returns the physical representation of this scan for batch query. By default this method throws
   * exception, data sources must overwrite this method to provide an implementation, if the
   * `Table` that creates this scan returns `TableCapability` support in its
   * `Table`.
   *
   * @throws UnsupportedOperationException throws an UnsupportedException when not supporting
   *                                       `TableCapability.BATCH_READ`
   */
  override def toBatch: Batch = {
    new ChangeFeedBatch(session, schema, config, cosmosClientStateHandle, diagnosticsConfig)
  }

  /**
   * Returns the physical representation of this scan for streaming query with micro-batch mode. By
   * default this method throws exception, data sources must overwrite this method to provide an
   * implementation, if the `Table` that creates this scan returns
   * `TableCapability` support in its `Table`.
   *
   * @param checkpointLocation a path to Hadoop FS scratch space that can be used for failure
   *                           recovery. Data streams for the same logical source in the same query
   *                           will be given the same checkpointLocation.
   * @throws UnsupportedOperationException throws an UnsupportedException when not supporting
   *                                       `TableCapability.MICRO_BATCH_READ`
   */
  override def toMicroBatchStream(checkpointLocation: String): MicroBatchStream = {
    new ChangeFeedMicroBatchStream(session, schema, config, cosmosClientStateHandle, checkpointLocation: String, diagnosticsConfig)
  }
}
