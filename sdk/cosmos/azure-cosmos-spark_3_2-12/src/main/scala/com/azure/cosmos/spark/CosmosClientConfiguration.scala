// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.core.management.AzureEnvironment

import java.lang.management.ManagementFactory

private[spark] case class CosmosClientConfiguration (
                                                      endpoint: String,
                                                      databaseAccountName: String,
                                                      authConfig: CosmosAuthConfig,
                                                      customApplicationNameSuffix: Option[String],
                                                      applicationName: String,
                                                      useGatewayMode: Boolean,
                                                      useEventualConsistency: Boolean,
                                                      enableClientTelemetry: Boolean,
                                                      disableTcpConnectionEndpointRediscovery: Boolean,
                                                      clientTelemetryEndpoint: Option[String],
                                                      preferredRegionsList: Option[Array[String]],
                                                      subscriptionId: Option[String],
                                                      tenantId: Option[String],
                                                      resourceGroupName: Option[String],
                                                      azureEnvironment: AzureEnvironment)

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

    if (customApplicationNameSuffix.isDefined){
      applicationName = s"$applicationName ${customApplicationNameSuffix.get}"
    }

    CosmosClientConfiguration(
      cosmosAccountConfig.endpoint,
      cosmosAccountConfig.accountName,
      cosmosAccountConfig.authConfig,
      customApplicationNameSuffix,
      applicationName,
      cosmosAccountConfig.useGatewayMode,
      useEventualConsistency,
      enableClientTelemetry = diagnosticsConfig.isClientTelemetryEnabled,
      cosmosAccountConfig.disableTcpConnectionEndpointRediscovery,
      diagnosticsConfig.clientTelemetryEndpoint,
      cosmosAccountConfig.preferredRegionsList,
      cosmosAccountConfig.subscriptionId,
      cosmosAccountConfig.tenantId,
      cosmosAccountConfig.resourceGroupName,
      cosmosAccountConfig.azureEnvironment)
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
