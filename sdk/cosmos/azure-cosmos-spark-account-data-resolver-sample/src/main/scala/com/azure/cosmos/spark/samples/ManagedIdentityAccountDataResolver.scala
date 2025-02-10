// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark.samples

import com.azure.core.credential.{TokenCredential, TokenRequestContext}
import com.azure.cosmos.spark.{AccountDataResolver, CosmosAccessToken}
import com.azure.identity.ManagedIdentityCredentialBuilder

import scala.collection.concurrent.TrieMap

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

class ManagedIdentityAccountDataResolver extends AccountDataResolver with BasicLoggingTrait {
  private val tokenProviders : TrieMap[ConfigParameters, Option[List[String] => CosmosAccessToken]] =
    new TrieMap[ConfigParameters, Option[List[String] => CosmosAccessToken]]()

  override def getAccountDataConfig(configs: Map[String, String]): Map[String, String] = {
    val configParameters = ConfigParameters.apply(configs)
    if (isEnabled(configParameters)) {
      configs +
        ("spark.cosmos.auth.type" -> "AccessToken") +
        ("spark.cosmos.account.tenantId" -> getRequiredConfig(configs, SampleConfigNames.TenantId)) +
        ("spark.cosmos.account.subscriptionId" -> getRequiredConfig(configs, SampleConfigNames.SubscriptionId)) +
        ("spark.cosmos.account.resourceGroupName" -> getRequiredConfig(configs, SampleConfigNames.ResourceGroupName))
    } else {
      configs
    }
  }

  private def getRequiredConfig(configs: Map[String, String], configName: String): String = {
    val valueOpt = configs.get(configName)
    assert(valueOpt.isDefined, s"Parameter '$configName' is missing.")
    valueOpt.get
  }

  private def isEnabled(configs: ConfigParameters): Boolean = {
    configs.isEnabledConfigValue.isDefined && configs.isEnabledConfigValue.get.toBoolean
  }

  private def getManagedIdentityTokenCredential(configs: ConfigParameters): Option[TokenCredential] = {
    logInfo(s"Constructing ManagedIdentity TokenCredential")
    val tokenCredentialBuilder = new ManagedIdentityCredentialBuilder()
    if (configs.clientId.isDefined) {
      tokenCredentialBuilder.clientId(configs.clientId.get)
    }

    if (configs.resourceId.isDefined) {
      tokenCredentialBuilder.resourceId(configs.resourceId.get)
    }

    Some(tokenCredentialBuilder.build())
  }

  private def getTokenCredential(configs: ConfigParameters): Option[TokenCredential] = {
    if (configs.authType.equalsIgnoreCase(SampleAuthTypes.ManagedIdentity)) {
      logInfo(s"Managed identity used")
      getManagedIdentityTokenCredential(configs)
    } else {
      logError(s"Invalid authType '${configs.authType}'.")
      assert(assertion = false, s"Invalid authType '${configs.authType}'.")
      None
    }
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
      val tokenCredential = getTokenCredential(configs)

      if (tokenCredential.isDefined) {
        logInfo(s"TokenCredential found - and access token provider used")
        Some((tokenRequestContextStrings: List[String]) => {
          val tokenRequestContext = new TokenRequestContext
          tokenRequestContext.setScopes(tokenRequestContextStrings.asJava)
          val accessToken = tokenCredential
            .get
            .getToken(tokenRequestContext)
            .block()
          CosmosAccessToken(accessToken.getToken, accessToken.getExpiresAt)
        })
      } else {
        logWarning(s"No TokenCredential provided")
        None
      }
    } else {
      logInfo(s"SampleAccountDataResolver is disabled")
      None
    }
  }

  private[this] object SampleConfigNames {
    val AuthType = "cosmos.auth.sample.authType"
    val CustomAuthEnabled = "cosmos.auth.sample.enabled"
    val ManagedIdentityClientId = "cosmos.auth.sample.managedIdentity.clientId"
    val ManagedIdentityResourceId = "cosmos.auth.sample.managedIdentity.resourceId"
    val ResourceGroupName = "cosmos.auth.sample.resourceGroupName"
    val SubscriptionId = "cosmos.auth.sample.subscriptionId"
    val TenantId = "cosmos.auth.sample.tenantId"
  }

  private[this] object SampleAuthTypes {
    val ManagedIdentity: String = "managedidentity"
  }

  private case class ConfigParameters(authType: String, clientId: Option[String], resourceId:Option[String], isEnabledConfigValue: Option[String])
  private object ConfigParameters {
    def apply(configs: Map[String, String]): ConfigParameters = {
      new ConfigParameters(
        getRequiredConfig(configs, SampleConfigNames.AuthType),
        configs.get(SampleConfigNames.ManagedIdentityClientId),
        configs.get(SampleConfigNames.ManagedIdentityResourceId),
        configs.get(SampleConfigNames.CustomAuthEnabled))
    }
  }
}
