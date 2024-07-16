// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.CosmosClientBuilder

class TestCosmosClientBuilderInterceptor extends CosmosClientBuilderInterceptor {
  override def process(cosmosClientBuilder: CosmosClientBuilder): CosmosClientBuilder = {
    TestCosmosClientBuilderInterceptor.callback(cosmosClientBuilder)
  }
}

private[spark]  object TestCosmosClientBuilderInterceptor {
  val defaultImplementation: CosmosClientBuilder => CosmosClientBuilder = builder => builder
  var callback: CosmosClientBuilder => CosmosClientBuilder = defaultImplementation
  def setCallback(interceptorCallback: CosmosClientBuilder => CosmosClientBuilder): Unit = {
    callback = interceptorCallback
  }

  def resetCallback(): Unit = {
    callback = defaultImplementation
  }
}
