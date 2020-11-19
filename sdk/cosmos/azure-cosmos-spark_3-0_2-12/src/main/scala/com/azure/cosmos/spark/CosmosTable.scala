// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import java.util

import org.apache.spark.sql.connector.catalog.{SupportsWrite, Table, TableCapability}
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.connector.write.{LogicalWriteInfo, WriteBuilder}
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

/**
 * CosmosTable is the entry point this is registered in the spark
 * @param userProvidedSchema
 * @param transforms
 * @param userConfig
 */
class CosmosTable(val userProvidedSchema: StructType,
                  val transforms: Array[Transform],
                  val userConfig: util.Map[String, String])
  extends Table with SupportsWrite with CosmosLoggingTrait {
  logInfo(s"Instantiated ${this.getClass.getSimpleName}")

  override def name(): String = "com.azure.cosmos.spark.write"

  override def schema(): StructType = {
    // TODO: moderakh add support for schema inference
    // for now schema is hard coded to make TestE2EMain to work
    StructType(Seq(StructField("number", IntegerType), StructField("word", StringType)))
  }

  override def capabilities(): util.Set[TableCapability] = Set(TableCapability.BATCH_WRITE).asJava

  override def newWriteBuilder(logicalWriteInfo: LogicalWriteInfo): WriteBuilder = new CosmosWriterBuilder(userConfig.asScala.toMap)

}
