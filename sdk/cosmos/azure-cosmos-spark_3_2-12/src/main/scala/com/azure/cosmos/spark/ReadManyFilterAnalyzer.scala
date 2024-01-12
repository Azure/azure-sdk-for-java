// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.SparkBridgeImplementationInternal
import com.azure.cosmos.models.{CosmosItemIdentity, PartitionKeyDefinition}
import org.apache.spark.sql.sources.{Filter, In}

import scala.collection.mutable.ListBuffer

private case class ReadManyFilterAnalyzer() {

    //scalastyle:off method.length
    def analyze(
                   filters: Array[Filter],
                   readConfig: CosmosReadConfig,
                   partitionKeyDefinition: PartitionKeyDefinition,
                   plannedInputPartitions: List[CosmosInputPartition]): Option[Map[NormalizedRange, ListBuffer[String]]] = {
        val readManyFilterMap = scala.collection.mutable.Map[NormalizedRange, ListBuffer[String]]()
        var skipReadManyFilter = false // if there are mis-formatted values, readManyFilter will not be applied, it will fallback to original query plan

        try {
            for (filter <- filters; if !skipReadManyFilter) {
                filter match {
                    case In(readConfig.runtimeFilteringConfig.readManyFilterProperty, values) => {
                        for (value <- values; if !skipReadManyFilter) {
                            CosmosItemIdentityHelper.tryParseCosmosItemIdentity(value.toString) match {
                                case Some(itemIdentity: CosmosItemIdentity) => {
                                    // calculate which planned input partition it belongs to
                                    val feedRange =
                                        SparkBridgeImplementationInternal.partitionKeyToNormalizedRange(
                                            itemIdentity.getPartitionKey,
                                            partitionKeyDefinition)

                                    val overlapPlannedPartitions =
                                        plannedInputPartitions.filter(inputPartition => {
                                            SparkBridgeImplementationInternal.doRangesOverlap(
                                                feedRange,
                                                inputPartition.feedRange
                                            )
                                        })

                                    // the readManyItem feedRange should always overlap with one of the planned input partitions
                                    // if there are no match have found, something is wrong, readManyFilter will not be applied, it will fallback to original query plan
                                    if (overlapPlannedPartitions.isEmpty) {
                                        skipReadManyFilter = true
                                    }

                                    // the readManyItem feedRange should only overlap with one planned input partitions
                                    // in case there are multiple partitions overlapped, pick the first one should be enough (this should never happen)
                                    readManyFilterMap.get(overlapPlannedPartitions(0).feedRange) match {
                                        case Some(readManyItemList) => readManyItemList += value.toString
                                        case None =>
                                            val readManyFilterList = new ListBuffer[String]
                                            readManyFilterList += value.toString
                                            readManyFilterMap.put(overlapPlannedPartitions(0).feedRange, readManyFilterList)
                                    }
                                }
                                case None => skipReadManyFilter = true // the readMany filter column value not in expected format, skip readMany filter
                            }
                        }
                    }
                    case _ => // no-op for other filters
                }
            }
        } catch {
            case _ => skipReadManyFilter = true
        }

        if (skipReadManyFilter) {
            None
        } else {
            Some(readManyFilterMap.toMap)
        }
    }
}
