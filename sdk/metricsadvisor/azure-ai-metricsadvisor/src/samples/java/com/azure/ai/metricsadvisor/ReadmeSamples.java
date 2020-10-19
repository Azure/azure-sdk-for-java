// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient;
import com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClientBuilder;
import com.azure.ai.metricsadvisor.models.AnomalyAlertConfiguration;
import com.azure.ai.metricsadvisor.models.AnomalyDetectionConfiguration;
import com.azure.ai.metricsadvisor.models.AnomalyDetectorDirection;
import com.azure.ai.metricsadvisor.models.ChangeThresholdCondition;
import com.azure.ai.metricsadvisor.models.DataFeed;
import com.azure.ai.metricsadvisor.models.DataFeedAccessMode;
import com.azure.ai.metricsadvisor.models.DataFeedAutoRollUpMethod;
import com.azure.ai.metricsadvisor.models.DataFeedGranularity;
import com.azure.ai.metricsadvisor.models.DataFeedGranularityType;
import com.azure.ai.metricsadvisor.models.DataFeedIngestionSettings;
import com.azure.ai.metricsadvisor.models.DataFeedMissingDataPointFillSettings;
import com.azure.ai.metricsadvisor.models.DataFeedOptions;
import com.azure.ai.metricsadvisor.models.DataFeedRollupSettings;
import com.azure.ai.metricsadvisor.models.DataFeedSchema;
import com.azure.ai.metricsadvisor.models.DataSourceMissingDataPointFillType;
import com.azure.ai.metricsadvisor.models.DetectionConditionsOperator;
import com.azure.ai.metricsadvisor.models.Dimension;
import com.azure.ai.metricsadvisor.models.EmailHook;
import com.azure.ai.metricsadvisor.models.HardThresholdCondition;
import com.azure.ai.metricsadvisor.models.Hook;
import com.azure.ai.metricsadvisor.models.ListAlertOptions;
import com.azure.ai.metricsadvisor.models.ListDataFeedIngestionOptions;
import com.azure.ai.metricsadvisor.models.Metric;
import com.azure.ai.metricsadvisor.models.MetricAnomalyAlertConditions;
import com.azure.ai.metricsadvisor.models.MetricAnomalyAlertConfiguration;
import com.azure.ai.metricsadvisor.models.MetricAnomalyAlertConfigurationsOperator;
import com.azure.ai.metricsadvisor.models.MetricAnomalyAlertScope;
import com.azure.ai.metricsadvisor.models.MetricWholeSeriesDetectionCondition;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import com.azure.ai.metricsadvisor.models.SQLServerDataFeedSource;
import com.azure.ai.metricsadvisor.models.Severity;
import com.azure.ai.metricsadvisor.models.SeverityCondition;
import com.azure.ai.metricsadvisor.models.SmartDetectionCondition;
import com.azure.ai.metricsadvisor.models.SuppressCondition;
import com.azure.ai.metricsadvisor.models.TimeMode;
import com.azure.core.exception.HttpResponseException;

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
    private MetricsAdvisorAdministrationClient metricsAdvisorAdministrationClient =
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
        MetricsAdvisorAdministrationClient metricsAdvisorAdministrationClient =
            new MetricsAdvisorAdministrationClientBuilder()
                .endpoint("{endpoint}")
                .credential(credential)
                .buildClient();
    }

    /**
     * Code snippet for creating a data feed.
     */
    public void createDataFeed() {
        final DataFeed createdSqlDataFeed = metricsAdvisorAdministrationClient.createDataFeed(
            "My data feed name",
            new SQLServerDataFeedSource("sql_server_connection_string", "query"),
            new DataFeedGranularity().setGranularityType(DataFeedGranularityType.DAILY),
            new DataFeedSchema(Arrays.asList(
                new Metric().setName("cost"),
                new Metric().setName("revenue")))
                .setDimensions(Arrays.asList(
                    new Dimension().setName("category"),
                    new Dimension().setName("city"))),
            new DataFeedIngestionSettings(OffsetDateTime.parse("2020-01-01T00:00:00Z")),
            new DataFeedOptions()
                .setDescription("My data feed description")
                .setRollupSettings(
                    new DataFeedRollupSettings()
                        .setAutoRollup(DataFeedAutoRollUpMethod.SUM, Arrays.asList("cost"), "__CUSTOM_SUM__"))
                .setMissingDataPointFillSettings(
                    new DataFeedMissingDataPointFillSettings()
                        .setFillType(DataSourceMissingDataPointFillType.SMART_FILLING))
                .setAccessMode(DataFeedAccessMode.PUBLIC));

        System.out.printf("Data feed Id : %s%n", createdSqlDataFeed.getId());
        System.out.printf("Data feed name : %s%n", createdSqlDataFeed.getName());
        System.out.printf("Is the query user is one of data feed administrator : %s%n", createdSqlDataFeed.isAdmin());
        System.out.printf("Data feed created time : %s%n", createdSqlDataFeed.getCreatedTime());
        System.out.printf("Data feed granularity type : %s%n",
            createdSqlDataFeed.getGranularity().getGranularityType());
        System.out.printf("Data feed granularity value : %d%n",
            createdSqlDataFeed.getGranularity().getCustomGranularityValue());
        System.out.println("Data feed related metric Ids:");
        createdSqlDataFeed.getMetricIds().forEach(metricId -> System.out.println(metricId));
        System.out.printf("Data feed source type: %s%n", createdSqlDataFeed.getSourceType());

        if (SQL_SERVER_DB.equals(createdSqlDataFeed.getSourceType())) {
            System.out.printf("Data feed sql server query: %s%n",
                ((SQLServerDataFeedSource) createdSqlDataFeed.getSource()).getQuery());
        }
    }

    /**
     * Code snippet for checking ingestion status.
     */
    public void checkIngestionStatus() {

        String dataFeedId = "3d48er30-6e6e-4391-b78f-b00dfee1e6f5";

        metricsAdvisorAdministrationClient.listDataFeedIngestionStatus(
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
            metricsAdvisorAdministrationClient.createMetricAnomalyDetectionConfiguration(
                metricId,
                new AnomalyDetectionConfiguration("My Anomaly detection configuration")
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
        Hook emailHook = new EmailHook("email hook")
            .setDescription("my email hook")
            .addEmailToAlert("alertme@alertme.com")
            .setExternalLink("https://adwiki.azurewebsites.net/articles/howto/alerts/create-hooks.html");

        final Hook hook = metricsAdvisorAdministrationClient.createHook(emailHook);
        EmailHook createdEmailHook = (EmailHook) hook;
        System.out.printf("Hook Id: %s%n", createdEmailHook.getId());
        System.out.printf("Hook Name: %s%n", createdEmailHook.getName());
        System.out.printf("Hook Description: %s%n", createdEmailHook.getDescription());
        System.out.printf("Hook External Link: %s%n", createdEmailHook.getExternalLink());
        System.out.printf("Hook Emails: %s%n", String.join(",", createdEmailHook.getEmailsToAlert()));
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
            = metricsAdvisorAdministrationClient.createAnomalyAlertConfiguration(
                new AnomalyAlertConfiguration("My Alert config name")
                    .setDescription("alert config description")
                    .setMetricAlertConfigurations(
                        Arrays.asList(
                            new MetricAnomalyAlertConfiguration(detectionConfigurationId1,
                                MetricAnomalyAlertScope.forWholeSeries()),
                            new MetricAnomalyAlertConfiguration(detectionConfigurationId2,
                                MetricAnomalyAlertScope.forWholeSeries())
                                .setAlertConditions(new MetricAnomalyAlertConditions()
                                    .setSeverityCondition(new SeverityCondition()
                                        .setMaxAlertSeverity(Severity.HIGH)))
                        ))
                    .setCrossMetricsOperator(MetricAnomalyAlertConfigurationsOperator.AND)
                    .setIdOfHooksToAlert(Arrays.asList(hookId1, hookId2)));
    }

    /**
     * Code snippet for querying anomaly detection.
     */
    public void queryAnomalyDetection() {
        String alertConfigurationId = "9ol48er30-6e6e-4391-b78f-b00dfee1e6f5";
        metricsAdvisorClient.listAlerts(
            alertConfigurationId,
            new ListAlertOptions(OffsetDateTime.parse("2020-01-01T00:00:00Z"),
                OffsetDateTime.now(),
                TimeMode.ANOMALY_TIME))
            .forEach(alert -> {
                System.out.printf("Alert Id: %s%n", alert.getId());
                System.out.printf("Alert created on: %s%n", alert.getCreatedTime());

                // List anomalies for returned alerts
                metricsAdvisorClient.listAnomaliesForAlert(
                    alertConfigurationId,
                    alert.getId())
                    .forEach(anomaly -> {
                        System.out.printf("Anomaly was created on: %s%n", anomaly.getCreatedTime());
                        System.out.printf("Anomaly severity: %s%n", anomaly.getSeverity().toString());
                        System.out.printf("Anomaly status: %s%n", anomaly.getStatus());
                        System.out.printf("Anomaly related series key: %s%n", anomaly.getSeriesKey().asMap());
                    });
            });
    }

    /**
     * Code snippet for handling exception
     */
    public void handlingException() {
        try {
            metricsAdvisorClient.getMetricFeedback("non_existing_feedback_id");
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
