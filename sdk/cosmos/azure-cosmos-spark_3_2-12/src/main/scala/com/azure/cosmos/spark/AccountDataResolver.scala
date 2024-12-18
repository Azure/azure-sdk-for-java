// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

/**
 * The AccountDataResolver trait is used to allow spark environments to provide custom authentication
 */
trait AccountDataResolver {
  /**
   * This method will be invoked during initialization to allow a spark environment to customize the account
   * configuration
   * @param configs the user configuration originally provided
   * @return the configuration with any projections done
   */
  def getAccountDataConfig(configs : Map[String, String]): Map[String, String]

  /**
   * This method will be invoked by the Cosmos DB Spark connector to retrieve access tokens. It will only
   * be used when the config `spark.cosmos.auth.type` is set to `AccessToken` - and in this case
   * the implementation of this trait will need to provide a function that can be used to produce
   * access tokens or None in the case that for the specified configuration no auth can be provided.
   * NOTE: It is important that implementations of this trait return singleton functions in
   * getAccessTokenProvider when applicable based on the configs passed in. Each new function instance
   * will result in a new CosmosClient being created in the cache - intentionally because the
   * AccountDataResolver implementation might choose completely different auth implementation based
   * on the config. The best pattern to achieve this would be to map the configs Map to a case class
   * containing the config values relevant to your AccountDataResolver implementation - then you can
   * use a TrieMap with the config case class as key and the function implementation as value
   * A sample implementing this pattern is under azure-cosmos-spark-account-data-resolver-sample
   * in this repo - see the 'ManagedIdentityAccountDataResolver' or
   * 'ServicePrincipalAccountDataResolver' classes.
   * @param configs the user configuration originally provided
   * @return A function that can be used to provide access tokens
   */
  def getAccessTokenProvider(configs : Map[String, String]): Option[List[String] => CosmosAccessToken]
}
