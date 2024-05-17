// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark.samples

import com.azure.core.credential.{TokenCredential, TokenRequestContext}
import com.azure.cosmos.spark.{AccountDataResolver, CosmosAccessToken}
import com.azure.identity.{ClientCertificateCredentialBuilder, ClientSecretCredentialBuilder, ManagedIdentityCredentialBuilder}

import java.io.ByteArrayInputStream
import java.util.Base64

// scalastyle:off underscore.import
import scala.collection.JavaConverters._
// scalastyle:on underscore.import

class SampleAccountDataResolver extends AccountDataResolver with BasicLoggingTrait {
  override def getAccountDataConfig(configs: Map[String, String]): Map[String, String] = {
    if (isEnabled(configs)) {
      val authType = getRequiredConfig(configs, SampleConfigNames.AuthType)
      if (authType.equalsIgnoreCase(SampleAuthTypes.MasterKey)) {
        configs + ("spark.cosmos.accountKey" -> getRequiredConfig(configs, SampleConfigNames.MasterKeySecret))
      } else {
        configs +
          ("spark.cosmos.auth.type" -> "AccessToken") +
          ("spark.cosmos.account.tenantId" -> getRequiredConfig(configs, SampleConfigNames.TenantId)) +
          ("spark.cosmos.account.subscriptionId" -> getRequiredConfig(configs, SampleConfigNames.SubscriptionId)) +
          ("spark.cosmos.account.resourceGroupName" -> getRequiredConfig(configs, SampleConfigNames.ResourceGroupName))
      }
    } else {
      configs
    }
  }

  private def getRequiredConfig(configs: Map[String, String], configName: String): String = {
    val valueOpt = configs.get(configName)
    assert(valueOpt.isDefined, s"Parameter '$configName' is missing.")
    valueOpt.get
  }

  private def isEnabled(configs: Map[String, String]): Boolean = {
    val enabled = configs.get(SampleConfigNames.CustomAuthEnabled)
    enabled.isDefined && enabled.get.toBoolean
  }

  private def getManagedIdentityTokenCredential(configs: Map[String, String]): Option[TokenCredential] = {
    logInfo(s"Constructing ManagedIdentity TokenCredential")
    val tokenCredentialBuilder = new ManagedIdentityCredentialBuilder()
    if (configs.contains(SampleConfigNames.ManagedIdentityClientId)) {
      tokenCredentialBuilder.clientId(configs(SampleConfigNames.ManagedIdentityClientId))
    }

    if (configs.contains(SampleConfigNames.ManagedIdentityResourceId)) {
      tokenCredentialBuilder.resourceId(configs(SampleConfigNames.ManagedIdentityResourceId))
    }

    Some(tokenCredentialBuilder.build())
  }

  private def getServicePrincipalTokenCredential(configs: Map[String, String]): Option[TokenCredential] = {
    logInfo(s"Constructing ServicePrincipal TokenCredential")
    val tenantId = getRequiredConfig(configs, SampleConfigNames.TenantId)
    val clientId = getRequiredConfig(configs, SampleConfigNames.ServicePrincipalClientId)

    if (configs.contains(SampleConfigNames.ServicePrincipalCert)) {

      val certInputStream = new ByteArrayInputStream(Base64.getDecoder.decode(configs.get(SampleConfigNames.ServicePrincipalCert).get))

      Some(new ClientCertificateCredentialBuilder()
        .authorityHost("https://login.microsoftonline.com/")
        .tenantId(tenantId)
        .clientId(clientId)
        .pemCertificate(certInputStream)
        .build())
    } else if (configs.contains(SampleConfigNames.ClientSecret)) {
      Some(new ClientSecretCredentialBuilder()
        .authorityHost("https://login.microsoftonline.com/")
        .tenantId(tenantId)
        .clientId(clientId)
        .clientSecret(configs(SampleConfigNames.ClientSecret))
        .build())
    } else {
      logError("Neither client secret nor client certificate are configured for service principal auth.")
      assert(assertion = false, "Neither client secret nor client certificate are configured for service principal auth.")
      None
    }
  }

  private def getTokenCredential(configs: Map[String, String]): Option[TokenCredential] = {
    val authType = getRequiredConfig(configs, SampleConfigNames.AuthType)
    if (authType.equalsIgnoreCase(SampleAuthTypes.ServicePrincipal)) {
      logInfo(s"Service principal used")
      getServicePrincipalTokenCredential(configs)
    } else if (authType.equalsIgnoreCase(SampleAuthTypes.ManagedIdentity)) {
      logInfo(s"Managed identity used")
      getManagedIdentityTokenCredential(configs)
    } else if (authType.equalsIgnoreCase(SampleAuthTypes.MasterKey)) {
      logInfo(s"Master key used")
      None
    } else {
      logError(s"Invalid authType '$authType'.")
      assert(assertion = false, s"Invalid authType '$authType'.")
      None
    }
  }

  override def getAccessTokenProvider(configs: Map[String, String]): Option[List[String] => CosmosAccessToken] = {
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
    val ManagedIdentityClientId = "cosmos.auth.sample.managedIdentity.clientId"
    val ManagedIdentityResourceId = "cosmos.auth.sample.managedIdentity.resourceId"
    val MasterKeySecret = "cosmos.auth.sample.key.secret"
    val ResourceGroupName = "cosmos.auth.sample.resourceGroupName"
    val ServicePrincipalCert= "cosmos.auth.sample.serviceprincipal.cert"
    val ServicePrincipalClientId = "cosmos.auth.sample.serviceprincipal.clientId"
    val SubscriptionId = "cosmos.auth.sample.subscriptionId"
    val TenantId = "cosmos.auth.sample.tenantId"
  }

  private[this] object SampleAuthTypes {
    val ManagedIdentity: String = "managedidentity"
    val MasterKey: String = "masterkey"
    val ServicePrincipal: String = "serviceprincipal"
  }
}
