// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration;

import com.azure.ai.metricsadvisor.models.AnomalyAlertConfiguration;
import com.azure.ai.metricsadvisor.models.AnomalyDetectionConfiguration;
import com.azure.ai.metricsadvisor.models.AnomalyDetectorDirection;
import com.azure.ai.metricsadvisor.models.ChangeThresholdCondition;
import com.azure.ai.metricsadvisor.models.DataFeed;
import com.azure.ai.metricsadvisor.models.DataFeedGranularity;
import com.azure.ai.metricsadvisor.models.DataFeedGranularityType;
import com.azure.ai.metricsadvisor.models.DataFeedIngestionProgress;
import com.azure.ai.metricsadvisor.models.DataFeedIngestionSettings;
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
import com.azure.ai.metricsadvisor.models.MetricAnomalyAlertConfiguration;
import com.azure.ai.metricsadvisor.models.MetricAnomalyAlertConfigurationsOperator;
import com.azure.ai.metricsadvisor.models.MetricAnomalyAlertScope;
import com.azure.ai.metricsadvisor.models.MetricSeriesGroupDetectionCondition;
import com.azure.ai.metricsadvisor.models.MetricSingleSeriesDetectionCondition;
import com.azure.ai.metricsadvisor.models.MetricWholeSeriesDetectionCondition;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import com.azure.ai.metricsadvisor.models.MySqlDataFeedSource;
import com.azure.ai.metricsadvisor.models.Severity;
import com.azure.ai.metricsadvisor.models.SeverityCondition;
import com.azure.ai.metricsadvisor.models.SmartDetectionCondition;
import com.azure.ai.metricsadvisor.models.SuppressCondition;
import com.azure.ai.metricsadvisor.models.WebHook;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.util.Context;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient}
 */
public class MetricsAdvisorAdministrationAsyncClientJavaDocCodeSnippets {
    MetricsAdvisorAdministrationAsyncClient metricAdvisorAdministrationAsyncClient =
        new MetricsAdvisorAdministrationClientBuilder().buildAsyncClient();

    /**
     * Code snippet for creating a {@link MetricsAdvisorAdministrationAsyncClient}
     */
    public void createMetricAdvisorAdministrationAsyncClient() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.instantiation
        MetricsAdvisorAdministrationAsyncClient metricAdvisorAdministrationAsyncClient =
            new MetricsAdvisorAdministrationClientBuilder()
                .credential(new MetricsAdvisorKeyCredential("{subscription_key}", "{api_key}"))
                .endpoint("{endpoint}")
                .buildAsyncClient();
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.instantiation
    }

    /**
     * Code snippet for creating a {@link MetricsAdvisorAdministrationAsyncClient} with pipeline
     */
    public void createMetricAdvisorAdministrationAsyncClientWithPipeline() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.pipeline.instantiation
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(/* add policies */)
            .build();

        MetricsAdvisorAdministrationAsyncClient metricAdvisorAdministrationAsyncClient =
            new MetricsAdvisorAdministrationClientBuilder()
                .credential(new MetricsAdvisorKeyCredential("{subscription_key}", "{api_key}"))
                .endpoint("{endpoint}")
                .pipeline(pipeline)
                .buildAsyncClient();
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.pipeline.instantiation
    }

    // Create Data Feed

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#createDataFeed(String,
     * DataFeedSource, DataFeedGranularity, DataFeedSchema, DataFeedIngestionSettings, DataFeedOptions)}
     */
    public void createDataFeed() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createDataFeed#String-DataFeedSource-DataFeedGranularity-DataFeedSchema-DataFeedIngestionSettings-DataFeedOptions

        metricAdvisorAdministrationAsyncClient.createDataFeed(
            "dataFeedName",
            new MySqlDataFeedSource("conn-string", "query"),
            new DataFeedGranularity().setGranularityType(DataFeedGranularityType.DAILY),
            new DataFeedSchema(Arrays.asList(new Metric().setName("metric1"), new Metric().setName("metric2"))),
            new DataFeedIngestionSettings(OffsetDateTime.parse("")),
            new DataFeedOptions().setDescription("data feed description")
                .setRollupSettings(new DataFeedRollupSettings().setRollupType(DataFeedRollupType.AUTO_ROLLUP)))
            .subscribe(dataFeed -> {
                System.out.printf("Data feed Id: %s%n", dataFeed.getId());
                System.out.printf("Data feed description: %s%n", dataFeed.getOptions().getDescription());
                System.out.printf("Data feed source type: %.2f%n", dataFeed.getSourceType());
                System.out.printf("Data feed creator: %.2f%n", dataFeed.getCreator());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createDataFeed#String-DataFeedSource-DataFeedGranularity-DataFeedSchema-DataFeedIngestionSettings-DataFeedOptions
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#createDataFeedWithResponse(String,
     * DataFeedSource, DataFeedGranularity, DataFeedSchema, DataFeedIngestionSettings, DataFeedOptions)}
     */
    public void createDataFeedWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createDataFeedWithResponse#String-DataFeedSource-DataFeedGranularity-DataFeedSchema-DataFeedIngestionSettings-DataFeedOptions

        metricAdvisorAdministrationAsyncClient.createDataFeedWithResponse(
            "dataFeedName",
            new MySqlDataFeedSource("conn-string", "query"),
            new DataFeedGranularity().setGranularityType(DataFeedGranularityType.DAILY),
            new DataFeedSchema(Arrays.asList(new Metric().setName("metric1"), new Metric().setName("metric2"))),
            new DataFeedIngestionSettings(OffsetDateTime.parse("")),
            new DataFeedOptions().setDescription("data feed description")
                .setRollupSettings(new DataFeedRollupSettings().setRollupType(DataFeedRollupType.AUTO_ROLLUP)))
            .subscribe(dataFeedResponse -> {
                System.out.printf("Data feed create operation status: %s%n", dataFeedResponse.getStatusCode());
                DataFeed dataFeed = dataFeedResponse.getValue();
                System.out.printf("Data feed Id: %s%n", dataFeed.getId());
                System.out.printf("Data feed description: %s%n", dataFeed.getOptions().getDescription());
                System.out.printf("Data feed source type: %.2f%n", dataFeed.getSourceType());
                System.out.printf("Data feed creator: %.2f%n", dataFeed.getCreator());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createDataFeedWithResponse#String-DataFeedSource-DataFeedGranularity-DataFeedSchema-DataFeedIngestionSettings-DataFeedOptions
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#getDataFeed(String)}
     */
    public void getDataFeed() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getDataFeed#String

        metricAdvisorAdministrationAsyncClient.getDataFeed("dataFeedId")
            .subscribe(dataFeed -> {
                System.out.printf("Data feed Id: %s%n", dataFeed.getId());
                System.out.printf("Data feed description: %s%n", dataFeed.getOptions().getDescription());
                System.out.printf("Data feed source type: %.2f%n", dataFeed.getSourceType());
                System.out.printf("Data feed creator: %.2f%n", dataFeed.getCreator());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getDataFeed#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#getDataFeedWithResponse(String)}
     */
    public void getDataFeedWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getDataFeedWithResponse#String

        metricAdvisorAdministrationAsyncClient.getDataFeedWithResponse("dataFeedId")
            .subscribe(dataFeedResponse -> {
                System.out.printf("Data feed create operation status: %s%n", dataFeedResponse.getStatusCode());
                DataFeed dataFeed = dataFeedResponse.getValue();
                System.out.printf("Data feed Id: %s%n", dataFeed.getId());
                System.out.printf("Data feed description: %s%n", dataFeed.getOptions().getDescription());
                System.out.printf("Data feed source type: %.2f%n", dataFeed.getSourceType());
                System.out.printf("Data feed creator: %.2f%n", dataFeed.getCreator());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getDataFeedWithResponse#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#updateDataFeed(DataFeed)}
     */
    public void updateDataFeed() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateDataFeed#DataFeed
        DataFeed existingDataFeed  = new DataFeed();
        metricAdvisorAdministrationAsyncClient.updateDataFeed(
            existingDataFeed.setOptions(new DataFeedOptions().setDescription("set updated description")))
            .subscribe(dataFeed -> {
                System.out.printf("Data feed Id: %s%n", dataFeed.getId());
                System.out.printf("Data feed updated description: %s%n", dataFeed.getOptions().getDescription());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateDataFeed#DataFeed
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#updateDataFeedWithResponse(DataFeed)}
     */
    public void updateDataFeedWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateDataFeedWithResponse#DataFeed
        DataFeed existingDataFeed  = new DataFeed();
        metricAdvisorAdministrationAsyncClient.updateDataFeedWithResponse(
            existingDataFeed.setOptions(new DataFeedOptions().setDescription("set updated description")))
            .subscribe(dataFeedResponse -> {
                System.out.printf("Data feed update operation status: %s%n", dataFeedResponse.getStatusCode());
                DataFeed dataFeed = dataFeedResponse.getValue();
                System.out.printf("Data feed Id: %s%n", dataFeed.getId());
                System.out.printf("Data feed updated description: %s%n", dataFeed.getOptions().getDescription());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateDataFeedWithResponse#DataFeed
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#deleteDataFeed(String)}.
     */
    public void deleteDataFeed() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteDataFeed#String
        metricAdvisorAdministrationAsyncClient.deleteDataFeed("dataFeedId");
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteDataFeed#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#deleteDataFeedWithResponse(String)}
     */
    public void deleteDataFeedWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteDataFeedWithResponse#String
        metricAdvisorAdministrationAsyncClient.deleteDataFeedWithResponse("dataFeedId")
            .subscribe(response -> {
                System.out.printf("Data feed delete operation status : %s%n", response.getStatusCode());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteDataFeedWithResponse#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#listDataFeeds()}
     */
    public void listDataFeeds() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listDataFeeds
        metricAdvisorAdministrationAsyncClient.listDataFeeds()
            .subscribe(dataFeed -> {
                System.out.printf("Data feed Id: %s%n", dataFeed.getId());
                System.out.printf("Data feed description: %s%n", dataFeed.getOptions().getDescription());
                System.out.printf("Data feed source type: %.2f%n", dataFeed.getSourceType());
                System.out.printf("Data feed creator: %.2f%n", dataFeed.getCreator());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listDataFeeds
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#listDataFeeds(ListDataFeedOptions)} with options.
     */
    public void listDataFeedWithOptions() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listDataFeeds#ListDataFeedOptions
        metricAdvisorAdministrationAsyncClient.listDataFeeds(
            new ListDataFeedOptions()
                .setListDataFeedFilter(
                    new ListDataFeedFilter()
                        .setDataFeedStatus(DataFeedStatus.ACTIVE)
                        .setDataFeedGranularityType(DataFeedGranularityType.DAILY))
                .setTop(3))
            .subscribe(dataFeed -> {
                System.out.printf("Data feed Id: %s%n", dataFeed.getId());
                System.out.printf("Data feed description: %s%n", dataFeed.getOptions().getDescription());
                System.out.printf("Data feed source type: %.2f%n", dataFeed.getSourceType());
                System.out.printf("Data feed creator: %.2f%n", dataFeed.getCreator());
                System.out.printf("Data feed status: %.2f%n", dataFeed.getStatus());
                System.out.printf("Data feed granularity type: %.2f%n", dataFeed.getGranularity().getGranularityType());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listDataFeeds#ListDataFeedOptions
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#createHook(Hook)}.
     */
    public void createHook() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createHook#Hook
        Hook emailHook = new EmailHook("email hook")
            .setDescription("my email hook")
            .addEmailToAlert("alertme@alertme.com")
            .setExternalLink("https://adwiki.azurewebsites.net/articles/howto/alerts/create-hooks.html");

        metricAdvisorAdministrationAsyncClient.createHook(emailHook)
            .subscribe(hook -> {
                EmailHook createdEmailHook = (EmailHook) hook;
                System.out.printf("Hook Id: %s%n", createdEmailHook.getId());
                System.out.printf("Hook Name: %s%n", createdEmailHook.getName());
                System.out.printf("Hook Description: %s%n", createdEmailHook.getDescription());
                System.out.printf("Hook External Link: %s%n", createdEmailHook.getExternalLink());
                System.out.printf("Hook Emails: %s%n", String.join(",", createdEmailHook.getEmailsToAlert()));
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createHook#Hook
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#createHookWithResponse(Hook)}.
     */
    public void createHookWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createHookWithResponse#Hook
        Hook emailHook = new EmailHook("email hook")
            .setDescription("my email hook")
            .addEmailToAlert("alertme@alertme.com")
            .setExternalLink("https://adwiki.azurewebsites.net/articles/howto/alerts/create-hooks.html");

        metricAdvisorAdministrationAsyncClient.createHookWithResponse(emailHook)
            .subscribe(response -> {
                System.out.printf("Response statusCode: %d%n", response.getStatusCode());
                EmailHook createdEmailHook = (EmailHook) response.getValue();
                System.out.printf("Hook Id: %s%n", createdEmailHook.getId());
                System.out.printf("Hook Name: %s%n", createdEmailHook.getName());
                System.out.printf("Hook Description: %s%n", createdEmailHook.getDescription());
                System.out.printf("Hook External Link: %s%n", createdEmailHook.getExternalLink());
                System.out.printf("Hook Emails: %s%n", String.join(",", createdEmailHook.getEmailsToAlert()));
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createHookWithResponse#Hook
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#getHook(String)}.
     */
    public void getHook() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getHook#String
        final String hookId = "f00853f1-6627-447f-bacf-8dccf2e86fed";
        metricAdvisorAdministrationAsyncClient.getHook(hookId)
            .subscribe(hook -> {
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
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getHook#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#getHookWithResponse(String)}.
     */
    public void getHookWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getHookWithResponse#String
        final String hookId = "f00853f1-6627-447f-bacf-8dccf2e86fed";
        metricAdvisorAdministrationAsyncClient.getHookWithResponse(hookId)
            .subscribe(response -> {
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
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getHookWithResponse#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#updateHook(Hook)}.
     */
    public void updateHook() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateHook#Hook
        final String emailHookId = "f00853f1-6627-447f-bacf-8dccf2e86fed";
        metricAdvisorAdministrationAsyncClient.getHook(emailHookId)
            .flatMap(hook -> {
                EmailHook emailHook = (EmailHook) hook;
                emailHook
                    .removeEmailToAlert("alertme@alertme.com")
                    .addEmailToAlert("alertme2@alertme.com")
                    .addEmailToAlert("alertme3@alertme.com");
                return metricAdvisorAdministrationAsyncClient.updateHook(emailHook);
            })
            .subscribe(hook -> {
                EmailHook emailHook = (EmailHook) hook;
                System.out.printf("Hook Id: %s%n", emailHook.getId());
                System.out.printf("Hook Name: %s%n", emailHook.getName());
                System.out.printf("Hook Description: %s%n", emailHook.getDescription());
                System.out.printf("Hook External Link: %s%n", emailHook.getExternalLink());
                System.out.printf("Hook Emails: %s%n", String.join(",", emailHook.getEmailsToAlert()));
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateHook#Hook
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#updateHookWithResponse(Hook)}.
     */
    public void updateHookWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateHookWithResponse#Hook
        final String emailHookId = "f00853f1-6627-447f-bacf-8dccf2e86fed";
        metricAdvisorAdministrationAsyncClient.getHookWithResponse(emailHookId)
            .flatMap(response -> {
                EmailHook emailHook = (EmailHook) response.getValue();
                emailHook
                    .removeEmailToAlert("alertme@alertme.com")
                    .addEmailToAlert("alertme2@alertme.com")
                    .addEmailToAlert("alertme3@alertme.com");
                return metricAdvisorAdministrationAsyncClient.updateHookWithResponse(emailHook);
            })
            .subscribe(response -> {
                System.out.printf("Response statusCode: %d%n", response.getStatusCode());
                EmailHook emailHook = (EmailHook) response.getValue();
                System.out.printf("Hook Id: %s%n", emailHook.getId());
                System.out.printf("Hook Name: %s%n", emailHook.getName());
                System.out.printf("Hook Description: %s%n", emailHook.getDescription());
                System.out.printf("Hook External Link: %s%n", emailHook.getExternalLink());
                System.out.printf("Hook Emails: %s%n", String.join(",", emailHook.getEmailsToAlert()));
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateHookWithResponse#Hook
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#deleteHook(String)}.
     */
    public void deleteHook() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteHook#String
        final String emailHookId = "f00853f1-6627-447f-bacf-8dccf2e86fed";
        metricAdvisorAdministrationAsyncClient.deleteHook(emailHookId);
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteHook#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#deleteHookWithResponse(String)}.
     */
    public void deleteHookWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteHookWithResponse#String
        final String emailHookId = "f00853f1-6627-447f-bacf-8dccf2e86fed";
        metricAdvisorAdministrationAsyncClient.deleteHookWithResponse(emailHookId)
            .subscribe(response -> {
                System.out.printf("Response statusCode: %d%n", response.getStatusCode());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteHookWithResponse#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#listHooks()}.
     */
    public void listHooks() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listHooks
        metricAdvisorAdministrationAsyncClient.listHooks()
            .subscribe(hook -> {
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
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listHooks
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#listHooks(ListHookOptions)}.
     */
    public void listHooksWithOptions() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listHooks#ListHookOptions
        ListHookOptions options = new ListHookOptions()
            .setSkip(100)
            .setTop(20);
        int[] pageCount = new int[1];
        metricAdvisorAdministrationAsyncClient.listHooks(options).byPage()
            .subscribe(hookPage -> {
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
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listHooks#ListHookOptions
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#listDataFeedIngestionStatus(String, ListDataFeedIngestionOptions)}.
     */
    public void listDataFeedIngestionStatus() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listDataFeedIngestionStatus#String-ListDataFeedIngestionOptions
        final String dataFeedId = "4957a2f7-a0f4-4fc0-b8d7-d866c1df0f4c";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final ListDataFeedIngestionOptions options = new ListDataFeedIngestionOptions(startTime, endTime);
        metricAdvisorAdministrationAsyncClient.listDataFeedIngestionStatus(dataFeedId, options)
            .subscribe(ingestionStatus -> {
                System.out.printf("Timestamp: %s%n", ingestionStatus.getTimestamp());
                System.out.printf("Status: %s%n", ingestionStatus.getStatus());
                System.out.printf("Message: %s%n", ingestionStatus.getMessage());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listDataFeedIngestionStatus#String-ListDataFeedIngestionOptions
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#refreshDataFeedIngestion(String, OffsetDateTime, OffsetDateTime)}.
     */
    public void refreshDataFeedIngestion() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.refreshDataFeedIngestion#String-OffsetDateTime-OffsetDateTime
        final String dataFeedId = "4957a2f7-a0f4-4fc0-b8d7-d866c1df0f4c";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-03-03T00:00:00Z");
        metricAdvisorAdministrationAsyncClient.refreshDataFeedIngestion(dataFeedId,
            startTime,
            endTime).subscribe();
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.refreshDataFeedIngestion#String-OffsetDateTime-OffsetDateTime
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#refreshDataFeedIngestionWithResponse(String, OffsetDateTime, OffsetDateTime)}.
     */
    public void refreshDataFeedIngestionWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.refreshDataFeedIngestionWithResponse#String-OffsetDateTime-OffsetDateTime
        final String dataFeedId = "4957a2f7-a0f4-4fc0-b8d7-d866c1df0f4c";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-03-03T00:00:00Z");
        metricAdvisorAdministrationAsyncClient.refreshDataFeedIngestionWithResponse(dataFeedId,
            startTime,
            endTime)
            .subscribe(response -> {
                System.out.printf("Response statusCode: %d%n", response.getStatusCode());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.refreshDataFeedIngestionWithResponse#String-OffsetDateTime-OffsetDateTime
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#getDataFeedIngestionProgress(String)}.
     */
    public void getDataFeedIngestionProgress() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getDataFeedIngestionProgress#String
        final String dataFeedId = "4957a2f7-a0f4-4fc0-b8d7-d866c1df0f4c";
        metricAdvisorAdministrationAsyncClient.getDataFeedIngestionProgress(dataFeedId)
            .subscribe(ingestionProgress -> {
                System.out.printf("Latest active timestamp: %s%n", ingestionProgress.getLatestActiveTimestamp());
                System.out.printf("Latest successful timestamp: %s%n", ingestionProgress.getLatestSuccessTimestamp());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getDataFeedIngestionProgress#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#getDataFeedIngestionProgressWithResponse(String)}.
     */
    public void getDataFeedIngestionProgressWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getDataFeedIngestionProgressWithResponse#String
        final String dataFeedId = "4957a2f7-a0f4-4fc0-b8d7-d866c1df0f4c";
        metricAdvisorAdministrationAsyncClient.getDataFeedIngestionProgressWithResponse(dataFeedId, Context.NONE)
            .subscribe(response -> {
                System.out.printf("Response statusCode: %d%n", response.getStatusCode());
                DataFeedIngestionProgress ingestionProgress = response.getValue();
                System.out.printf("Latest active timestamp: %s%n", ingestionProgress.getLatestActiveTimestamp());
                System.out.printf("Latest successful timestamp: %s%n", ingestionProgress.getLatestSuccessTimestamp());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getDataFeedIngestionProgressWithResponse#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#createMetricAnomalyDetectionConfiguration(String, AnomalyDetectionConfiguration)}.
     */
    public void createDetectionConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createMetricAnomalyDetectionConfiguration#String-AnomalyDetectionConfiguration
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
        metricAdvisorAdministrationAsyncClient
            .createMetricAnomalyDetectionConfiguration(metricId, detectionConfig)
            .subscribe(createdDetectionConfig -> {
                System.out.printf("Detection config Id: %s%n", createdDetectionConfig.getId());
                System.out.printf("Name: %s%n", createdDetectionConfig.getName());
                System.out.printf("Description: %s%n", createdDetectionConfig.getDescription());
                System.out.printf("MetricId: %s%n", createdDetectionConfig.getMetricId());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createMetricAnomalyDetectionConfiguration#String-AnomalyDetectionConfiguration
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#createMetricAnomalyDetectionConfigurationWithResponse(String, AnomalyDetectionConfiguration)}.
     */
    public void createDetectionConfigurationWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createMetricAnomalyDetectionConfigurationWithResponse#String-AnomalyDetectionConfiguration
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
        metricAdvisorAdministrationAsyncClient
            .createMetricAnomalyDetectionConfigurationWithResponse(metricId, detectionConfig)
            .subscribe(response -> {
                System.out.printf("Response statusCode: %d%n", response.getStatusCode());
                AnomalyDetectionConfiguration createdDetectionConfig = response.getValue();
                System.out.printf("Detection config Id: %s%n", createdDetectionConfig.getId());
                System.out.printf("Name: %s%n", createdDetectionConfig.getName());
                System.out.printf("Description: %s%n", createdDetectionConfig.getDescription());
                System.out.printf("MetricId: %s%n", createdDetectionConfig.getMetricId());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createMetricAnomalyDetectionConfigurationWithResponse#String-AnomalyDetectionConfiguration
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#getMetricAnomalyDetectionConfiguration(String)}.
     */
    public void getDetectionConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getMetricAnomalyDetectionConfiguration#String
        final String detectionConfigId = "7b8069a1-1564-46da-9f50-b5d0dd9129ab";
        metricAdvisorAdministrationAsyncClient
            .getMetricAnomalyDetectionConfiguration(detectionConfigId)
            .subscribe(detectionConfig -> {
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
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getMetricAnomalyDetectionConfiguration#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#getMetricAnomalyDetectionConfigurationWithResponse(String)}.
     */
    public void getDetectionConfigurationWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getMetricAnomalyDetectionConfigurationWithResponse#String
        final String detectionConfigId = "7b8069a1-1564-46da-9f50-b5d0dd9129ab";
        metricAdvisorAdministrationAsyncClient
            .getMetricAnomalyDetectionConfigurationWithResponse(detectionConfigId)
            .subscribe(response -> {
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
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getMetricAnomalyDetectionConfigurationWithResponse#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#listMetricAnomalyDetectionConfigurations(String)}.
     */
    public void listDetectionConfigurations() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listMetricAnomalyDetectionConfigurations#String
        final String metricId = "0b836da8-10e6-46cd-8f4f-28262e113a62";
        metricAdvisorAdministrationAsyncClient.listMetricAnomalyDetectionConfigurations(metricId)
            .subscribe(detectionConfig -> {
                System.out.printf("Detection config Id: %s%n", detectionConfig.getId());
                System.out.printf("Name: %s%n", detectionConfig.getName());
                System.out.printf("Description: %s%n", detectionConfig.getDescription());
                System.out.printf("MetricId: %s%n", detectionConfig.getMetricId());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listMetricAnomalyDetectionConfigurations#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#updateMetricAnomalyDetectionConfiguration(AnomalyDetectionConfiguration)}.
     */
    public void updateDetectionConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateMetricAnomalyDetectionConfiguration#AnomalyDetectionConfiguration
        final String detectionConfigId = "7b8069a1-1564-46da-9f50-b5d0dd9129ab";
        metricAdvisorAdministrationAsyncClient
            .getMetricAnomalyDetectionConfiguration(detectionConfigId)
            .flatMap(detectionConfig -> {
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
                return metricAdvisorAdministrationAsyncClient
                    .updateMetricAnomalyDetectionConfiguration(detectionConfig);
            })
            .subscribe(updatedDetectionConfig -> {
                System.out.printf("Detection config Id: %s%n", updatedDetectionConfig.getId());
                System.out.printf("Name: %s%n", updatedDetectionConfig.getName());
                System.out.printf("Description: %s%n", updatedDetectionConfig.getDescription());
                System.out.printf("MetricId: %s%n", updatedDetectionConfig.getMetricId());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateMetricAnomalyDetectionConfiguration#AnomalyDetectionConfiguration
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#updateMetricAnomalyDetectionConfigurationWithResponse(AnomalyDetectionConfiguration)}
     */
    public void updateDetectionConfigurationWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateMetricAnomalyDetectionConfigurationWithResponse#AnomalyDetectionConfiguration
        final String detectionConfigId = "7b8069a1-1564-46da-9f50-b5d0dd9129ab";
        metricAdvisorAdministrationAsyncClient
            .getMetricAnomalyDetectionConfigurationWithResponse(detectionConfigId)
            .flatMap(response -> {
                AnomalyDetectionConfiguration detectionConfig = response.getValue();
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
                return metricAdvisorAdministrationAsyncClient
                    .updateMetricAnomalyDetectionConfigurationWithResponse(detectionConfig);
            })
            .subscribe(response -> {
                AnomalyDetectionConfiguration updatedDetectionConfig = response.getValue();
                System.out.printf("Detection config Id: %s%n", updatedDetectionConfig.getId());
                System.out.printf("Name: %s%n", updatedDetectionConfig.getName());
                System.out.printf("Description: %s%n", updatedDetectionConfig.getDescription());
                System.out.printf("MetricId: %s%n", updatedDetectionConfig.getMetricId());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateMetricAnomalyDetectionConfigurationWithResponse#AnomalyDetectionConfiguration
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#deleteMetricAnomalyDetectionConfiguration(String)}.
     */
    public void deleteDetectionConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteMetricAnomalyDetectionConfiguration#String
        final String detectionConfigId = "7b8069a1-1564-46da-9f50-b5d0dd9129ab";
        metricAdvisorAdministrationAsyncClient
            .deleteMetricAnomalyDetectionConfiguration(detectionConfigId)
            .subscribe();
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteMetricAnomalyDetectionConfiguration#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#deleteMetricAnomalyDetectionConfigurationWithResponse(String)}.
     */
    public void deleteDetectionConfigurationWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteMetricAnomalyDetectionConfigurationWithResponse#String
        final String detectionConfigId = "7b8069a1-1564-46da-9f50-b5d0dd9129ab";
        metricAdvisorAdministrationAsyncClient
            .deleteMetricAnomalyDetectionConfigurationWithResponse(detectionConfigId)
            .subscribe(response -> {
                System.out.printf("Response statusCode: %d%n", response.getStatusCode());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteMetricAnomalyDetectionConfigurationWithResponse#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#createAnomalyAlertConfiguration(AnomalyAlertConfiguration)}
     */
    public void createAnomalyAlertConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createAnomalyAlertConfiguration#AnomalyAlertConfiguration
        String detectionConfigurationId1 = "9ol48er30-6e6e-4391-b78f-b00dfee1e6f5";
        String detectionConfigurationId2 = "3e58er30-6e6e-4391-b78f-b00dfee1e6f5";
        String hookId1 = "5f48er30-6e6e-4391-b78f-b00dfee1e6f5";
        String hookId2 = "8i48er30-6e6e-4391-b78f-b00dfee1e6f5";

        metricAdvisorAdministrationAsyncClient.createAnomalyAlertConfiguration(
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
                .setIdOfHooksToAlert(Arrays.asList(hookId1, hookId2)))
            .subscribe(anomalyAlertConfiguration -> {
                System.out.printf("Anomaly alert configuration Id: %s%n", anomalyAlertConfiguration.getId());
                System.out.printf("Anomaly alert configuration description: %s%n",
                    anomalyAlertConfiguration.getDescription());
                System.out.printf("Anomaly alert configuration hook ids: %.2f%n",
                    anomalyAlertConfiguration.getIdOfHooksToAlert());
                System.out.printf("Anomaly alert configuration cross metrics operator: %s%n",
                    anomalyAlertConfiguration.getCrossMetricsOperator().toString());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createAnomalyAlertConfiguration#AnomalyAlertConfiguration
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#createAnomalyAlertConfigurationWithResponse(AnomalyAlertConfiguration)}
     */
    public void createAnomalyAlertConfigurationWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createAnomalyAlertConfigurationWithResponse#AnomalyAlertConfiguration

        String detectionConfigurationId1 = "9ol48er30-6e6e-4391-b78f-b00dfee1e6f5";
        String detectionConfigurationId2 = "3e58er30-6e6e-4391-b78f-b00dfee1e6f5";
        String hookId1 = "5f48er30-6e6e-4391-b78f-b00dfee1e6f5";
        String hookId2 = "8i48er30-6e6e-4391-b78f-b00dfee1e6f5";

        metricAdvisorAdministrationAsyncClient.createAnomalyAlertConfigurationWithResponse(
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
                .setIdOfHooksToAlert(Arrays.asList(hookId1, hookId2)))
            .subscribe(alertConfigurationResponse -> {
                System.out.printf("Anomaly alert creation operation status: %s%n",
                    alertConfigurationResponse.getStatusCode());
                final AnomalyAlertConfiguration anomalyAlertConfiguration = alertConfigurationResponse.getValue();
                System.out.printf("Anomaly alert configuration Id: %s%n", anomalyAlertConfiguration.getId());
                System.out.printf("Anomaly alert configuration description: %s%n",
                    anomalyAlertConfiguration.getDescription());
                System.out.printf("Anomaly alert configuration hook ids: %s%n",
                    anomalyAlertConfiguration.getIdOfHooksToAlert());
                System.out.printf("Anomaly alert configuration cross metrics operator: %s%n",
                    anomalyAlertConfiguration.getCrossMetricsOperator().toString());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createAnomalyAlertConfigurationWithResponse#AnomalyAlertConfiguration
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#getAnomalyAlertConfiguration(String)}
     */
    public void getAnomalyAlertConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getAnomalyAlertConfiguration#String
        String alertConfigId = "1p0f8er30-6e6e-4391-b78f-bpfdfee1e6f5";

        metricAdvisorAdministrationAsyncClient.getAnomalyAlertConfiguration(alertConfigId)
            .subscribe(anomalyAlertConfiguration -> {
                System.out.printf("Anomaly alert configuration Id: %s%n", anomalyAlertConfiguration.getId());
                System.out.printf("Anomaly alert configuration description: %s%n",
                    anomalyAlertConfiguration.getDescription());
                System.out.printf("Anomaly alert configuration hook ids: %.2f%n",
                    anomalyAlertConfiguration.getIdOfHooksToAlert());
                System.out.printf("Anomaly alert configuration cross metrics operator: %s%n",
                    anomalyAlertConfiguration.getCrossMetricsOperator().toString());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getAnomalyAlertConfiguration#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#getAnomalyAlertConfigurationWithResponse(String)}
     */
    public void getAnomalyAlertConfigurationWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getAnomalyAlertConfigurationWithResponse#String
        String alertConfigId = "1p0f8er30-6e6e-4391-b78f-bpfdfee1e6f5";

        metricAdvisorAdministrationAsyncClient.getAnomalyAlertConfigurationWithResponse(alertConfigId)
            .subscribe(alertConfigurationResponse -> {
                System.out.printf("Anomaly alert creation operation status: %s%n",
                    alertConfigurationResponse.getStatusCode());
                final AnomalyAlertConfiguration anomalyAlertConfiguration = alertConfigurationResponse.getValue();
                System.out.printf("Anomaly alert configuration Id: %s%n", anomalyAlertConfiguration.getId());
                System.out.printf("Anomaly alert configuration description: %s%n",
                    anomalyAlertConfiguration.getDescription());
                System.out.printf("Anomaly alert configuration hook ids: %.2f%n",
                    anomalyAlertConfiguration.getIdOfHooksToAlert());
                System.out.printf("Anomaly alert configuration cross metrics operator: %s%n",
                    anomalyAlertConfiguration.getCrossMetricsOperator().toString());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getAnomalyAlertConfigurationWithResponse#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#updateAnomalyAlertConfiguration(AnomalyAlertConfiguration)}
     */
    public void updateAnomalyAlertConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateAnomalyAlertConfiguration#AnomalyAlertConfiguration

        String alertConfigId = "1p0f8er30-6e6e-4391-b78f-bpfdfee1e6f5";
        String additionalHookId = "2gh8er30-6e6e-4391-b78f-bpfdfee1e6f5";

        metricAdvisorAdministrationAsyncClient.getAnomalyAlertConfiguration(alertConfigId)
            .flatMap(existingAnomalyConfig -> {
                return metricAdvisorAdministrationAsyncClient.updateAnomalyAlertConfiguration(
                    existingAnomalyConfig
                        .addIdOfHookToAlert(additionalHookId)
                        .setDescription("updated to add more hook ids"));
            }).subscribe(updateAnomalyAlertConfiguration -> {
                System.out.printf("Updated anomaly alert configuration Id: %s%n",
                    updateAnomalyAlertConfiguration.getId());
                System.out.printf("Updated anomaly alert configuration description: %s%n",
                    updateAnomalyAlertConfiguration.getDescription());
                System.out.printf("Updated anomaly alert configuration hook ids: %.2f%n",
                    updateAnomalyAlertConfiguration.getIdOfHooksToAlert());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateAnomalyAlertConfiguration#AnomalyAlertConfiguration
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#updateAnomalyAlertConfigurationWithResponse(AnomalyAlertConfiguration)}
     */
    public void updateAnomalyAlertConfigurationWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateAnomalyAlertConfigurationWithResponse#AnomalyAlertConfiguration

        String alertConfigId = "1p0f8er30-6e6e-4391-b78f-bpfdfee1e6f5";
        String additionalHookId = "2gh8er30-6e6e-4391-b78f-bpfdfee1e6f5";

        metricAdvisorAdministrationAsyncClient.getAnomalyAlertConfiguration(alertConfigId)
            .flatMap(existingAnomalyConfig -> {
                return metricAdvisorAdministrationAsyncClient.updateAnomalyAlertConfigurationWithResponse(
                    existingAnomalyConfig
                        .addIdOfHookToAlert(additionalHookId)
                        .setDescription("updated to add more hook ids"));
            }).subscribe(alertConfigurationResponse -> {
                System.out.printf("Update anomaly alert operation status: %s%n",
                    alertConfigurationResponse.getStatusCode());
                final AnomalyAlertConfiguration updatAnomalyAlertConfiguration = alertConfigurationResponse.getValue();
                System.out.printf("Updated anomaly alert configuration Id: %s%n",
                    updatAnomalyAlertConfiguration.getId());
                System.out.printf("Updated anomaly alert configuration description: %s%n",
                    updatAnomalyAlertConfiguration.getDescription());
                System.out.printf("Updated anomaly alert configuration hook ids: %.2f%n",
                    updatAnomalyAlertConfiguration.getIdOfHooksToAlert());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateAnomalyAlertConfigurationWithResponse#AnomalyAlertConfiguration
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#deleteAnomalyAlertConfiguration(String)}.
     */
    public void deleteAnomalyAlertConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteAnomalyAlertConfiguration#String
        String alertConfigId = "1p0f8er30-6e6e-4391-b78f-bpfdfee1e6f5";
        metricAdvisorAdministrationAsyncClient.deleteAnomalyAlertConfiguration(alertConfigId);
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteAnomalyAlertConfiguration#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#deleteAnomalyAlertConfigurationWithResponse(String)}
     */
    public void deleteAnomalyAlertConfigurationWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteAnomalyAlertConfigurationWithResponse#String
        String alertConfigId = "1p0f8er30-6e6e-4391-b78f-bpfdfee1e6f5";

        metricAdvisorAdministrationAsyncClient.deleteAnomalyAlertConfigurationWithResponse(alertConfigId)
            .subscribe(response -> {
                System.out.printf("Anomaly alert config delete operation status : %s%n", response.getStatusCode());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteAnomalyAlertConfigurationWithResponse#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#listAnomalyAlertConfigurations(String)}.
     */
    public void listAnomalyAlertConfigurations() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listAnomalyAlertConfigurations#String
        String detectionConfigId = "3rt98er30-6e6e-4391-b78f-bpfdfee1e6f5";
        metricAdvisorAdministrationAsyncClient.listAnomalyAlertConfigurations(detectionConfigId)
            .subscribe(anomalyAlertConfiguration -> {
                System.out.printf("Anomaly alert configuration Id: %s%n", anomalyAlertConfiguration.getId());
                System.out.printf("Anomaly alert configuration description: %s%n",
                    anomalyAlertConfiguration.getDescription());
                System.out.printf("Anomaly alert configuration hook ids: %.2f%n",
                    anomalyAlertConfiguration.getIdOfHooksToAlert());
                System.out.printf("Anomaly alert configuration cross metrics operator: %s%n",
                    anomalyAlertConfiguration.getCrossMetricsOperator().toString());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listAnomalyAlertConfigurations#String
    }
}
