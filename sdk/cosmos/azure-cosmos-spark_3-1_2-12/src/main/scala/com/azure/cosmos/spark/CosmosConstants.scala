// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.core.util.CoreUtils
import com.azure.cosmos.implementation.HttpConstants

// cosmos db related constants
private object CosmosConstants {
  private[this] val propertiesFileName = "azure-cosmos-spark.properties"
  val currentVersion: String =
    CoreUtils.getProperties(propertiesFileName).get("version")
  val currentName: String =
    CoreUtils.getProperties(propertiesFileName).get("name")
  val userAgentSuffix = s"SparkConnector/$currentName/$currentVersion"
  val initialMaxRetryIntervalForTransientFailuresInMs = 100
  val maxRetryIntervalForTransientFailuresInMs = 5000
  val maxRetryCountForTransientFailures = 100
  val defaultDirectRequestTimeoutInSeconds = 10L
  val feedRangesCacheIntervalInMinutes = 1
  val defaultIoThreadCountFactorPerCore = 4

  object Names {
    val ItemsDataSourceShortName = "cosmos.oltp"
    val ChangeFeedDataSourceShortName = "cosmos.oltp.changeFeed"
  }

  object Properties {
    val Id = "id"
    val ETag = "_etag"
  }

  object StatusCodes {
    val Conflict = 409
    val ServiceUnavailable = 503
    val InternalServerError = 500
    val Gone = 410
    val Timeout = 408
    val PreconditionFailed = 412
  }

  object SystemProperties {
    val LineSeparator = System.getProperty("line.separator")
  }

  object TableProperties {
    val PartitionKeyDefinition = "CosmosPartitionKeyDefinition"
    val PartitionCount = "CosmosPartitionCount"
    val LastModified = "LastModified"
    val ProvisionedThroughput = "ProvisionedThroughput"
    val IndexingPolicy = "IndexingPolicy"
    val DefaultTtlInSeconds = "DefaultTtlInSeconds"
    val AnalyticalStoreTtlInSeconds = "AnalyticalStoreTtlInSeconds"
  }
}
