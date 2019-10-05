// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.query.aggregation;

import com.azure.data.cosmos.internal.Undefined;

public class SumAggregator implements Aggregator {
    private Double sum;

    @Override
    public void aggregate(Object item) {
        if (Undefined.Value().equals(item)) {
            return;
        }

        if (this.sum == null) {
            this.sum = 0.0;
        }
        this.sum += ((Number) item).doubleValue();
    }

    @Override
    public Object getResult() {
        if (this.sum == null) {
            return Undefined.Value();
        }
        return this.sum;
    }
}
