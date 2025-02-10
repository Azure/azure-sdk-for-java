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

class MasterKeyAccountDataResolver extends AccountDataResolver with BasicLoggingTrait {
  override def getAccountDataConfig(configs: Map[String, String]): Map[String, String] = {
    if (isEnabled(configs)) {
      configs + ("spark.cosmos.accountKey" -> getRequiredConfig(configs, SampleConfigNames.MasterKeySecret))
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
  private[this] object SampleConfigNames {
    val CustomAuthEnabled = "cosmos.auth.sample.enabled"
    val MasterKeySecret = "cosmos.auth.sample.key.secret"
  }

  /**
   * This method will be invoked by the Cosmos DB Spark connector to retrieve access tokens. It will only
   * be used when the config `spark.cosmos.auth.type` is set to `AccessToken` - and in this case
   * the implementation of this trait will need to provide a function that can be used to produce
   * access tokens or None in the case that for the specified configuration no auth can be provided.
   *
   * @param configs the user configuration originally provided
   * @return A function that can be used to provide access tokens
   */
  override def getAccessTokenProvider(configs: Map[String, String]): Option[List[String] => CosmosAccessToken] = ???
}
