// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.DerivedMetricInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.FilterConjunctionGroupInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.CollectionConfigurationInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.DocumentStreamInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.DocumentFilterConjunctionGroupInfo;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class FilteringConfiguration {
    private final Set<String> seenMetricIds;

    // key is the telemetry type
    private ConcurrentMap<String, List<DerivedMetricInfo>> validDerivedMetricInfos;

    // first key is the telemetry type, second key is the id associated with the document filters
    private ConcurrentMap<String, ConcurrentMap<String, List<FilterConjunctionGroupInfo>>> validDocumentFilterConjunctionGroupInfos;

    private volatile String etag;

    public FilteringConfiguration() {
        validDerivedMetricInfos = new ConcurrentHashMap<>();
        validDocumentFilterConjunctionGroupInfos = new ConcurrentHashMap<>();
        seenMetricIds = new HashSet<>();
        etag = "";
    }

    public synchronized void updateConfiguration(CollectionConfigurationInfo configuration) {
        etag = configuration.getETag();
        seenMetricIds.clear();
        parseDocumentFilterConfiguration(configuration);
        parseMetricFilterConfiguration(configuration);
    }

    public synchronized List<DerivedMetricInfo> fetchMetricConfigForTelemetryType(String telemetryType) {
        List<DerivedMetricInfo> result;
        if (validDerivedMetricInfos.containsKey(telemetryType)) {
            result = new ArrayList<>(validDerivedMetricInfos.get(telemetryType));
        } else {
            result = new ArrayList<>();
        }
        return result;
    }

    public synchronized ConcurrentMap<String, List<FilterConjunctionGroupInfo>>
        fetchDocumentsConfigForTelemetryType(String telemetryType) {
        ConcurrentMap<String, List<FilterConjunctionGroupInfo>> result;
        if (validDocumentFilterConjunctionGroupInfos.containsKey(telemetryType)) {
            result = new ConcurrentHashMap<>(validDocumentFilterConjunctionGroupInfos.get(telemetryType));
        } else {
            result = new ConcurrentHashMap<>();
        }
        return result;
    }

    public synchronized String getETag() {
        return etag;
    }

    private void parseDocumentFilterConfiguration(CollectionConfigurationInfo configuration) {
        ConcurrentMap<String, ConcurrentMap<String, List<FilterConjunctionGroupInfo>>> newValidDocumentsConfig
            = new ConcurrentHashMap<>();
        for (DocumentStreamInfo documentStreamInfo : configuration.getDocumentStreams()) {
            String documentStreamId = documentStreamInfo.getId();
            for (DocumentFilterConjunctionGroupInfo documentFilterGroupInfo : documentStreamInfo
                .getDocumentFilterGroups()) {
                String telemetryType = documentFilterGroupInfo.getTelemetryType().getValue();
                FilterConjunctionGroupInfo filterGroup = documentFilterGroupInfo.getFilters();

                // TODO (harskaur): In later PR, validate input before adding it to newValidDocumentsConfig
                // TODO (harskaur): If any validator methods throw an exception, catch the exception and track the error for post request body

                if (!newValidDocumentsConfig.containsKey(telemetryType)) {
                    newValidDocumentsConfig.put(telemetryType, new ConcurrentHashMap<>());
                }

                ConcurrentMap<String, List<FilterConjunctionGroupInfo>> innerMap
                    = newValidDocumentsConfig.get(telemetryType);
                if (innerMap.containsKey(documentStreamId)) {
                    innerMap.get(documentStreamId).add(filterGroup);
                } else {
                    List<FilterConjunctionGroupInfo> filterGroups = new ArrayList<>();
                    filterGroups.add(filterGroup);
                    innerMap.put(documentStreamId, filterGroups);
                }
            }
        }
        validDocumentFilterConjunctionGroupInfos = newValidDocumentsConfig;
    }

    private void parseMetricFilterConfiguration(CollectionConfigurationInfo configuration) {
        ConcurrentMap<String, List<DerivedMetricInfo>> newValidConfig = new ConcurrentHashMap<>();
        for (DerivedMetricInfo derivedMetricInfo : configuration.getMetrics()) {
            String telemetryType = derivedMetricInfo.getTelemetryType();
            String id = derivedMetricInfo.getId();
            if (!seenMetricIds.contains(id)) {
                seenMetricIds.add(id);
                // TODO (harskaur): In later PR, validate input before adding it to newValidConfig
                // TODO (harskaur): If any validator methods throw an exception, catch the exception and track the error for post request body

                if (newValidConfig.containsKey(telemetryType)) {
                    newValidConfig.get(telemetryType).add(derivedMetricInfo);
                } else {
                    List<DerivedMetricInfo> infos = new ArrayList<>();
                    infos.add(derivedMetricInfo);
                    newValidConfig.put(telemetryType, infos);
                }
            }
        }
        validDerivedMetricInfos = newValidConfig;
    }

}
