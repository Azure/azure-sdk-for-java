// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos

import com.azure.cosmos.implementation.{DocumentCollection, ImplementationBridgeHelpers, PartitionKeyRange, SparkBridgeImplementationInternal}
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl
import com.azure.cosmos.implementation.routing.Range
import com.azure.cosmos.models.{CosmosContainerProperties, CosmosQueryRequestOptions, FeedRange, ModelBridgeInternal}
import com.azure.cosmos.spark.NormalizedRange

import java.time.Duration
import scala.collection.mutable.ArrayBuffer

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

private[cosmos] object SparkBridgeInternal {
  private val containerPropertiesAccessor = ImplementationBridgeHelpers
    .CosmosContainerPropertiesHelper
    .getCosmosContainerPropertiesAccessor

  //scalastyle:off null
  val defaultQueryRequestOptions: CosmosQueryRequestOptions = null
  //scalastyle:on null

  def trySplitFeedRange
  (
    container: CosmosAsyncContainer,
    feedRange: NormalizedRange,
    targetedCountAfterSplit: Int
  ): Array[NormalizedRange] = {

    val list = container
      .trySplitFeedRange(new FeedRangeEpkImpl(toCosmosRange(feedRange)), targetedCountAfterSplit)
      .block

    list.asScala.map(e => SparkBridgeImplementationInternal.toNormalizedRange(e)).toArray
  }

  private[this] def toCosmosRange(range: NormalizedRange): Range[String] = {
    new Range[String](range.min, range.max, true, false)
  }

  private[cosmos] def getCacheKeyForContainer(container: CosmosAsyncContainer): String = {
    val database = container.getDatabase
    s"${database.getClient.getServiceEndpoint}|${database.getId}|${container.getId}"
  }

  private[cosmos] def getNormalizedEffectiveRange
  (
    container: CosmosAsyncContainer,
    feedRange: FeedRange
  ) : NormalizedRange = {

    SparkBridgeImplementationInternal
      .rangeToNormalizedRange(
        container.getNormalizedEffectiveRange(feedRange).block)
  }

  private[cosmos] def getPartitionKeyRanges
  (
    container: CosmosAsyncContainer
  ): List[PartitionKeyRange] = {
    val pkRanges = new ArrayBuffer[PartitionKeyRange]()

    container
      .getDatabase
      .getDocClientWrapper
      .readPartitionKeyRanges(container.getLink, defaultQueryRequestOptions)
      .collectList
      .block()
      .forEach(feedResponse => feedResponse.getResults.forEach(pkRange => pkRanges += pkRange))

    pkRanges.toList
  }

  private[cosmos] def clearCollectionCache(container: CosmosAsyncContainer, obsoleteRid: String): Unit = {
    val clientWrapper = container.getDatabase.getDocClientWrapper

    val link = container.getLinkWithoutTrailingSlash;

    val obsoleteValue = new DocumentCollection
    obsoleteValue.setResourceId(obsoleteRid)

    clientWrapper
      .getCollectionCache()
      .resolveByNameAsync(null, link, null, obsoleteValue)
      .block()
  }

  def getContainerPropertiesFromCollectionCache(container: CosmosAsyncContainer): CosmosContainerProperties = {
    val documentCollectionHolder = container
      .getDatabase
      .getDocClientWrapper
      .getCollectionCache
      .resolveByNameAsync(null, container.getLinkWithoutTrailingSlash, null)
      .block()

    if (documentCollectionHolder != null) {
      containerPropertiesAccessor.create(documentCollectionHolder)
    } else {
      container.read().block().getProperties
    }
  }
}
