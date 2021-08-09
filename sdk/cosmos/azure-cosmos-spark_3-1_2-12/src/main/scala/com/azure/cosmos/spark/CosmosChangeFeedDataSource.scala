// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.spark.CosmosPredicates.assertOnSparkDriver
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.connector.catalog.{Table, TableProvider}
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.sources.DataSourceRegister
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.util.CaseInsensitiveStringMap

import java.util
import java.util.Collections
import scala.collection.immutable.Map

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

class CosmosChangeFeedDataSource
  extends DataSourceRegister
    with TableProvider
    with BasicLoggingTrait {

  logTrace(s"Instantiated ${this.getClass.getSimpleName}")

  /**
   * Infer the schema of the table identified by the given options.
   *
   * @param options an immutable case-insensitive string-to-string
   * @return StructType inferred schema
   */
  override def inferSchema(options: CaseInsensitiveStringMap): StructType = {
    assertOnSparkDriver()
    val session = SparkSession.active
    new ChangeFeedTable(session, Array.empty, options).schema()
  }

  /**
   * Represents the format that this data source provider uses.
   */
  override def shortName(): String = CosmosConstants.Names.ChangeFeedDataSourceShortName

  /**
   * Return a `Table` instance with the specified table schema, partitioning and properties
   * to do read/write. The returned table should report the same schema and partitioning with the
   * specified ones, or Spark may fail the operation.
   *
   * @param schema       The specified table schema.
   * @param partitioning The specified table partitioning.
   * @param properties   The specified table properties. It's case preserving (contains exactly what
   *                     users specified) and implementations are free to use it case sensitively or
   *                     insensitively. It should be able to identify a table, e.g. file path, Kafka
   *                     topic name, etc.
   */
  override def getTable(schema: StructType,
                        partitioning: Array[Transform],
                        properties: util.Map[String, String]): Table = {

    assertOnSparkDriver()
    val session = SparkSession.active
    // getTable - This is used for loading table with user specified schema and other transformations.
    new ChangeFeedTable(
      session,
      partitioning,
      CosmosConfig.getEffectiveConfig(None, None, properties.asScala.toMap).asJava,
      Option.apply(schema))
  }

  /**
   * Returns true if the source has the ability of accepting external table metadata when getting
   * tables. The external table metadata includes user-specified schema from
   * `DataFrameReader`/`DataStreamReader` and schema/partitioning stored in Spark catalog.
   */
  override def supportsExternalMetadata(): Boolean = true
}
