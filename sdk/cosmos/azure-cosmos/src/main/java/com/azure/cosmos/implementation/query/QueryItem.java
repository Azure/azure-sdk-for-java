// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.JsonSerializable;
import com.azure.cosmos.implementation.Undefined;

/**
 * Used internally for query in the Azure Cosmos DB database service.
 */
public final class QueryItem extends JsonSerializable {
    private Object item;

    public QueryItem(String jsonString) {
        super(jsonString);
    }

    public Object getItem() {
        if (this.item == null) {
            Object rawItem = super.get("item");
            this.item = super.has("item") ? rawItem : Undefined.Value();
        }

        return this.item;
    }
}
