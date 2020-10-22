// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration;

import com.azure.ai.metricsadvisor.models.AnomalyDetectionConfiguration;
import com.azure.ai.metricsadvisor.models.AnomalyDetectorDirection;
import com.azure.ai.metricsadvisor.models.ChangeThresholdCondition;
import com.azure.ai.metricsadvisor.models.AnomalyAlertConfiguration;
import com.azure.ai.metricsadvisor.models.DataFeed;
import com.azure.ai.metricsadvisor.models.DataFeedGranularity;
import com.azure.ai.metricsadvisor.models.DataFeedGranularityType;
import com.azure.ai.metricsadvisor.models.DataFeedIngestionProgress;
import com.azure.ai.metricsadvisor.models.DataFeedIngestionSettings;
import com.azure.ai.metricsadvisor.models.DataFeedIngestionStatus;
import com.azure.ai.metricsadvisor.models.DataFeedOptions;
import com.azure.ai.metricsadvisor.models.DataFeedRollupSettings;
import com.azure.ai.metricsadvisor.models.DataFeedRollupType;
import com.azure.ai.metricsadvisor.models.DataFeedSchema;
import com.azure.ai.metricsadvisor.models.DataFeedSource;
import com.azure.ai.metricsadvisor.models.DataFeedStatus;
import com.azure.ai.metricsadvisor.models.DetectionConditionsOperator;
import com.azure.ai.metricsadvisor.models.DimensionKey;
import com.azure.ai.metricsadvisor.models.EmailHook;
import com.azure.ai.metricsadvisor.models.HardThresholdCondition;
import com.azure.ai.metricsadvisor.models.Hook;
import com.azure.ai.metricsadvisor.models.ListDataFeedFilter;
import com.azure.ai.metricsadvisor.models.ListDataFeedIngestionOptions;
import com.azure.ai.metricsadvisor.models.ListDataFeedOptions;
import com.azure.ai.metricsadvisor.models.ListHookOptions;
import com.azure.ai.metricsadvisor.models.Metric;
import com.azure.ai.metricsadvisor.models.MetricAnomalyAlertConditions;
import com.azure.ai.metricsadvisor.models.MetricSeriesGroupDetectionCondition;
import com.azure.ai.metricsadvisor.models.MetricSingleSeriesDetectionCondition;
import com.azure.ai.metricsadvisor.models.MetricWholeSeriesDetectionCondition;
import com.azure.ai.metricsadvisor.models.MetricAnomalyAlertConfiguration;
import com.azure.ai.metricsadvisor.models.MetricAnomalyAlertConfigurationsOperator;
import com.azure.ai.metricsadvisor.models.MetricAnomalyAlertScope;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import com.azure.ai.metricsadvisor.models.MySqlDataFeedSource;
import com.azure.ai.metricsadvisor.models.Severity;
import com.azure.ai.metricsadvisor.models.SeverityCondition;
import com.azure.ai.metricsadvisor.models.SmartDetectionCondition;
import com.azure.ai.metricsadvisor.models.SuppressCondition;
import com.azure.ai.metricsadvisor.models.WebHook;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.IterableStream;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Code snippet for {@link MetricsAdvisorAdministrationClient}
 */
public class MetricsAdvisorAdministrationClientJavaDocCodeSnippets {
    private MetricsAdvisorAdministrationClient metricsAdvisorAdministrationClient =
        new MetricsAdvisorAdministrationClientBuilder().buildClient();

    /**
     * Code snippet for creating a {@link MetricsAdvisorAdministrationClient}
     */
    public void createMetricsAdvisorAdministrationClient() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.instantiation
        MetricsAdvisorAdministrationClient metricsAdvisorAdministrationClient =
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
     * Code snippet for {@link MetricsAdvisorAdministrationClient#createDataFeed(String, DataFeedSource,
     * DataFeedGranularity, DataFeedSchema, DataFeedIngestionSettings, DataFeedOptions)}
     */
    public void createDataFeed() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createDataFeed#String-DataFeedSource-DataFeedGranularity-DataFeedSchema-DataFeedIngestionSettings-DataFeedOptions

        DataFeed createdDataFeed = metricsAdvisorAdministrationClient.createDataFeed(
            "dataFeedName",
            new MySqlDataFeedSource("conn-string", "query"),
            new DataFeedGranularity().setGranularityType(DataFeedGranularityType.DAILY),
            new DataFeedSchema(Arrays.asList(new Metric().setName("metric1"), new Metric().setName(
                "metric2"))),
            new DataFeedIngestionSettings(OffsetDateTime.parse("")),
            new DataFeedOptions().setDescription("data feed description")
                .setRollupSettings(new DataFeedRollupSettings().setRollupType(DataFeedRollupType.AUTO_ROLLUP)));

        System.out.printf("Data feed Id: %s%n", createdDataFeed.getId());
        System.out.printf("Data feed description: %s%n", createdDataFeed.getOptions().getDescription());
        System.out.printf("Data feed source type: %.2f%n", createdDataFeed.getSourceType());
        System.out.printf("Data feed creator: %.2f%n", createdDataFeed.getCreator());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createDataFeed#String-DataFeedSource-DataFeedGranularity-DataFeedSchema-DataFeedIngestionSettings-DataFeedOptions
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#createDataFeedWithResponse(String,
     * DataFeedSource, DataFeedGranularity, DataFeedSchema, DataFeedIngestionSettings, DataFeedOptions, Context)}
     */
    public void createDataFeedWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createDataFeedWithResponse#String-DataFeedSource-DataFeedGranularity-DataFeedSchema-DataFeedIngestionSettings-DataFeedOptions-Context

        final Response<DataFeed> createdDataFeedResponse =
            metricsAdvisorAdministrationClient.createDataFeedWithResponse(
                "dataFeedName",
                new MySqlDataFeedSource("conn-string", "query"),
                new DataFeedGranularity().setGranularityType(DataFeedGranularityType.DAILY),
                new DataFeedSchema(Arrays.asList(new Metric().setName("metric1"), new Metric().setName(
                    "metric2"))),
                new DataFeedIngestionSettings(OffsetDateTime.parse("")),
                new DataFeedOptions().setDescription("data feed description")
                    .setRollupSettings(new DataFeedRollupSettings().setRollupType(DataFeedRollupType.AUTO_ROLLUP)),
                Context.NONE);

        System.out.printf("Data feed create operation status: %s%n", createdDataFeedResponse.getStatusCode());
        DataFeed dataFeed = createdDataFeedResponse.getValue();
        System.out.printf("Data feed Id: %s%n", dataFeed.getId());
        System.out.printf("Data feed description: %s%n", dataFeed.getOptions().getDescription());
        System.out.printf("Data feed source type: %.2f%n", dataFeed.getSourceType());
        System.out.printf("Data feed creator: %.2f%n", dataFeed.getCreator());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createDataFeedWithResponse#String-DataFeedSource-DataFeedGranularity-DataFeedSchema-DataFeedIngestionSettings-DataFeedOptions-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#getDataFeed(String)}
     */
    public void getDataFeed() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDataFeed#String

        DataFeed dataFeed = metricsAdvisorAdministrationClient.getDataFeed("dataFeedId");
        System.out.printf("Data feed Id: %s%n", dataFeed.getId());
        System.out.printf("Data feed description: %s%n", dataFeed.getOptions().getDescription());
        System.out.printf("Data feed source type: %.2f%n", dataFeed.getSourceType());
        System.out.printf("Data feed creator: %.2f%n", dataFeed.getCreator());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDataFeed#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#getDataFeedWithResponse(String, Context)}
     */
    public void getDataFeedWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDataFeedWithResponse#String-Context

        final Response<DataFeed> dataFeedResponse =
            metricsAdvisorAdministrationClient.getDataFeedWithResponse("dataFeedId", Context.NONE);

        System.out.printf("Data feed create operation status: %s%n", dataFeedResponse.getStatusCode());
        DataFeed dataFeed = dataFeedResponse.getValue();
        System.out.printf("Data feed Id: %s%n", dataFeed.getId());
        System.out.printf("Data feed description: %s%n", dataFeed.getOptions().getDescription());
        System.out.printf("Data feed source type: %.2f%n", dataFeed.getSourceType());
        System.out.printf("Data feed creator: %.2f%n", dataFeed.getCreator());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDataFeedWithResponse#String-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#updateDataFeed(DataFeed)}
     */
    public void updateDataFeed() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateDataFeed#DataFeed

        DataFeed existingDataFeed = new DataFeed();
        final DataFeed updatedDataFeed = metricsAdvisorAdministrationClient.updateDataFeed(
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
            metricsAdvisorAdministrationClient.updateDataFeedWithResponse(
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
        metricsAdvisorAdministrationClient.deleteDataFeed("dataFeedId");
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteDataFeed#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#deleteDataFeedWithResponse(String, Context)}
     */
    public void deleteDataFeedWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteDataFeedWithResponse#String-Context
        final Response<Void> response = metricsAdvisorAdministrationClient.deleteDataFeedWithResponse("dataFeedId",
            Context.NONE);

        System.out.printf("Data feed delete operation status : %s%n", response.getStatusCode());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteDataFeedWithResponse#String-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#listDataFeeds()}
     */
    public void listDataFeeds() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDataFeeds
        metricsAdvisorAdministrationClient.listDataFeeds()
            .forEach(dataFeed -> {
                System.out.printf("Data feed Id: %s%n", dataFeed.getId());
                System.out.printf("Data feed description: %s%n", dataFeed.getOptions().getDescription());
                System.out.printf("Data feed source type: %.2f%n", dataFeed.getSourceType());
                System.out.printf("Data feed creator: %.2f%n", dataFeed.getCreator());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDataFeeds
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#listDataFeeds(ListDataFeedOptions, Context)}
     * with options.
     */
    public void listDataFeedWithOptions() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDataFeeds#ListDataFeedOptions-Context
        metricsAdvisorAdministrationClient.listDataFeeds(
            new ListDataFeedOptions()
                .setListDataFeedFilter(
                    new ListDataFeedFilter()
                        .setDataFeedStatus(DataFeedStatus.ACTIVE)
                        .setDataFeedGranularityType(DataFeedGranularityType.DAILY))
                .setTop(3), Context.NONE)
            .forEach(dataFeed -> {
                System.out.printf("Data feed Id: %s%n", dataFeed.getId());
                System.out.printf("Data feed description: %s%n", dataFeed.getOptions().getDescription());
                System.out.printf("Data feed source type: %.2f%n", dataFeed.getSourceType());
                System.out.printf("Data feed creator: %.2f%n", dataFeed.getCreator());
                System.out.printf("Data feed status: %.2f%n", dataFeed.getStatus());
                System.out.printf("Data feed granularity type: %.2f%n", dataFeed.getGranularity().getGranularityType());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDataFeeds#ListDataFeedOptions-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#createHook(Hook)}.
     */
    public void createHook() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createHook#Hook
        Hook emailHook = new EmailHook("email hook")
            .setDescription("my email hook")
            .addEmailToAlert("alertme@alertme.com")
            .setExternalLink("https://adwiki.azurewebsites.net/articles/howto/alerts/create-hooks.html");

        Hook hook = metricsAdvisorAdministrationClient.createHook(emailHook);
        EmailHook createdEmailHook = (EmailHook) hook;
        System.out.printf("Hook Id: %s%n", createdEmailHook.getId());
        System.out.printf("Hook Name: %s%n", createdEmailHook.getName());
        System.out.printf("Hook Description: %s%n", createdEmailHook.getDescription());
        System.out.printf("Hook External Link: %s%n", createdEmailHook.getExternalLink());
        System.out.printf("Hook Emails: %s%n", String.join(",", createdEmailHook.getEmailsToAlert()));
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createHook#Hook
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#createHookWithResponse(Hook)}.
     */
    public void createHookWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createHookWithResponse#Hook-Context
        Hook emailHook = new EmailHook("email hook")
            .setDescription("my email hook")
            .addEmailToAlert("alertme@alertme.com")
            .setExternalLink("https://adwiki.azurewebsites.net/articles/howto/alerts/create-hooks.html");

        Response<Hook> response = metricsAdvisorAdministrationClient.createHookWithResponse(emailHook, Context.NONE);
        System.out.printf("Response statusCode: %d%n", response.getStatusCode());
        EmailHook createdEmailHook = (EmailHook) response.getValue();
        System.out.printf("Hook Id: %s%n", createdEmailHook.getId());
        System.out.printf("Hook Name: %s%n", createdEmailHook.getName());
        System.out.printf("Hook Description: %s%n", createdEmailHook.getDescription());
        System.out.printf("Hook External Link: %s%n", createdEmailHook.getExternalLink());
        System.out.printf("Hook Emails: %s%n", String.join(",", createdEmailHook.getEmailsToAlert()));
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createHookWithResponse#Hook-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#getHook(String)}.
     */
    public void getHook() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getHook#String
        final String hookId = "f00853f1-6627-447f-bacf-8dccf2e86fed";
        Hook hook = metricsAdvisorAdministrationClient.getHook(hookId);
        if (hook instanceof EmailHook) {
            EmailHook emailHook = (EmailHook) hook;
            System.out.printf("Hook Id: %s%n", emailHook.getId());
            System.out.printf("Hook Name: %s%n", emailHook.getName());
            System.out.printf("Hook Description: %s%n", emailHook.getDescription());
            System.out.printf("Hook External Link: %s%n", emailHook.getExternalLink());
            System.out.printf("Hook Emails: %s%n", String.join(",", emailHook.getEmailsToAlert()));
        } else if (hook instanceof WebHook) {
            WebHook webHook = (WebHook) hook;
            System.out.printf("Hook Id: %s%n", webHook.getId());
            System.out.printf("Hook Name: %s%n", webHook.getName());
            System.out.printf("Hook Description: %s%n", webHook.getDescription());
            System.out.printf("Hook External Link: %s%n", webHook.getExternalLink());
            System.out.printf("Hook Endpoint: %s%n", webHook.getEndpoint());
            System.out.printf("Hook Headers: %s%n", webHook.getHttpHeaders());
        }
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getHook#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#getHookWithResponse(String)}.
     */
    public void getHookWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getHookWithResponse#String-Context
        final String hookId = "f00853f1-6627-447f-bacf-8dccf2e86fed";
        Response<Hook> response = metricsAdvisorAdministrationClient.getHookWithResponse(hookId, Context.NONE);
        System.out.printf("Response statusCode: %d%n", response.getStatusCode());
        Hook hook = response.getValue();
        if (hook instanceof EmailHook) {
            EmailHook emailHook = (EmailHook) hook;
            System.out.printf("Hook Id: %s%n", emailHook.getId());
            System.out.printf("Hook Name: %s%n", emailHook.getName());
            System.out.printf("Hook Description: %s%n", emailHook.getDescription());
            System.out.printf("Hook External Link: %s%n", emailHook.getExternalLink());
            System.out.printf("Hook Emails: %s%n", String.join(",", emailHook.getEmailsToAlert()));
        } else if (hook instanceof WebHook) {
            WebHook webHook = (WebHook) hook;
            System.out.printf("Hook Id: %s%n", webHook.getId());
            System.out.printf("Hook Name: %s%n", webHook.getName());
            System.out.printf("Hook Description: %s%n", webHook.getDescription());
            System.out.printf("Hook External Link: %s%n", webHook.getExternalLink());
            System.out.printf("Hook Endpoint: %s%n", webHook.getEndpoint());
            System.out.printf("Hook Headers: %s%n", webHook.getHttpHeaders());
        }
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getHookWithResponse#String-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#updateHook(Hook)}.
     */
    public void updateHook() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateHook#Hook
        final String emailHookId = "f00853f1-6627-447f-bacf-8dccf2e86fed";
        Hook hook = metricsAdvisorAdministrationClient.getHook(emailHookId);
        EmailHook emailHook = (EmailHook) hook;
        emailHook
            .removeEmailToAlert("alertme@alertme.com")
            .addEmailToAlert("alertme2@alertme.com")
            .addEmailToAlert("alertme3@alertme.com");
        Hook updatedHook = metricsAdvisorAdministrationClient.updateHook(emailHook);
        EmailHook updatedEmailHook = (EmailHook) updatedHook;
        System.out.printf("Hook Id: %s%n", updatedEmailHook.getId());
        System.out.printf("Hook Name: %s%n", updatedEmailHook.getName());
        System.out.printf("Hook Description: %s%n", updatedEmailHook.getDescription());
        System.out.printf("Hook External Link: %s%n", updatedEmailHook.getExternalLink());
        System.out.printf("Hook Emails: %s%n", String.join(",", updatedEmailHook.getEmailsToAlert()));
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateHook#Hook
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#updateHookWithResponse(Hook)}.
     */
    public void updateHookWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateHookWithResponse#Hook-Context
        final String emailHookId = "f00853f1-6627-447f-bacf-8dccf2e86fed";
        Response<Hook> response
            = metricsAdvisorAdministrationClient.getHookWithResponse(emailHookId, Context.NONE);
        EmailHook emailHook = (EmailHook) response.getValue();
        emailHook
            .removeEmailToAlert("alertme@alertme.com")
            .addEmailToAlert("alertme2@alertme.com")
            .addEmailToAlert("alertme3@alertme.com");
        Response<Hook> updateResponse
            = metricsAdvisorAdministrationClient.updateHookWithResponse(emailHook, Context.NONE);
        EmailHook updatedEmailHook = (EmailHook) updateResponse.getValue();
        System.out.printf("Hook Id: %s%n", updatedEmailHook.getId());
        System.out.printf("Hook Name: %s%n", updatedEmailHook.getName());
        System.out.printf("Hook Description: %s%n", updatedEmailHook.getDescription());
        System.out.printf("Hook External Link: %s%n", updatedEmailHook.getExternalLink());
        System.out.printf("Hook Emails: %s%n", String.join(",", updatedEmailHook.getEmailsToAlert()));
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateHookWithResponse#Hook-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#deleteHook(String)}.
     */
    public void deleteHook() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteHook#String
        final String emailHookId = "f00853f1-6627-447f-bacf-8dccf2e86fed";
        metricsAdvisorAdministrationClient.deleteHook(emailHookId);
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteHook#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#deleteHookWithResponse(String)}.
     */
    public void deleteHookWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteHookWithResponse#String-Context
        final String emailHookId = "f00853f1-6627-447f-bacf-8dccf2e86fed";
        Response<Void> response
            = metricsAdvisorAdministrationClient.deleteHookWithResponse(emailHookId, Context.NONE);
        System.out.printf("Response statusCode: %d%n", response.getStatusCode());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteHookWithResponse#String-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#listHooks()}.
     */
    public void listHooks() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listHooks
        PagedIterable<Hook> hooks = metricsAdvisorAdministrationClient.listHooks();
        for (Hook hook : hooks) {
            if (hook instanceof EmailHook) {
                EmailHook emailHook = (EmailHook) hook;
                System.out.printf("Hook Id: %s%n", emailHook.getId());
                System.out.printf("Hook Name: %s%n", emailHook.getName());
                System.out.printf("Hook Description: %s%n", emailHook.getDescription());
                System.out.printf("Hook External Link: %s%n", emailHook.getExternalLink());
                System.out.printf("Hook Emails: %s%n", String.join(",", emailHook.getEmailsToAlert()));
            } else if (hook instanceof WebHook) {
                WebHook webHook = (WebHook) hook;
                System.out.printf("Hook Id: %s%n", webHook.getId());
                System.out.printf("Hook Name: %s%n", webHook.getName());
                System.out.printf("Hook Description: %s%n", webHook.getDescription());
                System.out.printf("Hook External Link: %s%n", webHook.getExternalLink());
                System.out.printf("Hook Endpoint: %s%n", webHook.getEndpoint());
                System.out.printf("Hook Headers: %s%n", webHook.getHttpHeaders());
            }
        }
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listHooks
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#listHooks(ListHookOptions)}.
     */
    public void listHooksWithOptions() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listHooks#ListHookOptions-Context
        ListHookOptions options = new ListHookOptions()
            .setSkip(100)
            .setTop(20);
        PagedIterable<Hook> hooks = metricsAdvisorAdministrationClient.listHooks(options, Context.NONE);
        Stream<PagedResponse<Hook>> hooksPageStream = hooks.streamByPage();
        int[] pageCount = new int[1];
        hooksPageStream.forEach(hookPage -> {
            System.out.printf("Page: %d%n", pageCount[0]++);
            for (Hook hook : hookPage.getElements()) {
                if (hook instanceof EmailHook) {
                    EmailHook emailHook = (EmailHook) hook;
                    System.out.printf("Hook Id: %s%n", emailHook.getId());
                    System.out.printf("Hook Name: %s%n", emailHook.getName());
                    System.out.printf("Hook Description: %s%n", emailHook.getDescription());
                    System.out.printf("Hook External Link: %s%n", emailHook.getExternalLink());
                    System.out.printf("Hook Emails: %s%n", String.join(",", emailHook.getEmailsToAlert()));
                    System.out.printf("Hook Admins: %s%n", String.join(",", emailHook.getAdmins()));
                } else if (hook instanceof WebHook) {
                    WebHook webHook = (WebHook) hook;
                    System.out.printf("Hook Id: %s%n", webHook.getId());
                    System.out.printf("Hook Name: %s%n", webHook.getName());
                    System.out.printf("Hook Description: %s%n", webHook.getDescription());
                    System.out.printf("Hook External Link: %s%n", webHook.getExternalLink());
                    System.out.printf("Hook Endpoint: %s%n", webHook.getEndpoint());
                    System.out.printf("Hook Headers: %s%n", webHook.getHttpHeaders());
                    System.out.printf("Hook Admins: %s%n", String.join(",", webHook.getAdmins()));
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
            = metricsAdvisorAdministrationClient.listDataFeedIngestionStatus(dataFeedId, options);

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
            = metricsAdvisorAdministrationClient.listDataFeedIngestionStatus(dataFeedId, options, Context.NONE);
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
        metricsAdvisorAdministrationClient.refreshDataFeedIngestion(dataFeedId,
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
        Response<Void> response = metricsAdvisorAdministrationClient.refreshDataFeedIngestionWithResponse(dataFeedId,
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
            = metricsAdvisorAdministrationClient.getDataFeedIngestionProgress(dataFeedId);
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
            = metricsAdvisorAdministrationClient.getDataFeedIngestionProgressWithResponse(dataFeedId, Context.NONE);
        System.out.printf("Response statusCode: %d%n", response.getStatusCode());
        DataFeedIngestionProgress ingestionProgress = response.getValue();
        System.out.printf("Latest active timestamp: %s%n", ingestionProgress.getLatestActiveTimestamp());
        System.out.printf("Latest successful timestamp: %s%n", ingestionProgress.getLatestSuccessTimestamp());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDataFeedIngestionProgressWithResponse#String-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#createMetricAnomalyDetectionConfiguration(String, AnomalyDetectionConfiguration)}.
     */
    public void createDetectionConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createMetricAnomalyDetectionConfiguration#String-AnomalyDetectionConfiguration
        final MetricWholeSeriesDetectionCondition wholeSeriesCondition = new MetricWholeSeriesDetectionCondition()
            .setCrossConditionOperator(DetectionConditionsOperator.OR)
            .setSmartDetectionCondition(new SmartDetectionCondition()
                .setSensitivity(50)
                .setAnomalyDetectorDirection(AnomalyDetectorDirection.BOTH)
                .setSuppressCondition(new SuppressCondition().setMinNumber(50).setMinRatio(50)))
            .setHardThresholdCondition(new HardThresholdCondition()
                .setLowerBound(0.0)
                .setUpperBound(100.0)
                .setAnomalyDetectorDirection(AnomalyDetectorDirection.BOTH)
                .setSuppressCondition(new SuppressCondition().setMinNumber(5).setMinRatio(5)))
            .setChangeThresholdCondition(new ChangeThresholdCondition()
                .setChangePercentage(50)
                .setShiftPoint(30)
                .setWithinRange(true)
                .setAnomalyDetectorDirection(AnomalyDetectorDirection.BOTH)
                .setSuppressCondition(new SuppressCondition().setMinNumber(2).setMinRatio(2)));

        final String detectionConfigName = "my_detection_config";
        final String detectionConfigDescription = "anomaly detection config for metric";
        final AnomalyDetectionConfiguration detectionConfig
            = new AnomalyDetectionConfiguration(detectionConfigName)
            .setDescription(detectionConfigDescription)
            .setWholeSeriesDetectionCondition(wholeSeriesCondition);

        final String metricId = "0b836da8-10e6-46cd-8f4f-28262e113a62";
        AnomalyDetectionConfiguration createdDetectionConfig = metricsAdvisorAdministrationClient
            .createMetricAnomalyDetectionConfiguration(metricId, detectionConfig);
        System.out.printf("Detection config Id: %s%n", createdDetectionConfig.getId());
        System.out.printf("Name: %s%n", createdDetectionConfig.getName());
        System.out.printf("Description: %s%n", createdDetectionConfig.getDescription());
        System.out.printf("MetricId: %s%n", createdDetectionConfig.getMetricId());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createMetricAnomalyDetectionConfiguration#String-AnomalyDetectionConfiguration
    }


    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#createMetricAnomalyDetectionConfigurationWithResponse(String, AnomalyDetectionConfiguration, Context)}.
     */
    public void createDetectionConfigurationWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createMetricAnomalyDetectionConfigurationWithResponse#String-AnomalyDetectionConfiguration-Context
        final MetricWholeSeriesDetectionCondition wholeSeriesCondition = new MetricWholeSeriesDetectionCondition()
            .setCrossConditionOperator(DetectionConditionsOperator.OR)
            .setSmartDetectionCondition(new SmartDetectionCondition()
                .setSensitivity(50)
                .setAnomalyDetectorDirection(AnomalyDetectorDirection.BOTH)
                .setSuppressCondition(new SuppressCondition().setMinNumber(50).setMinRatio(50)))
            .setHardThresholdCondition(new HardThresholdCondition()
                .setLowerBound(0.0)
                .setUpperBound(100.0)
                .setAnomalyDetectorDirection(AnomalyDetectorDirection.BOTH)
                .setSuppressCondition(new SuppressCondition().setMinNumber(5).setMinRatio(5)))
            .setChangeThresholdCondition(new ChangeThresholdCondition()
                .setChangePercentage(50)
                .setShiftPoint(30)
                .setWithinRange(true)
                .setAnomalyDetectorDirection(AnomalyDetectorDirection.BOTH)
                .setSuppressCondition(new SuppressCondition().setMinNumber(2).setMinRatio(2)));

        final String detectionConfigName = "my_detection_config";
        final String detectionConfigDescription = "anomaly detection config for metric";
        final AnomalyDetectionConfiguration detectionConfig
            = new AnomalyDetectionConfiguration(detectionConfigName)
            .setDescription(detectionConfigDescription)
            .setWholeSeriesDetectionCondition(wholeSeriesCondition);

        final String metricId = "0b836da8-10e6-46cd-8f4f-28262e113a62";
        Response<AnomalyDetectionConfiguration> response = metricsAdvisorAdministrationClient
            .createMetricAnomalyDetectionConfigurationWithResponse(metricId, detectionConfig, Context.NONE);
        System.out.printf("Response statusCode: %d%n", response.getStatusCode());
        AnomalyDetectionConfiguration createdDetectionConfig = response.getValue();
        System.out.printf("Detection config Id: %s%n", createdDetectionConfig.getId());
        System.out.printf("Name: %s%n", createdDetectionConfig.getName());
        System.out.printf("Description: %s%n", createdDetectionConfig.getDescription());
        System.out.printf("MetricId: %s%n", createdDetectionConfig.getMetricId());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createMetricAnomalyDetectionConfigurationWithResponse#String-AnomalyDetectionConfiguration-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#getMetricAnomalyDetectionConfiguration(String)}.
     */
    public void getDetectionConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getMetricAnomalyDetectionConfiguration#String
        final String detectionConfigId = "7b8069a1-1564-46da-9f50-b5d0dd9129ab";
        AnomalyDetectionConfiguration detectionConfig = metricsAdvisorAdministrationClient
            .getMetricAnomalyDetectionConfiguration(detectionConfigId);
        System.out.printf("Detection config Id: %s%n", detectionConfig.getId());
        System.out.printf("Name: %s%n", detectionConfig.getName());
        System.out.printf("Description: %s%n", detectionConfig.getDescription());
        System.out.printf("MetricId: %s%n", detectionConfig.getMetricId());

        System.out.printf("Detection conditions specified for configuration...%n");

        System.out.printf("Whole Series Detection Conditions:%n");
        MetricWholeSeriesDetectionCondition wholeSeriesDetectionCondition
            = detectionConfig.getWholeSeriesDetectionCondition();

        System.out.printf("- Use %s operator for multiple detection conditions:%n",
            wholeSeriesDetectionCondition.getCrossConditionsOperator());

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
                = Arrays.toString(seriesKey.asMap().entrySet().stream().toArray());
            System.out.printf("- Series Key:%n", seriesKeyStr);
            System.out.printf(" - Use %s operator for multiple detection conditions:%n",
                seriesDetectionCondition.getCrossConditionsOperator());

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
                = Arrays.toString(seriesGroupKey.asMap().entrySet().stream().toArray());
            System.out.printf("- Series Group Key:%n", seriesGroupKeyStr);
            System.out.printf(" - Use %s operator for multiple detection conditions:%n",
                seriesGroupDetectionCondition.getCrossConditionsOperator());

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
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getMetricAnomalyDetectionConfiguration#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#getMetricAnomalyDetectionConfigurationWithResponse(String, Context)}.
     */
    public void getDetectionConfigurationWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getMetricAnomalyDetectionConfigurationWithResponse#String-Context
        final String detectionConfigId = "7b8069a1-1564-46da-9f50-b5d0dd9129ab";
        Response<AnomalyDetectionConfiguration> response = metricsAdvisorAdministrationClient
            .getMetricAnomalyDetectionConfigurationWithResponse(detectionConfigId, Context.NONE);
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
            wholeSeriesDetectionCondition.getCrossConditionsOperator());

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
                = Arrays.toString(seriesKey.asMap().entrySet().stream().toArray());
            System.out.printf("- Series Key:%n", seriesKeyStr);
            System.out.printf(" - Use %s operator for multiple detection conditions:%n",
                seriesDetectionCondition.getCrossConditionsOperator());

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
                = Arrays.toString(seriesGroupKey.asMap().entrySet().stream().toArray());
            System.out.printf("- Series Group Key:%n", seriesGroupKeyStr);
            System.out.printf(" - Use %s operator for multiple detection conditions:%n",
                seriesGroupDetectionCondition.getCrossConditionsOperator());

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
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getMetricAnomalyDetectionConfigurationWithResponse#String-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#listMetricAnomalyDetectionConfigurations(String)}.
     */
    public void listDetectionConfigurations() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listMetricAnomalyDetectionConfigurations#String
        final String metricId = "0b836da8-10e6-46cd-8f4f-28262e113a62";
        PagedIterable<AnomalyDetectionConfiguration> configsIterable
            = metricsAdvisorAdministrationClient.listMetricAnomalyDetectionConfigurations(metricId);

        for (AnomalyDetectionConfiguration detectionConfig : configsIterable) {
            System.out.printf("Detection config Id: %s%n", detectionConfig.getId());
            System.out.printf("Name: %s%n", detectionConfig.getName());
            System.out.printf("Description: %s%n", detectionConfig.getDescription());
            System.out.printf("MetricId: %s%n", detectionConfig.getMetricId());
        }
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listMetricAnomalyDetectionConfigurations#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#listMetricAnomalyDetectionConfigurations(String, Context)}.
     */
    public void listDetectionConfigurationsWithContext() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listMetricAnomalyDetectionConfigurations#String-Context
        final String metricId = "0b836da8-10e6-46cd-8f4f-28262e113a62";
        PagedIterable<AnomalyDetectionConfiguration> configsIterable
            = metricsAdvisorAdministrationClient.listMetricAnomalyDetectionConfigurations(metricId, Context.NONE);

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
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listMetricAnomalyDetectionConfigurations#String-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#updateMetricAnomalyDetectionConfiguration(AnomalyDetectionConfiguration)}.
     */
    public void updateDetectionConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateMetricAnomalyDetectionConfiguration#AnomalyDetectionConfiguration
        final String detectionConfigId = "7b8069a1-1564-46da-9f50-b5d0dd9129ab";
        AnomalyDetectionConfiguration detectionConfig = metricsAdvisorAdministrationClient
            .getMetricAnomalyDetectionConfiguration(detectionConfigId);

        detectionConfig.setName("updated config name");
        detectionConfig.setDescription("updated with more detection conditions");
        DimensionKey seriesGroupKey = new DimensionKey()
            .put("city", "Seoul");
        detectionConfig.addSeriesGroupDetectionCondition(
            new MetricSeriesGroupDetectionCondition(seriesGroupKey)
                .setSmartDetectionCondition(new SmartDetectionCondition()
                    .setSensitivity(10.0)
                    .setAnomalyDetectorDirection(AnomalyDetectorDirection.UP)
                    .setSuppressCondition(new SuppressCondition().setMinNumber(2).setMinRatio(2))));

        AnomalyDetectionConfiguration updatedDetectionConfig = metricsAdvisorAdministrationClient
            .updateMetricAnomalyDetectionConfiguration(detectionConfig);

        System.out.printf("Detection config Id: %s%n", updatedDetectionConfig.getId());
        System.out.printf("Name: %s%n", updatedDetectionConfig.getName());
        System.out.printf("Description: %s%n", updatedDetectionConfig.getDescription());
        System.out.printf("MetricId: %s%n", updatedDetectionConfig.getMetricId());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateMetricAnomalyDetectionConfiguration#AnomalyDetectionConfiguration
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#updateMetricAnomalyDetectionConfigurationWithResponse(AnomalyDetectionConfiguration, Context)}
     */
    public void updateDetectionConfigurationWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateMetricAnomalyDetectionConfigurationWithResponse#AnomalyDetectionConfiguration-Context
        final String detectionConfigId = "7b8069a1-1564-46da-9f50-b5d0dd9129ab";
        Response<AnomalyDetectionConfiguration> getResponse = metricsAdvisorAdministrationClient
            .getMetricAnomalyDetectionConfigurationWithResponse(detectionConfigId, Context.NONE);
        AnomalyDetectionConfiguration detectionConfig = getResponse.getValue();
        detectionConfig.setName("updated config name");
        detectionConfig.setDescription("updated with more detection conditions");
        DimensionKey seriesGroupKey = new DimensionKey()
            .put("city", "Seoul");
        detectionConfig.addSeriesGroupDetectionCondition(
            new MetricSeriesGroupDetectionCondition(seriesGroupKey)
                .setSmartDetectionCondition(new SmartDetectionCondition()
                    .setSensitivity(10.0)
                    .setAnomalyDetectorDirection(AnomalyDetectorDirection.UP)
                    .setSuppressCondition(new SuppressCondition().setMinNumber(2).setMinRatio(2))));

        Response<AnomalyDetectionConfiguration> updateResponse = metricsAdvisorAdministrationClient
            .updateMetricAnomalyDetectionConfigurationWithResponse(detectionConfig, Context.NONE);

        System.out.printf("Response StatusCode: %s%n", updateResponse.getStatusCode());
        AnomalyDetectionConfiguration updatedDetectionConfig = updateResponse.getValue();
        System.out.printf("Detection config Id: %s%n", updatedDetectionConfig.getId());
        System.out.printf("Name: %s%n", updatedDetectionConfig.getName());
        System.out.printf("Description: %s%n", updatedDetectionConfig.getDescription());
        System.out.printf("MetricId: %s%n", updatedDetectionConfig.getMetricId());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateMetricAnomalyDetectionConfigurationWithResponse#AnomalyDetectionConfiguration-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#deleteMetricAnomalyDetectionConfiguration(String)}.
     */
    public void deleteDetectionConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteMetricAnomalyDetectionConfiguration#String
        final String detectionConfigId = "7b8069a1-1564-46da-9f50-b5d0dd9129ab";
        metricsAdvisorAdministrationClient
            .deleteMetricAnomalyDetectionConfiguration(detectionConfigId);
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteMetricAnomalyDetectionConfiguration#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#deleteMetricAnomalyDetectionConfigurationWithResponse(String, Context)}.
     */
    public void deleteDetectionConfigurationWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteMetricAnomalyDetectionConfigurationWithResponse#String-Context
        final String detectionConfigId = "7b8069a1-1564-46da-9f50-b5d0dd9129ab";
        Response<Void> response = metricsAdvisorAdministrationClient
            .deleteMetricAnomalyDetectionConfigurationWithResponse(detectionConfigId, Context.NONE);
        System.out.printf("Response Status Code: %s%n", response.getStatusCode());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteMetricAnomalyDetectionConfigurationWithResponse#String-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#createAnomalyAlertConfiguration(AnomalyAlertConfiguration)}
     */
    public void createAnomalyAlertConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createAnomalyAlertConfiguration#AnomalyAlertConfiguration
        String detectionConfigurationId1 = "9ol48er30-6e6e-4391-b78f-b00dfee1e6f5";
        String detectionConfigurationId2 = "3e58er30-6e6e-4391-b78f-b00dfee1e6f5";
        String hookId1 = "5f48er30-6e6e-4391-b78f-b00dfee1e6f5";
        String hookId2 = "8i48er30-6e6e-4391-b78f-b00dfee1e6f5";

        final AnomalyAlertConfiguration anomalyAlertConfiguration
            = metricsAdvisorAdministrationClient.createAnomalyAlertConfiguration(
            new AnomalyAlertConfiguration("My Alert config name")
                .setDescription("alert config description")
                .setMetricAlertConfigurations(Arrays.asList(
                    new MetricAnomalyAlertConfiguration(detectionConfigurationId1,
                        MetricAnomalyAlertScope.forWholeSeries()),
                    new MetricAnomalyAlertConfiguration(detectionConfigurationId2,
                        MetricAnomalyAlertScope.forWholeSeries())
                        .setAlertConditions(new MetricAnomalyAlertConditions()
                            .setSeverityCondition(new SeverityCondition().setMaxAlertSeverity(Severity.HIGH)))))
                .setCrossMetricsOperator(MetricAnomalyAlertConfigurationsOperator.AND)
                .setIdOfHooksToAlert(Arrays.asList(hookId1, hookId2)));

        System.out.printf("Anomaly alert configuration Id: %s%n", anomalyAlertConfiguration.getId());
        System.out.printf("Anomaly alert configuration description: %s%n", anomalyAlertConfiguration.getDescription());
        System.out.printf("Anomaly alert configuration hook ids: %.2f%n",
            anomalyAlertConfiguration.getIdOfHooksToAlert());
        System.out.printf("Anomaly alert configuration cross metrics operator: %s%n",
            anomalyAlertConfiguration.getCrossMetricsOperator().toString());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createAnomalyAlertConfiguration#AnomalyAlertConfiguration
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#createAnomalyAlertConfigurationWithResponse(AnomalyAlertConfiguration, Context)}
     */
    public void createAnomalyAlertConfigurationWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createAnomalyAlertConfigurationWithResponse#AnomalyAlertConfiguration-Context

        String detectionConfigurationId1 = "9ol48er30-6e6e-4391-b78f-b00dfee1e6f5";
        String detectionConfigurationId2 = "3e58er30-6e6e-4391-b78f-b00dfee1e6f5";
        String hookId1 = "5f48er30-6e6e-4391-b78f-b00dfee1e6f5";
        String hookId2 = "8i48er30-6e6e-4391-b78f-b00dfee1e6f5";

        final Response<AnomalyAlertConfiguration> alertConfigurationResponse
            = metricsAdvisorAdministrationClient.createAnomalyAlertConfigurationWithResponse(
            new AnomalyAlertConfiguration("My Alert config name")
                .setDescription("alert config description")
                .setMetricAlertConfigurations(Arrays.asList(
                    new MetricAnomalyAlertConfiguration(detectionConfigurationId1,
                        MetricAnomalyAlertScope.forWholeSeries()),
                    new MetricAnomalyAlertConfiguration(detectionConfigurationId2,
                        MetricAnomalyAlertScope.forWholeSeries())
                        .setAlertConditions(new MetricAnomalyAlertConditions()
                            .setSeverityCondition(new SeverityCondition().setMaxAlertSeverity(Severity.HIGH)))))
                .setCrossMetricsOperator(MetricAnomalyAlertConfigurationsOperator.AND)
                .setIdOfHooksToAlert(Arrays.asList(hookId1, hookId2)), Context.NONE);

        System.out.printf("Anomaly alert creation operation status: %s%n", alertConfigurationResponse.getStatusCode());
        final AnomalyAlertConfiguration anomalyAlertConfiguration = alertConfigurationResponse.getValue();
        System.out.printf("Anomaly alert configuration Id: %s%n", anomalyAlertConfiguration.getId());
        System.out.printf("Anomaly alert configuration description: %s%n", anomalyAlertConfiguration.getDescription());
        System.out.printf("Anomaly alert configuration hook ids: %.2f%n",
            anomalyAlertConfiguration.getIdOfHooksToAlert());
        System.out.printf("Anomaly alert configuration cross metrics operator: %s%n",
            anomalyAlertConfiguration.getCrossMetricsOperator().toString());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createAnomalyAlertConfigurationWithResponse#AnomalyAlertConfiguration-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#getAnomalyAlertConfiguration(String)}
     */
    public void getAnomalyAlertConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getAnomalyAlertConfiguration#String
        String alertConfigId = "1p0f8er30-6e6e-4391-b78f-bpfdfee1e6f5";

        AnomalyAlertConfiguration anomalyAlertConfiguration
            = metricsAdvisorAdministrationClient.getAnomalyAlertConfiguration(alertConfigId);
        System.out.printf("Anomaly alert configuration Id: %s%n", anomalyAlertConfiguration.getId());
        System.out.printf("Anomaly alert configuration description: %s%n", anomalyAlertConfiguration.getDescription());
        System.out.printf("Anomaly alert configuration hook ids: %.2f%n",
            anomalyAlertConfiguration.getIdOfHooksToAlert());
        System.out.printf("Anomaly alert configuration cross metrics operator: %s%n",
            anomalyAlertConfiguration.getCrossMetricsOperator().toString());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getAnomalyAlertConfiguration#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#getAnomalyAlertConfigurationWithResponse(String, Context)}
     */
    public void getAnomalyAlertConfigurationWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getAnomalyAlertConfigurationWithResponse#String-Context
        String alertConfigId = "1p0f8er30-6e6e-4391-b78f-bpfdfee1e6f5";

        Response<AnomalyAlertConfiguration> alertConfigurationResponse
            = metricsAdvisorAdministrationClient.getAnomalyAlertConfigurationWithResponse(alertConfigId, Context.NONE);

        System.out.printf("Anomaly alert creation operation status: %s%n", alertConfigurationResponse.getStatusCode());
        final AnomalyAlertConfiguration anomalyAlertConfiguration = alertConfigurationResponse.getValue();
        System.out.printf("Anomaly alert configuration Id: %s%n", anomalyAlertConfiguration.getId());
        System.out.printf("Anomaly alert configuration description: %s%n", anomalyAlertConfiguration.getDescription());
        System.out.printf("Anomaly alert configuration hook ids: %.2f%n",
            anomalyAlertConfiguration.getIdOfHooksToAlert());
        System.out.printf("Anomaly alert configuration cross metrics operator: %s%n",
            anomalyAlertConfiguration.getCrossMetricsOperator().toString());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getAnomalyAlertConfigurationWithResponse#String-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#updateAnomalyAlertConfiguration(AnomalyAlertConfiguration)}
     */
    public void updateAnomalyAlertConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateAnomalyAlertConfiguration#AnomalyAlertConfiguration

        String alertConfigId = "1p0f8er30-6e6e-4391-b78f-bpfdfee1e6f5";
        String additionalHookId = "2gh8er30-6e6e-4391-b78f-bpfdfee1e6f5";

        AnomalyAlertConfiguration existingAnomalyConfig
            = metricsAdvisorAdministrationClient.getAnomalyAlertConfiguration(alertConfigId);
        final AnomalyAlertConfiguration updatAnomalyAlertConfiguration
            = metricsAdvisorAdministrationClient.updateAnomalyAlertConfiguration(
            existingAnomalyConfig
                .addIdOfHookToAlert(additionalHookId)
                .setDescription("updated to add more hook ids")
        );

        System.out.printf("Updated anomaly alert configuration Id: %s%n", updatAnomalyAlertConfiguration.getId());
        System.out.printf("Updated anomaly alert configuration description: %s%n",
            updatAnomalyAlertConfiguration.getDescription());
        System.out.printf("Updated anomaly alert configuration hook ids: %.2f%n",
            updatAnomalyAlertConfiguration.getIdOfHooksToAlert());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateAnomalyAlertConfiguration#AnomalyAlertConfiguration
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#updateAnomalyAlertConfigurationWithResponse(AnomalyAlertConfiguration, Context)}
     */
    public void updateAnomalyAlertConfigurationWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateAnomalyAlertConfigurationWithResponse#AnomalyAlertConfiguration-Context

        String alertConfigId = "1p0f8er30-6e6e-4391-b78f-bpfdfee1e6f5";
        String additionalHookId = "2gh8er30-6e6e-4391-b78f-bpfdfee1e6f5";

        AnomalyAlertConfiguration existingAnomalyConfig
            = metricsAdvisorAdministrationClient.getAnomalyAlertConfiguration(alertConfigId);
        final Response<AnomalyAlertConfiguration> alertConfigurationResponse
            = metricsAdvisorAdministrationClient.updateAnomalyAlertConfigurationWithResponse(
            existingAnomalyConfig
                .addIdOfHookToAlert(additionalHookId)
                .setDescription("updated to add more hook ids"), Context.NONE);

        System.out.printf("Update anomaly alert operation status: %s%n", alertConfigurationResponse.getStatusCode());
        final AnomalyAlertConfiguration updatAnomalyAlertConfiguration = alertConfigurationResponse.getValue();
        System.out.printf("Updated anomaly alert configuration Id: %s%n", updatAnomalyAlertConfiguration.getId());
        System.out.printf("Updated anomaly alert configuration description: %s%n",
            updatAnomalyAlertConfiguration.getDescription());
        System.out.printf("Updated anomaly alert configuration hook ids: %sf%n",
            updatAnomalyAlertConfiguration.getIdOfHooksToAlert());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateAnomalyAlertConfigurationWithResponse#AnomalyAlertConfiguration-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#deleteAnomalyAlertConfiguration(String)}.
     */
    public void deleteAnomalyAlertConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteAnomalyAlertConfiguration#String
        String alertConfigId = "1p0f8er30-6e6e-4391-b78f-bpfdfee1e6f5";
        metricsAdvisorAdministrationClient.deleteAnomalyAlertConfiguration(alertConfigId);
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteAnomalyAlertConfiguration#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#deleteAnomalyAlertConfigurationWithResponse(String, Context)}
     */
    public void deleteAnomalyAlertConfigurationWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteAnomalyAlertConfigurationWithResponse#String-Context
        String alertConfigId = "1p0f8er30-6e6e-4391-b78f-bpfdfee1e6f5";
        final Response<Void> response =
            metricsAdvisorAdministrationClient.deleteAnomalyAlertConfigurationWithResponse(alertConfigId, Context.NONE);

        System.out.printf("Anomaly alert config delete operation status : %s%n", response.getStatusCode());
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteAnomalyAlertConfigurationWithResponse#String-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#listAnomalyAlertConfigurations(String)}.
     */
    public void listAnomalyAlertConfigurations() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listAnomalyAlertConfigurations#String
        String detectionConfigId = "3rt98er30-6e6e-4391-b78f-bpfdfee1e6f5";
        metricsAdvisorAdministrationClient.listAnomalyAlertConfigurations(detectionConfigId)
            .forEach(anomalyAlertConfiguration -> {
                System.out.printf("Anomaly alert configuration Id: %s%n", anomalyAlertConfiguration.getId());
                System.out.printf("Anomaly alert configuration description: %s%n",
                    anomalyAlertConfiguration.getDescription());
                System.out.printf("Anomaly alert configuration hook ids: %.2f%n",
                    anomalyAlertConfiguration.getIdOfHooksToAlert());
                System.out.printf("Anomaly alert configuration cross metrics operator: %s%n",
                    anomalyAlertConfiguration.getCrossMetricsOperator().toString());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listAnomalyAlertConfigurations#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationClient#listAnomalyAlertConfigurations(String, Context)}.
     */
    public void listAnomalyAlertConfigurationsWithContext() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listAnomalyAlertConfigurations#String-Context
        String detectionConfigId = "3rt98er30-6e6e-4391-b78f-bpfdfee1e6f5";
        metricsAdvisorAdministrationClient.listAnomalyAlertConfigurations(detectionConfigId, Context.NONE)
            .forEach(anomalyAlertConfiguration -> {
                System.out.printf("Anomaly alert configuration Id: %s%n", anomalyAlertConfiguration.getId());
                System.out.printf("Anomaly alert configuration description: %s%n",
                    anomalyAlertConfiguration.getDescription());
                System.out.printf("Anomaly alert configuration hook ids: %.2f%n",
                    anomalyAlertConfiguration.getIdOfHooksToAlert());
                System.out.printf("Anomaly alert configuration cross metrics operator: %s%n",
                    anomalyAlertConfiguration.getCrossMetricsOperator().toString());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listAnomalyAlertConfigurations#String-Context
    }
}
