// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.CosmosException
import com.azure.cosmos.implementation.HttpConstants
import reactor.core.scala.publisher.SMono

private object Exceptions {
  def isResourceExistsException(cosmosException: CosmosException): Boolean = {
    cosmosException.getStatusCode == CosmosConstants.StatusCodes.Conflict
  }

  def isPreconditionFailedException(cosmosException: CosmosException): Boolean = {
    cosmosException.getStatusCode == CosmosConstants.StatusCodes.PreconditionFailed
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

  def isTimeout(cosmosException: CosmosException): Boolean = {
    cosmosException.getStatusCode == CosmosConstants.StatusCodes.Timeout
  }

  def isNotFoundException(throwable: Throwable): Boolean = {
    throwable match {
      case cosmosException: CosmosException =>
        isNotFoundExceptionCore(cosmosException)
      case _ => false
    }
  }

  def isNotFoundExceptionCore(cosmosException: CosmosException): Boolean = {
      cosmosException.getStatusCode == HttpConstants.StatusCodes.NOTFOUND &&
        cosmosException.getSubStatusCode == 0
  }
}
