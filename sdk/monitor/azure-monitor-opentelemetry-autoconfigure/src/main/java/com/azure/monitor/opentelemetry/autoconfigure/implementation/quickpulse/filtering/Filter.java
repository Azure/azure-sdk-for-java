package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.DerivedMetricInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.FilterConjunctionGroupInfo;

public class Filter {
    private static final String EXCEPTION_FIELDNAME_PREFIX = "Exception.";
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
            matched = matched || checkFilterConjunctionGroup(filterGroup.getFilters(), data);
        }
        return matched;
    }




}
