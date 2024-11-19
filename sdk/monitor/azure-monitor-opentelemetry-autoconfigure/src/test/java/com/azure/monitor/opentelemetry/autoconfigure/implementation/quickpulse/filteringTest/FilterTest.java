package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filteringTest;


import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering.Filter;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering.RequestDataColumns;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilterTest {

    private FilterInfo createFilterInfoWithParams(String fieldName, PredicateType predicate, String comparand) {
        FilterInfo result = new FilterInfo();
        result.setFieldName(fieldName);
        result.setPredicate(predicate);
        result.setComparand(comparand);
        return result;
    }

    private DerivedMetricInfo createDerivedMetricInfo(String id, String telemetryType, AggregationType agg,AggregationType backendAgg, String projection, List<FilterConjunctionGroupInfo> filterGroups) {
        DerivedMetricInfo result = new DerivedMetricInfo();
        result.setId(id);
        result.setTelemetryType(telemetryType);
        result.setAggregation(agg);
        result.setBackEndAggregation(backendAgg);
        result.setProjection(projection);
        result.setFilterGroups(filterGroups);
        return result;
    }

    private List<FilterConjunctionGroupInfo> createListWithOneFilterConjunctionGroupAndOneFilter(FilterInfo filter) {
        List<FilterConjunctionGroupInfo> result = new ArrayList<>();
        FilterConjunctionGroupInfo group = new FilterConjunctionGroupInfo();
        List<FilterInfo> filters = new ArrayList<>();
        filters.add(filter);
        group.setFilters(filters);
        result.add(group);
        return result;
    }


    @Test
    void testAnyFieldFilter() {
        FilterInfo anyFieldContainsHi = createFilterInfoWithParams(Filter.ANY_FIELD, PredicateType.CONTAINS, "hi");
        FilterInfo anyFieldNotContains = createFilterInfoWithParams(Filter.ANY_FIELD, PredicateType.DOES_NOT_CONTAIN, "hi");
        FilterInfo anyFieldContainsCool = createFilterInfoWithParams(Filter.ANY_FIELD, PredicateType.CONTAINS, "cool");
        FilterInfo anyFieldForNumeric = createFilterInfoWithParams(Filter.ANY_FIELD, PredicateType.CONTAINS, "200");
        FilterInfo anyFieldForBoolean = createFilterInfoWithParams(Filter.ANY_FIELD, PredicateType.CONTAINS, "true");


        RequestDataColumns request1 = new RequestDataColumns("https://test.com/hiThere", 200, 200, true, "GET /hiThere");
        RequestDataColumns request2 = new RequestDataColumns("https://test.com/bye", 200, 200, true, "GET /bye");
        request2.setCustomDimensions(new HashMap<>(Map.of("property", "cool")), null);

        List<FilterConjunctionGroupInfo> filterGroups = createListWithOneFilterConjunctionGroupAndOneFilter(anyFieldContainsHi);
        DerivedMetricInfo derivedMetricInfo = createDerivedMetricInfo("random-id", "Request", AggregationType.SUM, AggregationType.SUM, "Count()", filterGroups);

        FilterConjunctionGroupInfo filterGroup = filterGroups.get(0);

        // request contains "hi" in multiple fields & filter is contains hi
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, request1));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, request1));

        // request does not contain "hi" in any field & filter is contains hi
        assertFalse(Filter.checkFilterConjunctionGroup(filterGroup, request2));
        assertFalse(Filter.checkMetricFilters(derivedMetricInfo, request2));

        // request does not contain "hi" in any field & filter is does not contain hi
        filterGroup.setFilters(new ArrayList<FilterInfo>(List.of(anyFieldNotContains)));
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, request2));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, request2));

        // request contains "cool" in custom dimensions & filter is contains cool
        filterGroup.setFilters(new ArrayList<FilterInfo>(List.of(anyFieldContainsCool)));
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, request2));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, request2));

        // request contains 200 in duration & filter is contains "200".
        // fields are expected to be treated as string
        filterGroup.setFilters(new ArrayList<FilterInfo>(List.of(anyFieldForNumeric)));
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, request1));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, request1));

        // request contains true in Success & filter is contains "true".
        // fields are expected to be treated as string
        filterGroup.setFilters(new ArrayList<FilterInfo>(List.of(anyFieldForBoolean)));
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, request1));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, request1));

    }

    @Test
    void testCustomDimensionFilter() {
        FilterInfo customDimFilter = createFilterInfoWithParams(Filter.CUSTOM_DIM_FIELDNAME_PREFIX + "hi", PredicateType.EQUAL, "hi");
        List<FilterConjunctionGroupInfo> filterGroups = createListWithOneFilterConjunctionGroupAndOneFilter(customDimFilter);
        DerivedMetricInfo derivedMetricInfo = createDerivedMetricInfo("random-id", "Request", AggregationType.SUM, AggregationType.SUM, "Count()", filterGroups);
        RequestDataColumns request = new RequestDataColumns("https://test.com/hiThere", 200, 200, true, "GET /hiThere");
        Map<String, String> customDims = new HashMap<>(Map.of("property", "hi"));
        request.setCustomDimensions(customDims, null);
        FilterConjunctionGroupInfo filterGroup = filterGroups.get(0);

        // the asked for field is not in the custom dimensions so return false
        assertFalse(Filter.checkFilterConjunctionGroup(filterGroup, request));
        assertFalse(Filter.checkMetricFilters(derivedMetricInfo, request));

        // the asked for field is in the custom dimensions but value does not match
        customDims.put("hi", "bye");
        assertFalse(Filter.checkFilterConjunctionGroup(filterGroup, request));
        assertFalse(Filter.checkMetricFilters(derivedMetricInfo, request));

        // the asked for field is in the custom dimensions and value matches
        customDims.put("hi","hi");
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, request));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, request));

        // testing not equal predicate. The CustomDimensions.hi value != hi so return true.
        customDimFilter.setPredicate(PredicateType.NOT_EQUAL);
        customDims.put("hi", "bye");
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, request));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, request));

        // testing does not contain predicate. The CustomDimensions.hi value does not contain hi so return true.
        customDimFilter.setPredicate(PredicateType.DOES_NOT_CONTAIN);
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, request));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, request));

        // testing contains predicate. The CustomDimensions.hi value contains hi so return true.
        customDimFilter.setPredicate(PredicateType.CONTAINS);
        customDims.put("hi", "hi there");
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, request));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, request));
    }
}
