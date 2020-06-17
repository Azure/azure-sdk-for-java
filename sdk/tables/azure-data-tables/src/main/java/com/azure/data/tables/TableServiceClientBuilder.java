package com.azure.data.tables;

public class TableServiceClientBuilder {

    String connectionString;


    public TableServiceClientBuilder connectionString(String connectionString) {
        connectionString = connectionString;
        return this;
    }

    public TableServiceClient build() {
        return new TableServiceClient();
    }

    public TableServiceClientBuilder() {

    }

}
