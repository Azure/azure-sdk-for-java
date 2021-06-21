// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.ai.metricsadvisor.models.InfluxDbDataFeedSource;

/**
 * The {@link DataFeedSource} represents the base type for different
 * types of data sources that service can ingest data and perform
 * anomaly detection.
 *
 * @see AzureAppInsightsDataFeedSource
 * @see AzureBlobDataFeedSource
 * @see AzureCosmosDbDataFeedSource
 * @see AzureDataExplorerDataFeedSource
 * @see AzureDataLakeStorageGen2DataFeedSource
 * @see AzureEventHubsDataFeedSource
 * @see AzureLogAnalyticsDataFeedSource
 * @see AzureTableDataFeedSource
 * @see InfluxDbDataFeedSource
 * @see MongoDbDataFeedSource
 * @see MySqlDataFeedSource
 * @see PostgreSqlDataFeedSource
 * @see SqlServerDataFeedSource
 */
public abstract class DataFeedSource {
    // No common properties, used only as discriminator type.
}
