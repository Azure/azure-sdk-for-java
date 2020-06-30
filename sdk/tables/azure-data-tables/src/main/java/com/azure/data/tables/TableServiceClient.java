// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.annotation.ServiceClient;

import java.util.List;

@ServiceClient(
    builder = TableServiceClientBuilder.class)
public class TableServiceClient {

    TableServiceClient() {
    }

    /**
     * creates the table with the given name.  If a table with the same name already exists, the operation fails.
     *
     * @param name the name of the table to create
     */
    public AzureTable createTable(String name) {
        return null;
    }

    /**
     * deletes the given table. Will error if the table doesn't exists or cannot be found with the given name.
     *
     * @param name
     */
    public void deleteTable(String name) {
    }

    /**
     * retrieves the table client for the provided table or creates one if it doesn't exist
     *
     * @param name the name of the table
     * @return associated TableClient
     */
    public TableClient getTableClient(String name) {
        return null;
    }

    /**
     * query all the tables under the storage account and return them
     *
     * @param queryOptions the odata query object
     * @return a list of tables that meet the query
     */
    public List<AzureTable> queryTables(QueryOptions queryOptions) {
        return null;
    }

}
