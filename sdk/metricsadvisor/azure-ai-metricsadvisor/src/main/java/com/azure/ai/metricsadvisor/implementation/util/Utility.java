// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.administration.models.DataFeedIngestionProgress;
import com.azure.ai.metricsadvisor.administration.models.DataFeedIngestionStatus;
import com.azure.ai.metricsadvisor.administration.models.IngestionStatusType;
import com.azure.ai.metricsadvisor.implementation.models.AnomalyDimensionQuery;
import com.azure.ai.metricsadvisor.implementation.models.DetectionAnomalyFilterCondition;
import com.azure.ai.metricsadvisor.implementation.models.DetectionAnomalyResultQuery;
import com.azure.ai.metricsadvisor.implementation.models.DetectionIncidentFilterCondition;
import com.azure.ai.metricsadvisor.implementation.models.DetectionIncidentResultQuery;
import com.azure.ai.metricsadvisor.implementation.models.DimensionGroupIdentity;
import com.azure.ai.metricsadvisor.implementation.models.EnrichmentStatusQueryOption;
import com.azure.ai.metricsadvisor.implementation.models.MetricDataQueryOptions;
import com.azure.ai.metricsadvisor.implementation.models.MetricDimensionQueryOptions;
import com.azure.ai.metricsadvisor.implementation.models.MetricSeriesQueryOptions;
import com.azure.ai.metricsadvisor.models.AnomalyIncident;
import com.azure.ai.metricsadvisor.models.DimensionKey;
import com.azure.ai.metricsadvisor.models.ListAnomaliesDetectedOptions;
import com.azure.ai.metricsadvisor.models.ListAnomalyDimensionValuesOptions;
import com.azure.ai.metricsadvisor.models.ListIncidentsDetectedOptions;
import com.azure.ai.metricsadvisor.models.ListMetricDimensionValuesOptions;
import com.azure.ai.metricsadvisor.models.ListMetricSeriesDefinitionOptions;
import com.azure.ai.metricsadvisor.models.MetricFeedback;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Helper utility class to manage common methods.
 */
public final class Utility {
    private static final ClientLogger LOGGER = new ClientLogger(Utility.class);
    private static final String HTTP_REST_PROXY_SYNC_PROXY_ENABLE = "com.azure.core.http.restproxy.syncproxy.enable";
    private static final Context CONTEXT_WITH_SYNC = new Context(HTTP_REST_PROXY_SYNC_PROXY_ENABLE, true);

    /**
     * Extracts the result ID from the location URL.
     *
     * @param operationLocation The URL specified in the 'Location' response header containing the
     * resultId used to track the progress and obtain the result of the operation.
     *
     * @return The resultId used to track the progress.
     */
    public static String parseOperationId(String operationLocation) {
        if (!CoreUtils.isNullOrEmpty(operationLocation)) {
            int lastIndex = operationLocation.lastIndexOf('/');
            if (lastIndex != -1) {
                return operationLocation.substring(lastIndex + 1);
            }
        }
        throw LOGGER.logExceptionAsError(
            new RuntimeException("Failed to parse operation header for result Id from: " + operationLocation));
    }

    /**
     * Returns toString or null if object passed is null.
     * @param obj the object
     * @return Returns toString or null if object passed is null.
     */
    public static String toStringOrNull(Object obj) {
        return obj != null ? obj.toString() : null;
    }

    public static Context enableSync(Context context) {
        if (context == null || context == Context.NONE) {
            return CONTEXT_WITH_SYNC;
        }

        return context.addData(HTTP_REST_PROXY_SYNC_PROXY_ENABLE, true);
    }

    public static List<DataFeedIngestionStatus> toDataFeedIngestionStatus(List<com.azure.ai.metricsadvisor.implementation.models.DataFeedIngestionStatus> ingestionStatusList) {
        return ingestionStatusList
            .stream()
            .map(ingestionStatus -> {
                DataFeedIngestionStatus dataFeedIngestionStatus = new DataFeedIngestionStatus();
                DataFeedIngestionStatusHelper.setMessage(dataFeedIngestionStatus, ingestionStatus.getMessage());
                DataFeedIngestionStatusHelper.setIngestionStatusType(dataFeedIngestionStatus, IngestionStatusType.fromString(toStringOrNull(ingestionStatus.getStatus())));
                DataFeedIngestionStatusHelper.setTimestamp(dataFeedIngestionStatus, ingestionStatus.getTimestamp());
                return dataFeedIngestionStatus;
            })
            .collect(Collectors.toList());
    }

    public static DataFeedIngestionProgress toDataFeedIngestionProgress(
        com.azure.ai.metricsadvisor.implementation.models.DataFeedIngestionProgress dataFeedIngestionProgressResponse) {
        DataFeedIngestionProgress dataFeedIngestionProgress = new DataFeedIngestionProgress();
        DataFeedIngestionProgressHelper.setLatestActiveTimestamp(dataFeedIngestionProgress, dataFeedIngestionProgressResponse.getLatestActiveTimestamp());
        DataFeedIngestionProgressHelper.setLatestSuccessTimestamp(dataFeedIngestionProgress, dataFeedIngestionProgressResponse.getLatestSuccessTimestamp());
        return dataFeedIngestionProgress;
    }

    public static ListAnomaliesDetectedOptions getListAnomaliesDetectedOptions(ListAnomaliesDetectedOptions options,
                                                                        DetectionAnomalyResultQuery query,
                                                                        ClientLogger logger) {
        if (options == null) {
            options = new ListAnomaliesDetectedOptions();
        }

        if (options.getFilter() != null) {
            DetectionAnomalyFilterCondition innerFilter = AnomalyTransforms.toInnerFilter(options.getFilter(),
                logger);
            if (innerFilter != null) {
                query.setFilter(innerFilter);
            }
        }
        return options;
    }

    public static ListIncidentsDetectedOptions getListIncidentsDetectedOptions(ListIncidentsDetectedOptions options,
                                                                                DetectionIncidentResultQuery query) {
        if (options == null) {
            options = new ListIncidentsDetectedOptions();
        }
        if (options.getDimensionsToFilter() != null) {
            List<DimensionGroupIdentity> innerDimensionsToFilter = new ArrayList<>();
            for (DimensionKey dimensionToFilter : options.getDimensionsToFilter()) {
                innerDimensionsToFilter.add(new DimensionGroupIdentity()
                    .setDimension(dimensionToFilter.asMap()));
            }
            if (!innerDimensionsToFilter.isEmpty()) {
                query.setFilter(new DetectionIncidentFilterCondition()
                    .setDimensionFilter(innerDimensionsToFilter));
            }
        }
        return options;
    }

    public static ListAnomalyDimensionValuesOptions getListAnomalyDimensionValuesOptions(
        ListAnomalyDimensionValuesOptions options, AnomalyDimensionQuery query) {
        if (options == null) {
            options = new ListAnomalyDimensionValuesOptions();
        }
        if (options.getDimensionToFilter() != null) {
            query.setDimensionFilter(new DimensionGroupIdentity()
                .setDimension(options.getDimensionToFilter().asMap()));
        }
        return options;
    }

    public static MetricDimensionQueryOptions getMetricDimensionQueryOptions(String dimensionName,
                                                                              ListMetricDimensionValuesOptions options) {
        if (options == null) {
            options = new ListMetricDimensionValuesOptions();
        }
        return new MetricDimensionQueryOptions()
            .setDimensionName(dimensionName).setDimensionValueFilter(options.getDimensionValueToFilter());
    }

    public static MetricDataQueryOptions getMetricDataQueryOptions(OffsetDateTime startTime,
                                                                    List<Map<String, String>> dimensionList) {
        final MetricDataQueryOptions metricDataQueryOptions
            = new MetricDataQueryOptions()
            .setStartTime(startTime)
            .setEndTime(startTime)
            .setSeries(dimensionList);
        return metricDataQueryOptions;
    }

    public static MetricSeriesQueryOptions getMetricSeriesQueryOptions(OffsetDateTime activeSince,
                                                                        ListMetricSeriesDefinitionOptions options) {
        final MetricSeriesQueryOptions metricSeriesQueryOptions = new MetricSeriesQueryOptions()
            .setActiveSince(activeSince).setDimensionFilter(options.getDimensionCombinationsToFilter());
        return metricSeriesQueryOptions;
    }
    public static EnrichmentStatusQueryOption getEnrichmentStatusQueryOptions(OffsetDateTime startTime,
                                                                              OffsetDateTime endTime) {
        final EnrichmentStatusQueryOption enrichmentStatusQueryOption =
            new EnrichmentStatusQueryOption().setStartTime(startTime).setEndTime(endTime);
        return enrichmentStatusQueryOption;
    }

    public static void validateMetricEnrichedSeriesInputs(String detectionConfigurationId, List<DimensionKey> seriesKeys, OffsetDateTime startTime,
                                                          OffsetDateTime endTime, ClientLogger logger) {
        Objects.requireNonNull(seriesKeys, "'seriesKeys' is required.");
        if (seriesKeys.isEmpty()) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("'seriesKeys' cannot be empty."));
        }
        validateIncidentsForDetectionConfigInputs(detectionConfigurationId, startTime, endTime);
    }

    public static void validateIncidentsForDetectionConfigInputs(String detectionConfigurationId, OffsetDateTime startTime,
                                                          OffsetDateTime endTime) {
        Objects.requireNonNull(detectionConfigurationId, "'detectionConfigurationId' is required.");
        validateStartEndTime(startTime, endTime);
    }

    public static void validateAnomalyIncidentRootCausesInputs(AnomalyIncident anomalyIncident, ClientLogger logger) {
        if (anomalyIncident == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'anomalyIncident' is required."));
        }
        Objects.requireNonNull(anomalyIncident.getDetectionConfigurationId(),
            "'anomalyIncident.detectionConfigurationId' is required.");
        Objects.requireNonNull(anomalyIncident.getId(), "'anomalyIncident.id' is required");
    }

    public static void validateAnomalyDimensionValuesInputs(String detectionConfigurationId, String dimensionName, OffsetDateTime startTime,
                                                             OffsetDateTime endTime) {
        Objects.requireNonNull(dimensionName, "'dimensionName' is required.");
        validateIncidentsForDetectionConfigInputs(detectionConfigurationId, startTime, endTime);
    }

    public static void validateListAlertsInputs(String alertConfigurationId, OffsetDateTime startTime, OffsetDateTime endTime) {
        Objects.requireNonNull(alertConfigurationId, "'alertConfigurationId' is required.");
        validateStartEndTime(startTime, endTime);
    }

    public static void validateActiveSinceInput(OffsetDateTime activeSince, ClientLogger logger) {
        if (activeSince == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'activeSince' is required and cannot be null."));
        }
    }

    public static void validateListAnomaliesInputs(String alertConfigurationId, String alertId) {
        Objects.requireNonNull(alertConfigurationId, "'alertConfigurationId' is required.");
        Objects.requireNonNull(alertId, "'alertId' is required.");
    }

    public static void validateAddFeedbackInputs(String metricId, MetricFeedback metricFeedback) {
        Objects.requireNonNull(metricId, "'metricId' is required.");
        Objects.requireNonNull(metricFeedback, "'metricFeedback' is required.");
        Objects.requireNonNull(metricFeedback.getDimensionFilter(),
            "'metricFeedback.dimensionFilter' is required.");
    }

    public static void validateMetricSeriesInputs(String metricId, List<DimensionKey> seriesKeys, OffsetDateTime startTime,
                                            OffsetDateTime endTime, ClientLogger logger) {
        Objects.requireNonNull(metricId, "'metricId' cannot be null.");
        validateStartEndTime(startTime, endTime);
        if (CoreUtils.isNullOrEmpty(seriesKeys)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("'seriesKeys' cannot be null or empty."));
        }
    }

    public static void validateMetricEnrichmentStatusInputs(String metricId, String message, OffsetDateTime startTime, OffsetDateTime endTime) {
        Objects.requireNonNull(metricId, message);
        validateStartEndTime(startTime, endTime);
    }

    public static void validateStartEndTime(OffsetDateTime startTime, OffsetDateTime endTime) {
        Objects.requireNonNull(startTime, "'startTime' is required.");
        Objects.requireNonNull(endTime, "'endTime' is required.");
    }
}
