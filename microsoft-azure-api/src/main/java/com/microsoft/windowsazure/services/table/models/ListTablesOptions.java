package com.microsoft.windowsazure.services.table.models;

public class ListTablesOptions extends TableServiceOptions {
    private String prefix;

    @Override
    public ListTablesOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    public String getPrefix() {
        return prefix;
    }

    public ListTablesOptions setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }
}
