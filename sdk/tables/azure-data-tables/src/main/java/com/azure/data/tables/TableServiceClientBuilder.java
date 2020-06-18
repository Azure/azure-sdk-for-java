// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

public class TableServiceClientBuilder {

    String connectionString;


    public TableServiceClientBuilder connectionString(String connectionString) {
        this.connectionString = connectionString;
        return this;
    }

    public TableServiceClient buildClient() {
        return new TableServiceClient();
    }

    public TableServiceAsyncClient buildAsyncClient() {
        return new TableServiceAsyncClient();
    }

    public TableServiceClientBuilder() {

    }

}
