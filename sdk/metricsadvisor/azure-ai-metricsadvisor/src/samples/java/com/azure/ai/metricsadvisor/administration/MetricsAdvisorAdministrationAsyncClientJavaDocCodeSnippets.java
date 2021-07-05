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
import com.azure.ai.metricsadvisor.administration.models.DataFeedMetric;
import com.azure.ai.metricsadvisor.administration.models.DataFeedOptions;
import com.azure.ai.metricsadvisor.administration.models.DataFeedRollupSettings;
import com.azure.ai.metricsadvisor.administration.models.DataFeedRollupType;
import com.azure.ai.metricsadvisor.administration.models.DataFeedSchema;
import com.azure.ai.metricsadvisor.administration.models.DataFeedStatus;
import com.azure.ai.metricsadvisor.administration.models.DataSourceCredentialEntity;
import com.azure.ai.metricsadvisor.administration.models.DataSourceServicePrincipalInKeyVault;
import com.azure.ai.metricsadvisor.administration.models.DetectionConditionOperator;
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
import com.azure.ai.metricsadvisor.administration.models.SeverityCondition;
import com.azure.ai.metricsadvisor.administration.models.SmartDetectionCondition;
import com.azure.ai.metricsadvisor.administration.models.SuppressCondition;
import com.azure.ai.metricsadvisor.administration.models.WebNotificationHook;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.util.Context;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient}
 */
public class MetricsAdvisorAdministrationAsyncClientJavaDocCodeSnippets {
    MetricsAdvisorAdministrationAsyncClient metricsAdvisorAdminAsyncClient =
        new MetricsAdvisorAdministrationClientBuilder().buildAsyncClient();

    /**
     * Code snippet for creating a {@link MetricsAdvisorAdministrationAsyncClient}
     */
    public void createMetricAdvisorAdministrationAsyncClient() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.instantiation
        MetricsAdvisorAdministrationAsyncClient metricsAdvisorAdminAsyncClient =
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

        MetricsAdvisorAdministrationAsyncClient metricAdvisorAdminAsyncClient =
            new MetricsAdvisorAdministrationClientBuilder()
                .credential(new MetricsAdvisorKeyCredential("{subscription_key}", "{api_key}"))
                .endpoint("{endpoint}")
                .pipeline(pipeline)
                .buildAsyncClient();
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.pipeline.instantiation
    }

    // Create Data Feed

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#createDataFeed(DataFeed)}
     */
    public void createDataFeed() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createDataFeed#DataFeed
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

        metricsAdvisorAdminAsyncClient.createDataFeed(dataFeed)
            .subscribe(createdDataFeed -> {
                System.out.printf("Data feed Id: %s%n", createdDataFeed.getId());
                System.out.printf("Data feed description: %s%n", createdDataFeed.getOptions().getDescription());
                System.out.printf("Data feed source type: %s%n", createdDataFeed.getSourceType());
                System.out.printf("Data feed creator: %s%n", createdDataFeed.getCreator());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createDataFeed#DataFeed
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#createDataFeedWithResponse(DataFeed)}
     */
    public void createDataFeedWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createDataFeedWithResponse#DataFeed
        DataFeed dataFeed = new DataFeed()
            .setName("dataFeedName")
            .setSource(new MySqlDataFeedSource("conn-string", "query"))
            .setGranularity(new DataFeedGranularity().setGranularityType(DataFeedGranularityType.DAILY))
            .setSchema(new DataFeedSchema(
                Arrays.asList(
                    new DataFeedMetric("metric1"),
                    new DataFeedMetric("metric2")
                )
            ))
            .setIngestionSettings(new DataFeedIngestionSettings(OffsetDateTime.parse("2020-01-01T00:00:00Z")))
            .setOptions(new DataFeedOptions()
                .setDescription("data feed description")
                .setRollupSettings(new DataFeedRollupSettings()
                    .setRollupType(DataFeedRollupType.AUTO_ROLLUP)));

        metricsAdvisorAdminAsyncClient.createDataFeedWithResponse(dataFeed)
            .subscribe(dataFeedResponse -> {
                System.out.printf("Data feed create operation status: %s%n", dataFeedResponse.getStatusCode());
                DataFeed createdDataFeed = dataFeedResponse.getValue();
                System.out.printf("Data feed Id: %s%n", createdDataFeed.getId());
                System.out.printf("Data feed description: %s%n", createdDataFeed.getOptions().getDescription());
                System.out.printf("Data feed source type: %s%n", createdDataFeed.getSourceType());
                System.out.printf("Data feed creator: %s%n", createdDataFeed.getCreator());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createDataFeedWithResponse#DataFeed
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#getDataFeed(String)}
     */
    public void getDataFeed() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getDataFeed#String
        final String dataFeedId = "r47053f1-9080-09lo-bacf-8dccf2e86f";
        metricsAdvisorAdminAsyncClient.getDataFeed(dataFeedId)
            .subscribe(dataFeed -> {
                System.out.printf("Data feed Id: %s%n", dataFeed.getId());
                System.out.printf("Data feed description: %s%n", dataFeed.getOptions().getDescription());
                System.out.printf("Data feed source type: %s%n", dataFeed.getSourceType());
                System.out.printf("Data feed creator: %s%n", dataFeed.getCreator());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getDataFeed#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#getDataFeedWithResponse(String)}
     */
    public void getDataFeedWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getDataFeedWithResponse#String
        final String dataFeedId = "r47053f1-9080-09lo-bacf-8dccf2e86f";
        metricsAdvisorAdminAsyncClient.getDataFeedWithResponse(dataFeedId)
            .subscribe(dataFeedResponse -> {
                System.out.printf("Data feed get operation status: %s%n", dataFeedResponse.getStatusCode());
                DataFeed dataFeed = dataFeedResponse.getValue();
                System.out.printf("Data feed Id: %s%n", dataFeed.getId());
                System.out.printf("Data feed description: %s%n", dataFeed.getOptions().getDescription());
                System.out.printf("Data feed source type: %s%n", dataFeed.getSourceType());
                System.out.printf("Data feed creator: %s%n", dataFeed.getCreator());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getDataFeedWithResponse#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#updateDataFeed(DataFeed)}
     */
    public void updateDataFeed() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateDataFeed#DataFeed
        final String dataFeedId = "r47053f1-9080-09lo-bacf-8dccf2e86f";
        metricsAdvisorAdminAsyncClient.getDataFeed(dataFeedId)
            .flatMap(existingDataFeed -> {
                return metricsAdvisorAdminAsyncClient.updateDataFeed(
                    existingDataFeed
                       .setOptions(new DataFeedOptions()
                           .setDescription("set updated description"))
               );
            })
            .subscribe(updatedDataFeed -> {
                System.out.printf("Data feed Id: %s%n", updatedDataFeed.getId());
                System.out.printf("Data feed updated description: %s%n", updatedDataFeed.getOptions().getDescription());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateDataFeed#DataFeed
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#updateDataFeedWithResponse(DataFeed)}
     */
    public void updateDataFeedWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateDataFeedWithResponse#DataFeed
        final String dataFeedId = "r47053f1-9080-09lo-bacf-8dccf2e86f";
        metricsAdvisorAdminAsyncClient.getDataFeed(dataFeedId)
            .flatMap(existingDataFeed -> {
                return metricsAdvisorAdminAsyncClient.updateDataFeedWithResponse(
                    existingDataFeed
                        .setOptions(new DataFeedOptions()
                            .setDescription("set updated description"))
                );
            })
            .subscribe(dataFeedResponse -> {
                System.out.printf("Data feed update operation status: %s%n", dataFeedResponse.getStatusCode());
                DataFeed updatedDataFeed = dataFeedResponse.getValue();
                System.out.printf("Data feed Id: %s%n", updatedDataFeed.getId());
                System.out.printf("Data feed updated description: %s%n", updatedDataFeed.getOptions().getDescription());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateDataFeedWithResponse#DataFeed
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#deleteDataFeed(String)}.
     */
    public void deleteDataFeed() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteDataFeed#String
        final String dataFeedId = "t00853f1-9080-447f-bacf-8dccf2e86f";
        metricsAdvisorAdminAsyncClient.deleteDataFeed(dataFeedId);
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteDataFeed#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#deleteDataFeedWithResponse(String)}
     */
    public void deleteDataFeedWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteDataFeedWithResponse#String
        final String dataFeedId = "eh0854f1-8927-447f-bacf-8dccf2e86fwe";
        metricsAdvisorAdminAsyncClient.deleteDataFeedWithResponse(dataFeedId)
            .subscribe(response ->
                System.out.printf("Data feed delete operation status : %s%n", response.getStatusCode()));
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteDataFeedWithResponse#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#listDataFeeds()}
     */
    public void listDataFeeds() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listDataFeeds
        metricsAdvisorAdminAsyncClient.listDataFeeds()
            .subscribe(dataFeed -> {
                System.out.printf("Data feed Id: %s%n", dataFeed.getId());
                System.out.printf("Data feed description: %s%n", dataFeed.getOptions().getDescription());
                System.out.printf("Data feed source type: %s%n", dataFeed.getSourceType());
                System.out.printf("Data feed creator: %s%n", dataFeed.getCreator());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listDataFeeds
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#listDataFeeds(ListDataFeedOptions)} with options.
     */
    public void listDataFeedWithOptions() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listDataFeeds#ListDataFeedOptions
        metricsAdvisorAdminAsyncClient.listDataFeeds(
            new ListDataFeedOptions()
                .setListDataFeedFilter(
                    new ListDataFeedFilter()
                        .setDataFeedStatus(DataFeedStatus.ACTIVE)
                        .setDataFeedGranularityType(DataFeedGranularityType.DAILY))
                .setMaxPageSize(3))
            .subscribe(dataFeed -> {
                System.out.printf("Data feed Id: %s%n", dataFeed.getId());
                System.out.printf("Data feed description: %s%n", dataFeed.getOptions().getDescription());
                System.out.printf("Data feed source type: %s%n", dataFeed.getSourceType());
                System.out.printf("Data feed creator: %s%n", dataFeed.getCreator());
                System.out.printf("Data feed status: %s%n", dataFeed.getStatus());
                System.out.printf("Data feed granularity type: %s%n", dataFeed.getGranularity().getGranularityType());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listDataFeeds#ListDataFeedOptions
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#createHook(NotificationHook)}.
     */
    public void createHook() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createHook#NotificationHook
        NotificationHook emailNotificationHook = new EmailNotificationHook("email hook")
            .setDescription("my email hook")
            .setEmailsToAlert(new ArrayList<String>() {{
                    add("alertme@alertme.com");
                }})
            .setExternalLink("https://adwiki.azurewebsites.net/articles/howto/alerts/create-hooks.html");

        metricsAdvisorAdminAsyncClient.createHook(emailNotificationHook)
            .subscribe(hook -> {
                EmailNotificationHook createdEmailHook = (EmailNotificationHook) hook;
                System.out.printf("NotificationHook Id: %s%n", createdEmailHook.getId());
                System.out.printf("NotificationHook Name: %s%n", createdEmailHook.getName());
                System.out.printf("NotificationHook Description: %s%n", createdEmailHook.getDescription());
                System.out.printf("NotificationHook External Link: %s%n", createdEmailHook.getExternalLink());
                System.out.printf("NotificationHook Emails: %s%n", String.join(",",
                    createdEmailHook.getEmailsToAlert()));
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createHook#NotificationHook
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#createHookWithResponse(NotificationHook)}.
     */
    public void createHookWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createHookWithResponse#NotificationHook
        NotificationHook emailNotificationHook = new EmailNotificationHook("email hook")
            .setDescription("my email hook")
            .setEmailsToAlert(new ArrayList<String>() {{
                    add("alertme@alertme.com");
                }})
            .setExternalLink("https://adwiki.azurewebsites.net/articles/howto/alerts/create-hooks.html");

        metricsAdvisorAdminAsyncClient.createHookWithResponse(emailNotificationHook)
            .subscribe(response -> {
                System.out.printf("Response statusCode: %d%n", response.getStatusCode());
                EmailNotificationHook createdEmailHook = (EmailNotificationHook) response.getValue();
                System.out.printf("NotificationHook Id: %s%n", createdEmailHook.getId());
                System.out.printf("NotificationHook Name: %s%n", createdEmailHook.getName());
                System.out.printf("NotificationHook Description: %s%n", createdEmailHook.getDescription());
                System.out.printf("NotificationHook External Link: %s%n", createdEmailHook.getExternalLink());
                System.out.printf("NotificationHook Emails: %s%n", String.join(",",
                    createdEmailHook.getEmailsToAlert()));
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createHookWithResponse#NotificationHook
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#getHook(String)}.
     */
    public void getHook() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getHook#String
        final String hookId = "f00853f1-6627-447f-bacf-8dccf2e86fed";
        metricsAdvisorAdminAsyncClient.getHook(hookId)
            .subscribe(hook -> {
                if (hook instanceof EmailNotificationHook) {
                    EmailNotificationHook emailHook = (EmailNotificationHook) hook;
                    System.out.printf("Email Hook Id: %s%n", emailHook.getId());
                    System.out.printf("Email Hook Name: %s%n", emailHook.getName());
                    System.out.printf("Email Hook Description: %s%n", emailHook.getDescription());
                    System.out.printf("Email Hook External Link: %s%n", emailHook.getExternalLink());
                    System.out.printf("Email Hook Emails: %s%n", String.join(",",
                        emailHook.getEmailsToAlert()));
                } else if (hook instanceof WebNotificationHook) {
                    WebNotificationHook webHook = (WebNotificationHook) hook;
                    System.out.printf("Web Hook Id: %s%n", webHook.getId());
                    System.out.printf("Web Hook Name: %s%n", webHook.getName());
                    System.out.printf("Web Hook Description: %s%n", webHook.getDescription());
                    System.out.printf("Web Hook External Link: %s%n", webHook.getExternalLink());
                    System.out.printf("Web Hook Endpoint: %s%n", webHook.getEndpoint());
                    System.out.printf("Web Hook Headers: %s%n", webHook.getHttpHeaders());
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
        metricsAdvisorAdminAsyncClient.getHookWithResponse(hookId)
            .subscribe(response -> {
                System.out.printf("Response status code: %d%n", response.getStatusCode());
                NotificationHook notificationHook = response.getValue();
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
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getHookWithResponse#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#updateHook(NotificationHook)}.
     */
    public void updateHook() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateHook#NotificationHook
        final String emailHookId = "f00853f1-6627-447f-bacf-8dccf2e86fed";
        metricsAdvisorAdminAsyncClient.getHook(emailHookId)
            .flatMap(hook -> {
                EmailNotificationHook emailHook = (EmailNotificationHook) hook;
                List<String> emailsToUpdate = new ArrayList<>(emailHook.getEmailsToAlert());
                emailsToUpdate.remove("alertme@alertme.com");
                emailsToUpdate.add("alertme2@alertme.com");
                emailsToUpdate.add("alertme3@alertme.com");
                emailHook.setEmailsToAlert(emailsToUpdate);
                return metricsAdvisorAdminAsyncClient.updateHook(emailHook);
            })
            .subscribe(hook -> {
                EmailNotificationHook emailHook = (EmailNotificationHook) hook;
                System.out.printf("Email Hook Id: %s%n", emailHook.getId());
                System.out.printf("Email Hook Name: %s%n", emailHook.getName());
                System.out.printf("Email Hook Description: %s%n", emailHook.getDescription());
                System.out.printf("Email Hook External Link: %s%n", emailHook.getExternalLink());
                System.out.printf("Email Hook Emails: %s%n", String.join(",", emailHook.getEmailsToAlert()));
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateHook#NotificationHook
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#updateHookWithResponse(NotificationHook)}.
     */
    public void updateHookWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateHookWithResponse#NotificationHook
        final String emailHookId = "f00853f1-6627-447f-bacf-8dccf2e86fed";
        metricsAdvisorAdminAsyncClient.getHookWithResponse(emailHookId)
            .flatMap(response -> {
                EmailNotificationHook emailHook = (EmailNotificationHook) response.getValue();
                List<String> emailsToUpdate = new ArrayList<>(emailHook.getEmailsToAlert());
                emailsToUpdate.remove("alertme@alertme.com");
                emailsToUpdate.add("alertme2@alertme.com");
                emailsToUpdate.add("alertme3@alertme.com");
                emailHook.setEmailsToAlert(emailsToUpdate);
                return metricsAdvisorAdminAsyncClient.updateHookWithResponse(emailHook);
            })
            .subscribe(response -> {
                System.out.printf("Response statusCode: %d%n", response.getStatusCode());
                EmailNotificationHook emailHook = (EmailNotificationHook) response.getValue();
                System.out.printf("Email Hook Id: %s%n", emailHook.getId());
                System.out.printf("Email Hook Name: %s%n", emailHook.getName());
                System.out.printf("Email Hook Description: %s%n", emailHook.getDescription());
                System.out.printf("Email Hook External Link: %s%n", emailHook.getExternalLink());
                System.out.printf("Email Hook Emails: %s%n", String.join(",", emailHook.getEmailsToAlert()));
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateHookWithResponse#NotificationHook
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#deleteHook(String)}.
     */
    public void deleteHook() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteHook#String
        final String emailHookId = "f00853f1-6627-447f-bacf-8dccf2e86fed";
        metricsAdvisorAdminAsyncClient.deleteHook(emailHookId);
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteHook#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#deleteHookWithResponse(String)}.
     */
    public void deleteHookWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteHookWithResponse#String
        final String emailHookId = "f00853f1-6627-447f-bacf-8dccf2e86fed";
        metricsAdvisorAdminAsyncClient.deleteHookWithResponse(emailHookId)
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
        metricsAdvisorAdminAsyncClient.listHooks()
            .subscribe(hook -> {
                if (hook instanceof EmailNotificationHook) {
                    EmailNotificationHook emailHook = (EmailNotificationHook) hook;
                    System.out.printf("Email Hook Id: %s%n", emailHook.getId());
                    System.out.printf("Email Hook Name: %s%n", emailHook.getName());
                    System.out.printf("Email Hook Description: %s%n", emailHook.getDescription());
                    System.out.printf("Email Hook External Link: %s%n", emailHook.getExternalLink());
                    System.out.printf("Email Hook Emails: %s%n", String.join(",", emailHook.getEmailsToAlert()));
                } else if (hook instanceof WebNotificationHook) {
                    WebNotificationHook webHook = (WebNotificationHook) hook;
                    System.out.printf("Web Hook Id: %s%n", webHook.getId());
                    System.out.printf("Web Hook Name: %s%n", webHook.getName());
                    System.out.printf("Web Hook Description: %s%n", webHook.getDescription());
                    System.out.printf("Web Hook External Link: %s%n", webHook.getExternalLink());
                    System.out.printf("Web Hook Endpoint: %s%n", webHook.getEndpoint());
                    System.out.printf("Web Hook Headers: %s%n", webHook.getHttpHeaders());
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
            .setMaxPageSize(20);
        int[] pageCount = new int[1];
        metricsAdvisorAdminAsyncClient.listHooks(options).byPage()
            .subscribe(hookPage -> {
                System.out.printf("Page: %d%n", pageCount[0]++);
                for (NotificationHook notificationHook : hookPage.getElements()) {
                    if (notificationHook instanceof EmailNotificationHook) {
                        EmailNotificationHook emailHook = (EmailNotificationHook) notificationHook;
                        System.out.printf("Email Hook Id: %s%n", emailHook.getId());
                        System.out.printf("Email Hook Name: %s%n", emailHook.getName());
                        System.out.printf("Email Hook Description: %s%n", emailHook.getDescription());
                        System.out.printf("Email Hook External Link: %s%n", emailHook.getExternalLink());
                        System.out.printf("Email Hook Emails: %s%n", String.join(",", emailHook.getEmailsToAlert()));
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
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listHooks#ListHookOptions
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#listDataFeedIngestionStatus(String, ListDataFeedIngestionOptions)}.
     */
    public void listDataFeedIngestionStatusWithOptions() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listDataFeedIngestionStatus#String-ListDataFeedIngestionOptions
        final String dataFeedId = "4957a2f7-a0f4-4fc0-b8d7-d866c1df0f4c";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final ListDataFeedIngestionOptions options = new ListDataFeedIngestionOptions(startTime, endTime);
        metricsAdvisorAdminAsyncClient.listDataFeedIngestionStatus(dataFeedId, options)
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
        metricsAdvisorAdminAsyncClient.refreshDataFeedIngestion(dataFeedId,
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
        metricsAdvisorAdminAsyncClient.refreshDataFeedIngestionWithResponse(dataFeedId,
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
        metricsAdvisorAdminAsyncClient.getDataFeedIngestionProgress(dataFeedId)
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
        metricsAdvisorAdminAsyncClient.getDataFeedIngestionProgressWithResponse(dataFeedId, Context.NONE)
            .subscribe(response -> {
                System.out.printf("Response statusCode: %d%n", response.getStatusCode());
                DataFeedIngestionProgress ingestionProgress = response.getValue();
                System.out.printf("Latest active timestamp: %s%n", ingestionProgress.getLatestActiveTimestamp());
                System.out.printf("Latest successful timestamp: %s%n", ingestionProgress.getLatestSuccessTimestamp());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getDataFeedIngestionProgressWithResponse#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#createDetectionConfig(String, AnomalyDetectionConfiguration)}.
     */
    public void createDetectionConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createDetectionConfig#String-AnomalyDetectionConfiguration
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
        metricsAdvisorAdminAsyncClient
            .createDetectionConfig(metricId, detectionConfig)
            .subscribe(createdDetectionConfig -> {
                System.out.printf("Detection config Id: %s%n", createdDetectionConfig.getId());
                System.out.printf("Name: %s%n", createdDetectionConfig.getName());
                System.out.printf("Description: %s%n", createdDetectionConfig.getDescription());
                System.out.printf("MetricId: %s%n", createdDetectionConfig.getMetricId());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createDetectionConfig#String-AnomalyDetectionConfiguration
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#createDetectionConfigWithResponse(String, AnomalyDetectionConfiguration)}.
     */
    public void createDetectionConfigurationWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createDetectionConfigWithResponse#String-AnomalyDetectionConfiguration
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
        metricsAdvisorAdminAsyncClient
            .createDetectionConfigWithResponse(metricId, detectionConfig)
            .subscribe(response -> {
                System.out.printf("Response statusCode: %d%n", response.getStatusCode());
                AnomalyDetectionConfiguration createdDetectionConfig = response.getValue();
                System.out.printf("Detection config Id: %s%n", createdDetectionConfig.getId());
                System.out.printf("Name: %s%n", createdDetectionConfig.getName());
                System.out.printf("Description: %s%n", createdDetectionConfig.getDescription());
                System.out.printf("MetricId: %s%n", createdDetectionConfig.getMetricId());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createDetectionConfigWithResponse#String-AnomalyDetectionConfiguration
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#getDetectionConfig(String)}.
     */
    public void getDetectionConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getDetectionConfig#String
        final String detectionConfigId = "7b8069a1-1564-46da-9f50-b5d0dd9129ab";
        metricsAdvisorAdminAsyncClient
            .getDetectionConfig(detectionConfigId)
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
                        = Arrays.toString(seriesKey.asMap().entrySet().stream().toArray());
                    System.out.printf("- Series Key: %s%n", seriesKeyStr);
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
                        = Arrays.toString(seriesGroupKey.asMap().entrySet().stream().toArray());
                    System.out.printf("- Series Group Key: %s%n", seriesGroupKeyStr);
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
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getDetectionConfig#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#getDetectionConfigWithResponse(String)}.
     */
    public void getDetectionConfigurationWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getDetectionConfigWithResponse#String
        final String detectionConfigId = "7b8069a1-1564-46da-9f50-b5d0dd9129ab";
        metricsAdvisorAdminAsyncClient
            .getDetectionConfigWithResponse(detectionConfigId)
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
                        = Arrays.toString(seriesKey.asMap().entrySet().stream().toArray());
                    System.out.printf("- Series Key: %s%n", seriesKeyStr);
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
                        = Arrays.toString(seriesGroupKey.asMap().entrySet().stream().toArray());
                    System.out.printf("- Series Group Key: %s%n", seriesGroupKeyStr);
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
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getDetectionConfigWithResponse#String
    }

    public void listDetectionConfigurations() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listDetectionConfigs#String
        final String metricId = "0b836da8-10e6-46cd-8f4f-28262e113a62";
        metricsAdvisorAdminAsyncClient.listDetectionConfigs(metricId)
            .subscribe(detectionConfig -> {
                System.out.printf("Detection config Id: %s%n", detectionConfig.getId());
                System.out.printf("Name: %s%n", detectionConfig.getName());
                System.out.printf("Description: %s%n", detectionConfig.getDescription());
                System.out.printf("MetricId: %s%n", detectionConfig.getMetricId());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listDetectionConfigs#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#listDetectionConfigs(String, ListDetectionConfigsOptions)}.
     */
    public void listDetectionConfigurationsWithOptions() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listDetectionConfigs#String-ListDetectionConfigsOptions
        final String metricId = "0b836da8-10e6-46cd-8f4f-28262e113a62";
        metricsAdvisorAdminAsyncClient.listDetectionConfigs(metricId,
            new ListDetectionConfigsOptions())
            .subscribe(detectionConfig -> {
                System.out.printf("Detection config Id: %s%n", detectionConfig.getId());
                System.out.printf("Name: %s%n", detectionConfig.getName());
                System.out.printf("Description: %s%n", detectionConfig.getDescription());
                System.out.printf("MetricId: %s%n", detectionConfig.getMetricId());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listDetectionConfigs#String-ListDetectionConfigsOptions
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#updateDetectionConfig(AnomalyDetectionConfiguration)}.
     */
    public void updateDetectionConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateDetectionConfig#AnomalyDetectionConfiguration
        final String detectionConfigId = "7b8069a1-1564-46da-9f50-b5d0dd9129ab";
        metricsAdvisorAdminAsyncClient
            .getDetectionConfig(detectionConfigId)
            .flatMap(detectionConfig -> {
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
                return metricsAdvisorAdminAsyncClient
                    .updateDetectionConfig(detectionConfig);
            })
            .subscribe(updatedDetectionConfig -> {
                System.out.printf("Detection config Id: %s%n", updatedDetectionConfig.getId());
                System.out.printf("Name: %s%n", updatedDetectionConfig.getName());
                System.out.printf("Description: %s%n", updatedDetectionConfig.getDescription());
                System.out.printf("MetricId: %s%n", updatedDetectionConfig.getMetricId());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateDetectionConfig#AnomalyDetectionConfiguration
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#updateDetectionConfigWithResponse(AnomalyDetectionConfiguration)}
     */
    public void updateDetectionConfigurationWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateDetectionConfigWithResponse#AnomalyDetectionConfiguration
        final String detectionConfigId = "7b8069a1-1564-46da-9f50-b5d0dd9129ab";
        metricsAdvisorAdminAsyncClient
            .getDetectionConfigWithResponse(detectionConfigId)
            .flatMap(response -> {
                AnomalyDetectionConfiguration detectionConfig = response.getValue();
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
                return metricsAdvisorAdminAsyncClient
                    .updateDetectionConfigWithResponse(detectionConfig);
            })
            .subscribe(response -> {
                AnomalyDetectionConfiguration updatedDetectionConfig = response.getValue();
                System.out.printf("Detection config Id: %s%n", updatedDetectionConfig.getId());
                System.out.printf("Name: %s%n", updatedDetectionConfig.getName());
                System.out.printf("Description: %s%n", updatedDetectionConfig.getDescription());
                System.out.printf("MetricId: %s%n", updatedDetectionConfig.getMetricId());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateDetectionConfigWithResponse#AnomalyDetectionConfiguration
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#deleteDetectionConfig(String)}.
     */
    public void deleteDetectionConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteDetectionConfig#String
        final String detectionConfigId = "7b8069a1-1564-46da-9f50-b5d0dd9129ab";
        metricsAdvisorAdminAsyncClient
            .deleteDetectionConfig(detectionConfigId)
            .subscribe();
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteDetectionConfig#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#deleteDetectionConfigWithResponse(String)}.
     */
    public void deleteDetectionConfigurationWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteDetectionConfigWithResponse#String
        final String detectionConfigId = "7b8069a1-1564-46da-9f50-b5d0dd9129ab";
        metricsAdvisorAdminAsyncClient
            .deleteDetectionConfigWithResponse(detectionConfigId)
            .subscribe(response ->
                System.out.printf("Response statusCode: %d%n", response.getStatusCode()));
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteDetectionConfigWithResponse#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#createAlertConfig(AnomalyAlertConfiguration)}
     */
    public void createAnomalyAlertConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createAlertConfig#AnomalyAlertConfiguration
        String detectionConfigurationId1 = "9ol48er30-6e6e-4391-b78f-b00dfee1e6f5";
        String detectionConfigurationId2 = "3e58er30-6e6e-4391-b78f-b00dfee1e6f5";
        String hookId1 = "5f48er30-6e6e-4391-b78f-b00dfee1e6f5";
        String hookId2 = "8i48er30-6e6e-4391-b78f-b00dfee1e6f5";

        metricsAdvisorAdminAsyncClient.createAlertConfig(
            new AnomalyAlertConfiguration("My AnomalyAlert config name")
                .setDescription("alert config description")
                .setMetricAlertConfigurations(Arrays.asList(
                    new MetricAlertConfiguration(detectionConfigurationId1,
                        MetricAnomalyAlertScope.forWholeSeries()),
                    new MetricAlertConfiguration(detectionConfigurationId2,
                        MetricAnomalyAlertScope.forWholeSeries())
                        .setAlertConditions(new MetricAnomalyAlertConditions()
                            .setSeverityRangeCondition(new SeverityCondition(AnomalySeverity.HIGH, AnomalySeverity.HIGH)))))
                .setCrossMetricsOperator(MetricAlertConfigurationsOperator.AND)
                .setHookIdsToAlert(Arrays.asList(hookId1, hookId2)))
            .subscribe(anomalyAlertConfiguration -> {
                System.out.printf("DataPoint Anomaly alert configuration Id: %s%n", anomalyAlertConfiguration.getId());
                System.out.printf("DataPoint Anomaly alert configuration description: %s%n",
                    anomalyAlertConfiguration.getDescription());
                System.out.printf("DataPoint Anomaly alert configuration hook ids: %s%n",
                    anomalyAlertConfiguration.getHookIdsToAlert());
                System.out.printf("DataPoint Anomaly alert configuration cross metrics operator: %s%n",
                    anomalyAlertConfiguration.getCrossMetricsOperator().toString());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createAlertConfig#AnomalyAlertConfiguration
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#createAlertConfigWithResponse(AnomalyAlertConfiguration)}
     */
    public void createAnomalyAlertConfigurationWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createAlertConfigWithResponse#AnomalyAlertConfiguration

        String detectionConfigurationId1 = "9ol48er30-6e6e-4391-b78f-b00dfee1e6f5";
        String detectionConfigurationId2 = "3e58er30-6e6e-4391-b78f-b00dfee1e6f5";
        String hookId1 = "5f48er30-6e6e-4391-b78f-b00dfee1e6f5";
        String hookId2 = "8i48er30-6e6e-4391-b78f-b00dfee1e6f5";

        metricsAdvisorAdminAsyncClient.createAlertConfigWithResponse(
            new AnomalyAlertConfiguration("My AnomalyAlert config name")
                .setDescription("alert config description")
                .setMetricAlertConfigurations(Arrays.asList(
                    new MetricAlertConfiguration(detectionConfigurationId1,
                        MetricAnomalyAlertScope.forWholeSeries()),
                    new MetricAlertConfiguration(detectionConfigurationId2,
                        MetricAnomalyAlertScope.forWholeSeries())
                        .setAlertConditions(new MetricAnomalyAlertConditions()
                            .setSeverityRangeCondition(new SeverityCondition(AnomalySeverity.HIGH, AnomalySeverity.HIGH)))))
                .setCrossMetricsOperator(MetricAlertConfigurationsOperator.AND)
                .setHookIdsToAlert(Arrays.asList(hookId1, hookId2)))
            .subscribe(alertConfigurationResponse -> {
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
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createAlertConfigWithResponse#AnomalyAlertConfiguration
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#getAlertConfig(String)}
     */
    public void getAnomalyAlertConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getAlertConfig#String
        String alertConfigId = "1p0f8er30-6e6e-4391-b78f-bpfdfee1e6f5";

        metricsAdvisorAdminAsyncClient.getAlertConfig(alertConfigId)
            .subscribe(anomalyAlertConfiguration -> {
                System.out.printf("DataPoint Anomaly alert configuration Id: %s%n", anomalyAlertConfiguration.getId());
                System.out.printf("DataPoint Anomaly alert configuration description: %s%n",
                    anomalyAlertConfiguration.getDescription());
                System.out.printf("DataPoint Anomaly alert configuration hook ids: %s%n",
                    anomalyAlertConfiguration.getHookIdsToAlert());
                System.out.printf("DataPoint Anomaly alert configuration cross metrics operator: %s%n",
                    anomalyAlertConfiguration.getCrossMetricsOperator().toString());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getAlertConfig#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#getAlertConfigWithResponse(String)}
     */
    public void getAnomalyAlertConfigurationWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getAlertConfigWithResponse#String
        String alertConfigId = "1p0f8er30-6e6e-4391-b78f-bpfdfee1e6f5";

        metricsAdvisorAdminAsyncClient.getAlertConfigWithResponse(alertConfigId)
            .subscribe(alertConfigurationResponse -> {
                System.out.printf("DataPointAnomaly alert creation operation status: %s%n",
                    alertConfigurationResponse.getStatusCode());
                final AnomalyAlertConfiguration anomalyAlertConfiguration = alertConfigurationResponse.getValue();
                System.out.printf("DataPoint Anomaly alert configuration Id: %s%n", anomalyAlertConfiguration.getId());
                System.out.printf("DataPoint Anomaly alert configuration description: %s%n",
                    anomalyAlertConfiguration.getDescription());
                System.out.printf("DataPoint Anomaly alert configuration hook ids: %s%n",
                    anomalyAlertConfiguration.getHookIdsToAlert());
                System.out.printf("DataPoint Anomaly alert configuration cross metrics operator: %s%n",
                    anomalyAlertConfiguration.getCrossMetricsOperator().toString());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getAlertConfigWithResponse#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#updateAlertConfig(AnomalyAlertConfiguration)}
     */
    public void updateAnomalyAlertConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateAlertConfig#AnomalyAlertConfiguration

        String alertConfigId = "1p0f8er30-6e6e-4391-b78f-bpfdfee1e6f5";
        String additionalHookId = "2gh8er30-6e6e-4391-b78f-bpfdfee1e6f5";

        metricsAdvisorAdminAsyncClient.getAlertConfig(alertConfigId)
            .flatMap(existingAnomalyConfig -> {
                List<String> hookIds = new ArrayList<>(existingAnomalyConfig.getHookIdsToAlert());
                hookIds.add(additionalHookId);
                return metricsAdvisorAdminAsyncClient.updateAlertConfig(
                    existingAnomalyConfig
                        .setHookIdsToAlert(hookIds)
                        .setDescription("updated to add more hook ids"));
            }).subscribe(updateAnomalyAlertConfiguration -> {
                System.out.printf("Updated anomaly alert configuration Id: %s%n",
                    updateAnomalyAlertConfiguration.getId());
                System.out.printf("Updated anomaly alert configuration description: %s%n",
                    updateAnomalyAlertConfiguration.getDescription());
                System.out.printf("Updated anomaly alert configuration hook ids: %s%n",
                    updateAnomalyAlertConfiguration.getHookIdsToAlert());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateAlertConfig#AnomalyAlertConfiguration
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#updateAlertConfigWithResponse(AnomalyAlertConfiguration)}
     */
    public void updateAnomalyAlertConfigurationWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateAlertConfigWithResponse#AnomalyAlertConfiguration

        String alertConfigId = "1p0f8er30-6e6e-4391-b78f-bpfdfee1e6f5";
        String additionalHookId = "2gh8er30-6e6e-4391-b78f-bpfdfee1e6f5";

        metricsAdvisorAdminAsyncClient.getAlertConfig(alertConfigId)
            .flatMap(existingAnomalyConfig -> {
                List<String> hookIds = new ArrayList<>(existingAnomalyConfig.getHookIdsToAlert());
                hookIds.add(additionalHookId);
                return metricsAdvisorAdminAsyncClient.updateAlertConfigWithResponse(
                    existingAnomalyConfig
                        .setHookIdsToAlert(hookIds)
                        .setDescription("updated to add more hook ids"));
            }).subscribe(alertConfigurationResponse -> {
                System.out.printf("Update anomaly alert operation status: %s%n",
                    alertConfigurationResponse.getStatusCode());
                final AnomalyAlertConfiguration updatAnomalyAlertConfiguration = alertConfigurationResponse.getValue();
                System.out.printf("Updated anomaly alert configuration Id: %s%n",
                    updatAnomalyAlertConfiguration.getId());
                System.out.printf("Updated anomaly alert configuration description: %s%n",
                    updatAnomalyAlertConfiguration.getDescription());
                System.out.printf("Updated anomaly alert configuration hook ids: %s%n",
                    updatAnomalyAlertConfiguration.getHookIdsToAlert());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateAlertConfigWithResponse#AnomalyAlertConfiguration
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#deleteAlertConfig(String)}.
     */
    public void deleteAnomalyAlertConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteAlertConfig#String
        String alertConfigId = "1p0f8er30-6e6e-4391-b78f-bpfdfee1e6f5";
        metricsAdvisorAdminAsyncClient.deleteAlertConfig(alertConfigId);
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteAlertConfig#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#deleteAlertConfigWithResponse(String)}
     */
    public void deleteAnomalyAlertConfigurationWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteAlertConfigWithResponse#String
        String alertConfigId = "1p0f8er30-6e6e-4391-b78f-bpfdfee1e6f5";

        metricsAdvisorAdminAsyncClient.deleteAlertConfigWithResponse(alertConfigId)
            .subscribe(response -> {
                System.out.printf("DataPoint  Anomaly alert config delete operation status : %s%n",
                    response.getStatusCode());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteAlertConfigWithResponse#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#listAlertConfigs(String,ListAnomalyAlertConfigsOptions)}.
     */
    public void listAnomalyAlertConfigurations() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listAlertConfigs#String-ListAnomalyAlertConfigsOptions
        String detectionConfigId = "3rt98er30-6e6e-4391-b78f-bpfdfee1e6f5";
        metricsAdvisorAdminAsyncClient.listAlertConfigs(detectionConfigId, new ListAnomalyAlertConfigsOptions())
            .subscribe(anomalyAlertConfiguration -> {
                System.out.printf("DataPoint Anomaly alert configuration Id: %s%n", anomalyAlertConfiguration.getId());
                System.out.printf("DataPoint Anomaly alert configuration description: %s%n",
                    anomalyAlertConfiguration.getDescription());
                System.out.printf("DataPoint Anomaly alert configuration hook ids: %s%n",
                    anomalyAlertConfiguration.getHookIdsToAlert());
                System.out.printf("DataPoint Anomaly alert configuration cross metrics operator: %s%n",
                    anomalyAlertConfiguration.getCrossMetricsOperator().toString());
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listAlertConfigs#String-ListAnomalyAlertConfigsOptions
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#createDataSourceCredential(DataSourceCredentialEntity)}.
     */
    public void createDatasourceCredential() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createDataSourceCredential#DatasourceCredentialEntity
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

        metricsAdvisorAdminAsyncClient.createDataSourceCredential(datasourceCredential)
            .subscribe(credentialEntity -> {
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
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createDataSourceCredential#DatasourceCredentialEntity
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#createDataSourceCredentialWithResponse(DataSourceCredentialEntity)}.
     */
    public void createDatasourceCredentialWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createDataSourceCredentialWithResponse#DatasourceCredentialEntity
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

        metricsAdvisorAdminAsyncClient.createDataSourceCredentialWithResponse(datasourceCredential)
            .subscribe(credentialEntityWithResponse -> {
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
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createDataSourceCredentialWithResponse#DatasourceCredentialEntity
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#updateDataSourceCredential(DataSourceCredentialEntity)}.
     */
    public void updateDatasourceCredential() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateDataSourceCredential#DatasourceCredentialEntity
        String credentialId = "";
        metricsAdvisorAdminAsyncClient.getDataSourceCredential(credentialId)
            .flatMap(existingDatasourceCredential -> {
                DataSourceServicePrincipalInKeyVault actualCredentialSPInKV = null;
                if (existingDatasourceCredential instanceof DataSourceServicePrincipalInKeyVault) {
                    actualCredentialSPInKV  = (DataSourceServicePrincipalInKeyVault) existingDatasourceCredential;
                }
                return metricsAdvisorAdminAsyncClient.updateDataSourceCredential(
                    actualCredentialSPInKV.setDescription("set updated description"));
            })
            .subscribe(credentialEntity -> {
                if (credentialEntity instanceof DataSourceServicePrincipalInKeyVault) {
                    DataSourceServicePrincipalInKeyVault actualCredentialSPInKV
                        = (DataSourceServicePrincipalInKeyVault) credentialEntity;
                    System.out.printf("Actual credential entity key vault endpoint: %s%n",
                            actualCredentialSPInKV.getKeyVaultEndpoint());
                    System.out.printf("Actual credential entity key vault updated description: %s%n",
                        actualCredentialSPInKV.getDescription());
                }
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateDataSourceCredential#DatasourceCredentialEntity
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#updateDataSourceCredentialWithResponse(DataSourceCredentialEntity)}.
     */
    public void updateDatasourceCredentialWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateDataSourceCredentialWithResponse#DatasourceCredentialEntity
        String credentialId = "";
        metricsAdvisorAdminAsyncClient.getDataSourceCredential(credentialId)
            .flatMap(existingDatasourceCredential -> {
                DataSourceServicePrincipalInKeyVault actualCredentialSPInKV = null;
                if (existingDatasourceCredential instanceof DataSourceServicePrincipalInKeyVault) {
                    actualCredentialSPInKV  = (DataSourceServicePrincipalInKeyVault) existingDatasourceCredential;
                }
                return metricsAdvisorAdminAsyncClient.updateDataSourceCredentialWithResponse(
                    actualCredentialSPInKV.setDescription("set updated description"));
            })
            .subscribe(credentialEntityWithResponse -> {
                System.out.printf("Credential Entity creation operation status: %s%n",
                    credentialEntityWithResponse.getStatusCode());
                if (credentialEntityWithResponse.getValue() instanceof DataSourceServicePrincipalInKeyVault) {
                    DataSourceServicePrincipalInKeyVault actualCredentialSPInKV
                        = (DataSourceServicePrincipalInKeyVault) credentialEntityWithResponse.getValue();
                    System.out.printf("Actual credential entity key vault endpoint: %s%n",
                        actualCredentialSPInKV.getKeyVaultEndpoint());
                    System.out.printf("Actual credential entity key vault updated description: %s%n",
                        actualCredentialSPInKV.getDescription());
                }
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateDataSourceCredentialWithResponse#DatasourceCredentialEntity
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#getDataSourceCredential(String)}.
     */
    public void getDatasourceCredential() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getDataSourceCredential#String
        final String datasourceCredentialId = "f45668b2-bffa-11eb-8529-0246ac130003";

        metricsAdvisorAdminAsyncClient.getDataSourceCredential(datasourceCredentialId)
            .subscribe(credentialEntity -> {
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
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getDataSourceCredential#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#getDataSourceCredentialWithResponse(String)}.
     */
    public void getDatasourceCredentialWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getDataSourceCredentialWithResponse#String
        final String datasourceCredentialId = "f45668b2-bffa-11eb-8529-0246ac130003";

        metricsAdvisorAdminAsyncClient.getDataSourceCredentialWithResponse(datasourceCredentialId)
            .subscribe(credentialEntityWithResponse -> {
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
            });
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getDataSourceCredentialWithResponse#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#deleteDataSourceCredential(String)}.
     */
    public void deleteDatasourceCredential() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteDataSourceCredential#String
        final String datasourceCredentialId = "t00853f1-9080-447f-bacf-8dccf2e86f";
        metricsAdvisorAdminAsyncClient.deleteDataFeed(datasourceCredentialId);
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteDataSourceCredential#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#deleteDataSourceCredentialWithResponse(String)}
     */
    public void deleteDatasourceCredentialWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteDataSourceCredentialWithResponse#String
        final String datasourceCredentialId = "eh0854f1-8927-447f-bacf-8dccf2e86fwe";
        metricsAdvisorAdminAsyncClient.deleteDataSourceCredentialWithResponse(datasourceCredentialId)
            .subscribe(response ->
                System.out.printf("Datasource credential delete operation status : %s%n", response.getStatusCode()));
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteDataSourceCredentialWithResponse#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#listDataSourceCredentials()}
     */
    public void listDatasourceCredentials() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listDataSourceCredentials
        metricsAdvisorAdminAsyncClient.listDataSourceCredentials()
            .subscribe(datasourceCredentialEntity -> {
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
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listDataSourceCredentials
    }

    /**
     * Code snippet for {@link MetricsAdvisorAdministrationAsyncClient#listDataSourceCredentials(ListCredentialEntityOptions)} with options.
     */
    public void listDatasourceCredentialsWithOptions() {
        // BEGIN: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listDataSourceCredentials#ListCredentialEntityOptions
        metricsAdvisorAdminAsyncClient.listDataSourceCredentials(
            new ListCredentialEntityOptions()
                .setMaxPageSize(3))
            .subscribe(datasourceCredentialEntity -> {
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
        // END: com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listDataSourceCredentials#ListCredentialEntityOptions
    }
}
