package com.microsoft.windowsazure.services.table.models;

import java.util.List;

public class QueryTablesResult {
    private String nextTableName;
    private List<TableEntry> tables;

    public String getNextTableName() {
        return nextTableName;
    }

    public void setNextTableName(String nextTableName) {
        this.nextTableName = nextTableName;
    }

    public List<TableEntry> getTables() {
        return tables;
    }

    public void setTables(List<TableEntry> tables) {
        this.tables = tables;
    }
}
