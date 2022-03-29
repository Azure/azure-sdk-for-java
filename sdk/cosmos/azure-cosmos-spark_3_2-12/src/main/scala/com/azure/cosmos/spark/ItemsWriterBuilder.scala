// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.implementation.CosmosClientMetadataCachesSnapshot
import com.azure.cosmos.spark.diagnostics.LoggerHelper
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.connector.write.streaming.StreamingWrite
import org.apache.spark.sql.connector.write.{BatchWrite, WriteBuilder}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.util.CaseInsensitiveStringMap
// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

private class ItemsWriterBuilder
(
  userConfig: CaseInsensitiveStringMap,
  inputSchema: StructType,
  cosmosClientStateHandle: Broadcast[CosmosClientMetadataCachesSnapshot],
  diagnosticsConfig: DiagnosticsConfig
)
  extends WriteBuilder {
  @transient private lazy val log = LoggerHelper.getLogger(diagnosticsConfig, this.getClass)
  log.logInfo(s"Instantiated ${this.getClass.getSimpleName}")

  override def buildForBatch(): BatchWrite =
    new ItemsBatchWriter(
      userConfig.asCaseSensitiveMap().asScala.toMap,
      inputSchema,
      cosmosClientStateHandle,
      diagnosticsConfig)

  override def buildForStreaming(): StreamingWrite =
    new ItemsBatchWriter(
      userConfig.asCaseSensitiveMap().asScala.toMap,
      inputSchema,
      cosmosClientStateHandle,
      diagnosticsConfig)
}
