// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import java.util

import org.apache.spark.sql.connector.catalog.{Table, TableProvider}
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.sources.DataSourceRegister
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.util.CaseInsensitiveStringMap

class CosmosItemsDataSource extends DataSourceRegister with TableProvider with CosmosLoggingTrait {
  logInfo(s"Instantiated ${this.getClass.getSimpleName}")

  /**
    * Infer the schema of the table identified by the given options.
    * @param options an immutable case-insensitive string-to-string
    * @return StructType inferred schema
    */
  override def inferSchema(options: CaseInsensitiveStringMap): StructType = {
    new CosmosTable(Array.empty, options).schema()
  }

  /**
    * Represents the format that this data source provider uses.
    */
  override def shortName(): String = "cosmos.items"

  /**
    * Return a {@link Table} instance with the specified table schema, partitioning and properties
    * to do read/write. The returned table should report the same schema and partitioning with the
    * specified ones, or Spark may fail the operation.
    *
    * @param schema The specified table schema.
    * @param partitioning The specified table partitioning.
    * @param properties The specified table properties. It's case preserving (contains exactly what
    *                   users specified) and implementations are free to use it case sensitively or
    *                   insensitively. It should be able to identify a table, e.g. file path, Kafka
    *                   topic name, etc.
    */
  override def getTable(schema: StructType, partitioning: Array[Transform], properties: util.Map[String, String]): Table = {
    // getTable - This is used for loading table with user specified schema and other transformations.
    new CosmosTable(partitioning, properties, Option.apply(schema))
  }

  /**
    * Returns true if the source has the ability of accepting external table metadata when getting
    * tables. The external table metadata includes user-specified schema from
    * `DataFrameReader`/`DataStreamReader` and schema/partitioning stored in Spark catalog.
    */
  override def supportsExternalMetadata(): Boolean = true
}
