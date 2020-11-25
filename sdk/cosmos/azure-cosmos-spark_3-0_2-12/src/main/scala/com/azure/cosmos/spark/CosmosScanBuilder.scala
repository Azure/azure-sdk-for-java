// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import org.apache.spark.sql.connector.read.{Batch, InputPartition, PartitionReaderFactory,
  Scan, ScanBuilder, SupportsPushDownFilters, SupportsPushDownRequiredColumns}
import org.apache.spark.sql.sources.{EqualTo, Filter}
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.sql.util.CaseInsensitiveStringMap

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

case class CosmosScanBuilder(config: CaseInsensitiveStringMap)
  extends ScanBuilder
    with SupportsPushDownFilters
    with SupportsPushDownRequiredColumns
    with CosmosLoggingTrait {
  logInfo(s"Instantiated ${this.getClass.getSimpleName}")

  var processedPredicates : Option[AnalyzedFilters] = Option.empty

  /**
    * Pushes down filters, and returns filters that need to be evaluated after scanning.
    * @param filters pushed down filters.
    * @return the filters that spark need to evaluate
    */
  override def pushFilters(filters: Array[Filter]): Array[Filter] = {
    processedPredicates = Option.apply(FilterAnalyzer().analyze(filters))

    // return the filters that spark need to evaluate
    this.processedPredicates.get.filtersNotSupportedByCosmos
  }

  /**
    * Returns the filters that are pushed to Cosmos as query predicates
    * @return filters to be pushed to cosmos db.
    */
  override def pushedFilters: Array[Filter] = {
    if (this.processedPredicates.isDefined) {
      this.processedPredicates.get.filtersToBePushedDownToCosmos
    } else {
      Array[Filter]()
    }
  }

  override def build(): Scan = {
    assert(this.processedPredicates.isDefined)
    CosmosScan(config.asScala.toMap, this.processedPredicates.get.cosmosParametrizedQuery)
  }

  override def pruneColumns(requiredSchema: StructType): Unit = {
    // TODO moderakh add projection to the query
  }
}
