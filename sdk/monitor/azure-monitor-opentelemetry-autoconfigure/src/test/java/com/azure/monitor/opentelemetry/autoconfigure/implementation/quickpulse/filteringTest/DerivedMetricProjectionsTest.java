// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filteringTest;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering.*;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DerivedMetricProjectionsTest {

    public static final String REQUEST_AVG = "request-avg";
    public static final String REQUEST = "Request";
    public static final String DEPENDENCY = "Dependency";
    public static final String EXCEPTION = "Exception";
    public static final String TRACE = "Trace";

    private DerivedMetricInfo createDerivedMetricInfoWithEmptyFilters(String id, String telemetryType,
        AggregationType agg, AggregationType backendAgg, String projection) {
        DerivedMetricInfo result = new DerivedMetricInfo();
        result.setId(id);
        result.setTelemetryType(telemetryType);
        result.setAggregation(agg);
        result.setBackEndAggregation(backendAgg);
        result.setProjection(projection);

        FilterConjunctionGroupInfo filterGroup = new FilterConjunctionGroupInfo();
        filterGroup.setFilters(new ArrayList<FilterInfo>());
        List<FilterConjunctionGroupInfo> filterGroups = asList(filterGroup);

        result.setFilterGroups(filterGroups);
        return result;
    }

    @Test
    void testCountProjection() {
        String requestId = "id-for-request";
        String dependencyId = "id-for-dependency";
        String exceptionId = "id-for-exception";
        String traceId = "id-for-trace";

        DerivedMetricInfo dmiRequest = createDerivedMetricInfoWithEmptyFilters(requestId, REQUEST, AggregationType.SUM,
            AggregationType.SUM, DerivedMetricProjections.COUNT);

        DerivedMetricInfo dmiDep = createDerivedMetricInfoWithEmptyFilters(dependencyId, DEPENDENCY,
            AggregationType.SUM, AggregationType.SUM, DerivedMetricProjections.COUNT);

        DerivedMetricInfo dmiException = createDerivedMetricInfoWithEmptyFilters(exceptionId, EXCEPTION,
            AggregationType.SUM, AggregationType.SUM, DerivedMetricProjections.COUNT);

        DerivedMetricInfo dmiTrace = createDerivedMetricInfoWithEmptyFilters(traceId, TRACE, AggregationType.SUM,
            AggregationType.SUM, DerivedMetricProjections.COUNT);

        RequestDataColumns request = new RequestDataColumns("https://test.com/hiThere", 200000L, 200, true,
            "GET /hiThere", new HashMap<>(), new HashMap<>());
        DependencyDataColumns dependency = new DependencyDataColumns("test.com", 200000L, true, "GET /hiThere", 200,
            "HTTP", "https://test.com/hiThere?x=y", new HashMap<>(), new HashMap<>());
        ExceptionDataColumns exception
            = new ExceptionDataColumns("Exception Message", "Stack Trace", new HashMap<>(), new HashMap<>());
        TraceDataColumns trace = new TraceDataColumns("Message", new HashMap<>(), new HashMap<>());

        Map<String, AggregationType> projectionInfo = new HashMap<>();
        projectionInfo.put(requestId, AggregationType.SUM);
        projectionInfo.put(dependencyId, AggregationType.SUM);
        projectionInfo.put(exceptionId, AggregationType.SUM);
        projectionInfo.put(traceId, AggregationType.SUM);
        DerivedMetricProjections projections = new DerivedMetricProjections(projectionInfo);

        for (int i = 0; i < 2; i++) {
            projections.calculateProjection(dmiRequest, request);
        }

        for (int i = 0; i < 3; i++) {
            projections.calculateProjection(dmiDep, dependency);
        }

        for (int i = 0; i < 4; i++) {
            projections.calculateProjection(dmiTrace, trace);
        }

        projections.calculateProjection(dmiException, exception);

        Map<String, Double> finalValues = projections.fetchFinalDerivedMetricValues();
        assertEquals(finalValues.get(requestId), 2.0);
        assertEquals(finalValues.get(dependencyId), 3.0);
        assertEquals(finalValues.get(exceptionId), 1.0);
        assertEquals(finalValues.get(traceId), 4.0);
    }

    @Test
    void testDurationAvgProjection() {
        testDurationProjectionWith(REQUEST_AVG, "dependency-avg", AggregationType.AVG, 400.0);
    }

    @Test
    void testDurationMinProjection() {
        testDurationProjectionWith("request-min", "dependency-min", AggregationType.MIN, 200.0);
    }

    @Test
    void testDurationMaxProjection() {
        testDurationProjectionWith("request-max", "dependency-max", AggregationType.MAX, 600.0);
    }

    @Test
    void testCustomDimensionAvgProjection() {
        testCustomDimProjectionWith(REQUEST_AVG, AggregationType.AVG, 8.0);
    }

    @Test
    void testCustomDimensionMinProjection() {
        testCustomDimProjectionWith("request-min", AggregationType.MIN, 4.0);
    }

    @Test
    void testCustomDimensionMaxProjection() {
        testCustomDimProjectionWith("request-max", AggregationType.MAX, 15.0);
    }

    @Test
    void testCustomDimensionSumProjection() {
        testCustomDimProjectionWith("request-sum", AggregationType.SUM, 24.0);
    }

    @Test
    void testInvalidCustomDimensionProjection() {
        DerivedMetricInfo dmiRequest = createDerivedMetricInfoWithEmptyFilters(REQUEST_AVG, REQUEST,
            AggregationType.AVG, AggregationType.AVG, "CustomDimensions.property");

        Map<String, AggregationType> projectionInfo = new HashMap<>();
        projectionInfo.put(REQUEST_AVG, AggregationType.AVG);
        DerivedMetricProjections projections = new DerivedMetricProjections(projectionInfo);

        // The case where the desired custom dimension property is not in the request
        RequestDataColumns notContained = new RequestDataColumns("https://test.com/hiThere", 200000L, 200, true,
            "GET /hiThere", new HashMap<>(), new HashMap<>());
        projections.calculateProjection(dmiRequest, notContained);

        // The case where the value of the desired custom dim property can't be parsed to a double
        Map<String, String> customDims = new HashMap<>();
        customDims.put("property", "hi");
        RequestDataColumns notDouble = new RequestDataColumns("https://test.com/hiThere", 200000L, 200, true,
            "GET /hiThere", customDims, new HashMap<>());
        projections.calculateProjection(dmiRequest, notDouble);

        // invalid values should not be counted.
        Map<String, Double> finalValues = projections.fetchFinalDerivedMetricValues();
        assertEquals(finalValues.get(REQUEST_AVG), 0.0);
    }

    private List<RequestDataColumns> createRequestsOfDurations(List<Long> durations) {
        List<RequestDataColumns> result = new ArrayList<>();
        for (int i = 0; i < durations.size(); i++) {
            result.add(new RequestDataColumns("https://test.com/hiThere", durations.get(i), 200, true, "GET /hiThere",
                new HashMap<>(), new HashMap<>()));
        }
        return result;
    }

    private List<DependencyDataColumns> createDepsOfDurations(List<Long> durations) {
        List<DependencyDataColumns> result = new ArrayList<>();
        for (int i = 0; i < durations.size(); i++) {
            result.add(new DependencyDataColumns("test.com", durations.get(i), true, "GET /hiThere", 200, "HTTP",
                "https://test.com/hiThere?x=y", new HashMap<>(), new HashMap<>()));
        }
        return result;
    }

    private void testDurationProjectionWith(String requestId, String dependencyId, AggregationType aggregationType,
        double expectedValue) {
        DerivedMetricInfo dmiRequest = createDerivedMetricInfoWithEmptyFilters(requestId, REQUEST, aggregationType,
            aggregationType, KnownRequestColumns.DURATION);

        DerivedMetricInfo dmiDep = createDerivedMetricInfoWithEmptyFilters(dependencyId, DEPENDENCY, aggregationType,
            aggregationType, KnownRequestColumns.DURATION);

        List<Long> durations = asList(200000L, 400000L, 600000L);
        List<RequestDataColumns> requests = createRequestsOfDurations(durations);
        List<DependencyDataColumns> depedencies = createDepsOfDurations(durations);

        Map<String, AggregationType> projectionInfo = new HashMap<>();
        projectionInfo.put(requestId, aggregationType);
        projectionInfo.put(dependencyId, aggregationType);

        DerivedMetricProjections projections = new DerivedMetricProjections(projectionInfo);

        // request duration
        for (RequestDataColumns request : requests) {
            projections.calculateProjection(dmiRequest, request);
        }

        // dep duration
        for (DependencyDataColumns dep : depedencies) {
            projections.calculateProjection(dmiDep, dep);
        }

        Map<String, Double> finalValues = projections.fetchFinalDerivedMetricValues();
        assertEquals(finalValues.get(requestId), expectedValue);
        assertEquals(finalValues.get(dependencyId), expectedValue);
    }

    private void testCustomDimProjectionWith(String requestId, AggregationType aggregationType, double expectedValue) {
        DerivedMetricInfo dmiRequest = createDerivedMetricInfoWithEmptyFilters(requestId, REQUEST, aggregationType,
            aggregationType, "CustomDimensions.property");

        Map<String, AggregationType> projectionInfo = new HashMap<>();
        projectionInfo.put(requestId, aggregationType);
        DerivedMetricProjections projections = new DerivedMetricProjections(projectionInfo);

        List<String> customDimValues = asList("5.0", "15.0", "4.0");
        List<RequestDataColumns> requests = createRequestsWithCustomDimValues(customDimValues);

        for (RequestDataColumns request : requests) {
            projections.calculateProjection(dmiRequest, request);
        }

        Map<String, Double> finalValues = projections.fetchFinalDerivedMetricValues();
        assertEquals(finalValues.get(requestId), expectedValue);
    }

    private List<RequestDataColumns> createRequestsWithCustomDimValues(List<String> customDimValues) {
        List<RequestDataColumns> result = new ArrayList<>();
        for (String value : customDimValues) {
            Map<String, String> customDims = new HashMap<>();
            customDims.put("property", value);
            result.add(new RequestDataColumns("https://test.com/hiThere", 200000L, 200, true, "GET /hiThere",
                customDims, new HashMap<>()));
        }
        return result;
    }

}
