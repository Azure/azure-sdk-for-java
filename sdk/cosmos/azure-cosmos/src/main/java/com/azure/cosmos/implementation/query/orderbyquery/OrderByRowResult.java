// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query.orderbyquery;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.query.QueryItem;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

/**
 * Represents the result of a query in the Azure Cosmos DB database service.
 */
public final class OrderByRowResult<T> extends Document {
    private final Class<T> klass;
    private volatile List<QueryItem> orderByItems;
    private volatile T payload;
    private final FeedRangeEpkImpl targetRange;
    private final String backendContinuationToken;

    public OrderByRowResult(
            Class<T> klass,
            String jsonString,
            FeedRangeEpkImpl targetRange,
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

    @SuppressWarnings("unchecked")
    public T getPayload() {
        if (this.payload != null) {
            return this.payload;
        }
        final Object object = super.get("payload");
        if (klass == Document.class && !ObjectNode.class.isAssignableFrom(object.getClass())) {
            Document document = new Document();
            ModelBridgeInternal.setProperty(document, Constants.Properties.VALUE, object);
            payload = (T) document;
        } else {
            this.payload = super.getObject("payload", klass);
        }
        return payload;
    }

    public FeedRangeEpkImpl getSourceRange() {
        return this.targetRange;
    }

    public String getSourceBackendContinuationToken() {
        return this.backendContinuationToken;
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
