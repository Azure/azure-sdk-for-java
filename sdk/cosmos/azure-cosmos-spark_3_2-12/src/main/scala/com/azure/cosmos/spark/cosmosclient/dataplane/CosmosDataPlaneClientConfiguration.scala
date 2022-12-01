// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark.cosmosclient.dataplane

import com.azure.cosmos.spark.cosmosclient.ICosmosClientConfiguration
import com.azure.cosmos.spark.{CosmosAccountConfig, CosmosAuthConfig, CosmosConstants, DiagnosticsConfig}

import java.lang.management.ManagementFactory

private[spark] case class CosmosDataPlaneClientConfiguration (
                                                                 endpoint: String,
                                                                 authConfig: CosmosAuthConfig,
                                                                 customApplicationNameSuffix: Option[String],
                                                                 applicationName: String,
                                                                 useGatewayMode: Boolean,
                                                                 useEventualConsistency: Boolean,
                                                                 enableClientTelemetry: Boolean,
                                                                 disableTcpConnectionEndpointRediscovery: Boolean,
                                                                 clientTelemetryEndpoint: Option[String],
                                                                 preferredRegionsList: Option[Array[String]]) extends ICosmosClientConfiguration

private[spark] object CosmosDataPlaneClientConfiguration {
  def apply(
             config: Map[String, String],
             useEventualConsistency: Boolean): CosmosDataPlaneClientConfiguration = {

    val cosmosAccountConfig = CosmosAccountConfig.parseCosmosAccountConfig(config)
    val diagnosticsConfig = DiagnosticsConfig.parseDiagnosticsConfig(config)

    apply(cosmosAccountConfig, diagnosticsConfig, useEventualConsistency)
  }

  def apply(
            cosmosAccountConfig: CosmosAccountConfig,
            diagnosticsConfig: DiagnosticsConfig,
            useEventualConsistency: Boolean): CosmosDataPlaneClientConfiguration = {

    var applicationName = CosmosConstants.userAgentSuffix
    val customApplicationNameSuffix = cosmosAccountConfig.applicationName
    val runtimeInfo = runtimeInformation()
    if (runtimeInfo.isDefined) {
      applicationName = s"$applicationName ${runtimeInfo.get}"
    }

    if (customApplicationNameSuffix.isDefined){
      applicationName = s"$applicationName ${customApplicationNameSuffix.get}"
    }

      CosmosDataPlaneClientConfiguration(
          cosmosAccountConfig.endpoint,
          cosmosAccountConfig.authConfig,
          customApplicationNameSuffix,
          applicationName,
          cosmosAccountConfig.useGatewayMode,
          useEventualConsistency,
          enableClientTelemetry = diagnosticsConfig.isClientTelemetryEnabled,
          cosmosAccountConfig.disableTcpConnectionEndpointRediscovery,
          diagnosticsConfig.clientTelemetryEndpoint,
          cosmosAccountConfig.preferredRegionsList)
  }

  private[this] def runtimeInformation(): Option[String] = {
    try{
      Some(ManagementFactory.getRuntimeMXBean.getName)
    }
    catch{
      case _:Exception => None
    }
  }
}
