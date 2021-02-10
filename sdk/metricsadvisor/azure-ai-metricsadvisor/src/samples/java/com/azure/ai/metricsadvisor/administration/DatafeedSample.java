// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration;

import com.azure.ai.metricsadvisor.models.AzureAppInsightsDataFeedSource;
import com.azure.ai.metricsadvisor.models.DataFeed;
import com.azure.ai.metricsadvisor.models.DataFeedDimension;
import com.azure.ai.metricsadvisor.models.DataFeedGranularity;
import com.azure.ai.metricsadvisor.models.DataFeedGranularityType;
import com.azure.ai.metricsadvisor.models.DataFeedIngestionSettings;
import com.azure.ai.metricsadvisor.models.DataFeedMetric;
import com.azure.ai.metricsadvisor.models.DataFeedOptions;
import com.azure.ai.metricsadvisor.models.DataFeedSchema;
import com.azure.ai.metricsadvisor.models.DataFeedSourceType;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;

/**
 * Sample demonstrates how to create, get, update, delete and list datafeed.
 */
public class DatafeedSample {
    public static void main(String[] args) {
        final MetricsAdvisorAdministrationClient advisorAdministrationClient =
            new MetricsAdvisorAdministrationClientBuilder()
                .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
                .credential(new MetricsAdvisorKeyCredential("subscription_key", "api_key"))
                .buildClient();

        // Create Data feed
        System.out.printf("Creating Data feed%n");
        DataFeed appInsightsDataFeed = new DataFeed()
            .setName("sample_db")
            .setSource(new AzureAppInsightsDataFeedSource("application_id", "api_key", "azure_Cloud", "query"))
            .setGranularity(new DataFeedGranularity().setGranularityType(DataFeedGranularityType.DAILY))
            .setSchema(
                new DataFeedSchema(
                    Arrays.asList(
                        new DataFeedMetric().setName("cost"),
                        new DataFeedMetric().setName("revenue")
                    )).setDimensions(
                    Arrays.asList(
                        new DataFeedDimension().setName("city"),
                        new DataFeedDimension().setName("category")
                    ))
            ).setIngestionSettings(new DataFeedIngestionSettings(OffsetDateTime.parse("2020-07-01T00:00:00Z")));

        DataFeed dataFeed = advisorAdministrationClient
            .createDataFeed(appInsightsDataFeed);

        System.out.printf("Created data feed: %s%n", dataFeed.getId());

        // Retrieve the data feed that just created.
        System.out.printf("Fetching data feed Id: %s%n", dataFeed.getId());
        dataFeed = advisorAdministrationClient.getDataFeed(dataFeed.getId());
        System.out.printf("Data feed Id : %s%n", dataFeed.getId());
        System.out.printf("Data feed name : %s%n", dataFeed.getName());
        System.out.printf("Is the query user is one of data feed administrator : %s%n", dataFeed.isAdmin());
        System.out.printf("Data feed created time : %s%n", dataFeed.getCreatedTime());
        System.out.printf("Data feed granularity type : %s%n", dataFeed.getGranularity().getGranularityType());
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
            System.out.printf("Application Id : %s%n", createdAppInsightsDatafeedSource.getApplicationId());
            System.out.printf("API key : %s%n", createdAppInsightsDatafeedSource.getApiKey());
            System.out.printf("Query : %s%n", createdAppInsightsDatafeedSource.getQuery());
        }

        // Update the data feed.
        System.out.printf("Updating data feed: %s%n", dataFeed.getId());
        dataFeed = advisorAdministrationClient.updateDataFeed(dataFeed
            .setOptions(new DataFeedOptions().setAdminEmails(Collections.singletonList("admin1@admin.com"))));
        System.out.printf("Updated data feed admin list: %s%n", dataFeed.getOptions().getAdminEmails());

        // Delete the data feed.
        System.out.printf("Deleting data feed: %s%n", dataFeed.getId());
        advisorAdministrationClient.deleteDataFeed(dataFeed.getId());
        System.out.printf("Deleted data feed%n");

        // List data feeds.
        System.out.printf("Listing data feeds%n");
        advisorAdministrationClient.listDataFeeds().forEach(dataFeedItem -> {
            System.out.printf("Data feed Id : %s%n", dataFeedItem.getId());
            System.out.printf("Data feed name : %s%n", dataFeedItem.getName());
            System.out.printf("Is the query user is one of data feed administrator : %s%n", dataFeedItem.isAdmin());
            System.out.printf("Data feed created time : %s%n", dataFeedItem.getCreatedTime());
            System.out.printf("Data feed granularity type : %s%n", dataFeedItem.getGranularity().getGranularityType());
            System.out.printf("Data feed granularity value : %d%n",
                dataFeedItem.getGranularity().getCustomGranularityValue());
            System.out.println("Data feed related metric Id's:");
            dataFeedItem.getMetricIds().forEach((metricId, metricName)
                -> System.out.printf("Metric Id : %s, Metric Name: %s%n", metricId, metricName));
            System.out.printf("Data feed source type: %s%n", dataFeedItem.getSourceType());
        });
    }
}
