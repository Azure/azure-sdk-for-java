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
    private static final String EXCEPTION_FIELDNAME_PREFIX = "Exception.";
    private static final String CUSTOM_DIM_FIELDNAME_PREFIX = "CustomDimensions.";
    private static final String ANY_FIELD = "*";
    private static final String TELEMETRY_COLUMNS_CUSTOM_DIM_FIELD = "CustomDimensions";

    public static void renameExceptionFieldNamesForFiltering(FilterConjunctionGroupInfo filterConjunctionGroupInfo) {
        filterConjunctionGroupInfo.getFilters().forEach(filter -> {
            String fieldName = filter.getFieldName();
            if (fieldName.startsWith(EXCEPTION_FIELDNAME_PREFIX)) {
                filter.setFieldName(fieldName.substring(EXCEPTION_FIELDNAME_PREFIX.length()));
            }
        });
    }

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
                    if (filter.getPredicate().equals(PredicateType.EQUAL)) {
                        return fieldValue.equals(Boolean.parseBoolean(filter.getComparand().toLowerCase()));
                    } else if (filter.getPredicate().equals(PredicateType.NOT_EQUAL)) {
                        return !fieldValue.equals(Boolean.parseBoolean(filter.getComparand().toLowerCase()));
                    }
                } else if (filter.getFieldName().equals(KnownDependencyColumns.resultCode) ||
                    filter.getFieldName().equals(KnownRequestColumns.responseCode) ||
                    filter.getFieldName().equals(KnownDependencyColumns.duration)) {
                    double comparand = filter.getFieldName().equals(KnownDependencyColumns.duration) ?
                        getMsFromFilterTimestampString(filter.getComparand()) : Double.parseDouble(filter.getComparand());
                    PredicateType predicate = filter.getPredicate();
                    if (predicate.equals(PredicateType.EQUAL)) {
                        return fieldValue.equals(comparand);
                    } else if (predicate.equals(PredicateType.NOT_EQUAL)) {
                        return !fieldValue.equals(comparand);
                    } else if (predicate.equals(PredicateType.GREATER_THAN)) {
                        return fieldValue.equals(comparand);
                    } else if (predicate.equals(PredicateType.GREATER_THAN_OR_EQUAL)) {
                        return !fieldValue.equals(comparand);
                    } else if (predicate.equals(PredicateType.LESS_THAN)) {
                        return fieldValue.equals(comparand);
                    } else if (predicate.equals(PredicateType.LESS_THAN_OR_EQUAL)) {
                        return !fieldValue.equals(comparand);
                    }
                    return false;
                } else {
                    // string fields
                    return stringCompare((String) fieldValue, filter.getComparand(), filter.getPredicate());
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
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
                if (TELEMETRY_COLUMNS_CUSTOM_DIM_FIELD.equals(field.getName())) {
                    Map<String, String> customDimensions = (Map<String, String>) field.get(data);
                    for (String value : customDimensions.values()) {
                        if (stringCompare(value, filter.getComparand(), filter.getPredicate())) {
                            return true;
                        }
                    }
                } else {
                    String value = String.valueOf(field.get(data));
                    if (stringCompare(value, filter.getComparand(), filter.getPredicate())) {
                        return true;
                    }
                }
            }
        } catch (IllegalAccessException e) {
            // we should never get here as we are looping through fields that we know exist.
            return false;
        }
        return false;
    }

    private static boolean checkCustomDimFilter(FilterInfo filter, TelemetryColumns data) {
        try {
            Field customDimensionsField = data.getClass().getDeclaredField(TELEMETRY_COLUMNS_CUSTOM_DIM_FIELD);
            customDimensionsField.setAccessible(true);
            Map<String, String> customDimensions = (Map<String, String>) customDimensionsField.get(data);
            String fieldName = filter.getFieldName().replace(CUSTOM_DIM_FIELDNAME_PREFIX, "");
            if (customDimensions.containsKey(fieldName)) {
                String value = customDimensions.get(fieldName);
                return stringCompare(value, filter.getComparand(), filter.getPredicate());
            } else {
                return false; // the asked for field is not present in the custom dimensions
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return false;
        }
    }

    private static boolean stringCompare(String fieldValue, String comparand, PredicateType predicate) {
        if (predicate.equals(PredicateType.EQUAL)) {
            return fieldValue.equals(comparand);
        } else if (predicate.equals(PredicateType.NOT_EQUAL)) {
            return !fieldValue.equals(comparand);
        } else if (predicate.equals(PredicateType.CONTAINS)) {
            return fieldValue.toLowerCase().contains(comparand.toLowerCase());
        } else if (predicate.equals(PredicateType.DOES_NOT_CONTAIN)){
            return !fieldValue.toLowerCase().contains(comparand.toLowerCase());
        }
        return false;
    }





}
