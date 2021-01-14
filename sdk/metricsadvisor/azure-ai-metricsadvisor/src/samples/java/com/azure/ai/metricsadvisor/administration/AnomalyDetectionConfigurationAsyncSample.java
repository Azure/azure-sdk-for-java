// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration;

import com.azure.ai.metricsadvisor.models.AnomalyDetectionConfiguration;
import com.azure.ai.metricsadvisor.models.AnomalyDetectorDirection;
import com.azure.ai.metricsadvisor.models.ChangeThresholdCondition;
import com.azure.ai.metricsadvisor.models.DetectionConditionsOperator;
import com.azure.ai.metricsadvisor.models.DimensionKey;
import com.azure.ai.metricsadvisor.models.HardThresholdCondition;
import com.azure.ai.metricsadvisor.models.MetricSeriesGroupDetectionCondition;
import com.azure.ai.metricsadvisor.models.MetricSingleSeriesDetectionCondition;
import com.azure.ai.metricsadvisor.models.MetricWholeSeriesDetectionCondition;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import com.azure.ai.metricsadvisor.models.SmartDetectionCondition;
import com.azure.ai.metricsadvisor.models.SuppressCondition;
import com.azure.core.http.rest.PagedFlux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * Sample demonstrates how to create, get, update delete and list anomaly detection configuration.
 */
public class AnomalyDetectionConfigurationAsyncSample {
    public static void main(String[] args) {
        final MetricsAdvisorAdministrationAsyncClient advisorAdministrationAsyncClient =
            new MetricsAdvisorAdministrationClientBuilder()
                .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
                .credential(new MetricsAdvisorKeyCredential("subscription_key", "api_key"))
                .buildAsyncClient();

        final String metricId = "0b836da8-10e6-46cd-8f4f-28262e113a62";

        // Create the detection configuration.
        Mono<AnomalyDetectionConfiguration> createDetectionConfigMono
            = advisorAdministrationAsyncClient
            .createMetricAnomalyDetectionConfig(metricId, prepareDetectionConfigurationObject());

        createDetectionConfigMono
            .doOnSubscribe(__ ->
                System.out.printf("Creating detection configuration for metric: %s%n", metricId))
            .doOnSuccess(config ->
                System.out.printf("Created detection configuration: %s%n", config.getId()));

        // Retrieve the detection configuration that just created.
        Mono<AnomalyDetectionConfiguration> fetchDetectionConfigMono = createDetectionConfigMono
            .flatMap(createdDetectionConfig -> {
                return advisorAdministrationAsyncClient.getMetricAnomalyDetectionConfig(
                    createdDetectionConfig.getId())
                    .doOnSubscribe(__ ->
                        System.out.printf("Fetching detection configuration: %s%n", createdDetectionConfig.getId()))
                    .doOnSuccess(config ->
                        System.out.printf("Fetched detection configuration%n"))
                    .doOnNext(detectionConfig -> {
                        printDetectionConfiguration(detectionConfig);
                    });
            });

        // Update the detection configuration.
        Mono<AnomalyDetectionConfiguration> updateDetectionConfigMono = fetchDetectionConfigMono
            .flatMap(detectionConfig -> {
                final String detectionConfigId = detectionConfig.getId();
                detectionConfig = updateDetectionConfigurationObject(detectionConfig);
                return advisorAdministrationAsyncClient.updateMetricAnomalyDetectionConfig(detectionConfig)
                    .doOnSubscribe(__ ->
                        System.out.printf("Updating detection configuration: %s%n", detectionConfigId))
                    .doOnSuccess(config ->
                        System.out.printf("Updated detection configuration%n"));
            });

        // List configurations
        System.out.printf("Listing detection configurations%n");
        PagedFlux<AnomalyDetectionConfiguration> detectionConfigsFlux
            = advisorAdministrationAsyncClient.listMetricAnomalyDetectionConfigs(metricId);

        detectionConfigsFlux.doOnNext(detectionConfig -> printDetectionConfiguration(detectionConfig))
            .blockLast();

        // Delete the detection configuration.
        Mono<Void> deleteDetectionConfigMono = updateDetectionConfigMono.flatMap(detectionConfig -> {
            return advisorAdministrationAsyncClient.deleteMetricAnomalyDetectionConfiguration(detectionConfig.getId())
                .doOnSubscribe(__ ->
                    System.out.printf("Deleting detection configuration: %s%n", detectionConfig.getId()))
                .doOnSuccess(config ->
                    System.out.printf("Deleted detection configuration%n"));
        });

        /*
         This will block until all the above CRUD on operation on detection is completed.
         This is strongly discouraged for use in production as it eliminates the benefits of asynchronous IO.
          It is used here to ensure the sample runs to completion.
        */
        deleteDetectionConfigMono.block();
    }

    private static AnomalyDetectionConfiguration prepareDetectionConfigurationObject() {
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
        return detectionConfig;
    }

    private static AnomalyDetectionConfiguration
        updateDetectionConfigurationObject(AnomalyDetectionConfiguration detectionConfig) {
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
        return detectionConfig;
    }

    private static void printDetectionConfiguration(AnomalyDetectionConfiguration detectionConfig) {
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
    }
}
