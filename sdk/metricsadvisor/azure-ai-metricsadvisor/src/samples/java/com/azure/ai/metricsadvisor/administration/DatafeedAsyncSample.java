// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration;

import com.azure.ai.metricsadvisor.administration.models.AzureAppInsightsDataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.DataFeed;
import com.azure.ai.metricsadvisor.administration.models.DataFeedDimension;
import com.azure.ai.metricsadvisor.administration.models.DataFeedGranularity;
import com.azure.ai.metricsadvisor.administration.models.DataFeedGranularityType;
import com.azure.ai.metricsadvisor.administration.models.DataFeedIngestionSettings;
import com.azure.ai.metricsadvisor.administration.models.DataFeedMetric;
import com.azure.ai.metricsadvisor.administration.models.DataFeedOptions;
import com.azure.ai.metricsadvisor.administration.models.DataFeedSchema;
import com.azure.ai.metricsadvisor.administration.models.DataFeedSourceType;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;

/**
 * Async sample demonstrates how to create, get, update, delete and list datafeed.
 */
public class DatafeedAsyncSample {
    public static void main(String[] args) {
        final MetricsAdvisorAdministrationAsyncClient advisorAdministrationAsyncClient =
            new MetricsAdvisorAdministrationClientBuilder()
                .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
                .credential(new MetricsAdvisorKeyCredential("subscription_key", "api_key"))
                .buildAsyncClient();

        // Create Data feed
        System.out.printf("Creating Data feed%n");
        DataFeed appInsightsDataFeed = new DataFeed()
            .setName("sample_db")
            .setSource(new AzureAppInsightsDataFeedSource("application_id", "api_key", "azure_Cloud", "query"))
            .setGranularity(new DataFeedGranularity().setGranularityType(DataFeedGranularityType.DAILY))
            .setSchema(
                new DataFeedSchema(
                    Arrays.asList(
                        new DataFeedMetric("cost"),
                        new DataFeedMetric("revenue")
                )).setDimensions(
                    Arrays.asList(
                        new DataFeedDimension("city"),
                        new DataFeedDimension("category")
                    ))
            ).setIngestionSettings(new DataFeedIngestionSettings(OffsetDateTime.parse("2020-07-01T00:00:00Z")));

        final Mono<DataFeed> createdDataFeedMono = advisorAdministrationAsyncClient
            .createDataFeed(appInsightsDataFeed);

        createdDataFeedMono
            .doOnSubscribe(__ ->
                System.out.printf("Creating Data feed%n"))
            .doOnSuccess(dataFeed ->
                System.out.printf("Created Data feed: %s%n", dataFeed.getId()));

        // Retrieve the data feed that just created.
        Mono<DataFeed> fetchDataFeedMono =
            createdDataFeedMono.flatMap(createdDataFeed -> {
                return advisorAdministrationAsyncClient.getDataFeed(createdDataFeed.getId())
                    .doOnSubscribe(__ ->
                        System.out.printf("Fetching Data feed: %s%n", createdDataFeed.getId()))
                    .doOnSuccess(config ->
                        System.out.printf("Fetched Data feed%n"))
                    .doOnNext(dataFeed -> {
                        System.out.printf("Data feed Id : %s%n", dataFeed.getId());
                        System.out.printf("Data feed name : %s%n", dataFeed.getName());
                        System.out.printf("Is the query user is one of data feed administrator : %s%n",
                            dataFeed.isAdmin());
                        System.out.printf("Data feed created time : %s%n", dataFeed.getCreatedTime());
                        System.out.printf("Data feed granularity type : %s%n",
                            dataFeed.getGranularity().getGranularityType());
                        System.out.printf("Data feed granularity value : %d%n",
                            dataFeed.getGranularity().getCustomGranularityValue());
                        System.out.println("Data feed related metric Id's:");
                        dataFeed.getMetricIds().forEach((metricId, metricName)
                            -> System.out.printf("Metric Id : %s, Metric Name: %s%n", metricId, metricName));
                        System.out.printf("Data feed source type: %s%n", dataFeed.getSourceType());

                        if (DataFeedSourceType.AZURE_APP_INSIGHTS.equals(dataFeed.getSourceType())) {
                            AzureAppInsightsDataFeedSource createdAppInsightsDatafeedSource
                                = (AzureAppInsightsDataFeedSource) dataFeed.getSource();
                            System.out.println("Data feed source details");
                            System.out.printf("Application Id : %s%n",
                                createdAppInsightsDatafeedSource.getApplicationId());
                            System.out.printf("API key : %s%n", createdAppInsightsDatafeedSource.getApiKey());
                            System.out.printf("Query : %s%n", createdAppInsightsDatafeedSource.getQuery());
                        }
                    });
            });

        // Update the data feed.
        Mono<DataFeed> updateDataFeedMono = fetchDataFeedMono
            .flatMap(dataFeed -> {
                return advisorAdministrationAsyncClient.updateDataFeed(dataFeed
                    .setOptions(new DataFeedOptions().setAdmins(Collections.singletonList("admin1@admin.com"))))
                    .doOnSubscribe(__ ->
                        System.out.printf("Updating data feed: %s%n", dataFeed.getId()))
                    .doOnSuccess(updatedDataFeed -> {

                        System.out.printf("Updated data feed%n");
                        System.out.printf("Updated data feed admin list: %s%n",
                            String.join(",", updatedDataFeed.getOptions().getAdmins()));
                    });
            });

        // Delete the data feed.
        Mono<Void> deleteDataFeedMono = updateDataFeedMono.flatMap(dataFeed -> {
            return advisorAdministrationAsyncClient.deleteDataFeed(dataFeed.getId())
                .doOnSubscribe(__ ->
                    System.out.printf("Deleting data feed: %s%n", dataFeed.getId()))
                .doOnSuccess(config ->
                    System.out.printf("Deleted data feed%n"));
        });

        /*
          This will block until all the above CRUD on operation on email hook is completed.
          This is strongly discouraged for use in production as it eliminates the benefits
          of asynchronous IO. It is used here to ensure the sample runs to completion.
         */
        deleteDataFeedMono.block();

        // List data feeds.
        System.out.printf("Listing data feeds%n");
        advisorAdministrationAsyncClient.listDataFeeds()
            .doOnNext(dataFeedItem -> {
                System.out.printf("Data feed Id : %s%n", dataFeedItem.getId());
                System.out.printf("Data feed name : %s%n", dataFeedItem.getName());
                System.out.printf("Is the query user is one of data feed administrator : %s%n", dataFeedItem.isAdmin());
                System.out.printf("Data feed created time : %s%n", dataFeedItem.getCreatedTime());
                System.out.printf("Data feed granularity type : %s%n",
                    dataFeedItem.getGranularity().getGranularityType());
                System.out.printf("Data feed granularity value : %d%n",
                    dataFeedItem.getGranularity().getCustomGranularityValue());
                System.out.println("Data feed related metric Id's:");
                dataFeedItem.getMetricIds().forEach((metricId, metricName)
                    -> System.out.printf("Metric Id : %s, Metric Name: %s%n", metricId, metricName));
                System.out.printf("Data feed source type: %s%n", dataFeedItem.getSourceType());
            });
    }
}
