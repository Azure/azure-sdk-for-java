// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration;

import com.azure.ai.metricsadvisor.administration.models.AnomalyAlertConfiguration;
import com.azure.ai.metricsadvisor.administration.models.AnomalyDetectionConfiguration;
import com.azure.ai.metricsadvisor.administration.models.AnomalyDetectorDirection;
import com.azure.ai.metricsadvisor.administration.models.AnomalySeverity;
import com.azure.ai.metricsadvisor.administration.models.ChangeThresholdCondition;
import com.azure.ai.metricsadvisor.administration.models.DataFeed;
import com.azure.ai.metricsadvisor.administration.models.DataFeedDimension;
import com.azure.ai.metricsadvisor.administration.models.DataFeedGranularity;
import com.azure.ai.metricsadvisor.administration.models.DataFeedGranularityType;
import com.azure.ai.metricsadvisor.administration.models.DataFeedIngestionProgress;
import com.azure.ai.metricsadvisor.administration.models.DataFeedIngestionSettings;
import com.azure.ai.metricsadvisor.administration.models.DataFeedIngestionStatus;
import com.azure.ai.metricsadvisor.administration.models.DataFeedMetric;
import com.azure.ai.metricsadvisor.administration.models.DataFeedOptions;
import com.azure.ai.metricsadvisor.administration.models.DataFeedRollupSettings;
import com.azure.ai.metricsadvisor.administration.models.DataFeedRollupType;
import com.azure.ai.metricsadvisor.administration.models.DataFeedSchema;
import com.azure.ai.metricsadvisor.administration.models.DataFeedStatus;
import com.azure.ai.metricsadvisor.administration.models.DataSourceCredentialEntity;
import com.azure.ai.metricsadvisor.administration.models.DataSourceServicePrincipalInKeyVault;
import com.azure.ai.metricsadvisor.administration.models.DetectionConditionOperator;
import com.azure.ai.metricsadvisor.administration.models.SeverityCondition;
import com.azure.ai.metricsadvisor.models.DimensionKey;
import com.azure.ai.metricsadvisor.administration.models.EmailNotificationHook;
import com.azure.ai.metricsadvisor.administration.models.HardThresholdCondition;
import com.azure.ai.metricsadvisor.administration.models.ListAnomalyAlertConfigsOptions;
import com.azure.ai.metricsadvisor.administration.models.ListCredentialEntityOptions;
import com.azure.ai.metricsadvisor.administration.models.ListDataFeedFilter;
import com.azure.ai.metricsadvisor.administration.models.ListDataFeedIngestionOptions;
import com.azure.ai.metricsadvisor.administration.models.ListDataFeedOptions;
import com.azure.ai.metricsadvisor.administration.models.ListHookOptions;
import com.azure.ai.metricsadvisor.administration.models.ListDetectionConfigsOptions;
import com.azure.ai.metricsadvisor.administration.models.MetricAnomalyAlertConditions;
import com.azure.ai.metricsadvisor.administration.models.MetricAlertConfiguration;
import com.azure.ai.metricsadvisor.administration.models.MetricAlertConfigurationsOperator;
import com.azure.ai.metricsadvisor.administration.models.MetricAnomalyAlertScope;
import com.azure.ai.metricsadvisor.administration.models.MetricSeriesGroupDetectionCondition;
import com.azure.ai.metricsadvisor.administration.models.MetricSingleSeriesDetectionCondition;
import com.azure.ai.metricsadvisor.administration.models.MetricWholeSeriesDetectionCondition;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import com.azure.ai.metricsadvisor.administration.models.MySqlDataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.NotificationHook;
import com.azure.ai.metricsadvisor.administration.models.SmartDetectionCondition;
import com.azure.ai.metricsadvisor.administration.models.SuppressCondition;
import com.azure.ai.metricsadvisor.administration.models.WebNotificationHook;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.IterableStream;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Code snippet for {@link MetricsAdvisorAdministrationClient}
 */
public class MetricsAdvisorAdministrationClientJavaDocCodeSnippets {
    private MetricsAdvisorAdministrationClient metricsAdvisorAdminClient =
        new MetricsAdvisorAdministrationClientBuilder().buildClient();

    /**
     * Code snippet for creating a {@link MetricsAdvisorAdministrationClient}
     */
    public void createMetricsAdvisorAdministrationClient() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.instantiation
        MetricsAdvisorAdministrationClient metricsAdvisorAdminClient =
            new MetricsAdvisorAdministrationClientBuilder()
                .credential(new MetricsAdvisorKeyCredential("{subscription_key}", "{api_key}"))
                .endpoint("{endpoint}")
                .buildClient();
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.instantiation
    }

    /**
     * Code snippet for creating a {@link MetricsAdvisorAdministrationClient} with pipeline
     */
    public void createMetricsAdvisorAdministrationClientWithPipeline() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.pipeline.instantiation
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(/* add policies */)
            .build();

        MetricsAdvisorAdministrationClient metricsAdvisorAdministrationClient =
            new MetricsAdvisorAdministrationClientBuilder()
                .credential(new MetricsAdvisorKeyCredential("{subscription_key}", "{api_key}"))
                .endpoint("{endpoint}")
                .pipeline(pipeline)
                .buildClient();
        // END:  com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.pipeline.instantiation
    }

    // Create Data Feed

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#createDataFeed(DataFeed)}
     */
    public void createDataFeed() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createDataFeed#DataFeed
        DataFeed dataFeed = new DataFeed()
            .setName("dataFeedName")
            .setSource(new MySqlDataFeedSource("conn-string", "query"))
            .setGranularity(new DataFeedGranularity().setGranularityType(DataFeedGranularityType.DAILY))
            .setSchema(new DataFeedSchema(
                Arrays.asList(
                    new DataFeedMetric("cost"),
                    new DataFeedMetric("revenue")
                )).setDimensions(
                Arrays.asList(
                    new DataFeedDimension("city"),
                    new DataFeedDimension("category")
                ))
            )
            .setIngestionSettings(new DataFeedIngestionSettings(OffsetDateTime.parse("2020-01-01T00:00:00Z")))
            .setOptions(new DataFeedOptions()
                .setDescription("data feed description")
                .setRollupSettings(new DataFeedRollupSettings()
                    .setRollupType(DataFeedRollupType.AUTO_ROLLUP)));

        DataFeed createdDataFeed = metricsAdvisorAdminClient.createDataFeed(dataFeed);

        System.out.printf("Data feed Id: %s%n", createdDataFeed.getId());
        System.out.printf("Data feed description: %s%n", createdDataFeed.getOptions().getDescription());
        System.out.printf("Data feed source type: %s%n", createdDataFeed.getSourceType());
        System.out.printf("Data feed creator: %s%n", createdDataFeed.getCreator());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createDataFeed#DataFeed
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#createDataFeedWithResponse(DataFeed, Context)}
     */
    public void createDataFeedWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createDataFeedWithResponse#DataFeed-Context
        DataFeed dataFeed = new DataFeed()
            .setName("dataFeedName")
            .setSource(new MySqlDataFeedSource("conn-string", "query"))
            .setGranularity(new DataFeedGranularity().setGranularityType(DataFeedGranularityType.DAILY))
            .setSchema(new DataFeedSchema(
                Arrays.asList(
                    new DataFeedMetric("cost"),
                    new DataFeedMetric("revenue")
                )).setDimensions(
                Arrays.asList(
                    new DataFeedDimension("city"),
                    new DataFeedDimension("category")
                ))
            )
            .setIngestionSettings(new DataFeedIngestionSettings(OffsetDateTime.parse("2020-01-01T00:00:00Z")))
            .setOptions(new DataFeedOptions()
                .setDescription("data feed description")
                .setRollupSettings(new DataFeedRollupSettings()
                    .setRollupType(DataFeedRollupType.AUTO_ROLLUP)));

        final Response<DataFeed> createdDataFeedResponse =
            metricsAdvisorAdminClient.createDataFeedWithResponse(dataFeed, Context.NONE);

        System.out.printf("Data feed create operation status: %s%n", createdDataFeedResponse.getStatusCode());
        DataFeed createdDataFeed = createdDataFeedResponse.getValue();
        System.out.printf("Data feed Id: %s%n", createdDataFeed.getId());
        System.out.printf("Data feed description: %s%n", createdDataFeed.getOptions().getDescription());
        System.out.printf("Data feed source type: %s%n", createdDataFeed.getSourceType());
        System.out.printf("Data feed creator: %s%n", createdDataFeed.getCreator());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createDataFeedWithResponse#DataFeed-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#getDataFeed(String)}
     */
    public void getDataFeed() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDataFeed#String
        final String dataFeedId = "r47053f1-9080-09lo-bacf-8dccf2e86f";
        DataFeed dataFeed = metricsAdvisorAdminClient.getDataFeed(dataFeedId);
        System.out.printf("Data feed Id: %s%n", dataFeed.getId());
        System.out.printf("Data feed description: %s%n", dataFeed.getOptions().getDescription());
        System.out.printf("Data feed source type: %s%n", dataFeed.getSourceType());
        System.out.printf("Data feed creator: %s%n", dataFeed.getCreator());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDataFeed#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#getDataFeedWithResponse(String, Context)}
     */
    public void getDataFeedWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDataFeedWithResponse#String-Context
        final String dataFeedId = "r47053f1-9080-09lo-bacf-8dccf2e86f";
        final Response<DataFeed> dataFeedResponse =
            metricsAdvisorAdminClient.getDataFeedWithResponse(dataFeedId, Context.NONE);

        System.out.printf("Data feed get operation status: %s%n", dataFeedResponse.getStatusCode());
        DataFeed dataFeed = dataFeedResponse.getValue();
        System.out.printf("Data feed Id: %s%n", dataFeed.getId());
        System.out.printf("Data feed description: %s%n", dataFeed.getOptions().getDescription());
        System.out.printf("Data feed source type: %s%n", dataFeed.getSourceType());
        System.out.printf("Data feed creator: %s%n", dataFeed.getCreator());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDataFeedWithResponse#String-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#updateDataFeed(DataFeed)}
     */
    public void updateDataFeed() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateDataFeed#DataFeed

        DataFeed existingDataFeed = new DataFeed();
        final DataFeed updatedDataFeed = metricsAdvisorAdminClient.updateDataFeed(
            existingDataFeed.setOptions(new DataFeedOptions().setDescription("set updated description")));

        System.out.printf("Data feed Id: %s%n", updatedDataFeed.getId());
        System.out.printf("Data feed updated description: %s%n", updatedDataFeed.getOptions().getDescription());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateDataFeed#DataFeed
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#updateDataFeedWithResponse(DataFeed, Context)}
     */
    public void updateDataFeedWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateDataFeedWithResponse#DataFeed-Context
        DataFeed existingDataFeed = new DataFeed();
        final Response<DataFeed> updateDataFeedWithResponse =
            metricsAdvisorAdminClient.updateDataFeedWithResponse(
                existingDataFeed.setOptions(new DataFeedOptions().setDescription("set updated description")),
                Context.NONE);

        System.out.printf("Data feed update operation status: %s%n", updateDataFeedWithResponse.getStatusCode());
        DataFeed dataFeed = updateDataFeedWithResponse.getValue();
        System.out.printf("Data feed Id: %s%n", dataFeed.getId());
        System.out.printf("Data feed updated description: %s%n", dataFeed.getOptions().getDescription());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateDataFeedWithResponse#DataFeed-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#deleteDataFeed(String)}.
     */
    public void deleteDataFeed() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteDataFeed#String
        final String dataFeedId = "r47053f1-9080-09lo-bacf-8dccf2e86f";
        metricsAdvisorAdminClient.deleteDataFeed(dataFeedId);
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteDataFeed#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#deleteDataFeedWithResponse(String, Context)}
     */
    public void deleteDataFeedWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteDataFeedWithResponse#String-Context
        final String dataFeedId = "r47053f1-9080-09lo-bacf-8dccf2e86f";
        final Response<Void> response = metricsAdvisorAdminClient
            .deleteDataFeedWithResponse(dataFeedId, Context.NONE);
        System.out.printf("Data feed delete operation status : %s%n", response.getStatusCode());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteDataFeedWithResponse#String-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#listDataFeeds()}
     */
    public void listDataFeeds() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDataFeeds
        metricsAdvisorAdminClient.listDataFeeds()
            .forEach(dataFeed -> {
                System.out.printf("Data feed Id: %s%n", dataFeed.getId());
                System.out.printf("Data feed description: %s%n", dataFeed.getOptions().getDescription());
                System.out.printf("Data feed source type: %s%n", dataFeed.getSourceType());
                System.out.printf("Data feed creator: %s%n", dataFeed.getCreator());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDataFeeds
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#listDataFeeds(ListDataFeedOptions, Context)}
     * with options.
     */
    public void listDataFeedWithOptions() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDataFeeds#ListDataFeedOptions-Context
        metricsAdvisorAdminClient.listDataFeeds(
            new ListDataFeedOptions()
                .setListDataFeedFilter(
                    new ListDataFeedFilter()
                        .setDataFeedStatus(DataFeedStatus.ACTIVE)
                        .setDataFeedGranularityType(DataFeedGranularityType.DAILY))
                .setMaxPageSize(3), Context.NONE)
            .forEach(dataFeed -> {
                System.out.printf("Data feed Id: %s%n", dataFeed.getId());
                System.out.printf("Data feed description: %s%n", dataFeed.getOptions().getDescription());
                System.out.printf("Data feed source type: %s%n", dataFeed.getSourceType());
                System.out.printf("Data feed creator: %s%n", dataFeed.getCreator());
                System.out.printf("Data feed status: %s%n", dataFeed.getStatus());
                System.out.printf("Data feed granularity type: %s%n", dataFeed.getGranularity().getGranularityType());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDataFeeds#ListDataFeedOptions-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#createHook(NotificationHook)}.
     */
    public void createHook() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createHook#NotificationHook
        NotificationHook emailNotificationHook = new EmailNotificationHook("email notificationHook")
            .setDescription("my email notificationHook")
            .setEmailsToAlert(new ArrayList<String>() {{
                    add("alertme@alertme.com");
                }})
            .setExternalLink("https://adwiki.azurewebsites.net/articles/howto/alerts/create-hooks.html");

        NotificationHook notificationHook = metricsAdvisorAdminClient.createHook(emailNotificationHook);
        EmailNotificationHook createdEmailHook = (EmailNotificationHook) notificationHook;
        System.out.printf("NotificationHook Id: %s%n", createdEmailHook.getId());
        System.out.printf("NotificationHook Name: %s%n", createdEmailHook.getName());
        System.out.printf("NotificationHook Description: %s%n", createdEmailHook.getDescription());
        System.out.printf("NotificationHook External Link: %s%n", createdEmailHook.getExternalLink());
        System.out.printf("NotificationHook Emails: %s%n", String.join(",",
            createdEmailHook.getEmailsToAlert()));
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createHook#NotificationHook
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#createHookWithResponse(NotificationHook, Context)}.
     */
    public void createHookWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createHookWithResponse#NotificationHook-Context
        NotificationHook emailNotificationHook = new EmailNotificationHook("email hook")
            .setDescription("my email hook")
            .setEmailsToAlert(new ArrayList<String>() {{
                    add("alertme@alertme.com");
                }})
            .setExternalLink("https://adwiki.azurewebsites.net/articles/howto/alerts/create-hooks.html");

        Response<NotificationHook> response
            = metricsAdvisorAdminClient.createHookWithResponse(emailNotificationHook, Context.NONE);
        System.out.printf("Response statusCode: %d%n", response.getStatusCode());
        EmailNotificationHook createdEmailHook = (EmailNotificationHook) response.getValue();
        System.out.printf("NotificationHook Id: %s%n", createdEmailHook.getId());
        System.out.printf("NotificationHook Name: %s%n", createdEmailHook.getName());
        System.out.printf("NotificationHook Description: %s%n", createdEmailHook.getDescription());
        System.out.printf("NotificationHook External Link: %s%n", createdEmailHook.getExternalLink());
        System.out.printf("NotificationHook Emails: %s%n", String.join(",",
            createdEmailHook.getEmailsToAlert()));
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createHookWithResponse#NotificationHook-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#getHook(String)}.
     */
    public void getHook() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getHook#String
        final String hookId = "f00853f1-6627-447f-bacf-8dccf2e86fed";
        NotificationHook notificationHook = metricsAdvisorAdminClient.getHook(hookId);
        if (notificationHook instanceof EmailNotificationHook) {
            EmailNotificationHook emailHook = (EmailNotificationHook) notificationHook;
            System.out.printf("NotificationHook Id: %s%n", emailHook.getId());
            System.out.printf("NotificationHook Name: %s%n", emailHook.getName());
            System.out.printf("NotificationHook Description: %s%n", emailHook.getDescription());
            System.out.printf("NotificationHook External Link: %s%n", emailHook.getExternalLink());
            System.out.printf("NotificationHook Emails: %s%n", String.join(",", emailHook.getEmailsToAlert()));
        } else if (notificationHook instanceof WebNotificationHook) {
            WebNotificationHook webHook = (WebNotificationHook) notificationHook;
            System.out.printf("NotificationHook Id: %s%n", webHook.getId());
            System.out.printf("NotificationHook Name: %s%n", webHook.getName());
            System.out.printf("NotificationHook Description: %s%n", webHook.getDescription());
            System.out.printf("NotificationHook External Link: %s%n", webHook.getExternalLink());
            System.out.printf("NotificationHook Endpoint: %s%n", webHook.getEndpoint());
            System.out.printf("NotificationHook Headers: %s%n", webHook.getHttpHeaders());
        }
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getHook#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#getHookWithResponse(String, Context)}.
     */
    public void getHookWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getHookWithResponse#String-Context
        final String hookId = "f00853f1-6627-447f-bacf-8dccf2e86fed";
        Response<NotificationHook> response = metricsAdvisorAdminClient.getHookWithResponse(hookId, Context.NONE);
        System.out.printf("Response statusCode: %d%n", response.getStatusCode());
        NotificationHook notificationHook = response.getValue();
        if (notificationHook instanceof EmailNotificationHook) {
            EmailNotificationHook emailHook = (EmailNotificationHook) notificationHook;
            System.out.printf("NotificationHook Id: %s%n", emailHook.getId());
            System.out.printf("NotificationHook Name: %s%n", emailHook.getName());
            System.out.printf("NotificationHook Description: %s%n", emailHook.getDescription());
            System.out.printf("NotificationHook External Link: %s%n", emailHook.getExternalLink());
            System.out.printf("NotificationHook Emails: %s%n", String.join(",", emailHook.getEmailsToAlert()));
        } else if (notificationHook instanceof WebNotificationHook) {
            WebNotificationHook webHook = (WebNotificationHook) notificationHook;
            System.out.printf("NotificationHook Id: %s%n", webHook.getId());
            System.out.printf("NotificationHook Name: %s%n", webHook.getName());
            System.out.printf("NotificationHook Description: %s%n", webHook.getDescription());
            System.out.printf("NotificationHook External Link: %s%n", webHook.getExternalLink());
            System.out.printf("NotificationHook Endpoint: %s%n", webHook.getEndpoint());
            System.out.printf("NotificationHook Headers: %s%n", webHook.getHttpHeaders());
        }
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getHookWithResponse#String-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#updateHook(NotificationHook)}.
     */
    public void updateHook() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateHook#NotificationHook
        final String emailHookId = "f00853f1-6627-447f-bacf-8dccf2e86fed";
        NotificationHook notificationHook = metricsAdvisorAdminClient.getHook(emailHookId);
        EmailNotificationHook emailHook = (EmailNotificationHook) notificationHook;
        List<String> emailsToUpdate = new ArrayList<>(emailHook.getEmailsToAlert());
        emailsToUpdate.remove("alertme@alertme.com");
        emailsToUpdate.add("alertme2@alertme.com");
        emailsToUpdate.add("alertme3@alertme.com");
        emailHook.setEmailsToAlert(emailsToUpdate);
        NotificationHook updatedNotificationHook = metricsAdvisorAdminClient.updateHook(emailHook);
        EmailNotificationHook updatedEmailHook = (EmailNotificationHook) updatedNotificationHook;
        System.out.printf("NotificationHook Id: %s%n", updatedEmailHook.getId());
        System.out.printf("NotificationHook Name: %s%n", updatedEmailHook.getName());
        System.out.printf("NotificationHook Description: %s%n", updatedEmailHook.getDescription());
        System.out.printf("NotificationHook External Link: %s%n", updatedEmailHook.getExternalLink());
        System.out.printf("NotificationHook Emails: %s%n", String.join(",",
            updatedEmailHook.getEmailsToAlert()));
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateHook#NotificationHook
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#updateHookWithResponse(NotificationHook, Context)}.
     */
    public void updateHookWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateHookWithResponse#NotificationHook-Context
        final String emailHookId = "f00853f1-6627-447f-bacf-8dccf2e86fed";
        Response<NotificationHook> response
            = metricsAdvisorAdminClient.getHookWithResponse(emailHookId, Context.NONE);
        EmailNotificationHook emailHook = (EmailNotificationHook) response.getValue();
        List<String> emailsToUpdate = new ArrayList<>(emailHook.getEmailsToAlert());
        emailsToUpdate.remove("alertme@alertme.com");
        emailsToUpdate.add("alertme2@alertme.com");
        emailsToUpdate.add("alertme3@alertme.com");
        emailHook.setEmailsToAlert(emailsToUpdate);
        Response<NotificationHook> updateResponse
            = metricsAdvisorAdminClient.updateHookWithResponse(emailHook, Context.NONE);
        EmailNotificationHook updatedEmailHook = (EmailNotificationHook) updateResponse.getValue();
        System.out.printf("Email Hook Id: %s%n", updatedEmailHook.getId());
        System.out.printf("Email Hook Name: %s%n", updatedEmailHook.getName());
        System.out.printf("Email Hook Description: %s%n", updatedEmailHook.getDescription());
        System.out.printf("Email Hook External Link: %s%n", updatedEmailHook.getExternalLink());
        System.out.printf("Email Hook Emails: %s%n", String.join(",",
            updatedEmailHook.getEmailsToAlert()));
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateHookWithResponse#NotificationHook-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#deleteHook(String)}.
     */
    public void deleteHook() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteHook#String
        final String emailHookId = "f00853f1-6627-447f-bacf-8dccf2e86fed";
        metricsAdvisorAdminClient.deleteHook(emailHookId);
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteHook#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#deleteHookWithResponse(String, Context)}.
     */
    public void deleteHookWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteHookWithResponse#String-Context
        final String emailHookId = "f00853f1-6627-447f-bacf-8dccf2e86fed";
        Response<Void> response
            = metricsAdvisorAdminClient.deleteHookWithResponse(emailHookId, Context.NONE);
        System.out.printf("Response status code: %d%n", response.getStatusCode());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteHookWithResponse#String-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#listHooks()}.
     */
    public void listHooks() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listHooks
        PagedIterable<NotificationHook> hooks = metricsAdvisorAdminClient.listHooks();
        for (NotificationHook notificationHook : hooks) {
            if (notificationHook instanceof EmailNotificationHook) {
                EmailNotificationHook emailHook = (EmailNotificationHook) notificationHook;
                System.out.printf("Email Hook Id: %s%n", emailHook.getId());
                System.out.printf("Email Hook Name: %s%n", emailHook.getName());
                System.out.printf("Email Hook Description: %s%n", emailHook.getDescription());
                System.out.printf("Email Hook External Link: %s%n", emailHook.getExternalLink());
                System.out.printf("Email Hook Emails: %s%n", String.join(",", emailHook.getEmailsToAlert()));
            } else if (notificationHook instanceof WebNotificationHook) {
                WebNotificationHook webHook = (WebNotificationHook) notificationHook;
                System.out.printf("Web Hook Id: %s%n", webHook.getId());
                System.out.printf("Web Hook Name: %s%n", webHook.getName());
                System.out.printf("Web Hook Description: %s%n", webHook.getDescription());
                System.out.printf("Web Hook External Link: %s%n", webHook.getExternalLink());
                System.out.printf("Web Hook Endpoint: %s%n", webHook.getEndpoint());
                System.out.printf("Web Hook Headers: %s%n", webHook.getHttpHeaders());
            }
        }
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listHooks
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#listHooks(ListHookOptions, Context)}.
     */
    public void listHooksWithOptions() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listHooks#ListHookOptions-Context
        ListHookOptions options = new ListHookOptions()
            .setSkip(100)
            .setMaxPageSize(20);
        PagedIterable<NotificationHook> hooks = metricsAdvisorAdminClient.listHooks(options, Context.NONE);
        Stream<PagedResponse<NotificationHook>> hooksPageStream = hooks.streamByPage();
        int[] pageCount = new int[1];
        hooksPageStream.forEach(hookPage -> {
            System.out.printf("Page: %d%n", pageCount[0]++);
            for (NotificationHook notificationHook : hookPage.getElements()) {
                if (notificationHook instanceof EmailNotificationHook) {
                    EmailNotificationHook emailHook = (EmailNotificationHook) notificationHook;
                    System.out.printf("Email Hook Id: %s%n", emailHook.getId());
                    System.out.printf("Email Hook Name: %s%n", emailHook.getName());
                    System.out.printf("Email Hook Description: %s%n", emailHook.getDescription());
                    System.out.printf("Email Hook External Link: %s%n", emailHook.getExternalLink());
                    System.out.printf("Email Hook Emails: %s%n", String.join(",",
                        emailHook.getEmailsToAlert()));
                    System.out.printf("Email Hook Admins: %s%n", String.join(",", emailHook.getAdmins()));
                } else if (notificationHook instanceof WebNotificationHook) {
                    WebNotificationHook webHook = (WebNotificationHook) notificationHook;
                    System.out.printf("Web Hook Id: %s%n", webHook.getId());
                    System.out.printf("Web Hook Name: %s%n", webHook.getName());
                    System.out.printf("Web Hook Description: %s%n", webHook.getDescription());
                    System.out.printf("Web Hook External Link: %s%n", webHook.getExternalLink());
                    System.out.printf("Web Hook Endpoint: %s%n", webHook.getEndpoint());
                    System.out.printf("Web Hook Headers: %s%n", webHook.getHttpHeaders());
                    System.out.printf("Web Hook Admins: %s%n", String.join(",", webHook.getAdmins()));
                }
            }
        });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listHooks#ListHookOptions-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#listDataFeedIngestionStatus(String, ListDataFeedIngestionOptions)}.
     */
    public void listDataFeedIngestionStatus() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDataFeedIngestionStatus#String-ListDataFeedIngestionOptions
        final String dataFeedId = "4957a2f7-a0f4-4fc0-b8d7-d866c1df0f4c";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final ListDataFeedIngestionOptions options = new ListDataFeedIngestionOptions(startTime, endTime);
        PagedIterable<DataFeedIngestionStatus> ingestionStatuses
            = metricsAdvisorAdminClient.listDataFeedIngestionStatus(dataFeedId, options);

        for (DataFeedIngestionStatus ingestionStatus : ingestionStatuses) {
            System.out.printf("Timestamp: %s%n", ingestionStatus.getTimestamp());
            System.out.printf("Status: %s%n", ingestionStatus.getStatus());
            System.out.printf("Message: %s%n", ingestionStatus.getMessage());
        }
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDataFeedIngestionStatus#String-ListDataFeedIngestionOptions
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#listDataFeedIngestionStatus(String, ListDataFeedIngestionOptions, Context)}.
     */
    public void listDataFeedIngestionStatusWithContext() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDataFeedIngestionStatus#String-ListDataFeedIngestionOptions-Context
        final String dataFeedId = "4957a2f7-a0f4-4fc0-b8d7-d866c1df0f4c";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final ListDataFeedIngestionOptions options = new ListDataFeedIngestionOptions(startTime, endTime);
        PagedIterable<DataFeedIngestionStatus> ingestionStatuses
            = metricsAdvisorAdminClient.listDataFeedIngestionStatus(dataFeedId, options, Context.NONE);
        Stream<PagedResponse<DataFeedIngestionStatus>> ingestionStatusPageStream = ingestionStatuses.streamByPage();
        int[] pageCount = new int[1];
        ingestionStatusPageStream.forEach(ingestionStatusPage -> {
            System.out.printf("Page: %d%n", pageCount[0]++);
            for (DataFeedIngestionStatus ingestionStatus : ingestionStatusPage.getElements()) {
                System.out.printf("Timestamp: %s%n", ingestionStatus.getTimestamp());
                System.out.printf("Status: %s%n", ingestionStatus.getStatus());
                System.out.printf("Message: %s%n", ingestionStatus.getMessage());
            }
        });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDataFeedIngestionStatus#String-ListDataFeedIngestionOptions-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#refreshDataFeedIngestion(String, OffsetDateTime, OffsetDateTime)}.
     */
    public void refreshDataFeedIngestion() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.refreshDataFeedIngestion#String-OffsetDateTime-OffsetDateTime
        final String dataFeedId = "4957a2f7-a0f4-4fc0-b8d7-d866c1df0f4c";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-03-03T00:00:00Z");
        metricsAdvisorAdminClient.refreshDataFeedIngestion(dataFeedId,
            startTime,
            endTime);
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.refreshDataFeedIngestion#String-OffsetDateTime-OffsetDateTime
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#refreshDataFeedIngestionWithResponse(String, OffsetDateTime, OffsetDateTime, Context)}.
     */
    public void refreshDataFeedIngestionWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.refreshDataFeedIngestionWithResponse#String-OffsetDateTime-OffsetDateTime-Context
        final String dataFeedId = "4957a2f7-a0f4-4fc0-b8d7-d866c1df0f4c";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-03-03T00:00:00Z");
        Response<Void> response = metricsAdvisorAdminClient.refreshDataFeedIngestionWithResponse(dataFeedId,
            startTime,
            endTime,
            Context.NONE);
        System.out.printf("Response statusCode: %d%n", response.getStatusCode());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.refreshDataFeedIngestionWithResponse#String-OffsetDateTime-OffsetDateTime-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#getDataFeedIngestionProgress(String)}.
     */
    public void getDataFeedIngestionProgress() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDataFeedIngestionProgress#String
        final String dataFeedId = "4957a2f7-a0f4-4fc0-b8d7-d866c1df0f4c";
        DataFeedIngestionProgress ingestionProgress
            = metricsAdvisorAdminClient.getDataFeedIngestionProgress(dataFeedId);
        System.out.printf("Latest active timestamp: %s%n", ingestionProgress.getLatestActiveTimestamp());
        System.out.printf("Latest successful timestamp: %s%n", ingestionProgress.getLatestSuccessTimestamp());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDataFeedIngestionProgress#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#getDataFeedIngestionProgressWithResponse(String, Context)}.
     */
    public void getDataFeedIngestionProgressWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDataFeedIngestionProgressWithResponse#String-Context
        final String dataFeedId = "4957a2f7-a0f4-4fc0-b8d7-d866c1df0f4c";
        Response<DataFeedIngestionProgress> response
            = metricsAdvisorAdminClient.getDataFeedIngestionProgressWithResponse(dataFeedId, Context.NONE);
        System.out.printf("Response statusCode: %d%n", response.getStatusCode());
        DataFeedIngestionProgress ingestionProgress = response.getValue();
        System.out.printf("Latest active timestamp: %s%n", ingestionProgress.getLatestActiveTimestamp());
        System.out.printf("Latest successful timestamp: %s%n", ingestionProgress.getLatestSuccessTimestamp());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDataFeedIngestionProgressWithResponse#String-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#createDetectionConfig(String, AnomalyDetectionConfiguration)}.
     */
    public void createDetectionConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createDetectionConfig#String-AnomalyDetectionConfiguration
        final MetricWholeSeriesDetectionCondition wholeSeriesCondition = new MetricWholeSeriesDetectionCondition()
            .setConditionOperator(DetectionConditionOperator.OR)
            .setSmartDetectionCondition(new SmartDetectionCondition(
                50,
                AnomalyDetectorDirection.BOTH,
                new SuppressCondition(50, 50)))
            .setHardThresholdCondition(new HardThresholdCondition(
                AnomalyDetectorDirection.BOTH,
                new SuppressCondition(5, 5))
                .setLowerBound(0.0)
                .setUpperBound(100.0))
            .setChangeThresholdCondition(new ChangeThresholdCondition(
                50,
                30,
                true,
                AnomalyDetectorDirection.BOTH,
                new SuppressCondition(2, 2)));

        final String detectionConfigName = "my_detection_config";
        final String detectionConfigDescription = "anomaly detection config for metric";
        final AnomalyDetectionConfiguration detectionConfig
            = new AnomalyDetectionConfiguration(detectionConfigName)
            .setDescription(detectionConfigDescription)
            .setWholeSeriesDetectionCondition(wholeSeriesCondition);

        final String metricId = "0b836da8-10e6-46cd-8f4f-28262e113a62";
        AnomalyDetectionConfiguration createdDetectionConfig = metricsAdvisorAdminClient
            .createDetectionConfig(metricId, detectionConfig);
        System.out.printf("Detection config Id: %s%n", createdDetectionConfig.getId());
        System.out.printf("Name: %s%n", createdDetectionConfig.getName());
        System.out.printf("Description: %s%n", createdDetectionConfig.getDescription());
        System.out.printf("MetricId: %s%n", createdDetectionConfig.getMetricId());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createDetectionConfig#String-AnomalyDetectionConfiguration
    }


    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#createDetectionConfigWithResponse(String, AnomalyDetectionConfiguration, Context)}.
     */
    public void createDetectionConfigurationWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createDetectionConfigWithResponse#String-AnomalyDetectionConfiguration-Context
        final MetricWholeSeriesDetectionCondition wholeSeriesCondition = new MetricWholeSeriesDetectionCondition()
            .setConditionOperator(DetectionConditionOperator.OR)
            .setSmartDetectionCondition(new SmartDetectionCondition(
                50,
                AnomalyDetectorDirection.BOTH,
                new SuppressCondition(50, 50)))
            .setHardThresholdCondition(new HardThresholdCondition(
                AnomalyDetectorDirection.BOTH,
                new SuppressCondition(5, 5))
                .setLowerBound(0.0)
                .setUpperBound(100.0))
            .setChangeThresholdCondition(new ChangeThresholdCondition(
                50,
                30,
                true,
                AnomalyDetectorDirection.BOTH,
                new SuppressCondition(2, 2)));

        final String detectionConfigName = "my_detection_config";
        final String detectionConfigDescription = "anomaly detection config for metric";
        final AnomalyDetectionConfiguration detectionConfig
            = new AnomalyDetectionConfiguration(detectionConfigName)
            .setDescription(detectionConfigDescription)
            .setWholeSeriesDetectionCondition(wholeSeriesCondition);

        final String metricId = "0b836da8-10e6-46cd-8f4f-28262e113a62";
        Response<AnomalyDetectionConfiguration> response = metricsAdvisorAdminClient
            .createDetectionConfigWithResponse(metricId, detectionConfig, Context.NONE);
        System.out.printf("Response statusCode: %d%n", response.getStatusCode());
        AnomalyDetectionConfiguration createdDetectionConfig = response.getValue();
        System.out.printf("Detection config Id: %s%n", createdDetectionConfig.getId());
        System.out.printf("Name: %s%n", createdDetectionConfig.getName());
        System.out.printf("Description: %s%n", createdDetectionConfig.getDescription());
        System.out.printf("MetricId: %s%n", createdDetectionConfig.getMetricId());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createDetectionConfigWithResponse#String-AnomalyDetectionConfiguration-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#getDetectionConfig(String)}.
     */
    public void getDetectionConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDetectionConfig#String
        final String detectionConfigId = "7b8069a1-1564-46da-9f50-b5d0dd9129ab";
        AnomalyDetectionConfiguration detectionConfig = metricsAdvisorAdminClient
            .getDetectionConfig(detectionConfigId);
        System.out.printf("Detection config Id: %s%n", detectionConfig.getId());
        System.out.printf("Name: %s%n", detectionConfig.getName());
        System.out.printf("Description: %s%n", detectionConfig.getDescription());
        System.out.printf("MetricId: %s%n", detectionConfig.getMetricId());

        System.out.printf("Detection conditions specified for configuration...%n");

        System.out.printf("Whole Series Detection Conditions:%n");
        MetricWholeSeriesDetectionCondition wholeSeriesDetectionCondition
            = detectionConfig.getWholeSeriesDetectionCondition();

        System.out.printf("- Use %s operator for multiple detection conditions:%n",
            wholeSeriesDetectionCondition.getConditionOperator());

        System.out.printf("- Smart Detection Condition:%n");
        System.out.printf(" - Sensitivity: %s%n",
            wholeSeriesDetectionCondition.getSmartDetectionCondition()
                .getSensitivity());
        System.out.printf(" - Detection direction: %s%n",
            wholeSeriesDetectionCondition.getSmartDetectionCondition()
                .getAnomalyDetectorDirection());
        System.out.printf(" - Suppress conditions: minimum number: %s; minimum ratio: %s%n",
            wholeSeriesDetectionCondition.getSmartDetectionCondition()
                .getSuppressCondition().getMinNumber(),
            wholeSeriesDetectionCondition.getSmartDetectionCondition()
                .getSuppressCondition().getMinRatio());

        System.out.printf("- Hard Threshold Condition:%n");
        System.out.printf(" - Lower bound: %s%n",
            wholeSeriesDetectionCondition.getHardThresholdCondition()
                .getLowerBound());
        System.out.printf(" - Upper bound: %s%n",
            wholeSeriesDetectionCondition.getHardThresholdCondition()
                .getUpperBound());
        System.out.printf(" - Suppress conditions: minimum number: %s; minimum ratio: %s%n",
            wholeSeriesDetectionCondition.getHardThresholdCondition()
                .getSuppressCondition().getMinNumber(),
            wholeSeriesDetectionCondition.getHardThresholdCondition()
                .getSuppressCondition().getMinRatio());

        System.out.printf("- Change Threshold Condition:%n");
        System.out.printf(" - Change percentage: %s%n",
            wholeSeriesDetectionCondition.getChangeThresholdCondition()
                .getChangePercentage());
        System.out.printf(" - Shift point: %s%n",
            wholeSeriesDetectionCondition.getChangeThresholdCondition()
                .getShiftPoint());
        System.out.printf(" - Detect anomaly if within range: %s%n",
            wholeSeriesDetectionCondition.getChangeThresholdCondition()
                .isWithinRange());
        System.out.printf(" - Suppress conditions: minimum number: %s; minimum ratio: %s%n",
            wholeSeriesDetectionCondition.getChangeThresholdCondition()
                .getSuppressCondition().getMinNumber(),
            wholeSeriesDetectionCondition.getChangeThresholdCondition()
                .getSuppressCondition().getMinRatio());

        List<MetricSingleSeriesDetectionCondition> seriesDetectionConditions
            = detectionConfig.getSeriesDetectionConditions();
        System.out.printf("Series Detection Conditions:%n");
        for (MetricSingleSeriesDetectionCondition seriesDetectionCondition : seriesDetectionConditions) {
            DimensionKey seriesKey = seriesDetectionCondition.getSeriesKey();
            final String seriesKeyStr
                = Arrays.toString(seriesKey.asMap().entrySet().toArray());
            System.out.printf("- Series Key:%s%n", seriesKeyStr);
            System.out.printf(" - Use %s operator for multiple detection conditions:%n",
                seriesDetectionCondition.getConditionOperator());

            System.out.printf(" - Smart Detection Condition:%n");
            System.out.printf("  - Sensitivity: %s%n",
                seriesDetectionCondition.getSmartDetectionCondition()
                    .getSensitivity());
            System.out.printf("  - Detection direction: %s%n",
                seriesDetectionCondition.getSmartDetectionCondition()
                    .getAnomalyDetectorDirection());
            System.out.printf("  - Suppress conditions: minimum number: %s; minimum ratio: %s%n",
                seriesDetectionCondition.getSmartDetectionCondition()
                    .getSuppressCondition().getMinNumber(),
                seriesDetectionCondition.getSmartDetectionCondition()
                    .getSuppressCondition().getMinRatio());

            System.out.printf(" - Hard Threshold Condition:%n");
            System.out.printf("  -  Lower bound: %s%n",
                seriesDetectionCondition.getHardThresholdCondition()
                    .getLowerBound());
            System.out.printf("  -  Upper bound: %s%n",
                seriesDetectionCondition.getHardThresholdCondition()
                    .getUpperBound());
            System.out.printf("  -  Suppress conditions: minimum number: %s; minimum ratio: %s%n",
                seriesDetectionCondition.getHardThresholdCondition()
                    .getSuppressCondition().getMinNumber(),
                seriesDetectionCondition.getHardThresholdCondition()
                    .getSuppressCondition().getMinRatio());

            System.out.printf(" - Change Threshold Condition:%n");
            System.out.printf("  -  Change percentage: %s%n",
                seriesDetectionCondition.getChangeThresholdCondition()
                    .getChangePercentage());
            System.out.printf("  -  Shift point: %s%n",
                seriesDetectionCondition.getChangeThresholdCondition()
                    .getShiftPoint());
            System.out.printf("  -  Detect anomaly if within range: %s%n",
                seriesDetectionCondition.getChangeThresholdCondition()
                    .isWithinRange());
            System.out.printf("  -  Suppress conditions: minimum number: %s; minimum ratio: %s%n",
                seriesDetectionCondition.getChangeThresholdCondition()
                    .getSuppressCondition().getMinNumber(),
                seriesDetectionCondition.getChangeThresholdCondition()
                    .getSuppressCondition().getMinRatio());
        }

        List<MetricSeriesGroupDetectionCondition> seriesGroupDetectionConditions
            = detectionConfig.getSeriesGroupDetectionConditions();
        System.out.printf("Series Group Detection Conditions:%n");
        for (MetricSeriesGroupDetectionCondition seriesGroupDetectionCondition
            : seriesGroupDetectionConditions) {
            DimensionKey seriesGroupKey = seriesGroupDetectionCondition.getSeriesGroupKey();
            final String seriesGroupKeyStr
                = Arrays.toString(seriesGroupKey.asMap().entrySet().toArray());
            System.out.printf("- Series Group Key:%s%n", seriesGroupKeyStr);
            System.out.printf(" - Use %s operator for multiple detection conditions:%n",
                seriesGroupDetectionCondition.getConditionOperator());

            System.out.printf(" - Smart Detection Condition:%n");
            System.out.printf("  - Sensitivity: %s%n",
                seriesGroupDetectionCondition.getSmartDetectionCondition()
                    .getSensitivity());
            System.out.printf("  - Detection direction: %s%n",
                seriesGroupDetectionCondition.getSmartDetectionCondition()
                    .getAnomalyDetectorDirection());
            System.out.printf("  - Suppress conditions: minimum number: %s; minimum ratio: %s%n",
                seriesGroupDetectionCondition.getSmartDetectionCondition()
                    .getSuppressCondition().getMinNumber(),
                seriesGroupDetectionCondition.getSmartDetectionCondition()
                    .getSuppressCondition().getMinRatio());

            System.out.printf(" - Hard Threshold Condition:%n");
            System.out.printf("  -  Lower bound: %s%n",
                seriesGroupDetectionCondition.getHardThresholdCondition()
                    .getLowerBound());
            System.out.printf("  -  Upper bound: %s%n",
                seriesGroupDetectionCondition.getHardThresholdCondition()
                    .getUpperBound());
            System.out.printf("  -  Suppress conditions: minimum number: %s; minimum ratio: %s%n",
                seriesGroupDetectionCondition.getHardThresholdCondition()
                    .getSuppressCondition().getMinNumber(),
                seriesGroupDetectionCondition.getHardThresholdCondition()
                    .getSuppressCondition().getMinRatio());

            System.out.printf(" - Change Threshold Condition:%n");
            System.out.printf("  -  Change percentage: %s%n",
                seriesGroupDetectionCondition.getChangeThresholdCondition()
                    .getChangePercentage());
            System.out.printf("  -  Shift point: %s%n",
                seriesGroupDetectionCondition.getChangeThresholdCondition()
                    .getShiftPoint());
            System.out.printf("  -  Detect anomaly if within range: %s%n",
                seriesGroupDetectionCondition.getChangeThresholdCondition()
                    .isWithinRange());
            System.out.printf("  -  Suppress conditions: minimum number: %s; minimum ratio: %s%n",
                seriesGroupDetectionCondition.getChangeThresholdCondition()
                    .getSuppressCondition().getMinNumber(),
                seriesGroupDetectionCondition.getChangeThresholdCondition()
                    .getSuppressCondition().getMinRatio());
        }
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDetectionConfig#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#getDetectionConfigWithResponse(String, Context)}.
     */
    public void getDetectionConfigurationWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDetectionConfigWithResponse#String-Context
        final String detectionConfigId = "7b8069a1-1564-46da-9f50-b5d0dd9129ab";
        Response<AnomalyDetectionConfiguration> response = metricsAdvisorAdminClient
            .getDetectionConfigWithResponse(detectionConfigId, Context.NONE);
        System.out.printf("Response statusCode: %d%n", response.getStatusCode());
        AnomalyDetectionConfiguration detectionConfig = response.getValue();
        System.out.printf("Detection config Id: %s%n", detectionConfig.getId());
        System.out.printf("Name: %s%n", detectionConfig.getName());
        System.out.printf("Description: %s%n", detectionConfig.getDescription());
        System.out.printf("MetricId: %s%n", detectionConfig.getMetricId());

        System.out.printf("Detection conditions specified for configuration...%n");

        System.out.printf("Whole Series Detection Conditions:%n");
        MetricWholeSeriesDetectionCondition wholeSeriesDetectionCondition
            = detectionConfig.getWholeSeriesDetectionCondition();

        System.out.printf("- Use %s operator for multiple detection conditions:%n",
            wholeSeriesDetectionCondition.getConditionOperator());

        System.out.printf("- Smart Detection Condition:%n");
        System.out.printf(" - Sensitivity: %s%n",
            wholeSeriesDetectionCondition.getSmartDetectionCondition()
                .getSensitivity());
        System.out.printf(" - Detection direction: %s%n",
            wholeSeriesDetectionCondition.getSmartDetectionCondition()
                .getAnomalyDetectorDirection());
        System.out.printf(" - Suppress conditions: minimum number: %s; minimum ratio: %s%n",
            wholeSeriesDetectionCondition.getSmartDetectionCondition()
                .getSuppressCondition().getMinNumber(),
            wholeSeriesDetectionCondition.getSmartDetectionCondition()
                .getSuppressCondition().getMinRatio());

        System.out.printf("- Hard Threshold Condition:%n");
        System.out.printf(" - Lower bound: %s%n",
            wholeSeriesDetectionCondition.getHardThresholdCondition()
                .getLowerBound());
        System.out.printf(" - Upper bound: %s%n",
            wholeSeriesDetectionCondition.getHardThresholdCondition()
                .getUpperBound());
        System.out.printf(" - Suppress conditions: minimum number: %s; minimum ratio: %s%n",
            wholeSeriesDetectionCondition.getHardThresholdCondition()
                .getSuppressCondition().getMinNumber(),
            wholeSeriesDetectionCondition.getHardThresholdCondition()
                .getSuppressCondition().getMinRatio());

        System.out.printf("- Change Threshold Condition:%n");
        System.out.printf(" - Change percentage: %s%n",
            wholeSeriesDetectionCondition.getChangeThresholdCondition()
                .getChangePercentage());
        System.out.printf(" - Shift point: %s%n",
            wholeSeriesDetectionCondition.getChangeThresholdCondition()
                .getShiftPoint());
        System.out.printf(" - Detect anomaly if within range: %s%n",
            wholeSeriesDetectionCondition.getChangeThresholdCondition()
                .isWithinRange());
        System.out.printf(" - Suppress conditions: minimum number: %s; minimum ratio: %s%n",
            wholeSeriesDetectionCondition.getChangeThresholdCondition()
                .getSuppressCondition().getMinNumber(),
            wholeSeriesDetectionCondition.getChangeThresholdCondition()
                .getSuppressCondition().getMinRatio());

        List<MetricSingleSeriesDetectionCondition> seriesDetectionConditions
            = detectionConfig.getSeriesDetectionConditions();
        System.out.printf("Series Detection Conditions:%n");
        for (MetricSingleSeriesDetectionCondition seriesDetectionCondition : seriesDetectionConditions) {
            DimensionKey seriesKey = seriesDetectionCondition.getSeriesKey();
            final String seriesKeyStr
                = Arrays.toString(seriesKey.asMap().entrySet().toArray());
            System.out.printf("- Series Key:%s%n", seriesKeyStr);
            System.out.printf(" - Use %s operator for multiple detection conditions:%n",
                seriesDetectionCondition.getConditionOperator());

            System.out.printf(" - Smart Detection Condition:%n");
            System.out.printf("  - Sensitivity: %s%n",
                seriesDetectionCondition.getSmartDetectionCondition()
                    .getSensitivity());
            System.out.printf("  - Detection direction: %s%n",
                seriesDetectionCondition.getSmartDetectionCondition()
                    .getAnomalyDetectorDirection());
            System.out.printf("  - Suppress conditions: minimum number: %s; minimum ratio: %s%n",
                seriesDetectionCondition.getSmartDetectionCondition()
                    .getSuppressCondition().getMinNumber(),
                seriesDetectionCondition.getSmartDetectionCondition()
                    .getSuppressCondition().getMinRatio());

            System.out.printf(" - Hard Threshold Condition:%n");
            System.out.printf("  -  Lower bound: %s%n",
                seriesDetectionCondition.getHardThresholdCondition()
                    .getLowerBound());
            System.out.printf("  -  Upper bound: %s%n",
                seriesDetectionCondition.getHardThresholdCondition()
                    .getUpperBound());
            System.out.printf("  -  Suppress conditions: minimum number: %s; minimum ratio: %s%n",
                seriesDetectionCondition.getHardThresholdCondition()
                    .getSuppressCondition().getMinNumber(),
                seriesDetectionCondition.getHardThresholdCondition()
                    .getSuppressCondition().getMinRatio());

            System.out.printf(" - Change Threshold Condition:%n");
            System.out.printf("  -  Change percentage: %s%n",
                seriesDetectionCondition.getChangeThresholdCondition()
                    .getChangePercentage());
            System.out.printf("  -  Shift point: %s%n",
                seriesDetectionCondition.getChangeThresholdCondition()
                    .getShiftPoint());
            System.out.printf("  -  Detect anomaly if within range: %s%n",
                seriesDetectionCondition.getChangeThresholdCondition()
                    .isWithinRange());
            System.out.printf("  -  Suppress conditions: minimum number: %s; minimum ratio: %s%n",
                seriesDetectionCondition.getChangeThresholdCondition()
                    .getSuppressCondition().getMinNumber(),
                seriesDetectionCondition.getChangeThresholdCondition()
                    .getSuppressCondition().getMinRatio());
        }

        List<MetricSeriesGroupDetectionCondition> seriesGroupDetectionConditions
            = detectionConfig.getSeriesGroupDetectionConditions();
        System.out.printf("Series Group Detection Conditions:%n");
        for (MetricSeriesGroupDetectionCondition seriesGroupDetectionCondition
            : seriesGroupDetectionConditions) {
            DimensionKey seriesGroupKey = seriesGroupDetectionCondition.getSeriesGroupKey();
            final String seriesGroupKeyStr
                = Arrays.toString(seriesGroupKey.asMap().entrySet().toArray());
            System.out.printf("- Series Group Key:%s%n", seriesGroupKeyStr);
            System.out.printf(" - Use %s operator for multiple detection conditions:%n",
                seriesGroupDetectionCondition.getConditionOperator());

            System.out.printf(" - Smart Detection Condition:%n");
            System.out.printf("  - Sensitivity: %s%n",
                seriesGroupDetectionCondition.getSmartDetectionCondition()
                    .getSensitivity());
            System.out.printf("  - Detection direction: %s%n",
                seriesGroupDetectionCondition.getSmartDetectionCondition()
                    .getAnomalyDetectorDirection());
            System.out.printf("  - Suppress conditions: minimum number: %s; minimum ratio: %s%n",
                seriesGroupDetectionCondition.getSmartDetectionCondition()
                    .getSuppressCondition().getMinNumber(),
                seriesGroupDetectionCondition.getSmartDetectionCondition()
                    .getSuppressCondition().getMinRatio());

            System.out.printf(" - Hard Threshold Condition:%n");
            System.out.printf("  -  Lower bound: %s%n",
                seriesGroupDetectionCondition.getHardThresholdCondition()
                    .getLowerBound());
            System.out.printf("  -  Upper bound: %s%n",
                seriesGroupDetectionCondition.getHardThresholdCondition()
                    .getUpperBound());
            System.out.printf("  -  Suppress conditions: minimum number: %s; minimum ratio: %s%n",
                seriesGroupDetectionCondition.getHardThresholdCondition()
                    .getSuppressCondition().getMinNumber(),
                seriesGroupDetectionCondition.getHardThresholdCondition()
                    .getSuppressCondition().getMinRatio());

            System.out.printf(" - Change Threshold Condition:%n");
            System.out.printf("  -  Change percentage: %s%n",
                seriesGroupDetectionCondition.getChangeThresholdCondition()
                    .getChangePercentage());
            System.out.printf("  -  Shift point: %s%n",
                seriesGroupDetectionCondition.getChangeThresholdCondition()
                    .getShiftPoint());
            System.out.printf("  -  Detect anomaly if within range: %s%n",
                seriesGroupDetectionCondition.getChangeThresholdCondition()
                    .isWithinRange());
            System.out.printf("  -  Suppress conditions: minimum number: %s; minimum ratio: %s%n",
                seriesGroupDetectionCondition.getChangeThresholdCondition()
                    .getSuppressCondition().getMinNumber(),
                seriesGroupDetectionCondition.getChangeThresholdCondition()
                    .getSuppressCondition().getMinRatio());
        }
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDetectionConfigWithResponse#String-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#listDetectionConfigs(String)}.
     */
    public void listDetectionConfigurations() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDetectionConfigs#String
        final String metricId = "0b836da8-10e6-46cd-8f4f-28262e113a62";
        PagedIterable<AnomalyDetectionConfiguration> configsIterable
            = metricsAdvisorAdminClient.listDetectionConfigs(metricId);

        for (AnomalyDetectionConfiguration detectionConfig : configsIterable) {
            System.out.printf("Detection config Id: %s%n", detectionConfig.getId());
            System.out.printf("Name: %s%n", detectionConfig.getName());
            System.out.printf("Description: %s%n", detectionConfig.getDescription());
            System.out.printf("MetricId: %s%n", detectionConfig.getMetricId());
        }
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDetectionConfigs#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#listDetectionConfigs(String, ListDetectionConfigsOptions, Context)}.
     */
    public void listDetectionConfigurationsWithContext() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDetectionConfigs#String-ListDetectionConfigsOptions-Context
        final String metricId = "0b836da8-10e6-46cd-8f4f-28262e113a62";
        PagedIterable<AnomalyDetectionConfiguration> configsIterable
            = metricsAdvisorAdminClient.listDetectionConfigs(metricId,
                new ListDetectionConfigsOptions(), Context.NONE);

        Stream<PagedResponse<AnomalyDetectionConfiguration>> configByPageStream
            = configsIterable.streamByPage();

        configByPageStream.forEach(configPage -> {
            IterableStream<AnomalyDetectionConfiguration> pageElements = configPage.getElements();
            for (AnomalyDetectionConfiguration detectionConfig : pageElements) {
                System.out.printf("Detection config Id: %s%n", detectionConfig.getId());
                System.out.printf("Name: %s%n", detectionConfig.getName());
                System.out.printf("Description: %s%n", detectionConfig.getDescription());
                System.out.printf("MetricId: %s%n", detectionConfig.getMetricId());
            }
        });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDetectionConfigs#String-ListDetectionConfigsOptions-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#updateDetectionConfig(AnomalyDetectionConfiguration)}.
     */
    public void updateDetectionConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateDetectionConfig#AnomalyDetectionConfiguration
        final String detectionConfigId = "7b8069a1-1564-46da-9f50-b5d0dd9129ab";
        AnomalyDetectionConfiguration detectionConfig = metricsAdvisorAdminClient
            .getDetectionConfig(detectionConfigId);

        detectionConfig.setName("updated config name");
        detectionConfig.setDescription("updated with more detection conditions");
        DimensionKey seriesGroupKey = new DimensionKey()
            .put("city", "Seoul");
        detectionConfig.addSeriesGroupDetectionCondition(
            new MetricSeriesGroupDetectionCondition(seriesGroupKey)
                .setSmartDetectionCondition(new SmartDetectionCondition(
                    10.0,
                    AnomalyDetectorDirection.UP,
                    new SuppressCondition(2, 2))));

        AnomalyDetectionConfiguration updatedDetectionConfig = metricsAdvisorAdminClient
            .updateDetectionConfig(detectionConfig);

        System.out.printf("Detection config Id: %s%n", updatedDetectionConfig.getId());
        System.out.printf("Name: %s%n", updatedDetectionConfig.getName());
        System.out.printf("Description: %s%n", updatedDetectionConfig.getDescription());
        System.out.printf("MetricId: %s%n", updatedDetectionConfig.getMetricId());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateDetectionConfig#AnomalyDetectionConfiguration
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#updateDetectionConfigWithResponse(AnomalyDetectionConfiguration, Context)}
     */
    public void updateDetectionConfigurationWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateDetectionConfigWithResponse#AnomalyDetectionConfiguration-Context
        final String detectionConfigId = "7b8069a1-1564-46da-9f50-b5d0dd9129ab";
        Response<AnomalyDetectionConfiguration> getResponse = metricsAdvisorAdminClient
            .getDetectionConfigWithResponse(detectionConfigId, Context.NONE);
        AnomalyDetectionConfiguration detectionConfig = getResponse.getValue();
        detectionConfig.setName("updated config name");
        detectionConfig.setDescription("updated with more detection conditions");
        DimensionKey seriesGroupKey = new DimensionKey()
            .put("city", "Seoul");
        detectionConfig.addSeriesGroupDetectionCondition(
            new MetricSeriesGroupDetectionCondition(seriesGroupKey)
                .setSmartDetectionCondition(new SmartDetectionCondition(
                    10.0,
                    AnomalyDetectorDirection.UP,
                    new SuppressCondition(2, 2))));

        Response<AnomalyDetectionConfiguration> updateResponse = metricsAdvisorAdminClient
            .updateDetectionConfigWithResponse(detectionConfig, Context.NONE);

        System.out.printf("Response StatusCode: %s%n", updateResponse.getStatusCode());
        AnomalyDetectionConfiguration updatedDetectionConfig = updateResponse.getValue();
        System.out.printf("Detection config Id: %s%n", updatedDetectionConfig.getId());
        System.out.printf("Name: %s%n", updatedDetectionConfig.getName());
        System.out.printf("Description: %s%n", updatedDetectionConfig.getDescription());
        System.out.printf("MetricId: %s%n", updatedDetectionConfig.getMetricId());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateDetectionConfigWithResponse#AnomalyDetectionConfiguration-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#deleteDetectionConfig(String)}.
     */
    public void deleteDetectionConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteDetectionConfig#String
        final String detectionConfigId = "7b8069a1-1564-46da-9f50-b5d0dd9129ab";
        metricsAdvisorAdminClient
            .deleteDetectionConfig(detectionConfigId);
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteDetectionConfig#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#deleteDetectionConfigWithResponse(String, Context)}.
     */
    public void deleteDetectionConfigurationWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteDetectionConfigWithResponse#String-Context
        final String detectionConfigId = "7b8069a1-1564-46da-9f50-b5d0dd9129ab";
        Response<Void> response = metricsAdvisorAdminClient
            .deleteDetectionConfigWithResponse(detectionConfigId, Context.NONE);
        System.out.printf("Response Status Code: %s%n", response.getStatusCode());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteDetectionConfigWithResponse#String-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#createAlertConfig(AnomalyAlertConfiguration)}
     */
    public void createAnomalyAlertConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createAlertConfig#AnomalyAlertConfiguration
        String detectionConfigurationId1 = "9ol48er30-6e6e-4391-b78f-b00dfee1e6f5";
        String detectionConfigurationId2 = "3e58er30-6e6e-4391-b78f-b00dfee1e6f5";
        String hookId1 = "5f48er30-6e6e-4391-b78f-b00dfee1e6f5";
        String hookId2 = "8i48er30-6e6e-4391-b78f-b00dfee1e6f5";

        final AnomalyAlertConfiguration anomalyAlertConfiguration
            = metricsAdvisorAdminClient.createAlertConfig(
                new AnomalyAlertConfiguration("My AnomalyAlert config name")
                .setDescription("alert config description")
                .setMetricAlertConfigurations(Arrays.asList(
                    new MetricAlertConfiguration(detectionConfigurationId1,
                        MetricAnomalyAlertScope.forWholeSeries()),
                    new MetricAlertConfiguration(detectionConfigurationId2,
                        MetricAnomalyAlertScope.forWholeSeries())
                        .setAlertConditions(new MetricAnomalyAlertConditions()
                            .setSeverityRangeCondition(new SeverityCondition(AnomalySeverity.HIGH,
                                AnomalySeverity.HIGH)))))
                .setCrossMetricsOperator(MetricAlertConfigurationsOperator.AND)
                .setHookIdsToAlert(Arrays.asList(hookId1, hookId2)));

        System.out.printf("DataPoint Anomaly alert configuration Id: %s%n",
            anomalyAlertConfiguration.getId());
        System.out.printf("DataPoint Anomaly alert configuration description: %s%n",
            anomalyAlertConfiguration.getDescription());
        System.out.printf("DataPoint Anomaly alert configuration hook ids: %s%n",
            anomalyAlertConfiguration.getHookIdsToAlert());
        System.out.printf("DataPoint Anomaly alert configuration cross metrics operator: %s%n",
            anomalyAlertConfiguration.getCrossMetricsOperator().toString());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createAlertConfig#AnomalyAlertConfiguration
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#createAlertConfigWithResponse(AnomalyAlertConfiguration, Context)}
     */
    public void createAnomalyAlertConfigurationWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createAlertConfigWithResponse#AnomalyAlertConfiguration-Context

        String detectionConfigurationId1 = "9ol48er30-6e6e-4391-b78f-b00dfee1e6f5";
        String detectionConfigurationId2 = "3e58er30-6e6e-4391-b78f-b00dfee1e6f5";
        String hookId1 = "5f48er30-6e6e-4391-b78f-b00dfee1e6f5";
        String hookId2 = "8i48er30-6e6e-4391-b78f-b00dfee1e6f5";

        final Response<AnomalyAlertConfiguration> alertConfigurationResponse
            = metricsAdvisorAdminClient.createAlertConfigWithResponse(
                new AnomalyAlertConfiguration("My AnomalyAlert config name")
                .setDescription("alert config description")
                .setMetricAlertConfigurations(Arrays.asList(
                    new MetricAlertConfiguration(detectionConfigurationId1,
                        MetricAnomalyAlertScope.forWholeSeries()),
                    new MetricAlertConfiguration(detectionConfigurationId2,
                        MetricAnomalyAlertScope.forWholeSeries())
                        .setAlertConditions(new MetricAnomalyAlertConditions()
                            .setSeverityRangeCondition(new SeverityCondition(AnomalySeverity.HIGH,
                                AnomalySeverity.HIGH)))))
                .setCrossMetricsOperator(MetricAlertConfigurationsOperator.AND)
                .setHookIdsToAlert(Arrays.asList(hookId1, hookId2)), Context.NONE);

        System.out.printf("DataPoint Anomaly alert creation operation status: %s%n",
            alertConfigurationResponse.getStatusCode());
        final AnomalyAlertConfiguration anomalyAlertConfiguration = alertConfigurationResponse.getValue();
        System.out.printf("DataPoint Anomaly alert configuration Id: %s%n", anomalyAlertConfiguration.getId());
        System.out.printf("DataPoint Anomaly alert configuration description: %s%n",
            anomalyAlertConfiguration.getDescription());
        System.out.printf("DataPoint Anomaly alert configuration hook ids: %s%n",
            anomalyAlertConfiguration.getHookIdsToAlert());
        System.out.printf("DataPoint Anomaly alert configuration cross metrics operator: %s%n",
            anomalyAlertConfiguration.getCrossMetricsOperator().toString());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createAlertConfigWithResponse#AnomalyAlertConfiguration-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#getAlertConfig(String)}
     */
    public void getAnomalyAlertConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getAlertConfig#String
        String alertConfigId = "1p0f8er30-6e6e-4391-b78f-bpfdfee1e6f5";

        AnomalyAlertConfiguration anomalyAlertConfiguration
            = metricsAdvisorAdminClient.getAlertConfig(alertConfigId);
        System.out.printf("DataPoint Anomaly alert configuration Id: %s%n",
            anomalyAlertConfiguration.getId());
        System.out.printf("DataPoint Anomaly alert configuration description: %s%n",
            anomalyAlertConfiguration.getDescription());
        System.out.printf("DataPoint Anomaly alert configuration hook ids: %s%n",
            anomalyAlertConfiguration.getHookIdsToAlert());
        System.out.printf("DataPoint Anomaly alert configuration cross metrics operator: %s%n",
            anomalyAlertConfiguration.getCrossMetricsOperator().toString());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getAlertConfig#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#getAlertConfigWithResponse(String, Context)}
     */
    public void getAnomalyAlertConfigurationWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getAlertConfigWithResponse#String-Context
        String alertConfigId = "1p0f8er30-6e6e-4391-b78f-bpfdfee1e6f5";

        Response<AnomalyAlertConfiguration> alertConfigurationResponse
            = metricsAdvisorAdminClient.getAlertConfigWithResponse(alertConfigId, Context.NONE);

        System.out.printf("DataPoint Anomaly alert creation operation status: %s%n",
            alertConfigurationResponse.getStatusCode());
        final AnomalyAlertConfiguration anomalyAlertConfiguration = alertConfigurationResponse.getValue();
        System.out.printf("DataPoint Anomaly alert configuration Id: %s%n", anomalyAlertConfiguration.getId());
        System.out.printf("DataPoint Anomaly alert configuration description: %s%n",
            anomalyAlertConfiguration.getDescription());
        System.out.printf("DataPoint Anomaly alert configuration hook ids: %s%n",
            anomalyAlertConfiguration.getHookIdsToAlert());
        System.out.printf("DataPoint Anomaly alert configuration cross metrics operator: %s%n",
            anomalyAlertConfiguration.getCrossMetricsOperator().toString());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getAlertConfigWithResponse#String-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#updateAlertConfig(AnomalyAlertConfiguration)}
     */
    public void updateAnomalyAlertConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateAlertConfig#AnomalyAlertConfiguration

        String alertConfigId = "1p0f8er30-6e6e-4391-b78f-bpfdfee1e6f5";
        String additionalHookId = "2gh8er30-6e6e-4391-b78f-bpfdfee1e6f5";

        AnomalyAlertConfiguration existingAnomalyConfig
            = metricsAdvisorAdminClient.getAlertConfig(alertConfigId);
        List<String> hookIds = new ArrayList<>(existingAnomalyConfig.getHookIdsToAlert());
        hookIds.add(additionalHookId);
        final AnomalyAlertConfiguration updatAnomalyAlertConfiguration
            = metricsAdvisorAdminClient.updateAlertConfig(
            existingAnomalyConfig
                .setHookIdsToAlert(hookIds)
                .setDescription("updated to add more hook ids")
        );

        System.out.printf("Updated anomaly alert configuration Id: %s%n", updatAnomalyAlertConfiguration.getId());
        System.out.printf("Updated anomaly alert configuration description: %s%n",
            updatAnomalyAlertConfiguration.getDescription());
        System.out.printf("Updated anomaly alert configuration hook ids: %s%n",
            updatAnomalyAlertConfiguration.getHookIdsToAlert());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateAlertConfig#AnomalyAlertConfiguration
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#updateAlertConfigWithResponse(AnomalyAlertConfiguration, Context)}
     */
    public void updateAnomalyAlertConfigurationWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateAlertConfigWithResponse#AnomalyAlertConfiguration-Context

        String alertConfigId = "1p0f8er30-6e6e-4391-b78f-bpfdfee1e6f5";
        String additionalHookId = "2gh8er30-6e6e-4391-b78f-bpfdfee1e6f5";

        AnomalyAlertConfiguration existingAnomalyConfig
            = metricsAdvisorAdminClient.getAlertConfig(alertConfigId);
        List<String> hookIds = new ArrayList<>(existingAnomalyConfig.getHookIdsToAlert());
        hookIds.add(additionalHookId);
        final Response<AnomalyAlertConfiguration> alertConfigurationResponse
            = metricsAdvisorAdminClient.updateAlertConfigWithResponse(
            existingAnomalyConfig
                .setHookIdsToAlert(hookIds)
                .setDescription("updated to add more hook ids"), Context.NONE);

        System.out.printf("Update anomaly alert operation status: %s%n", alertConfigurationResponse.getStatusCode());
        final AnomalyAlertConfiguration updatAnomalyAlertConfiguration = alertConfigurationResponse.getValue();
        System.out.printf("Updated anomaly alert configuration Id: %s%n", updatAnomalyAlertConfiguration.getId());
        System.out.printf("Updated anomaly alert configuration description: %s%n",
            updatAnomalyAlertConfiguration.getDescription());
        System.out.printf("Updated anomaly alert configuration hook ids: %sf%n",
            updatAnomalyAlertConfiguration.getHookIdsToAlert());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateAlertConfigWithResponse#AnomalyAlertConfiguration-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#deleteAlertConfig(String)}.
     */
    public void deleteAnomalyAlertConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteAlertConfig#String
        String alertConfigId = "1p0f8er30-6e6e-4391-b78f-bpfdfee1e6f5";
        metricsAdvisorAdminClient.deleteAlertConfig(alertConfigId);
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteAlertConfig#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#deleteAlertConfigWithResponse(String, Context)}
     */
    public void deleteAnomalyAlertConfigurationWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteAlertConfigWithResponse#String-Context
        String alertConfigId = "1p0f8er30-6e6e-4391-b78f-bpfdfee1e6f5";
        final Response<Void> response =
            metricsAdvisorAdminClient.deleteAlertConfigWithResponse(alertConfigId, Context.NONE);

        System.out.printf("DataPoint Anomaly alert config delete operation status : %s%n", response.getStatusCode());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteAlertConfigWithResponse#String-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#listAlertConfigs(String, ListAnomalyAlertConfigsOptions)}.
     */
    public void listAnomalyAlertConfigurations() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listAlertConfigs#String-ListAnomalyAlertConfigsOptions
        String detectionConfigId = "3rt98er30-6e6e-4391-b78f-bpfdfee1e6f5";
        metricsAdvisorAdminClient.listAlertConfigs(detectionConfigId, new ListAnomalyAlertConfigsOptions())
            .forEach(anomalyAlertConfiguration -> {
                System.out.printf("DataPoint Anomaly alert configuration Id: %s%n", anomalyAlertConfiguration.getId());
                System.out.printf("DataPoint Anomaly alert configuration description: %s%n",
                    anomalyAlertConfiguration.getDescription());
                System.out.printf("DataPoint Anomaly alert configuration hook ids: %s%n",
                    anomalyAlertConfiguration.getHookIdsToAlert());
                System.out.printf("DataPoint Anomaly alert configuration cross metrics operator: %s%n",
                    anomalyAlertConfiguration.getCrossMetricsOperator().toString());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listAlertConfigs#String-ListAnomalyAlertConfigsOptions
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#listAlertConfigs(String, ListAnomalyAlertConfigsOptions, Context)}.
     */
    public void listAnomalyAlertConfigurationsWithContext() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listAlertConfigs#String-ListAnomalyAlertConfigsOptions-Context
        String detectionConfigId = "3rt98er30-6e6e-4391-b78f-bpfdfee1e6f5";
        metricsAdvisorAdminClient.listAlertConfigs(detectionConfigId,
            new ListAnomalyAlertConfigsOptions(), Context.NONE)
            .forEach(anomalyAlertConfiguration -> {
                System.out.printf("DataPoint Anomaly alert configuration Id: %s%n", anomalyAlertConfiguration.getId());
                System.out.printf("DataPoint Anomaly alert configuration description: %s%n",
                    anomalyAlertConfiguration.getDescription());
                System.out.printf("DataPoint Anomaly alert configuration hook ids: %s%n",
                    anomalyAlertConfiguration.getHookIdsToAlert());
                System.out.printf("DataPoint Anomaly alert configuration cross metrics operator: %s%n",
                    anomalyAlertConfiguration.getCrossMetricsOperator().toString());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listAlertConfigs#String-ListAnomalyAlertConfigsOptions-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#createDataSourceCredential(DataSourceCredentialEntity)}.
     */
    public void createDatasourceCredential() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createDataSourceCredential#DatasourceCredentialEntity
        DataSourceCredentialEntity datasourceCredential;
        final String name = "sample_name" + UUID.randomUUID();
        final String cId = "f45668b2-bffa-11eb-8529-0246ac130003";
        final String tId = "67890ded-5e07-4e52-b225-4ae8f905afb5";
        final String mockSecr = "890hy69-5e07-4e52-b225-4ae8f905afb5";

        datasourceCredential = new DataSourceServicePrincipalInKeyVault()
            .setName(name)
            .setKeyVaultForDataSourceSecrets("kv", cId, mockSecr)
            .setTenantId(tId)
            .setSecretNameForDataSourceClientId("DSClientID_1")
            .setSecretNameForDataSourceClientSecret("DSClientSer_1");

        DataSourceCredentialEntity credentialEntity =
            metricsAdvisorAdminClient.createDataSourceCredential(datasourceCredential);
        if (credentialEntity instanceof DataSourceServicePrincipalInKeyVault) {
            DataSourceServicePrincipalInKeyVault actualCredentialSPInKV
                = (DataSourceServicePrincipalInKeyVault) credentialEntity;
            System.out
                .printf("Actual credential entity key vault endpoint: %s%n",
                    actualCredentialSPInKV.getKeyVaultEndpoint());
            System.out.printf("Actual credential entity key vault client Id: %s%n",
                actualCredentialSPInKV.getKeyVaultClientId());
            System.out.printf("Actual credential entity key vault secret name for data source: %s%n",
                actualCredentialSPInKV.getSecretNameForDataSourceClientId());
            System.out.printf("Actual credential entity key vault secret for data source: %s%n",
                actualCredentialSPInKV.getSecretNameForDataSourceClientSecret());
        }
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createDataSourceCredential#DatasourceCredentialEntity
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#createDataSourceCredentialWithResponse(DataSourceCredentialEntity, Context)}.
     */
    public void createDatasourceCredentialWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createDataSourceCredentialWithResponse#DatasourceCredentialEntity-Context
        DataSourceCredentialEntity datasourceCredential;
        final String name = "sample_name" + UUID.randomUUID();
        final String cId = "f45668b2-bffa-11eb-8529-0246ac130003";
        final String tId = "67890ded-5e07-4e52-b225-4ae8f905afb5";
        final String mockSecr = "890hy69-5e07-4e52-b225-4ae8f905afb5";

        datasourceCredential = new DataSourceServicePrincipalInKeyVault()
            .setName(name)
            .setKeyVaultForDataSourceSecrets("kv", cId, mockSecr)
            .setTenantId(tId)
            .setSecretNameForDataSourceClientId("DSClientID_1")
            .setSecretNameForDataSourceClientSecret("DSClientSer_1");

        Response<DataSourceCredentialEntity> credentialEntityWithResponse =
            metricsAdvisorAdminClient.createDataSourceCredentialWithResponse(datasourceCredential, Context.NONE);

        System.out.printf("Credential Entity creation operation status: %s%n",
            credentialEntityWithResponse.getStatusCode());
        if (credentialEntityWithResponse.getValue() instanceof DataSourceServicePrincipalInKeyVault) {
            DataSourceServicePrincipalInKeyVault actualCredentialSPInKV
                = (DataSourceServicePrincipalInKeyVault) credentialEntityWithResponse.getValue();
            System.out
                .printf("Actual credential entity key vault endpoint: %s%n",
                    actualCredentialSPInKV.getKeyVaultEndpoint());
            System.out.printf("Actual credential entity key vault client Id: %s%n",
                actualCredentialSPInKV.getKeyVaultClientId());
            System.out.printf("Actual credential entity key vault secret name for data source: %s%n",
                actualCredentialSPInKV.getSecretNameForDataSourceClientId());
            System.out.printf("Actual credential entity key vault secret for data source: %s%n",
                actualCredentialSPInKV.getSecretNameForDataSourceClientSecret());
        }
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createDataSourceCredentialWithResponse#DatasourceCredentialEntity-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#updateDataSourceCredential(DataSourceCredentialEntity)}.
     */
    public void updateDatasourceCredential() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateDataSourceCredential#DatasourceCredentialEntity
        final String datasourceCredentialId = "f45668b2-bffa-11eb-8529-0246ac130003";
        DataSourceCredentialEntity existingDatasourceCredential =
            metricsAdvisorAdminClient.getDataSourceCredential(datasourceCredentialId);
        DataSourceServicePrincipalInKeyVault actualCredentialSPInKV = null;
        if (existingDatasourceCredential instanceof DataSourceServicePrincipalInKeyVault) {
            actualCredentialSPInKV  = (DataSourceServicePrincipalInKeyVault) existingDatasourceCredential;
        }

        DataSourceCredentialEntity credentialEntity =
            metricsAdvisorAdminClient.updateDataSourceCredential(
                actualCredentialSPInKV.setDescription("set updated description"));

        if (credentialEntity instanceof DataSourceServicePrincipalInKeyVault) {
            DataSourceServicePrincipalInKeyVault updatedCredentialSPInKV
                = (DataSourceServicePrincipalInKeyVault) credentialEntity;
            System.out.printf("Actual credential entity key vault endpoint: %s%n",
                updatedCredentialSPInKV.getKeyVaultEndpoint());
            System.out.printf("Actual credential entity key vault updated description: %s%n",
                updatedCredentialSPInKV.getDescription());
        }
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateDataSourceCredential#DatasourceCredentialEntity
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#updateDataSourceCredentialWithResponse(DataSourceCredentialEntity, Context)}.
     */
    public void updateDatasourceCredentialWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateDataSourceCredentialWithResponse#DatasourceCredentialEntity-Context
        final String datasourceCredentialId = "f45668b2-bffa-11eb-8529-0246ac130003";
        DataSourceCredentialEntity existingDatasourceCredential =
            metricsAdvisorAdminClient.getDataSourceCredential(datasourceCredentialId);
        DataSourceServicePrincipalInKeyVault actualCredentialSPInKV = null;
        if (existingDatasourceCredential instanceof DataSourceServicePrincipalInKeyVault) {
            actualCredentialSPInKV  = (DataSourceServicePrincipalInKeyVault) existingDatasourceCredential;
        }
        Response<DataSourceCredentialEntity> credentialEntityWithResponse =
            metricsAdvisorAdminClient.updateDataSourceCredentialWithResponse(
                actualCredentialSPInKV.setDescription("set updated description"), Context.NONE);

        System.out.printf("Credential Entity creation operation status: %s%n",
            credentialEntityWithResponse.getStatusCode());
        if (credentialEntityWithResponse.getValue() instanceof DataSourceServicePrincipalInKeyVault) {
            DataSourceServicePrincipalInKeyVault updatedCredentialSPInKV
                = (DataSourceServicePrincipalInKeyVault) credentialEntityWithResponse.getValue();
            System.out.printf("Actual credential entity key vault endpoint: %s%n",
                updatedCredentialSPInKV.getKeyVaultEndpoint());
            System.out.printf("Actual credential entity key vault updated description: %s%n",
                updatedCredentialSPInKV.getDescription());
        }
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateDataSourceCredentialWithResponse#DatasourceCredentialEntity-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#getDataSourceCredential(String)}.
     */
    public void getDatasourceCredential() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDataSourceCredential#String
        final String datasourceCredentialId = "f45668b2-bffa-11eb-8529-0246ac130003";

        DataSourceCredentialEntity credentialEntity =
            metricsAdvisorAdminClient.getDataSourceCredential(datasourceCredentialId);
        if (credentialEntity instanceof DataSourceServicePrincipalInKeyVault) {
            DataSourceServicePrincipalInKeyVault actualCredentialSPInKV
                = (DataSourceServicePrincipalInKeyVault) credentialEntity;
            System.out
                .printf("Actual credential entity key vault endpoint: %s%n",
                    actualCredentialSPInKV.getKeyVaultEndpoint());
            System.out.printf("Actual credential entity key vault client Id: %s%n",
                actualCredentialSPInKV.getKeyVaultClientId());
            System.out.printf("Actual credential entity key vault secret name for data source: %s%n",
                actualCredentialSPInKV.getSecretNameForDataSourceClientId());
            System.out.printf("Actual credential entity key vault secret for data source: %s%n",
                actualCredentialSPInKV.getSecretNameForDataSourceClientSecret());
        }
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDataSourceCredential#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#getDataSourceCredentialWithResponse(String, Context)}.
     */
    public void getDatasourceCredentialWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDataSourceCredentialWithResponse#String-Context
        final String datasourceCredentialId = "f45668b2-bffa-11eb-8529-0246ac130003";

        Response<DataSourceCredentialEntity> credentialEntityWithResponse =
            metricsAdvisorAdminClient.getDataSourceCredentialWithResponse(datasourceCredentialId, Context.NONE);
        System.out.printf("Credential Entity creation operation status: %s%n",
            credentialEntityWithResponse.getStatusCode());
        if (credentialEntityWithResponse.getValue() instanceof DataSourceServicePrincipalInKeyVault) {
            DataSourceServicePrincipalInKeyVault actualCredentialSPInKV
                = (DataSourceServicePrincipalInKeyVault) credentialEntityWithResponse.getValue();
            System.out
                .printf("Actual credential entity key vault endpoint: %s%n",
                    actualCredentialSPInKV.getKeyVaultEndpoint());
            System.out.printf("Actual credential entity key vault client Id: %s%n",
                actualCredentialSPInKV.getKeyVaultClientId());
            System.out.printf("Actual credential entity key vault secret name for data source: %s%n",
                actualCredentialSPInKV.getSecretNameForDataSourceClientId());
            System.out.printf("Actual credential entity key vault secret for data source: %s%n",
                actualCredentialSPInKV.getSecretNameForDataSourceClientSecret());
        }
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDataSourceCredentialWithResponse#String-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#deleteDataSourceCredential(String)}.
     */
    public void deleteDatasourceCredential() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteDataSourceCredential#String
        final String datasourceCredentialId = "t00853f1-9080-447f-bacf-8dccf2e86f";
        metricsAdvisorAdminClient.deleteDataFeed(datasourceCredentialId);
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteDataSourceCredential#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#deleteDataFeedWithResponse(String, Context)}
     */
    public void deleteDatasourceCredentialWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteDataSourceCredentialWithResponse#String-Context
        final String datasourceCredentialId = "eh0854f1-8927-447f-bacf-8dccf2e86fwe";
        Response<Void> response =
            metricsAdvisorAdminClient.deleteDataSourceCredentialWithResponse(datasourceCredentialId, Context.NONE);
        System.out.printf("Datasource credential delete operation status : %s%n", response.getStatusCode());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteDataSourceCredentialWithResponse#String-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#listDataSourceCredentials()}
     */
    public void listDatasourceCredentials() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDataSourceCredentials
        metricsAdvisorAdminClient.listDataSourceCredentials()
            .forEach(datasourceCredentialEntity -> {
                if (datasourceCredentialEntity instanceof DataSourceServicePrincipalInKeyVault) {
                    DataSourceServicePrincipalInKeyVault actualCredentialSPInKV
                        = (DataSourceServicePrincipalInKeyVault) datasourceCredentialEntity;
                    System.out
                        .printf("Actual credential entity key vault endpoint: %s%n",
                            actualCredentialSPInKV.getKeyVaultEndpoint());
                    System.out.printf("Actual credential entity key vault client Id: %s%n",
                        actualCredentialSPInKV.getKeyVaultClientId());
                    System.out.printf("Actual credential entity key vault secret name for data source: %s%n",
                        actualCredentialSPInKV.getSecretNameForDataSourceClientId());
                    System.out.printf("Actual credential entity key vault secret for data source: %s%n",
                        actualCredentialSPInKV.getSecretNameForDataSourceClientSecret());
                }
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDataSourceCredentials
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#listDataSourceCredentials(ListCredentialEntityOptions, Context)} with options.
     */
    public void listDatasourceCredentialsWithOptions() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDataSourceCredentials#ListCredentialEntityOptions-Context
        metricsAdvisorAdminClient.listDataSourceCredentials(
                new ListCredentialEntityOptions()
                    .setMaxPageSize(3),
                Context.NONE)
            .forEach(datasourceCredentialEntity -> {
                if (datasourceCredentialEntity instanceof DataSourceServicePrincipalInKeyVault) {
                    DataSourceServicePrincipalInKeyVault actualCredentialSPInKV
                        = (DataSourceServicePrincipalInKeyVault) datasourceCredentialEntity;
                    System.out
                        .printf("Actual credential entity key vault endpoint: %s%n",
                            actualCredentialSPInKV.getKeyVaultEndpoint());
                    System.out.printf("Actual credential entity key vault client Id: %s%n",
                        actualCredentialSPInKV.getKeyVaultClientId());
                    System.out.printf("Actual credential entity key vault secret name for data source: %s%n",
                        actualCredentialSPInKV.getSecretNameForDataSourceClientId());
                    System.out.printf("Actual credential entity key vault secret for data source: %s%n",
                        actualCredentialSPInKV.getSecretNameForDataSourceClientSecret());
                }
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDataSourceCredentials#ListCredentialEntityOptions-Context
    }

}
