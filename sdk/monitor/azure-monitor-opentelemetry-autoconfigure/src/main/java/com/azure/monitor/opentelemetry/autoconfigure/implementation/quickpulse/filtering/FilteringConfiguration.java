// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.util.Arrays.asList;

public class FilteringConfiguration {
    private Set<String> seenMetricIds;

    // key is the telemetry type
    private ConcurrentMap<String, DerivedMetricInfo[]> validDerivedMetricInfos;

    // first key is the telemetry type, second key is the id associated with the document filters
    private ConcurrentMap<String, Map<String, FilterConjunctionGroupInfo[]>> validDocumentFilterConjunctionGroupInfos;

    private volatile String etag;

    public FilteringConfiguration() {
        validDerivedMetricInfos = new ConcurrentHashMap<String, DerivedMetricInfo[]>();
        validDocumentFilterConjunctionGroupInfos = new ConcurrentHashMap<String, Map<String, FilterConjunctionGroupInfo[]>>();
        seenMetricIds = new HashSet<String>();
        etag = "";
    }

    public void updateConfiguration(CollectionConfigurationInfo configuration) {
        this.etag = configuration.getETag();
        parseDocumentFilterConfiguration(configuration);
        parseMetricFilterConfiguration(configuration);
    }

    private void parseDocumentFilterConfiguration(CollectionConfigurationInfo configuration) {

    }

    private void parseMetricFilterConfiguration(CollectionConfigurationInfo configuration) {

    }

    public static class Validator {
        private static final Set<String> knownStringColumns = new HashSet<String>(asList(
            KnownRequestColumns.URL,
            KnownRequestColumns.NAME,
            KnownDependencyColumns.DATA,
            KnownDependencyColumns.TARGET,
            KnownDependencyColumns.TYPE,
            "Message",
            "Exception.Message",
            "Exception.StackTrace"
        ));

        public void validateTelemetryType(TelemetryType telemetryType) throws TelemetryTypeException {
            if (telemetryType.equals(TelemetryType.PERFORMANCE_COUNTER)) {
                throw new TelemetryTypeException("The telemetry type PerformanceCounter was specified, but the distro does not send performance counters other than the default CPU/Mem to quickpulse");
            } else if (telemetryType.equals(TelemetryType.EVENT)) {
                throw new TelemetryTypeException("The telemetry type Event was specified, but the distro does not send events to quickpulse");
            } else if (telemetryType.equals(TelemetryType.METRIC)) {
                throw new TelemetryTypeException("The telemetry type Metric was specified, but the distro does not support sending open telemetry metrics to quickpulse");
            } else if (!TelemetryType.values().contains(telemetryType)) {
                throw new TelemetryTypeException(telemetryType + " is not a valid telemetry type");
            }
        }

        public void checkCustomMetricProjection(DerivedMetricInfo derivedMetricInfo) throws UnexpectedFilterCreateException {
            if (derivedMetricInfo.getProjection().startsWith("CustomMetrics.")) {
                throw new UnexpectedFilterCreateException("The projection of a customMetric property is not supported in this distro.");
            }
        }

        public void validateMetricFilters(DerivedMetricInfo derivedMetricInfo) {
            derivedMetricInfo.getFilterGroups().forEach(filterGroup -> {
                filterGroup.getFilters().forEach(filter -> {
                    TelemetryType telemetryType = TelemetryType.fromString(derivedMetricInfo.getTelemetryType());
                    validateFieldNames(filter.getFieldName(), telemetryType);
                    validatePredicateAndComparand(filter);
                });
            });
        }

        public void validateDocumentFilters(DocumentFilterConjunctionGroupInfo documentFilterConjunctionGroupInfo) {
            FilterConjunctionGroupInfo conjunctionGroupInfo = documentFilterConjunctionGroupInfo.getFilters();
            conjunctionGroupInfo.getFilters().forEach(filter -> {
                validateFieldNames(filter.getFieldName(), documentFilterConjunctionGroupInfo.getTelemetryType());
                validatePredicateAndComparand(filter);
            });
        }

        private boolean isCustomDimOrAnyField(String fieldName) {
            return fieldName.startsWith("CustomDimensions.") || fieldName.equals("*");
        }

        private void validateFieldNames(String fieldName, TelemetryType telemetryType) throws UnexpectedFilterCreateException {
            if (telemetryType.equals(TelemetryType.REQUEST)) {
                if(!this.isCustomDimOrAnyField(fieldName) && !KnownRequestColumns.allColumns.contains(fieldName)) {
                    throw new UnexpectedFilterCreateException(fieldName + " is not a valid field name for the telemetry type Request");
                }
            } else if (telemetryType.equals(TelemetryType.DEPENDENCY)) {

            }
        }
    }


}
