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
        PrivateFieldAccessHelper.set(incident, "id", innerIncident.getIncidentId());
        if (innerIncident.getMetricId() != null) {
            PrivateFieldAccessHelper.set(incident, "metricId", innerIncident.getMetricId().toString());
        }
        if (innerIncident.getAnomalyDetectionConfigurationId() != null) {
            PrivateFieldAccessHelper.set(incident, "detectionConfigurationId",
                innerIncident.getAnomalyDetectionConfigurationId().toString());
        }
        if (innerIncident.getRootNode() != null && innerIncident.getRootNode().getDimension() != null) {
            PrivateFieldAccessHelper.set(incident,
                "rootDimensionKey",
                new DimensionKey(innerIncident.getRootNode().getDimension()));
        }
        if (innerIncident.getProperty() != null) {
            PrivateFieldAccessHelper.set(incident, "severity",
                innerIncident.getProperty().getMaxSeverity());
            PrivateFieldAccessHelper.set(incident, "status",
                innerIncident.getProperty().getIncidentStatus());
        }

        PrivateFieldAccessHelper.set(incident, "startTime",
            innerIncident.getStartTime());
        PrivateFieldAccessHelper.set(incident, "lastTime",
            innerIncident.getLastTime());

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
