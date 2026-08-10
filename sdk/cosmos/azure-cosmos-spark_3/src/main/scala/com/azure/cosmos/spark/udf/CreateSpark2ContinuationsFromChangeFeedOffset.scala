// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark.udf

import com.azure.cosmos.implementation.SparkBridgeImplementationInternal
import com.azure.cosmos.implementation.SparkBridgeImplementationInternal.rangeToNormalizedRange
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState
import com.azure.cosmos.implementation.query.CompositeContinuationToken
import com.azure.cosmos.spark.{ChangeFeedOffset, CosmosClientCache, CosmosClientCacheItem, CosmosClientConfiguration, CosmosConfig, CosmosContainerConfig, CosmosReadConfig, Loan}
import com.azure.cosmos.{CosmosAsyncClient, SparkBridgeInternal}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.api.java.UDF2

import scala.collection.mutable

@SerialVersionUID(1L)
class CreateSpark2ContinuationsFromChangeFeedOffset extends UDF2[Map[String, String], String, Map[Int, Long]] {
  override def call
  (
    userProvidedConfig: Map[String, String],
    changeFeedOffset: String
  ): Map[Int, Long] = {

    val effectiveUserConfig = CosmosConfig.getEffectiveConfig(None, None, userProvidedConfig)
    val readConfig = CosmosReadConfig.parseCosmosReadConfig(effectiveUserConfig)
    val cosmosClientConfig = CosmosClientConfiguration(
      effectiveUserConfig,
      readConsistencyStrategy = readConfig.readConsistencyStrategy,
      CosmosClientConfiguration.getSparkEnvironmentInfo(SparkSession.getActiveSession))

    val cosmosContainerConfig: CosmosContainerConfig =
      CosmosContainerConfig.parseCosmosContainerConfig(effectiveUserConfig)

    Loan(
      List[Option[CosmosClientCacheItem]](
        Some(CosmosClientCache(
          cosmosClientConfig,
          None,
          s"UDF CreateSpark2ContinuationsFromChangeFeedOffset"
        ))
      ))
      .to(cosmosClientCacheItems => {
        createSpark2ContinuationsFromChangeFeedOffset(
          cosmosClientCacheItems.head.get.cosmosClient,
          cosmosContainerConfig.database,
          cosmosContainerConfig.container,
          changeFeedOffset
        )
      })
  }

  private[this] def createSpark2ContinuationsFromChangeFeedOffset
  (
    client: CosmosAsyncClient,
    databaseName: String,
    containerName: String,
    offsetJson: String
  ): Map[Int, Long] = {

    val effectiveOffsetJson = if (offsetJson.indexOf("\n") == 2 && offsetJson.size > 2) {
      offsetJson.substring(3)
    } else {
      offsetJson
    }
    val offset: ChangeFeedOffset = ChangeFeedOffset.fromJson(effectiveOffsetJson)

    val container = client
      .getDatabase(databaseName)
      .getContainer(containerName)

    val expectedContainerResourceId = SparkBridgeInternal
      .getContainerPropertiesFromCollectionCache(container)
      .getResourceId

    val pkRanges = SparkBridgeInternal
      .getPartitionKeyRanges(container)

    val lsnsByPkRangeId = mutable.Map[Int, Long]()

    pkRanges
      .foreach(pkRange => {
        val normalizedRange = rangeToNormalizedRange(pkRange.toRange)
        val parsedChangeFeedState = SparkBridgeImplementationInternal.parseChangeFeedState(offset.changeFeedState)
        val effectiveChangeFeedState = ChangeFeedState
          .fromString(
            SparkBridgeImplementationInternal
              .extractChangeFeedStateForRange(parsedChangeFeedState, normalizedRange)
          )

        val containerResourceId = effectiveChangeFeedState.getContainerRid

        if (!expectedContainerResourceId.equalsIgnoreCase(containerResourceId)) {
          throw new IllegalArgumentException(
            s"The provided change feed offset is for a different container (either completely different container " +
              s"or container with same name but after being deleted and recreated). Name:$containerName " +
              s"Expected ResourceId: $expectedContainerResourceId, " +
              s"Actual ResourceId: $containerResourceId"
          )
        }

        var minLsn: Option[CompositeContinuationToken] = None

        effectiveChangeFeedState
          .extractContinuationTokens()
          .forEach(token => {

            if (minLsn.isEmpty) {
              minLsn = Some(token)
            } else if (SparkBridgeImplementationInternal.toLsn(token.getToken) <
              SparkBridgeImplementationInternal.toLsn(minLsn.get.getToken)) {
              minLsn = Some(token)
            }
          })

        if (minLsn.isDefined) {
          lsnsByPkRangeId.put(
            pkRange.getId.toInt,
            Math.max(0, SparkBridgeImplementationInternal.toLsn(minLsn.get.getToken)))
        }
      })

    lsnsByPkRangeId.toMap
  }
}

