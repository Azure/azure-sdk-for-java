// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.TokenCredential;

/**
 * builds table client
 */
@ServiceClientBuilder(serviceClients = {TableClient.class, TableAsyncClient.class})
public class TableClientBuilder {
    private String connectionString;
    private String tableName;

    /**
     * Sets the connection string to help build the client
     *
     * @param connectionString the connection string to the storage account
     * @return the TableClientBuilder
     */
    public TableClientBuilder connectionString(String connectionString) {
        this.connectionString = connectionString;
        return this;
    }

    /**
     * Sets the tokenCredential to help build the client
     *
     * @param tokenCredential the tokenCredential to the storage account
     * @return the TableClientBuilder
     */
    public TableClientBuilder connectionString(TokenCredential tokenCredential) {
        return this;
    }

    /**
     * Sets the table name to help build the client
     *
     * @param tableName name of the table for which the client is created for
     * @return the TableClientBuilder
     */
    public TableClientBuilder tableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    /**
     * builds a sync tableClient
     *
     * @return a sync tableClient
     */
    public TableClient buildClient() {
        return new TableClient(tableName);
    }

    /**
     * builds an async tableClient
     *
     * @return an aysnc tableClient
     */
    public TableAsyncClient buildAsyncClient() {
        return new TableAsyncClient(tableName);
    }

    /**
     * table client builder constructor
     */
    public TableClientBuilder() {
    }

    /**
     * gets the connection string
     * @return the connection string
     */
    public String getConnectionString() {
        return this.connectionString;
    }

}
