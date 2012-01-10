package com.microsoft.windowsazure.services.table.models;

public class QueryTablesOptions extends TableServiceOptions {
    private QueryBuilder query;

    @Override
    public QueryTablesOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    public QueryBuilder getQuery() {
        return query;
    }

    public QueryTablesOptions setQuery(QueryBuilder query) {
        this.query = query;
        return this;
    }
}
