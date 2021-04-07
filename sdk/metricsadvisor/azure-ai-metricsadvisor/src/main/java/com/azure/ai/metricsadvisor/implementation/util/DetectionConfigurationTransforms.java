// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.implementation.models.AnomalyDetectionConfigurationLogicType;
import com.azure.ai.metricsadvisor.implementation.models.AnomalyDetectionConfigurationPatch;
import com.azure.ai.metricsadvisor.implementation.models.DimensionGroupConfiguration;
import com.azure.ai.metricsadvisor.implementation.models.DimensionGroupIdentity;
import com.azure.ai.metricsadvisor.implementation.models.SeriesConfiguration;
import com.azure.ai.metricsadvisor.implementation.models.SeriesIdentity;
import com.azure.ai.metricsadvisor.implementation.models.WholeMetricConfiguration;
import com.azure.ai.metricsadvisor.models.DetectionConditionsOperator;
import com.azure.ai.metricsadvisor.models.DimensionKey;
import com.azure.ai.metricsadvisor.models.MetricWholeSeriesDetectionCondition;
import com.azure.ai.metricsadvisor.models.AnomalyDetectionConfiguration;
import com.azure.ai.metricsadvisor.models.MetricSeriesGroupDetectionCondition;
import com.azure.ai.metricsadvisor.models.MetricSingleSeriesDetectionCondition;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Expose transformation methods to transform {@link com.azure.ai.metricsadvisor.models.AnomalyDetectionConfiguration}
 * model to REST API wire model and vice-versa.
 */
public final class DetectionConfigurationTransforms {
    /**
     * Transform configuration wire model to
     * {@link com.azure.ai.metricsadvisor.implementation.models.AnomalyDetectionConfiguration}.
     *
     * @param innerConfiguration The wire model instance.
     * @return The custom model instance.
     */
    public static AnomalyDetectionConfiguration
        fromInner(com.azure.ai.metricsadvisor.implementation.models.AnomalyDetectionConfiguration innerConfiguration) {
        AnomalyDetectionConfiguration configuration
            = new AnomalyDetectionConfiguration(innerConfiguration.getName());

        AnomalyDetectionConfigurationHelper.setId(configuration,
            innerConfiguration.getAnomalyDetectionConfigurationId().toString());
        AnomalyDetectionConfigurationHelper.setMetricId(configuration,
            innerConfiguration.getMetricId().toString());

        configuration.setDescription(innerConfiguration.getDescription());

        WholeMetricConfiguration innerWholeSeriesConditions = innerConfiguration.getWholeMetricConfiguration();
        if (innerWholeSeriesConditions != null) {
            MetricWholeSeriesDetectionCondition wholeSeriesConditions
                = new MetricWholeSeriesDetectionCondition();
            if (innerWholeSeriesConditions.getConditionOperator() != null) {
                wholeSeriesConditions.setCrossConditionOperator(
                    DetectionConditionsOperator.fromString(innerWholeSeriesConditions
                        .getConditionOperator()
                        .toString()));
            }

            wholeSeriesConditions
                .setSmartDetectionCondition(innerWholeSeriesConditions.getSmartDetectionCondition())
                .setChangeThresholdCondition(innerWholeSeriesConditions.getChangeThresholdCondition())
                .setHardThresholdCondition(innerWholeSeriesConditions.getHardThresholdCondition());

            configuration.setWholeSeriesDetectionCondition(wholeSeriesConditions);
        }

        List<DimensionGroupConfiguration> innerSeriesGroupConfigurationsList
            = innerConfiguration.getDimensionGroupOverrideConfigurations();

        if (innerSeriesGroupConfigurationsList != null) {
            innerSeriesGroupConfigurationsList.forEach(innerSeriesGroupConfiguration -> {
                DimensionKey groupDimensionKey;
                if (innerSeriesGroupConfiguration.getGroup().getDimension() != null) {
                    groupDimensionKey = new DimensionKey(innerSeriesGroupConfiguration
                        .getGroup()
                        .getDimension());
                } else {
                    groupDimensionKey = new DimensionKey();
                }

                MetricSeriesGroupDetectionCondition seriesGroupCondition
                    = new MetricSeriesGroupDetectionCondition(groupDimensionKey);


                if (innerSeriesGroupConfiguration.getConditionOperator() != null) {
                    seriesGroupCondition.setCrossConditionOperator(DetectionConditionsOperator.fromString(
                        innerSeriesGroupConfiguration
                            .getConditionOperator()
                            .toString()));
                }

                seriesGroupCondition
                    .setSmartDetectionCondition(innerSeriesGroupConfiguration.getSmartDetectionCondition())
                    .setChangeThresholdCondition(innerSeriesGroupConfiguration.getChangeThresholdCondition())
                    .setHardThresholdCondition(innerSeriesGroupConfiguration.getHardThresholdCondition());

                configuration.addSeriesGroupDetectionCondition(seriesGroupCondition);

            });
        }

        List<SeriesConfiguration> innerSeriesConfigurationsList
            = innerConfiguration.getSeriesOverrideConfigurations();

        if (innerSeriesConfigurationsList != null) {
            innerSeriesConfigurationsList.forEach(innerSeriesConfiguration -> {
                DimensionKey groupDimensionKey;
                if (innerSeriesConfiguration.getSeries().getDimension() != null) {
                    groupDimensionKey = new DimensionKey(innerSeriesConfiguration
                        .getSeries()
                        .getDimension());
                } else {
                    groupDimensionKey = new DimensionKey();
                }
                MetricSingleSeriesDetectionCondition seriesCondition
                    = new MetricSingleSeriesDetectionCondition(groupDimensionKey);

                if (innerSeriesConfiguration.getConditionOperator() != null) {
                    seriesCondition.setCrossConditionOperator(DetectionConditionsOperator.fromString(
                        innerSeriesConfiguration
                            .getConditionOperator()
                            .toString()));
                }

                seriesCondition
                    .setSmartDetectionCondition(innerSeriesConfiguration.getSmartDetectionCondition())
                    .setChangeThresholdCondition(innerSeriesConfiguration.getChangeThresholdCondition())
                    .setHardThresholdCondition(innerSeriesConfiguration.getHardThresholdCondition());

                configuration.addSingleSeriesDetectionCondition(seriesCondition);
            });
        }
        return configuration;
    }

    /**
     * Transform {@link com.azure.ai.metricsadvisor.models.AnomalyDetectionConfiguration} to create API wire model.
     *
     * @param metricId The metric id.
     * @param detectionConfiguration The custom model instance.
     * @return The wire model instance.
     */
    public static com.azure.ai.metricsadvisor.implementation.models.AnomalyDetectionConfiguration toInnerForCreate(
        ClientLogger logger,
        String metricId,
        AnomalyDetectionConfiguration detectionConfiguration) {

        com.azure.ai.metricsadvisor.implementation.models.AnomalyDetectionConfiguration innerDetectionConfiguration
            = new com.azure.ai.metricsadvisor.implementation.models.AnomalyDetectionConfiguration();
        innerDetectionConfiguration.setMetricId(UUID.fromString(metricId));
        if (CoreUtils.isNullOrEmpty(detectionConfiguration.getName())) {
            throw logger.logExceptionAsError(new IllegalArgumentException("detectionConfiguration.name is required"));
        }
        innerDetectionConfiguration.setName(detectionConfiguration.getName());
        innerDetectionConfiguration.setDescription(detectionConfiguration.getDescription());
        MetricWholeSeriesDetectionCondition wholeSeriesCondition
            = detectionConfiguration.getWholeSeriesDetectionCondition();
        if (wholeSeriesCondition == null) {
            throw logger.logExceptionAsError(
                new NullPointerException("detectionConfiguration.wholeSeriesCondition is required"));
        }
        innerDetectionConfiguration.setWholeMetricConfiguration(setupInnerWholeSeriesConfiguration(logger,
            true,
            wholeSeriesCondition));

        innerDetectionConfiguration.setDimensionGroupOverrideConfigurations(
            detectionConfiguration.getSeriesGroupDetectionConditions()
                .stream()
                .map(seriesGroupCondition ->
                    setupInnerSeriesGroupConfiguration(logger, true, seriesGroupCondition))
                .collect(Collectors.toList())
        );

        innerDetectionConfiguration.setSeriesOverrideConfigurations(
            detectionConfiguration.getSeriesDetectionConditions()
                .stream()
                .map(seriesGroupCondition ->
                    setupInnerSeriesConfiguration(logger, true, seriesGroupCondition))
                .collect(Collectors.toList())
        );
        return innerDetectionConfiguration;
    }

    /**
     * Transform {@link com.azure.ai.metricsadvisor.models.AnomalyDetectionConfiguration} to update API wire model.
     *
     * @param detectionConfiguration The custom model instance.
     * @return The wire model instance.
     */
    public static AnomalyDetectionConfigurationPatch
        toInnerForUpdate(ClientLogger logger, AnomalyDetectionConfiguration detectionConfiguration) {

        AnomalyDetectionConfigurationPatch innerDetectionConfiguration = new AnomalyDetectionConfigurationPatch();
        innerDetectionConfiguration.setName(detectionConfiguration.getName());
        innerDetectionConfiguration.setDescription(detectionConfiguration.getDescription());
        MetricWholeSeriesDetectionCondition wholeSeriesCondition
            = detectionConfiguration.getWholeSeriesDetectionCondition();
        if (wholeSeriesCondition != null) {
            innerDetectionConfiguration.setWholeMetricConfiguration(setupInnerWholeSeriesConfiguration(logger,
                false,
                wholeSeriesCondition));
        }

        innerDetectionConfiguration.setDimensionGroupOverrideConfigurations(
            detectionConfiguration.getSeriesGroupDetectionConditions()
                .stream()
                .map(seriesGroupCondition ->
                    setupInnerSeriesGroupConfiguration(logger, false, seriesGroupCondition))
                .collect(Collectors.toList())
        );

        innerDetectionConfiguration.setSeriesOverrideConfigurations(
            detectionConfiguration.getSeriesDetectionConditions()
                .stream()
                .map(seriesGroupCondition ->
                    setupInnerSeriesConfiguration(logger, false, seriesGroupCondition))
                .collect(Collectors.toList())
        );

        return innerDetectionConfiguration;
    }

    private static WholeMetricConfiguration
        setupInnerWholeSeriesConfiguration(ClientLogger logger,
        boolean isCreate,
        MetricWholeSeriesDetectionCondition wholeSeriesCondition) {
        WholeMetricConfiguration innerWholeSeriesCondition = new WholeMetricConfiguration();
        DetectionConditionsOperator crossConditionOperator = wholeSeriesCondition.getCrossConditionsOperator();
        if (crossConditionOperator != null) {
            innerWholeSeriesCondition.setConditionOperator(
                AnomalyDetectionConfigurationLogicType.fromString(crossConditionOperator.toString()));
        } else if (isCreate && hasMultipleNestedConditions(wholeSeriesCondition)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("detectionConfiguration.wholeSeriesCondition.crossConditionsOperator"
                    + " is required when multiple conditions are specified for the whole series."));
        }
        innerWholeSeriesCondition
            .setSmartDetectionCondition(wholeSeriesCondition.getSmartDetectionCondition())
            .setChangeThresholdCondition(wholeSeriesCondition.getChangeThresholdCondition())
            .setHardThresholdCondition(wholeSeriesCondition.getHardThresholdCondition());

        return innerWholeSeriesCondition;
    }

    private static DimensionGroupConfiguration
        setupInnerSeriesGroupConfiguration(ClientLogger logger,
        boolean isCreate,
        MetricSeriesGroupDetectionCondition seriesGroupCondition) {
        if (isCreate && seriesGroupCondition.getSeriesGroupKey() == null) {
            throw logger.logExceptionAsError(
                new NullPointerException("MetricSeriesGroupDetectionCondition.seriesGroupKey is required"));
        }

        DimensionGroupConfiguration innerConfiguration = new DimensionGroupConfiguration();
        if (seriesGroupCondition.getSeriesGroupKey() != null) {
            innerConfiguration.setGroup(new DimensionGroupIdentity()
                .setDimension(seriesGroupCondition.getSeriesGroupKey().asMap()));
        }

        DetectionConditionsOperator groupConditionOperator = seriesGroupCondition.getCrossConditionsOperator();
        if (groupConditionOperator != null) {
            innerConfiguration.setConditionOperator(
                AnomalyDetectionConfigurationLogicType.fromString(groupConditionOperator.toString()));
        } else if (isCreate && hasMultipleNestedConditions(seriesGroupCondition)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException(
                    "detectionConfiguration.seriesGroupConditions.crossConditionsOperator"
                        + " is required when multiple conditions are specified for a series group."));
        }

        innerConfiguration
            .setSmartDetectionCondition(seriesGroupCondition.getSmartDetectionCondition())
            .setChangeThresholdCondition(seriesGroupCondition.getChangeThresholdCondition())
            .setHardThresholdCondition(seriesGroupCondition.getHardThresholdCondition());

        return innerConfiguration;
    }

    private static SeriesConfiguration
        setupInnerSeriesConfiguration(ClientLogger logger,
        boolean isCreate,
        MetricSingleSeriesDetectionCondition seriesCondition) {
        if (isCreate && seriesCondition.getSeriesKey() == null) {
            throw logger.logExceptionAsError(
                new NullPointerException("MetricSingleSeriesDetectionCondition.seriesKey is required"));
        }
        SeriesConfiguration innerConfiguration = new SeriesConfiguration()
            .setSeries(new SeriesIdentity()
                .setDimension(seriesCondition.getSeriesKey().asMap()));

        DetectionConditionsOperator seriesConditionOperator = seriesCondition.getCrossConditionsOperator();
        if (seriesConditionOperator != null) {
            innerConfiguration.setConditionOperator(
                AnomalyDetectionConfigurationLogicType.fromString(seriesConditionOperator.toString()));
        } else if (isCreate && hasMultipleNestedConditions(seriesCondition)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException(
                    "detectionConfiguration.seriesConditions.crossConditionsOperator"
                        + " is required when multiple conditions are specified for a series."));
        }

        innerConfiguration
            .setSmartDetectionCondition(seriesCondition.getSmartDetectionCondition())
            .setChangeThresholdCondition(seriesCondition.getChangeThresholdCondition())
            .setHardThresholdCondition(seriesCondition.getHardThresholdCondition());

        return innerConfiguration;
    }

    private static boolean hasMultipleNestedConditions(MetricWholeSeriesDetectionCondition
        seriesDetectionCondition) {
        Optional<Boolean> multipleConditionsOpt = Stream.of(
            seriesDetectionCondition.getSmartDetectionCondition() != null,
            seriesDetectionCondition.getChangeThresholdCondition() != null,
            seriesDetectionCondition.getChangeThresholdCondition() != null)
            .filter(p -> p)
            .skip(1)
            .findAny();
        return multipleConditionsOpt.isPresent();
    }

    private static boolean hasMultipleNestedConditions(MetricSeriesGroupDetectionCondition
        seriesDetectionCondition) {
        Optional<Boolean> multipleConditionsOpt = Stream.of(
            seriesDetectionCondition.getSmartDetectionCondition() != null,
            seriesDetectionCondition.getChangeThresholdCondition() != null,
            seriesDetectionCondition.getChangeThresholdCondition() != null)
            .filter(p -> p)
            .skip(1)
            .findAny();
        return multipleConditionsOpt.isPresent();
    }

    private static boolean hasMultipleNestedConditions(MetricSingleSeriesDetectionCondition
        seriesDetectionCondition) {
        Optional<Boolean> multipleConditionsOpt = Stream.of(
            seriesDetectionCondition.getSmartDetectionCondition() != null,
            seriesDetectionCondition.getChangeThresholdCondition() != null,
            seriesDetectionCondition.getChangeThresholdCondition() != null)
            .filter(p -> p)
            .skip(1)
            .findAny();
        return multipleConditionsOpt.isPresent();
    }
}
