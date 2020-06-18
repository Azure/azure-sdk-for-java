// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.annotation.ServiceClientBuilder;

@ServiceClientBuilder(serviceClients = {TableClient.class, TableAsyncClient.class})
public class TableClientBuilder {
    String connectionString;
    String tableName;


    public TableClientBuilder connectionString(String connectionString) {
        this.connectionString = connectionString;
        return this;
    }

    public TableClientBuilder tableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public TableClient buildClient() {
        return new TableClient(tableName);
    }

    public TableAsyncClient buildAsyncClient() {
        return new TableAsyncClient(tableName);
    }

    TableClientBuilder() {

    }

}
