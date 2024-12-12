package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filteringTest;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering.*;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.AggregationType;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.DerivedMetricInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.FilterInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.FilterConjunctionGroupInfo;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DerivedMetricProjectionsTest {
    private DerivedMetricInfo createDerivedMetricInfoWithEmptyFilters(String id, String telemetryType, AggregationType agg,
                                                      AggregationType backendAgg, String projection) {
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
        DerivedMetricInfo dmiRequest = createDerivedMetricInfoWithEmptyFilters("id-for-request", "Request", AggregationType.SUM, AggregationType.SUM, DerivedMetricProjections.COUNT);
        DerivedMetricInfo dmiDep = createDerivedMetricInfoWithEmptyFilters("id-for-dependency", "Dependency", AggregationType.SUM, AggregationType.SUM, DerivedMetricProjections.COUNT);
        DerivedMetricInfo dmiException = createDerivedMetricInfoWithEmptyFilters("id-for-exception", "Exception", AggregationType.SUM, AggregationType.SUM, DerivedMetricProjections.COUNT);
        DerivedMetricInfo dmiTrace = createDerivedMetricInfoWithEmptyFilters("id-for-trace", "Trace", AggregationType.SUM, AggregationType.SUM, DerivedMetricProjections.COUNT);

        RequestDataColumns request = new RequestDataColumns("https://test.com/hiThere", 200000L, 200, true, "GET /hiThere", new HashMap<>(), new HashMap<>());
        DependencyDataColumns dependency = new DependencyDataColumns("test.com", 200000L, true, "GET /hiThere", 200, "HTTP", "https://test.com/hiThere?x=y", new HashMap<>(), new HashMap<>());
        ExceptionDataColumns exception = new ExceptionDataColumns("Exception Message", "Stack Trace", new HashMap<>(), new HashMap<>());
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
    void testDurationProjection() {
        DerivedMetricInfo dmiRequestAvg = createDerivedMetricInfoWithEmptyFilters("request-avg", "Request", AggregationType.AVG, AggregationType.AVG, KnownRequestColumns.DURATION);
        DerivedMetricInfo dmiRequestMin = createDerivedMetricInfoWithEmptyFilters("request-min", "Request", AggregationType.MIN, AggregationType.MIN, KnownRequestColumns.DURATION);
        DerivedMetricInfo dmiRequestMax = createDerivedMetricInfoWithEmptyFilters("request-max", "Request", AggregationType.MAX, AggregationType.MAX, KnownRequestColumns.DURATION);
        DerivedMetricInfo dmiDepAvg = createDerivedMetricInfoWithEmptyFilters("dependency-avg", "Dependency", AggregationType.AVG, AggregationType.AVG, KnownRequestColumns.DURATION);
        DerivedMetricInfo dmiDepMin = createDerivedMetricInfoWithEmptyFilters("dependency-min", "Dependency", AggregationType.MIN, AggregationType.MIN, KnownRequestColumns.DURATION);
        DerivedMetricInfo dmiDepMax = createDerivedMetricInfoWithEmptyFilters("dependency-max", "Dependency", AggregationType.MAX, AggregationType.MAX, KnownRequestColumns.DURATION);

        // The main dif between these requests/deps is the duration.
        RequestDataColumns request1 = new RequestDataColumns("https://test.com/hiThere", 200000L, 200, true, "GET /hiThere", new HashMap<>(), new HashMap<>());
        RequestDataColumns request2 = new RequestDataColumns("https://test.com/hiThere", 400000L, 200, true, "GET /hiThere", new HashMap<>(), new HashMap<>());
        RequestDataColumns request3 = new RequestDataColumns("https://test.com/hiThere", 600000L, 200, true, "GET /hiThere", new HashMap<>(), new HashMap<>());
        RequestDataColumns request4 = new RequestDataColumns("https://test.com/hiThere", 100000L, 200, true, "GET /hiThere", new HashMap<>(), new HashMap<>());
        RequestDataColumns request5 = new RequestDataColumns("https://test.com/hiThere", 500000L, 200, true, "GET /hiThere", new HashMap<>(), new HashMap<>());
        DependencyDataColumns dep1 = new DependencyDataColumns("test.com", 200000L, true, "GET /hiThere", 200, "HTTP", "https://test.com/hiThere?x=y", new HashMap<>(), new HashMap<>());
        DependencyDataColumns dep2 = new DependencyDataColumns("test.com", 400000L, true, "GET /hiThere", 200, "HTTP", "https://test.com/hiThere?x=y", new HashMap<>(), new HashMap<>());
        DependencyDataColumns dep3 = new DependencyDataColumns("test.com", 600000L, true, "GET /hiThere", 200, "HTTP", "https://test.com/hiThere?x=y", new HashMap<>(), new HashMap<>());
        DependencyDataColumns dep4 = new DependencyDataColumns("test.com", 100000L, true, "GET /hiThere", 200, "HTTP", "https://test.com/hiThere?x=y", new HashMap<>(), new HashMap<>());
        DependencyDataColumns dep5 = new DependencyDataColumns("test.com", 500000L, true, "GET /hiThere", 200, "HTTP", "https://test.com/hiThere?x=y", new HashMap<>(), new HashMap<>());

        Map<String, AggregationType> projectionInfo = new HashMap<>();
        projectionInfo.put("request-avg", AggregationType.AVG);
        projectionInfo.put("request-min", AggregationType.MIN);
        projectionInfo.put("request-max", AggregationType.MAX);
        projectionInfo.put("dependency-avg", AggregationType.AVG);
        projectionInfo.put("dependency-min", AggregationType.MIN);
        projectionInfo.put("dependency-max", AggregationType.MAX);
        DerivedMetricProjections projections = new DerivedMetricProjections(projectionInfo);

        // request duration - avg
        projections.calculateProjection(dmiRequestAvg, request1);
        projections.calculateProjection(dmiRequestAvg, request2);
        projections.calculateProjection(dmiRequestAvg, request3);

        // request duration - min
        projections.calculateProjection(dmiRequestMin, request3);
        projections.calculateProjection(dmiRequestMin, request4);
        projections.calculateProjection(dmiRequestMin, request5);

        // request duration - max
        projections.calculateProjection(dmiRequestMax, request5);
        projections.calculateProjection(dmiRequestMax, request4);
        projections.calculateProjection(dmiRequestMax, request3);

        // dep duration - avg
        projections.calculateProjection(dmiDepAvg, dep1);
        projections.calculateProjection(dmiDepAvg, dep2);
        projections.calculateProjection(dmiDepAvg, dep3);

        // dep duration - min
        projections.calculateProjection(dmiDepMin, dep3);
        projections.calculateProjection(dmiDepMin, dep4);
        projections.calculateProjection(dmiDepMin, dep5);

        // dep duration - max
        projections.calculateProjection(dmiDepMax, dep5);
        projections.calculateProjection(dmiDepMax, dep4);
        projections.calculateProjection(dmiDepMax, dep3);

        Map<String, Double> finalValues = projections.fetchFinalDerivedMetricValues();
        assertEquals(finalValues.get("request-avg"), 400.0);
        assertEquals(finalValues.get("request-min"), 100.0);
        assertEquals(finalValues.get("request-max"), 600.0);
        assertEquals(finalValues.get("dependency-avg"), 400.0);
        assertEquals(finalValues.get("dependency-min"), 100.0);
        assertEquals(finalValues.get("dependency-max"), 600.0);
    }

    // TODO (harskaur): add test for custom dim projection

}
