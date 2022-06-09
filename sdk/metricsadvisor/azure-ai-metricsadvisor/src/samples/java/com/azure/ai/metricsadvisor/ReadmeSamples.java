// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient;
import com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClientBuilder;
import com.azure.ai.metricsadvisor.administration.models.AnomalyAlertConfiguration;
import com.azure.ai.metricsadvisor.administration.models.AnomalyDetectionConfiguration;
import com.azure.ai.metricsadvisor.administration.models.AnomalyDetectorDirection;
import com.azure.ai.metricsadvisor.administration.models.AnomalySeverity;
import com.azure.ai.metricsadvisor.administration.models.ChangeThresholdCondition;
import com.azure.ai.metricsadvisor.administration.models.DataFeed;
import com.azure.ai.metricsadvisor.administration.models.DataFeedDimension;
import com.azure.ai.metricsadvisor.administration.models.DataFeedGranularity;
import com.azure.ai.metricsadvisor.administration.models.DataFeedGranularityType;
import com.azure.ai.metricsadvisor.administration.models.DataFeedIngestionSettings;
import com.azure.ai.metricsadvisor.administration.models.DataFeedMetric;
import com.azure.ai.metricsadvisor.administration.models.DataFeedOptions;
import com.azure.ai.metricsadvisor.administration.models.DataFeedRollupSettings;
import com.azure.ai.metricsadvisor.administration.models.DataFeedRollupType;
import com.azure.ai.metricsadvisor.administration.models.DataFeedSchema;
import com.azure.ai.metricsadvisor.administration.models.DetectionConditionOperator;
import com.azure.ai.metricsadvisor.administration.models.EmailNotificationHook;
import com.azure.ai.metricsadvisor.administration.models.HardThresholdCondition;
import com.azure.ai.metricsadvisor.administration.models.ListDataFeedIngestionOptions;
import com.azure.ai.metricsadvisor.administration.models.MetricAnomalyAlertConditions;
import com.azure.ai.metricsadvisor.administration.models.MetricAlertConfiguration;
import com.azure.ai.metricsadvisor.administration.models.MetricAlertConfigurationsOperator;
import com.azure.ai.metricsadvisor.administration.models.MetricAnomalyAlertScope;
import com.azure.ai.metricsadvisor.administration.models.MetricWholeSeriesDetectionCondition;
import com.azure.ai.metricsadvisor.administration.models.SeverityCondition;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import com.azure.ai.metricsadvisor.administration.models.MySqlDataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.NotificationHook;
import com.azure.ai.metricsadvisor.administration.models.SmartDetectionCondition;
import com.azure.ai.metricsadvisor.administration.models.SqlServerDataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.SuppressCondition;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;

import static com.azure.ai.metricsadvisor.administration.models.DataFeedSourceType.SQL_SERVER_DB;

/**
 * Class containing code snippets that will be injected to README.md.
 */
@SuppressWarnings("unused")
public class ReadmeSamples {
    private MetricsAdvisorClient metricsAdvisorClient = new MetricsAdvisorClientBuilder().buildClient();
    private MetricsAdvisorAdministrationClient metricsAdvisorAdminClient =
        new MetricsAdvisorAdministrationClientBuilder().buildClient();

    /**
     * Code snippet for getting advisor client using MetricsAdvisorKeyCredential.
     */
    public void useMetricsAdvisorKeyCredential() {
        // BEGIN: readme-sample-createMetricsAdvisorClient
        MetricsAdvisorKeyCredential credential = new MetricsAdvisorKeyCredential("subscription_key", "api_key");
        MetricsAdvisorClient metricsAdvisorClient = new MetricsAdvisorClientBuilder()
            .endpoint("{endpoint}")
            .credential(credential)
            .buildClient();
        // END: readme-sample-createMetricsAdvisorClient
    }

    /**
     * Code snippet for getting administration client using MetricsAdvisorKeyCredential.
     */
    public void getMetricsAdvisorAdministrationClient() {
        // BEGIN: readme-sample-createMetricsAdvisorAdministrationClient
        MetricsAdvisorKeyCredential credential = new MetricsAdvisorKeyCredential("subscription_key", "api_key");
        MetricsAdvisorAdministrationClient metricsAdvisorAdminClient =
            new MetricsAdvisorAdministrationClientBuilder()
                .endpoint("{endpoint}")
                .credential(credential)
                .buildClient();
        // END: readme-sample-createMetricsAdvisorAdministrationClient
    }

    /**
     * Code snippet for getting advisor client using AAD Authentication.
     */
    public void useAADAuthentication() {
        // BEGIN: readme-sample-createMetricsAdvisorClientWithAAD
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        MetricsAdvisorClient metricsAdvisorClient = new MetricsAdvisorClientBuilder()
            .endpoint("{endpoint}")
            .credential(credential)
            .buildClient();
        // END: readme-sample-createMetricsAdvisorClientWithAAD
    }

    /**
     * Code snippet for getting administration client using AAD Authentication.
     */
    public void metricsAdvisorAdministrationClientAAD() {
        // BEGIN: readme-sample-createMetricsAdvisorAdministrationClientWithAAD
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        MetricsAdvisorAdministrationClient metricsAdvisorAdminClient =
            new MetricsAdvisorAdministrationClientBuilder()
                .endpoint("{endpoint}")
                .credential(credential)
                .buildClient();
        // END: readme-sample-createMetricsAdvisorAdministrationClientWithAAD
    }

    /**
     * Code snippet for creating a data feed.
     */
    public void createDataFeed() {
        // BEGIN: readme-sample-createDataFeed
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
        final DataFeed createdSqlDataFeed = metricsAdvisorAdminClient.createDataFeed(dataFeed);

        System.out.printf("Data feed Id : %s%n", createdSqlDataFeed.getId());
        System.out.printf("Data feed name : %s%n", createdSqlDataFeed.getName());
        System.out.printf("Is the query user is one of data feed administrator : %s%n", createdSqlDataFeed.isAdmin());
        System.out.printf("Data feed created time : %s%n", createdSqlDataFeed.getCreatedTime());
        System.out.printf("Data feed granularity type : %s%n",
            createdSqlDataFeed.getGranularity().getGranularityType());
        System.out.printf("Data feed granularity value : %d%n",
            createdSqlDataFeed.getGranularity().getCustomGranularityValue());
        System.out.println("Data feed related metric Ids:");
        dataFeed.getMetricIds().forEach((metricId, metricName)
            -> System.out.printf("Metric Id : %s, Metric Name: %s%n", metricId, metricName));
        System.out.printf("Data feed source type: %s%n", createdSqlDataFeed.getSourceType());

        if (SQL_SERVER_DB == createdSqlDataFeed.getSourceType()) {
            System.out.printf("Data feed sql server query: %s%n",
                ((SqlServerDataFeedSource) createdSqlDataFeed.getSource()).getQuery());
        }
        // END: readme-sample-createDataFeed
    }

    /**
     * Code snippet for checking ingestion status.
     */
    public void checkIngestionStatus() {
        // BEGIN: readme-sample-checkIngestionStatus
        String dataFeedId = "3d48er30-6e6e-4391-b78f-b00dfee1e6f5";

        metricsAdvisorAdminClient.listDataFeedIngestionStatus(
            dataFeedId,
            new ListDataFeedIngestionOptions(
                OffsetDateTime.parse("2020-01-01T00:00:00Z"),
                OffsetDateTime.parse("2020-09-09T00:00:00Z"))
        ).forEach(dataFeedIngestionStatus -> {
            System.out.printf("Message : %s%n", dataFeedIngestionStatus.getMessage());
            System.out.printf("Timestamp value : %s%n", dataFeedIngestionStatus.getTimestamp());
            System.out.printf("Status : %s%n", dataFeedIngestionStatus.getStatus());
        });
        // END: readme-sample-checkIngestionStatus
    }

    /**
     * Code snippet for configuring anomaly detection.
     */
    public void configureAnomalyDetection() {
        // BEGIN: readme-sample-createAnomalyDetectionConfiguration
        String metricId = "3d48er30-6e6e-4391-b78f-b00dfee1e6f5";

        ChangeThresholdCondition changeThresholdCondition = new ChangeThresholdCondition(
            20,
            10,
            true,
            AnomalyDetectorDirection.BOTH,
            new SuppressCondition(1, 2));

        HardThresholdCondition hardThresholdCondition = new HardThresholdCondition(
            AnomalyDetectorDirection.DOWN,
            new SuppressCondition(1, 1))
            .setLowerBound(5.0);

        SmartDetectionCondition smartDetectionCondition = new SmartDetectionCondition(
            10.0,
            AnomalyDetectorDirection.UP,
            new SuppressCondition(1, 2));

        final AnomalyDetectionConfiguration anomalyDetectionConfiguration =
            metricsAdvisorAdminClient.createDetectionConfig(
                metricId,
                new AnomalyDetectionConfiguration("My dataPoint anomaly detection configuration")
                    .setDescription("anomaly detection config description")
                    .setWholeSeriesDetectionCondition(
                        new MetricWholeSeriesDetectionCondition()
                            .setChangeThresholdCondition(changeThresholdCondition)
                            .setHardThresholdCondition(hardThresholdCondition)
                            .setSmartDetectionCondition(smartDetectionCondition)
                            .setConditionOperator(DetectionConditionOperator.OR))
            );
        // END: readme-sample-createAnomalyDetectionConfiguration
    }

    /**
     * Code snippet for creating an email hook alert.
     */
    public void createHook() {
        // BEGIN: readme-sample-createHook
        NotificationHook emailNotificationHook = new EmailNotificationHook("email Hook")
            .setDescription("my email Hook")
            .setEmailsToAlert(Collections.singletonList("alertme@alertme.com"))
            .setExternalLink("https://adwiki.azurewebsites.net/articles/howto/alerts/create-hooks.html");

        final NotificationHook notificationHook = metricsAdvisorAdminClient.createHook(emailNotificationHook);
        EmailNotificationHook createdEmailHook = (EmailNotificationHook) notificationHook;
        System.out.printf("Email Hook Id: %s%n", createdEmailHook.getId());
        System.out.printf("Email Hook name: %s%n", createdEmailHook.getName());
        System.out.printf("Email Hook description: %s%n", createdEmailHook.getDescription());
        System.out.printf("Email Hook external Link: %s%n", createdEmailHook.getExternalLink());
        System.out.printf("Email Hook emails to alert: %s%n",
            String.join(",", createdEmailHook.getEmailsToAlert()));
        // END: readme-sample-createHook
    }

    /**
     * Code snippet for configuring alert.
     */
    public void configureAlert() {
        // BEGIN: readme-sample-createAnomalyAlertConfiguration
        String detectionConfigurationId1 = "9ol48er30-6e6e-4391-b78f-b00dfee1e6f5";
        String detectionConfigurationId2 = "3e58er30-6e6e-4391-b78f-b00dfee1e6f5";
        String hookId1 = "5f48er30-6e6e-4391-b78f-b00dfee1e6f5";
        String hookId2 = "8i48er30-6e6e-4391-b78f-b00dfee1e6f5";

        final AnomalyAlertConfiguration anomalyAlertConfiguration
            = metricsAdvisorAdminClient.createAlertConfig(
                new AnomalyAlertConfiguration("My anomaly alert config name")
                    .setDescription("alert config description")
                    .setMetricAlertConfigurations(
                        Arrays.asList(
                            new MetricAlertConfiguration(detectionConfigurationId1,
                                MetricAnomalyAlertScope.forWholeSeries()),
                            new MetricAlertConfiguration(detectionConfigurationId2,
                                MetricAnomalyAlertScope.forWholeSeries())
                                .setAlertConditions(new MetricAnomalyAlertConditions()
                                    .setSeverityRangeCondition(new SeverityCondition(AnomalySeverity.HIGH,
                                        AnomalySeverity.HIGH)))
                        ))
                    .setCrossMetricsOperator(MetricAlertConfigurationsOperator.AND)
                    .setHookIdsToAlert(Arrays.asList(hookId1, hookId2)));
        // END: readme-sample-createAnomalyAlertConfiguration
    }

    /**
     * Code snippet for querying anomaly detection.
     */
    public void queryAlertsForDetection() {
        // BEGIN: readme-sample-listAnomaliesForAlert
        String alertConfigurationId = "9ol48er30-6e6e-4391-b78f-b00dfee1e6f5";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        metricsAdvisorClient.listAlerts(
            alertConfigurationId,
                startTime, endTime)
            .forEach(alert -> {
                System.out.printf("AnomalyAlert Id: %s%n", alert.getId());
                System.out.printf("AnomalyAlert created on: %s%n", alert.getCreatedTime());

                // List anomalies for returned alerts
                metricsAdvisorClient.listAnomaliesForAlert(
                    alertConfigurationId,
                    alert.getId())
                    .forEach(anomaly -> {
                        System.out.printf("DataPoint Anomaly was created on: %s%n", anomaly.getCreatedTime());
                        System.out.printf("DataPoint Anomaly severity: %s%n", anomaly.getSeverity().toString());
                        System.out.printf("DataPoint Anomaly status: %s%n", anomaly.getStatus());
                        System.out.printf("DataPoint Anomaly related series key: %s%n", anomaly.getSeriesKey().asMap());
                    });
            });
        // END: readme-sample-listAnomaliesForAlert
    }

    /**
     * Code snippet for handling exception
     */
    public void handlingException() {
        // BEGIN: readme-sample-handlingException
        try {
            metricsAdvisorClient.getFeedback("non_existing_feedback_id");
        } catch (HttpResponseException e) {
            System.out.println(e.getMessage());
        }
        // END: readme-sample-handlingException
    }

    /**
     * Code snippet for getting async client using the MetricsAdvisorKeyCredential authentication.
     */
    public void useMetricsAdvisorKeyCredentialAsyncClient() {
        // BEGIN: readme-sample-asyncClient
        MetricsAdvisorKeyCredential credential = new MetricsAdvisorKeyCredential("subscription_key", "api_key");
        MetricsAdvisorAsyncClient metricsAdvisorAsyncClient = new MetricsAdvisorClientBuilder()
            .credential(credential)
            .endpoint("{endpoint}")
            .buildAsyncClient();
        // END: readme-sample-asyncClient
    }
}
