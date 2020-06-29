// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query.aggregation;

import com.azure.cosmos.implementation.Undefined;
import com.azure.cosmos.implementation.query.ItemComparator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class MaxAggregator implements Aggregator {
    private static final String MAX_PROPERTY_NAME = "max";
    private Object value;

    public MaxAggregator() {
        this.value = Undefined.value();
    }

    static Object getValue(Object value) {

        if (value instanceof TextNode) {
            return ((JsonNode) value).asText();
        } else if (value instanceof NumericNode) {
            return ((JsonNode) value).numberValue();
        } else if (value instanceof BooleanNode) {
            return ((JsonNode) value).asBoolean();
        } else if (value instanceof NullNode) {
            return null;
        } else {
            return value;
        }
    }

    @Override
    public void aggregate(Object item) {

        if (item instanceof ObjectNode) {
            ObjectNode objectNode = (ObjectNode) item;
            if (objectNode.hasNonNull(MinAggregator.COUNT_PROPERTY_NAME)) {
                long count = objectNode.get(MinAggregator.COUNT_PROPERTY_NAME).asLong();
                if (count == 0) {
                    // Ignore the value since the continuation / partition had no results that matched the filter
                    // so min/max is undefined.
                    return;
                }

                if (objectNode.has(MAX_PROPERTY_NAME)) {
                    item = getValue(objectNode.get(MAX_PROPERTY_NAME));
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
        } else if (ItemComparator.getInstance().compare(item, this.value) > 0) {
            this.value = item;
        }

    }

    @Override
    public Object getResult() {
        return this.value;
    }
}
