// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.models.PartitionKeyDefinition
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.connector.expressions.NamedReference
import org.apache.spark.sql.connector.read.SupportsRuntimeFiltering
import org.apache.spark.sql.sources.Filter
import org.apache.spark.sql.types.StructType

private[spark] class ItemsScan(session: SparkSession,
                               schema: StructType,
                               config: Map[String, String],
                               readConfig: CosmosReadConfig,
                               analyzedFilters: AnalyzedAggregatedFilters,
                               cosmosClientStateHandles: Broadcast[CosmosClientMetadataCachesSnapshots],
                               diagnosticsConfig: DiagnosticsConfig,
                               sparkEnvironmentInfo: String,
                               partitionKeyDefinition: PartitionKeyDefinition)
  extends ItemsScanBase(
    session,
    schema,
    config,
    readConfig,
    analyzedFilters,
    cosmosClientStateHandles,
    diagnosticsConfig,
    sparkEnvironmentInfo,
    partitionKeyDefinition)
    with SupportsRuntimeFiltering { // SupportsRuntimeFiltering extends scan
  override def filterAttributes(): Array[NamedReference] = {
    runtimeFilterAttributesCore()
  }

  override def filter(filters: Array[Filter]): Unit = {
    runtimeFilterCore(filters)
  }
}
