// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.query.aggregation;

import com.azure.data.cosmos.internal.Undefined;
import com.azure.data.cosmos.internal.query.ItemComparator;

public class MinAggregator implements Aggregator {
    private Object value;

    public MinAggregator() {
        this.value = Undefined.Value();
    }

    @Override
    public void aggregate(Object item) {
        if (Undefined.Value().equals(this.value)) {
            this.value = item;
        } else if (ItemComparator.getInstance().compare(item, this.value) < 0) {
            this.value = item;
        }
    }

    @Override
    public Object getResult() {
        return this.value;
    }
}
