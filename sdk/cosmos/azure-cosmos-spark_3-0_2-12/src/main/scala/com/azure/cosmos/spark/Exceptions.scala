// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.CosmosException

private object Exceptions {
  def isResourceExistsException(cosmosException: CosmosException): Boolean = {
    cosmosException.getStatusCode == CosmosConstants.StatusCodes.Conflict
  }

  def canBeTransientFailure(cosmosException: CosmosException): Boolean = {
    // TODO: moderakh SDK should only throw 503 and not 410,
    // however due a bug in core SDK we currently may throw 410 on write
    // once that's fixed remove GONE here
    cosmosException.getStatusCode == CosmosConstants.StatusCodes.Gone ||
      cosmosException.getStatusCode == CosmosConstants.StatusCodes.ServiceUnavailable ||
      cosmosException.getStatusCode == CosmosConstants.StatusCodes.InternalServerError ||
    cosmosException.getStatusCode == CosmosConstants.StatusCodes.Timeout
  }
}
