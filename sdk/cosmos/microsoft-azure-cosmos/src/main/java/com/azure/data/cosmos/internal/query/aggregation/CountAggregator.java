// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.query.aggregation;

public class CountAggregator implements Aggregator {
    private long value;

    @Override
    public void aggregate(Object item) {
        value += Long.parseLong(item.toString());
    }

    @Override
    public Object getResult() {
        return value;
    }
}
