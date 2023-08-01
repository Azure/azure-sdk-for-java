// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.Undefined;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Used internally for query in the Azure Cosmos DB database service.
 */
public final class QueryItem extends JsonSerializable {
    private Object item;

    public QueryItem(ObjectNode objectNode) {
        super(objectNode);
    }

    public QueryItem(String jsonString) {
        super(jsonString);
    }

    public Object getItem() {
        if (this.item == null) {
            Object rawItem = super.get("item");
            this.item = super.has("item") ? rawItem : Undefined.value();
        }

        return this.item;
    }

    @Override
    public String toJson() {
        return super.toJson();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
