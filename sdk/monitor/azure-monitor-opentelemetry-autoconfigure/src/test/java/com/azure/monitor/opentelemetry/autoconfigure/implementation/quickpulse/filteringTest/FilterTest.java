// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filteringTest;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.MessageData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.TelemetryExceptionData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.TelemetryExceptionDetails;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.TelemetryItem;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.MonitorDomain;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.RequestData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.RemoteDependencyData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.QuickPulseTestBase;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering.Filter;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering.KnownRequestColumns;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering.KnownDependencyColumns;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering.RequestDataColumns;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering.TraceDataColumns;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering.DependencyDataColumns;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering.ExceptionDataColumns;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.AggregationType;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.DerivedMetricInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.FilterInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.PredicateType;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.FilterConjunctionGroupInfo;

import io.vertx.core.cli.annotations.Description;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

class FilterTest {

    private FilterInfo createFilterInfoWithParams(String fieldName, PredicateType predicate, String comparand) {
        FilterInfo result = new FilterInfo();
        result.setFieldName(fieldName);
        result.setPredicate(predicate);
        result.setComparand(comparand);
        return result;
    }

    private DerivedMetricInfo createDerivedMetricInfo(String id, String telemetryType, AggregationType agg,
        AggregationType backendAgg, String projection, List<FilterConjunctionGroupInfo> filterGroups) {
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
    @Description("This tests if the any field (*) filter can filter telemetry correctly, with various combos of predicates & column types")
    void testAnyFieldFilter() {
        FilterInfo anyFieldContainsHi = createFilterInfoWithParams(Filter.ANY_FIELD, PredicateType.CONTAINS, "hi");
        FilterInfo anyFieldNotContains
            = createFilterInfoWithParams(Filter.ANY_FIELD, PredicateType.DOES_NOT_CONTAIN, "hi");
        FilterInfo anyFieldContainsCool = createFilterInfoWithParams(Filter.ANY_FIELD, PredicateType.CONTAINS, "cool");
        FilterInfo anyFieldForNumeric = createFilterInfoWithParams(Filter.ANY_FIELD, PredicateType.CONTAINS, "200");
        FilterInfo anyFieldForBoolean = createFilterInfoWithParams(Filter.ANY_FIELD, PredicateType.CONTAINS, "true");

        RequestDataColumns request1
            = new RequestDataColumns("https://test.com/hiThere", 200, 200, true, "GET /hiThere");
        RequestDataColumns request2 = new RequestDataColumns("https://test.com/bye", 200, 200, true, "GET /bye");
        Map<String, String> customDims = new HashMap<>();
        customDims.put("property", "cool");
        request2.setCustomDimensions(customDims, null);

        List<FilterConjunctionGroupInfo> filterGroups
            = createListWithOneFilterConjunctionGroupAndOneFilter(anyFieldContainsHi);
        DerivedMetricInfo derivedMetricInfo = createDerivedMetricInfo("random-id", "Request", AggregationType.SUM,
            AggregationType.SUM, "Count()", filterGroups);

        FilterConjunctionGroupInfo filterGroup = filterGroups.get(0);

        // request contains "hi" in multiple fields & filter is contains hi
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, request1));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, request1));

        // request does not contain "hi" in any field & filter is contains hi
        assertFalse(Filter.checkFilterConjunctionGroup(filterGroup, request2));
        assertFalse(Filter.checkMetricFilters(derivedMetricInfo, request2));

        // request does not contain "hi" in any field & filter is does not contain hi
        filterGroup.setFilters(asList(anyFieldNotContains));
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, request2));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, request2));

        // request contains "cool" in custom dimensions & filter is contains cool
        filterGroup.setFilters(asList(anyFieldContainsCool));
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, request2));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, request2));

        // request contains 200 in duration & filter is contains "200".
        // fields are expected to be treated as string
        filterGroup.setFilters(asList(anyFieldForNumeric));
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, request1));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, request1));

        // request contains true in Success & filter is contains "true".
        // fields are expected to be treated as string
        filterGroup.setFilters(asList(anyFieldForBoolean));
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, request1));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, request1));

    }

    @Test
    @Description("This tests if the custom dimension filter works correctly, with various predicates")
    void testCustomDimensionFilter() {
        FilterInfo customDimFilter
            = createFilterInfoWithParams(Filter.CUSTOM_DIM_FIELDNAME_PREFIX + "hi", PredicateType.EQUAL, "hi");
        List<FilterConjunctionGroupInfo> filterGroups
            = createListWithOneFilterConjunctionGroupAndOneFilter(customDimFilter);
        DerivedMetricInfo derivedMetricInfo = createDerivedMetricInfo("random-id", "Request", AggregationType.SUM,
            AggregationType.SUM, "Count()", filterGroups);
        RequestDataColumns request = new RequestDataColumns("https://test.com/hiThere", 200, 200, true, "GET /hiThere");

        Map<String, String> customDims1 = new HashMap<>();
        customDims1.put("property", "hi");

        Map<String, String> customDimsHiBye = new HashMap<>();
        customDimsHiBye.put("hi", "bye");

        Map<String, String> customDimsHi = new HashMap<>();
        customDimsHi.put("hi", "hi");

        Map<String, String> customDimsHiThere = new HashMap<>();
        customDimsHiThere.put("hi", "hi there");

        request.setCustomDimensions(customDims1, null);
        FilterConjunctionGroupInfo filterGroup = filterGroups.get(0);

        // the asked for field is not in the custom dimensions so return false
        assertFalse(Filter.checkFilterConjunctionGroup(filterGroup, request));
        assertFalse(Filter.checkMetricFilters(derivedMetricInfo, request));

        // the asked for field is in the custom dimensions but value does not match
        request.setCustomDimensions(customDimsHiBye, null);
        assertFalse(Filter.checkFilterConjunctionGroup(filterGroup, request));
        assertFalse(Filter.checkMetricFilters(derivedMetricInfo, request));

        // the asked for field is in the custom dimensions and value matches
        request.setCustomDimensions(customDimsHi, null);
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, request));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, request));

        // testing not equal predicate. The CustomDimensions.hi value != hi so return true.
        customDimFilter.setPredicate(PredicateType.NOT_EQUAL);
        request.setCustomDimensions(customDimsHiBye, null);
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, request));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, request));

        // testing does not contain predicate. The CustomDimensions.hi value does not contain hi so return true.
        customDimFilter.setPredicate(PredicateType.DOES_NOT_CONTAIN);
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, request));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, request));

        // testing contains predicate. The CustomDimensions.hi value contains hi so return true.
        customDimFilter.setPredicate(PredicateType.CONTAINS);
        request.setCustomDimensions(customDimsHiThere, null);
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, request));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, request));
    }

    @Test
    @Description("This tests if filters on known boolean columns (Success) work correctly, with various predicates and telemetry types")
    void testBooleanFilter() {
        FilterInfo booleanFilter = createFilterInfoWithParams(KnownRequestColumns.SUCCESS, PredicateType.EQUAL, "true");
        List<FilterConjunctionGroupInfo> filterGroups
            = createListWithOneFilterConjunctionGroupAndOneFilter(booleanFilter);
        DerivedMetricInfo derivedMetricInfo = createDerivedMetricInfo("random-id", "Request", AggregationType.SUM,
            AggregationType.SUM, "Count()", filterGroups);
        RequestDataColumns requestTrue
            = new RequestDataColumns("https://test.com/hiThere", 200, 200, true, "GET /hiThere");
        RequestDataColumns requestFalse
            = new RequestDataColumns("https://test.com/hiThere", 200, 0, false, "GET /hiThere");
        DependencyDataColumns dependencyTrue = new DependencyDataColumns("test.com", 200, true, "GET /hiThere", 200,
            "HTTP", "https://test.com/hiThere?x=y");
        DependencyDataColumns dependencyFalse = new DependencyDataColumns("test.com", 200, false, "GET /hiThere", 0,
            "HTTP", "https://test.com/hiThere?x=y");
        FilterConjunctionGroupInfo filterGroup = filterGroups.get(0);

        // Request Success filter matches
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, requestTrue));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, requestTrue));

        // Request Success filter does not match
        assertFalse(Filter.checkFilterConjunctionGroup(filterGroup, requestFalse));
        assertFalse(Filter.checkMetricFilters(derivedMetricInfo, requestFalse));

        // Request Success filter matches for != predicate
        booleanFilter.setPredicate(PredicateType.NOT_EQUAL);
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, requestFalse));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, requestFalse));

        // Dependency Success filter matches
        derivedMetricInfo.setTelemetryType("Dependency");
        booleanFilter.setPredicate(PredicateType.EQUAL);
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, dependencyTrue));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, dependencyTrue));

        // Dependency Success filter does not match
        assertFalse(Filter.checkFilterConjunctionGroup(filterGroup, dependencyFalse));
        assertFalse(Filter.checkMetricFilters(derivedMetricInfo, dependencyFalse));

        // Dependency Success filter matches for != predicate
        booleanFilter.setPredicate(PredicateType.NOT_EQUAL);
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, dependencyFalse));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, dependencyFalse));
    }

    @Test
    @Description("This tests if filtering works on known numeric columns, with various predicates and telemetry types")
    void testNumericFilters() {
        FilterInfo numericFilter
            = createFilterInfoWithParams(KnownRequestColumns.RESPONSE_CODE, PredicateType.EQUAL, "200");
        List<FilterConjunctionGroupInfo> filterGroups
            = createListWithOneFilterConjunctionGroupAndOneFilter(numericFilter);
        DerivedMetricInfo derivedMetricInfo = createDerivedMetricInfo("random-id", "Request", AggregationType.SUM,
            AggregationType.SUM, "Count()", filterGroups);
        RequestDataColumns requestSuccessful
            = new RequestDataColumns("https://test.com/hiThere", 1234567890000L, 200, true, "GET /hiThere");
        RequestDataColumns requestFail
            = new RequestDataColumns("https://test.com/hiThere", 1234567890000L, 404, false, "GET /hiThere");
        RequestDataColumns requestShortDuration
            = new RequestDataColumns("https://test.com/hiThere", 400, 200, true, "GET /hiThere");
        DependencyDataColumns dependencySuccessful = new DependencyDataColumns("test.com", 1234567890000L, true,
            "GET /hiThere", 200, "HTTP", "https://test.com/hiThere?x=y");
        DependencyDataColumns dependencyFail = new DependencyDataColumns("test.com", 1234567890000L, false,
            "GET /hiThere", 0, "HTTP", "https://test.com/hiThere?x=y");
        DependencyDataColumns dependencyShortDuration = new DependencyDataColumns("test.com", 400, true, "GET /hiThere",
            200, "HTTP", "https://test.com/hiThere?x=y");
        FilterConjunctionGroupInfo filterGroup = filterGroups.get(0);

        // Request ResponseCode filter matches
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, requestSuccessful));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, requestSuccessful));

        // Request ResponseCode filter does not match
        assertFalse(Filter.checkFilterConjunctionGroup(filterGroup, requestFail));
        assertFalse(Filter.checkMetricFilters(derivedMetricInfo, requestFail));

        // Dependency ResultCode filter matches
        derivedMetricInfo.setTelemetryType("Dependency");
        numericFilter.setFieldName(KnownDependencyColumns.RESULT_CODE);
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, dependencySuccessful));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, dependencySuccessful));

        // Dependency ResultCode filter does not match
        assertFalse(Filter.checkFilterConjunctionGroup(filterGroup, dependencyFail));
        assertFalse(Filter.checkMetricFilters(derivedMetricInfo, dependencyFail));

        // Dependency Duration filter matches
        numericFilter.setFieldName(KnownDependencyColumns.DURATION);
        // 14 days, 6 hrs, 56 miutes, 7.89 seconds (1234567890 ms is the matching value for what the user would put in filtering UI)
        // the UI sends the following string down to SDK
        numericFilter.setComparand("14.6:56:7.89");
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, dependencyFail));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, dependencyFail));

        // Dependency duration filter does not match
        assertFalse(Filter.checkFilterConjunctionGroup(filterGroup, dependencyShortDuration));
        assertFalse(Filter.checkMetricFilters(derivedMetricInfo, dependencyShortDuration));

        // Request Duration filter matches
        numericFilter.setFieldName(KnownRequestColumns.DURATION);
        derivedMetricInfo.setTelemetryType("Request");
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, requestSuccessful));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, requestSuccessful));

        // Dependency duration filter does not match
        assertFalse(Filter.checkFilterConjunctionGroup(filterGroup, requestShortDuration));
        assertFalse(Filter.checkMetricFilters(derivedMetricInfo, requestShortDuration));

        // != predicate
        numericFilter.setPredicate(PredicateType.NOT_EQUAL);
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, requestShortDuration));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, requestShortDuration));

        // < predicate
        numericFilter.setPredicate(PredicateType.LESS_THAN);
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, requestShortDuration));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, requestShortDuration));

        // <= predicate
        numericFilter.setPredicate(PredicateType.LESS_THAN_OR_EQUAL);
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, requestShortDuration));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, requestShortDuration));

        // > predicate
        numericFilter.setPredicate(PredicateType.GREATER_THAN);
        assertFalse(Filter.checkFilterConjunctionGroup(filterGroup, requestShortDuration));
        assertFalse(Filter.checkMetricFilters(derivedMetricInfo, requestShortDuration));

        // >= predicate
        numericFilter.setPredicate(PredicateType.GREATER_THAN_OR_EQUAL);
        assertFalse(Filter.checkFilterConjunctionGroup(filterGroup, requestShortDuration));
        assertFalse(Filter.checkMetricFilters(derivedMetricInfo, requestShortDuration));
    }

    @Test
    @Description("This tests if filtering works on a known string column from each telemetry type, with various predicates")
    void testStringFilters() {
        FilterInfo stringFilter = createFilterInfoWithParams(KnownRequestColumns.URL, PredicateType.CONTAINS, "hi");
        List<FilterConjunctionGroupInfo> filterGroups
            = createListWithOneFilterConjunctionGroupAndOneFilter(stringFilter);
        DerivedMetricInfo derivedMetricInfo = createDerivedMetricInfo("random-id", "Request", AggregationType.SUM,
            AggregationType.SUM, "Count()", filterGroups);
        RequestDataColumns requestHi
            = new RequestDataColumns("https://test.com/hiThere", 200, 200, true, "GET /hiThere");
        RequestDataColumns requestBye = new RequestDataColumns("https://test.com/bye", 200, 200, true, "GET /bye");
        DependencyDataColumns dependencyHi = new DependencyDataColumns("test.com", 200, true, "GET /hiThere", 200,
            "HTTP", "https://test.com/hiThere?x=y");
        DependencyDataColumns dependencyBye
            = new DependencyDataColumns("test.com", 200, true, "GET /bye", 200, "HTTP", "https://test.com/bye");
        TraceDataColumns traceHi = new TraceDataColumns("hi there");
        TraceDataColumns traceBye = new TraceDataColumns("bye");
        ExceptionDataColumns exceptionHi = new ExceptionDataColumns("Exception Message hi", "Stack Trace");
        ExceptionDataColumns exception = new ExceptionDataColumns("Exception Message", "Stack Trace");
        FilterConjunctionGroupInfo filterGroup = filterGroups.get(0);

        // Request Url filter matches
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, requestHi));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, requestHi));

        // Request Url filter does not match
        assertFalse(Filter.checkFilterConjunctionGroup(filterGroup, requestBye));
        assertFalse(Filter.checkMetricFilters(derivedMetricInfo, requestBye));

        // Dependency Data filter matches
        derivedMetricInfo.setTelemetryType("Dependency");
        stringFilter.setFieldName(KnownDependencyColumns.DATA);
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, dependencyHi));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, dependencyHi));

        // Dependency Data filter does not match
        assertFalse(Filter.checkFilterConjunctionGroup(filterGroup, dependencyBye));
        assertFalse(Filter.checkMetricFilters(derivedMetricInfo, dependencyBye));

        // Trace Message filter matches
        derivedMetricInfo.setTelemetryType("Trace");
        stringFilter.setFieldName("Message");
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, traceHi));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, traceHi));

        // Trace Message filter does not match
        assertFalse(Filter.checkFilterConjunctionGroup(filterGroup, traceBye));
        assertFalse(Filter.checkMetricFilters(derivedMetricInfo, traceBye));

        // Exception.Message filter matches.
        derivedMetricInfo.setTelemetryType("Exception");
        stringFilter.setFieldName("Exception.Message");
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, exceptionHi));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, exceptionHi));

        // Exception Message filter does not match
        assertFalse(Filter.checkFilterConjunctionGroup(filterGroup, exception));
        assertFalse(Filter.checkMetricFilters(derivedMetricInfo, exception));

        // != predicate
        stringFilter.setPredicate(PredicateType.NOT_EQUAL);
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, exception));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, exception));

        // not contains
        stringFilter.setPredicate(PredicateType.DOES_NOT_CONTAIN);
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, exception));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, exception));

        // equal
        stringFilter.setPredicate(PredicateType.EQUAL);
        assertFalse(Filter.checkFilterConjunctionGroup(filterGroup, exception));
        assertFalse(Filter.checkMetricFilters(derivedMetricInfo, exception));
    }

    @Test
    @Description("If a FilterConjunctionGroupInfo has an empty list of filters, telemetry should match")
    void testEmptyFilterConjunctionGroupInfo() {
        // create empty filter list
        FilterConjunctionGroupInfo filterGroup = new FilterConjunctionGroupInfo();
        filterGroup.setFilters(new ArrayList<FilterInfo>());
        List<FilterConjunctionGroupInfo> filterGroups = asList(filterGroup);

        DerivedMetricInfo derivedMetricInfo = createDerivedMetricInfo("random-id", "Request", AggregationType.SUM,
            AggregationType.SUM, "Count()", filterGroups);
        RequestDataColumns request = new RequestDataColumns("https://test.com/hiThere", 200, 200, true, "GET /hiThere");

        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, request));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, request));
    }

    @Test
    @Description("If there are multiple filters in a FilterConjunctionGroupInfo, then telemetry should only match if all conditions satisfied")
    void testMultipleFiltersInGroup() {
        FilterInfo filter1 = createFilterInfoWithParams(KnownRequestColumns.URL, PredicateType.CONTAINS, "hi");
        FilterInfo filter2 = createFilterInfoWithParams(KnownRequestColumns.RESPONSE_CODE, PredicateType.EQUAL, "200");
        FilterConjunctionGroupInfo filterGroup = new FilterConjunctionGroupInfo();
        filterGroup.setFilters(asList(filter1, filter2));
        List<FilterConjunctionGroupInfo> filterGroups = asList(filterGroup);

        DerivedMetricInfo derivedMetricInfo = createDerivedMetricInfo("random-id", "Request", AggregationType.SUM,
            AggregationType.SUM, "Count()", filterGroups);
        RequestDataColumns requestHi
            = new RequestDataColumns("https://test.com/hiThere", 200, 200, true, "GET /hiThere");
        RequestDataColumns requestBye = new RequestDataColumns("https://test.com/bye", 200, 200, true, "GET /bye");

        // matches both filters
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, requestHi));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, requestHi));

        // only one filter matches, the entire conjunction group should return false
        assertFalse(Filter.checkFilterConjunctionGroup(filterGroup, requestBye));
        assertFalse(Filter.checkMetricFilters(derivedMetricInfo, requestBye));
    }

    @Test
    @Description("Test the constructors for child classes of TelemetryColumns")
    void testConstructors() {
        TelemetryItem requestItem
            = QuickPulseTestBase.createRequestTelemetry("GET /hiThere", new Date(), 1234567890L, "200", true);
        TelemetryItem dependencyItem = QuickPulseTestBase.createRemoteDependencyTelemetry("GET /hiThere",
            "https://test.com/hiThere?x=y", 400, true);

        TelemetryExceptionData exceptionItem = new TelemetryExceptionData();
        TelemetryExceptionDetails details = new TelemetryExceptionDetails();
        details.setMessage("A message");
        details.setStack("A stack trace");
        exceptionItem.setExceptions(asList(details));

        MessageData traceItem = new MessageData();
        traceItem.setMessage("A message");

        MonitorDomain requestData = requestItem.getData().getBaseData();
        RequestDataColumns requestDataColumns = new RequestDataColumns((RequestData) requestData);

        MonitorDomain dependencyData = dependencyItem.getData().getBaseData();
        ((RemoteDependencyData) dependencyData).setType("HTTP");
        ((RemoteDependencyData) dependencyData).setTarget("test.com");
        ((RemoteDependencyData) dependencyData).setResultCode("200");
        DependencyDataColumns dependencyDataColumns = new DependencyDataColumns((RemoteDependencyData) dependencyData);

        ExceptionDataColumns exceptionData = new ExceptionDataColumns(exceptionItem);
        TraceDataColumns traceDataColumns = new TraceDataColumns(traceItem);

        assertTrue(requestDataColumns.getSuccess());
        assertEquals(requestDataColumns.getDuration(), 1234567890000L);
        assertEquals(requestDataColumns.getResponseCode(), 200);
        assertEquals(requestDataColumns.getName(), "GET /hiThere");
        assertEquals(requestDataColumns.getUrl(), "foo");

        assertTrue(dependencyDataColumns.getSuccess());
        assertEquals(dependencyDataColumns.getData(), "https://test.com/hiThere?x=y");
        assertEquals(dependencyDataColumns.getName(), "GET /hiThere");
        assertEquals(dependencyDataColumns.getType(), "HTTP");
        assertEquals(dependencyDataColumns.getDuration(), 400000L);
        assertEquals(dependencyDataColumns.getResultCode(), 200);
        assertEquals(dependencyDataColumns.getTarget(), "test.com");

        assertEquals(exceptionData.getMessage(), "A message");
        assertEquals(exceptionData.getStackTrace(), "A stack trace");

        assertEquals(traceDataColumns.getMessage(), "A message");
    }

}
