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

    // Extract userConfig conversion to avoid repeated calls
    private[this] val userConfigMap = userConfig.asCaseSensitiveMap().asScala.toMap

    private[this] val writeConfig = CosmosWriteConfig.parseWriteConfig(
      userConfigMap,
      inputSchema
    )

    private[this] val containerConfig = CosmosContainerConfig.parseCosmosContainerConfig(
      userConfigMap
    )

    override def toBatch(): BatchWrite =
      new ItemsBatchWriter(
        userConfigMap,
        inputSchema,
        cosmosClientStateHandles,
        diagnosticsConfig,
        sparkEnvironmentInfo)

    override def toStreaming: StreamingWrite =
      new ItemsBatchWriter(
        userConfigMap,
        inputSchema,
        cosmosClientStateHandles,
        diagnosticsConfig,
        sparkEnvironmentInfo)

    override def supportedCustomMetrics(): Array[CustomMetric] = supportedCosmosMetrics

    override def requiredDistribution(): Distribution = {
      if (writeConfig.bulkEnabled && writeConfig.bulkTransactional) {
        log.logInfo("Transactional batch mode enabled - configuring data distribution by partition key columns")
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
      if (writeConfig.bulkEnabled && writeConfig.bulkTransactional) {
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
        // Use loan pattern to ensure client is properly closed
        using(createClientForPartitionKeyLookup()) { clientCacheItem =>
          val container = ThroughputControlHelper.getContainer(
            userConfigMap,
            containerConfig,
            clientCacheItem,
            None
          )

          // Simplified retrieval using SparkBridgeInternal directly
          val containerProperties = SparkBridgeInternal.getContainerPropertiesFromCollectionCache(container)
          val partitionKeyDefinition = containerProperties.getPartitionKeyDefinition

          extractPartitionKeyPaths(partitionKeyDefinition)
        }
      } catch {
        case ex: Exception =>
          log.logWarning(s"Failed to get partition key definition for transactional writes: ${ex.getMessage}")
          Seq.empty[String]
      }
    }

    private def createClientForPartitionKeyLookup(): CosmosClientCacheItem = {
      CosmosClientCache(
        CosmosClientConfiguration(
          userConfigMap,
          ReadConsistencyStrategy.EVENTUAL,
          sparkEnvironmentInfo
        ),
        Some(cosmosClientStateHandles.value.cosmosClientMetadataCaches),
        "ItemsWriterBuilder-PKLookup"
      )
    }

    private def extractPartitionKeyPaths(partitionKeyDefinition: com.azure.cosmos.models.PartitionKeyDefinition): Seq[String] = {
      if (partitionKeyDefinition != null && partitionKeyDefinition.getPaths != null) {
        val paths = partitionKeyDefinition.getPaths.asScala
        if (paths.isEmpty) {
          log.logError("Partition key definition has 0 columns - this should not happen for modern containers")
        }
        paths.map(path => {
          // Remove leading '/' from partition key path (e.g., "/pk" -> "pk")
          if (path.startsWith("/")) path.substring(1) else path
        }).toSeq
      } else {
        log.logError("Partition key definition is null - this should not happen for modern containers")
        Seq.empty[String]
      }
    }

    // Scala loan pattern to ensure resources are properly cleaned up
    private def using[A <: { def close(): Unit }, B](resource: A)(f: A => B): B = {
      try {
        f(resource)
      } finally {
        if (resource != null) resource.close()
      }
    }
  }
}
