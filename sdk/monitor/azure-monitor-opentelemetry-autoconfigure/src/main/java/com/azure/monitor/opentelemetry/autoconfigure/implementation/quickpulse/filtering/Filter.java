// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.DerivedMetricInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.FilterConjunctionGroupInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.FilterInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.PredicateType;

import java.util.List;
import java.util.Map;

public class Filter {
    public static final String CUSTOM_DIM_FIELDNAME_PREFIX = "CustomDimensions.";
    public static final String ANY_FIELD = "*";

    // To be used when checking telemetry against metric charts filters
    public static boolean checkMetricFilters(DerivedMetricInfo derivedMetricInfo, TelemetryColumns data) {
        if (derivedMetricInfo.getFilterGroups().isEmpty()) {
            // This should never happen - even when a user does not add filter pills to the derived metric,
            // the filterGroups array should have one filter group with an empty array of filters.
            return true;
        }

        // Haven't yet seen any case where there is more than one filter group in a derived metric info.
        // Just to be safe, handling the multiple filter conjunction group case as an or operation.
        boolean matched = false;
        for (FilterConjunctionGroupInfo filterGroup : derivedMetricInfo.getFilterGroups()) {
            matched = matched || checkFilterConjunctionGroup(filterGroup, data);
        }
        return matched;
    }

    // To be used when checking telemetry against document filters. This also gets reused in the logic for checking metrics
    // charts filters.
    public static boolean checkFilterConjunctionGroup(FilterConjunctionGroupInfo filterConjunctionGroupInfo,
        TelemetryColumns data) {
        // All of the filters need to match for this to return true (and operation).
        for (FilterInfo filter : filterConjunctionGroupInfo.getFilters()) {
            if (!checkFilter(filter, data)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkFilter(FilterInfo filter, TelemetryColumns data) {
        if (ANY_FIELD.equals(filter.getFieldName())) {
            return checkAnyFieldFilter(filter, data);
        } else if (filter.getFieldName().startsWith(CUSTOM_DIM_FIELDNAME_PREFIX)) {
            return checkCustomDimFilter(filter, data);
        } else {
            String fieldName = filter.getFieldName();

            if (filter.getFieldName().equals(KnownRequestColumns.SUCCESS)) {
                boolean fieldValueBoolean = data.getFieldValue(filter.getFieldName(), Boolean.class);
                boolean comparand = Boolean.parseBoolean(filter.getComparand().toLowerCase());
                if (filter.getPredicate().equals(PredicateType.EQUAL)) {
                    return fieldValueBoolean == comparand;
                } else if (filter.getPredicate().equals(PredicateType.NOT_EQUAL)) {
                    return fieldValueBoolean != comparand;
                }
            } else if (filter.getFieldName().equals(KnownDependencyColumns.DURATION)) {
                long comparand = getMicroSecondsFromFilterTimestampString(filter.getComparand());
                long fieldValueLong = data.getFieldValue(KnownRequestColumns.DURATION, Long.class);
                return numericCompare(fieldValueLong, comparand, filter.getPredicate());
            } else if (filter.getFieldName().equals(KnownDependencyColumns.RESULT_CODE)
                || filter.getFieldName().equals(KnownRequestColumns.RESPONSE_CODE)) {
                int comparand = Integer.parseInt(filter.getComparand());
                PredicateType predicate = filter.getPredicate();
                int fieldValueInt = data.getFieldValue(fieldName, Integer.class);
                return numericCompare(fieldValueInt, comparand, predicate);
            } else {
                // string fields
                String fieldValueString = data.getFieldValue(fieldName, String.class);
                return stringCompare(fieldValueString, filter.getComparand(), filter.getPredicate());
            }
        }
        return false;
    }

    private static boolean checkAnyFieldFilter(FilterInfo filter, TelemetryColumns data) {

        List<String> values = data.getAllFieldValuesAsString();
        for (String value : values) {
            if (stringCompare(value, filter.getComparand(), filter.getPredicate())) {
                return true;
            }
        }
        Map<String, String> customDimensions = data.getCustomDimensions();
        for (String value : customDimensions.values()) {
            if (stringCompare(value, filter.getComparand(), filter.getPredicate())) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkCustomDimFilter(FilterInfo filter, TelemetryColumns data) {
        Map<String, String> customDimensions = data.getCustomDimensions();
        String fieldName = filter.getFieldName().replace(CUSTOM_DIM_FIELDNAME_PREFIX, "");
        if (customDimensions.containsKey(fieldName)) {
            String value = customDimensions.get(fieldName);
            return stringCompare(value, filter.getComparand(), filter.getPredicate());
        } else {
            return false; // the asked for field is not present in the custom dimensions
        }
    }

    private static boolean stringCompare(String fieldValue, String comparand, PredicateType predicate) {
        if (predicate.equals(PredicateType.EQUAL)) {
            return fieldValue != null && fieldValue.equals(comparand);
        } else if (predicate.equals(PredicateType.NOT_EQUAL)) {
            return fieldValue != null && !fieldValue.equals(comparand);
        } else if (predicate.equals(PredicateType.CONTAINS)) {
            return fieldValue != null && fieldValue.toLowerCase().contains(comparand.toLowerCase());
        } else if (predicate.equals(PredicateType.DOES_NOT_CONTAIN)) {
            return fieldValue != null && !fieldValue.toLowerCase().contains(comparand.toLowerCase());
        }
        return false;
    }

    private static boolean numericCompare(long fieldValue, long comparand, PredicateType predicate) {
        if (predicate.equals(PredicateType.EQUAL)) {
            return fieldValue == comparand;
        } else if (predicate.equals(PredicateType.NOT_EQUAL)) {
            return fieldValue != comparand;
        } else if (predicate.equals(PredicateType.GREATER_THAN)) {
            return fieldValue > comparand;
        } else if (predicate.equals(PredicateType.GREATER_THAN_OR_EQUAL)) {
            return fieldValue >= comparand;
        } else if (predicate.equals(PredicateType.LESS_THAN)) {
            return fieldValue < comparand;
        } else if (predicate.equals(PredicateType.LESS_THAN_OR_EQUAL)) {
            return fieldValue <= comparand;
        } else {
            return false;
        }
    }

    public static long getMicroSecondsFromFilterTimestampString(String timestamp) {
        // The service side will return a timestamp in the following format:
        // [days].[hours]:[minutes]:[seconds]
        // the seconds may be a whole number or something like 7.89. 7.89 seconds translates to 7890000 microseconds.
        // examples: "14.6:56:7.89" = 1234567890000 microseconds, "0.0:0:0.2" = 200000 microseconds

        // Split the timestamp by ":"
        String[] parts = timestamp.split(":");
        if (parts.length != 3) {
            return Long.MIN_VALUE; // Return a special value to indicate an error
        }

        // Parse seconds and minutes
        long microseconds = (long) (Double.parseDouble(parts[2]) * 1000000L);
        int minutes = parseInt(parts[1]);

        // Split the first part by "." to get days and hours
        String[] firstPart = parts[0].split("\\.");
        if (firstPart.length != 2) {
            return Long.MIN_VALUE; // Return a special value to indicate an error
        }

        int hours = parseInt(firstPart[1]);
        int days = parseInt(firstPart[0]);

        if (minutes == Integer.MIN_VALUE || hours == Integer.MIN_VALUE || days == Integer.MIN_VALUE) {
            return Long.MIN_VALUE; // Return a special value to indicate an error
        }

        // Calculate the total microseconds
        return microseconds + (minutes * 60L * 1000000L) + (hours * 60L * 60L * 1000000L)
            + (days * 24L * 60L * 60L * 1000000L);

    }

    private static int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return Integer.MIN_VALUE;
        }
    }

}
