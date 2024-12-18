package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.*;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

public class Validator {
    private static final Set<String> knownStringColumns
        = new HashSet<String>(asList(KnownRequestColumns.URL, KnownRequestColumns.NAME, KnownDependencyColumns.DATA,
        KnownDependencyColumns.TARGET, KnownDependencyColumns.TYPE, KnownTraceColumns.MESSAGE,
        KnownExceptionColumns.MESSAGE, KnownExceptionColumns.STACK));

    private static final Set<String> knownNumericColumns = new HashSet<>(asList(KnownRequestColumns.RESPONSE_CODE,
        KnownRequestColumns.DURATION, KnownDependencyColumns.RESULT_CODE));

    private static final Set<PredicateType> validStringPredicates = new HashSet<>(asList(PredicateType.CONTAINS,
        PredicateType.DOES_NOT_CONTAIN, PredicateType.EQUAL, PredicateType.NOT_EQUAL));

    public static void validateTelemetryType(TelemetryType telemetryType) throws TelemetryTypeException {
        if (telemetryType.equals(TelemetryType.PERFORMANCE_COUNTER)) {
            throw new TelemetryTypeException(
                "The telemetry type PerformanceCounter was specified, but the distro does not send performance counters other than CPU/Mem to quickpulse");
        } else if (telemetryType.equals(TelemetryType.EVENT)) {
            throw new TelemetryTypeException(
                "The telemetry type Event was specified, but the distro does not send events to quickpulse");
        } else if (telemetryType.equals(TelemetryType.METRIC)) {
            throw new TelemetryTypeException(
                "The telemetry type Metric was specified, but the distro does not support sending open telemetry metrics to quickpulse");
        }
    }

    public static void checkCustomMetricProjection(DerivedMetricInfo derivedMetricInfo)
        throws UnexpectedFilterCreateException {
        if (derivedMetricInfo.getProjection().startsWith("CustomMetrics.")) {
            throw new UnexpectedFilterCreateException(
                "The projection of a customMetric property is not supported in this distro.");
        }
    }

    public static void validateMetricFilters(DerivedMetricInfo derivedMetricInfo)
        throws UnexpectedFilterCreateException {
        for (FilterConjunctionGroupInfo conjunctionGroupInfo : derivedMetricInfo.getFilterGroups()) {
            for (FilterInfo filter : conjunctionGroupInfo.getFilters()) {
                TelemetryType telemetryType = TelemetryType.fromString(derivedMetricInfo.getTelemetryType());
                validateFieldNames(filter.getFieldName(), telemetryType);
                validatePredicateAndComparand(filter);
            }
        }
    }

    public static void
    validateDocumentFilters(DocumentFilterConjunctionGroupInfo documentFilterConjunctionGroupInfo)
        throws UnexpectedFilterCreateException {
        FilterConjunctionGroupInfo conjunctionGroupInfo = documentFilterConjunctionGroupInfo.getFilters();
        for (FilterInfo filter : conjunctionGroupInfo.getFilters()) {
            validateFieldNames(filter.getFieldName(), documentFilterConjunctionGroupInfo.getTelemetryType());
            validatePredicateAndComparand(filter);
        }
    }

    private static boolean isCustomDimOrAnyField(String fieldName) {
        return fieldName.startsWith(Filter.CUSTOM_DIM_FIELDNAME_PREFIX) || fieldName.equals(Filter.ANY_FIELD);
    }

    private static void validateFieldNames(String fieldName, TelemetryType telemetryType)
        throws UnexpectedFilterCreateException {
        if (fieldName.isEmpty()) {
            throw new UnexpectedFilterCreateException("A filter must have a non-empty field name.");
        }
        if (fieldName.startsWith("CustomMetrics.")) {
            throw new UnexpectedFilterCreateException(
                "Filtering of a customMetric property is not supported in this distro.");
        }
    }

    private static void validatePredicateAndComparand(FilterInfo filter) throws UnexpectedFilterCreateException {
        if (filter.getComparand().isEmpty()) {
            // It is possible to not type in a comparand and the service side to send us empty string.
            throw new UnexpectedFilterCreateException("A filter must have a non-empty comparand. FilterName: "
                + filter.getFieldName() + " Predicate: " + filter.getPredicate().getValue());
        } else if (Filter.ANY_FIELD.equals(filter.getFieldName())
            && !(filter.getPredicate().equals(PredicateType.CONTAINS)
            || filter.getPredicate().equals(PredicateType.DOES_NOT_CONTAIN))) {
            // While the UI allows != and == for the ANY_FIELD fieldName, .net classic code only allows contains/not contains & the spec follows
            // .net classic behavior for this particular condition.
            throw new UnexpectedFilterCreateException(
                "The predicate " + filter.getPredicate().getValue() + " is not supported for the field name *");
        } else if (knownNumericColumns.contains(filter.getFieldName())) {

            // Just in case a strange timestamp value is passed from the service side. The service side should send a duration with a specific
            // format ([days].[hours]:[minutes]:[seconds] - the seconds may be a whole number or something like 7.89).
            if (KnownDependencyColumns.DURATION.equals(filter.getFieldName())) {
                if (Filter.getMicroSecondsFromFilterTimestampString(filter.getComparand()) == Long.MIN_VALUE) {
                    throw new UnexpectedFilterCreateException(
                        "The provided duration timestamp can't be converted to microseconds: " + filter.getComparand());
                }
            } else { // The service side not does not validate if resultcode or responsecode is a numeric value
                try {
                    Long.parseLong(filter.getComparand());
                } catch (NumberFormatException e) {
                    throw new UnexpectedFilterCreateException(
                        "Could not convert the provided result/response code to a numeric value: "
                            + filter.getComparand());
                }
            }
        } else if (knownStringColumns.contains(filter.getFieldName())
            || filter.getFieldName().startsWith(Filter.CUSTOM_DIM_FIELDNAME_PREFIX)) {
            // While the UI allows a user to select any predicate for a custom dimension filter, .net classic treats all custom dimensions like
            // String values. therefore we validate for predicates applicable to String. This is called out in the spec as well.
            if (!validStringPredicates.contains(filter.getPredicate())) {
                throw new UnexpectedFilterCreateException("The predicate " + filter.getPredicate().getValue()
                    + " is not supported for the field " + filter.getFieldName());
            }
        }
    }
}
