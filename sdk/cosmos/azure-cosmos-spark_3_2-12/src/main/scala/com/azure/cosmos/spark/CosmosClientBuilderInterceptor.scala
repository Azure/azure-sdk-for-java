// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.CosmosClientBuilder

/**
 * The CosmosClientBuilderInterceptor trait is used to allow spark environments to provide customizations of the
 * Cosmos client builder configuration - for example to enable diagnostics
 */
trait CosmosClientBuilderInterceptor {

  /**
   * This method will be invoked by the Cosmos DB Spark connector when instantiating new CosmosClients. If the
   * returned function is defined, it will be invoked to allow making modifications on the client builder - for
   * example to configure diagnostics - before the builder is used to instantiate a new client instance.
   * NOTE: It is important that implementations of this trait return singleton functions in
   * getClientBuilderInterceptor when applicable based on the configs passed in. Each new function instance
   * will result in a new CosmosClient being created in the cache - intentionally because the
   * CosmosClientBuilderInterceptor implementation might choose completely different interceptions based
   * on the config. The best pattern to achieve this would be to map the configs Map to a case class
   * containing the config values relevant to your CosmosClientBuilderInterceptor implementation - then you can
   * use a TrieMap with the config case class as key and the function implementation as value
   * A sample implementing this pattern is under azure-cosmos-spark-account-data-resolver-sample
   * in this repo - see the 'LoggingClientBuilderInterceptor' class.
   *
   * @param configs the user configuration originally provided
   * @return A function that is used to allow changing client builder before instantiating a new client instance
   *         to be added to the cache
   */
  def getClientBuilderInterceptor(configs : Map[String, String]): Option[CosmosClientBuilder => CosmosClientBuilder]
}
