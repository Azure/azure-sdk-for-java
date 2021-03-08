// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.{CosmosAsyncClient, CosmosAsyncContainer, ThroughputControlGroupConfigBuilder}

private object ThroughputControlHelper {
    def getContainer(userConfig: Map[String, String],
                     cosmosContainerConfig: CosmosContainerConfig,
                     client: CosmosAsyncClient): CosmosAsyncContainer = {

        val container = client.getDatabase(cosmosContainerConfig.database).getContainer(cosmosContainerConfig.container)
        val cosmosThroughputControlConfig = CosmosThroughputControlConfig.parseThroughputControlConfig(userConfig)

        if (cosmosThroughputControlConfig.isDefined) {
            val throughputControlConfig = cosmosThroughputControlConfig.get

            val groupConfigBuilder = new ThroughputControlGroupConfigBuilder()
                .setGroupName(throughputControlConfig.groupName)
                .setDefault(true)

            if (throughputControlConfig.targetThroughput.isDefined) {
                groupConfigBuilder.setTargetThroughput(throughputControlConfig.targetThroughput.get)
            }
            if (throughputControlConfig.targetThroughputThreshold.isDefined) {
                groupConfigBuilder.setTargetThroughputThreshold(throughputControlConfig.targetThroughputThreshold.get)
            }

            val globalThroughputControlConfigBuilder = client.createGlobalThroughputControlConfigBuilder(
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
}
