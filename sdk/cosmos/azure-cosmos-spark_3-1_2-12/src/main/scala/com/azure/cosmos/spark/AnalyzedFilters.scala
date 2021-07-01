// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.models.CosmosParameterizedQuery
import org.apache.spark.sql.sources.Filter

// TODO: moderakh, thought for future optimization:
//  if we can identify if the user filter is a equality on cosmos partitionKeyValue
//  then we can set partitionKeyValue in the CosmosQueryOption
//  the benefit is that if the partitionKeyValue is set in the CosmosQueryOption
//  the antlr query parsing support can eliminate the need for query plan fetch from GW
//  partitionKeyValue would also be the only filter I would consider as an option for
//  pushing down filters to change feed
private case class AnalyzedFilters(cosmosParametrizedQuery: CosmosParameterizedQuery,
                                   filtersToBePushedDownToCosmos: Array[Filter],
                                   filtersNotSupportedByCosmos: Array[Filter])

