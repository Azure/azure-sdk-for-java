package com.microsoft.windowsazure.services.table.models;

import java.util.List;

public class QueryTablesResult {
    private String continuationToken;
    private List<TableEntry> tables;

    public String getContinuationToken() {
        return continuationToken;
    }

    public void setContinuationToken(String continuationToken) {
        this.continuationToken = continuationToken;
    }

    public List<TableEntry> getTables() {
        return tables;
    }

    public void setTables(List<TableEntry> tables) {
        this.tables = tables;
    }
}
