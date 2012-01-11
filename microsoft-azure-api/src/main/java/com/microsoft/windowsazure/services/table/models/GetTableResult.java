package com.microsoft.windowsazure.services.table.models;

public class GetTableResult {
    private TableEntry tableEntry;

    public TableEntry getTableEntry() {
        return tableEntry;
    }

    public void setTableEntry(TableEntry tableEntry) {
        this.tableEntry = tableEntry;
    }
}
