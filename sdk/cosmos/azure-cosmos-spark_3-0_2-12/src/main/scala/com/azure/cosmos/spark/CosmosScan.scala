// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.models.CosmosParametrizedQuery
import org.apache.spark.sql.connector.read.{Batch, InputPartition, PartitionReaderFactory, Scan}
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}

case class CosmosScan(schema: StructType,
                      config: Map[String, String], cosmosQuery: CosmosParametrizedQuery)
  extends Scan
    with Batch
    with CosmosLoggingTrait {
  logInfo(s"Instantiated ${this.getClass.getSimpleName}")

  /**
    * Returns the actual schema of this data source scan, which may be different from the physical
    * schema of the underlying storage, as column pruning or other optimizations may happen.
    */
  override def readSchema(): StructType = {
    schema
  }

  override def planInputPartitions(): Array[InputPartition] = {
    // TODO: moderakh use get feed range?
    // for now we are returning one partition hence only one spark task will be created.
    Array(CosmosInputPartition())
  }

  override def createReaderFactory(): PartitionReaderFactory = {
    CosmosScanPartitionReaderFactory(config, schema, cosmosQuery)
  }

  override def toBatch: Batch = {
    // TODO: moderakh should we refactor Bath to a new class? of should it be here?
    this
  }
}
