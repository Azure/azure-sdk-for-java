// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.CosmosAsyncClient

/**
 * The CosmosClientInterceptor trait is used to allow spark environments to provide customizations of the
 * Cosmos client configuration - for example to inject faults
 */
trait CosmosClientInterceptor {

  /**
   * This method will be invoked by the Cosmos DB Spark connector when instantiating new CosmosClients. If the
   * returned function is defined, it will be invoked to allow making modifications on the client - for example to
   * inject faults.
   * NOTE: It is important that implementations of this trait return singleton functions in
   * getClientInterceptor when applicable based on the configs passed in. Each new function instance
   * will result in a new CosmosClient being created in the cache - intentionally because the
   * CosmosClientInterceptor implementation might choose completely different interceptions based
   * on the config. The best pattern to achieve this would be to map the configs Map to a case class
   * containing the config values relevant to your CosmosClientInterceptor implementation - then you can
   * use a TrieMap with the config case class as key and the function implementation as value
   * A sample implementing this pattern is under azure-cosmos-spark-account-data-resolver-sample
   * in this repo - see the 'FaultInjectingClientInterceptor' class.
   *
   * @param configs the user configuration originally provided
   * @return A function that is used to allow changing the new client instance to be added to the cache
   */
  def getClientInterceptor(configs : Map[String, String]): Option[CosmosAsyncClient => CosmosAsyncClient]
}
