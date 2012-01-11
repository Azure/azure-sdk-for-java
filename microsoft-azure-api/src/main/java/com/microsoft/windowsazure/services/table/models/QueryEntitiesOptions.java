package com.microsoft.windowsazure.services.table.models;

public class QueryEntitiesOptions extends TableServiceOptions {
    private QueryBuilder query;

    @Override
    public QueryEntitiesOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    public QueryBuilder getQuery() {
        return query;
    }

    public QueryEntitiesOptions setQuery(QueryBuilder query) {
        this.query = query;
        return this;
    }
}
