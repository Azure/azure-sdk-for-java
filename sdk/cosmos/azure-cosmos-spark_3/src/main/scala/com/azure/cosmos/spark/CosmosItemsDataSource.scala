// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.models.{CosmosItemIdentity, PartitionKey}
import com.azure.cosmos.spark.CosmosPredicates.assertOnSparkDriver
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import org.apache.spark.sql.{DataFrame, Row, SparkSession}

import java.util
import org.apache.spark.sql.connector.catalog.{Table, TableProvider}
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.sources.DataSourceRegister
import org.apache.spark.sql.types.{StringType, StructType}
import org.apache.spark.sql.util.CaseInsensitiveStringMap


// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

class CosmosItemsDataSource extends DataSourceRegister with TableProvider with BasicLoggingTrait {
  logTrace(s"Instantiated ${this.getClass.getSimpleName}")

  private lazy val sparkSession = {
    assertOnSparkDriver()
    SparkSession.active
  }

  /**
   * Infer the schema of the table identified by the given options.
   *
   * @param options an immutable case-insensitive string-to-string
   * @return StructType inferred schema
   */
  override def inferSchema(options: CaseInsensitiveStringMap): StructType = {
    new ItemsTable(sparkSession, Array.empty, None, None, options).schema()
  }

  /**
   * Represents the format that this data source provider uses.
   */
  override def shortName(): String = CosmosConstants.Names.ItemsDataSourceShortName

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
  override def getTable(schema: StructType, partitioning: Array[Transform], properties: util.Map[String, String]): Table = {
    // getTable - This is used for loading table with user specified schema and other transformations.
    new ItemsTable(
      sparkSession,
      partitioning,
      None,
      None,
      properties,
      Option.apply(schema))
  }

  /**
   * Returns true if the source has the ability of accepting external table metadata when getting
   * tables. The external table metadata includes user-specified schema from
   * `DataFrameReader`/`DataStreamReader` and schema/partitioning stored in Spark catalog.
   */
  override def supportsExternalMetadata(): Boolean = true
}

object CosmosItemsDataSource {
  /**
   * Easy way to validate the version of the Cosmos Data Source
   * @return the version of the Cosmos Data Source
   */
  def version : String = {
    CosmosConstants.currentVersion
  }

  def readMany(df: DataFrame, userConfig: java.util.Map[String, String]): DataFrame = {
    readMany(df, userConfig, null)
  }

  def readMany(df: DataFrame, userConfig: java.util.Map[String, String], userProvidedSchema: StructType): DataFrame = {
    val readManyReader = new CosmosReadManyReader(
      userProvidedSchema,
      userConfig.asScala.toMap)

    val idFieldExtraction = df
      .schema
      .find(field => field.name.equals(CosmosConstants.Properties.Id) && field.dataType.equals(StringType))
      .map(field => (row: Row) => {
        val id = row.getString(row.fieldIndex(field.name))
        new CosmosItemIdentity(new PartitionKey(id), id)
      })

    val itemIdentityFieldExtraction = df
      .schema
      .find(field => field.name.equals(CosmosConstants.Properties.ItemIdentity) && field.dataType.equals(StringType))
      .map(field => (row: Row) => CosmosItemIdentityHelper.tryParseCosmosItemIdentity(row.getString(row.fieldIndex(field.name))).get)

    val readManyFilterExtraction = itemIdentityFieldExtraction
      .getOrElse(idFieldExtraction.getOrElse((row: Row) => {
        val id = row.getString(0)
        new CosmosItemIdentity(new PartitionKey(id), id)
      }))

    readManyReader.readMany(df.rdd, readManyFilterExtraction)
  }
}
