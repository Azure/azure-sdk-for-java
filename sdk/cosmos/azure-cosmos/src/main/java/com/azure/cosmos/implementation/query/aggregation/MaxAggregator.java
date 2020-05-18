// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query.aggregation;

import com.azure.cosmos.implementation.Undefined;
import com.azure.cosmos.implementation.query.ItemComparator;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class MaxAggregator implements Aggregator {
    private Object value;

    public MaxAggregator() {
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

                if (objectNode.has("max")) {
                    item = getValue(objectNode.get("max"));
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

    static Object getValue(Object value) {
        if (value instanceof TextNode) {
            return ((TextNode) value).asText();
        } else if (value instanceof DoubleNode) {
            return ((DoubleNode) value).asDouble();
        } else if (value instanceof IntNode) {
            return ((IntNode) value).intValue();
        } else if (value instanceof BooleanNode) {
            return ((BooleanNode) value).asBoolean();
        } else if (value instanceof NullNode) {
            return null;
        } else {
            return value;
        }
    }

    @Override
    public Object getResult() {
        return this.value;
    }
}
