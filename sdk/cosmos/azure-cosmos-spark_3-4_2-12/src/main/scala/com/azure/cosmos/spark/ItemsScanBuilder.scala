// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.SparkBridgeInternal
import com.azure.cosmos.models.PartitionKeyDefinition
import com.azure.cosmos.spark.diagnostics.LoggerHelper
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.connector.read.{Scan, ScanBuilder, SupportsPushDownFilters, SupportsPushDownRequiredColumns}
import org.apache.spark.sql.sources.Filter
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.util.CaseInsensitiveStringMap

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

private case class ItemsScanBuilder(session: SparkSession,
                                    config: CaseInsensitiveStringMap,
                                    inputSchema: StructType,
                                    cosmosClientStateHandles: Broadcast[CosmosClientMetadataCachesSnapshots],
                                    diagnosticsConfig: DiagnosticsConfig,
                                    sparkEnvironmentInfo: String)
  extends ScanBuilder
    with SupportsPushDownFilters
    with SupportsPushDownRequiredColumns {

  @transient private lazy val log = LoggerHelper.getLogger(diagnosticsConfig, this.getClass)
  log.logTrace(s"Instantiated ${this.getClass.getSimpleName}")

  private val configMap = config.asScala.toMap
  private val readConfig = CosmosReadConfig.parseCosmosReadConfig(configMap)
  private var processedPredicates : Option[AnalyzedAggregatedFilters] = Option.empty

  private val clientConfiguration = CosmosClientConfiguration.apply(
    configMap,
    readConfig.forceEventualConsistency,
    CosmosClientConfiguration.getSparkEnvironmentInfo(Some(session))
  )
  private val containerConfig = CosmosContainerConfig.parseCosmosContainerConfig(configMap)
  private val description = {
    s"""Cosmos ItemsScanBuilder: ${containerConfig.database}.${containerConfig.container}""".stripMargin
  }

  private val partitionKeyDefinition: PartitionKeyDefinition = {
    TransientErrorsRetryPolicy.executeWithRetry(() => {
      val calledFrom = s"ItemsScan($description()).getPartitionKeyDefinition"
      Loan(
        List[Option[CosmosClientCacheItem]](
          Some(CosmosClientCache.apply(
            clientConfiguration,
            Some(cosmosClientStateHandles.value.cosmosClientMetadataCaches),
            calledFrom
          )),
          ThroughputControlHelper.getThroughputControlClientCacheItem(
            configMap, calledFrom, Some(cosmosClientStateHandles), sparkEnvironmentInfo)
        ))
        .to(clientCacheItems => {
          val container =
            ThroughputControlHelper.getContainer(
              configMap,
              containerConfig,
              clientCacheItems(0).get,
              clientCacheItems(1))

          SparkBridgeInternal
            .getContainerPropertiesFromCollectionCache(container)
            .getPartitionKeyDefinition()
        })
    })
  }

  private val filterAnalyzer = FilterAnalyzer(readConfig, partitionKeyDefinition)

  /**
    * Pushes down filters, and returns filters that need to be evaluated after scanning.
    * @param filters pushed down filters.
    * @return the filters that spark need to evaluate
    */
  override def pushFilters(filters: Array[Filter]): Array[Filter] = {
    this.processedPredicates = Option.apply(filterAnalyzer.analyze(filters))

    // return the filters that spark need to evaluate
    this.processedPredicates.get.filtersNotSupportedByCosmos
  }

  /**
    * Returns the filters that are pushed to Cosmos as query predicates
    * @return filters to be pushed to cosmos db.
    */
  override def pushedFilters: Array[Filter] = {
    if (this.processedPredicates.isDefined) {
      this.processedPredicates.get.filtersToBePushedDownToCosmos
    } else {
      Array[Filter]()
    }
  }

  override def build(): Scan = {
    val effectiveAnalyzedFilters = this.processedPredicates match {
      case Some(analyzedFilters) => analyzedFilters
      case None => filterAnalyzer.analyze(Array.empty[Filter])
    }

    // TODO when inferring schema we should consolidate the schema from pruneColumns
    new ItemsScan(
      session,
      inputSchema,
      this.configMap,
      this.readConfig,
      effectiveAnalyzedFilters,
      cosmosClientStateHandles,
      diagnosticsConfig,
      sparkEnvironmentInfo,
      partitionKeyDefinition)
  }

  /**
    * Applies column pruning w.r.t. the given requiredSchema.
    *
    * Implementation should try its best to prune the unnecessary columns or nested fields, but it's
    * also OK to do the pruning partially, e.g., a data source may not be able to prune nested
    * fields, and only prune top-level columns.
    *
    * Note that, `Scan` implementation should take care of the column
    * pruning applied here.
    */
  override def pruneColumns(requiredSchema: StructType): Unit = {
    // TODO: we need to decide whether do a push down or not on the projection
    // spark will do column pruning on the returned data.
    // pushing down projection to cosmos has tradeoffs:
    //   - it increases consumed RU in cosmos query engine
    //   - it decrease the networking layer latency
  }
}
