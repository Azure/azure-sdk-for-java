// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.CosmosClientMetadataCachesSnapshot
import com.azure.cosmos.models.PartitionKey
import com.azure.cosmos.spark.CosmosTableSchemaInferer.{
  IdAttributeName,
  RawJsonBodyAttributeName,
  TimestampAttributeName
}
import com.azure.cosmos.{CosmosAsyncClient, CosmosClientBuilder, CosmosException}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.connector.catalog.{SupportsRead, SupportsWrite, Table, TableCapability}
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.connector.read.ScanBuilder
import org.apache.spark.sql.connector.write.{LogicalWriteInfo, WriteBuilder}
import org.apache.spark.sql.types.{LongType, StringType, StructField, StructType}
import org.apache.spark.sql.util.CaseInsensitiveStringMap
import org.codehaus.jackson.node.ObjectNode

import java.util
import java.util.UUID

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

private object CosmosTable {
  // TODO fabianm - ??? Should we also add a default "_pk" column, which would retrieve the
  //  pk value ???
  private[spark] val defaultSchemaForInferenceDisabled = StructType(Seq(
    StructField(RawJsonBodyAttributeName, StringType),
    StructField(IdAttributeName, StringType),
    StructField(TimestampAttributeName, LongType)
  ))
}

/**
 * CosmosTable is the entry point this is registered in the spark
 *
 * @param transforms         The specified table partitioning.
 * @param userConfig         The effective user configuration
 * @param userProvidedSchema The user provided schema - can be null/none
 */
private class CosmosTable(val transforms: Array[Transform],
                          val databaseName: Option[String],
                          val containerName: Option[String],
                          val userConfig: util.Map[String, String],
                          val userProvidedSchema: Option[StructType] = None)
  extends Table
    with SupportsWrite
    with SupportsRead
    with CosmosLoggingTrait {
  logInfo(s"Instantiated ${this.getClass.getSimpleName}")

  // This can only be used for data operation against a certain container.
  private lazy val containerStateHandle: Broadcast[CosmosClientMetadataCachesSnapshot] =
    initializeAndBroadcastCosmosClientStateForContainer()
  private val effectiveUserConfig = CosmosConfig.getEffectiveConfig(userConfig.asScala.toMap)
  private val clientConfig = CosmosAccountConfig.parseCosmosAccountConfig(effectiveUserConfig)
  private val cosmosContainerConfig =
    CosmosContainerConfig.parseCosmosContainerConfig(effectiveUserConfig, databaseName, containerName)
  private val tableName = s"com.azure.cosmos.spark.items.${clientConfig.accountName}." +
    s"${cosmosContainerConfig.database}.${cosmosContainerConfig.container}"
  private val client = new CosmosClientBuilder().endpoint(clientConfig.endpoint)
    .key(clientConfig.key)
    .buildAsyncClient()

  override def name(): String = tableName

  override def capabilities(): util.Set[TableCapability] = Set(
    // ACCEPT_ANY_SCHEMA is needed because of this bug https://github.com/apache/spark/pull/30273
    // It was fixed in Spark 3.1.0 but Databricks currently only supports 3.0.1
    TableCapability.ACCEPT_ANY_SCHEMA,
    TableCapability.BATCH_WRITE,
    TableCapability.BATCH_READ).asJava

  override def newScanBuilder(options: CaseInsensitiveStringMap): ScanBuilder = {
    // TODO moderakh how options and userConfig should be merged? is there any difference?
    CosmosScanBuilder(new CaseInsensitiveStringMap(CosmosConfig.getEffectiveConfig(options.asCaseSensitiveMap().asScala.toMap).asJava),
      schema(),
      containerStateHandle)
  }

  override def schema(): StructType = {
    userProvidedSchema.getOrElse(this.inferSchema(client, effectiveUserConfig))
  }

  private def inferSchema(client: CosmosAsyncClient,
                          userConfig: Map[String, String]): StructType = {

    CosmosTableSchemaInferer.inferSchema(
      client,
      userConfig,
      CosmosTable.defaultSchemaForInferenceDisabled)
  }

  override def newWriteBuilder(logicalWriteInfo: LogicalWriteInfo): WriteBuilder = {
    // TODO: moderakh merge logicalWriteInfo config with other configs
    new CosmosWriterBuilder(
      new CaseInsensitiveStringMap(CosmosConfig.getEffectiveConfig(userConfig.asScala.toMap).asJava),
      logicalWriteInfo.schema(),
      containerStateHandle
    )
  }

  // This can be used only when databaseName and ContainerName are specified.
  private[spark] def initializeAndBroadcastCosmosClientStateForContainer(): Broadcast[CosmosClientMetadataCachesSnapshot] = {
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
