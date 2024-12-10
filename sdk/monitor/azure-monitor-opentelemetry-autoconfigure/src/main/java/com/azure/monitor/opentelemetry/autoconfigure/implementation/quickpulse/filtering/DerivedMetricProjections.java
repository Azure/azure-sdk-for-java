package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.AggregationType;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.DerivedMetricInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class DerivedMetricProjections {

    public static final String COUNT = "Count()";
    private final Map<String, DerivedMetricAggregation> derivedMetricValues = new HashMap<>();

    public DerivedMetricProjections(Map<String, AggregationType> projectionInfo) {
        for (Map.Entry<String, AggregationType> entry : projectionInfo.entrySet()) {
            AggregationType aggregationType = entry.getValue();
            DerivedMetricAggregation value;
            if (aggregationType.equals(AggregationType.MIN)) {
                value = new DerivedMetricAggregation(Long.MAX_VALUE, aggregationType);
            } else if (aggregationType.equals(AggregationType.MAX)) {
                value = new DerivedMetricAggregation(Long.MIN_VALUE, aggregationType);
            } else if (aggregationType.equals(AggregationType.SUM) || aggregationType.equals(AggregationType.AVG)) {
                value = new DerivedMetricAggregation(0, aggregationType);
            } else {
                value = null; // we should never hit this case - that means the UI gave us an invalid aggregation type
            }
            derivedMetricValues.put(entry.getKey(), value);
        }
    }

    // This is intended to be called once for every post request
    public Map<String, Double> fetchFinalDerivedMetricValues() {
        Map<String, Double> result = new HashMap<>();
        for (Map.Entry<String, DerivedMetricAggregation> entry : derivedMetricValues.entrySet()) {
            String id = entry.getKey();
            DerivedMetricAggregation dma = entry.getValue();
            double intermediateValue = dma.aggregation.doubleValue();
            double count = dma.count.doubleValue();
            if (count == 0) {
                result.put(id, 0.0);
            } else {
                if (dma.aggregationType.equals(AggregationType.AVG)) {
                    result.put(id, intermediateValue / count);
                } else {
                    result.put(id, intermediateValue);
                }
            }
        }
        return result;
    }

    public void calculateProjection(DerivedMetricInfo derivedMetricInfo, TelemetryColumns columns) {
        double incrementBy = Double.NaN;
        if (COUNT.equals(derivedMetricInfo.getProjection())) {
            incrementBy = 1.0;
        } else if (KnownRequestColumns.DURATION.equals(derivedMetricInfo.getProjection())) {
            if (columns instanceof RequestDataColumns || columns instanceof DependencyDataColumns) {
                long duration = columns.getFieldValue(KnownRequestColumns.DURATION, Long.class);
                // in case duration from telemetrycolumns doesn't parse correctly.
                incrementBy = duration != -1 ? (double) duration : Double.NaN;
            }
            // The UI doesn't allow for Trace/Exception metrics charts to selection a projection that is a Duration,
            // so letting that case slip though.
        } else if (derivedMetricInfo.getProjection().startsWith(Filter.CUSTOM_DIM_FIELDNAME_PREFIX)) {
            String customDimKey = derivedMetricInfo.getProjection().substring(Filter.CUSTOM_DIM_FIELDNAME_PREFIX.length());
            incrementBy = columns.getCustomDimValueForProjection(customDimKey);
            // It is possible for the custom dim value to not parse to a double, or for the custom dim key to not be present.
            // For now, such cases produce Double.Nan and get skipped when calculating projection.
            // TODO (harskaur): For future PR, the error tracker should track the errors mentioned in lines above.
        }

        if (incrementBy != Double.NaN) {
            calculateAggregation(derivedMetricInfo.getAggregation(), derivedMetricInfo.getId(), incrementBy);
        }
    }

    private void calculateAggregation(AggregationType type, String id, double incrementBy) {
        DerivedMetricAggregation dma = derivedMetricValues.get(id);
        dma.count.getAndAdd(1);
        // TODO (harskaur): Use atomic double?? Long will turn out inaccurate with custom dim projections
        if (type.equals(AggregationType.SUM) || type.equals(AggregationType.AVG)) {
            dma.aggregation.getAndAdd((long) incrementBy);
        } else if (type.equals(AggregationType.MIN)) {
            dma.aggregation.getAndAccumulate((long) incrementBy , Math::min);
        } else if (type.equals(AggregationType.MAX)) {
            dma.aggregation.getAndAccumulate((long) incrementBy , Math::max);
        }
    }

    static class DerivedMetricAggregation {
        // This class represents the intermediate state of a derived metric value.
        // It keeps track of the count and the aggregated value so that these two
        // fields can be used to determine the final value of a derived metric
        // when the data fetcher asks for it.

        // Depending on the aggregationType, aggregation holds different values.
        // For min, it is the current minimum value
        // For max, it is the current max value
        // For sum & avg, this represents the current sum.
        // When metric values are retrieved by the data fetcher, the final value will
        // be determined based on the count and the aggregation.

        // TODO (harskaur): Use atomic double?? Long will turn out inaccurate with custom dim projections
        AtomicLong aggregation;
        AtomicLong count = new AtomicLong(0);
        AggregationType aggregationType;
        DerivedMetricAggregation(long initValue, AggregationType type) {
            aggregation = new AtomicLong(initValue);
            aggregationType = type;
        }
    }
}
