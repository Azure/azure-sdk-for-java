// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.administration.models.BoundaryMeasureType;
import com.azure.ai.metricsadvisor.administration.models.MetricAnomalyAlertSnoozeCondition;
import com.azure.ai.metricsadvisor.implementation.models.AnomalyAlertingConfiguration;
import com.azure.ai.metricsadvisor.implementation.models.AnomalyAlertingConfigurationLogicType;
import com.azure.ai.metricsadvisor.implementation.models.AnomalyAlertingConfigurationPatch;
import com.azure.ai.metricsadvisor.implementation.models.AnomalyScope;
import com.azure.ai.metricsadvisor.implementation.models.DimensionGroupIdentity;
import com.azure.ai.metricsadvisor.implementation.models.Direction;
import com.azure.ai.metricsadvisor.implementation.models.MetricAlertingConfiguration;
import com.azure.ai.metricsadvisor.implementation.models.SeverityCondition;
import com.azure.ai.metricsadvisor.implementation.models.ValueCondition;
import com.azure.ai.metricsadvisor.administration.models.AnomalyAlertConfiguration;
import com.azure.ai.metricsadvisor.administration.models.BoundaryDirection;
import com.azure.ai.metricsadvisor.implementation.models.ValueType;
import com.azure.ai.metricsadvisor.models.DimensionKey;
import com.azure.ai.metricsadvisor.administration.models.MetricAnomalyAlertConditions;
import com.azure.ai.metricsadvisor.administration.models.MetricAlertConfiguration;
import com.azure.ai.metricsadvisor.administration.models.MetricAlertConfigurationsOperator;
import com.azure.ai.metricsadvisor.administration.models.MetricAnomalyAlertScope;
import com.azure.ai.metricsadvisor.administration.models.MetricAnomalyAlertScopeType;
import com.azure.ai.metricsadvisor.administration.models.MetricBoundaryCondition;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.ai.metricsadvisor.administration.models.TopNGroupScope;
import com.azure.core.util.logging.ClientLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Helper class to convert service level alert configuration models to SDK models.
 */
public final class AlertConfigurationTransforms {
    private static final ClientLogger LOGGER = new ClientLogger(AlertConfigurationTransforms.class);

    private AlertConfigurationTransforms() {
    }

    /**
     * Transform method to map to service level alert configuration model for create operation.
     *
     * @param alertConfiguration the SDK provided alert configuration object.
     *
     * @return the service level alert configuration model.
     */
    public static AnomalyAlertingConfiguration toInnerForCreate(
        AnomalyAlertConfiguration alertConfiguration) {
        AnomalyAlertingConfiguration innerAlertConfiguration = new AnomalyAlertingConfiguration();

        innerAlertConfiguration.setName(alertConfiguration.getName());
        innerAlertConfiguration.setDescription(alertConfiguration.getDescription());
        innerAlertConfiguration.setCrossMetricsOperator(alertConfiguration.getCrossMetricsOperator() == null
            ? null : AnomalyAlertingConfigurationLogicType.fromString(
            alertConfiguration.getCrossMetricsOperator().toString()));

        List<String> rawIds = AnomalyAlertConfigurationHelper.getHookIdsToAlertRaw(alertConfiguration);
        if (rawIds != null) {
            innerAlertConfiguration.setHookIds(rawIds
                .stream()
                .map(UUID::fromString).collect(Collectors.toList()));
        }

        List<String> dimensions = AnomalyAlertConfigurationHelper.getDimensionsToSplitAlertRaw(alertConfiguration);
        if (dimensions != null) {
            innerAlertConfiguration.setSplitAlertByDimensions(dimensions);
        }

        List<MetricAlertingConfiguration> innerMetricAlertConfigurations =
            getMetricAlertConfigList(alertConfiguration.getMetricAlertConfigurations());

        innerAlertConfiguration.setMetricAlertingConfigurations(innerMetricAlertConfigurations);
        return innerAlertConfiguration;
    }

    /**
     * Transform method to map to service level alert configuration model for update operation.
     *
     * @param alertConfiguration the SDK provided alert configuration object.
     *
     * @return the service level alert configuration model.
     */
    public static AnomalyAlertingConfigurationPatch toInnerForUpdate(
        AnomalyAlertConfiguration alertConfiguration) {
        AnomalyAlertingConfigurationPatch innerAlertConfiguration = new AnomalyAlertingConfigurationPatch();

        innerAlertConfiguration.setName(alertConfiguration.getName());
        innerAlertConfiguration.setDescription(alertConfiguration.getDescription());
        innerAlertConfiguration.setCrossMetricsOperator(alertConfiguration.getCrossMetricsOperator() == null
            ? null : AnomalyAlertingConfigurationLogicType.fromString(
            alertConfiguration.getCrossMetricsOperator().toString()));

        List<String> rawIds = AnomalyAlertConfigurationHelper.getHookIdsToAlertRaw(alertConfiguration);
        if (rawIds != null) {
            innerAlertConfiguration.setHookIds(rawIds
                .stream()
                .map(UUID::fromString).collect(Collectors.toList()));
        }

        List<String> dimensions = AnomalyAlertConfigurationHelper.getDimensionsToSplitAlertRaw(alertConfiguration);
        if (dimensions != null) {
            innerAlertConfiguration.setSplitAlertByDimensions(dimensions);
        }

        List<MetricAlertingConfiguration> innerMetricAlertConfigurations =
            getMetricAlertConfigList(alertConfiguration.getMetricAlertConfigurations());

        innerAlertConfiguration.setMetricAlertingConfigurations(innerMetricAlertConfigurations);
        return innerAlertConfiguration;
    }

    /**
     * Internal helper method to get the service DataFeedMetric alert configurations list.
     *
     * @param metricAlertConfigurations the SDK level provided metric configurations list.
     *
     * @return the service required DataFeedMetric alert configurations list.
     */
    private static List<MetricAlertingConfiguration> getMetricAlertConfigList(
        List<MetricAlertConfiguration> metricAlertConfigurations) {
        List<MetricAlertingConfiguration> innerMetricAlertConfigurations = new ArrayList<>();
        for (MetricAlertConfiguration metricAnomalyAlertConfiguration : metricAlertConfigurations) {
            MetricAlertingConfiguration innerMetricAlertConfiguration = new MetricAlertingConfiguration();
            innerMetricAlertConfiguration
                .setAnomalyDetectionConfigurationId(UUID
                    .fromString(metricAnomalyAlertConfiguration.getDetectionConfigurationId()));
            innerMetricAlertConfiguration
                .setNegationOperation(metricAnomalyAlertConfiguration.isNegationOperationEnabled());

            // 1. Set the scope.
            final MetricAnomalyAlertScope alertScope = metricAnomalyAlertConfiguration.getAlertScope();
            if (alertScope.getScopeType() == MetricAnomalyAlertScopeType.WHOLE_SERIES) {
                innerMetricAlertConfiguration.setAnomalyScopeType(AnomalyScope.ALL);
            } else if (alertScope.getScopeType() == MetricAnomalyAlertScopeType.SERIES_GROUP) {
                innerMetricAlertConfiguration.setAnomalyScopeType(AnomalyScope.DIMENSION);
                DimensionGroupIdentity innerId = new DimensionGroupIdentity()
                        .setDimension(metricAnomalyAlertConfiguration
                            .getAlertScope()
                            .getSeriesGroupInScope()
                            .asMap());
                innerMetricAlertConfiguration.setDimensionAnomalyScope(innerId);
            } else if (alertScope.getScopeType() == MetricAnomalyAlertScopeType.TOP_N) {
                innerMetricAlertConfiguration.setAnomalyScopeType(AnomalyScope.TOPN);
                com.azure.ai.metricsadvisor.implementation.models.TopNGroupScope innerTopNGroupScope =
                    new com.azure.ai.metricsadvisor.implementation.models.TopNGroupScope()
                        .setTop(alertScope.getTopNGroupInScope().getTop())
                        .setPeriod(alertScope.getTopNGroupInScope().getPeriod())
                        .setMinTopCount(alertScope.getTopNGroupInScope().getMinTopCount());
                innerMetricAlertConfiguration.setTopNAnomalyScope(innerTopNGroupScope);
            }

            // 2. Set alert conditions (boundary and boundary conditions).
            final MetricAnomalyAlertConditions alertConditions = metricAnomalyAlertConfiguration.getAlertConditions();
            if (alertConditions != null) {
                if (alertConditions.getSeverityCondition() != null) {
                    innerMetricAlertConfiguration.setSeverityFilter(new SeverityCondition()
                        .setMaxAlertSeverity(alertConditions.getSeverityCondition().getMinAlertSeverity())
                        .setMaxAlertSeverity(alertConditions.getSeverityCondition().getMaxAlertSeverity()));
                }
                final MetricBoundaryCondition boundaryConditions = alertConditions.getMetricBoundaryCondition();
                ValueCondition innerValueCondition = new ValueCondition();
                if (boundaryConditions != null) {
                    BoundaryDirection direction = boundaryConditions.getDirection();
                    if (direction == BoundaryDirection.LOWER) {
                        innerValueCondition.setDirection(Direction.DOWN);
                    } else if (direction == BoundaryDirection.UPPER) {
                        innerValueCondition.setDirection(Direction.UP);
                    } else if (direction == BoundaryDirection.BOTH) {
                        innerValueCondition.setDirection(Direction.BOTH);
                    } else {
                        throw LOGGER.logExceptionAsError(new IllegalStateException("Unexpected value: "
                            + direction));
                    }
                    innerValueCondition.setLower(boundaryConditions.getLowerBoundary());
                    innerValueCondition.setUpper(boundaryConditions.getUpperBoundary());
                    if (boundaryConditions.getCompanionMetricId() != null) {
                        innerValueCondition
                            .setMetricId(UUID.fromString(boundaryConditions.getCompanionMetricId()));
                        innerValueCondition.setTriggerForMissing(boundaryConditions.shouldAlertIfDataPointMissing());
                    }
                    if (boundaryConditions.getMeasureType() != null) {
                        innerValueCondition.setType(
                            ValueType.fromString(boundaryConditions.getMeasureType().toString()));
                    }
                    innerMetricAlertConfiguration.setValueFilter(innerValueCondition);
                }
            }

            // 3. Set alert snooze conditions
            if (metricAnomalyAlertConfiguration.getAlertSnoozeCondition() != null) {
                com.azure.ai.metricsadvisor.implementation.models.MetricAnomalyAlertSnoozeCondition innerSnoozeCondition
                    = new com.azure.ai.metricsadvisor.implementation.models.MetricAnomalyAlertSnoozeCondition();

                innerSnoozeCondition
                    .setAutoSnooze(metricAnomalyAlertConfiguration.getAlertSnoozeCondition().getAutoSnooze())
                    .setSnoozeScope(metricAnomalyAlertConfiguration.getAlertSnoozeCondition().getSnoozeScope())
                    .setOnlyForSuccessive(metricAnomalyAlertConfiguration.getAlertSnoozeCondition().isOnlyForSuccessive());

                innerMetricAlertConfiguration.setSnoozeFilter(innerSnoozeCondition);
            }
            innerMetricAlertConfigurations.add(innerMetricAlertConfiguration);
        }
        return innerMetricAlertConfigurations;
    }

    public static PagedResponse<AnomalyAlertConfiguration> fromInnerPagedResponse(
        PagedResponse<AnomalyAlertingConfiguration> innerResponse) {
        final List<AnomalyAlertingConfiguration>
            innerConfigurationList = innerResponse.getValue();
        List<AnomalyAlertConfiguration> configurationList;
        if (innerConfigurationList != null) {
            configurationList = innerConfigurationList
                .stream()
                .map(innerConfiguration -> fromInner(innerConfiguration))
                .collect(Collectors.toList());
        } else {
            configurationList = new ArrayList<>();
        }
        return new PagedResponseBase<Void, AnomalyAlertConfiguration>(
            innerResponse.getRequest(),
            innerResponse.getStatusCode(),
            innerResponse.getHeaders(),
            configurationList,
            innerResponse.getContinuationToken(),
            null);
    }

    /**
     * Helper method to map the service level {@link AnomalyAlertingConfiguration alert configuration} to SDK
     * {@link AnomalyAlertConfiguration}.
     *
     * @param innerAlertConfiguration the service provided {@link AnomalyAlertingConfiguration alert configuration}
     *
     * @return the mapped {@link AnomalyAlertConfiguration}.
     */
    public static AnomalyAlertConfiguration fromInner(
        AnomalyAlertingConfiguration innerAlertConfiguration) {
        AnomalyAlertConfiguration alertConfiguration;
        if (innerAlertConfiguration.getCrossMetricsOperator() == null) {
            alertConfiguration = new AnomalyAlertConfiguration(innerAlertConfiguration.getName());
        } else {
            alertConfiguration = new AnomalyAlertConfiguration(innerAlertConfiguration.getName(),
                MetricAlertConfigurationsOperator.fromString(innerAlertConfiguration
                    .getCrossMetricsOperator()
                    .toString()));
        }
        AnomalyAlertConfigurationHelper.setId(alertConfiguration,
            innerAlertConfiguration.getAnomalyAlertingConfigurationId().toString());

        alertConfiguration.setDescription(innerAlertConfiguration.getDescription());
        alertConfiguration.setHookIdsToAlert(innerAlertConfiguration
            .getHookIds()
            .stream()
            .map(UUID::toString).collect(Collectors.toList()));

        List<MetricAlertingConfiguration> innerMetricAlertConfigurations =
            innerAlertConfiguration.getMetricAlertingConfigurations();
        if (innerMetricAlertConfigurations != null) {
            List<MetricAlertConfiguration> metricAlertConfigurations = new ArrayList<>();
            for (MetricAlertingConfiguration innerMetricAlertConfiguration : innerMetricAlertConfigurations) {
                MetricAnomalyAlertScope alertScope = null;
                if (innerMetricAlertConfiguration.getAnomalyScopeType() == AnomalyScope.ALL) {
                    alertScope = MetricAnomalyAlertScope.forWholeSeries();
                } else if (innerMetricAlertConfiguration.getAnomalyScopeType() == AnomalyScope.DIMENSION) {
                    DimensionKey seriesGroupId = new DimensionKey();
                    for (Map.Entry<String, String> entry
                        : innerMetricAlertConfiguration.getDimensionAnomalyScope().getDimension().entrySet()) {
                        seriesGroupId.put(entry.getKey(), entry.getValue());
                    }
                    alertScope = MetricAnomalyAlertScope.forSeriesGroup(seriesGroupId);
                } else if (innerMetricAlertConfiguration.getAnomalyScopeType() == AnomalyScope.TOPN) {
                    com.azure.ai.metricsadvisor.implementation.models.TopNGroupScope innerTopNGroupScope
                        = innerMetricAlertConfiguration.getTopNAnomalyScope();

                    alertScope =
                        MetricAnomalyAlertScope.forTopNGroup(new TopNGroupScope(innerTopNGroupScope.getTop(),
                            innerTopNGroupScope.getPeriod(),
                            innerTopNGroupScope.getMinTopCount()));
                }
                MetricAlertConfiguration metricAlertConfiguration
                    = new MetricAlertConfiguration(
                    innerMetricAlertConfiguration.getAnomalyDetectionConfigurationId().toString(),
                    alertScope, innerMetricAlertConfiguration.isNegationOperation());
                // Set alert condition
                if (innerMetricAlertConfiguration.getSeverityFilter() != null
                    || innerMetricAlertConfiguration.getValueFilter() != null) {
                    MetricAnomalyAlertConditions alertConditions = new MetricAnomalyAlertConditions();
                    // Set severity based condition.
                    if (innerMetricAlertConfiguration.getSeverityFilter() != null) {
                        alertConditions.setSeverityRangeCondition(
                            new com.azure.ai.metricsadvisor.administration.models.SeverityCondition(
                                innerMetricAlertConfiguration.getSeverityFilter().getMinAlertSeverity(),
                                innerMetricAlertConfiguration.getSeverityFilter().getMaxAlertSeverity()));
                    }
                    // Set boundary based condition.
                    ValueCondition innerValueCondition = innerMetricAlertConfiguration.getValueFilter();
                    if (innerValueCondition != null) {
                        MetricBoundaryCondition boundaryCondition = new MetricBoundaryCondition();
                        MetricBoundaryConditionHelper.setLowerBoundary(boundaryCondition,
                            innerValueCondition.getLower());
                        MetricBoundaryConditionHelper.setUpperBoundary(boundaryCondition,
                            innerValueCondition.getUpper());
                        if (innerValueCondition.getDirection() == Direction.DOWN) {
                            MetricBoundaryConditionHelper.setBoundaryDirection(boundaryCondition,
                                BoundaryDirection.LOWER);
                        } else if (innerValueCondition.getDirection() == Direction.UP) {
                            MetricBoundaryConditionHelper.setBoundaryDirection(boundaryCondition,
                                BoundaryDirection.UPPER);
                        } else if (innerValueCondition.getDirection() == Direction.BOTH) {
                            MetricBoundaryConditionHelper.setBoundaryDirection(boundaryCondition,
                                BoundaryDirection.BOTH);
                        }
                        if (innerValueCondition.getMetricId() != null) {
                            boolean triggerIfMissing = innerValueCondition.isTriggerForMissing() != null
                                && innerValueCondition.isTriggerForMissing();
                            boundaryCondition.setCompanionMetricId(innerValueCondition.getMetricId().toString(),
                                triggerIfMissing);
                        }
                        if (innerValueCondition.getType() != null) {
                            boundaryCondition.setMeasureType(
                                BoundaryMeasureType.fromString(innerValueCondition.getType().toString()));
                        }
                        alertConditions.setMetricBoundaryCondition(boundaryCondition);
                    }
                    metricAlertConfiguration.setAlertConditions(alertConditions);
                }

                com.azure.ai.metricsadvisor.implementation.models.MetricAnomalyAlertSnoozeCondition
                    innerSnoozeCondition = innerMetricAlertConfiguration.getSnoozeFilter();

                if (innerSnoozeCondition != null) {
                    metricAlertConfiguration.setAlertSnoozeCondition(new MetricAnomalyAlertSnoozeCondition(
                        innerSnoozeCondition.getAutoSnooze(),
                        innerSnoozeCondition.getSnoozeScope(),
                        innerSnoozeCondition.isOnlyForSuccessive()));
                }
                metricAlertConfigurations.add(metricAlertConfiguration);
            }
            alertConfiguration.setMetricAlertConfigurations(metricAlertConfigurations);
        }
        return alertConfiguration;
    }
}
