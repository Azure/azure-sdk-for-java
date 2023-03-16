// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.implementation.models.AnomalyResult;
import com.azure.ai.metricsadvisor.implementation.models.DetectionAnomalyFilterCondition;
import com.azure.ai.metricsadvisor.implementation.models.DimensionGroupIdentity;
import com.azure.ai.metricsadvisor.implementation.models.SeverityFilterCondition;
import com.azure.ai.metricsadvisor.administration.models.AnomalySeverity;
import com.azure.ai.metricsadvisor.models.DataPointAnomaly;
import com.azure.ai.metricsadvisor.models.DimensionKey;
import com.azure.ai.metricsadvisor.models.ListAnomaliesDetectedFilter;
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
        AnomalySeverity minSeverity = filter.getMinSeverity();
        AnomalySeverity maxSeverity = filter.getMaxSeverity();
        if ((minSeverity != null) ^ (maxSeverity != null)) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "Both min and max severity are required if anomalies needs to be filtered based on 'severity'"));
        }
        if (minSeverity != null) {
            innerFilter.setSeverityFilter(new SeverityFilterCondition()
                .setMin(minSeverity).
                    setMax(maxSeverity));
        }
        List<DimensionKey> seriesKeys = filter.getSeriesGroupKeys();
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

    public static PagedResponse<DataPointAnomaly> fromInnerPagedResponse(PagedResponse<AnomalyResult> innerResponse) {
        List<DataPointAnomaly> dataPointAnomalyList;
        final List<AnomalyResult> innerAnomalyList = innerResponse.getValue();
        if (innerAnomalyList == null || innerAnomalyList.isEmpty()) {
            dataPointAnomalyList = new ArrayList<>();
        } else {
            dataPointAnomalyList = innerAnomalyList
                .stream()
                .map(innerAnomaly -> fromInner(innerAnomaly))
                .collect(Collectors.toList());
        }

        final IterableStream<DataPointAnomaly> pageElements
            = new IterableStream<>(dataPointAnomalyList);

        return new PagedResponseBase<Void, DataPointAnomaly>(innerResponse.getRequest(),
            innerResponse.getStatusCode(),
            innerResponse.getHeaders(),
            new AnomalyPage(pageElements, innerResponse.getContinuationToken()),
            null);
    }

    private static DataPointAnomaly fromInner(AnomalyResult innerAnomaly) {
        DataPointAnomaly dataPointAnomaly = new DataPointAnomaly();
        if (innerAnomaly.getDataFeedId() != null) {
            AnomalyHelper.setDataFeedId(dataPointAnomaly, innerAnomaly.getDataFeedId().toString());
        }
        if (innerAnomaly.getMetricId() != null) {
            AnomalyHelper.setMetricId(dataPointAnomaly, innerAnomaly.getMetricId().toString());
        }
        if (innerAnomaly.getAnomalyDetectionConfigurationId() != null) {
            AnomalyHelper.setDetectionConfigurationId(dataPointAnomaly,
                innerAnomaly.getAnomalyDetectionConfigurationId().toString());
        }
        if (innerAnomaly.getDimension() != null) {
            AnomalyHelper.setSeriesKey(dataPointAnomaly, new DimensionKey(innerAnomaly.getDimension()));
        }
        if (innerAnomaly.getProperty() != null) {
            AnomalyHelper.setSeverity(dataPointAnomaly, innerAnomaly.getProperty().getAnomalySeverity());
            AnomalyHelper.setStatus(dataPointAnomaly, innerAnomaly.getProperty().getAnomalyStatus());
            AnomalyHelper.setValue(dataPointAnomaly, innerAnomaly.getProperty().getValue());
            AnomalyHelper.setExpectedValue(dataPointAnomaly, innerAnomaly.getProperty().getExpectedValue());
        }

        AnomalyHelper.setTimeStamp(dataPointAnomaly, innerAnomaly.getTimestamp());
        AnomalyHelper.setCreatedTime(dataPointAnomaly, innerAnomaly.getCreatedTime());
        AnomalyHelper.setModifiedTime(dataPointAnomaly, innerAnomaly.getModifiedTime());
        return dataPointAnomaly;
    }

    private static final class AnomalyPage implements Page<DataPointAnomaly> {
        private final IterableStream<DataPointAnomaly> elements;
        private final String continuationTToken;

        private AnomalyPage(IterableStream<DataPointAnomaly> elements, String continuationTToken) {
            this.elements = elements;
            this.continuationTToken = continuationTToken;
        }

        @Override
        public IterableStream<DataPointAnomaly> getElements() {
            return this.elements;
        }

        @Override
        public String getContinuationToken() {
            return this.continuationTToken;
        }
    }
}
