// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark.fabric

import com.azure.cosmos.spark.AccountDataResolver
import com.azure.cosmos.spark.CosmosAccessToken
import com.fasterxml.jackson.databind.ObjectMapper

import java.time.{Instant, OffsetDateTime, ZoneOffset}
import scala.collection.concurrent.TrieMap

class FabricAccountDataResolver extends AccountDataResolver with BasicLoggingTrait {
    private val tokenProviders : TrieMap[ConfigParameters, Option[List[String] => CosmosAccessToken]] =
            new TrieMap[ConfigParameters, Option[List[String] => CosmosAccessToken]]()
    private final val AUDIENCE = "https://cosmos.azure.com/.default"


  override def getAccountDataConfig(configs: Map[String, String]): Map[String, String] = {
    val configParameters = ConfigParameters.apply(configs)
    if (isEnabled(configParameters)) {
      configs + ("spark.cosmos.auth.type" -> "AccessToken")
    } else {
      configs
    }
  }

  private def isEnabled(configs: ConfigParameters): Boolean = {
    configs.isEnabledConfigValue.isDefined && configs.isEnabledConfigValue.get.toBoolean
  }

  override def getAccessTokenProvider(configs: Map[String, String]): Option[List[String] => CosmosAccessToken] = {
    val configParameters = ConfigParameters.apply(configs)
    tokenProviders.getOrElseUpdate(
      configParameters,
      getAccessTokenProviderImpl(configParameters)
    )
  }

  private def getAccessTokenProviderImpl(configs: ConfigParameters): Option[List[String] => CosmosAccessToken] = {
    if (isEnabled(configs)) {
      logInfo(s"FabricAccountDataResolver is enabled")
      Some((_: List[String]) => {
        var accessToken = ""
        accessToken = mssparkutils.credentials.getToken(configs.audience.getOrElse(AUDIENCE))
        val parts = accessToken.split("\\.")
        val payloadJson = new String(java.util.Base64.getUrlDecoder.decode(parts(1)))
        val mapper = new ObjectMapper()
        val node = mapper.readTree(payloadJson)

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

  private[this] object FabricConfigNames {
    val Audience = "cosmos.auth.fabric.audience"
    val CustomAuthEnabled = "cosmos.auth.fabric.enabled"
  }

  private case class ConfigParameters(audience: Option[String], isEnabledConfigValue: Option[String])
  private object ConfigParameters {
    def apply(configs: Map[String, String]): ConfigParameters = {
      new ConfigParameters(
        configs.get(FabricConfigNames.Audience),
        configs.get(FabricConfigNames.CustomAuthEnabled)
      )
    }
  }
}
