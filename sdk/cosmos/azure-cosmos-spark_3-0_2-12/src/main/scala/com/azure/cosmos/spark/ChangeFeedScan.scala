// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.CosmosClientMetadataCachesSnapshot
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.connector.read.{Batch, Scan}
import org.apache.spark.sql.types.StructType

private case class ChangeFeedScan
(
  schema: StructType,
  config: Map[String, String],
  cosmosClientStateHandle: Broadcast[CosmosClientMetadataCachesSnapshot]
)
  extends Scan
    with CosmosLoggingTrait {

  logTrace(s"Instantiated ${this.getClass.getSimpleName}")

  /**
   * Returns the actual schema of this data source scan, which may be different from the physical
   * schema of the underlying storage, as column pruning or other optimizations may happen.
   */
  override def readSchema(): StructType = {
    schema
  }

  // TODO fabianm Implement
  override def description(): String = super.description()

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
    new ChangeFeedBatch(schema, config, cosmosClientStateHandle)
  }
}
