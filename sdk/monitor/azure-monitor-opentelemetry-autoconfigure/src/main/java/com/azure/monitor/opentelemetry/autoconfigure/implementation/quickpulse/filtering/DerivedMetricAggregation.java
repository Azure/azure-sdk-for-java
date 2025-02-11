// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.AggregationType;

public class DerivedMetricAggregation {
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

    private double aggregation;
    private long count = 0;
    final AggregationType aggregationType;
    private final Object lock = new Object();

    DerivedMetricAggregation(double initValue, AggregationType type) {
        aggregation = initValue;
        aggregationType = type;
    }

    void update(double incrementBy) {
        synchronized (lock) {
            count++;
            if (aggregationType.equals(AggregationType.SUM) || aggregationType.equals(AggregationType.AVG)) {
                aggregation += incrementBy;
            } else if (aggregationType.equals(AggregationType.MIN)) {
                aggregation = Math.min(aggregation, incrementBy);
            } else if (aggregationType.equals(AggregationType.MAX)) {
                aggregation = Math.max(aggregation, incrementBy);
            }
        }
    }

    double getFinalValue() {
        synchronized (lock) {
            if (count == 0) {
                return 0.0;
            }
            if (aggregationType.equals(AggregationType.AVG)) {
                return aggregation / count;
            }
            return aggregation;
        }
    }
}
