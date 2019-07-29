// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.query.orderbyquery;

import com.azure.data.cosmos.internal.Document;
import com.azure.data.cosmos.internal.PartitionKeyRange;
import com.azure.data.cosmos.internal.query.QueryItem;

import java.util.List;

/**
 * Represents the result of a query in the Azure Cosmos DB database service.
 */
public final class OrderByRowResult<T> extends Document {
    private final Class<T> klass;
    private volatile List<QueryItem> orderByItems;
    private volatile T payload;
    private final PartitionKeyRange targetRange;
    private final String backendContinuationToken;

    public OrderByRowResult(
            Class<T> klass, 
            String jsonString, 
            PartitionKeyRange targetRange,
            String backendContinuationToken) {
        super(jsonString);
        this.klass = klass;
        this.targetRange = targetRange;
        this.backendContinuationToken = backendContinuationToken;
    }

    public List<QueryItem> getOrderByItems() {
        return this.orderByItems != null ? this.orderByItems
                : (this.orderByItems = super.getList("orderByItems", QueryItem.class));
    }

    public T getPayload() {
        return this.payload != null ? this.payload : (this.payload = super.getObject("payload", klass));
    }

    public PartitionKeyRange getSourcePartitionKeyRange() {
        return this.targetRange;
    }

    public String getSourceBackendContinuationToken() {
        return this.backendContinuationToken;
    }
}