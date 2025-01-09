// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.PredicateType;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.TelemetryType;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.DerivedMetricInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.FilterConjunctionGroupInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.DocumentFilterConjunctionGroupInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.FilterInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.CollectionConfigurationError;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.CollectionConfigurationErrorType;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.KeyValuePairString;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    private final List<CollectionConfigurationError> errors = new ArrayList<>();

    private static final ClientLogger LOGGER = new ClientLogger(Validator.class);

    public boolean isValidDerivedMetricInfo(DerivedMetricInfo derivedMetricInfo, String etag) {
        TelemetryType telemetryType = TelemetryType.fromString(derivedMetricInfo.getTelemetryType());
        if (!isValidTelemetryType(telemetryType)) {
            constructAndTrackCollectionConfigurationError(
                CollectionConfigurationErrorType.METRIC_TELEMETRY_TYPE_UNSUPPORTED,
                "The user selected a telemetry type that the SDK does not support for Live Metrics Filtering. "
                    + "Telemetry type: " + telemetryType,
                etag, derivedMetricInfo.getId(), true);
            return false;
        }

        if (!isNotCustomMetricProjection(derivedMetricInfo.getProjection())) {
            constructAndTrackCollectionConfigurationError(
                CollectionConfigurationErrorType.FILTER_FAILURE_TO_CREATE_UNEXPECTED,
                "The user selected a projection of Custom Metric, which this SDK does not support.", etag,
                derivedMetricInfo.getId(), true);
            return false;
        }

        for (FilterConjunctionGroupInfo conjunctionGroupInfo : derivedMetricInfo.getFilterGroups()) {
            for (FilterInfo filter : conjunctionGroupInfo.getFilters()) {
                if (!isValidFieldName(filter.getFieldName(), etag, derivedMetricInfo.getId(), true)) {
                    return false;
                }
                if (!isValidPredicateAndComparand(filter, etag, derivedMetricInfo.getId(), true)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isValidDocConjunctionGroupInfo(DocumentFilterConjunctionGroupInfo documentFilterConjunctionGroupInfo,
        String etag, String id) {
        TelemetryType telemetryType = documentFilterConjunctionGroupInfo.getTelemetryType();
        if (!isValidTelemetryType(telemetryType)) {
            constructAndTrackCollectionConfigurationError(
                CollectionConfigurationErrorType.METRIC_TELEMETRY_TYPE_UNSUPPORTED,
                "The user selected a telemetry type that the SDK does not support for Live Metrics Filtering. "
                    + "Telemetry type: " + telemetryType,
                etag, id, false);
            return false;
        }

        FilterConjunctionGroupInfo conjunctionGroupInfo = documentFilterConjunctionGroupInfo.getFilters();
        for (FilterInfo filter : conjunctionGroupInfo.getFilters()) {
            if (!isValidFieldName(filter.getFieldName(), etag, id, false)) {
                return false;
            }
            if (!isValidPredicateAndComparand(filter, etag, id, false)) {
                return false;
            }
        }
        return true;
    }

    public void constructAndTrackCollectionConfigurationError(CollectionConfigurationErrorType errorType,
        String message, String eTag, String id, boolean isDerivedMetricId) {
        CollectionConfigurationError error = new CollectionConfigurationError();
        error.setMessage(message);
        error.setCollectionConfigurationErrorType(errorType);

        KeyValuePairString keyValuePair1 = new KeyValuePairString();
        keyValuePair1.setKey("ETag");
        keyValuePair1.setValue(eTag);

        KeyValuePairString keyValuePair2 = new KeyValuePairString();
        keyValuePair2.setKey(isDerivedMetricId ? "DerivedMetricInfoId" : "DocumentStreamInfoId");
        keyValuePair2.setValue(id);

        List<KeyValuePairString> data = new ArrayList<>();
        data.add(keyValuePair1);
        data.add(keyValuePair2);

        error.setData(data);

        errors.add(error);
        // This message gets logged once for every error we see on config validation. Config validation
        // only happens once per config change.
        LOGGER.verbose("{}. Due to this misconfiguration the {} rule will be ignored by the SDK.", message,
            isDerivedMetricId ? "derived metric" : "document filter conjunction");
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

    private boolean isValidFieldName(String fieldName, String etag, String id, boolean isDerivedMetricId) {
        if (fieldName.isEmpty()) {
            constructAndTrackCollectionConfigurationError(
                CollectionConfigurationErrorType.FILTER_FAILURE_TO_CREATE_UNEXPECTED,
                "The user specified an empty field name for a filter.", etag, id, isDerivedMetricId);
            return false;
        }
        if (fieldName.startsWith("CustomMetrics.")) {
            constructAndTrackCollectionConfigurationError(
                CollectionConfigurationErrorType.FILTER_FAILURE_TO_CREATE_UNEXPECTED,
                "The user selected a custom metric field name, but this SDK does not support filtering of custom metrics. ",
                etag, id, isDerivedMetricId);
            return false;
        }
        return true;
    }

    private boolean isValidPredicateAndComparand(FilterInfo filter, String etag, String id, boolean isDerivedMetricId) {
        if (filter.getComparand().isEmpty()) {
            // It is possible to not type in a comparand and the service side to send us empty string.
            constructAndTrackCollectionConfigurationError(
                CollectionConfigurationErrorType.FILTER_FAILURE_TO_CREATE_UNEXPECTED,
                "The user specified an empty comparand value for a filter.", etag, id, isDerivedMetricId);
            return false;
        } else if (Filter.ANY_FIELD.equals(filter.getFieldName())
            && !(filter.getPredicate().equals(PredicateType.CONTAINS)
                || filter.getPredicate().equals(PredicateType.DOES_NOT_CONTAIN))) {
            // While the UI allows != and == for the ANY_FIELD fieldName, .net classic code only allows contains/not contains & the spec follows
            // .net classic behavior for this particular condition.
            constructAndTrackCollectionConfigurationError(
                CollectionConfigurationErrorType.FILTER_FAILURE_TO_CREATE_UNEXPECTED,
                "The specified predicate is not supported for the fieldName * (Any field): "
                    + filter.getPredicate().getValue(),
                etag, id, isDerivedMetricId);
            return false;
        } else if (knownNumericColumns.contains(filter.getFieldName())) {
            // Just in case a strange timestamp value is passed from the service side. The service side should send a duration with a specific
            // format ([days].[hours]:[minutes]:[seconds] - the seconds may be a whole number or something like 7.89).
            if (KnownDependencyColumns.DURATION.equals(filter.getFieldName())) {
                if (Filter.getMicroSecondsFromFilterTimestampString(filter.getComparand()) == Long.MIN_VALUE) {
                    constructAndTrackCollectionConfigurationError(
                        CollectionConfigurationErrorType.FILTER_FAILURE_TO_CREATE_UNEXPECTED,
                        "The duration string provided by the user could not be parsed to a numeric value: "
                            + filter.getComparand(),
                        etag, id, isDerivedMetricId);
                    return false;
                }
            } else { // The service side not does not validate if resultcode or responsecode is a numeric value
                try {
                    Long.parseLong(filter.getComparand());
                } catch (NumberFormatException e) {
                    constructAndTrackCollectionConfigurationError(
                        CollectionConfigurationErrorType.FILTER_FAILURE_TO_CREATE_UNEXPECTED,
                        "The result/response code specified by the user did not parse to a numeric value: "
                            + filter.getComparand(),
                        etag, id, isDerivedMetricId);
                    return false;
                }
            }
        } else if (knownStringColumns.contains(filter.getFieldName())
            || filter.getFieldName().startsWith(Filter.CUSTOM_DIM_FIELDNAME_PREFIX)) {
            // While the UI allows a user to select any predicate for a custom dimension filter, .net classic treats all custom dimensions like
            // String values. therefore we validate for predicates applicable to String. This is called out in the spec as well.
            if (!validStringPredicates.contains(filter.getPredicate())) {
                constructAndTrackCollectionConfigurationError(
                    CollectionConfigurationErrorType.FILTER_FAILURE_TO_CREATE_UNEXPECTED,
                    "The user selected a predicate (" + filter.getPredicate().getValue()
                        + ") that is not supported for the field name " + filter.getFieldName(),
                    etag, id, isDerivedMetricId);
                return false;
            }
        }
        return true;
    }

    public List<CollectionConfigurationError> getErrors() {
        return new ArrayList<CollectionConfigurationError>(errors);
    }
}
