// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import java.lang.management.ManagementFactory

private[spark] case class CosmosClientConfiguration (
                                                      endpoint: String,
                                                      key: String,
                                                      applicationName: String,
                                                      useGatewayMode: Boolean,
                                                      useEventualConsistency: Boolean,
                                                      preferredRegionsList: Option[Array[String]])

private[spark] object CosmosClientConfiguration {
  def apply(
             config: Map[String, String],
             useEventualConsistency: Boolean): CosmosClientConfiguration = {
    val cosmosAccountConfig = CosmosAccountConfig.parseCosmosAccountConfig(config)
    var applicationName = CosmosConstants.userAgentSuffix
    val runtimeInfo = runtimeInformation()
    if (runtimeInfo.isDefined) {
      applicationName = s"$applicationName ${runtimeInfo.get}"
    }

    if (cosmosAccountConfig.applicationName.isDefined){
      applicationName = s"$applicationName ${cosmosAccountConfig.applicationName.get}"
    }

    CosmosClientConfiguration(
      cosmosAccountConfig.endpoint,
      cosmosAccountConfig.key,
      applicationName,
      cosmosAccountConfig.useGatewayMode,
      useEventualConsistency,
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
