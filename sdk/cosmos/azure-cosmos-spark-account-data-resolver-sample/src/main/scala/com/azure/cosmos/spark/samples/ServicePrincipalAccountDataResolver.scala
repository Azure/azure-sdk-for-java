// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark.samples

import com.azure.core.credential.{TokenCredential, TokenRequestContext}
import com.azure.cosmos.spark.{AccountDataResolver, CosmosAccessToken}
import com.azure.identity.{ClientCertificateCredentialBuilder, ClientSecretCredentialBuilder}

import java.io.ByteArrayInputStream
import java.util.Base64
import scala.collection.concurrent.TrieMap

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

class ServicePrincipalAccountDataResolver extends AccountDataResolver with BasicLoggingTrait {
  private val tokenProviders: TrieMap[ConfigParameters, Option[List[String] => CosmosAccessToken]] =
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

  private def getServicePrincipalTokenCredential(configs: ConfigParameters): Option[TokenCredential] = {
    logInfo(s"Constructing ServicePrincipal TokenCredential")

    if (configs.cert.isDefined) {
      val sendChain = configs.sendChainConfigValue match {
        case Some(sendChainText) => sendChainText.toBoolean
        case None=> false
      }

      val certInputStream = new ByteArrayInputStream(Base64.getDecoder.decode(configs.cert.get))

      Some(new ClientCertificateCredentialBuilder()
        .authorityHost("https://login.microsoftonline.com/")
        .tenantId(configs.tenantId)
        .clientId(configs.clientId)
        .pemCertificate(certInputStream)
        .sendCertificateChain(sendChain)
        .build())
    } else if (configs.clientSecret.isDefined) {
      Some(new ClientSecretCredentialBuilder()
        .authorityHost("https://login.microsoftonline.com/")
        .tenantId(configs.tenantId)
        .clientId(configs.clientId)
        .clientSecret(configs.clientSecret.get)
        .build())
    } else {
      logError("Neither client secret nor client certificate are configured for service principal auth.")
      assert(assertion = false, "Neither client secret nor client certificate are configured for service principal auth.")
      None
    }
  }

  private def getTokenCredential(configs: ConfigParameters): Option[TokenCredential] = {
    if (configs.authType.equalsIgnoreCase(SampleAuthTypes.ServicePrincipal)) {
      logInfo(s"Service principal used")
      getServicePrincipalTokenCredential(configs)
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
    val ClientSecret = "cosmos.auth.sample.serviceprincipal.clientsecret"
    val CustomAuthEnabled = "cosmos.auth.sample.enabled"
    val ResourceGroupName = "cosmos.auth.sample.resourceGroupName"
    val ServicePrincipalCert= "cosmos.auth.sample.serviceprincipal.cert"
    val ServicePrincipalCertSendChain= "cosmos.auth.sample.serviceprincipal.cert.sendChain"
    val ServicePrincipalClientId = "cosmos.auth.sample.serviceprincipal.clientId"
    val SubscriptionId = "cosmos.auth.sample.subscriptionId"
    val TenantId = "cosmos.auth.sample.tenantId"
  }

  private[this] object SampleAuthTypes {
    val ServicePrincipal: String = "serviceprincipal"
  }

  private case class ConfigParameters(
                                       authType: String,
                                       tenantId: String,
                                       clientId: String,
                                       cert: Option[String],
                                       clientSecret: Option[String],
                                       sendChainConfigValue: Option[String],
                                       isEnabledConfigValue: Option[String])

  private object ConfigParameters {
    def apply(configs: Map[String, String]): ConfigParameters = {
      new ConfigParameters(
        getRequiredConfig(configs, SampleConfigNames.AuthType),
        getRequiredConfig(configs, SampleConfigNames.TenantId),
        getRequiredConfig(configs, SampleConfigNames.ServicePrincipalClientId),
        configs.get(SampleConfigNames.ServicePrincipalCert),
        configs.get(SampleConfigNames.ClientSecret),
        configs.get(SampleConfigNames.ServicePrincipalCertSendChain),
        configs.get(SampleConfigNames.CustomAuthEnabled))
    }
  }
}
