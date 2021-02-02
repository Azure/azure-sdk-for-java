// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

// cosmos db related constants
private object CosmosConstants {
  val CosmosIdFieldName = "id"

  object StatusCodes {
    val ConflictStatusCode = 409
    val ServerError = 503
    val Gone = 410
    val Timeout = 408
  }
}
