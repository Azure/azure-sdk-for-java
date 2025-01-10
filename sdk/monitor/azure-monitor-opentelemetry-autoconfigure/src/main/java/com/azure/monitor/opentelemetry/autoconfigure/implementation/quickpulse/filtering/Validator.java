// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.PredicateType;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.TelemetryType;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.DerivedMetricInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.FilterConjunctionGroupInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.DocumentFilterConjunctionGroupInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.FilterInfo;

import java.util.HashSet;
import java.util.Set;
import java.util.Optional;

import static java.util.Arrays.asList;

public class Validator {
    private final Set<String> knownStringColumns
        = new HashSet<String>(asList(KnownRequestColumns.URL, KnownRequestColumns.NAME, KnownDependencyColumns.DATA,
            KnownDependencyColumns.TARGET, KnownDependencyColumns.TYPE, KnownTraceColumns.MESSAGE,
            KnownExceptionColumns.MESSAGE, KnownExceptionColumns.STACK));

    private final Set<String> knownNumericColumns = new HashSet<>(
        asList(KnownRequestColumns.RESPONSE_CODE, KnownRequestColumns.DURATION, KnownDependencyColumns.RESULT_CODE));

    private final Set<PredicateType> validStringPredicates = new HashSet<>(
        asList(PredicateType.CONTAINS, PredicateType.DOES_NOT_CONTAIN, PredicateType.EQUAL, PredicateType.NOT_EQUAL));

    // The string in the return Optional represents an error encountered when validating the derived metric info.
    // If the Optional is empty, that means validation passed.
    public Optional<String> validateDerivedMetricInfo(DerivedMetricInfo derivedMetricInfo) {
        TelemetryType telemetryType = TelemetryType.fromString(derivedMetricInfo.getTelemetryType());
        if (!isValidTelemetryType(telemetryType)) {
            return Optional
                .of("The user selected a telemetry type that the SDK does not support for Live Metrics Filtering. "
                    + "Telemetry type: " + telemetryType);
        }

        if (!isNotCustomMetricProjection(derivedMetricInfo.getProjection())) {
            return Optional.of("The user selected a projection of Custom Metric, which this SDK does not support.");
        }

        for (FilterConjunctionGroupInfo conjunctionGroupInfo : derivedMetricInfo.getFilterGroups()) {
            for (FilterInfo filter : conjunctionGroupInfo.getFilters()) {
                Optional<String> error = validateFieldName(filter.getFieldName());
                if (error.isPresent()) {
                    return error;
                }

                error = validatePredicateAndComparand(filter);
                if (error.isPresent()) {
                    return error;
                }
            }
        }
        return Optional.empty();
    }

    // The string in the return Optional represents an error encountered when validating the DocumentFilterConjunctionGroupInfo.
    // If the Optional is empty, that means validation passed.
    public Optional<String>
        validateDocConjunctionGroupInfo(DocumentFilterConjunctionGroupInfo documentFilterConjunctionGroupInfo) {
        TelemetryType telemetryType = documentFilterConjunctionGroupInfo.getTelemetryType();
        if (!isValidTelemetryType(telemetryType)) {
            return Optional
                .of("The user selected a telemetry type that the SDK does not support for Live Metrics Filtering. "
                    + "Telemetry type: " + telemetryType);
        }

        FilterConjunctionGroupInfo conjunctionGroupInfo = documentFilterConjunctionGroupInfo.getFilters();
        for (FilterInfo filter : conjunctionGroupInfo.getFilters()) {
            Optional<String> error = validateFieldName(filter.getFieldName());
            if (error.isPresent()) {
                return error;
            }

            error = validatePredicateAndComparand(filter);
            if (error.isPresent()) {
                return error;
            }
        }
        return Optional.empty();
    }

    private boolean isValidTelemetryType(TelemetryType telemetryType) {
        if (telemetryType.equals(TelemetryType.PERFORMANCE_COUNTER)) {
            return false;
        } else if (telemetryType.equals(TelemetryType.EVENT)) {
            return false;
        } else if (telemetryType.equals(TelemetryType.METRIC)) {
            return false;
        }
        return true;

    }

    private boolean isNotCustomMetricProjection(String projection) {
        if (projection.startsWith("CustomMetrics.")) {
            return false;
        }
        return true;
    }

    // The string in the return Optional represents an error encountered when validating the field name.
    // If the Optional is empty, that means validation passed.
    private Optional<String> validateFieldName(String fieldName) {
        if (fieldName.isEmpty()) {
            return Optional.of("The user specified an empty field name for a filter.");
        }
        if (fieldName.startsWith("CustomMetrics.")) {
            return Optional.of(
                "The user selected a custom metric field name, but this SDK does not support filtering of custom metrics.");
        }
        return Optional.empty();
    }

    // The string in the return Optional represents an error encountered when validating the predicate/comparand.
    // If the Optional is empty, that means validation passed.
    private Optional<String> validatePredicateAndComparand(FilterInfo filter) {
        if (filter.getComparand().isEmpty()) {
            // It is possible to not type in a comparand and the service side to send us empty string.
            return Optional.of("The user specified an empty comparand value for a filter.");
        } else if (Filter.ANY_FIELD.equals(filter.getFieldName())
            && !(filter.getPredicate().equals(PredicateType.CONTAINS)
                || filter.getPredicate().equals(PredicateType.DOES_NOT_CONTAIN))) {
            // While the UI allows != and == for the ANY_FIELD fieldName, .net classic code only allows contains/not contains & the spec follows
            // .net classic behavior for this particular condition.
            return Optional.of("The specified predicate is not supported for the fieldName * (Any field): "
                + filter.getPredicate().getValue());
        } else if (knownNumericColumns.contains(filter.getFieldName())) {
            // Just in case a strange timestamp value is passed from the service side. The service side should send a duration with a specific
            // format ([days].[hours]:[minutes]:[seconds] - the seconds may be a whole number or something like 7.89).
            if (KnownDependencyColumns.DURATION.equals(filter.getFieldName())) {
                if (Filter.getMicroSecondsFromFilterTimestampString(filter.getComparand()) == Long.MIN_VALUE) {
                    return Optional
                        .of("The duration string provided by the user could not be parsed to a numeric value: "
                            + filter.getComparand());
                }
            } else { // The service side not does not validate if resultcode or responsecode is a numeric value
                try {
                    Long.parseLong(filter.getComparand());
                } catch (NumberFormatException e) {
                    return Optional
                        .of("The result/response code specified by the user did not parse to a numeric value: "
                            + filter.getComparand());
                }
            }
        } else if (knownStringColumns.contains(filter.getFieldName())
            || filter.getFieldName().startsWith(Filter.CUSTOM_DIM_FIELDNAME_PREFIX)) {
            // While the UI allows a user to select any predicate for a custom dimension filter, .net classic treats all custom dimensions like
            // String values. therefore we validate for predicates applicable to String. This is called out in the spec as well.
            if (!validStringPredicates.contains(filter.getPredicate())) {
                return Optional.of("The user selected a predicate (" + filter.getPredicate().getValue()
                    + ") that is not supported for the field name " + filter.getFieldName());
            }
        }
        return Optional.empty();
    }
}
