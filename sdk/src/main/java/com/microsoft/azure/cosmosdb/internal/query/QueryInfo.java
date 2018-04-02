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

package com.microsoft.azure.cosmosdb.internal.query;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.microsoft.azure.cosmosdb.JsonSerializable;
import com.microsoft.azure.cosmosdb.internal.query.aggregation.AggregateOperator;

/**
 * Used internally to encapsulates a query's information in the Azure Cosmos DB database service.
 */
@SuppressWarnings("serial")
public final class QueryInfo extends JsonSerializable {
    private Integer top;
    private Collection<SortOrder> orderBy;
    private Collection<AggregateOperator> aggregates;
    private Collection<String> orderByExpressions;
    private String rewrittenQuery;

    public QueryInfo() { }

    public QueryInfo(String jsonString) {
        super(jsonString);
    }

    public QueryInfo(JSONObject jsonObject) {
        super(jsonObject);
    }

    public Integer getTop() {
        return this.top != null ? this.top : (this.top = super.getInt("top"));
    }

    public Collection<SortOrder> getOrderBy() {
        return this.orderBy != null ? this.orderBy : (this.orderBy = super.getCollection("orderBy", SortOrder.class));
    }

    public String getRewrittenQuery() {
        return this.rewrittenQuery != null ? this.rewrittenQuery
                : (this.rewrittenQuery = super.getString("rewrittenQuery"));
    }

    public boolean hasTop() {
        return this.getTop() != null;
    }

    public boolean hasOrderBy() {
        Collection<SortOrder> orderBy = this.getOrderBy();
        return orderBy != null && orderBy.size() > 0;
    }

    public boolean hasRewrittenQuery() {
        return !StringUtils.isEmpty(this.getRewrittenQuery());
    }

    public boolean hasAggregates() {
        Collection<AggregateOperator> aggregates = this.getAggregates();
        return aggregates != null && aggregates.size() > 0;
    }

    public Collection<AggregateOperator> getAggregates() {
        return this.aggregates != null
                ? this.aggregates
                : (this.aggregates = super.getCollection("aggregates", AggregateOperator.class));
    }

    public Collection<String> getOrderByExpressions() {
        return this.orderByExpressions != null
                ? this.orderByExpressions
                : (this.orderByExpressions = super.getCollection("orderByExpressions", String.class));
    }
}