// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.AggregationType;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.DerivedMetricInfo;

import java.util.HashMap;
import java.util.Map;

public class DerivedMetricProjections {

    public static final String COUNT = "Count()";
    private final Map<String, DerivedMetricAggregation> derivedMetricValues = new HashMap<>();

    private static final ClientLogger LOGGER = new ClientLogger(DerivedMetricProjections.class);

    public DerivedMetricProjections(Map<String, AggregationType> projectionInfo) {
        for (Map.Entry<String, AggregationType> entry : projectionInfo.entrySet()) {
            AggregationType aggregationType = entry.getValue();
            DerivedMetricAggregation value;
            if (aggregationType.equals(AggregationType.MIN)) {
                value = new DerivedMetricAggregation(Double.MAX_VALUE, aggregationType);
            } else if (aggregationType.equals(AggregationType.MAX)) {
                value = new DerivedMetricAggregation(Double.MIN_VALUE, aggregationType);
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
            result.put(id, dma.getFinalValue());
        }
        return result;
    }

    // Once a telemetry item passes a metric chart filter, we use that telemetry item to increment
    // a derived metric
    public void calculateProjection(DerivedMetricInfo derivedMetricInfo, TelemetryColumns columns) {
        double incrementBy = Double.NaN;
        if (COUNT.equals(derivedMetricInfo.getProjection())) {
            incrementBy = 1.0;
        } else if (KnownRequestColumns.DURATION.equals(derivedMetricInfo.getProjection())) {
            long duration = columns.getFieldValue(KnownRequestColumns.DURATION, Long.class);
            // in case duration from telemetrycolumns doesn't parse correctly.
            // also quickpulse expects duration derived metrics to be reported in millis.
            incrementBy = duration == -1 ? Double.NaN : (double) duration / 1000.0;
        } else if (derivedMetricInfo.getProjection().startsWith(Filter.CUSTOM_DIM_FIELDNAME_PREFIX)) {
            String customDimKey
                = derivedMetricInfo.getProjection().substring(Filter.CUSTOM_DIM_FIELDNAME_PREFIX.length());
            incrementBy = columns.getCustomDimValueForProjection(customDimKey);
            // It is possible for the custom dim value to not parse to a double, or for the custom dim key to not be present.
            // For now, such cases produce Double.Nan and get skipped when calculating projection.
        }

        if (Double.isNaN(incrementBy)) {
            LOGGER.verbose(
                "This telemetry item will not be counted in derived metric projections because the Duration or a CustomDimension column could not be interpreted as a numeric value.");
        } else {
            calculateAggregation(derivedMetricInfo.getId(), incrementBy);
        }
    }

    private void calculateAggregation(String id, double incrementBy) {
        DerivedMetricAggregation dma = derivedMetricValues.get(id);
        dma.update(incrementBy);
    }

}
