// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import com.azure.cosmos.CosmosException
import com.azure.cosmos.implementation.HttpConstants
import com.azure.cosmos.implementation.HttpConstants.SubStatusCodes
import reactor.core.scala.publisher.SMono

private object Exceptions {
  def isResourceExistsException(statusCode: Int): Boolean = {
    statusCode == CosmosConstants.StatusCodes.Conflict
  }

  def isPreconditionFailedException(statusCode: Int): Boolean = {
    statusCode == CosmosConstants.StatusCodes.PreconditionFailed
  }

  def canBeTransientFailure(statusCode: Int, subStatusCode: Int): Boolean = {
    // TODO: moderakh SDK should only throw 503 and not 410,
    // however due a bug in core SDK we currently may throw 410 on write
    // once that's fixed remove GONE here
    statusCode == CosmosConstants.StatusCodes.Gone ||
      statusCode == CosmosConstants.StatusCodes.ServiceUnavailable ||
      statusCode == CosmosConstants.StatusCodes.InternalServerError ||
      statusCode == CosmosConstants.StatusCodes.Timeout ||
      statusCode == CosmosConstants.StatusCodes.NotFound && subStatusCode == 1002
  }

  def isTimeout(statusCode: Int): Boolean = {
    statusCode == CosmosConstants.StatusCodes.Timeout
  }

  def isNotFoundException(throwable: Throwable): Boolean = {
    throwable match {
      case cosmosException: CosmosException =>
        isNotFoundExceptionCore(cosmosException.getStatusCode, cosmosException.getSubStatusCode)
      case _ => false
    }
  }

  def isNotFoundExceptionCore(statusCode: Int, subStatusCode: Int): Boolean = {
      statusCode == CosmosConstants.StatusCodes.NotFound &&
        subStatusCode == 0
  }
}
