// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import java.util
import com.azure.cosmos.spark.CosmosTableSchemaInferrer.{IdAttributeName, RawJsonBodyAttributeName, TimestampAttributeName}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.connector.catalog.{SupportsWrite, TableCapability}
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.connector.write.{LogicalWriteInfo, WriteBuilder}
import org.apache.spark.sql.types.{LongType, StringType, StructField, StructType}
import org.apache.spark.sql.util.CaseInsensitiveStringMap

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

private object ItemsTable {
  private[spark] val defaultSchemaForInferenceDisabled = StructType(Seq(
    StructField(RawJsonBodyAttributeName, StringType, nullable=false),
    StructField(IdAttributeName, StringType, nullable=false),
    StructField(TimestampAttributeName, LongType, nullable=false)
  ))
}

/**
 * ItemsTable is the entry point this is registered in the spark
 *
 * @param transforms         The specified table partitioning.
 * @param userConfig         The effective user configuration
 * @param userProvidedSchema The user provided schema - can be null/none
 */
private class ItemsTable(override val sparkSession: SparkSession,
                         override val transforms: Array[Transform],
                         override val databaseName: Option[String],
                         override val containerName: Option[String],
                         override val userConfig: util.Map[String, String],
                         override val userProvidedSchema: Option[StructType] = None)
  extends ItemsReadOnlyTable(sparkSession, transforms, databaseName, containerName, userConfig, userProvidedSchema)
  with SupportsWrite {

  override def capabilities(): util.Set[TableCapability] = Set(
    TableCapability.ACCEPT_ANY_SCHEMA,
    TableCapability.BATCH_WRITE,
    TableCapability.BATCH_READ,
    TableCapability.STREAMING_WRITE).asJava

  override def newWriteBuilder(logicalWriteInfo: LogicalWriteInfo): WriteBuilder = {
    // TODO: moderakh merge logicalWriteInfo config with other configs
    new ItemsWriterBuilder(
      new CaseInsensitiveStringMap(
        CosmosConfig.getEffectiveConfig(databaseName, containerName, this.effectiveUserConfig).asJava),
      logicalWriteInfo.schema(),
      containerStateHandle,
      diagnosticsConfig
    )
  }
}
