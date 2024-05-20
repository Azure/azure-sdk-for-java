// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.CosmosException
import com.azure.cosmos.implementation.CosmosClientMetadataCachesSnapshot
import com.azure.cosmos.models.{CosmosItemIdentity, PartitionKey}
import com.azure.cosmos.spark.CosmosPredicates.assertOnSparkDriver
import com.azure.cosmos.spark.diagnostics.{BasicLoggingTrait, DiagnosticsContext}
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.TaskContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.sql.types.StructType

import java.util.UUID

private[spark] class CosmosReadManyReader(
                                         val userProvidedSchema: StructType,
                                         val userConfig: Map[String, String]
                                         ) extends BasicLoggingTrait with Serializable {
  val effectiveUserConfig: Map[String, String] = CosmosConfig.getEffectiveConfig(
    databaseName = None,
    containerName = None,
    userConfig)

  val clientConfig: CosmosAccountConfig = CosmosAccountConfig.parseCosmosAccountConfig(effectiveUserConfig)
  val readConfig: CosmosReadConfig = CosmosReadConfig.parseCosmosReadConfig(effectiveUserConfig)
  val cosmosContainerConfig: CosmosContainerConfig =
    CosmosContainerConfig.parseCosmosContainerConfig(effectiveUserConfig, None, None)
  //scalastyle:off multiple.string.literals
  val tableName: String = s"com.azure.cosmos.spark.items.${clientConfig.accountName}." +
    s"${cosmosContainerConfig.database}.${cosmosContainerConfig.container}"
  private lazy val sparkSession = {
    assertOnSparkDriver()
    SparkSession.active
  }
  val sparkEnvironmentInfo: String = CosmosClientConfiguration.getSparkEnvironmentInfo(Some(sparkSession))
  logTrace(s"Instantiated ${this.getClass.getSimpleName} for $tableName")

  private[spark] def initializeAndBroadcastCosmosClientStatesForContainer(): Broadcast[CosmosClientMetadataCachesSnapshots] = {
    val calledFrom = s"CosmosReadManyReader($tableName).initializeAndBroadcastCosmosClientStateForContainer"
    Loan(
      List[Option[CosmosClientCacheItem]](
        Some(
          CosmosClientCache(
            CosmosClientConfiguration(
              effectiveUserConfig,
              useEventualConsistency = readConfig.forceEventualConsistency,
              sparkEnvironmentInfo),
            None,
            calledFrom)),
        ThroughputControlHelper.getThroughputControlClientCacheItem(
          effectiveUserConfig,
          calledFrom,
          None,
          sparkEnvironmentInfo)
      ))
      .to(clientCacheItems => {
        val container =
          ThroughputControlHelper.getContainer(
            effectiveUserConfig,
            cosmosContainerConfig,
            clientCacheItems(0).get,
            clientCacheItems(1))
        try {
          container.readItem(
            UUID.randomUUID().toString,
            new PartitionKey(UUID.randomUUID().toString),
            classOf[ObjectNode])
            .block()
        } catch {
          case _: CosmosException => None
        }

        val state = new CosmosClientMetadataCachesSnapshot()
        state.serialize(clientCacheItems(0).get.cosmosClient)

        var throughputControlState: Option[CosmosClientMetadataCachesSnapshot] = None
        if (clientCacheItems(1).isDefined) {
          throughputControlState = Some(new CosmosClientMetadataCachesSnapshot())
          throughputControlState.get.serialize(clientCacheItems(1).get.cosmosClient)
        }

        val metadataSnapshots = CosmosClientMetadataCachesSnapshots(state, throughputControlState)
        sparkSession.sparkContext.broadcast(metadataSnapshots)
      })
  }

  def readMany(inputRdd: RDD[Row], identityExtraction:  Row => CosmosItemIdentity): DataFrame = {
    val correlationActivityId = UUID.randomUUID()
    val calledFrom = s"CosmosReadManyReader.readMany($correlationActivityId)"
    val schema = Loan(
      List[Option[CosmosClientCacheItem]](
        Some(CosmosClientCache(
          CosmosClientConfiguration(
            effectiveUserConfig,
            useEventualConsistency = readConfig.forceEventualConsistency,
            sparkEnvironmentInfo),
          None,
          calledFrom
        )),
        ThroughputControlHelper.getThroughputControlClientCacheItem(
          effectiveUserConfig,
          calledFrom,
          None,
          sparkEnvironmentInfo)
      ))
      .to(clientCacheItems => Option.apply(userProvidedSchema).getOrElse(
        CosmosTableSchemaInferrer.inferSchema(
          clientCacheItems(0).get,
          clientCacheItems(1),
          effectiveUserConfig,
          ItemsTable.defaultSchemaForInferenceDisabled)))

    val clientStates = initializeAndBroadcastCosmosClientStatesForContainer

    sparkSession.sqlContext.createDataFrame(
      inputRdd.mapPartitionsWithIndex(
        (partitionIndex: Int, readManyFilterIterator: Iterator[Row]) => {
          val readManyFilters: Iterator[CosmosItemIdentity] = readManyFilterIterator
            .map(row => identityExtraction.apply(row))

          logInfo(s"Creating an ItemsPartitionReaderWithReadMany for Activity $correlationActivityId to read for "
            + s"input partition [$partitionIndex] ${tableName}")

          val reader = new ItemsPartitionReaderWithReadMany(
            effectiveUserConfig,
            CosmosReadManyHelper.FullRangeFeedRange,
            schema,
            DiagnosticsContext(correlationActivityId, partitionIndex.toString),
            clientStates,
            DiagnosticsConfig.parseDiagnosticsConfig(effectiveUserConfig),
            sparkEnvironmentInfo,
            TaskContext.get,
            readManyFilters)

          new Iterator[Row] {
            override def hasNext: Boolean = reader.next()

            override def next(): Row = reader.getCurrentRow()
          }
        },
        preservesPartitioning = true
      ),
      schema)
  }
}

private object CosmosReadManyHelper {
  val FullRangeFeedRange: NormalizedRange = NormalizedRange("", "FF")
}
