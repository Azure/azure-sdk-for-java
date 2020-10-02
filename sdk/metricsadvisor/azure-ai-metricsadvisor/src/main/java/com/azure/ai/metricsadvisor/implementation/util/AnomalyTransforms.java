// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.implementation.models.AnomalyResult;
import com.azure.ai.metricsadvisor.implementation.models.DetectionAnomalyFilterCondition;
import com.azure.ai.metricsadvisor.implementation.models.DimensionGroupIdentity;
import com.azure.ai.metricsadvisor.implementation.models.SeverityFilterCondition;
import com.azure.ai.metricsadvisor.models.Anomaly;
import com.azure.ai.metricsadvisor.models.DimensionKey;
import com.azure.ai.metricsadvisor.models.ListAnomaliesDetectedFilter;
import com.azure.ai.metricsadvisor.models.Severity;
import com.azure.core.http.rest.Page;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AnomalyTransforms {
    public static DetectionAnomalyFilterCondition toInnerFilter(ListAnomaliesDetectedFilter filter,
                                                                ClientLogger logger) {
        DetectionAnomalyFilterCondition innerFilter = new DetectionAnomalyFilterCondition();
        Severity minSeverity = filter.getMinSeverity();
        Severity maxSeverity = filter.getMaxSeverity();
        if ((minSeverity != null) ^ (maxSeverity != null)) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "Both min and max severity are required if anomalies needs to be filtered based on 'severity'"));
        }
        if (minSeverity != null) {
            innerFilter.setSeverityFilter(new SeverityFilterCondition()
                .setMin(minSeverity).
                    setMax(maxSeverity));
        }
        List<DimensionKey> seriesKeys = filter.getSeriesKeys();
        if (seriesKeys != null && !seriesKeys.isEmpty()) {
            innerFilter.setDimensionFilter(seriesKeys
                .stream()
                .map(key -> new DimensionGroupIdentity().setDimension(key.asMap()))
                .collect(Collectors.toList()));
        }

        if (innerFilter.getSeverityFilter() != null
            || innerFilter.getDimensionFilter() != null) {
            return innerFilter;
        } else {
            return null;
        }
    }

    public static PagedResponse<Anomaly> fromInnerPagedResponse(PagedResponse<AnomalyResult> innerResponse) {
        List<Anomaly> anomalyList;
        final List<AnomalyResult> innerAnomalyList = innerResponse.getValue();
        if (innerAnomalyList == null || innerAnomalyList.isEmpty()) {
            anomalyList = new ArrayList<>();
        } else {
            anomalyList = innerAnomalyList
                .stream()
                .map(innerAnomaly -> fromInner(innerAnomaly))
                .collect(Collectors.toList());
        }

        final IterableStream<Anomaly> pageElements
            = new IterableStream<>(anomalyList);

        return new PagedResponseBase<Void, Anomaly>(innerResponse.getRequest(),
            innerResponse.getStatusCode(),
            innerResponse.getHeaders(),
            new AnomalyPage(pageElements, innerResponse.getContinuationToken()),
            null);
    }

    private static Anomaly fromInner(AnomalyResult innerAnomaly) {
        Anomaly anomaly = new Anomaly();
        if (innerAnomaly.getMetricId() != null) {
            PrivateFieldAccessHelper.set(anomaly, "metricId",
                innerAnomaly.getMetricId().toString());
        }
        if (innerAnomaly.getAnomalyDetectionConfigurationId() != null) {
            PrivateFieldAccessHelper.set(anomaly, "detectionConfigurationId",
                innerAnomaly.getAnomalyDetectionConfigurationId().toString());
        }
        if (innerAnomaly.getDimension() != null) {
            DimensionKey seriesId = new DimensionKey(innerAnomaly.getDimension());
            PrivateFieldAccessHelper.set(anomaly, "seriesKey", seriesId);
        }
        if (innerAnomaly.getProperty() != null) {
            PrivateFieldAccessHelper.set(anomaly, "severity",
                innerAnomaly.getProperty().getAnomalySeverity());
            PrivateFieldAccessHelper.set(anomaly, "status",
                innerAnomaly.getProperty().getAnomalyStatus());
        }

        PrivateFieldAccessHelper.set(anomaly, "timeStamp",
            innerAnomaly.getTimestamp());
        PrivateFieldAccessHelper.set(anomaly, "createdTime",
            innerAnomaly.getCreatedTime());
        PrivateFieldAccessHelper.set(anomaly, "modifiedTime",
            innerAnomaly.getModifiedTime());

        return anomaly;
    }

    private static final class AnomalyPage implements Page<Anomaly> {
        private final IterableStream<Anomaly> elements;
        private final String continuationTToken;

        private AnomalyPage(IterableStream<Anomaly> elements, String continuationTToken) {
            this.elements = elements;
            this.continuationTToken = continuationTToken;
        }

        @Override
        public IterableStream<Anomaly> getElements() {
            return this.elements;
        }

        @Override
        public String getContinuationToken() {
            return this.continuationTToken;
        }
    }
}
