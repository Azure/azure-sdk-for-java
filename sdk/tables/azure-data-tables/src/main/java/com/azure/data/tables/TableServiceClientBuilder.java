// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

public class TableServiceClientBuilder {

    String connectionString;

    /**
     * Sets the connection string to help build the client
     *
     * @param connectionString the connection string to the storage account
     * @return the TableServiceClientBuilder
     */
    public TableServiceClientBuilder connectionString(String connectionString) {
        this.connectionString = connectionString;
        return this;
    }

    /**
     * builds a sync TableServiceClient
     *
     * @return a sync TableServiceClient
     */
    public TableServiceClient buildClient() {
        return new TableServiceClient();
    }

    /**
     * builds an async TableServiceAsyncClient
     *
     * @return an aysnc TableServiceAsyncClient
     */
    public TableServiceAsyncClient buildAsyncClient() {
        return new TableServiceAsyncClient();
    }

    public TableServiceClientBuilder() {

    }

}
