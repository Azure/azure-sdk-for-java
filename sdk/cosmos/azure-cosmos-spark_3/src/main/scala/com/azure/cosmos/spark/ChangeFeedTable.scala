// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.CosmosException
import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, UUIDs}
import com.azure.cosmos.models.PartitionKey
import com.azure.cosmos.spark.diagnostics.LoggerHelper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.connector.catalog.{SupportsRead, Table, TableCapability}
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.connector.read.ScanBuilder
import org.apache.spark.sql.util.CaseInsensitiveStringMap

import java.util

// scalastyle:off underscore.import
import com.azure.cosmos.spark.CosmosTableSchemaInferrer._
import org.apache.spark.sql.types._

import scala.collection.JavaConverters._
// scalastyle:on underscore.import

private[spark] object ChangeFeedTable {

  private[spark] val defaultIncrementalChangeFeedSchemaForInferenceDisabled = StructType(Seq(
    StructField(RawJsonBodyAttributeName, StringType, nullable=false),
    StructField(IdAttributeName, StringType, nullable=false),
    StructField(TimestampAttributeName, LongType, nullable=false),
    StructField(ETagAttributeName, StringType, nullable=false),
    StructField(LsnAttributeName, LongType, nullable=false)
  ))

  private[spark] val defaultFullFidelityChangeFeedSchemaForInferenceDisabled = StructType(Seq(
    StructField(RawJsonBodyAttributeName, StringType, nullable=true),
    StructField(IdAttributeName, StringType, nullable=false),
    StructField(TimestampAttributeName, LongType, nullable=false),
    StructField(ETagAttributeName, StringType, nullable=true),
    StructField(LsnAttributeName, LongType, nullable=false),
    StructField(MetadataJsonBodyAttributeName, StringType, nullable=false),
    StructField(PreviousRawJsonBodyAttributeName, StringType, nullable=true),
    StructField(OperationTypeAttributeName, StringType, nullable=false),
    StructField(CrtsAttributeName, LongType, nullable=false),
    StructField(PreviousImageLsnAttributeName, LongType, nullable=true)
  ))
}

/**
 * ChangeFeedTable is the entry point for the change feed data source - this is registered in the spark
 *
 * @param transforms         The specified table partitioning.
 * @param userConfig         The effective user configuration
 * @param userProvidedSchema The user provided schema - can be null/none
 */
private class ChangeFeedTable(val session: SparkSession,
                              val transforms: Array[Transform],
                              val userConfig: util.Map[String, String],
                              val userProvidedSchema: Option[StructType] = None)
  extends Table
    with SupportsRead {

  private val diagnosticsConfig = DiagnosticsConfig.parseDiagnosticsConfig(userConfig.asScala.toMap)
  @transient private lazy val log = LoggerHelper.getLogger(diagnosticsConfig, this.getClass)

  log.logTrace(s"Instantiated ${this.getClass.getSimpleName}")

  private val effectiveUserConfig = CosmosConfig.getEffectiveConfig(None, None, userConfig.asScala.toMap)
  private val clientConfig = CosmosAccountConfig.parseCosmosAccountConfig(effectiveUserConfig)
  private val cosmosContainerConfig = CosmosContainerConfig.parseCosmosContainerConfig(effectiveUserConfig)
  private val changeFeedConfig = CosmosChangeFeedConfig.parseCosmosChangeFeedConfig(effectiveUserConfig)
  private val readConfig = CosmosReadConfig.parseCosmosReadConfig(effectiveUserConfig)
  private val tableName = s"com.azure.cosmos.spark.changeFeed.items.${clientConfig.accountName}." +
    s"${cosmosContainerConfig.database}.${cosmosContainerConfig.container}"
  private val sparkEnvironmentInfo = CosmosClientConfiguration.getSparkEnvironmentInfo(Some(session))
  private val cosmosClientConfig = CosmosClientConfiguration(
    effectiveUserConfig,
    readConsistencyStrategy = readConfig.readConsistencyStrategy,
    sparkEnvironmentInfo)
  // This can only be used for data operation against a certain container.
  private lazy val containerStateHandles: Broadcast[CosmosClientMetadataCachesSnapshots] =
    initializeAndBroadcastCosmosClientStatesForContainer()

  override def name(): String = tableName

  override def capabilities(): util.Set[TableCapability] = Set(
    TableCapability.BATCH_READ,
    TableCapability.MICRO_BATCH_READ
  ).asJava

  override def newScanBuilder(options: CaseInsensitiveStringMap): ScanBuilder = {
    val effectiveOptions = Option.apply(options) match {
      case Some(optionsProvided) => effectiveUserConfig ++ optionsProvided.asScala
      case None => effectiveUserConfig
    }

    ChangeFeedScanBuilder(
      session,
      new CaseInsensitiveStringMap(effectiveOptions.asJava),
      schema(),
      containerStateHandles,
      diagnosticsConfig)
  }

  override def schema(): StructType = {
    val calledFrom = s"ChangeFeedTable(name ${tableName}).schema"
    Loan(
      List[Option[CosmosClientCacheItem]](
        Some(CosmosClientCache(cosmosClientConfig, None, calledFrom)),
        ThroughputControlHelper.getThroughputControlClientCacheItem(effectiveUserConfig, calledFrom, None, sparkEnvironmentInfo)
      ))
      .to(clientCacheItems =>
        userProvidedSchema.getOrElse(this.inferSchema(clientCacheItems(0).get, clientCacheItems(1), effectiveUserConfig))
    )
  }

  private def inferSchema(clientCacheItem: CosmosClientCacheItem,
                          throughputControlClientCacheItem: Option[CosmosClientCacheItem],
                          userConfig: Map[String, String]): StructType = {

    val defaultSchema: StructType = changeFeedConfig.changeFeedMode match {
      case ChangeFeedModes.Incremental | ChangeFeedModes.LatestVersion =>
        ChangeFeedTable.defaultIncrementalChangeFeedSchemaForInferenceDisabled
      case ChangeFeedModes.FullFidelity | ChangeFeedModes.AllVersionsAndDeletes =>
        ChangeFeedTable.defaultFullFidelityChangeFeedSchemaForInferenceDisabled
    }

    CosmosTableSchemaInferrer.inferSchema(
      clientCacheItem,
      throughputControlClientCacheItem,
      userConfig,
      defaultSchema)
  }

  // This can be used only when databaseName and ContainerName are specified.
  private[spark] def initializeAndBroadcastCosmosClientStatesForContainer()
  : Broadcast[CosmosClientMetadataCachesSnapshots] = {

    val calledFrom = s"ChangeFeedTable(name ${tableName}).initializeAndBroadcastCosmosClientStateForContainer"
    Loan(
      List[Option[CosmosClientCacheItem]](
        Some(CosmosClientCache(
          cosmosClientConfig,
          None,
          calledFrom)),
        ThroughputControlHelper.getThroughputControlClientCacheItem(
          effectiveUserConfig,
          calledFrom,
          None,
          sparkEnvironmentInfo
        )
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
            UUIDs.nonBlockingRandomUUID().toString, new PartitionKey(UUIDs.nonBlockingRandomUUID().toString), classOf[ObjectNode])
            .block()
        } catch {
          case _: CosmosException =>
        }

        val state = new CosmosClientMetadataCachesSnapshot()
        state.serialize(clientCacheItems(0).get.cosmosClient)

        var throughputControlState: Option[CosmosClientMetadataCachesSnapshot] = None
        if (clientCacheItems(1).isDefined) {
          throughputControlState = Some(new CosmosClientMetadataCachesSnapshot())
          throughputControlState.get.serialize(clientCacheItems(1).get.cosmosClient)
        }

        val metadataSnapshots = CosmosClientMetadataCachesSnapshots(state, throughputControlState)
        val sparkSession = SparkSession.active
        sparkSession.sparkContext.broadcast(metadataSnapshots)
      })
  }
}

