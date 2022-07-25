// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.{CosmosAsyncContainer, ThroughputControlGroupConfigBuilder}
import org.apache.spark.broadcast.Broadcast

private object ThroughputControlHelper {
    def getContainer(userConfig: Map[String, String],
                     cosmosContainerConfig: CosmosContainerConfig,
                     cacheItem: CosmosClientCacheItem,
                     cosmosClientStateHandles: Option[Broadcast[CosmosClientMetadataCachesSnapshots]]): (CosmosAsyncContainer,Option[CosmosClientCacheItem]) = {

        val throughputControlConfigOpt = CosmosThroughputControlConfig.parseThroughputControlConfig(userConfig)
        val diagnosticConfig = DiagnosticsConfig.parseDiagnosticsConfig(userConfig)

        val container = cacheItem.client.getDatabase(cosmosContainerConfig.database).getContainer(cosmosContainerConfig.container)

        if (throughputControlConfigOpt.isDefined) {
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

            // Customer can choose different database account for throughput control
            // Get the cosmos client for throughput control
            // TODO: validate whether to use eventual consistency
            val throughputControlClientConfig =
                CosmosClientConfiguration.apply(throughputControlConfig.cosmosAccountConfig, diagnosticConfig, false)

            val throughputControlClientMetadataCache =
                cosmosClientStateHandles match {
                    case None => None
                    case Some(_) => cosmosClientStateHandles.get.value.throughputControlClientMetadataCaches
                }

            Loan(
                CosmosClientCache
                 .apply(
                    throughputControlClientConfig,
                     throughputControlClientMetadataCache,
                    s"ThroughputControlHelp.getContainer: ${cacheItem.context}"
                ))
                .to(throughputControlCachedItem => {
                    val globalThroughputControlConfigBuilder = throughputControlCachedItem.client.createGlobalThroughputControlConfigBuilder(
                        throughputControlConfig.globalControlDatabase,
                        throughputControlConfig.globalControlContainer)

                    if (throughputControlConfig.globalControlRenewInterval.isDefined) {
                        globalThroughputControlConfigBuilder.setControlItemRenewInterval(throughputControlConfig.globalControlRenewInterval.get)
                    }
                    if (throughputControlConfig.globalControlExpireInterval.isDefined) {
                        globalThroughputControlConfigBuilder.setControlItemExpireInterval(throughputControlConfig.globalControlExpireInterval.get)
                    }

                    container.enableGlobalThroughputControlGroup(groupConfigBuilder.build(), globalThroughputControlConfigBuilder.build())

                    (container, Some(throughputControlCachedItem))
                })
        } else {
            (container, None)
        }
    }
}
