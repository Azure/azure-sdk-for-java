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

package com.microsoft.azure.cosmosdb.internal.query.orderbyquery;

import java.util.List;

import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.PartitionKeyRange;
import com.microsoft.azure.cosmosdb.internal.query.QueryItem;

/**
 * Represents the result of a query in the Azure Cosmos DB database service.
 */
@SuppressWarnings("serial")
public final class OrderByRowResult<T> extends Document {
    private final PartitionKeyRange targetRange;
    private final Class<T> klass;
    private volatile List<QueryItem> orderByItems;
    private volatile T payload;

    public OrderByRowResult(Class<T> klass, String jsonString, PartitionKeyRange targetRange) {
        super(jsonString);
        this.klass = klass;
        this.targetRange = targetRange;
    }

    public List<QueryItem> getOrderByItems() {
        return this.orderByItems != null ? this.orderByItems
                : (this.orderByItems = (List<QueryItem>) super.getCollection("orderByItems", QueryItem.class));
    }

    public T getPayload() {
        return this.payload != null ? this.payload : (this.payload = super.getObject("payload", klass));
    }

    public PartitionKeyRange getSourcePartitionKeyRange() {
        return this.targetRange;
    }
}