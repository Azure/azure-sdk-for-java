// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration;

import com.azure.ai.metricsadvisor.administration.models.ListDataFeedIngestionOptions;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;

import java.time.OffsetDateTime;

/**
 * Sample demonstrates how to list, get progress, and refresh data feed ingestion.
 */
public class DataFeedIngestionAsyncSample {
    public static void main(String[] args) {
        final MetricsAdvisorAdministrationAsyncClient advisorAdministrationAsyncClient =
            new MetricsAdvisorAdministrationClientBuilder()
                .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
                .credential(new MetricsAdvisorKeyCredential("subscription_key", "api_key"))
                .buildAsyncClient();

        // List the ingestion status of a data feed.
        final String dataFeedId = "4957a2f7-a0f4-4fc0-b8d7-d866c1df0f4c";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final ListDataFeedIngestionOptions options = new ListDataFeedIngestionOptions(startTime, endTime);
        advisorAdministrationAsyncClient.listDataFeedIngestionStatus(dataFeedId, options)
            .doOnSubscribe(__ -> System.out.printf("Listing ingestion status%n"))
            .doOnNext(ingestionStatus -> {
                System.out.printf("Timestamp: %s%n", ingestionStatus.getTimestamp());
                System.out.printf("Status: %s%n", ingestionStatus.getStatus());
                System.out.printf("Message: %s%n", ingestionStatus.getMessage());
            }).blockLast();


        // Get the ingestion progress.
        advisorAdministrationAsyncClient.getDataFeedIngestionProgress(dataFeedId)
            .doOnSubscribe(__ -> System.out.printf("Retrieving ingestion progress%n"))
            .doOnNext(ingestionProgress -> {
                System.out.printf("Latest active timestamp: %s%n",
                    ingestionProgress.getLatestActiveTimestamp());
                System.out.printf("Latest successful timestamp: %s%n",
                    ingestionProgress.getLatestSuccessTimestamp());
            }).block();

        // Reingest the data in the data source for a given period and overwrite ingested data
        // for the same period.
        final OffsetDateTime dataPointStartTimeSamp = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime dataPointEndTimeSamp = OffsetDateTime.parse("2020-03-03T00:00:00Z");
        advisorAdministrationAsyncClient.refreshDataFeedIngestionWithResponse(dataFeedId,
            dataPointStartTimeSamp,
            dataPointEndTimeSamp)
            .doOnSubscribe(__ -> System.out.printf("Refreshing ingestion for a period%n"))
            .doOnNext(response -> {
                System.out.printf("Response statusCode: %d%n", response.getStatusCode());
            })
            .block();

        /*
          Each of the above sample a varient of block() operator which will block
          until the operation is completed.
          This is strongly discouraged for use in production as it eliminates the benefits
          of asynchronous IO. It is used here to ensure the sample runs to completion.
         */
    }
}
