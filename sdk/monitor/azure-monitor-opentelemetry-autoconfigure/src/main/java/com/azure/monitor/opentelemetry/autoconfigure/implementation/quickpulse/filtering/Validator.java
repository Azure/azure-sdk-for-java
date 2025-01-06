package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

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

    // TODO (harskaur): In ErrorTracker PR, track a list of configuration validation errors here

    public boolean isValidDerivedMetricInfo(DerivedMetricInfo derivedMetricInfo) {
        TelemetryType telemetryType = TelemetryType.fromString(derivedMetricInfo.getTelemetryType());
        if (!isValidTelemetryType(telemetryType)) {
            return false;
        }

        if (!isNotCustomMetricProjection(derivedMetricInfo.getProjection())) {
            return false;
        }

        for (FilterConjunctionGroupInfo conjunctionGroupInfo : derivedMetricInfo.getFilterGroups()) {
            for (FilterInfo filter : conjunctionGroupInfo.getFilters()) {
                if (!isValidFieldName(filter.getFieldName(), telemetryType)) {
                    return false;
                }
                if (!isValidPredicateAndComparand(filter)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean
        isValidDocConjunctionGroupInfo(DocumentFilterConjunctionGroupInfo documentFilterConjunctionGroupInfo) {
        TelemetryType telemetryType = documentFilterConjunctionGroupInfo.getTelemetryType();
        if (!isValidTelemetryType(telemetryType)) {
            return false;
        }

        FilterConjunctionGroupInfo conjunctionGroupInfo = documentFilterConjunctionGroupInfo.getFilters();
        for (FilterInfo filter : conjunctionGroupInfo.getFilters()) {
            if (!isValidFieldName(filter.getFieldName(), telemetryType)) {
                return false;
            }
            if (!isValidPredicateAndComparand(filter)) {
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

        // TODO (harskaur): For ErrorTracker PR, add this error to list of tracked errors
    }

    private boolean isValidTelemetryType(TelemetryType telemetryType) {
        // TODO (harskaur): In ErrorTracker PR, create an error message & track an error for each false case
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
            // TODO (harskaur): In ErrorTracker PR, create an error message & track an error for this case
            return false;
        }
        return true;
    }

    private boolean isValidFieldName(String fieldName, TelemetryType telemetryType) {
        // TODO (harskaur): In ErrorTracker PR, create an error message & track an error for each false case
        if (fieldName.isEmpty()) {
            return false;
        }
        if (fieldName.startsWith("CustomMetrics.")) {
            return false;
        }
        return true;
    }

    private boolean isValidPredicateAndComparand(FilterInfo filter) {
        // TODO (harskaur): In ErrorTracker PR, create an error message & track an error for each false case
        if (filter.getComparand().isEmpty()) {
            // It is possible to not type in a comparand and the service side to send us empty string.
            return false;
        } else if (Filter.ANY_FIELD.equals(filter.getFieldName())
            && !(filter.getPredicate().equals(PredicateType.CONTAINS)
                || filter.getPredicate().equals(PredicateType.DOES_NOT_CONTAIN))) {
            // While the UI allows != and == for the ANY_FIELD fieldName, .net classic code only allows contains/not contains & the spec follows
            // .net classic behavior for this particular condition.
            return false;
        } else if (knownNumericColumns.contains(filter.getFieldName())) {
            // Just in case a strange timestamp value is passed from the service side. The service side should send a duration with a specific
            // format ([days].[hours]:[minutes]:[seconds] - the seconds may be a whole number or something like 7.89).
            if (KnownDependencyColumns.DURATION.equals(filter.getFieldName())) {
                if (Filter.getMicroSecondsFromFilterTimestampString(filter.getComparand()) == Long.MIN_VALUE) {
                    return false;
                }
            } else { // The service side not does not validate if resultcode or responsecode is a numeric value
                try {
                    Long.parseLong(filter.getComparand());
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        } else if (knownStringColumns.contains(filter.getFieldName())
            || filter.getFieldName().startsWith(Filter.CUSTOM_DIM_FIELDNAME_PREFIX)) {
            // While the UI allows a user to select any predicate for a custom dimension filter, .net classic treats all custom dimensions like
            // String values. therefore we validate for predicates applicable to String. This is called out in the spec as well.
            if (!validStringPredicates.contains(filter.getPredicate())) {
                return false;
            }
        }
        return true;
    }
}
