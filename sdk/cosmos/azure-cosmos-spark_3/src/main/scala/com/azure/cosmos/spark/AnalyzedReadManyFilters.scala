// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import org.apache.spark.sql.sources.Filter

private[spark] case class AnalyzedReadManyFilters(
                                            filtersToBePushedDownToCosmos: Array[Filter],
                                            filtersNotSupportedByCosmos: Array[Filter],
                                            readManyFiltersOpt: Option[List[ReadManyFilter]])
