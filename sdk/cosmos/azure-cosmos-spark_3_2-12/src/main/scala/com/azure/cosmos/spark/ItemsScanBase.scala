// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.SparkBridgeImplementationInternal
import com.azure.cosmos.models.{PartitionKeyDefinition, SqlParameter, SqlQuerySpec}
import com.azure.cosmos.spark.CosmosPredicates.requireNotNull
import com.azure.cosmos.spark.diagnostics.{DiagnosticsContext, LoggerHelper}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.connector.expressions.{Expressions, NamedReference}
import org.apache.spark.sql.connector.read.streaming.ReadLimit
import org.apache.spark.sql.connector.read.{Batch, InputPartition, PartitionReaderFactory, Scan, Statistics, SupportsReportStatistics}
import org.apache.spark.sql.sources.Filter
import org.apache.spark.sql.types.StructType

import java.util.concurrent.atomic.{AtomicLong, AtomicReference}
import java.util.{OptionalLong, UUID}

private abstract class ItemsScanBase(session: SparkSession,
                                     schema: StructType,
                                     config: Map[String, String],
                                     readConfig: CosmosReadConfig,
                                     analyzedFilters: AnalyzedAggregatedFilters,
                                     cosmosClientStateHandles: Broadcast[CosmosClientMetadataCachesSnapshots],
                                     diagnosticsConfig: DiagnosticsConfig,
                                     sparkEnvironmentInfo: String)
  extends Scan
    with Batch
    with SupportsReportStatistics {

  requireNotNull(analyzedFilters, "analyzedFilters")

  @transient private lazy val log = LoggerHelper.getLogger(diagnosticsConfig, this.getClass)
  log.logTrace(s"Instantiated ${this.getClass.getSimpleName}")

  private val clientConfiguration = CosmosClientConfiguration.apply(
    config,
    readConfig.forceEventualConsistency,
    CosmosClientConfiguration.getSparkEnvironmentInfo(Some(session))
  )
  private val containerConfig = CosmosContainerConfig.parseCosmosContainerConfig(config)
  private val partitioningConfig = CosmosPartitioningConfig.parseCosmosPartitioningConfig(config)
  private val defaultMinPartitionCount = 1 + (2 * session.sparkContext.defaultParallelism)
  private val plannedInputPartitionsRef = new AtomicReference[Array[CosmosInputPartition]]()
  private val cosmosQuery = analyzedFilters.cosmosParametrizedQuery

  private lazy val partitionKeyDefinition: PartitionKeyDefinition = {
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
            config, calledFrom, Some(cosmosClientStateHandles), sparkEnvironmentInfo)
        ))
        .to(clientCacheItems => {
          val container =
            ThroughputControlHelper.getContainer(
              config,
              containerConfig,
              clientCacheItems(0).get,
              clientCacheItems(1))

          container.read().block().getProperties.getPartitionKeyDefinition()
        })
    })
  }

  private val readManyFiltersMapRef = {
    analyzedFilters.readManyFiltersOpt match {
      case Some(readManyFilters) => new AtomicReference[Map[NormalizedRange, String]](getReadManyFilterMap(readManyFilters))
      case None => new AtomicReference[Map[NormalizedRange, String]]()
    }
  }
  private lazy val readManyFilterAnalyzer =
    ReadManyFilterAnalyzer(readConfig)

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

  //scalastyle:off method.length
  override def planInputPartitions(): Array[InputPartition] = {
    if (plannedInputPartitionsRef.get() == null) {
      val partitionMetadata = CosmosPartitionPlanner.getFilteredPartitionMetadata(
        config,
        clientConfiguration,
        Some(cosmosClientStateHandles),
        containerConfig,
        partitioningConfig,
        false
      )

      val calledFrom = s"ItemsScan($description()).planInputPartitions"
      Loan(
        List[Option[CosmosClientCacheItem]](
          Some(CosmosClientCache.apply(
            clientConfiguration,
            Some(cosmosClientStateHandles.value.cosmosClientMetadataCaches),
            calledFrom
          )),
          ThroughputControlHelper.getThroughputControlClientCacheItem(
            config, calledFrom, Some(cosmosClientStateHandles), sparkEnvironmentInfo)
        ))
        .to(clientCacheItems => {
          val container =
            ThroughputControlHelper.getContainer(
              config,
              containerConfig,
              clientCacheItems(0).get,
              clientCacheItems(1))
          SparkUtils.safeOpenConnectionInitCaches(container, log)

          val cosmosInputPartitions = CosmosPartitionPlanner
            .createInputPartitions(
              partitioningConfig,
              container,
              partitionMetadata,
              defaultMinPartitionCount,
              CosmosPartitionPlanner.DefaultPartitionSizeInMB,
              ReadLimit.allAvailable(),
              false
            )
          plannedInputPartitionsRef.set(cosmosInputPartitions)
        })
    }

    if (readManyFiltersMapRef.get() == null) {
      // there is nothing to prune, return the original planned input partition
      plannedInputPartitionsRef.get().map(_.asInstanceOf[InputPartition])
    } else {
      // only return partitions has matching filter criteria
      val afterPrunePlannedPartitions = prunePartitions()
      if (afterPrunePlannedPartitions.size < plannedInputPartitionsRef.get().size) {
        log.logInfo(s"There are ${plannedInputPartitionsRef.get().size - afterPrunePlannedPartitions.size} partitions got pruned")
      }

      afterPrunePlannedPartitions.map(_.asInstanceOf[InputPartition])
    }
  }

  private[this] def prunePartitions(): Array[CosmosInputPartition] = {
    plannedInputPartitionsRef.get().filter(
      inputPartition => {
        readManyFiltersMapRef
          .get()
          .keys
          .exists(readManyFilterFeedRange =>
            SparkBridgeImplementationInternal.doRangesOverlap(readManyFilterFeedRange, inputPartition.feedRange))
      })
  }

  override def createReaderFactory(): PartitionReaderFactory = {
    val correlationActivityId = UUID.randomUUID()
    log.logInfo(s"Creating ItemsScan with CorrelationActivityId '${correlationActivityId.toString}' for query '${cosmosQuery.queryText}'")
    ItemsScanPartitionReaderFactory(
      config,
      containerConfig,
      schema,
      cosmosQuery,
      DiagnosticsContext(correlationActivityId, cosmosQuery.queryText),
      cosmosClientStateHandles,
      DiagnosticsConfig.parseDiagnosticsConfig(config),
      sparkEnvironmentInfo,
      readManyFiltersMapRef)
  }

  override def toBatch: Batch = {
    this
  }

  def runtimeFilterAttributesCore(): Array[NamedReference] = {
    // we start with the specific readManyFilterProperty
    // but more optimization can be achieved here, for example,
    //     if id is the partitionKey as well, then the readMany optimization can kick in automatically if there are filters based on id
    //     or if the filter is based on partition key, then we can change into use readAllItems by partition key value
    if (readConfig.runtimeFilteringEnabled && readConfig.readManyFilteringConfig.readManyFilteringEnabled) {
      log.logInfo(s"filterAttribute is called and ${readConfig.readManyFilteringConfig.readManyFilterProperty} is returned")
      Seq(Expressions.column(readConfig.readManyFilteringConfig.readManyFilterProperty)).toArray
    } else {
      Array[NamedReference]()
    }
  }

  def runtimeFilterCore(filters: Array[Filter]): Unit = {
    // this method will be called for runtime filters
    // for now, we will only care about partition dynamic pruning filters which is a IN filter
    // and the filter property matches the read runtime filter property '_itemIdentity'
    // but that being said, other optimizations can be done in future as well - for example filter by only pk value
    log.logDebug("Runtime filter is called")

    if (shouldApplyRuntimeFilter() && readConfig.readManyFilteringConfig.readManyFilteringEnabled) {
      val readManyFilters = readManyFilterAnalyzer.analyze(filters)

      readManyFilters.readManyFiltersOpt match {
        case Some(readManyFilters) => readManyFiltersMapRef.set(getReadManyFilterMap(readManyFilters))
        case _ =>
      }
    }
  }

  private[this] def shouldApplyRuntimeFilter(): Boolean = {
    !analyzedFilters.isCustomQuery && readConfig.runtimeFilteringEnabled
  }

  private[this] def getReadManyFilterMap(readManyFilters: List[ReadManyFilter]): Map[NormalizedRange, String] = {
    readManyFilters.map(readManyFilter => {
      val feedRange =
        SparkBridgeImplementationInternal.partitionKeyToNormalizedRange(readManyFilter.partitionKey, this.partitionKeyDefinition)

      feedRange -> readManyFilter.value
    }).toMap
  }

  override def estimateStatistics(): Statistics = {

    // if there is no filters being pushed down, then we can safely use the collection statistics
    // else we will fallback to let spark do the calculation
    if (canUseCollectionStatistics()) {
      val plannedInputPartitions = this.planInputPartitions()
      val itemCount = new AtomicLong(0)
      val totalDocSizeInKB = new AtomicLong(0)

      for (inputPartition <- plannedInputPartitions) {
        val partitionMetadata = PartitionMetadataCache.apply(
          config,
          clientConfiguration,
          Some(cosmosClientStateHandles),
          containerConfig,
          inputPartition.asInstanceOf[CosmosInputPartition].feedRange
        ).block()

        itemCount.addAndGet(partitionMetadata.documentCount)
        totalDocSizeInKB.addAndGet(partitionMetadata.totalDocumentSizeInKB)
      }

      log.logInfo(s"totalDocSizeInKB ${totalDocSizeInKB.get()}, container ${containerConfig.container}")
      SparkCosmosStatistics(OptionalLong.of(totalDocSizeInKB.get() * 1024), OptionalLong.of(itemCount.get()))
    } else {
      // for other cases, fall back to spark statistics calculation
      SparkCosmosStatistics(OptionalLong.empty(), OptionalLong.empty())
    }
  }

  private[this] def canUseCollectionStatistics(): Boolean = {
    val canUseCollectionStatistics = analyzedFilters.filtersToBePushedDownToCosmos.isEmpty

    log.logInfo(s"canUseCollectionStatistics $canUseCollectionStatistics ${containerConfig.container}")
    canUseCollectionStatistics
  }

  private case class SparkCosmosStatistics(sizeInBytes: OptionalLong, numRows: OptionalLong) extends Statistics
}
