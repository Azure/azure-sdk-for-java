// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration;

import com.azure.ai.metricsadvisor.implementation.models.DataSourceType;
import com.azure.ai.metricsadvisor.models.AzureAppInsightsDataFeedSource;
import com.azure.ai.metricsadvisor.models.DataFeed;
import com.azure.ai.metricsadvisor.models.DataFeedGranularity;
import com.azure.ai.metricsadvisor.models.DataFeedGranularityType;
import com.azure.ai.metricsadvisor.models.DataFeedIngestionSettings;
import com.azure.ai.metricsadvisor.models.DataFeedOptions;
import com.azure.ai.metricsadvisor.models.DataFeedSchema;
import com.azure.ai.metricsadvisor.models.Dimension;
import com.azure.ai.metricsadvisor.models.Metric;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;

import java.time.OffsetDateTime;
import java.util.Arrays;

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
        DataFeed dataFeed = advisorAdministrationClient
            .createDataFeed("sample_db",
                new AzureAppInsightsDataFeedSource("application_id", "api_key", "azure_Cloud", "query"),
                new DataFeedGranularity().setGranularityType(DataFeedGranularityType.DAILY),
                new DataFeedSchema(Arrays.asList(new Metric().setName("cost"), new Metric().setName(
                    "revenue"))).setDimensions(Arrays.asList(new Dimension().setName("city"),
                    new Dimension().setName("category"))),
                new DataFeedIngestionSettings(OffsetDateTime.parse("2020-07-01T00:00:00Z")), null);

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
        dataFeed.getMetricIds().forEach(metricId -> System.out.println(metricId));
        System.out.printf("Data feed source type: %s%n", dataFeed.getSourceType());

        if (DataSourceType.AZURE_APPLICATION_INSIGHTS.equals(dataFeed.getSource())) {
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
            .setOptions(new DataFeedOptions().setAdmins(Arrays.asList("admin1@admin.com"))));
        System.out.printf("Updated data feed admin list: %s%n", dataFeed.getOptions().getAdmins());

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
            dataFeedItem.getMetricIds().forEach(metricId -> System.out.println(metricId));
            System.out.printf("Data feed source type: %s%n", dataFeedItem.getSourceType());
        });
    }
}
