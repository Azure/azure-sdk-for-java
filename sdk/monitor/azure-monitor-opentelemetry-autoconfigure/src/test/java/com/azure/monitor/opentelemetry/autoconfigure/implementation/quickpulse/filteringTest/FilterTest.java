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


        RequestDataColumns request1 = new RequestDataColumns("https://test.com/hiThere", 200L, 200, true, "GET /hiThere");
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
        //derivedMetricInfo.setFilterGroups(new FilterConjunctionGroupInfo[]{conjunctionGroup});
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, request2));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, request2));

        // request contains "cool" in custom dimensions & filter is contains cool
        filterGroup.setFilters(new ArrayList<FilterInfo>(List.of(anyFieldContainsCool)));
        //derivedMetricInfo.setFilterGroups(new FilterConjunctionGroupInfo[]{conjunctionGroup});
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, request2));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, request2));

        // request contains 200 in duration & filter is contains "200".
        // fields are expected to be treated as string
        filterGroup.setFilters(new ArrayList<FilterInfo>(List.of(anyFieldForNumeric)));
        //derivedMetricInfo.setFilterGroups(new FilterConjunctionGroupInfo[]{conjunctionGroup});
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, request1));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, request1));

        // request contains true in Success & filter is contains "true".
        // fields are expected to be treated as string
        filterGroup.setFilters(new ArrayList<FilterInfo>(List.of(anyFieldForBoolean)));
        //derivedMetricInfo.setFilterGroups(new FilterConjunctionGroupInfo[]{conjunctionGroup});
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, request1));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, request1));

    }
}
