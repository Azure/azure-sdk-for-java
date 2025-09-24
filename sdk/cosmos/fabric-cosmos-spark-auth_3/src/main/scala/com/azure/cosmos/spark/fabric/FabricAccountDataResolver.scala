// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark.fabric

import com.azure.cosmos.spark.AccountDataResolver
import com.azure.cosmos.spark.CosmosAccessToken
import com.azure.cosmos.spark.CosmosConfigNames.AccountDataResolverServiceName
import com.fasterxml.jackson.databind.ObjectMapper

import java.time.{Instant, OffsetDateTime, ZoneOffset}
import scala.collection.concurrent.TrieMap

class FabricAccountDataResolver extends AccountDataResolver with BasicLoggingTrait {

  override def getAccountDataConfig(configs: Map[String, String]): Map[String, String] = {
    val configParameters = ConfigParameters.apply(configs)
    if (isEnabled(configParameters)) {
      configs + ("spark.cosmos.auth.type" -> "AccessToken")
    } else {
      configs
    }
  }

  private def isEnabled(configs: ConfigParameters): Boolean = {
    configs.accountDataResolverServiceName.isDefined && configs.accountDataResolverServiceName.get
      .equalsIgnoreCase(FabricAccountDataResolver.ACCOUNT_DATA_RESOLVER_SERVICE_NAME)
  }

  override def getAccessTokenProvider(configs: Map[String, String]): Option[List[String] => CosmosAccessToken] = {
    val configParameters = ConfigParameters.apply(configs)
    FabricAccountDataResolver.tokenProviders.getOrElseUpdate(
      configParameters,
      getAccessTokenProviderImpl(configParameters)
    )
  }

  private def getAccessTokenProviderImpl(configs: ConfigParameters): Option[List[String] => CosmosAccessToken] = {
    if (isEnabled(configs)) {
      logInfo(s"FabricAccountDataResolver is enabled")
      Some((_: List[String]) => {
        // obtains the access token from fabric environment
        val accessToken = mssparkutils.credentials.getToken(configs.audience.getOrElse(FabricAccountDataResolver.AUDIENCE))
        // Extract the expiration time from the JWT payload
        val parts = accessToken.split("\\.")
        val payloadJson = new String(java.util.Base64.getUrlDecoder.decode(parts(1)))
        val node = FabricAccountDataResolver.objectMapper.readTree(payloadJson)
        val expirationEpoch = node.get("exp").asLong()
        val expirationTime = OffsetDateTime.ofInstant(
          Instant.ofEpochSecond(expirationEpoch),
          ZoneOffset.UTC
        )
        CosmosAccessToken(accessToken, expirationTime)
      })
    } else {
      logInfo(s"FabricAccountDataResolver is disabled")
      None
    }
  }

}

private object FabricConfigNames {
  val Audience = "spark.cosmos.auth.aad.audience"
}

private case class ConfigParameters(audience: Option[String], accountDataResolverServiceName: Option[String])
private object ConfigParameters {
  def apply(configs: Map[String, String]): ConfigParameters = {
    // lookup keys case-insensitively to accept lowercase variations
    def getIgnoreCase(key: String): Option[String] =
      configs.collectFirst { case (k, v) if k.equalsIgnoreCase(key) => v }
    new ConfigParameters(
      getIgnoreCase(FabricConfigNames.Audience),
      getIgnoreCase(AccountDataResolverServiceName)
    )
  }
}

private object FabricAccountDataResolver {
  private val tokenProviders : TrieMap[ConfigParameters, Option[List[String] => CosmosAccessToken]] =
    new TrieMap[ConfigParameters, Option[List[String] => CosmosAccessToken]]()
  private final val AUDIENCE = "https://cosmos.azure.com/.default"
  private final val ACCOUNT_DATA_RESOLVER_SERVICE_NAME = "com.azure.cosmos.spark.fabric.FabricAccountDataResolver"
  private final val objectMapper = new ObjectMapper()
}
