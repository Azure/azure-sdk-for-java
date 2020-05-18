// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query.aggregation;

import com.azure.cosmos.implementation.Undefined;
import com.azure.cosmos.implementation.query.ItemComparator;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class MinAggregator implements Aggregator {
    private Object value;

    public MinAggregator() {
        this.value = Undefined.value();
    }

    @Override
    public void aggregate(Object item) {

        if (item instanceof ObjectNode) {
            ObjectNode objectNode = (ObjectNode) item;
            if (objectNode.hasNonNull("count")) {
                long count = objectNode.get("count").asLong();
                if (count == 0) {
                    // Ignore the value since the continuation / partition had no results that matched the filter
                    // so min/max is undefined.
                    return;
                }

                if (objectNode.has("min")) {
                    item = MaxAggregator.getValue(objectNode.get("min"));
                } else {
                    item = Undefined.value();
                }
            }
        }

        // Add check for undefined
        if (item == Undefined.value()) {
            return;
        }

        if (Undefined.value().equals(this.value)) {
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
