// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.query;

import com.azure.data.cosmos.JsonSerializable;
import com.azure.data.cosmos.internal.query.aggregation.AggregateOperator;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;

/**
 * Used internally to encapsulates a query's information in the Azure Cosmos DB database service.
 */
public final class QueryInfo extends JsonSerializable {
    private static final String HAS_SELECT_VALUE = "hasSelectValue";
    private Integer top;
    private List<SortOrder> orderBy;
    private Collection<AggregateOperator> aggregates;
    private Collection<String> orderByExpressions;
    private String rewrittenQuery;
    private Integer offset;
    private Integer limit;

    public QueryInfo() { }

    public QueryInfo(String jsonString) {
        super(jsonString);
    }

    public Integer getTop() {
        return this.top != null ? this.top : (this.top = super.getInt("top"));
    }

    public List<SortOrder> getOrderBy() {
        return this.orderBy != null ? this.orderBy : (this.orderBy = super.getList("orderBy", SortOrder.class));
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

    public boolean hasSelectValue(){
        return super.has(HAS_SELECT_VALUE) && super.getBoolean(HAS_SELECT_VALUE);
    }

    public boolean hasOffset() {
        return this.getOffset() != null;
    }

    public boolean hasLimit() {
        return this.getLimit() != null;
    }

    public Integer getLimit() {
        return this.limit != null ? this.limit : (this.limit = super.getInt("limit"));
    }

    public Integer getOffset() {
        return this.offset != null ? this.offset : (this.offset = super.getInt("offset"));
    }
}

