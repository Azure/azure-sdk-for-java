package com.microsoft.windowsazure.services.table.models;

import java.util.List;

public class QueryBuilder {
    private List<String> fields;
    private String from;
    private FilterExpression filter;
    private List<String> orderBy;
    private Integer top;
    private String partitionKey;
    private String nextPartitionKey;
    private String rowKey;
    private String nextRowKey;

    public List<String> getFields() {
        return fields;
    }

    public QueryBuilder setFields(List<String> fields) {
        this.fields = fields;
        return this;
    }

    public String getFrom() {
        return from;
    }

    public QueryBuilder setFrom(String from) {
        this.from = from;
        return this;
    }

    public FilterExpression getFilter() {
        return filter;
    }

    public QueryBuilder setFilter(FilterExpression where) {
        this.filter = where;
        return this;
    }

    public List<String> getOrderBy() {
        return orderBy;
    }

    public QueryBuilder setOrderBy(List<String> orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    public Integer getTop() {
        return top;
    }

    public QueryBuilder setTop(Integer top) {
        this.top = top;
        return this;
    }

    public String getPartitionKey() {
        return partitionKey;
    }

    public QueryBuilder setPartitionKey(String partitionKey) {
        this.partitionKey = partitionKey;
        return this;
    }

    public String getNextPartitionKey() {
        return nextPartitionKey;
    }

    public QueryBuilder setNextPartitionKey(String nextPartitionKey) {
        this.nextPartitionKey = nextPartitionKey;
        return this;
    }

    public String getRowKey() {
        return rowKey;
    }

    public QueryBuilder setRowKey(String rowKey) {
        this.rowKey = rowKey;
        return this;
    }

    public String getNextRowKey() {
        return nextRowKey;
    }

    public QueryBuilder setNextRowKey(String nextRowKey) {
        this.nextRowKey = nextRowKey;
        return this;
    }
}
