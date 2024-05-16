// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration;

import com.azure.ai.metricsadvisor.administration.models.DataFeedIngestionProgress;
import com.azure.ai.metricsadvisor.administration.models.DataFeedIngestionStatus;
import com.azure.ai.metricsadvisor.administration.models.ListDataFeedIngestionOptions;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import com.azure.core.http.rest.PagedIterable;

import java.time.OffsetDateTime;

/**
 * Sample demonstrates how to list, get progress, and refresh data feed ingestion.
 */
public class DataFeedIngestionSample {
    public static void main(String[] args) {
        final MetricsAdvisorAdministrationClient advisorAdministrationClient =
            new MetricsAdvisorAdministrationClientBuilder()
                .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
                .credential(new MetricsAdvisorKeyCredential("subscription_key", "api_key"))
                .buildClient();

        // List the ingestion status of a data feed.
        System.out.printf("Listing ingestion status%n");
        final String dataFeedId = "4957a2f7-a0f4-4fc0-b8d7-d866c1df0f4c";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final ListDataFeedIngestionOptions options = new ListDataFeedIngestionOptions(startTime, endTime);
        PagedIterable<DataFeedIngestionStatus> ingestionStatusIterable
            = advisorAdministrationClient.listDataFeedIngestionStatus(dataFeedId, options);

        for (DataFeedIngestionStatus ingestionStatus : ingestionStatusIterable) {
            System.out.printf("Timestamp: %s%n", ingestionStatus.getTimestamp());
            System.out.printf("Status: %s%n", ingestionStatus.getStatus());
            System.out.printf("Message: %s%n", ingestionStatus.getMessage());
        }

        // Get the ingestion progress.
        System.out.printf("Retrieving ingestion progress%n");
        DataFeedIngestionProgress ingestionProgress = advisorAdministrationClient.getDataFeedIngestionProgress(dataFeedId);
        System.out.printf("Latest active timestamp: %s%n",
            ingestionProgress.getLatestActiveTimestamp());
        System.out.printf("Latest successful timestamp: %s%n",
            ingestionProgress.getLatestSuccessTimestamp());

        // Reingest the data in the data source for a given period and overwrite ingested data
        // for the same period.
        System.out.printf("Refreshing ingestion for a period%n");
        final OffsetDateTime dataPointStartTimeSamp = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime dataPointEndTimeSamp = OffsetDateTime.parse("2020-03-03T00:00:00Z");
        advisorAdministrationClient.refreshDataFeedIngestion(dataFeedId,
            dataPointStartTimeSamp,
            dataPointEndTimeSamp);
        System.out.printf("Refreshing of ingestion requested%n");
    }
}
