// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.models.{CosmosItemIdentity, PartitionKey, PartitionKeyDefinition}
import org.apache.spark.sql.sources.{EqualTo, Filter, In}

import scala.collection.mutable.ListBuffer

private[spark] case class ReadManyFilterAnalyzer(
                                           readConfig: CosmosReadConfig,
                                           partitionKeyDefinition: PartitionKeyDefinition) {

  private val effectiveReadManyFilteringConfig =
    CosmosReadManyFilteringConfig.getEffectiveReadManyFilteringConfig(
      readConfig.readManyFilteringConfig,
      partitionKeyDefinition)

  def analyze(filters: Array[Filter]): AnalyzedReadManyFilters = {
    val readManyFilters = ListBuffer[ReadManyFilter]()
    val filtersToBeHandled = ListBuffer[Filter]()
    val filtersCanNotBeHandled = ListBuffer[Filter]()

    for (filter <- filters) {
      filter match {
        case In(effectiveReadManyFilteringConfig.readManyFilterProperty, values) => {
          getReadManyFilters(values, readManyFilters)
          // if we have reached here, then it means the filter can be handled
          filtersToBeHandled += filter
        }
        // when there is only one item in the IN list,it may be optimized to use EqualTo instead
        case EqualTo(effectiveReadManyFilteringConfig.readManyFilterProperty, value) => {
          getReadManyFilters(Array(value), readManyFilters)
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

  private[this] def getReadManyFilterFromId(value: String): ReadManyFilter = {
    val partitionKey = new PartitionKey(value)
    ReadManyFilter(partitionKey, CosmosItemIdentityHelper.getCosmosItemIdentityValueString(value, List(value)))
  }

  private[this] def getReadManyFilterFromNonId(value: String): ReadManyFilter = {
    CosmosItemIdentityHelper.tryParseCosmosItemIdentity(value) match {
      case Some(itemIdentity: CosmosItemIdentity) =>
        ReadManyFilter(itemIdentity.getPartitionKey, value)
      case _ => throw new IllegalArgumentException(s"${readConfig.readManyFilteringConfig.readManyFilterProperty} value is mis-formatted")
    }
  }

  private[this] def getReadManyFilters(values: Array[Any], readManyFilters: ListBuffer[ReadManyFilter]): Unit = {
    effectiveReadManyFilteringConfig.readManyFilterProperty match {
      case CosmosConstants.Properties.Id =>
        for (value <- values) {
          readManyFilters += getReadManyFilterFromId(value.toString)
        }
      case _ =>
        for (value <- values) {
          readManyFilters += getReadManyFilterFromNonId(value.toString)
        }
    }
  }
}
