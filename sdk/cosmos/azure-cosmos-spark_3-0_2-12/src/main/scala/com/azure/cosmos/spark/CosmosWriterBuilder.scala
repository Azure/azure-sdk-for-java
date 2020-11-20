// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import org.apache.spark.sql.connector.write.{BatchWrite, WriteBuilder}

class CosmosWriterBuilder(userConfig: Map[String, String]) extends WriteBuilder with CosmosLoggingTrait {
  logInfo(s"Instantiated ${this.getClass.getSimpleName}")

  override def buildForBatch(): BatchWrite = new CosmosBatchWriter(userConfig: Map[String, String])
}
