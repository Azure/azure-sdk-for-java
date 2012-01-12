package com.microsoft.windowsazure.services.table.models;

public class QueryTablesOptions extends TableServiceOptions {
    private String nextTableName;
    private Query query;

    @Override
    public QueryTablesOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    public Query getQuery() {
        return query;
    }

    public QueryTablesOptions setQuery(Query query) {
        this.query = query;
        return this;
    }

    public String getNextTableName() {
        return nextTableName;
    }

    public QueryTablesOptions setNextTableName(String nextTableName) {
        this.nextTableName = nextTableName;
        return this;
    }
}
