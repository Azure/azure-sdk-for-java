// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.models.CosmosParametrizedQuery
import org.apache.spark.sql.sources.Filter

case class AnalyzedFilters(cosmosParametrizedQuery: CosmosParametrizedQuery,
                           filtersToBePushedDownToCosmos: Array[Filter],
                           filtersNotSupportedByCosmos: Array[Filter])

