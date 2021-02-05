// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

// cosmos db related constants
private object CosmosConstants {
  val CosmosIdFieldName = "id"

  object StatusCodes {
    val Conflict = 409
    val ServiceUnavailable = 503
    val InternalServerError = 500
    val Gone = 410
    val Timeout = 408
  }
}
