// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.models.{CosmosParameterizedQuery, PartitionKeyDefinition, SqlParameter, SqlQuerySpec}
import com.azure.cosmos.spark.CosmosPredicates.requireNotNull
import com.azure.cosmos.spark.diagnostics.{DiagnosticsContext, LoggerHelper}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.connector.expressions.{Expressions, NamedReference}
import org.apache.spark.sql.connector.read.streaming.ReadLimit
import org.apache.spark.sql.connector.read.{Batch, InputPartition, PartitionReaderFactory, Scan, Statistics, SupportsReportStatistics}
import org.apache.spark.sql.sources.Filter
import org.apache.spark.sql.types.StructType

import java.util.{OptionalLong, UUID}
import java.util.concurrent.atomic.{AtomicLong, AtomicReference}

private abstract class ItemsScanBase(session: SparkSession,
                                 schema: StructType,
                                 config: Map[String, String],
                                 readConfig: CosmosReadConfig,
                                 cosmosQuery: CosmosParameterizedQuery,
                                 cosmosClientStateHandles: Broadcast[CosmosClientMetadataCachesSnapshots],
                                 diagnosticsConfig: DiagnosticsConfig,
                                 sparkEnvironmentInfo: String)
  extends Scan
    with Batch
    with SupportsReportStatistics {

    requireNotNull(cosmosQuery, "cosmosQuery")

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
    private val readManyFilterMapRef = new AtomicReference[Map[NormalizedRange, List[String]]]()

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

        if (readManyFilterMapRef.get() == null) {
            // there is nothing to prune, return the original planned input partition
            plannedInputPartitionsRef.get().map(_.asInstanceOf[InputPartition])
        } else {
            // only return partitions has matching filter criteria
            prunePartitions().map(_.asInstanceOf[InputPartition])
        }
    }

    private def prunePartitions(): Array[CosmosInputPartition] = {
        plannedInputPartitionsRef.get().filter(
            inputPartition => {
                readManyFilterMapRef
                    .get()
                    .keys
                    .exists(readManyFilterPartitionRange => readManyFilterPartitionRange.compare(inputPartition.feedRange) == 0)
            })
    }

    override def createReaderFactory(): PartitionReaderFactory = {
        val correlationActivityId = UUID.randomUUID()
        log.logInfo(s"Creating ItemsScan with CorrelationActivityId '${correlationActivityId.toString}' for query '${cosmosQuery.queryText}'")
        ItemsScanPartitionReaderFactory(config,
            schema,
            cosmosQuery,
            DiagnosticsContext(correlationActivityId, cosmosQuery.queryText),
            cosmosClientStateHandles,
            DiagnosticsConfig.parseDiagnosticsConfig(config),
            sparkEnvironmentInfo,
            readManyFilterMapRef)
    }

    override def toBatch: Batch = {
        this
    }

    def filterAttributesCore(): Array[NamedReference] = {
        // we start with the specific readManyFilterProperty
        // but more optimization can be achieved here, for example,
        //     if id is the partitionKey as well, then the readMany optimization can kick in automatically if there are filters based on id
        //     or if the filter is based on partition key, then we can change into use readAllItems by partition key value
        if (readConfig.runtimeFilteringConfig.readRuntimeFilteringEnabled) {
            log.logInfo(s"filterAttribute is called and ${readConfig.runtimeFilteringConfig.readManyFilterProperty} is returned")
            Seq(Expressions.column(readConfig.runtimeFilteringConfig.readManyFilterProperty)).toArray
        } else {
            Array[NamedReference]()
        }
    }

    def filterCore(filters: Array[Filter]): Unit = {
        // this method will be called for runtime filters
        // for now, we will only care about partition dynamic pruning filters which is a IN filter
        // and the filter property matches the read runtime filter property '_itemIdentity'
        // but that being said, other optimizations can be done in future as well - for example filter by pnly pk value
        log.logInfo("Runtime filter is called")

        if (readConfig.runtimeFilteringConfig.readRuntimeFilteringEnabled) {
            val partitionKeyDefinition = getPartitionKeyDefinition()
            val plannedInputPartitions = planInputPartitions()

            val readManyFilterMapOpt = ReadManyFilterAnalyzer().analyze(
                filters,
                readConfig,
                partitionKeyDefinition,
                plannedInputPartitions.map(_.asInstanceOf[CosmosInputPartition]).toList)

            readManyFilterMapOpt match {
                case Some(readManyFilterMap) => readManyFilterMapRef.set(readManyFilterMap.map { case (key, value) => (key, value.toList) })
                case _ =>
            }
        }
    }

    private def getPartitionKeyDefinition(): PartitionKeyDefinition = {
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
                    SparkUtils.safeOpenConnectionInitCaches(container, log)

                    container.read().block().getProperties.getPartitionKeyDefinition()
                })
        })
    }

    override def estimateStatistics(): Statistics = {
        // for now, just always return the collection estimateStatistics
        // else by default internally spark will use Long.MaxValue
        // only do when there is no pushed down filters
        if (cosmosQuery.queryText.equals("SELECT * FROM r") && readManyFilterMapRef.get() == null) {
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

            SparkCosmosStatistics(OptionalLong.of(totalDocSizeInKB.get() * 1024), OptionalLong.of(itemCount.get()))
        } else {
            // for other cases, fall back to spark statistics calculation
            SparkCosmosStatistics(OptionalLong.empty(), OptionalLong.empty())
        }
    }

    case class SparkCosmosStatistics(sizeInBytes: OptionalLong, numRows: OptionalLong) extends Statistics {}
}
