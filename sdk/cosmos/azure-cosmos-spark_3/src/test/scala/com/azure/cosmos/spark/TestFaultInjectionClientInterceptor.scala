// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.CosmosAsyncClient

class TestFaultInjectionClientInterceptor extends CosmosClientInterceptor {
  override def getClientInterceptor(configs: Map[String, String]): Option[CosmosAsyncClient => CosmosAsyncClient] = {
    Some(TestFaultInjectionClientInterceptor.callback)
  }
}

private[spark]  object TestFaultInjectionClientInterceptor {
  val defaultImplementation: CosmosAsyncClient => CosmosAsyncClient = client => client
  var callback: CosmosAsyncClient => CosmosAsyncClient = defaultImplementation
  def setCallback(interceptorCallback: CosmosAsyncClient => CosmosAsyncClient): Unit = {
    callback = interceptorCallback
  }

  def resetCallback(): Unit = {
    callback = defaultImplementation
  }
}
