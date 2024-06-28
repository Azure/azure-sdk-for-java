// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.cosmos.CosmosClientBuilder

/**
 * The CosmosClientBuilderInterceptor trait is used to allow spark environments to provide customizations of the
 * Cosmos client builder configuration
 */
trait CosmosClientBuilderInterceptor {
  def process(cosmosClientBuilder : CosmosClientBuilder): CosmosClientBuilder
}
