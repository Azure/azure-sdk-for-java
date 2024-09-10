// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import com.azure.core.util.CoreUtils
import com.azure.cosmos.implementation.HttpConstants
import reactor.util.concurrent.Queues

// cosmos db related constants
private[cosmos] object CosmosConstants {
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
  val defaultHttpRequestTimeoutInSeconds = 70L
  val feedRangesCacheIntervalInMinutes = 1L
  val defaultIoThreadCountFactorPerCore = 4
  val smallestPossibleReactorQueueSizeLargerThanOne: Int = math.min(8, Queues.XS_BUFFER_SIZE)
  val defaultMetricsIntervalInSeconds = 60
  val defaultSlf4jMetricReporterEnabled = false
  val readOperationEndToEndTimeoutInSeconds = 65
  val batchOperationEndToEndTimeoutInSeconds = 65

  object Names {
    val ItemsDataSourceShortName = "cosmos.oltp"
    val ChangeFeedDataSourceShortName = "cosmos.oltp.changeFeed"
  }

  object MetricNames {
    val BytesWritten = "bytesWritten"
    val RecordsWritten = "recordsWritten"
    val TotalRequestCharge = "cosmos.totalRequestCharge"

    val KnownCustomMetricNames: Set[String] = Set(TotalRequestCharge)
  }

  object Properties {
    val Id = "id"
    val ETag = "_etag"
    val ItemIdentity = "_itemIdentity"
  }

  object StatusCodes {
    val Conflict: Int = HttpConstants.StatusCodes.CONFLICT
    val ServiceUnavailable: Int = HttpConstants.StatusCodes.SERVICE_UNAVAILABLE
    val InternalServerError: Int = HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR
    val Gone: Int = HttpConstants.StatusCodes.GONE
    val Timeout: Int = HttpConstants.StatusCodes.REQUEST_TIMEOUT
    val PreconditionFailed: Int = HttpConstants.StatusCodes.PRECONDITION_FAILED
    val NotFound: Int = HttpConstants.StatusCodes.NOTFOUND
    val BadRequest: Int = HttpConstants.StatusCodes.BADREQUEST
  }

  object SystemProperties {
    val LineSeparator: String = System.getProperty("line.separator")
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
