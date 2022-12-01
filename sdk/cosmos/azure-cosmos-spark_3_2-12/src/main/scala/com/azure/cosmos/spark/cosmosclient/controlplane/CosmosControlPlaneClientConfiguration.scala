// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark.cosmosclient.controlplane

import com.azure.cosmos.spark.cosmosclient.ICosmosClientConfiguration
import com.azure.cosmos.spark.{CosmosAadAuthConfig, CosmosAccountConfig, CosmosConstants}

import java.lang.management.ManagementFactory

private[spark] case class CosmosControlPlaneClientConfiguration(
                                                                   endpoint: String,
                                                                   authConfig: CosmosAadAuthConfig,
                                                                   customApplicationNameSuffix: Option[String],
                                                                   applicationName: String) extends ICosmosClientConfiguration

private[spark] object CosmosControlPlaneClientConfiguration {

    def apply(config: Map[String, String]): CosmosControlPlaneClientConfiguration = {

        val cosmosAccountConfig = CosmosAccountConfig.parseCosmosAccountConfig(config)
        var applicationName = CosmosConstants.userAgentSuffix
        val customApplicationNameSuffix = cosmosAccountConfig.applicationName
        val runtimeInfo = runtimeInformation()
        if (runtimeInfo.isDefined) {
            applicationName = s"$applicationName ${runtimeInfo.get}"
        }

        if (customApplicationNameSuffix.isDefined) {
            applicationName = s"$applicationName ${customApplicationNameSuffix.get}"
        }

        CosmosControlPlaneClientConfiguration(
            cosmosAccountConfig.endpoint,
            cosmosAccountConfig.authConfig.asInstanceOf[CosmosAadAuthConfig],
            customApplicationNameSuffix,
            applicationName)
    }

    private[this] def runtimeInformation(): Option[String] = {
        try {
            Some(ManagementFactory.getRuntimeMXBean.getName)
        }
        catch {
            case _: Exception => None
        }
    }
}
