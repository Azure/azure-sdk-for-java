// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.models.CosmosParametrizedQuery
import org.apache.spark.sql.sources.Filter

// TODO: moderakh, thought for future optimization:
//  if we can identify if the user filter is a equality on cosmos partitionKeyValue
//  then we can set partitionKeyValue in the CosmosQueryOption
//  the benfit is that if the partitionKeyValue is set in the CosmosQueryOption
//  the antlr query parsing support can eliminate the need for query plan fetch from GW
case class AnalyzedFilters(cosmosParametrizedQuery: CosmosParametrizedQuery,
                           filtersToBePushedDownToCosmos: Array[Filter],
                           filtersNotSupportedByCosmos: Array[Filter])

