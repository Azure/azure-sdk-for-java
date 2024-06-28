// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

import java.lang.management.ManagementFactory

private[spark] case class CosmosClientConfiguration (
                                                      endpoint: String,
                                                      databaseAccountName: String,
                                                      authConfig: CosmosAuthConfig,
                                                      customApplicationNameSuffix: Option[String],
                                                      applicationName: String,
                                                      useGatewayMode: Boolean,
                                                      enforceNativeTransport: Boolean,
                                                      proactiveConnectionInitialization: Option[String],
                                                      proactiveConnectionInitializationDurationInSeconds: Int,
                                                      httpConnectionPoolSize: Int,
                                                      useEventualConsistency: Boolean,
                                                      enableClientTelemetry: Boolean,
                                                      disableTcpConnectionEndpointRediscovery: Boolean,
                                                      clientTelemetryEndpoint: Option[String],
                                                      preferredRegionsList: Option[Array[String]],
                                                      subscriptionId: Option[String],
                                                      tenantId: Option[String],
                                                      resourceGroupName: Option[String],
                                                      azureEnvironmentEndpoints: java.util.Map[String, String],
                                                      sparkEnvironmentInfo: String,
                                                      clientBuilderInterceptors: Option[String])

private[spark] object CosmosClientConfiguration {
  def apply(
             config: Map[String, String],
             useEventualConsistency: Boolean,
             sparkEnvironmentInfo: String): CosmosClientConfiguration = {

    val cosmosAccountConfig = CosmosAccountConfig.parseCosmosAccountConfig(config)
    val diagnosticsConfig = DiagnosticsConfig.parseDiagnosticsConfig(config)

    apply(cosmosAccountConfig, diagnosticsConfig, useEventualConsistency, sparkEnvironmentInfo)
  }

  def apply(
            cosmosAccountConfig: CosmosAccountConfig,
            diagnosticsConfig: DiagnosticsConfig,
            useEventualConsistency: Boolean,
            sparkEnvironmentInfo: String): CosmosClientConfiguration = {

    var applicationName = CosmosConstants.userAgentSuffix

    if (!sparkEnvironmentInfo.isEmpty) {
      applicationName = s"$applicationName|$sparkEnvironmentInfo"
    }

    val runtimeInfo = runtimeInformation()
    if (runtimeInfo.isDefined) {
      applicationName = s"$applicationName|${runtimeInfo.get}"
    }

    val customApplicationNameSuffix = cosmosAccountConfig.applicationName
    if (customApplicationNameSuffix.isDefined){
      applicationName = s"$applicationName|${customApplicationNameSuffix.get}"
    }

    CosmosClientConfiguration(
      cosmosAccountConfig.endpoint,
      cosmosAccountConfig.accountName,
      cosmosAccountConfig.authConfig,
      customApplicationNameSuffix,
      applicationName,
      cosmosAccountConfig.useGatewayMode,
      cosmosAccountConfig.enforceNativeTransport,
      cosmosAccountConfig.proactiveConnectionInitialization,
      cosmosAccountConfig.proactiveConnectionInitializationDurationInSeconds,
      cosmosAccountConfig.httpConnectionPoolSize,
      useEventualConsistency,
      enableClientTelemetry = diagnosticsConfig.isClientTelemetryEnabled,
      cosmosAccountConfig.disableTcpConnectionEndpointRediscovery,
      diagnosticsConfig.clientTelemetryEndpoint,
      cosmosAccountConfig.preferredRegionsList,
      cosmosAccountConfig.subscriptionId,
      cosmosAccountConfig.tenantId,
      cosmosAccountConfig.resourceGroupName,
      cosmosAccountConfig.azureEnvironmentEndpoints,
      sparkEnvironmentInfo,
      cosmosAccountConfig.clientBuilderInterceptors)
  }

  private[spark] def getSparkEnvironmentInfo(sessionOption: Option[SparkSession]): String = {
    sessionOption match {
      case Some(session) => getSparkEnvironmentInfoFromConfig(Some(session.sparkContext.getConf))
      case _ => ""
    }
  }

  private[spark] def getSparkEnvironmentInfoFromConfig(configOption: Option[SparkConf]): String = {
    configOption match {
      case Some(config) =>
        if (config.contains("spark.databricks.clusterUsageTags.orgId")) {
          val workspaceId = config.get("spark.databricks.clusterUsageTags.orgId", "")
          val clusterName = config.get("spark.databricks.clusterUsageTags.clusterName", "").take(32)

          s"DBX|$workspaceId|$clusterName"
        } else if (config.contains("spark.synapse.workspace.name")) {
          val workspaceId = config.get("spark.synapse.workspace.name", "")
          val clusterName = config.get("spark.synapse.pool.name", "").take(32)
          s"SYN|$workspaceId|$clusterName"
        } else {
          ""
        }
      case _ => ""
    }
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
