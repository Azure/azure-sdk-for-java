// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.models.CosmosItemIdentity
import org.apache.spark.sql.sources.{Filter, In}

import scala.collection.mutable.ListBuffer

private case class ReadManyFilterAnalyzer(readConfig: CosmosReadConfig) {

  def analyze(filters: Array[Filter]): AnalyzedReadManyFilters = {
    val readManyFilters = ListBuffer[ReadManyFilter]()
    val filtersToBeHandled = ListBuffer[Filter]()
    val filtersCanNotBeHandled = ListBuffer[Filter]()

    for (filter <- filters) {
      filter match {
        case In(readConfig.readManyFilteringConfig.readManyFilterProperty, values) => {
          for (value <- values) {
            CosmosItemIdentityHelper.tryParseCosmosItemIdentity(value.toString) match {
              case Some(itemIdentity: CosmosItemIdentity) =>
                readManyFilters += ReadManyFilter(itemIdentity.getPartitionKey, value.toString)
              case _ => throw new IllegalArgumentException(s"${readConfig.readManyFilteringConfig.readManyFilterProperty} value is mis-formatted")
            }
          }

          // if we have reached here, then it means the filter can be handled
          filtersToBeHandled += filter
        }
        case _ => filtersCanNotBeHandled += filter
      }
    }

    if (filtersToBeHandled.isEmpty) {
      AnalyzedReadManyFilters(Array.empty[Filter], filters, None)
    } else {
      AnalyzedReadManyFilters(filtersToBeHandled.toArray, filtersCanNotBeHandled.toArray, Some(readManyFilters.toList))
    }
  }
}
