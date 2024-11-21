package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.DerivedMetricInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.FilterConjunctionGroupInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.FilterInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.PredicateType;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class Filter {
    public static final String EXCEPTION_FIELDNAME_PREFIX = "Exception.";
    public static final String CUSTOM_DIM_FIELDNAME_PREFIX = "CustomDimensions.";
    public static final String ANY_FIELD = "*";
    private static final String TELEMETRY_COLUMNS_CUSTOM_DIM_FIELD = "CustomDimensions";

    public static void renameExceptionFieldNamesForFiltering(FilterConjunctionGroupInfo filterConjunctionGroupInfo) {
        filterConjunctionGroupInfo.getFilters().forEach(filter -> {
            String fieldName = filter.getFieldName();
            if (fieldName.startsWith(EXCEPTION_FIELDNAME_PREFIX)) {
                filter.setFieldName(fieldName.substring(EXCEPTION_FIELDNAME_PREFIX.length()));
            }
        });
    }

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
    public static boolean checkFilterConjunctionGroup(FilterConjunctionGroupInfo filterConjunctionGroupInfo, TelemetryColumns data) {
        // All of the filters need to match for this to return true (and operation).
        for (FilterInfo filter : filterConjunctionGroupInfo.getFilters()) {
            if (!checkFilter(filter, data)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkFilter(FilterInfo filter, TelemetryColumns data) {
        try {
            if (ANY_FIELD.equals(filter.getFieldName())) {
                return checkAnyFieldFilter(filter, data);
            } else if (filter.getFieldName().startsWith(CUSTOM_DIM_FIELDNAME_PREFIX)) {
                return checkCustomDimFilter(filter, data);
            } else {
                Object fieldValue = getFieldValue(data, filter.getFieldName());

                if (filter.getFieldName().equals(KnownRequestColumns.success)) {
                    boolean fieldValueBoolean = (Boolean) fieldValue;
                    boolean comparand = Boolean.parseBoolean(filter.getComparand().toLowerCase());
                    if (filter.getPredicate().equals(PredicateType.EQUAL)) {
                        return fieldValueBoolean == comparand;
                    } else if (filter.getPredicate().equals(PredicateType.NOT_EQUAL)) {
                        return fieldValueBoolean != comparand;
                    }
                } else if (filter.getFieldName().equals(KnownDependencyColumns.resultCode) ||
                    filter.getFieldName().equals(KnownRequestColumns.responseCode) ||
                    filter.getFieldName().equals(KnownDependencyColumns.duration)) {
                    long comparand = filter.getFieldName().equals(KnownDependencyColumns.duration) ?
                        getMicroSecondsFromFilterTimestampString(filter.getComparand()): Long.parseLong(filter.getComparand());
                    long fieldValueLong = filter.getFieldName().equals(KnownDependencyColumns.duration) ?
                        (Long) fieldValue : ((Integer) fieldValue).longValue();
                    PredicateType predicate = filter.getPredicate();
                    if (predicate.equals(PredicateType.EQUAL)) {
                        return fieldValueLong == comparand;
                    } else if (predicate.equals(PredicateType.NOT_EQUAL)) {
                        return fieldValueLong != comparand;
                    } else if (predicate.equals(PredicateType.GREATER_THAN)) {
                        return fieldValueLong > comparand;
                    } else if (predicate.equals(PredicateType.GREATER_THAN_OR_EQUAL)) {
                        return fieldValueLong >= comparand;
                    } else if (predicate.equals(PredicateType.LESS_THAN)) {
                        return fieldValueLong < comparand;
                    } else if (predicate.equals(PredicateType.LESS_THAN_OR_EQUAL)) {
                        return fieldValueLong <= comparand;
                    }
                    return false;
                } else {
                    // string fields
                    return stringCompare((String) fieldValue, filter.getComparand(), filter.getPredicate());
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // because we will be doing validation of filtering configuration when the config changes,
            // there should be no case where we are accessing fields that don't exist or are invalid.
            return false;
        }
        return false;
    }

    private static Object getFieldValue(Object data, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = data.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(data);
    }

    private static boolean checkAnyFieldFilter(FilterInfo filter, TelemetryColumns data) {
        try {
            Field[] fields = data.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                String value = String.valueOf(field.get(data));
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
        } catch (IllegalAccessException e) {
            // we should never get here as we are looping through fields that we know exist.
            return false;
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
        } else if (predicate.equals(PredicateType.DOES_NOT_CONTAIN)){
            return fieldValue != null && !fieldValue.toLowerCase().contains(comparand.toLowerCase());
        }
        return false;
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
        return microseconds
            + (minutes * 60L * 1000000L)
            + (hours * 60L * 60L * 1000000L)
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
