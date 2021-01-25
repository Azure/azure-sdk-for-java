// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import java.util
import java.util.UUID

import com.azure.cosmos.implementation.{CosmosClientMetadataCachesSnapshot, RxDocumentClientImpl}
import com.azure.cosmos.models.PartitionKey
import com.azure.cosmos.{CosmosBridgeInternal, CosmosClientBuilder, CosmosException}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.connector.catalog.{SupportsRead, SupportsWrite, Table, TableCapability}
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.connector.read.ScanBuilder
import org.apache.spark.sql.connector.write.{LogicalWriteInfo, WriteBuilder}
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.sql.util.CaseInsensitiveStringMap
import org.codehaus.jackson.node.ObjectNode
import org.json4s.scalap.scalasig.ClassFileParser.state
import reactor.core.publisher.Mono

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

/**
 * CosmosTable is the entry point this is registered in the spark
 * @param userProvidedSchema
 * @param transforms
 * @param userConfig
 */
class CosmosTable(val transforms: Array[Transform],
                  val userConfig: util.Map[String, String],
                  val userProvidedSchema: Option[StructType] = None)
  extends Table
    with SupportsWrite
    with SupportsRead
    with CosmosLoggingTrait {
  logInfo(s"Instantiated ${this.getClass.getSimpleName}")

  val clientConfig = CosmosAccountConfig.parseCosmosAccountConfig(CosmosConfig.getEffectiveConfig(userConfig.asScala.toMap))
  val client = new CosmosClientBuilder().endpoint(clientConfig.endpoint)
    .key(clientConfig.key)
    .buildAsyncClient()

  // This can only be used for data operation against a certain container.
  lazy val containerStateHandle : Broadcast[CosmosClientMetadataCachesSnapshot] = initializeAndBroadcastCosmosClientStateForContainer

  // This can be used only when databaseName and ContainerName are specified.
  def initializeAndBroadcastCosmosClientStateForContainer() : Broadcast[CosmosClientMetadataCachesSnapshot] = {
    val cosmosContainerConfig = CosmosContainerConfig.parseCosmosContainerConfig(userConfig.asScala.toMap)
    try {
      client.getDatabase(cosmosContainerConfig.database).getContainer(cosmosContainerConfig.container).readItem(
        UUID.randomUUID().toString, new PartitionKey(UUID.randomUUID().toString), classOf[ObjectNode])
        .block()
    } catch {
      case e: CosmosException => None
    }

    val state = new CosmosClientMetadataCachesSnapshot()
    state.serialize(client)

    val sparkSession = SparkSession.active
    sparkSession.sparkContext.broadcast(state)
  }

  // TODO: FIXME moderakh
  // A name to identify this table. Implementations should provide a meaningful name, like the
  // database and table name from catalog, or the location of files for this table.
  override def name(): String = "com.azure.cosmos.spark.write"

  override def schema(): StructType = {
    userProvidedSchema.getOrElse(CosmosTableSchemaInferer.inferSchema())
  }

  override def capabilities(): util.Set[TableCapability] = Set(
    TableCapability.BATCH_WRITE,
    TableCapability.BATCH_READ).asJava

  override def newScanBuilder(options: CaseInsensitiveStringMap): ScanBuilder = {
    // TODO moderakh how options and userConfig should be merged? is there any difference?
    CosmosScanBuilder(new CaseInsensitiveStringMap(CosmosConfig.getEffectiveConfig(options.asCaseSensitiveMap().asScala.toMap).asJava),
      schema(),
      containerStateHandle)
  }

  override def newWriteBuilder(logicalWriteInfo: LogicalWriteInfo): WriteBuilder = {
    // TODO: moderakh merge logicalWriteInfo config with other configs
    new CosmosWriterBuilder(
      new CaseInsensitiveStringMap(CosmosConfig.getEffectiveConfig(userConfig.asScala.toMap).asJava),
      logicalWriteInfo.schema(),
      containerStateHandle
    )
  }
}
