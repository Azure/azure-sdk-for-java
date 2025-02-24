// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.DerivedMetricInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.FilterConjunctionGroupInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.CollectionConfigurationInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.DocumentStreamInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.DocumentFilterConjunctionGroupInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.AggregationType;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.TelemetryType;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.CollectionConfigurationError;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Optional;

public class FilteringConfiguration {

    // key is the telemetry type
    private final Map<TelemetryType, List<DerivedMetricInfo>> validDerivedMetricInfos;

    // first key is the telemetry type, second key is the id associated with the document filters
    private final Map<TelemetryType, Map<String, List<FilterConjunctionGroupInfo>>> validDocumentFilterConjunctionGroupInfos;

    private final String etag;

    // key is the derived metric id
    private final Map<String, AggregationType> validProjectionInfo;

    private final Validator validator = new Validator();

    private final ConfigErrorTracker errorTracker = new ConfigErrorTracker();

    private static final ClientLogger logger = new ClientLogger(FilteringConfiguration.class);

    public FilteringConfiguration() {
        validDerivedMetricInfos = new HashMap<>();
        validDocumentFilterConjunctionGroupInfos = new HashMap<>();
        etag = "";
        validProjectionInfo = new HashMap<>();
        logger.verbose(
            "Initializing an empty live metrics filtering configuration - did not yet receive a configuration from ping or post.");
    }

    public FilteringConfiguration(CollectionConfigurationInfo configuration) {
        logger.verbose("About to parse and validate a new live metrics filtering configuration with etag {}",
            configuration.getETag());
        validDerivedMetricInfos = parseMetricFilterConfiguration(configuration);
        validDocumentFilterConjunctionGroupInfos = parseDocumentFilterConfiguration(configuration);
        etag = configuration.getETag();
        validProjectionInfo = initValidProjectionInfo();
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

    public Map<String, AggregationType> getValidProjectionInitInfo() {
        return new HashMap<>(validProjectionInfo);
    }

    public List<CollectionConfigurationError> getErrors() {
        return errorTracker.getErrors();
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

                Optional<String> docFilterGroupError
                    = validator.validateDocConjunctionGroupInfo(documentFilterGroupInfo);

                if (docFilterGroupError.isPresent()) {
                    errorTracker.addError(docFilterGroupError.get(), configuration.getETag(), documentStreamId, false);
                } else { // passed validation, store valid docFilterGroupInfo
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
        }
        return result;
    }

    private Map<TelemetryType, List<DerivedMetricInfo>>
        parseMetricFilterConfiguration(CollectionConfigurationInfo configuration) {
        Set<String> seenMetricIds = new HashSet<>();
        Map<TelemetryType, List<DerivedMetricInfo>> result = new HashMap<>();
        for (DerivedMetricInfo derivedMetricInfo : configuration.getMetrics()) {
            TelemetryType telemetryType = TelemetryType.fromString(derivedMetricInfo.getTelemetryType());
            String id = derivedMetricInfo.getId();

            if (!seenMetricIds.contains(id)) {
                seenMetricIds.add(id);
                Optional<String> dmiError = validator.validateDerivedMetricInfo(derivedMetricInfo);
                if (dmiError.isPresent()) {
                    errorTracker.addError(dmiError.get(), configuration.getETag(), id, true);

                } else { // validation passed, store valid dmi
                    if (result.containsKey(telemetryType)) {
                        result.get(telemetryType).add(derivedMetricInfo);
                    } else {
                        List<DerivedMetricInfo> infos = new ArrayList<>();
                        infos.add(derivedMetricInfo);
                        result.put(telemetryType, infos);
                    }
                }
            } else {
                errorTracker.addError("A duplicate metric id was found in this configuration", configuration.getETag(),
                    id, true);
            }

        }
        return result;
    }

    private Map<String, AggregationType> initValidProjectionInfo() {
        Map<String, AggregationType> result = new HashMap<>();
        for (List<DerivedMetricInfo> derivedMetricInfoList : validDerivedMetricInfos.values()) {
            for (DerivedMetricInfo derivedMetricInfo : derivedMetricInfoList) {
                result.put(derivedMetricInfo.getId(), derivedMetricInfo.getAggregation());
            }
        }
        return result;
    }

}
