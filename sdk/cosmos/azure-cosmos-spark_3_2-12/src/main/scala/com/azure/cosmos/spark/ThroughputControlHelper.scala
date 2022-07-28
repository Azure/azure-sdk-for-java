// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.{CosmosAsyncContainer, ThroughputControlGroupConfigBuilder}
import org.apache.spark.broadcast.Broadcast

private object ThroughputControlHelper {
    def getContainer(userConfig: Map[String, String],
                     cosmosContainerConfig: CosmosContainerConfig,
                     cacheItem: CosmosClientCacheItem,
                     throughputControlCacheItemOpt: Option[CosmosClientCacheItem]): CosmosAsyncContainer = {

        val throughputControlConfigOpt = CosmosThroughputControlConfig.parseThroughputControlConfig(userConfig)

        val container = cacheItem.client.getDatabase(cosmosContainerConfig.database).getContainer(cosmosContainerConfig.container)

        if (throughputControlConfigOpt.isDefined) {
            assert(throughputControlCacheItemOpt.isDefined)
            val throughputControlCacheItem = throughputControlCacheItemOpt.get
            val throughputControlConfig = throughputControlConfigOpt.get
            val groupConfigBuilder = new ThroughputControlGroupConfigBuilder()
                .groupName(throughputControlConfig.groupName)
                .defaultControlGroup(true)

            if (throughputControlConfig.targetThroughput.isDefined) {
                groupConfigBuilder.targetThroughput(throughputControlConfig.targetThroughput.get)
            }
            if (throughputControlConfig.targetThroughputThreshold.isDefined) {
                groupConfigBuilder.targetThroughputThreshold(throughputControlConfig.targetThroughputThreshold.get)
            }

            val globalThroughputControlConfigBuilder = throughputControlCacheItem.client.createGlobalThroughputControlConfigBuilder(
                throughputControlConfig.globalControlDatabase,
                throughputControlConfig.globalControlContainer)

            if (throughputControlConfig.globalControlRenewInterval.isDefined) {
                globalThroughputControlConfigBuilder.setControlItemRenewInterval(throughputControlConfig.globalControlRenewInterval.get)
            }
            if (throughputControlConfig.globalControlExpireInterval.isDefined) {
                globalThroughputControlConfigBuilder.setControlItemExpireInterval(throughputControlConfig.globalControlExpireInterval.get)
            }

            container.enableGlobalThroughputControlGroup(groupConfigBuilder.build(), globalThroughputControlConfigBuilder.build())
        }

        container
    }

    def getThroughputControlClientCacheItem(userConfig: Map[String, String],
                                            calledFrom: String,
                                            cosmosClientStateHandles: Option[Broadcast[CosmosClientMetadataCachesSnapshots]]): Option[CosmosClientCacheItem] = {
        val throughputControlConfigOpt = CosmosThroughputControlConfig.parseThroughputControlConfig(userConfig)
        val diagnosticConfig = DiagnosticsConfig.parseDiagnosticsConfig(userConfig)

        if (throughputControlConfigOpt.isDefined) {
            val throughputControlClientConfig =
                CosmosClientConfiguration.apply(throughputControlConfigOpt.get.cosmosAccountConfig, diagnosticConfig, false)

            val throughputControlClientMetadata =
                cosmosClientStateHandles match {
                    case None => None
                    case Some(_) => cosmosClientStateHandles.get.value.throughputControlClientMetadataCaches
                }
            Some(CosmosClientCache.apply(
                throughputControlClientConfig,
                throughputControlClientMetadata,
                s"ThroughputControl: $calledFrom"))
        } else {
            None
        }
    }
}
