/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.azure.data.cosmos.internal.query;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.azure.data.cosmos.JsonSerializable;
import com.azure.data.cosmos.internal.Utils.ValueHolder;

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
        super.set(CompositeContinuationTokenPropertyName, compositeContinuationToken.toJson());
    }

    private void setOrderByItems(QueryItem[] orderByItems) {
        super.set(OrderByItemsPropetryName, orderByItems);
    }

    private void setRid(String rid) {
        super.set(RidPropertyName, rid);
    }

    private void setInclusive(boolean inclusive) {
        super.set(InclusivePropertyName, inclusive);
    }
}
