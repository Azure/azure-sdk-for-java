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
   * @param configs the user configuration originally provided
   * @return A function that can be used to provide access tokens
   */
  def getAccessTokenProvider(configs : Map[String, String]): Option[List[String] => CosmosAccessToken]
}
