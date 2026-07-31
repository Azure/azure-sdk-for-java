// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.{CosmosException, ReadConsistencyStrategy, SparkBridgeInternal}
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
import java.util.concurrent.atomic.AtomicBoolean

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

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

  /**
   * Resolves the partition key paths for the target container.
   * Uses a single Loan block that also infers the schema (if needed) and warms
   * the client metadata caches for broadcast — avoiding three separate Loan round-trips.
   *
   * @return (pkPaths, schema, broadcastClientStates)
   */
  private[spark] def initializeReaderState(): (List[String], StructType, Broadcast[CosmosClientMetadataCachesSnapshots], Boolean) = {
    val calledFrom = s"CosmosReadManyByPartitionKeyReader($tableName).initializeReaderState"
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

        // 1. Resolve PK paths from the collection cache (with transient-error retry)
        val pkPaths = TransientErrorsRetryPolicy.executeWithRetry(() => {
          SparkBridgeInternal
            .getContainerPropertiesFromCollectionCache(container)
            .getPartitionKeyDefinition
            .getPaths.asScala.map(_.stripPrefix("/")).toList
        })

        // 2. Infer schema if not user-provided
        val schema = Option.apply(userProvidedSchema).getOrElse(
          CosmosTableSchemaInferrer.inferSchema(
            clientCacheItems(0).get,
            clientCacheItems(1),
            effectiveUserConfig,
            ItemsTable.defaultSchemaForInferenceDisabled))

        // 3. Warm-up readItem so collection/routing-map caches are populated before broadcast
        try {
          container.readItem(
            UUIDs.nonBlockingRandomUUID().toString,
            new PartitionKey(UUIDs.nonBlockingRandomUUID().toString),
            classOf[ObjectNode])
            .block()
        } catch {
          case _: CosmosException =>
            // Expected when the random read targets a non-existent item; we only need the
            // routing map / collection cache populated as a side-effect of the call.
            ()
        }

        // 4. Serialize and broadcast client state
        val state = new CosmosClientMetadataCachesSnapshot()
        state.serialize(clientCacheItems(0).get.cosmosClient)

        var throughputControlState: Option[CosmosClientMetadataCachesSnapshot] = None
        if (clientCacheItems(1).isDefined) {
          throughputControlState = Some(new CosmosClientMetadataCachesSnapshot())
          throughputControlState.get.serialize(clientCacheItems(1).get.cosmosClient)
        }

        val metadataSnapshots = CosmosClientMetadataCachesSnapshots(state, throughputControlState)
        val broadcastStates = sparkSession.sparkContext.broadcast(metadataSnapshots)

        (pkPaths, schema, broadcastStates, readConfig.readManyByPkTreatNullAsNone)
      })
  }

  def readManyByPartitionKeys(
    inputRdd: RDD[Row],
    pkExtraction: Row => PartitionKey,
    readerState: (List[String], StructType, Broadcast[CosmosClientMetadataCachesSnapshots], Boolean)): DataFrame = {

    val correlationActivityId = UUIDs.nonBlockingRandomUUID()
    val (_, schema, clientStates, _) = readerState

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
            private val isClosed = new AtomicBoolean(false)

            private def closeReader(): Unit = {
              if (isClosed.compareAndSet(false, true)) {
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

