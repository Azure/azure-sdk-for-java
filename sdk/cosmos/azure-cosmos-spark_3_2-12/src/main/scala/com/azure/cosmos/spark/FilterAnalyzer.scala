// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.models.PartitionKeyDefinition
import org.apache.spark.sql.sources.Filter

private case class FilterAnalyzer(
                                   cosmosReadConfig: CosmosReadConfig,
                                   partitionKeyDefinition: PartitionKeyDefinition) {
  private lazy val readManyFilterAnalyzer = ReadManyFilterAnalyzer(cosmosReadConfig, partitionKeyDefinition)
  private lazy val queryFilterAnalyzer = QueryFilterAnalyzer(cosmosReadConfig)

  def analyze(filters: Array[Filter]): AnalyzedAggregatedFilters = {

    if (cosmosReadConfig.customQuery.isDefined) {
      AnalyzedAggregatedFilters(
        cosmosReadConfig.customQuery.get,
        true,
        Array.empty[Filter],
        filters,
        None)
    } else {
      // The filters are exclusive to each other
      // calling the filter here from more specific to more generic
      var analyzedReadManyFilters = Option.empty[AnalyzedReadManyFilters]
      if (cosmosReadConfig.readManyFilteringConfig.readManyFilteringEnabled) {
        analyzedReadManyFilters = Some(readManyFilterAnalyzer.analyze(filters))
      }

      if (analyzedReadManyFilters.isEmpty || analyzedReadManyFilters.get.filtersToBePushedDownToCosmos.isEmpty) {
        // readMany filter can not be applied, fallback to use the query filter
        val analyzedQueryFilters = queryFilterAnalyzer.analyze(filters)
        AnalyzedAggregatedFilters(
          analyzedQueryFilters.cosmosParametrizedQuery,
          false,
          analyzedQueryFilters.filtersToBePushedDownToCosmos,
          analyzedQueryFilters.filtersNotSupportedByCosmos,
          None)
      } else {
        // will use readMany filter
        AnalyzedAggregatedFilters(
          QueryFilterAnalyzer.rootParameterizedQuery,
          false,
          analyzedReadManyFilters.get.filtersToBePushedDownToCosmos,
          analyzedReadManyFilters.get.filtersNotSupportedByCosmos,
          analyzedReadManyFilters.get.readManyFiltersOpt)
      }
    }
  }
}
