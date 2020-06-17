package com.azure.data.tables;

public class TableAsyncClientBuilder {

    String connectionString;
    String tableName;


    public TableAsyncClientBuilder connectionString(String connectionString) {
        connectionString = connectionString;
        return this;
    }

    public TableAsyncClientBuilder tableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public TableAsyncClient build() {
        return new TableAsyncClient(tableName);
    }

    public void TableAysncClientBuilder() {

    }
}
