// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.core.util.CoreUtils
import com.azure.cosmos.implementation.HttpConstants

// cosmos db related constants
private object CosmosConstants {
  private[this] val currentVersion =
    CoreUtils.getProperties(HttpConstants.Versions.AZURE_COSMOS_PROPERTIES_FILE_NAME).get("version")
  val userAgentSuffix = s" SparkConnector/$currentVersion"

  object Names {
    val ItemsDataSourceShortName = "cosmos.items"
    val ChangeFeedDataSourceShortName = "cosmos.changeFeed"
  }

  object Properties {
    val Id = "id"
  }

  object StatusCodes {
    val Conflict = 409
    val ServiceUnavailable = 503
    val InternalServerError = 500
    val Gone = 410
    val Timeout = 408
  }
}
