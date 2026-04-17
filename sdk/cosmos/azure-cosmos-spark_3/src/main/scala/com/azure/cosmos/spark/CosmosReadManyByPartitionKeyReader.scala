// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.{CosmosException, ReadConsistencyStrategy}
import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, UUIDs}
import com.azure.cosmos.models.PartitionKey
import com.azure.cosmos.spark.CosmosPredicates.assertOnSparkDriver
import com.azure.cosmos.spark.diagnostics.{BasicLoggingTrait, DiagnosticsContext}
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.TaskContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.sql.types.StructType

import java.util.UUID

private[spark] class CosmosReadManyByPartitionKeyReader(
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
    CosmosContainerConfig.parseCosmosContainerConfig(effectiveUserConfig)
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
    val calledFrom = s"CosmosReadManyByPartitionKeyReader($tableName).initializeAndBroadcastCosmosClientStateForContainer"
    Loan(
      List[Option[CosmosClientCacheItem]](
        Some(
          CosmosClientCache(
            CosmosClientConfiguration(
              effectiveUserConfig,
              readConsistencyStrategy = readConfig.readConsistencyStrategy,
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

        // Warm-up readItem: intentionally issues a lookup for a random id/partition-key pair
        // on the driver so that the collection/routing-map caches are populated before we serialize
        // the client state and broadcast it to executors. This costs ~1 RU + 1 RTT per broadcast build
        // (expected 404) but avoids every executor doing the same lookup in parallel on first use.
        try {
          container.readItem(
            UUIDs.nonBlockingRandomUUID().toString,
            new PartitionKey(UUIDs.nonBlockingRandomUUID().toString),
            classOf[ObjectNode])
            .block()
        } catch {
          // The warm-up readItem is only used to hydrate the collection/routing-map caches.
          // A 404 (item not found) is expected, but we log other CosmosExceptions at debug to
          // aid diagnosis (auth failures, throttling, etc.) while not failing reader setup.
          case ex: CosmosException =>
            logDebug(s"Warm-up readItem for metadata caches completed with exception: ${ex.getMessage}", ex)
            None
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

  def readManyByPartitionKey(inputRdd: RDD[Row], pkExtraction: Row => PartitionKey): DataFrame = {
    val correlationActivityId = UUIDs.nonBlockingRandomUUID()
    val calledFrom = s"CosmosReadManyByPartitionKeyReader.readManyByPartitionKey($correlationActivityId)"
    val schema = Loan(
      List[Option[CosmosClientCacheItem]](
        Some(CosmosClientCache(
          CosmosClientConfiguration(
            effectiveUserConfig,
            readConsistencyStrategy = readConfig.readConsistencyStrategy,
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
        (partitionIndex: Int, rowIterator: Iterator[Row]) => {
          val pkIterator: Iterator[PartitionKey] = rowIterator
            .map(row => pkExtraction.apply(row))

          logInfo(s"Creating an ItemsPartitionReaderWithReadManyByPartitionKey for Activity $correlationActivityId to read for "
            + s"input partition [$partitionIndex] ${tableName}")

          val taskContext = TaskContext.get
          val reader = new ItemsPartitionReaderWithReadManyByPartitionKey(
            effectiveUserConfig,
            CosmosReadManyHelper.FullRangeFeedRange,
            schema,
            DiagnosticsContext(correlationActivityId, partitionIndex.toString),
            clientStates,
            DiagnosticsConfig.parseDiagnosticsConfig(effectiveUserConfig),
            sparkEnvironmentInfo,
            taskContext,
            pkIterator)

          new Iterator[Row] {
            private var isClosed = false

            private def closeReader(): Unit = {
              if (!isClosed) {
                isClosed = true
                reader.close()
              }
            }

            if (taskContext != null) {
              taskContext.addTaskCompletionListener[Unit](_ => closeReader())
            }

            override def hasNext: Boolean = {
              try {
                val hasMore = reader.next()
                if (!hasMore) {
                  closeReader()
                }
                hasMore
              } catch {
                case error: Throwable =>
                  closeReader()
                  throw error
              }
            }

            override def next(): Row = {
              try {
                reader.getCurrentRow()
              } catch {
                case error: Throwable =>
                  closeReader()
                  throw error
              }
            }
          }
        },
        preservesPartitioning = true
      ),
      schema)
  }
}

