// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.implementation.models.IncidentResult;
import com.azure.ai.metricsadvisor.models.DimensionKey;
import com.azure.ai.metricsadvisor.models.Incident;
import com.azure.core.http.rest.Page;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.util.IterableStream;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IncidentTransforms {
    public static PagedResponse<Incident> fromInnerPagedResponse(PagedResponse<IncidentResult> innerResponse) {
        List<Incident> incidentList;
        final List<IncidentResult> innerIncidentList = innerResponse.getValue();
        if (innerIncidentList == null || innerIncidentList.isEmpty()) {
            incidentList = new ArrayList<>();
        } else {
            incidentList = innerIncidentList
                .stream()
                .map(innerConfiguration -> fromInner(innerConfiguration))
                .collect(Collectors.toList());
        }

        final IterableStream<Incident> pageElements
            = new IterableStream<>(incidentList);

        return new PagedResponseBase<Void, Incident>(innerResponse.getRequest(),
            innerResponse.getStatusCode(),
            innerResponse.getHeaders(),
            new IncidentPage(pageElements, innerResponse.getContinuationToken()),
            null);
    }


    private static Incident fromInner(IncidentResult innerIncident) {
        Incident incident = new Incident();
        IncidentHelper.setId(incident, innerIncident.getIncidentId());
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
        }

        IncidentHelper.setStartTime(incident, innerIncident.getStartTime());
        IncidentHelper.setLastTime(incident, innerIncident.getLastTime());
        return incident;
    }

    private static final class IncidentPage implements Page<Incident> {
        private final IterableStream<Incident> elements;
        private final String continuationTToken;

        private IncidentPage(IterableStream<Incident> elements, String continuationTToken) {
            this.elements = elements;
            this.continuationTToken = continuationTToken;
        }

        @Override
        public IterableStream<Incident> getElements() {
            return this.elements;
        }

        @Override
        public String getContinuationToken() {
            return this.continuationTToken;
        }
    }
}
