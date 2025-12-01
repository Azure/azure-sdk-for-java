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

  /**
   * Execute transactional batch operations against Cosmos DB.
   * Operations with the same partition key are executed atomically.
   * 
   * Input DataFrame schema:
   * - id: String (required)
   * - partitionKey: String (required)
   * - operationType: String (required - "create", "replace", "upsert", "delete", "read")
   * - document: String (optional - JSON document, required for create/replace/upsert)
   * 
   * Output DataFrame schema:
   * - id: String
   * - partitionKey: String
   * - operationType: String
   * - statusCode: Int
   * - success: Boolean
   * - resultDocument: String (optional - for read operations)
   * - errorMessage: String (optional)
   * 
   * @param df DataFrame containing batch operations
   * @param userConfig Cosmos DB configuration
   * @return DataFrame with execution results
   */
  def writeTransactionalBatch(df: DataFrame, userConfig: java.util.Map[String, String]): DataFrame = {
    writeTransactionalBatch(df, userConfig, null)
  }

  /**
   * Execute transactional batch operations against Cosmos DB with custom output schema.
   * 
   * @param df DataFrame containing batch operations
   * @param userConfig Cosmos DB configuration
   * @param userProvidedSchema Custom output schema (optional)
   * @return DataFrame with execution results
   */
  def writeTransactionalBatch(df: DataFrame, userConfig: java.util.Map[String, String], userProvidedSchema: StructType): DataFrame = {
    val batchWriter = new TransactionalBatchWriter(
      userProvidedSchema,
      userConfig.asScala.toMap)

    // Create default extraction function based on DataFrame schema
    val defaultOperationExtraction = createBatchOperationExtraction(df)

    batchWriter.writeTransactionalBatch(df.rdd, defaultOperationExtraction)
  }

  /**
   * Execute transactional batch operations with custom extraction logic.
   * This allows advanced users to provide their own logic for extracting batch operations from rows.
   * 
   * @param df DataFrame containing batch operations
   * @param operationExtraction Custom function to extract BatchOperation from Row
   * @param userConfig Cosmos DB configuration
   * @return DataFrame with execution results
   */
  def writeTransactionalBatch(
    df: DataFrame,
    operationExtraction: Row => BatchOperation,
    userConfig: java.util.Map[String, String]
  ): DataFrame = {
    writeTransactionalBatch(df, operationExtraction, userConfig, null)
  }

  /**
   * Execute transactional batch operations with custom extraction logic and output schema.
   * 
   * @param df DataFrame containing batch operations
   * @param operationExtraction Custom function to extract BatchOperation from Row
   * @param userConfig Cosmos DB configuration
   * @param userProvidedSchema Custom output schema (optional)
   * @return DataFrame with execution results
   */
  def writeTransactionalBatch(
    df: DataFrame,
    operationExtraction: Row => BatchOperation,
    userConfig: java.util.Map[String, String],
    userProvidedSchema: StructType
  ): DataFrame = {
    val batchWriter = new TransactionalBatchWriter(
      userProvidedSchema,
      userConfig.asScala.toMap)

    batchWriter.writeTransactionalBatch(df.rdd, operationExtraction)
  }

  /**
   * Creates default batch operation extraction function from DataFrame schema.
   * Follows the same pattern as standard Cosmos writes - converts flat DataFrame rows to JSON documents.
   * 
   * Supports schemas with or without operationType column:
   * - With operationType: Uses specified operation (create, upsert, replace, delete, read)
   * - Without operationType: Defaults all operations to "upsert"
   * 
   * @param df Input DataFrame
   * @return Extraction function that converts Row to BatchOperation
   */
  private def createBatchOperationExtraction(df: DataFrame): Row => BatchOperation = {
    val schema = df.schema
    val serializationConfig = CosmosSerializationConfig.parseSerializationConfig(Map.empty)
    
    // Check if operationType column exists
    val hasOperationType = schema.fields.exists(_.name == "operationType")
    val operationTypeIndex = if (hasOperationType) Some(schema.fieldIndex("operationType")) else None
    
    (row: Row) => {
      // Create row converter inside the lambda to avoid serialization issues
      // The serializationConfig (Map) is serializable, but CosmosRowConverter is not
      val rowConverter = CosmosRowConverter.get(serializationConfig)
      
      // Convert entire row to ObjectNode (just like standard writes)
      val documentNode = rowConverter.fromRowToObjectNode(row)
      
      // Remove operationType from the document if it was included
      // operationType is metadata for the batch operation, not part of the document
      if (hasOperationType) {
        documentNode.remove("operationType")
      }
      
      // Handle partitionKey column - if it exists, rename to "pk" for Cosmos DB
      val hasPartitionKeyColumn = schema.fieldNames.contains("partitionKey")
      if (hasPartitionKeyColumn) {
        val pkValue = documentNode.get("partitionKey")
        documentNode.remove("partitionKey")
        documentNode.set("pk", pkValue)
      }
      
      // Extract id and partition key from the document
      val idNode = documentNode.get(CosmosConstants.Properties.Id)
      if (idNode == null || idNode.isNull) {
        throw new IllegalArgumentException(
          s"Missing or null 'id' field in document. Each item must have a non-null 'id' property. Document: ${documentNode.toString}"
        )
      }
      val id = idNode.asText()
      
      // Extract partition key value for batch API
      val partitionKey = if (documentNode.has("pk")) {
        documentNode.get("pk").asText()
      } else {
        id // Fall back to id if no explicit partition key
      }
      
      // Get operation type from column or default to "upsert"
      val operationType = operationTypeIndex match {
        case Some(index) => row.getString(index)
        case None => "upsert"
      }
      
      BatchOperation(id, partitionKey, operationType, documentNode)
    }
  }
}
