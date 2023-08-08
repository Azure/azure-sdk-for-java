// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.CosmosException
import com.azure.cosmos.implementation.CosmosClientMetadataCachesSnapshot
import com.azure.cosmos.models.PartitionKey
import com.azure.cosmos.spark.CosmosTableSchemaInferrer.{IdAttributeName, RawJsonBodyAttributeName, TimestampAttributeName}
import com.azure.cosmos.spark.diagnostics.LoggerHelper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.connector.catalog.{SupportsRead, Table, TableCapability}
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.connector.read.ScanBuilder
import org.apache.spark.sql.types.{LongType, StringType, StructField, StructType}
import org.apache.spark.sql.util.CaseInsensitiveStringMap

import java.util
import java.util.{Collections, UUID}

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

private object ItemsReadOnlyTable {
  private[spark] val defaultSchemaForInferenceDisabled = StructType(Seq(
    StructField(RawJsonBodyAttributeName, StringType),
    StructField(IdAttributeName, StringType),
    StructField(TimestampAttributeName, LongType)
  ))
}

/**
 * ItemsTable is the entry point this is registered in the spark
 *
 * @param transforms         The specified table partitioning.
 * @param userConfig         The effective user configuration
 * @param userProvidedSchema The user provided schema - can be null/none
 */
private[spark] class ItemsReadOnlyTable(val sparkSession: SparkSession,
                                        val transforms: Array[Transform],
                                        val databaseName: Option[String],
                                        val containerName: Option[String],
                                        val userConfig: util.Map[String, String],
                                        val userProvidedSchema: Option[StructType] = None,
                                        val tableProperties: util.Map[String, String] =
                                          Collections.emptyMap[String, String])
  extends Table
    with SupportsRead {

  private[this] val tablePropertiesClone = Collections.unmodifiableMap[String, String](tableProperties)
  protected val diagnosticsConfig: DiagnosticsConfig = DiagnosticsConfig.parseDiagnosticsConfig(userConfig.asScala.toMap)

  @transient private lazy val log = LoggerHelper.getLogger(diagnosticsConfig, this.getClass)

  // This can only be used for data operation against a certain container.
  protected lazy val containerStateHandles: Broadcast[CosmosClientMetadataCachesSnapshots] =
    initializeAndBroadcastCosmosClientStatesForContainer()
  protected val effectiveUserConfig: Map[String, String] = CosmosConfig.getEffectiveConfig(
    databaseName,
    containerName,
    userConfig.asScala.toMap)
  protected val clientConfig: CosmosAccountConfig = CosmosAccountConfig.parseCosmosAccountConfig(effectiveUserConfig)
  protected val readConfig: CosmosReadConfig = CosmosReadConfig.parseCosmosReadConfig(effectiveUserConfig)
  protected val cosmosContainerConfig: CosmosContainerConfig =
    CosmosContainerConfig.parseCosmosContainerConfig(effectiveUserConfig, databaseName, containerName)
  //scalastyle:off multiple.string.literals
  protected val tableName: String = s"com.azure.cosmos.spark.items.${clientConfig.accountName}." +
    s"${cosmosContainerConfig.database}.${cosmosContainerConfig.container}"
  log.logInfo(s"Instantiated ${this.getClass.getSimpleName} for $tableName")
  //scalastyle:on multiple.string.literals

  override def name(): String = tableName

  override def properties(): util.Map[String, String] = {
    tablePropertiesClone
  }

  override def capabilities(): util.Set[TableCapability] = Set(
    TableCapability.ACCEPT_ANY_SCHEMA,
    TableCapability.BATCH_READ).asJava

  override def newScanBuilder(options: CaseInsensitiveStringMap): ScanBuilder = {
    log.logDebug(s"-->newScanBuilder $tableName")
    // TODO moderakh how options and userConfig should be merged? is there any difference?

    val effectiveOptions = Option.apply(options) match {
      case Some(optionsProvided) => effectiveUserConfig ++ optionsProvided.asScala
      case None => effectiveUserConfig
    }

    ItemsScanBuilder(sparkSession,
      new CaseInsensitiveStringMap(
        CosmosConfig.getEffectiveConfig(
          databaseName,
          containerName,
          effectiveOptions).asJava),
      schema(),
      containerStateHandles,
      diagnosticsConfig)
  }

  override def schema(): StructType = {
    val calledFrom = s"ItemReadOnlyTable($tableName).schema"
    Loan(
      List[Option[CosmosClientCacheItem]](
        Some(CosmosClientCache(
          CosmosClientConfiguration(effectiveUserConfig, useEventualConsistency = readConfig.forceEventualConsistency),
          None,
          calledFrom
        )),
        ThroughputControlHelper.getThroughputControlClientCacheItem(
          effectiveUserConfig,
          calledFrom,
          None)
      ))
      .to(clientCacheItems => userProvidedSchema.getOrElse(this.inferSchema(clientCacheItems(0).get, clientCacheItems(1), effectiveUserConfig)))
  }

  private def inferSchema(clientCacheItem: CosmosClientCacheItem,
                          throughputControlClientCacheItemOpt: Option[CosmosClientCacheItem],
                          userConfig: Map[String, String]): StructType = {

    CosmosTableSchemaInferrer.inferSchema(
      clientCacheItem,
      throughputControlClientCacheItemOpt,
      userConfig,
      ItemsTable.defaultSchemaForInferenceDisabled)
  }

  // This can be used only when databaseName and ContainerName are specified.
  private[spark] def initializeAndBroadcastCosmosClientStatesForContainer(): Broadcast[CosmosClientMetadataCachesSnapshots] = {
    val calledFrom = s"ItemReadOnlyTable($tableName).initializeAndBroadcastCosmosClientStateForContainer"
    Loan(
      List[Option[CosmosClientCacheItem]](
        Some(
          CosmosClientCache(
            CosmosClientConfiguration(effectiveUserConfig, useEventualConsistency = readConfig.forceEventualConsistency),
            None,
            calledFrom)),
        ThroughputControlHelper.getThroughputControlClientCacheItem(
          effectiveUserConfig,
          calledFrom,
          None)
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
}
