// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.query;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.JsonSerializable;
import com.azure.data.cosmos.internal.Utils.ValueHolder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public final class OrderByContinuationToken extends JsonSerializable {
    private static final String CompositeContinuationTokenPropertyName = "compositeToken";
    private static final String OrderByItemsPropetryName = "orderByItems";
    private static final String RidPropertyName = "rid";
    private static final String InclusivePropertyName = "inclusive";
    private static final Logger logger = LoggerFactory.getLogger(OrderByContinuationToken.class);

    public OrderByContinuationToken(CompositeContinuationToken compositeContinuationToken, QueryItem[] orderByItems,
            String rid, boolean inclusive) {
        if (compositeContinuationToken == null) {
            throw new IllegalArgumentException("CompositeContinuationToken must not be null.");
        }

        if (orderByItems == null) {
            throw new IllegalArgumentException("orderByItems must not be null.");
        }

        if (orderByItems.length == 0) {
            throw new IllegalArgumentException("orderByItems must not be empty.");
        }

        if (rid == null) {
            throw new IllegalArgumentException("rid must not be null.");
        }

        this.setCompositeContinuationToken(compositeContinuationToken);
        this.setOrderByItems(orderByItems);
        this.setRid(rid);
        this.setInclusive(inclusive);
    }

    private OrderByContinuationToken(String serializedOrderByContinuationToken) {
        super(serializedOrderByContinuationToken);
    }

    public static boolean tryParse(String serializedOrderByContinuationToken,
            ValueHolder<OrderByContinuationToken> outOrderByContinuationToken) {
        boolean parsed;
        try {
            OrderByContinuationToken orderByContinuationToken = new OrderByContinuationToken(
                    serializedOrderByContinuationToken);
            CompositeContinuationToken compositeContinuationToken = orderByContinuationToken
                    .getCompositeContinuationToken();
            if (compositeContinuationToken == null) {
                throw new IllegalArgumentException("compositeContinuationToken must not be null.");
            }

            orderByContinuationToken.getOrderByItems();
            orderByContinuationToken.getRid();
            orderByContinuationToken.getInclusive();

            outOrderByContinuationToken.v = orderByContinuationToken;
            parsed = true;
        } catch (Exception ex) {
            logger.debug(
                    "Received exception {} when trying to parse: {}", 
                    ex.getMessage(), 
                    serializedOrderByContinuationToken);
            parsed = false;
            outOrderByContinuationToken.v = null;
        }

        return parsed;
    }

    public CompositeContinuationToken getCompositeContinuationToken() {
        ValueHolder<CompositeContinuationToken> outCompositeContinuationToken = new ValueHolder<CompositeContinuationToken>();
        boolean succeeded = CompositeContinuationToken.tryParse(super.getString(CompositeContinuationTokenPropertyName),
                outCompositeContinuationToken);
        if (!succeeded) {
            throw new IllegalArgumentException("Continuation Token was not able to be parsed");
        }

        return outCompositeContinuationToken.v;
    }

    public QueryItem[] getOrderByItems() {
        List<QueryItem> queryItems = new ArrayList<QueryItem>();
        ArrayNode arrayNode = (ArrayNode) super.get(OrderByItemsPropetryName);
        for (JsonNode jsonNode : arrayNode) {
            QueryItem queryItem = new QueryItem(jsonNode.toString());
            queryItems.add(queryItem);
        }

        QueryItem[] queryItemsArray = new QueryItem[queryItems.size()];

        return queryItems.toArray(queryItemsArray);
    }

    public String getRid() {
        return super.getString(RidPropertyName);
    }

    public boolean getInclusive() {
        return super.getBoolean(InclusivePropertyName);
    }

    private void setCompositeContinuationToken(CompositeContinuationToken compositeContinuationToken) {
        BridgeInternal.setProperty(this, CompositeContinuationTokenPropertyName, compositeContinuationToken.toJson());
    }

    private void setOrderByItems(QueryItem[] orderByItems) {
        BridgeInternal.setProperty(this, OrderByItemsPropetryName, orderByItems);
    }

    private void setRid(String rid) {
        BridgeInternal.setProperty(this, RidPropertyName, rid);
    }

    private void setInclusive(boolean inclusive) {
        BridgeInternal.setProperty(this, InclusivePropertyName, inclusive);
    }
}
