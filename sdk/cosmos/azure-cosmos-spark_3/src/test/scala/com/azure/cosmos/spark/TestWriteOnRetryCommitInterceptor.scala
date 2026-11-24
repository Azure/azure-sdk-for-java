// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

class TestWriteOnRetryCommitInterceptor extends WriteOnRetryCommitInterceptor {
  /**
   * This method will be invoked by the Cosmos DB Spark connector before retrying a commit during writes (currently
   * only when bulk mode is enabled).
   *
   * @return A function that will be invoked before retrying a commit on the write path.
   */
  override def beforeRetryCommit(): Unit =
  {
    TestWriteOnRetryCommitInterceptor.callback.apply()
  }
}

private[spark]  object TestWriteOnRetryCommitInterceptor {
  val defaultImplementation: () => Unit = () => {}
  var callback: () => Unit = defaultImplementation
  def setCallback(interceptorCallback: () => Unit): Unit = {
    callback = interceptorCallback
  }

  def resetCallback(): Unit = {
    callback = defaultImplementation
  }
}
