// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import org.apache.spark.sql.connector.write.{BatchWrite, WriteBuilder}
import org.apache.spark.sql.types.StructType

class CosmosWriterBuilder(userConfig: Map[String, String], inputSchema: StructType) extends WriteBuilder with CosmosLoggingTrait {
  logInfo(s"Instantiated ${this.getClass.getSimpleName}")

  override def buildForBatch(): BatchWrite = new CosmosBatchWriter(userConfig: Map[String, String], inputSchema)
}
