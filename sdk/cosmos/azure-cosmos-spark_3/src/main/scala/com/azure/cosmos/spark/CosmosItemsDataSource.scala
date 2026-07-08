// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.models.{CosmosItemIdentity, PartitionKey, PartitionKeyBuilder}
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

  def readManyByPartitionKeys(df: DataFrame, userConfig: java.util.Map[String, String]): DataFrame = {
    readManyByPartitionKeys(df, userConfig, null)
  }

  def readManyByPartitionKeys(
    df: DataFrame,
    userConfig: java.util.Map[String, String],
    userProvidedSchema: StructType): DataFrame = {

    val readManyReader = new CosmosReadManyByPartitionKeyReader(
      userProvidedSchema,
      userConfig.asScala.toMap)

    // Initialize reader state once: resolves PK paths, infers schema, broadcasts client caches,
    // and returns the resolved treatNullAsNone flag from the reader's parsed config - avoiding
    // duplicate config parsing between the data source and the reader.
    val readerState = readManyReader.initializeReaderState()
    val (pkPaths, _, _, sharedTreatNullAsNone) = readerState

    // Option 1: Look for the _partitionKeyIdentity column (produced by GetCosmosPartitionKeyValue UDF)
    val pkIdentityFieldExtraction = df
      .schema
      .find(field => field.name.equals(CosmosConstants.Properties.PartitionKeyIdentity) && field.dataType.equals(StringType))
      .map(field => (row: Row) => {
        val rawValue = row.getString(row.fieldIndex(field.name))
        CosmosPartitionKeyHelper.tryParsePartitionKey(rawValue, sharedTreatNullAsNone)
          .getOrElse(throw new IllegalArgumentException(
            s"Invalid _partitionKeyIdentity value in row: '$rawValue'. " +
              "Expected format: pk([...json...])"))
      })

    // Option 2: Detect PK columns by matching the container's partition key paths against the DataFrame schema
    val pkColumnExtraction: Option[Row => PartitionKey] = if (pkIdentityFieldExtraction.isDefined) {
      None // no need to resolve PK paths - _partitionKeyIdentity column takes precedence
    } else {
      val treatNullAsNone = sharedTreatNullAsNone

      // Nested PK paths (containing /) cannot be resolved from top-level DataFrame columns.
      if (pkPaths.exists(_.contains("/"))) {
        throw new IllegalArgumentException(
          "Container has nested partition key path(s) " + pkPaths.mkString("[", ",", "]") + ". " +
            "Nested paths cannot be resolved from DataFrame columns automatically - add a " +
            "'_partitionKeyIdentity' column produced by the GetCosmosPartitionKeyValue UDF.")
      }

      // Allow DataFrames to provide a contiguous top-level prefix of the container's
      // hierarchical partition key paths. For example: tenant, or tenant + region.
      val dfFieldNames = df.schema.fieldNames.toSet
      val matchedPrefix = pkPaths.takeWhile(path => dfFieldNames.contains(path))
      val hasNonPrefixMatch = pkPaths.drop(matchedPrefix.size).exists(path => dfFieldNames.contains(path))

      if (hasNonPrefixMatch) {
        throw new IllegalArgumentException(
          "DataFrame columns matching the container's partition key paths must form a contiguous top-level prefix " +
            "(for example: tenant, or tenant + region). " +
            "For nested or non-prefix partition key extraction, add a '_partitionKeyIdentity' column produced " +
            "by the GetCosmosPartitionKeyValue UDF.")
      }

      if (matchedPrefix.nonEmpty) {
        Some((row: Row) => {
          if (matchedPrefix.size == 1) {
            buildPartitionKey(row.getAs[Any](matchedPrefix.head), treatNullAsNone)
          } else {
            val builder = new PartitionKeyBuilder()
            for (path <- matchedPrefix) {
              addPartitionKeyComponent(builder, row.getAs[Any](path), treatNullAsNone, matchedPrefix.size)
            }
            builder.build()
          }
        })
      } else {
        None
      }
    }

    val pkExtraction = pkIdentityFieldExtraction
      .orElse(pkColumnExtraction)
      .getOrElse(
        throw new IllegalArgumentException(
          "Cannot determine partition key extraction from the input DataFrame. " +
            "Either add a '_partitionKeyIdentity' column (using the GetCosmosPartitionKeyValue UDF) " +
            "or ensure the DataFrame contains columns matching a top-level prefix of the container's partition key paths."))

    readManyReader.readManyByPartitionKeys(df.rdd, pkExtraction, readerState)
  }

  private def addPartitionKeyComponent(
    builder: PartitionKeyBuilder,
    value: Any,
    treatNullAsNone: Boolean,
    partitionKeyComponentCount: Int): Unit = {
    value match {
      case s: String => builder.add(s)
      case n: Number => builder.add(n.doubleValue())
      case b: Boolean => builder.add(b)
      case null =>
        CosmosPartitionKeyHelper.validateNoneHandlingForPartitionKeyComponentCount(
          partitionKeyComponentCount,
          treatNullAsNone)
        if (treatNullAsNone) builder.addNoneValue()
        else builder.addNullValue()
      case other =>
        // Reject unknown types rather than silently .toString-ing them - the document field
        // was stored with its original type and a stringified value will never match.
        // Supported types: String, Number (Byte/Short/Int/Long/Float/Double/BigDecimal), Boolean, null.
        throw new IllegalArgumentException(
          s"Unsupported partition key column type '${other.getClass.getName}' with value '$other'. " +
            "Supported types are String, Number (integral or floating-point), Boolean, and null. " +
            "For other source types, convert the column before calling readManyByPartitionKeys or use " +
            "the GetCosmosPartitionKeyValue UDF to produce a '_partitionKeyIdentity' column.")
    }
  }

  private def buildPartitionKey(value: Any, treatNullAsNone: Boolean): PartitionKey = {
    val builder = new PartitionKeyBuilder()
    addPartitionKeyComponent(builder, value, treatNullAsNone, 1)
    builder.build()
  }
}
