// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.CosmosClientMetadataCachesSnapshot
import com.azure.cosmos.models.PartitionKey
import com.azure.cosmos.spark.diagnostics.LoggerHelper
import com.azure.cosmos.{CosmosAsyncClient, CosmosException}
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.connector.catalog.{SupportsRead, Table, TableCapability}
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.connector.read.ScanBuilder
import org.apache.spark.sql.util.CaseInsensitiveStringMap

import java.util
import java.util.UUID

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
    StructField(ETagAttributeName, StringType, nullable=false)
  ))

  private[spark] val defaultFullFidelityChangeFeedSchemaForInferenceDisabled = StructType(Seq(
    StructField(RawJsonBodyAttributeName, StringType, nullable=false),
    StructField(IdAttributeName, StringType, nullable=false),
    StructField(TimestampAttributeName, LongType, nullable=false),
    StructField(ETagAttributeName, StringType, nullable=false),
    StructField(OperationTypeAttributeName, StringType, nullable=false),
    StructField(PreviousRawJsonBodyAttributeName, StringType, nullable=true),
    StructField(TtlExpiredAttributeName, BooleanType, nullable=true)
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

  // This can only be used for data operation against a certain container.
  private lazy val containerStateHandle: Broadcast[CosmosClientMetadataCachesSnapshot] =
    initializeAndBroadcastCosmosClientStateForContainer()
  private val effectiveUserConfig = CosmosConfig.getEffectiveConfig(None, None, userConfig.asScala.toMap)
  private val clientConfig = CosmosAccountConfig.parseCosmosAccountConfig(effectiveUserConfig)
  private val cosmosContainerConfig = CosmosContainerConfig.parseCosmosContainerConfig(effectiveUserConfig)
  private val changeFeedConfig = CosmosChangeFeedConfig.parseCosmosChangeFeedConfig(effectiveUserConfig)
  private val readConfig = CosmosReadConfig.parseCosmosReadConfig(effectiveUserConfig)
  private val tableName = s"com.azure.cosmos.spark.changeFeed.items.${clientConfig.accountName}." +
    s"${cosmosContainerConfig.database}.${cosmosContainerConfig.container}"
  private val client = CosmosClientCache(
    CosmosClientConfiguration(effectiveUserConfig,
    useEventualConsistency = readConfig.forceEventualConsistency), None)
  private val container = ThroughputControlHelper.getContainer(effectiveUserConfig, cosmosContainerConfig, client)
  SparkUtils.safeOpenConnectionInitCaches(container, log)

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
      containerStateHandle,
      diagnosticsConfig)
  }

  override def schema(): StructType = {
    userProvidedSchema.getOrElse(this.inferSchema(client, effectiveUserConfig))
  }

  private def inferSchema(client: CosmosAsyncClient,
                          userConfig: Map[String, String]): StructType = {

    val defaultSchema: StructType = changeFeedConfig.changeFeedMode match {
      case ChangeFeedModes.Incremental =>
        ChangeFeedTable.defaultIncrementalChangeFeedSchemaForInferenceDisabled
      case ChangeFeedModes.FullFidelity =>
        ChangeFeedTable.defaultFullFidelityChangeFeedSchemaForInferenceDisabled
    }

    CosmosTableSchemaInferrer.inferSchema(
      client,
      userConfig,
      defaultSchema)
  }

  // This can be used only when databaseName and ContainerName are specified.
  private[spark] def initializeAndBroadcastCosmosClientStateForContainer()
  : Broadcast[CosmosClientMetadataCachesSnapshot] = {

    try {
        container.readItem(
            UUID.randomUUID().toString, new PartitionKey(UUID.randomUUID().toString), classOf[ObjectNode])
        .block()
    } catch {
      case _: CosmosException => None
    }

    val state = new CosmosClientMetadataCachesSnapshot()
    state.serialize(client)

    val sparkSession = SparkSession.active
    sparkSession.sparkContext.broadcast(state)
  }
}

