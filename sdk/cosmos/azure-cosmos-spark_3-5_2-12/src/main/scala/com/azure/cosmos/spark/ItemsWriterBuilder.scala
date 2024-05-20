// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.spark.diagnostics.LoggerHelper
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.connector.metric.CustomMetric
import org.apache.spark.sql.connector.write.streaming.StreamingWrite
import org.apache.spark.sql.connector.write.{BatchWrite, Write, WriteBuilder}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.util.CaseInsensitiveStringMap

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

private class ItemsWriterBuilder
(
  userConfig: CaseInsensitiveStringMap,
  inputSchema: StructType,
  cosmosClientStateHandles: Broadcast[CosmosClientMetadataCachesSnapshots],
  diagnosticsConfig: DiagnosticsConfig,
  sparkEnvironmentInfo: String
)
  extends WriteBuilder {
  @transient private lazy val log = LoggerHelper.getLogger(diagnosticsConfig, this.getClass)
  log.logTrace(s"Instantiated ${this.getClass.getSimpleName}")

  override def build(): Write = {
    new CosmosWrite
  }

  override def buildForBatch(): BatchWrite =
    new ItemsBatchWriter(
      userConfig.asCaseSensitiveMap().asScala.toMap,
      inputSchema,
      cosmosClientStateHandles,
      diagnosticsConfig,
      sparkEnvironmentInfo)

  override def buildForStreaming(): StreamingWrite =
    new ItemsBatchWriter(
      userConfig.asCaseSensitiveMap().asScala.toMap,
      inputSchema,
      cosmosClientStateHandles,
      diagnosticsConfig,
      sparkEnvironmentInfo)

  private class CosmosWrite extends Write {

    private[this] val supportedCosmosMetrics: Array[CustomMetric] = {
      Array(
        new CosmosBytesWrittenMetric(),
        new CosmosRecordsWrittenMetric(),
        new TotalRequestChargeMetric()
      )
    }

    override def toBatch(): BatchWrite =
      new ItemsBatchWriter(
        userConfig.asCaseSensitiveMap().asScala.toMap,
        inputSchema,
        cosmosClientStateHandles,
        diagnosticsConfig,
        sparkEnvironmentInfo)

    override def toStreaming: StreamingWrite =
      new ItemsBatchWriter(
        userConfig.asCaseSensitiveMap().asScala.toMap,
        inputSchema,
        cosmosClientStateHandles,
        diagnosticsConfig,
        sparkEnvironmentInfo)

    override def supportedCustomMetrics(): Array[CustomMetric] = supportedCosmosMetrics
  }
}
