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

public class DerivedMetricProjectionsTest {
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
        DerivedMetricInfo dmiRequest = createDerivedMetricInfoWithEmptyFilters("id-for-request", "Request",
            AggregationType.SUM, AggregationType.SUM, DerivedMetricProjections.COUNT);
        DerivedMetricInfo dmiDep = createDerivedMetricInfoWithEmptyFilters("id-for-dependency", "Dependency",
            AggregationType.SUM, AggregationType.SUM, DerivedMetricProjections.COUNT);
        DerivedMetricInfo dmiException = createDerivedMetricInfoWithEmptyFilters("id-for-exception", "Exception",
            AggregationType.SUM, AggregationType.SUM, DerivedMetricProjections.COUNT);
        DerivedMetricInfo dmiTrace = createDerivedMetricInfoWithEmptyFilters("id-for-trace", "Trace",
            AggregationType.SUM, AggregationType.SUM, DerivedMetricProjections.COUNT);

        RequestDataColumns request = new RequestDataColumns("https://test.com/hiThere", 200000L, 200, true,
            "GET /hiThere", new HashMap<>(), new HashMap<>());
        DependencyDataColumns dependency = new DependencyDataColumns("test.com", 200000L, true, "GET /hiThere", 200,
            "HTTP", "https://test.com/hiThere?x=y", new HashMap<>(), new HashMap<>());
        ExceptionDataColumns exception
            = new ExceptionDataColumns("Exception Message", "Stack Trace", new HashMap<>(), new HashMap<>());
        TraceDataColumns trace = new TraceDataColumns("Message", new HashMap<>(), new HashMap<>());

        Map<String, AggregationType> projectionInfo = new HashMap<>();
        projectionInfo.put("id-for-request", AggregationType.SUM);
        projectionInfo.put("id-for-dependency", AggregationType.SUM);
        projectionInfo.put("id-for-exception", AggregationType.SUM);
        projectionInfo.put("id-for-trace", AggregationType.SUM);
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
        assertEquals(finalValues.get("id-for-request"), 2.0);
        assertEquals(finalValues.get("id-for-dependency"), 3.0);
        assertEquals(finalValues.get("id-for-exception"), 1.0);
        assertEquals(finalValues.get("id-for-trace"), 4.0);
    }

    @Test
    void testDurationAvgProjection() {
        testDurationProjectionWith("request-avg", "dependency-avg", AggregationType.AVG, 400.0);
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
        testCustomDimProjectionWith("request-avg", AggregationType.AVG, 8.0);
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
        DerivedMetricInfo dmiRequest = createDerivedMetricInfoWithEmptyFilters("request-avg", "Request",
            AggregationType.AVG, AggregationType.AVG, "CustomDimensions.property");

        Map<String, AggregationType> projectionInfo = new HashMap<>();
        projectionInfo.put("request-avg", AggregationType.AVG);
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
        assertEquals(finalValues.get("request-avg"), 0.0);
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

    private void testDurationProjectionWith(String requestId, String depedencyId, AggregationType aggregationType,
        double expectedValue) {
        DerivedMetricInfo dmiRequest = createDerivedMetricInfoWithEmptyFilters(requestId, "Request", aggregationType,
            aggregationType, KnownRequestColumns.DURATION);

        DerivedMetricInfo dmiDep = createDerivedMetricInfoWithEmptyFilters(depedencyId, "Dependency", aggregationType,
            aggregationType, KnownRequestColumns.DURATION);

        List<Long> durations = asList(200000L, 400000L, 600000L);
        List<RequestDataColumns> requests = createRequestsOfDurations(durations);
        List<DependencyDataColumns> depedencies = createDepsOfDurations(durations);

        Map<String, AggregationType> projectionInfo = new HashMap<>();
        projectionInfo.put(requestId, aggregationType);
        projectionInfo.put(depedencyId, aggregationType);

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
        assertEquals(finalValues.get(depedencyId), expectedValue);
    }

    private void testCustomDimProjectionWith(String requestId, AggregationType aggregationType, double expectedValue) {
        DerivedMetricInfo dmiRequest = createDerivedMetricInfoWithEmptyFilters(requestId, "Request", aggregationType,
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
