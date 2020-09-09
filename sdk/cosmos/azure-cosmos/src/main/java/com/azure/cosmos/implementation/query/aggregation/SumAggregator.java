// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query.aggregation;

import com.azure.cosmos.implementation.Undefined;

public class SumAggregator implements Aggregator {
    private Double sum;

    @Override
    public void aggregate(Object item) {
        if (Undefined.value().equals(item) || item == null) {
            return;
        }

        if (this.sum == null) {
            this.sum = 0.0;
        }
        if (MaxAggregator.getValue(item) != null) {
            this.sum += ((Number) MaxAggregator.getValue(item)).doubleValue();
        }
    }

    @Override
    public Object getResult() {
        if (this.sum == null) {
            return Undefined.value();
        }
        return this.sum;
    }
}
