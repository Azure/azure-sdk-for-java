// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.CosmosClientMetadataCachesSnapshot
import com.azure.cosmos.spark.diagnostics.LoggerHelper
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.connector.read.{Scan, ScanBuilder, SupportsPushDownFilters, SupportsPushDownRequiredColumns}
import org.apache.spark.sql.sources.Filter
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.util.CaseInsensitiveStringMap

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

private case class ChangeFeedScanBuilder
(
  session: SparkSession,
  config: CaseInsensitiveStringMap,
  inputSchema: StructType,
  cosmosClientStateHandle: Broadcast[CosmosClientMetadataCachesSnapshot],
  diagnosticsConfig: DiagnosticsConfig
)
  extends ScanBuilder
    with SupportsPushDownFilters
    with SupportsPushDownRequiredColumns {

  @transient private lazy val log = LoggerHelper.getLogger(diagnosticsConfig, this.getClass)

  log.logTrace(s"Instantiated ${this.getClass.getSimpleName}")

  /**
   * Pushes down filters, and returns filters that need to be evaluated after scanning.
   * @param filters pushed down filters.
   * @return the filters that spark need to evaluate
   */
  override def pushFilters(filters: Array[Filter]): Array[Filter] = {
    filters
  }

  /**
   * Returns the filters that are pushed to Cosmos as query predicates
   * @return filters to be pushed to cosmos db.
   */
  override def pushedFilters: Array[Filter] = {
    Array[Filter]()
  }

  override def build(): Scan = {
    ChangeFeedScan(
      session,
      inputSchema,
      config.asScala.toMap,
      cosmosClientStateHandle,
      diagnosticsConfig)
  }

  /**
   * Applies column pruning w.r.t. the given requiredSchema.
   *
   * Implementation should try its best to prune the unnecessary columns or nested fields, but it's
   * also OK to do the pruning partially, e.g., a data source may not be able to prune nested
   * fields, and only prune top-level columns.
   *
   * Note that, `Scan` implementation should take care of the column
   * pruning applied here.
   */
  override def pruneColumns(requiredSchema: StructType): Unit = {
    //   Column pruning is not really applicable for change feed
    //   there is no way to push down column-pruning to the backend
    //   so entire json documents (including previous image for full fidelity)
    //   will be retrieved from the backend and any column pruning would only
    //   happen on the client
  }
}
