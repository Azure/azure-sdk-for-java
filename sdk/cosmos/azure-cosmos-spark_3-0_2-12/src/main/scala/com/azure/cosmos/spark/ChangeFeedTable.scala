// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.CosmosClientMetadataCachesSnapshot
import com.azure.cosmos.models.PartitionKey
import com.azure.cosmos.{CosmosAsyncClient, CosmosException}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.connector.catalog.{SupportsRead, Table, TableCapability}
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.connector.read.ScanBuilder
import org.apache.spark.sql.util.CaseInsensitiveStringMap
import org.codehaus.jackson.node.ObjectNode

import java.util
import java.util.UUID

// scalastyle:off underscore.import
import com.azure.cosmos.spark.CosmosTableSchemaInferrer._
import org.apache.spark.sql.types._

import scala.collection.JavaConverters._
// scalastyle:on underscore.import

private[spark] object ChangeFeedTable {

  private[spark] val defaultIncrementalChangeFeedSchemaForInferenceDisabled = StructType(Seq(
    StructField(RawJsonBodyAttributeName, StringType),
    StructField(IdAttributeName, StringType),
    StructField(TimestampAttributeName, LongType),
    StructField(ETagAttributeName, StringType)
  ))

  private[spark] val defaultFullFidelityChangeFeedSchemaForInferenceDisabled = StructType(Seq(
    StructField(RawJsonBodyAttributeName, StringType),
    StructField(IdAttributeName, StringType),
    StructField(TimestampAttributeName, LongType),
    StructField(ETagAttributeName, StringType),
    StructField(OperationTypeAttributeName, StringType),
    StructField(PreviousRawJsonBodyAttributeName, StringType),
    StructField(TtlExpiredAttributeName, BooleanType)
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
    with SupportsRead
    with CosmosLoggingTrait {

  logTrace(s"Instantiated ${this.getClass.getSimpleName}")

  // This can only be used for data operation against a certain container.
  private lazy val containerStateHandle: Broadcast[CosmosClientMetadataCachesSnapshot] =
    initializeAndBroadcastCosmosClientStateForContainer()
  private val effectiveUserConfig = CosmosConfig.getEffectiveConfig(userConfig.asScala.toMap)
  private val clientConfig = CosmosAccountConfig.parseCosmosAccountConfig(effectiveUserConfig)
  private val cosmosContainerConfig = CosmosContainerConfig.parseCosmosContainerConfig(effectiveUserConfig)
  private val changeFeedConfig = CosmosChangeFeedConfig.parseCosmosChangeFeedConfig(effectiveUserConfig)
  private val readConfig = CosmosReadConfig.parseCosmosReadConfig(effectiveUserConfig)
  private val tableName = s"com.azure.cosmos.spark.changeFeed.items.${clientConfig.accountName}." +
    s"${cosmosContainerConfig.database}.${cosmosContainerConfig.container}"
  private val client = CosmosClientCache(
    CosmosClientConfiguration(effectiveUserConfig,
    useEventualConsistency = readConfig.forceEventualConsistency), None)

  override def name(): String = tableName

  override def capabilities(): util.Set[TableCapability] = Set(
    TableCapability.BATCH_READ,
    TableCapability.MICRO_BATCH_READ
  ).asJava

  override def newScanBuilder(options: CaseInsensitiveStringMap): ScanBuilder = {
    ChangeFeedScanBuilder(
      session,
      new CaseInsensitiveStringMap(
        CosmosConfig.getEffectiveConfig(options.asCaseSensitiveMap().asScala.toMap).asJava),
      schema(),
      containerStateHandle)
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

    CosmosTableSchemaInferrer.inferSchema(client, userConfig, defaultSchema)
  }

  // This can be used only when databaseName and ContainerName are specified.
  private[spark] def initializeAndBroadcastCosmosClientStateForContainer()
  : Broadcast[CosmosClientMetadataCachesSnapshot] = {

    try {
      client.getDatabase(cosmosContainerConfig.database).getContainer(cosmosContainerConfig.container).readItem(
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

