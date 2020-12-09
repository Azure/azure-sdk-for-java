// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import org.apache.spark.sql.connector.write.{BatchWrite, WriteBuilder}
import org.apache.spark.sql.util.CaseInsensitiveStringMap
// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

class CosmosWriterBuilder(userConfig: CaseInsensitiveStringMap) extends WriteBuilder with CosmosLoggingTrait {
  logInfo(s"Instantiated ${this.getClass.getSimpleName}")

  override def buildForBatch(): BatchWrite = new CosmosBatchWriter(userConfig.asCaseSensitiveMap().asScala.toMap)
}
