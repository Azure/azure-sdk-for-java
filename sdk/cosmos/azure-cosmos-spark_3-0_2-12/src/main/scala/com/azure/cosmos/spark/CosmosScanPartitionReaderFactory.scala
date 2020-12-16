// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.CosmosClientState
import com.azure.cosmos.models.CosmosParametrizedQuery
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.connector.read.{InputPartition, PartitionReader, PartitionReaderFactory}
import org.apache.spark.sql.types.StructType

case class CosmosScanPartitionReaderFactory(config: Map[String, String],
                                            readSchema: StructType,
                                            cosmosQuery: CosmosParametrizedQuery,
                                            cosmosClientStateHandle: Broadcast[CosmosClientState])
  extends PartitionReaderFactory with CosmosLoggingTrait {
  logInfo(s"Instantiated ${this.getClass.getSimpleName}")

  override def createReader(partition: InputPartition): PartitionReader[InternalRow] = {
    CosmosPartitionReader(config,
      readSchema,
      cosmosQuery,
      cosmosClientStateHandle)
  }
}
