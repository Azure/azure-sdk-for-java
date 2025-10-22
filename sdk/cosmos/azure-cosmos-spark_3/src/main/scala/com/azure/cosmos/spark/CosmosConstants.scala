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
  val userAgentSuffix = s"SparkConnector|$currentName|$currentVersion"
  val initialMaxRetryIntervalForTransientFailuresInMs = 100
  val maxRetryIntervalForTransientFailuresInMs = 5000
  val maxRetryCountForTransientFailures = 100
  val defaultDirectRequestTimeoutInSeconds = 10L
  val defaultIoThreadCountFactorPerCore = 4
  val smallestPossibleReactorQueueSizeLargerThanOne: Int = math.min(8, Queues.XS_BUFFER_SIZE)
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

    val ChangeFeedLsnRange = "cosmos.changeFeed.partition.lsnRange"
    val ChangeFeedItemsCnt = "cosmos.changeFeed.partition.itemsCnt"
    val ChangeFeedPartitionIndex = "cosmos.changeFeed.partition.index"

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

  object ChangeFeedMetricsConfigs {
    private val MetricsHistoryPropertyName = "spark.cosmos.changeFeed.performance.metrics.history"
    private val MetricsHistoryEnvName = "SPARK.COSMOS.CHANGEFEED.PERFORMANCE.METRICS.HISTORY"
    private val DefaultMetricsHistory = "5"
    private val MetricsHistoryDecayFactorPropertyName = "spark.cosmos.changeFeed.performance.metrics.decayFactor"
    private val MetricsHistoryDecayFactorEnvName = "SPARK.COSMOS.CHANGEFEED.PERFORMANCE.METRICS.DECAYFACTOR"
    private val DefaultMetricsHistoryDecayFactor = "0.85"
    val MetricsHistory: Int =
      Option(System.getProperty(MetricsHistoryPropertyName))
        .orElse(sys.env.get(MetricsHistoryEnvName))
        .getOrElse(DefaultMetricsHistory).toInt
    val MetricsHistoryDecayFactor: Double =
      Option(System.getProperty(MetricsHistoryDecayFactorPropertyName))
       .orElse(sys.env.get(MetricsHistoryDecayFactorEnvName))
       .getOrElse(DefaultMetricsHistoryDecayFactor).toDouble
  }
}
