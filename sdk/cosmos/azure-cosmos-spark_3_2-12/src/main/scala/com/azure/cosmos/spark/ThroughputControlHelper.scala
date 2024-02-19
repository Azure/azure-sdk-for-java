// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.implementation.ImplementationBridgeHelpers
import com.azure.cosmos.models.{CosmosBulkExecutionOptions, CosmosChangeFeedRequestOptions, CosmosItemRequestOptions, CosmosQueryRequestOptionsBase, PriorityLevel}
import com.azure.cosmos.spark.diagnostics.BasicLoggingTrait
import com.azure.cosmos.{CosmosAsyncContainer, ThroughputControlGroupConfigBuilder}
import org.apache.spark.broadcast.Broadcast
import reactor.core.scala.publisher.SMono

private object ThroughputControlHelper extends BasicLoggingTrait {
    def getContainer(userConfig: Map[String, String],
                     cosmosContainerConfig: CosmosContainerConfig,
                     cacheItem: CosmosClientCacheItem,
                     throughputControlCacheItemOpt: Option[CosmosClientCacheItem]): CosmosAsyncContainer = {

        val throughputControlConfigOpt = CosmosThroughputControlConfig.parseThroughputControlConfig(userConfig)

        val container = cacheItem.cosmosClient.getDatabase(cosmosContainerConfig.database).getContainer(cosmosContainerConfig.container)

        if (throughputControlConfigOpt.isDefined) {
            val throughputControlConfig = throughputControlConfigOpt.get

            if (throughputControlConfig.globalControlUseDedicatedContainer) {
                assert(throughputControlCacheItemOpt.isDefined)
                val throughputControlCacheItem = throughputControlCacheItemOpt.get

                this.enableGlobalThroughputControlGroup(
                    userConfig,
                    cosmosContainerConfig,
                    container,
                    cacheItem,
                    throughputControlCacheItem,
                    throughputControlConfig)
            } else {
                this.enableLocalThroughputControlGroup(
                    userConfig,
                    cosmosContainerConfig,
                    container,
                    cacheItem,
                    throughputControlConfig
                )
            }
        }

        container
    }

    def populateThroughputControlGroupName(
                                              bulkExecutionOptions: CosmosBulkExecutionOptions,
                                              throughputControlConfigOpt: Option[CosmosThroughputControlConfig]
                                          ): Unit = {
        if (throughputControlConfigOpt.isDefined) {
            bulkExecutionOptions.setThroughputControlGroupName(throughputControlConfigOpt.get.groupName)
        }
    }

    def populateThroughputControlGroupName(
                                              itemRequestOptions: CosmosItemRequestOptions,
                                              throughputControlConfigOpt: Option[CosmosThroughputControlConfig]
                                          ): Unit = {
        if (throughputControlConfigOpt.isDefined) {
            itemRequestOptions.setThroughputControlGroupName(throughputControlConfigOpt.get.groupName)
        }
    }

    def populateThroughputControlGroupName(
                                              queryRequestOptions: CosmosQueryRequestOptionsBase[_],
                                              throughputControlConfigOpt: Option[CosmosThroughputControlConfig]
                                          ): Unit = {
        if (throughputControlConfigOpt.isDefined) {
            queryRequestOptions.setThroughputControlGroupName(throughputControlConfigOpt.get.groupName)
        }
    }

    def populateThroughputControlGroupName(
                                              changeFeedRequestOptions: CosmosChangeFeedRequestOptions,
                                              throughputControlConfigOpt: Option[CosmosThroughputControlConfig]
                                          ): Unit = {
        if (throughputControlConfigOpt.isDefined) {
            changeFeedRequestOptions.setThroughputControlGroupName(throughputControlConfigOpt.get.groupName)
        }
    }

    private def enableGlobalThroughputControlGroup(
                                                      userConfig: Map[String, String],
                                                      cosmosContainerConfig: CosmosContainerConfig,
                                                      container: CosmosAsyncContainer,
                                                      cacheItem: CosmosClientCacheItem,
                                                      throughputControlCacheItem: CosmosClientCacheItem,
                                                      throughputControlConfig: CosmosThroughputControlConfig): Unit = {
        val groupConfigBuilder = new ThroughputControlGroupConfigBuilder()
            .groupName(throughputControlConfig.groupName)

        if (throughputControlConfig.targetThroughput.isDefined) {
            groupConfigBuilder.targetThroughput(throughputControlConfig.targetThroughput.get)
        }
        if (throughputControlConfig.targetThroughputThreshold.isDefined) {
            groupConfigBuilder.targetThroughputThreshold(throughputControlConfig.targetThroughputThreshold.get)
        }
        if (throughputControlConfig.priorityLevel.isDefined) {
            val priority = throughputControlConfig.priorityLevel.get
            logDebug(s"Configure throughput control with priority $priority")
            groupConfigBuilder.priorityLevel(parsePriorityLevel(throughputControlConfig.priorityLevel.get))
        }

        val globalThroughputControlConfigBuilder = throughputControlCacheItem.cosmosClient.createGlobalThroughputControlConfigBuilder(
            throughputControlConfig.globalControlDatabase.get,
            throughputControlConfig.globalControlContainer.get)

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

    private def enableLocalThroughputControlGroup(
                                          userConfig: Map[String, String],
                                          cosmosContainerConfig: CosmosContainerConfig,
                                          container: CosmosAsyncContainer,
                                          cacheItem: CosmosClientCacheItem,
                                          throughputControlConfig: CosmosThroughputControlConfig): Unit = {

        val groupConfigBuilder = new ThroughputControlGroupConfigBuilder()
            .groupName(throughputControlConfig.groupName)

        // If there is no SparkExecutorCount being captured, then fall back to use 1 executor count
        // If the spark executor count is somehow 0, then fall back to 1 executor count
        val instanceCount = math.max(userConfig.getOrElse(CosmosConfigNames.SparkExecutorCount, "1").toInt, 1)
        logDebug(s"Configure throughput control without using dedicated container, instance count $instanceCount")

        if (throughputControlConfig.targetThroughput.isDefined) {
            val targetThroughputByExecutor =
                (throughputControlConfig.targetThroughput.get / instanceCount).ceil.toInt
            logDebug(s"Configure throughput control with throughput $targetThroughputByExecutor")
            groupConfigBuilder.targetThroughput(targetThroughputByExecutor)
        }
        if (throughputControlConfig.targetThroughputThreshold.isDefined) {
            // Try to limit to 2 decimal digits
            val targetThroughputThresholdByExecutor =
                (throughputControlConfig.targetThroughputThreshold.get * 10000 / instanceCount).ceil / 10000
            logDebug(s"Configure throughput control with throughput threshold $targetThroughputThresholdByExecutor")
            groupConfigBuilder.targetThroughputThreshold(targetThroughputThresholdByExecutor)
        }
        if (throughputControlConfig.priorityLevel.isDefined) {
            groupConfigBuilder.priorityLevel(parsePriorityLevel(throughputControlConfig.priorityLevel.get))
        }

        // Currently CosmosDB data plane SDK does not support query database/container throughput by using AAD authentication
        // As a mitigation we are going to pass a throughput query mono which internally use management SDK to query throughput
        val throughputQueryMonoOpt = getThroughputQueryMono(userConfig, cacheItem, cosmosContainerConfig)
        ImplementationBridgeHelpers
            .CosmosAsyncContainerHelper
            .getCosmosAsyncContainerAccessor
            .enableLocalThroughputControlGroup(
                container,
                groupConfigBuilder.build(),
                if (throughputQueryMonoOpt.isDefined) throughputQueryMonoOpt.get.asJava() else null)
    }

    def parsePriorityLevel(priorityLevels: PriorityLevels.PriorityLevel) = {
        priorityLevels match {
            case PriorityLevels.Low => PriorityLevel.LOW
            case PriorityLevels.High => PriorityLevel.HIGH
        }
    }

    def getThroughputControlClientCacheItem(userConfig: Map[String, String],
                                            calledFrom: String,
                                            cosmosClientStateHandles: Option[Broadcast[CosmosClientMetadataCachesSnapshots]],
                                            sparkEnvironmentInfo: String): Option[CosmosClientCacheItem] = {
        val throughputControlConfigOpt = CosmosThroughputControlConfig.parseThroughputControlConfig(userConfig)
        val diagnosticConfig = DiagnosticsConfig.parseDiagnosticsConfig(userConfig)

        if (throughputControlConfigOpt.isDefined) {
            val throughputControlClientConfig =
                CosmosClientConfiguration.apply(
                  throughputControlConfigOpt.get.cosmosAccountConfig,
                  diagnosticConfig,
                  false,
                  sparkEnvironmentInfo)

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
