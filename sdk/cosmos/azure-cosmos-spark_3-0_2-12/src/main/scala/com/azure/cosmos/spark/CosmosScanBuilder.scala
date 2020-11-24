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

  var filtersToBePushDownToCosmos: Array[Filter] = Array.empty
  var filtersToBeEvaluatedBySpark: Array[Filter] = Array.empty

  /**
    * Pushes down filters, and returns filters that need to be evaluated after scanning.
    * @param filters
    * @return filters to be evaluated after scanning
    */
  override def pushFilters(filters: Array[Filter]): Array[Filter] = {
    // TODO moderakh we need to build the push down filter translation to Cosmos query
    // for now leave it to spark to filter

    // TODO: moderakh identify all the filters which are relevant to cosmos db
    this.filtersToBePushDownToCosmos = filters.filter(
      filter => filter match {
        case EqualTo(attribute, value) => true
        case _ => false
      }
    )
    this.filtersToBeEvaluatedBySpark = filters

    // return all filter so spark also applies the filters
    filters
  }

  /**
    * Returns the filters that are pushed to the data source via {@link #pushFilters ( Filter[ ] )}.
    * @return
    */
  override def pushedFilters: Array[Filter] = {
    this.filtersToBePushDownToCosmos
  }

  override def build(): Scan = {
    CosmosScan(config.asScala.toMap, buildQuery())
  }

  // TODO moderakh: the build query only supports the most trivial query
  def buildQuery(): String = {
    // TODO moderakh solidify the query builder
    val queryBuilder = new StringBuilder

    queryBuilder.append("SELECT * FROM r")

    if (filtersToBePushDownToCosmos.nonEmpty) {
      queryBuilder.append(" WHERE ")
    }

    // TODO: moderakh we should user parametrized query
    // SQL query injection attack

    for (filter <- filtersToBePushDownToCosmos) {
      filter match {
        case EqualTo(attribute, value) => {
          // TODO: moderakh how to handle column names? column names with special names?

          queryBuilder.append("r.").append(attribute).append(" = ").append(s"$value")
        }
        case _ => throw new Exception("unsupported")
      }
    }
    queryBuilder.toString
  }

  override def pruneColumns(requiredSchema: StructType): Unit = {
    // TODO moderakh add projection to the query
  }
}
