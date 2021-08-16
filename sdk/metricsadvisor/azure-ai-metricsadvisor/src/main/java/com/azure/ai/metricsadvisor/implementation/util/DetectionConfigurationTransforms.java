// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.administration.models.HardThresholdCondition;
import com.azure.ai.metricsadvisor.administration.models.SmartDetectionCondition;
import com.azure.ai.metricsadvisor.administration.models.SuppressCondition;
import com.azure.ai.metricsadvisor.implementation.models.AnomalyDetectionConfigurationLogicType;
import com.azure.ai.metricsadvisor.implementation.models.AnomalyDetectionConfigurationPatch;
import com.azure.ai.metricsadvisor.implementation.models.ChangeThresholdConditionPatch;
import com.azure.ai.metricsadvisor.implementation.models.DimensionGroupConfiguration;
import com.azure.ai.metricsadvisor.implementation.models.DimensionGroupIdentity;
import com.azure.ai.metricsadvisor.implementation.models.HardThresholdConditionPatch;
import com.azure.ai.metricsadvisor.implementation.models.SeriesConfiguration;
import com.azure.ai.metricsadvisor.implementation.models.SeriesIdentity;
import com.azure.ai.metricsadvisor.implementation.models.SmartDetectionConditionPatch;
import com.azure.ai.metricsadvisor.implementation.models.WholeMetricConfiguration;
import com.azure.ai.metricsadvisor.implementation.models.WholeMetricConfigurationPatch;
import com.azure.ai.metricsadvisor.administration.models.ChangeThresholdCondition;
import com.azure.ai.metricsadvisor.administration.models.DetectionConditionOperator;
import com.azure.ai.metricsadvisor.models.DimensionKey;
import com.azure.ai.metricsadvisor.administration.models.MetricWholeSeriesDetectionCondition;
import com.azure.ai.metricsadvisor.administration.models.AnomalyDetectionConfiguration;
import com.azure.ai.metricsadvisor.administration.models.MetricSeriesGroupDetectionCondition;
import com.azure.ai.metricsadvisor.administration.models.MetricSingleSeriesDetectionCondition;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Expose transformation methods to transform {@link AnomalyDetectionConfiguration}
 * model to REST API wire model and vice-versa.
 */
public final class DetectionConfigurationTransforms {
    private DetectionConfigurationTransforms() {
    }

    public static PagedResponse<AnomalyDetectionConfiguration> fromInnerPagedResponse(
            PagedResponse<com.azure.ai.metricsadvisor.implementation.models.AnomalyDetectionConfiguration>
                innerResponse) {
        final List<com.azure.ai.metricsadvisor.implementation.models.AnomalyDetectionConfiguration>
            innerConfigurationList = innerResponse.getValue();
        List<AnomalyDetectionConfiguration> configurationList;
        if (innerConfigurationList != null) {
            configurationList = innerConfigurationList
                .stream()
                .map(innerConfiguration -> DetectionConfigurationTransforms.fromInner(innerConfiguration))
                .collect(Collectors.toList());
        } else {
            configurationList = new ArrayList<>();
        }
        return new PagedResponseBase<Void, AnomalyDetectionConfiguration>(
            innerResponse.getRequest(),
            innerResponse.getStatusCode(),
            innerResponse.getHeaders(),
            configurationList,
            innerResponse.getContinuationToken(),
            null);
    }

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
                wholeSeriesConditions.setConditionOperator(
                    DetectionConditionOperator.fromString(innerWholeSeriesConditions
                        .getConditionOperator()
                        .toString()));
            }

            wholeSeriesConditions
                .setSmartDetectionCondition(fromInner(innerWholeSeriesConditions.getSmartDetectionCondition()))
                .setChangeThresholdCondition(fromInner(innerWholeSeriesConditions.getChangeThresholdCondition()))
                .setHardThresholdCondition(fromInner(innerWholeSeriesConditions.getHardThresholdCondition()));

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
                    seriesGroupCondition.setConditionOperator(DetectionConditionOperator.fromString(
                        innerSeriesGroupConfiguration
                            .getConditionOperator()
                            .toString()));
                }

                seriesGroupCondition
                    .setSmartDetectionCondition(fromInner(innerSeriesGroupConfiguration.getSmartDetectionCondition()))
                    .setChangeThresholdCondition(fromInner(innerSeriesGroupConfiguration.getChangeThresholdCondition()))
                    .setHardThresholdCondition(fromInner(innerSeriesGroupConfiguration.getHardThresholdCondition()));

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
                    seriesCondition.setConditionOperator(DetectionConditionOperator.fromString(
                        innerSeriesConfiguration
                            .getConditionOperator()
                            .toString()));
                }

                seriesCondition
                    .setSmartDetectionCondition(fromInner(innerSeriesConfiguration.getSmartDetectionCondition()))
                    .setChangeThresholdCondition(fromInner(innerSeriesConfiguration.getChangeThresholdCondition()))
                    .setHardThresholdCondition(fromInner(innerSeriesConfiguration.getHardThresholdCondition()));

                configuration.addSingleSeriesDetectionCondition(seriesCondition);
            });
        }
        return configuration;
    }

    /**
     * Transform {@link AnomalyDetectionConfiguration} to create API wire model.
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
        innerDetectionConfiguration.setWholeMetricConfiguration(setupInnerWholeSeriesConfigurationForCreate(logger,
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
     * Transform {@link AnomalyDetectionConfiguration} to update API wire model.
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
            innerDetectionConfiguration.setWholeMetricConfiguration(setupInnerWholeSeriesConfigurationForUpdate(
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
        setupInnerWholeSeriesConfigurationForCreate(ClientLogger logger,
                                                    MetricWholeSeriesDetectionCondition wholeSeriesCondition) {
        WholeMetricConfiguration innerWholeSeriesCondition = new WholeMetricConfiguration();
        DetectionConditionOperator crossConditionOperator = wholeSeriesCondition.getConditionOperator();
        if (crossConditionOperator != null) {
            innerWholeSeriesCondition.setConditionOperator(
                AnomalyDetectionConfigurationLogicType.fromString(crossConditionOperator.toString()));
        } else if (hasMultipleNestedConditions(wholeSeriesCondition)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("detectionConfiguration.wholeSeriesCondition.conditionOperator"
                    + " is required when multiple conditions are specified for the whole series."));
        }
        innerWholeSeriesCondition
            .setSmartDetectionCondition(toInnerForCreate(wholeSeriesCondition.getSmartDetectionCondition()))
            .setChangeThresholdCondition(toInnerForCreate(wholeSeriesCondition.getChangeThresholdCondition()))
            .setHardThresholdCondition(toInnerForCreate(wholeSeriesCondition.getHardThresholdCondition()));

        return innerWholeSeriesCondition;
    }

    private static WholeMetricConfigurationPatch
        setupInnerWholeSeriesConfigurationForUpdate(MetricWholeSeriesDetectionCondition wholeSeriesCondition) {
        WholeMetricConfigurationPatch innerWholeSeriesCondition = new WholeMetricConfigurationPatch();
        DetectionConditionOperator crossConditionOperator = wholeSeriesCondition.getConditionOperator();
        if (crossConditionOperator != null) {
            innerWholeSeriesCondition.setConditionOperator(
                AnomalyDetectionConfigurationLogicType.fromString(crossConditionOperator.toString()));
        }

        if (wholeSeriesCondition.getSmartDetectionCondition() != null) {
            innerWholeSeriesCondition
                .setSmartDetectionCondition(toInnerForUpdate(wholeSeriesCondition.getSmartDetectionCondition()));
        }

        if (wholeSeriesCondition.getChangeThresholdCondition() != null) {
            innerWholeSeriesCondition
                .setChangeThresholdCondition(toInnerForUpdate(wholeSeriesCondition.getChangeThresholdCondition()));
        }

        if (wholeSeriesCondition.getHardThresholdCondition() != null) {
            innerWholeSeriesCondition
                .setHardThresholdCondition(toInnerForUpdate(wholeSeriesCondition.getHardThresholdCondition()));
        }

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

        DetectionConditionOperator groupConditionOperator = seriesGroupCondition.getConditionOperator();
        if (groupConditionOperator != null) {
            innerConfiguration.setConditionOperator(
                AnomalyDetectionConfigurationLogicType.fromString(groupConditionOperator.toString()));
        } else if (isCreate && hasMultipleNestedConditions(seriesGroupCondition)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException(
                    "detectionConfiguration.seriesGroupConditions.conditionOperator"
                        + " is required when multiple conditions are specified for a series group."));
        }

        innerConfiguration
            .setSmartDetectionCondition(toInnerForCreate(seriesGroupCondition.getSmartDetectionCondition()))
            .setChangeThresholdCondition(toInnerForCreate(seriesGroupCondition.getChangeThresholdCondition()))
            .setHardThresholdCondition(toInnerForCreate(seriesGroupCondition.getHardThresholdCondition()));

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

        DetectionConditionOperator seriesConditionOperator = seriesCondition.getConditionOperator();
        if (seriesConditionOperator != null) {
            innerConfiguration.setConditionOperator(
                AnomalyDetectionConfigurationLogicType.fromString(seriesConditionOperator.toString()));
        } else if (isCreate && hasMultipleNestedConditions(seriesCondition)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException(
                    "detectionConfiguration.seriesConditions.conditionOperator"
                        + " is required when multiple conditions are specified for a series."));
        }

        innerConfiguration
            .setSmartDetectionCondition(toInnerForCreate(seriesCondition.getSmartDetectionCondition()))
            .setChangeThresholdCondition(toInnerForCreate(seriesCondition.getChangeThresholdCondition()))
            .setHardThresholdCondition(toInnerForCreate(seriesCondition.getHardThresholdCondition()));

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

    private static ChangeThresholdCondition fromInner(
        com.azure.ai.metricsadvisor.implementation.models.ChangeThresholdCondition inner) {
        if (inner == null) {
            return null;
        }
        return new ChangeThresholdCondition(
            inner.getChangePercentage(),
            inner.getShiftPoint(),
            inner.isWithinRange(),
            inner.getAnomalyDetectorDirection(),
            fromInner(inner.getSuppressCondition()));
    }

    private static com.azure.ai.metricsadvisor.implementation.models.ChangeThresholdCondition toInnerForCreate(
        ChangeThresholdCondition condition) {
        if (condition == null) {
            return null;
        }
        return new com.azure.ai.metricsadvisor.implementation.models.ChangeThresholdCondition()
            .setAnomalyDetectorDirection(condition.getAnomalyDetectorDirection())
            .setChangePercentage(condition.getChangePercentage())
            .setShiftPoint(condition.getShiftPoint())
            .setWithinRange(condition.isWithinRange())
            .setSuppressCondition(toInnerForCreate(condition.getSuppressCondition()));
    }

    private static ChangeThresholdConditionPatch toInnerForUpdate(ChangeThresholdCondition condition) {
        if (condition == null) {
            return null;
        }
        ChangeThresholdConditionPatch inner = new ChangeThresholdConditionPatch();
        inner.setAnomalyDetectorDirection(condition.getAnomalyDetectorDirection())
            .setChangePercentage(condition.getChangePercentage())
            .setShiftPoint(condition.getShiftPoint())
            .setWithinRange(condition.isWithinRange());

        if (condition.getSuppressCondition() != null) {
            inner.setSuppressCondition(toInnerForUpdate(condition.getSuppressCondition()));
        }
        return inner;
    }

    private static HardThresholdCondition fromInner(
        com.azure.ai.metricsadvisor.implementation.models.HardThresholdCondition inner) {
        if (inner == null) {
            return null;
        }
        return new HardThresholdCondition(inner.getAnomalyDetectorDirection(), fromInner(inner.getSuppressCondition()))
            .setLowerBound(inner.getLowerBound())
            .setUpperBound(inner.getUpperBound());
    }

    private static com.azure.ai.metricsadvisor.implementation.models.HardThresholdCondition toInnerForCreate(
        HardThresholdCondition condition) {
        if (condition == null) {
            return null;
        }
        return new com.azure.ai.metricsadvisor.implementation.models.HardThresholdCondition()
            .setAnomalyDetectorDirection(condition.getAnomalyDetectorDirection())
            .setSuppressCondition(toInnerForCreate(condition.getSuppressCondition()))
            .setLowerBound(condition.getLowerBound())
            .setUpperBound(condition.getUpperBound());
    }

    private static HardThresholdConditionPatch toInnerForUpdate(HardThresholdCondition condition) {
        if (condition == null) {
            return null;
        }
        HardThresholdConditionPatch inner = new HardThresholdConditionPatch();
        inner.setAnomalyDetectorDirection(condition.getAnomalyDetectorDirection())
            .setLowerBound(condition.getLowerBound())
            .setUpperBound(condition.getUpperBound());

        if (condition.getSuppressCondition() != null) {
            inner.setSuppressCondition(toInnerForUpdate(condition.getSuppressCondition()));
        }
        return inner;
    }

    private static SmartDetectionCondition fromInner(
        com.azure.ai.metricsadvisor.implementation.models.SmartDetectionCondition inner) {
        if (inner == null) {
            return null;
        }
        return new SmartDetectionCondition(inner.getSensitivity(),
            inner.getAnomalyDetectorDirection(),
            fromInner(inner.getSuppressCondition()));
    }

    private static com.azure.ai.metricsadvisor.implementation.models.SmartDetectionCondition toInnerForCreate(
        SmartDetectionCondition condition) {
        if (condition == null) {
            return null;
        }
        return new com.azure.ai.metricsadvisor.implementation.models.SmartDetectionCondition()
            .setSensitivity(condition.getSensitivity())
            .setAnomalyDetectorDirection(condition.getAnomalyDetectorDirection())
            .setSuppressCondition(toInnerForCreate(condition.getSuppressCondition()));
    }

    private static SmartDetectionConditionPatch toInnerForUpdate(SmartDetectionCondition condition) {
        if (condition == null) {
            return null;
        }
        SmartDetectionConditionPatch inner = new SmartDetectionConditionPatch();
        inner
            .setSensitivity(condition.getSensitivity())
            .setAnomalyDetectorDirection(condition.getAnomalyDetectorDirection());

        if (condition.getSuppressCondition() != null) {
            inner.setSuppressCondition(toInnerForUpdate(condition.getSuppressCondition()));
        }
        return inner;
    }

    private static SuppressCondition fromInner(
        com.azure.ai.metricsadvisor.implementation.models.SuppressCondition inner) {
        return inner != null ? new SuppressCondition(inner.getMinNumber(), inner.getMinRatio()) : null;
    }

    private static com.azure.ai.metricsadvisor.implementation.models.SuppressCondition toInnerForCreate(
        SuppressCondition condition) {
        return condition != null ? new com.azure.ai.metricsadvisor.implementation.models.SuppressCondition()
            .setMinNumber(condition.getMinNumber())
            .setMinRatio(condition.getMinRatio()) : null;
    }

    private static com.azure.ai.metricsadvisor.implementation.models.SuppressConditionPatch toInnerForUpdate(
        SuppressCondition condition) {
        return condition != null ? new com.azure.ai.metricsadvisor.implementation.models.SuppressConditionPatch()
            .setMinNumber(condition.getMinNumber())
            .setMinRatio(condition.getMinRatio()) : null;
    }
}
