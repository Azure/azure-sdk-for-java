// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient;
import com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClientBuilder;
import com.azure.ai.metricsadvisor.models.AnomalyAlertConfiguration;
import com.azure.ai.metricsadvisor.models.AnomalyDetectionConfiguration;
import com.azure.ai.metricsadvisor.models.AnomalyDetectorDirection;
import com.azure.ai.metricsadvisor.models.AnomalySeverity;
import com.azure.ai.metricsadvisor.models.ChangeThresholdCondition;
import com.azure.ai.metricsadvisor.models.DataFeed;
import com.azure.ai.metricsadvisor.models.DataFeedDimension;
import com.azure.ai.metricsadvisor.models.DataFeedGranularity;
import com.azure.ai.metricsadvisor.models.DataFeedGranularityType;
import com.azure.ai.metricsadvisor.models.DataFeedIngestionSettings;
import com.azure.ai.metricsadvisor.models.DataFeedMetric;
import com.azure.ai.metricsadvisor.models.DataFeedOptions;
import com.azure.ai.metricsadvisor.models.DataFeedRollupSettings;
import com.azure.ai.metricsadvisor.models.DataFeedRollupType;
import com.azure.ai.metricsadvisor.models.DataFeedSchema;
import com.azure.ai.metricsadvisor.models.DetectionConditionsOperator;
import com.azure.ai.metricsadvisor.models.EmailNotificationHook;
import com.azure.ai.metricsadvisor.models.HardThresholdCondition;
import com.azure.ai.metricsadvisor.models.ListDataFeedIngestionOptions;
import com.azure.ai.metricsadvisor.models.MetricAnomalyAlertConditions;
import com.azure.ai.metricsadvisor.models.MetricAnomalyAlertConfiguration;
import com.azure.ai.metricsadvisor.models.MetricAnomalyAlertConfigurationsOperator;
import com.azure.ai.metricsadvisor.models.MetricAnomalyAlertScope;
import com.azure.ai.metricsadvisor.models.MetricWholeSeriesDetectionCondition;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import com.azure.ai.metricsadvisor.models.MySqlDataFeedSource;
import com.azure.ai.metricsadvisor.models.NotificationHook;
import com.azure.ai.metricsadvisor.models.SQLServerDataFeedSource;
import com.azure.ai.metricsadvisor.models.SeverityCondition;
import com.azure.ai.metricsadvisor.models.SmartDetectionCondition;
import com.azure.ai.metricsadvisor.models.SuppressCondition;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.time.OffsetDateTime;
import java.util.Arrays;

import static com.azure.ai.metricsadvisor.models.DataFeedSourceType.SQL_SERVER_DB;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS ARE USED TO EXTRACT
 * APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING LINE NUMBERS OF EXISTING CODE
 * SAMPLES.
 * <p>
 * Class containing code snippets that will be injected to README.md.
 */
public class ReadmeSamples {
    private MetricsAdvisorClient metricsAdvisorClient = new MetricsAdvisorClientBuilder().buildClient();
    private MetricsAdvisorAdministrationClient metricsAdvisorAdminClient =
        new MetricsAdvisorAdministrationClientBuilder().buildClient();

    /**
     * Code snippet for getting advisor client using MetricsAdvisorKeyCredential.
     */
    public void useMetricsAdvisorKeyCredential() {
        MetricsAdvisorKeyCredential credential = new MetricsAdvisorKeyCredential("subscription_key", "api_key");
        MetricsAdvisorClient metricsAdvisorClient = new MetricsAdvisorClientBuilder()
            .endpoint("{endpoint}")
            .credential(credential)
            .buildClient();
    }

    /**
     * Code snippet for getting administration client using MetricsAdvisorKeyCredential.
     */
    public void getMetricsAdvisorAdministrationClient() {
        MetricsAdvisorKeyCredential credential = new MetricsAdvisorKeyCredential("subscription_key", "api_key");
        MetricsAdvisorAdministrationClient metricsAdvisorAdminClient =
            new MetricsAdvisorAdministrationClientBuilder()
                .endpoint("{endpoint}")
                .credential(credential)
                .buildClient();
    }

    /**
     * Code snippet for getting advisor client using AAD Authentication.
     */
    public void useAADAuthentication() {
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        MetricsAdvisorClient metricsAdvisorClient = new MetricsAdvisorClientBuilder()
            .endpoint("{endpoint}")
            .credential(credential)
            .buildClient();
    }

    /**
     * Code snippet for getting administration client using AAD Authentication.
     */
    public void metricsAdvisorAdministrationClientAAD() {
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        MetricsAdvisorAdministrationClient metricsAdvisorAdminClient =
            new MetricsAdvisorAdministrationClientBuilder()
                .endpoint("{endpoint}")
                .credential(credential)
                .buildClient();
    }

    /**
     * Code snippet for creating a data feed.
     */
    public void createDataFeed() {
        DataFeed dataFeed = new DataFeed()
            .setName("dataFeedName")
            .setSource(new MySqlDataFeedSource("conn-string", "query"))
            .setGranularity(new DataFeedGranularity().setGranularityType(DataFeedGranularityType.DAILY))
            .setSchema(new DataFeedSchema(
                Arrays.asList(
                    new DataFeedMetric().setName("cost"),
                    new DataFeedMetric().setName("revenue")
                )).setDimensions(
                Arrays.asList(
                    new DataFeedDimension().setName("city"),
                    new DataFeedDimension().setName("category")
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
                ((SQLServerDataFeedSource) createdSqlDataFeed.getSource()).getQuery());
        }
    }

    /**
     * Code snippet for checking ingestion status.
     */
    public void checkIngestionStatus() {
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
    }

    /**
     * Code snippet for configuring anomaly detection.
     */
    public void configureAnomalyDetection() {
        String metricId = "3d48er30-6e6e-4391-b78f-b00dfee1e6f5";

        ChangeThresholdCondition changeThresholdCondition = new ChangeThresholdCondition()
            .setAnomalyDetectorDirection(AnomalyDetectorDirection.BOTH)
            .setChangePercentage(20)
            .setShiftPoint(10)
            .setWithinRange(true)
            .setSuppressCondition(new SuppressCondition().setMinNumber(1).setMinRatio(2));

        HardThresholdCondition hardThresholdCondition = new HardThresholdCondition()
            .setAnomalyDetectorDirection(AnomalyDetectorDirection.DOWN)
            .setLowerBound(5.0)
            .setSuppressCondition(new SuppressCondition().setMinNumber(1).setMinRatio(1));

        SmartDetectionCondition smartDetectionCondition = new SmartDetectionCondition()
            .setAnomalyDetectorDirection(AnomalyDetectorDirection.UP)
            .setSensitivity(10.0)
            .setSuppressCondition(new SuppressCondition().setMinNumber(1).setMinRatio(2));

        final AnomalyDetectionConfiguration anomalyDetectionConfiguration =
            metricsAdvisorAdminClient.createMetricAnomalyDetectionConfig(
                metricId,
                new AnomalyDetectionConfiguration("My dataPoint anomaly detection configuration")
                    .setDescription("anomaly detection config description")
                    .setWholeSeriesDetectionCondition(
                        new MetricWholeSeriesDetectionCondition()
                            .setChangeThresholdCondition(changeThresholdCondition)
                            .setHardThresholdCondition(hardThresholdCondition)
                            .setSmartDetectionCondition(smartDetectionCondition)
                            .setCrossConditionOperator(DetectionConditionsOperator.OR))
            );
    }

    /**
     * Code snippet for creating an email hook alert.
     */
    public void createHook() {
        NotificationHook emailNotificationHook = new EmailNotificationHook("email Hook")
            .setDescription("my email Hook")
            .addEmailToAlert("alertme@alertme.com")
            .setExternalLink("https://adwiki.azurewebsites.net/articles/howto/alerts/create-hooks.html");

        final NotificationHook notificationHook = metricsAdvisorAdminClient.createHook(emailNotificationHook);
        EmailNotificationHook createdEmailHook = (EmailNotificationHook) notificationHook;
        System.out.printf("Email Hook Id: %s%n", createdEmailHook.getId());
        System.out.printf("Email Hook name: %s%n", createdEmailHook.getName());
        System.out.printf("Email Hook description: %s%n", createdEmailHook.getDescription());
        System.out.printf("Email Hook external Link: %s%n", createdEmailHook.getExternalLink());
        System.out.printf("Email Hook emails to alert: %s%n",
            String.join(",", createdEmailHook.getEmailsToAlert()));
    }

    /**
     * Code snippet for configuring alert.
     */
    public void configureAlert() {
        String detectionConfigurationId1 = "9ol48er30-6e6e-4391-b78f-b00dfee1e6f5";
        String detectionConfigurationId2 = "3e58er30-6e6e-4391-b78f-b00dfee1e6f5";
        String hookId1 = "5f48er30-6e6e-4391-b78f-b00dfee1e6f5";
        String hookId2 = "8i48er30-6e6e-4391-b78f-b00dfee1e6f5";

        final AnomalyAlertConfiguration anomalyAlertConfiguration
            = metricsAdvisorAdminClient.createAnomalyAlertConfig(
                new AnomalyAlertConfiguration("My anomaly alert config name")
                    .setDescription("alert config description")
                    .setMetricAlertConfigurations(
                        Arrays.asList(
                            new MetricAnomalyAlertConfiguration(detectionConfigurationId1,
                                MetricAnomalyAlertScope.forWholeSeries()),
                            new MetricAnomalyAlertConfiguration(detectionConfigurationId2,
                                MetricAnomalyAlertScope.forWholeSeries())
                                .setAlertConditions(new MetricAnomalyAlertConditions()
                                    .setSeverityRangeCondition(new SeverityCondition()
                                        .setMaxAlertSeverity(AnomalySeverity.HIGH)))
                        ))
                    .setCrossMetricsOperator(MetricAnomalyAlertConfigurationsOperator.AND)
                    .setIdOfHooksToAlert(Arrays.asList(hookId1, hookId2)));
    }

    /**
     * Code snippet for querying anomaly detection.
     */
    public void queryAlertsForDetection() {
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
    }

    /**
     * Code snippet for handling exception
     */
    public void handlingException() {
        try {
            metricsAdvisorClient.getFeedback("non_existing_feedback_id");
        } catch (HttpResponseException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Code snippet for getting async client using the MetricsAdvisorKeyCredential authentication.
     */
    public void useMetricsAdvisorKeyCredentialAsyncClient() {
        MetricsAdvisorKeyCredential credential = new MetricsAdvisorKeyCredential("subscription_key", "api_key");
        MetricsAdvisorAsyncClient metricsAdvisorAsyncClient = new MetricsAdvisorClientBuilder()
            .credential(credential)
            .endpoint("{endpoint}")
            .buildAsyncClient();
    }
}
