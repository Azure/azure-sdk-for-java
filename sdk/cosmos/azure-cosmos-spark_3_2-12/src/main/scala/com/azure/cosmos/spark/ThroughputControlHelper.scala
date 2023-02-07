// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.ImplementationBridgeHelpers
import com.azure.cosmos.{CosmosAsyncContainer, ThroughputControlGroupConfigBuilder}
import org.apache.spark.broadcast.Broadcast
import reactor.core.scala.publisher.SMono

private object ThroughputControlHelper {
    def getContainer(userConfig: Map[String, String],
                     cosmosContainerConfig: CosmosContainerConfig,
                     cacheItem: CosmosClientCacheItem,
                     throughputControlCacheItemOpt: Option[CosmosClientCacheItem]): CosmosAsyncContainer = {

        val throughputControlConfigOpt = CosmosThroughputControlConfig.parseThroughputControlConfig(userConfig)

        val container = cacheItem.cosmosClient.getDatabase(cosmosContainerConfig.database).getContainer(cosmosContainerConfig.container)

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

            val globalThroughputControlConfigBuilder = throughputControlCacheItem.cosmosClient.createGlobalThroughputControlConfigBuilder(
                throughputControlConfig.globalControlDatabase,
                throughputControlConfig.globalControlContainer)

            if (throughputControlConfig.globalControlRenewInterval.isDefined) {
                globalThroughputControlConfigBuilder.setControlItemRenewInterval(throughputControlConfig.globalControlRenewInterval.get)
            }
            if (throughputControlConfig.globalControlExpireInterval.isDefined) {
                globalThroughputControlConfigBuilder.setControlItemExpireInterval(throughputControlConfig.globalControlExpireInterval.get)
            }

            // Currently CosmosDB data plane SDK does not support query database/container throughput by using AAD authentication
            // As a mitigation we are going to pass a throughput query mono which internally use management SDK to query throughput
            val throughputQueryMonoOpt = getThroughputQueryMono(userConfig, cacheItem, cosmosContainerConfig)
            throughputQueryMonoOpt match {
                case Some(throughputQueryMono) =>
                    ImplementationBridgeHelpers.CosmosAsyncContainerHelper.getCosmosAsyncContainerAccessor
                        .enableGlobalThroughputControlGroup(
                            container,
                            groupConfigBuilder.build(),
                            globalThroughputControlConfigBuilder.build(),
                            throughputQueryMono.asJava())
                case None =>
                    container.enableGlobalThroughputControlGroup(
                        groupConfigBuilder.build(),
                        globalThroughputControlConfigBuilder.build())
            }
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

    private def getThroughputQueryMono(
                                          userConfig: Map[String, String],
                                          cacheItem: CosmosClientCacheItem,
                                          cosmosContainerConfig: CosmosContainerConfig): Option[SMono[Integer]] = {
        val cosmosAuthConfig = CosmosAccountConfig.parseCosmosAccountConfig(userConfig).authConfig
        cosmosAuthConfig match {
            case _: CosmosMasterKeyAuthConfig => None
            case _: CosmosAadAuthConfig =>
                Some(cacheItem.sparkCatalogClient.readContainerThroughput(cosmosContainerConfig.database, cosmosContainerConfig.container))
        }
    }
}
