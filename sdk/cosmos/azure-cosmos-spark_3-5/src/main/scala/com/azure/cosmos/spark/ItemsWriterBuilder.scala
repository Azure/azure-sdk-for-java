// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.{CosmosAsyncClient, ReadConsistencyStrategy, SparkBridgeInternal}
import com.azure.cosmos.spark.diagnostics.LoggerHelper
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.connector.distributions.{Distribution, Distributions}
import org.apache.spark.sql.connector.expressions.{Expression, Expressions, NullOrdering, SortDirection, SortOrder}
import org.apache.spark.sql.connector.metric.CustomMetric
import org.apache.spark.sql.connector.write.streaming.StreamingWrite
import org.apache.spark.sql.connector.write.{BatchWrite, RequiresDistributionAndOrdering, Write, WriteBuilder}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.util.CaseInsensitiveStringMap

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

private class ItemsWriterBuilder
(
  userConfig: CaseInsensitiveStringMap,
  inputSchema: StructType,
  cosmosClientStateHandles: Broadcast[CosmosClientMetadataCachesSnapshots],
  diagnosticsConfig: DiagnosticsConfig,
  sparkEnvironmentInfo: String
)
  extends WriteBuilder {
  @transient private lazy val log = LoggerHelper.getLogger(diagnosticsConfig, this.getClass)
  log.logTrace(s"Instantiated ${this.getClass.getSimpleName}")

  override def build(): Write = {
    new CosmosWrite
  }

  override def buildForBatch(): BatchWrite =
    new ItemsBatchWriter(
      userConfig.asCaseSensitiveMap().asScala.toMap,
      inputSchema,
      cosmosClientStateHandles,
      diagnosticsConfig,
      sparkEnvironmentInfo)

  override def buildForStreaming(): StreamingWrite =
    new ItemsBatchWriter(
      userConfig.asCaseSensitiveMap().asScala.toMap,
      inputSchema,
      cosmosClientStateHandles,
      diagnosticsConfig,
      sparkEnvironmentInfo)

  private class CosmosWrite extends Write with RequiresDistributionAndOrdering {

    private[this] val supportedCosmosMetrics: Array[CustomMetric] = {
      Array(
        new CosmosBytesWrittenMetric(),
        new CosmosRecordsWrittenMetric(),
        new TotalRequestChargeMetric()
      )
    }

    private[this] val writeConfig = CosmosWriteConfig.parseWriteConfig(
      userConfig.asCaseSensitiveMap().asScala.toMap,
      inputSchema
    )

    private[this] val containerConfig = CosmosContainerConfig.parseCosmosContainerConfig(
      userConfig.asCaseSensitiveMap().asScala.toMap
    )

    override def toBatch(): BatchWrite =
      new ItemsBatchWriter(
        userConfig.asCaseSensitiveMap().asScala.toMap,
        inputSchema,
        cosmosClientStateHandles,
        diagnosticsConfig,
        sparkEnvironmentInfo)

    override def toStreaming: StreamingWrite =
      new ItemsBatchWriter(
        userConfig.asCaseSensitiveMap().asScala.toMap,
        inputSchema,
        cosmosClientStateHandles,
        diagnosticsConfig,
        sparkEnvironmentInfo)

    override def supportedCustomMetrics(): Array[CustomMetric] = supportedCosmosMetrics

    override def requiredDistribution(): Distribution = {
      if (writeConfig.bulkEnabled && writeConfig.itemWriteStrategy == ItemWriteStrategy.ItemTransactionalBatch) {
        // For transactional writes, partition by all partition key columns
        val partitionKeyPaths = getPartitionKeyColumnNames()
        if (partitionKeyPaths.nonEmpty) {
          // Use public Expressions.column() factory - returns NamedReference
          val clustering = partitionKeyPaths.map(path => Expressions.column(path): Expression).toArray
          Distributions.clustered(clustering)
        } else {
          Distributions.unspecified()
        }
      } else {
        Distributions.unspecified()
      }
    }

    override def requiredOrdering(): Array[SortOrder] = {
      if (writeConfig.bulkEnabled && writeConfig.itemWriteStrategy == ItemWriteStrategy.ItemTransactionalBatch) {
        // For transactional writes, order by all partition key columns (ascending)
        val partitionKeyPaths = getPartitionKeyColumnNames()
        if (partitionKeyPaths.nonEmpty) {
          partitionKeyPaths.map { path =>
            // Use public Expressions.sort() factory for creating SortOrder
            Expressions.sort(
              Expressions.column(path),
              SortDirection.ASCENDING,
              NullOrdering.NULLS_FIRST
            )
          }.toArray
        } else {
          Array.empty[SortOrder]
        }
      } else {
        Array.empty[SortOrder]
      }
    }

    private def getPartitionKeyColumnNames(): Seq[String] = {
      try {
        // Need to create a temporary container client to get partition key definition
        val clientCacheItem = CosmosClientCache(
          CosmosClientConfiguration(
            userConfig.asCaseSensitiveMap().asScala.toMap,
            ReadConsistencyStrategy.EVENTUAL,
            sparkEnvironmentInfo
          ),
          Some(cosmosClientStateHandles.value.cosmosClientMetadataCaches),
          "ItemsWriterBuilder-PKLookup"
        )

        val container = ThroughputControlHelper.getContainer(
          userConfig.asCaseSensitiveMap().asScala.toMap,
          containerConfig,
          clientCacheItem,
          None
        )

        val containerProperties = SparkBridgeInternal.getContainerPropertiesFromCollectionCache(container)
        val partitionKeyDefinition = containerProperties.getPartitionKeyDefinition

        // Release the client
        clientCacheItem.close()

        if (partitionKeyDefinition != null && partitionKeyDefinition.getPaths != null) {
          val paths = partitionKeyDefinition.getPaths.asScala
          paths.map(path => {
            // Remove leading '/' from partition key path (e.g., "/pk" -> "pk")
            if (path.startsWith("/")) path.substring(1) else path
          }).toSeq
        } else {
          Seq.empty[String]
        }
      } catch {
        case ex: Exception =>
          log.logWarning(s"Failed to get partition key definition for transactional writes: ${ex.getMessage}")
          Seq.empty[String]
      }
    }
  }
}
