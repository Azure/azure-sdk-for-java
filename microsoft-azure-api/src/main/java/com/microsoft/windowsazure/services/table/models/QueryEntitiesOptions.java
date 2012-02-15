package com.microsoft.windowsazure.services.table.models;

public class QueryEntitiesOptions extends TableServiceOptions {
    private Query query;
    public String nextPartitionKey;
    public String nextRowKey;

    @Override
    public QueryEntitiesOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    public Query getQuery() {
        return query;
    }

    public QueryEntitiesOptions setQuery(Query query) {
        this.query = query;
        return this;
    }

    public String getNextPartitionKey() {
        return nextPartitionKey;
    }

    public QueryEntitiesOptions setNextPartitionKey(String nextPartitionKey) {
        this.nextPartitionKey = nextPartitionKey;
        return this;
    }

    public String getNextRowKey() {
        return nextRowKey;
    }

    public QueryEntitiesOptions setNextRowKey(String nextRowKey) {
        this.nextRowKey = nextRowKey;
        return this;
    }
}
