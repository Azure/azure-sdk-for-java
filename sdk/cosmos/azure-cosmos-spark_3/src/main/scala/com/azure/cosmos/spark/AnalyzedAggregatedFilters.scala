// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.models.CosmosParameterizedQuery
import org.apache.spark.sql.sources.Filter

private[spark] case class AnalyzedAggregatedFilters(
                                              cosmosParametrizedQuery: CosmosParameterizedQuery,
                                              isCustomQuery: Boolean,
                                              filtersToBePushedDownToCosmos: Array[Filter],
                                              filtersNotSupportedByCosmos: Array[Filter],
                                              readManyFiltersOpt: Option[List[ReadManyFilter]])
