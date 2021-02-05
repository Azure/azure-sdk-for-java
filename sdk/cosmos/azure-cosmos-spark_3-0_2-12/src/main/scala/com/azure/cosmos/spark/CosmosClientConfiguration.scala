// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import java.lang.management.ManagementFactory

private[spark] case class CosmosClientConfiguration (
    endpoint: String,
    key: String,
    applicationName: String,
    useGatewayMode: Boolean,
    useEventualConsistency: Boolean)

private[spark] object CosmosClientConfiguration {
    def apply(
         config: Map[String, String],
         useEventualConsistency: Boolean): CosmosClientConfiguration = {
        val cosmosAccountConfig = CosmosAccountConfig.parseCosmosAccountConfig(config)
        val applicationName = cosmosAccountConfig.applicationName match {
            case None =>
                s"${CosmosConstants.userAgentSuffix} ${ManagementFactory.getRuntimeMXBean.getName}"
            case Some(appName) =>
                s"${CosmosConstants.userAgentSuffix} ${ManagementFactory.getRuntimeMXBean.getName} $appName"
        }

        CosmosClientConfiguration(
            cosmosAccountConfig.endpoint,
            cosmosAccountConfig.key,
            applicationName,
            cosmosAccountConfig.useGatewayMode,
            useEventualConsistency)
    }
}
