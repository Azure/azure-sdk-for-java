package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filteringTest;


import com.azure.monitor.opentelemetry.autoconfigure.implementation.builders.ExceptionTelemetryBuilder;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.builders.MessageTelemetryBuilder;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.builders.RequestTelemetryBuilder;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.*;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.QuickPulseTestBase;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering.*;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.*;
import io.vertx.core.cli.annotations.Description;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

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
        List<FilterInfo> filters = new ArrayList<>(List.of(filter));
        group.setFilters(filters);
        result.add(group);
        return result;
    }


    @Test
    @Description("This tests if the any field (*) filter can filter telemetry correctly, with various combos of predicates & column types")
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
    @Description("This tests if the custom dimension filter works correctly, with various predicates")
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
        request.setCustomDimensions(customDims, null);
        assertFalse(Filter.checkFilterConjunctionGroup(filterGroup, request));
        assertFalse(Filter.checkMetricFilters(derivedMetricInfo, request));

        // the asked for field is in the custom dimensions and value matches
        customDims.put("hi","hi");
        request.setCustomDimensions(customDims, null);
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, request));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, request));

        // testing not equal predicate. The CustomDimensions.hi value != hi so return true.
        customDimFilter.setPredicate(PredicateType.NOT_EQUAL);
        customDims.put("hi", "bye");
        request.setCustomDimensions(customDims, null);
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, request));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, request));

        // testing does not contain predicate. The CustomDimensions.hi value does not contain hi so return true.
        customDimFilter.setPredicate(PredicateType.DOES_NOT_CONTAIN);
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, request));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, request));

        // testing contains predicate. The CustomDimensions.hi value contains hi so return true.
        customDimFilter.setPredicate(PredicateType.CONTAINS);
        customDims.put("hi", "hi there");
        request.setCustomDimensions(customDims, null);
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, request));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, request));
    }

    @Test
    @Description("This tests if filters on known boolean columns (Success) work correctly, with various predicates and telemetry types")
    void testBooleanFilter() {
        FilterInfo booleanFilter = createFilterInfoWithParams(KnownRequestColumns.success, PredicateType.EQUAL, "true");
        List<FilterConjunctionGroupInfo> filterGroups = createListWithOneFilterConjunctionGroupAndOneFilter(booleanFilter);
        DerivedMetricInfo derivedMetricInfo = createDerivedMetricInfo("random-id", "Request", AggregationType.SUM, AggregationType.SUM, "Count()", filterGroups);
        RequestDataColumns request = new RequestDataColumns("https://test.com/hiThere", 200, 200, true, "GET /hiThere");
        DependencyDataColumns dependency = new DependencyDataColumns("test.com", 200, true, "GET /hiThere", 200, "HTTP", "https://test.com/hiThere?x=y");
        FilterConjunctionGroupInfo filterGroup = filterGroups.get(0);

        // Request Success filter matches
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, request));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, request));

        // Request Success filter does not match
        request.setSuccess(false);
        assertFalse(Filter.checkFilterConjunctionGroup(filterGroup, request));
        assertFalse(Filter.checkMetricFilters(derivedMetricInfo, request));

        // Request Success filter matches for != predicate
        booleanFilter.setPredicate(PredicateType.NOT_EQUAL);
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, request));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, request));

        // Dependency Success filter matches
        derivedMetricInfo.setTelemetryType("Dependency");
        booleanFilter.setPredicate(PredicateType.EQUAL);
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, dependency));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, dependency));

        // Dependency Success filter does not match
        dependency.setSuccess(false);
        assertFalse(Filter.checkFilterConjunctionGroup(filterGroup, dependency));
        assertFalse(Filter.checkMetricFilters(derivedMetricInfo, dependency));

        // Dependency Success filter matches for != predicate
        booleanFilter.setPredicate(PredicateType.NOT_EQUAL);
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, dependency));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, dependency));
    }

    @Test
    @Description("This tests if filtering works on known numeric columns, with various predicates and telemetry types")
    void testNumericFilters() {
        FilterInfo numericFilter = createFilterInfoWithParams(KnownRequestColumns.responseCode, PredicateType.EQUAL, "200");
        List<FilterConjunctionGroupInfo> filterGroups = createListWithOneFilterConjunctionGroupAndOneFilter(numericFilter);
        DerivedMetricInfo derivedMetricInfo = createDerivedMetricInfo("random-id", "Request", AggregationType.SUM, AggregationType.SUM, "Count()", filterGroups);
        RequestDataColumns request = new RequestDataColumns("https://test.com/hiThere", 1234567890000L, 200, true, "GET /hiThere");
        DependencyDataColumns dependency = new DependencyDataColumns("test.com", 1234567890000L, true, "GET /hiThere", 200, "HTTP", "https://test.com/hiThere?x=y");
        FilterConjunctionGroupInfo filterGroup = filterGroups.get(0);

        // Request ResponseCode filter matches
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, request));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, request));

        // Request ResponseCode filter does not match
        request.setResponseCode(404);
        request.setSuccess(false);
        assertFalse(Filter.checkFilterConjunctionGroup(filterGroup, request));
        assertFalse(Filter.checkMetricFilters(derivedMetricInfo, request));

        // Dependency ResultCode filter matches
        derivedMetricInfo.setTelemetryType("Dependency");
        numericFilter.setFieldName(KnownDependencyColumns.resultCode);
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, dependency));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, dependency));

        // Dependency ResultCode filter does not match
        dependency.setResultCode(404);
        dependency.setSuccess(false);
        assertFalse(Filter.checkFilterConjunctionGroup(filterGroup, dependency));
        assertFalse(Filter.checkMetricFilters(derivedMetricInfo, dependency));

        // Dependency Duration filter matches
        numericFilter.setFieldName(KnownDependencyColumns.duration);
        // 14 days, 6 hrs, 56 miutes, 7.89 seconds (1234567890 ms is the matching value for what the user would put in filtering UI)
        // the UI sends the following string down to SDK
        numericFilter.setComparand("14.6:56:7.89");
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, dependency));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, dependency));

        // Dependency duration filter does not match
        dependency.setDuration(400);
        assertFalse(Filter.checkFilterConjunctionGroup(filterGroup, dependency));
        assertFalse(Filter.checkMetricFilters(derivedMetricInfo, dependency));

        // Request Duration filter matches
        numericFilter.setFieldName(KnownRequestColumns.duration);
        derivedMetricInfo.setTelemetryType("Request");
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, request));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, request));

        // Dependency duration filter does not match
        request.setDuration(400);
        assertFalse(Filter.checkFilterConjunctionGroup(filterGroup, request));
        assertFalse(Filter.checkMetricFilters(derivedMetricInfo, request));

        // != predicate
        numericFilter.setPredicate(PredicateType.NOT_EQUAL);
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, request));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, request));

        // < predicate
        numericFilter.setPredicate(PredicateType.LESS_THAN);
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, request));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, request));

        // <= predicate
        numericFilter.setPredicate(PredicateType.LESS_THAN_OR_EQUAL);
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, request));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, request));

        // > predicate
        numericFilter.setPredicate(PredicateType.GREATER_THAN);
        assertFalse(Filter.checkFilterConjunctionGroup(filterGroup, request));
        assertFalse(Filter.checkMetricFilters(derivedMetricInfo, request));

        // >= predicate
        numericFilter.setPredicate(PredicateType.GREATER_THAN_OR_EQUAL);
        assertFalse(Filter.checkFilterConjunctionGroup(filterGroup, request));
        assertFalse(Filter.checkMetricFilters(derivedMetricInfo, request));
    }

    @Test
    @Description("This tests if filtering works on a known string column from each telemetry type, with various predicates")
    void testStringFilters() {
        FilterInfo stringFilter = createFilterInfoWithParams(KnownRequestColumns.url, PredicateType.CONTAINS, "hi");
        List<FilterConjunctionGroupInfo> filterGroups = createListWithOneFilterConjunctionGroupAndOneFilter(stringFilter);
        DerivedMetricInfo derivedMetricInfo = createDerivedMetricInfo("random-id", "Request", AggregationType.SUM, AggregationType.SUM, "Count()", filterGroups);
        RequestDataColumns request = new RequestDataColumns("https://test.com/hiThere", 200, 200, true, "GET /hiThere");
        DependencyDataColumns dependency = new DependencyDataColumns("test.com", 200, true, "GET /hiThere", 200, "HTTP", "https://test.com/hiThere?x=y");
        TraceDataColumns trace = new TraceDataColumns("hi there");
        ExceptionDataColumns exception = new ExceptionDataColumns("Exception Message hi", "Stack Trace");
        FilterConjunctionGroupInfo filterGroup = filterGroups.get(0);

        // Request Url filter matches
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, request));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, request));

        // Request Url filter does not match
        request.setUrl("https://test.com/bye");
        assertFalse(Filter.checkFilterConjunctionGroup(filterGroup, request));
        assertFalse(Filter.checkMetricFilters(derivedMetricInfo, request));

        // Dependency Data filter matches
        derivedMetricInfo.setTelemetryType("Dependency");
        stringFilter.setFieldName(KnownDependencyColumns.data);
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, dependency));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, dependency));

        // Dependency Data filter does not match
        dependency.setData("https://test.com/bye");
        assertFalse(Filter.checkFilterConjunctionGroup(filterGroup, dependency));
        assertFalse(Filter.checkMetricFilters(derivedMetricInfo, dependency));

        // Trace Message filter matches
        derivedMetricInfo.setTelemetryType("Trace");
        stringFilter.setFieldName("Message");
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, trace));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, trace));

        // Trace Message filter does not match
        trace.setMessage("bye");
        assertFalse(Filter.checkFilterConjunctionGroup(filterGroup, trace));
        assertFalse(Filter.checkMetricFilters(derivedMetricInfo, trace));

        // Exception Message filter matches. Note that fieldName is "Message" here and that's intended (we remove the Exception. prefix when validating config)
        derivedMetricInfo.setTelemetryType("Exception");
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, exception));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, exception));

        // Exception Message filter does not match
        exception.setMessage("Exception Message");
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
        List<FilterConjunctionGroupInfo> filterGroups = new ArrayList<FilterConjunctionGroupInfo>(List.of(filterGroup));

        DerivedMetricInfo derivedMetricInfo = createDerivedMetricInfo("random-id", "Request", AggregationType.SUM, AggregationType.SUM, "Count()", filterGroups);
        RequestDataColumns request = new RequestDataColumns("https://test.com/hiThere", 200, 200, true, "GET /hiThere");

        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, request));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, request));
    }

    @Test
    @Description("If there are multiple filters in a FilterConjunctionGroupInfo, then telemetry should only match if all conditions satisfied")
    void testMultipleFiltersInGroup() {
        FilterInfo filter1 = createFilterInfoWithParams(KnownRequestColumns.url, PredicateType.CONTAINS, "hi");
        FilterInfo filter2 = createFilterInfoWithParams(KnownRequestColumns.responseCode, PredicateType.EQUAL, "200");
        FilterConjunctionGroupInfo filterGroup = new FilterConjunctionGroupInfo();
        filterGroup.setFilters(new ArrayList<>(List.of(filter1, filter2)));
        List<FilterConjunctionGroupInfo> filterGroups = new ArrayList<>(List.of(filterGroup));

        DerivedMetricInfo derivedMetricInfo = createDerivedMetricInfo("random-id", "Request", AggregationType.SUM, AggregationType.SUM, "Count()", filterGroups);
        RequestDataColumns request = new RequestDataColumns("https://test.com/hiThere", 200, 200, true, "GET /hiThere");

        // matches both filters
        assertTrue(Filter.checkFilterConjunctionGroup(filterGroup, request));
        assertTrue(Filter.checkMetricFilters(derivedMetricInfo, request));

        // only one filter matches, the entire conjunction group should return false
        request.setUrl("https://test.com/bye");
        assertFalse(Filter.checkFilterConjunctionGroup(filterGroup, request));
        assertFalse(Filter.checkMetricFilters(derivedMetricInfo, request));
    }

    @Test
    @Description("Test the constructors for child classes of TelemetryColumns")
    void testConstructors() {
        TelemetryItem requestItem = QuickPulseTestBase.createRequestTelemetry("GET /hiThere", new Date(), 1234567890L, "200", true);
        TelemetryItem dependencyItem = QuickPulseTestBase.createRemoteDependencyTelemetry("GET /hiThere", "https://test.com/hiThere?x=y" , 400, true);

        TelemetryExceptionData exceptionItem = new TelemetryExceptionData();
        TelemetryExceptionDetails details = new TelemetryExceptionDetails();
        details.setMessage("A message");
        details.setStack("A stack trace");
        exceptionItem.setExceptions(new ArrayList<TelemetryExceptionDetails>(List.of(details)));

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
        assertEquals(dependencyDataColumns.getDuration(),400000L);
        assertEquals(dependencyDataColumns.getResultCode(), 200);
        assertEquals(dependencyDataColumns.getTarget(), "test.com");

        assertEquals(exceptionData.getMessage(), "A message");
        assertEquals(exceptionData.getStackTrace(), "A stack trace");

        assertEquals(traceDataColumns.getMessage(), "A message");
    }

}


