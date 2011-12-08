package com.microsoft.windowsazure.services.table.models;

public class QueryTablesOptions extends TableServiceOptions {
    private String query;

    @Override
    public QueryTablesOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    public String getQuery() {
        return query;
    }

    public QueryTablesOptions setQuery(String query) {
        this.query = query;
        return this;
    }
}
