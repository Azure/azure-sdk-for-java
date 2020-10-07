// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration;

import com.azure.ai.metricsadvisor.models.AnomalyAlertConfiguration;
import com.azure.ai.metricsadvisor.models.MetricAnomalyAlertConditions;
import com.azure.ai.metricsadvisor.models.MetricAnomalyAlertConfiguration;
import com.azure.ai.metricsadvisor.models.MetricAnomalyAlertConfigurationsOperator;
import com.azure.ai.metricsadvisor.models.MetricAnomalyAlertScope;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import com.azure.ai.metricsadvisor.models.Severity;
import com.azure.ai.metricsadvisor.models.SeverityCondition;

import java.util.Arrays;

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

        // Create Anomaly alert config
        System.out.printf("Creating Anomaly alert config%n");
        String detectionConfigurationId1 = "9ol48er30-6e6e-4391-b78f-b00dfee1e6f5";
        String detectionConfigurationId2 = "3e58er30-6e6e-4391-b78f-b00dfee1e6f5";
        String hookId1 = "5f48er30-6e6e-4391-b78f-b00dfee1e6f5";
        String hookId2 = "8i48er30-6e6e-4391-b78f-b00dfee1e6f5";

        System.out.println("Creating Anomaly alert config%n");
        final AnomalyAlertConfiguration createdAnomalyAlertConfig =
            advisorAdministrationClient.createAnomalyAlertConfiguration(
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

        System.out.printf("Created Anomaly alert config: %s%n", createdAnomalyAlertConfig.getId());

        // Retrieve the anomaly alert config that just created.
        System.out.printf("Fetching Anomaly alert config: %s%n", createdAnomalyAlertConfig.getId());

        final AnomalyAlertConfiguration fetchAnomalyAlertConfig =
            advisorAdministrationClient.getAnomalyAlertConfiguration(createdAnomalyAlertConfig.getId());
        System.out.printf("Anomaly alert config Id : %s%n", fetchAnomalyAlertConfig.getId());
        System.out.printf("Anomaly alert config name : %s%n", fetchAnomalyAlertConfig.getName());
        System.out.printf("Anomaly alert config description : %s%n", fetchAnomalyAlertConfig.getDescription());
        System.out.println("Anomaly alert configuration hook ids:");
        fetchAnomalyAlertConfig.getIdOfHooksToAlert().forEach(System.out::println);
        System.out.printf("Anomaly alert configuration cross metrics operator: %s%n",
            fetchAnomalyAlertConfig.getCrossMetricsOperator().toString());
        System.out.println("Metric level alert configurations for this anomaly alert config:");
        fetchAnomalyAlertConfig.getMetricAlertConfigurations().
            forEach(metricAnomalyAlertConfiguration -> {
                System.out.printf("Alert detection configuration Id: %s%n",
                    metricAnomalyAlertConfiguration.getDetectionConfigurationId());
                System.out.printf("Alert configuration negation value",
                    metricAnomalyAlertConfiguration.isNegationOperationEnabled());
                System.out.printf("Alert configuration scope type",
                    metricAnomalyAlertConfiguration.getAlertScope().getScopeType().toString());
            });

        // Update the anomaly alert config.
        System.out.printf("Updating anomaly alert config: %s%n", fetchAnomalyAlertConfig.getId());

        final AnomalyAlertConfiguration updatedAnomalyAlertConfig
            = advisorAdministrationClient.updateAnomalyAlertConfiguration(
            fetchAnomalyAlertConfig
                .removeHookToAlert(hookId2)
                .setDescription("updated to remove hookId2"));


        System.out.printf("Updated anomaly alert config%n");
        System.out.println("Updated anomaly alert config hook Id list:");
        updatedAnomalyAlertConfig.getIdOfHooksToAlert().forEach(System.out::println);

        // Delete the anomaly alert config.
        System.out.printf("Deleting anomaly alert config: %s%n", updatedAnomalyAlertConfig.getId());

        advisorAdministrationClient.deleteAnomalyAlertConfiguration(updatedAnomalyAlertConfig.getId());
        System.out.printf("Deleted anomaly alert config%n");


        // List Anomaly alert configs.
        System.out.printf("Listing Anomaly alert configs for a detection configurations%n");
        advisorAdministrationClient.listAnomalyAlertConfigurations(detectionConfigurationId1)
            .forEach(anomalyAlertConfigurationItem -> {
                System.out.printf("Anomaly alert config Id : %s%n", anomalyAlertConfigurationItem.getId());
                System.out.printf("Anomaly alert config name : %s%n", anomalyAlertConfigurationItem.getName());
                System.out.printf("Anomaly alert config description : %s%n",
                    anomalyAlertConfigurationItem.getDescription());
                System.out.println("Anomaly alert configuration hook ids:");
                anomalyAlertConfigurationItem.getIdOfHooksToAlert().forEach(System.out::println);
                System.out.printf("Anomaly alert configuration cross metrics operator: %s%n",
                    anomalyAlertConfigurationItem.getCrossMetricsOperator().toString());
            });
    }
}
