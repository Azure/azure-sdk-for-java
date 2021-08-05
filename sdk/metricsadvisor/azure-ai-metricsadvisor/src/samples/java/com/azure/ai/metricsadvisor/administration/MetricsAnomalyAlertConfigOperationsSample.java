// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration;

import com.azure.ai.metricsadvisor.administration.models.AnomalyAlertConfiguration;
import com.azure.ai.metricsadvisor.administration.models.AnomalySeverity;
import com.azure.ai.metricsadvisor.administration.models.ListAnomalyAlertConfigsOptions;
import com.azure.ai.metricsadvisor.administration.models.MetricAnomalyAlertConditions;
import com.azure.ai.metricsadvisor.administration.models.MetricAlertConfiguration;
import com.azure.ai.metricsadvisor.administration.models.MetricAlertConfigurationsOperator;
import com.azure.ai.metricsadvisor.administration.models.MetricAnomalyAlertScope;
import com.azure.ai.metricsadvisor.administration.models.SeverityCondition;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Async sample demonstrates how to create, get, update, delete and list anomaly alert configurations.
 */
public class MetricsAnomalyAlertConfigOperationsSample {
    public static void main(String[] args) {
        final MetricsAdvisorAdministrationClient advisorAdministrationClient =
            new MetricsAdvisorAdministrationClientBuilder()
                .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
                .credential(new MetricsAdvisorKeyCredential("subscription_key", "api_key"))
                .buildClient();

        // Create DataPointAnomaly alert config
        System.out.printf("Creating DataPoint Anomaly alert config%n");
        String detectionConfigurationId1 = "ff3014a0-bbbb-41ec-a637-677e77b81299";
        String detectionConfigurationId2 = "e87d899d-a5a0-4259-b752-11aea34d5e34";
        String hookId1 = "5f48er30-6e6e-4391-b78f-b00dfee1e6f5";
        String hookId2 = "8i48er30-6e6e-4391-b78f-b00dfee1e6f5";

        System.out.println("Creating DataPoint Anomaly alert config%n");
        final AnomalyAlertConfiguration createdAnomalyAlertConfig =
            advisorAdministrationClient.createAlertConfig(
                new AnomalyAlertConfiguration("My Anomaly Alert config name")
                    .setDescription("alert config description")
                    .setMetricAlertConfigurations(Arrays.asList(
                        new MetricAlertConfiguration(detectionConfigurationId1,
                            MetricAnomalyAlertScope.forWholeSeries()),
                        new MetricAlertConfiguration(detectionConfigurationId2,
                            MetricAnomalyAlertScope.forWholeSeries())
                            .setAlertConditions(new MetricAnomalyAlertConditions()
                                .setSeverityRangeCondition(new SeverityCondition(AnomalySeverity.LOW, AnomalySeverity.LOW)))))
                    .setCrossMetricsOperator(MetricAlertConfigurationsOperator.AND)
                    .setHookIdsToAlert(Arrays.asList(hookId1, hookId2)));

        System.out.printf("Created DataPoint Anomaly alert config: %s%n", createdAnomalyAlertConfig.getId());

        // Retrieve the anomaly alert config that just created.
        System.out.printf("Fetching DataPoint Anomaly alert config: %s%n", createdAnomalyAlertConfig.getId());

        final AnomalyAlertConfiguration fetchAnomalyAlertConfig =
            advisorAdministrationClient.getAlertConfig(createdAnomalyAlertConfig.getId());
        System.out.printf("DataPoint Anomaly alert config Id : %s%n", fetchAnomalyAlertConfig.getId());
        System.out.printf("DataPoint Anomaly alert config name : %s%n", fetchAnomalyAlertConfig.getName());
        System.out.printf("DataPoint Anomaly alert config description : %s%n", fetchAnomalyAlertConfig.getDescription());
        System.out.println("DataPoint Anomaly alert configuration hook ids:");
        fetchAnomalyAlertConfig.getHookIdsToAlert().forEach(System.out::println);
        System.out.printf("DataPoint Anomaly alert configuration cross metrics operator: %s%n",
            fetchAnomalyAlertConfig.getCrossMetricsOperator().toString());
        System.out.println("DataFeedMetric level alert configurations for this anomaly alert config:");
        fetchAnomalyAlertConfig.getMetricAlertConfigurations().
            forEach(metricAnomalyAlertConfiguration -> {
                System.out.printf("Anomaly Alert detection configuration Id: %s%n",
                    metricAnomalyAlertConfiguration.getDetectionConfigurationId());
                System.out.printf("Anomaly Alert configuration negation value: %b%n",
                    metricAnomalyAlertConfiguration.isNegationOperationEnabled());
                System.out.printf("Anomaly Alert configuration scope type: %s%n",
                    metricAnomalyAlertConfiguration.getAlertScope().getScopeType());
            });

        // Update the anomaly alert config.
        System.out.printf("Updating anomaly alert config: %s%n", fetchAnomalyAlertConfig.getId());

        List<String> hookIds = new ArrayList<>(fetchAnomalyAlertConfig.getHookIdsToAlert());
        hookIds.remove(hookId2);

        final AnomalyAlertConfiguration updatedAnomalyAlertConfig
            = advisorAdministrationClient.updateAlertConfig(
            fetchAnomalyAlertConfig
                .setHookIdsToAlert(hookIds)
                .setDescription("updated to remove hookId2"));


        System.out.printf("Updated anomaly alert config%n");
        System.out.println("Updated anomaly alert config hook Id list:");
        updatedAnomalyAlertConfig.getHookIdsToAlert().forEach(System.out::println);

        // Delete the anomaly alert config.
        System.out.printf("Deleting anomaly alert config: %s%n", updatedAnomalyAlertConfig.getId());

        advisorAdministrationClient.deleteAlertConfig(updatedAnomalyAlertConfig.getId());
        System.out.printf("Deleted anomaly alert config%n");


        // List DataPointAnomaly alert configs.
        System.out.printf("Listing DataPoint Anomaly alert configs for a detection configurations%n");
        advisorAdministrationClient.listAlertConfigs(detectionConfigurationId1,
            new ListAnomalyAlertConfigsOptions())
            .forEach(anomalyAlertConfigurationItem -> {
                System.out.printf("DataPoint Anomaly alert config Id : %s%n", anomalyAlertConfigurationItem.getId());
                System.out.printf("DataPoint Anomaly alert config name : %s%n", anomalyAlertConfigurationItem.getName());
                System.out.printf("DataPoint Anomaly alert config description : %s%n",
                    anomalyAlertConfigurationItem.getDescription());
                System.out.println("DataPoint Anomaly alert configuration hook ids:");
                anomalyAlertConfigurationItem.getHookIdsToAlert().forEach(System.out::println);
                System.out.printf("DataPoint Anomaly alert configuration cross metrics operator: %s%n",
                    anomalyAlertConfigurationItem.getCrossMetricsOperator().toString());
            });
    }
}
