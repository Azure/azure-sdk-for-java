// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.DerivedMetricInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.FilterConjunctionGroupInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.CollectionConfigurationInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.DocumentStreamInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.DocumentFilterConjunctionGroupInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.TelemetryType;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;

public class FilteringConfiguration {
    private final Set<String> seenMetricIds = new HashSet<>();

    // key is the telemetry type
    private final Map<TelemetryType, List<DerivedMetricInfo>> validDerivedMetricInfos;

    // first key is the telemetry type, second key is the id associated with the document filters
    private final Map<TelemetryType, Map<String, List<FilterConjunctionGroupInfo>>> validDocumentFilterConjunctionGroupInfos;

    private final String etag;

    public FilteringConfiguration() {
        validDerivedMetricInfos = new HashMap<>();
        validDocumentFilterConjunctionGroupInfos = new HashMap<>();
        etag = "";
    }

    public FilteringConfiguration(CollectionConfigurationInfo configuration) {
        validDerivedMetricInfos = parseMetricFilterConfiguration(configuration);
        validDocumentFilterConjunctionGroupInfos = parseDocumentFilterConfiguration(configuration);
        etag = configuration.getETag();
    }

    public List<DerivedMetricInfo> fetchMetricConfigForTelemetryType(TelemetryType telemetryType) {
        if (validDerivedMetricInfos.containsKey(telemetryType)) {
            return new ArrayList<>(validDerivedMetricInfos.get(telemetryType));
        }
        return new ArrayList<>();
    }

    public Map<String, List<FilterConjunctionGroupInfo>>
        fetchDocumentsConfigForTelemetryType(TelemetryType telemetryType) {
        Map<String, List<FilterConjunctionGroupInfo>> result;
        if (validDocumentFilterConjunctionGroupInfos.containsKey(telemetryType)) {
            result = new HashMap<>(validDocumentFilterConjunctionGroupInfos.get(telemetryType));
        } else {
            result = new HashMap<>();
        }
        return result;
    }

    public String getETag() {
        return etag;
    }

    private Map<TelemetryType, Map<String, List<FilterConjunctionGroupInfo>>>
        parseDocumentFilterConfiguration(CollectionConfigurationInfo configuration) {
        Map<TelemetryType, Map<String, List<FilterConjunctionGroupInfo>>> result = new HashMap<>();
        for (DocumentStreamInfo documentStreamInfo : configuration.getDocumentStreams()) {
            String documentStreamId = documentStreamInfo.getId();
            for (DocumentFilterConjunctionGroupInfo documentFilterGroupInfo : documentStreamInfo
                .getDocumentFilterGroups()) {
                TelemetryType telemetryType = documentFilterGroupInfo.getTelemetryType();
                FilterConjunctionGroupInfo filterGroup = documentFilterGroupInfo.getFilters();

                // TODO (harskaur): In later PR, validate input before adding it to newValidDocumentsConfig
                // TODO (harskaur): If any validator methods throw an exception, catch the exception and track the error for post request body

                if (!result.containsKey(telemetryType)) {
                    result.put(telemetryType, new HashMap<>());
                }

                Map<String, List<FilterConjunctionGroupInfo>> innerMap = result.get(telemetryType);
                if (innerMap.containsKey(documentStreamId)) {
                    innerMap.get(documentStreamId).add(filterGroup);
                } else {
                    List<FilterConjunctionGroupInfo> filterGroups = new ArrayList<>();
                    filterGroups.add(filterGroup);
                    innerMap.put(documentStreamId, filterGroups);
                }
            }
        }
        return result;
    }

    private Map<TelemetryType, List<DerivedMetricInfo>>
        parseMetricFilterConfiguration(CollectionConfigurationInfo configuration) {
        Map<TelemetryType, List<DerivedMetricInfo>> result = new HashMap<>();
        for (DerivedMetricInfo derivedMetricInfo : configuration.getMetrics()) {
            TelemetryType telemetryType = TelemetryType.fromString(derivedMetricInfo.getTelemetryType());
            String id = derivedMetricInfo.getId();
            if (!seenMetricIds.contains(id)) {
                seenMetricIds.add(id);
                // TODO (harskaur): In later PR, validate input before adding it to newValidConfig
                // TODO (harskaur): If any validator methods throw an exception, catch the exception and track the error for post request body

                if (result.containsKey(telemetryType)) {
                    result.get(telemetryType).add(derivedMetricInfo);
                } else {
                    List<DerivedMetricInfo> infos = new ArrayList<>();
                    infos.add(derivedMetricInfo);
                    result.put(telemetryType, infos);
                }
            }
        }
        return result;
    }

}
