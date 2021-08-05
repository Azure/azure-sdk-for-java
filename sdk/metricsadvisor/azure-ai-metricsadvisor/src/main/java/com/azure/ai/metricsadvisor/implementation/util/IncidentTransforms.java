// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.implementation.models.IncidentResult;
import com.azure.ai.metricsadvisor.models.AnomalyIncident;
import com.azure.ai.metricsadvisor.models.DimensionKey;
import com.azure.core.http.rest.Page;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.util.IterableStream;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IncidentTransforms {
    public static PagedResponse<AnomalyIncident> fromInnerPagedResponse(PagedResponse<IncidentResult> innerResponse) {
        List<AnomalyIncident> anomalyIncidentList;
        final List<IncidentResult> innerIncidentList = innerResponse.getValue();
        if (innerIncidentList == null || innerIncidentList.isEmpty()) {
            anomalyIncidentList = new ArrayList<>();
        } else {
            anomalyIncidentList = innerIncidentList
                .stream()
                .map(innerConfiguration -> fromInner(innerConfiguration))
                .collect(Collectors.toList());
        }

        final IterableStream<AnomalyIncident> pageElements
            = new IterableStream<>(anomalyIncidentList);

        return new PagedResponseBase<Void, AnomalyIncident>(innerResponse.getRequest(),
            innerResponse.getStatusCode(),
            innerResponse.getHeaders(),
            new IncidentPage(pageElements, innerResponse.getContinuationToken()),
            null);
    }


    private static AnomalyIncident fromInner(IncidentResult innerIncident) {
        AnomalyIncident incident = new AnomalyIncident();
        IncidentHelper.setId(incident, innerIncident.getIncidentId());
        if (innerIncident.getDataFeedId() != null) {
            IncidentHelper.setDataFeedId(incident, innerIncident.getDataFeedId().toString());
        }
        if (innerIncident.getMetricId() != null) {
            IncidentHelper.setMetricId(incident, innerIncident.getMetricId().toString());
        }
        if (innerIncident.getAnomalyDetectionConfigurationId() != null) {
            IncidentHelper.setDetectionConfigurationId(incident,
                innerIncident.getAnomalyDetectionConfigurationId().toString());
        }
        if (innerIncident.getRootNode() != null && innerIncident.getRootNode().getDimension() != null) {
            IncidentHelper.setRootDimensionKey(incident,
                new DimensionKey(innerIncident.getRootNode().getDimension()));
        }
        if (innerIncident.getProperty() != null) {
            IncidentHelper.setSeverity(incident, innerIncident.getProperty().getMaxSeverity());
            IncidentHelper.setStatus(incident, innerIncident.getProperty().getIncidentStatus());
            IncidentHelper.setValue(incident, innerIncident.getProperty().getValueOfRootNode());
            IncidentHelper.setExpectedValue(incident, innerIncident.getProperty().getExpectedValueOfRootNode());
        }

        IncidentHelper.setStartTime(incident, innerIncident.getStartTime());
        IncidentHelper.setLastTime(incident, innerIncident.getLastTime());
        return incident;
    }

    private static final class IncidentPage implements Page<AnomalyIncident> {
        private final IterableStream<AnomalyIncident> elements;
        private final String continuationTToken;

        private IncidentPage(IterableStream<AnomalyIncident> elements, String continuationTToken) {
            this.elements = elements;
            this.continuationTToken = continuationTToken;
        }

        @Override
        public IterableStream<AnomalyIncident> getElements() {
            return this.elements;
        }

        @Override
        public String getContinuationToken() {
            return this.continuationTToken;
        }
    }
}
