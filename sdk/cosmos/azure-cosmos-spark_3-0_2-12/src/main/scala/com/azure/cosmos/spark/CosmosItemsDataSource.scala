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

  override def inferSchema(caseInsensitiveStringMap: CaseInsensitiveStringMap): StructType = {
    getTable(null,
      Array.empty[Transform],
      caseInsensitiveStringMap.asCaseSensitiveMap()).schema()
  }

  override def shortName(): String = "cosmos.items"

  override def getTable(structType: StructType, transforms: Array[Transform], map: util.Map[String, String]): Table = {
    // getTable - This is used for loading table with user specified schema and other transformations.
    new CosmosTable(structType, transforms, map)
  }
}
