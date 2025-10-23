// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

/**
 * The CosmosClientInterceptor trait is used to allow spark environments to provide customizations of the
 * Cosmos client configuration - for example to inject faults
 */
trait WriteOnRetryCommitInterceptor {

  /**
   * This method will be invoked by the Cosmos DB Spark connector before retrying a commit during writes (currently
   * only when bulk mode is enabled).
   *
   * @return A function that will be invoked before retrying a commit on the write path.
   */
  def beforeRetryCommit():Unit
}
