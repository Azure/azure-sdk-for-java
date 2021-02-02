// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.CosmosException

private object Exceptions {
  def isResourceExistsException(cosmosException: CosmosException) : Boolean = {
    cosmosException.getStatusCode == CosmosConstants.StatusCodes.ConflictStatusCode
  }
}
