// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, SparkBridgeImplementationInternal}
import com.azure.cosmos.models.{CosmosParameterizedQuery, SqlParameter, SqlQuerySpec}
import com.azure.cosmos.spark.CosmosPredicates.requireNotNull
import com.azure.cosmos.spark.diagnostics.{DiagnosticsContext, LoggerHelper}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.connector.read.streaming.ReadLimit
import org.apache.spark.sql.connector.read.{Batch, InputPartition, PartitionReaderFactory, Scan}
import org.apache.spark.sql.types.StructType

import java.util.UUID

private case class ItemsScan(session: SparkSession,
                             schema: StructType,
                             config: Map[String, String],
                             readConfig: CosmosReadConfig,
                             cosmosQuery: CosmosParameterizedQuery,
                             cosmosClientStateHandle: Broadcast[CosmosClientMetadataCachesSnapshot],
                             diagnosticsConfig: DiagnosticsConfig)
  extends Scan
    with Batch {

  requireNotNull(cosmosQuery, "cosmosQuery")

  @transient private lazy val log = LoggerHelper.getLogger(diagnosticsConfig, this.getClass)
  log.logInfo(s"Instantiated ${this.getClass.getSimpleName}")

  private val clientConfiguration = CosmosClientConfiguration.apply(config, readConfig.forceEventualConsistency)
  private val containerConfig = CosmosContainerConfig.parseCosmosContainerConfig(config)
  private val partitioningConfig = CosmosPartitioningConfig.parseCosmosPartitioningConfig(config)
  private val defaultMinPartitionCount = 1 + (2 * session.sparkContext.defaultParallelism)

  override def description(): String = {
    s"""Cosmos ItemsScan: ${containerConfig.database}.${containerConfig.container}
       | - Cosmos Query: ${toPrettyString(cosmosQuery.toSqlQuerySpec)}""".stripMargin
  }

  private[this] def toPrettyString(query: SqlQuerySpec) = {
    //scalastyle:off magic.number
    val sb = new StringBuilder()
    //scalastyle:on magic.number
    sb.append(query.getQueryText)
    query.getParameters.forEach(
      (p: SqlParameter) => sb
        .append(CosmosConstants.SystemProperties.LineSeparator)
        .append(" > param: ")
        .append(p.getName)
        .append(" = ")
        .append(p.getValue(classOf[Any])))

    sb.toString
  }

  /**
   * Returns the actual schema of this data source scan, which may be different from the physical
   * schema of the underlying storage, as column pruning or other optimizations may happen.
   */
  override def readSchema(): StructType = {
    schema
  }

  override def planInputPartitions(): Array[InputPartition] = {
    val partitionMetadata = CosmosPartitionPlanner.getFilteredPartitionMetadata(
      config,
      clientConfiguration,
      Some(cosmosClientStateHandle),
      containerConfig,
      partitioningConfig,
      false
    )

    Loan(CosmosClientCache.apply(
      clientConfiguration,
      Some(cosmosClientStateHandle),
      s"ItemsScan($description()).planInputPartitions"
    ))
      .to(clientCacheItem => {
        val container = ThroughputControlHelper
          .getContainer(config, containerConfig, clientCacheItem.client)
        SparkUtils.safeOpenConnectionInitCaches(container, log)

        CosmosPartitionPlanner
          .createInputPartitions(
            partitioningConfig,
            container,
            partitionMetadata,
            defaultMinPartitionCount,
            CosmosPartitionPlanner.DefaultPartitionSizeInMB,
            ReadLimit.allAvailable(),
            false
          )
          .map(_.asInstanceOf[InputPartition])
      })
  }

  override def createReaderFactory(): PartitionReaderFactory = {
    val correlationActivityId = UUID.randomUUID()
    log.logInfo(s"Creating ItemsScan with CorrelationActivityId '${correlationActivityId.toString}' for query '${cosmosQuery.queryText}'")
    ItemsScanPartitionReaderFactory(config,
      schema,
      cosmosQuery,
      DiagnosticsContext(correlationActivityId, cosmosQuery.queryText),
      cosmosClientStateHandle,
      DiagnosticsConfig.parseDiagnosticsConfig(config))
  }

  override def toBatch: Batch = {
    this
  }
}
