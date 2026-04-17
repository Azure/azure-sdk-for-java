// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.models.{CosmosItemIdentity, PartitionKey, PartitionKeyBuilder}
import com.azure.cosmos.spark.CosmosPredicates.assertOnSparkDriver
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.azure.cosmos.{SparkBridgeInternal}
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

  def readManyByPartitionKey(df: DataFrame, userConfig: java.util.Map[String, String]): DataFrame = {
    readManyByPartitionKey(df, userConfig, null)
  }

  def readManyByPartitionKey(
    df: DataFrame,
    userConfig: java.util.Map[String, String],
    userProvidedSchema: StructType): DataFrame = {

    val readManyReader = new CosmosReadManyByPartitionKeyReader(
      userProvidedSchema,
      userConfig.asScala.toMap)

    // Option 1: Look for the _partitionKeyIdentity column (produced by GetCosmosPartitionKeyValue UDF)
    val pkIdentityFieldExtraction = df
      .schema
      .find(field => field.name.equals(CosmosConstants.Properties.PartitionKeyIdentity) && field.dataType.equals(StringType))
      .map(field => (row: Row) =>
        CosmosPartitionKeyHelper.tryParsePartitionKey(row.getString(row.fieldIndex(field.name))).get)

    // Option 2: Detect PK columns by matching the container's partition key paths against the DataFrame schema
    val pkColumnExtraction: Option[Row => PartitionKey] = if (pkIdentityFieldExtraction.isDefined) {
      None // no need to resolve PK paths - _partitionKeyIdentity column takes precedence
    } else {
      val effectiveConfig = CosmosConfig.getEffectiveConfig(
        databaseName = None,
        containerName = None,
        userConfig.asScala.toMap)
      val readConfig = CosmosReadConfig.parseCosmosReadConfig(effectiveConfig)
      val containerConfig = CosmosContainerConfig.parseCosmosContainerConfig(effectiveConfig)
      val sparkEnvironmentInfo = CosmosClientConfiguration.getSparkEnvironmentInfo(None)
      val calledFrom = s"CosmosItemsDataSource.readManyByPartitionKey"
      val treatNullAsNone = readConfig.readManyByPkTreatNullAsNone

      val pkPaths = Loan(
        List[Option[CosmosClientCacheItem]](
          Some(
            CosmosClientCache(
              CosmosClientConfiguration(
                effectiveConfig,
                readConsistencyStrategy = readConfig.readConsistencyStrategy,
                sparkEnvironmentInfo),
              None,
              calledFrom)),
          ThroughputControlHelper.getThroughputControlClientCacheItem(
            effectiveConfig,
            calledFrom,
            None,
            sparkEnvironmentInfo)
        ))
        .to(clientCacheItems => {
          val container =
            ThroughputControlHelper.getContainer(
              effectiveConfig,
              containerConfig,
              clientCacheItems(0).get,
              clientCacheItems(1))

          val pkDefinition = SparkBridgeInternal
            .getContainerPropertiesFromCollectionCache(container)
            .getPartitionKeyDefinition

          pkDefinition.getPaths.asScala.map(_.stripPrefix("/")).toList
        })

      // Check if ALL PK path columns exist in the DataFrame schema
      val dfFieldNames = df.schema.fieldNames.toSet
      val allPkColumnsPresent = pkPaths.forall(path => dfFieldNames.contains(path))

      if (allPkColumnsPresent && pkPaths.nonEmpty) {
        // pkPaths already defined above
        Some((row: Row) => {
          if (pkPaths.size == 1) {
            // Single partition key
            buildPartitionKey(row.getAs[Any](pkPaths.head), treatNullAsNone)
          } else {
            // Hierarchical partition key — build level by level
            val builder = new PartitionKeyBuilder()
            for (path <- pkPaths) {
              addPartitionKeyComponent(builder, row.getAs[Any](path), treatNullAsNone)
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
            "or ensure the DataFrame contains columns matching the container's partition key paths."))

    readManyReader.readManyByPartitionKey(df.rdd, pkExtraction)
  }

  private def addPartitionKeyComponent(builder: PartitionKeyBuilder, value: Any, treatNullAsNone: Boolean): Unit = {
    value match {
      case s: String => builder.add(s)
      case n: Number => builder.add(n.doubleValue())
      case b: Boolean => builder.add(b)
      case null =>
        if (treatNullAsNone) builder.addNoneValue()
        else builder.addNullValue()
      case other => builder.add(other.toString)
    }
  }

  private def buildPartitionKey(value: Any, treatNullAsNone: Boolean): PartitionKey = {
    val builder = new PartitionKeyBuilder()
    addPartitionKeyComponent(builder, value, treatNullAsNone)
    builder.build()
  }
}
