// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import java.lang.management.ManagementFactory

private[spark] case class CosmosClientConfiguration (
                                                      endpoint: String,
                                                      key: String,
                                                      customApplicationNameSuffix: Option[String],
                                                      applicationName: String,
                                                      useGatewayMode: Boolean,
                                                      useEventualConsistency: Boolean,
                                                      enableClientTelemetry: Boolean,
                                                      disableTcpConnectionEndpointRediscovery: Boolean,
                                                      clientTelemetryEndpoint: Option[String],
                                                      preferredRegionsList: Option[Array[String]])

private[spark] object CosmosClientConfiguration {
  def apply(
             config: Map[String, String],
             useEventualConsistency: Boolean): CosmosClientConfiguration = {

    val cosmosAccountConfig = CosmosAccountConfig.parseCosmosAccountConfig(config)
    val diagnosticsConfig = DiagnosticsConfig.parseDiagnosticsConfig(config)

    apply(cosmosAccountConfig, diagnosticsConfig, useEventualConsistency)
  }

  def apply(
            cosmosAccountConfig: CosmosAccountConfig,
            diagnosticsConfig: DiagnosticsConfig,
            useEventualConsistency: Boolean): CosmosClientConfiguration = {

    var applicationName = CosmosConstants.userAgentSuffix
    val customApplicationNameSuffix = cosmosAccountConfig.applicationName
    val runtimeInfo = runtimeInformation()
    if (runtimeInfo.isDefined) {
      applicationName = s"$applicationName ${runtimeInfo.get}"
    }

    if (cosmosAccountConfig.applicationName.isDefined){
      applicationName = s"$applicationName $customApplicationNameSuffix"
    }

    CosmosClientConfiguration(
      cosmosAccountConfig.endpoint,
      cosmosAccountConfig.key,
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
