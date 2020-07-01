// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.annotation.ServiceClient;

import java.util.List;

/**
 * client for table service
 */
@ServiceClient(
    builder = TableServiceClientBuilder.class)
public class TableServiceClient {

    TableServiceClient() {
    }

    /**
     * creates the table with the given name.  If a table with the same name already exists, the operation fails.
     *
     * @param name the name of the table to create
     * @return AzureTable of the created table
     */
    public AzureTable createTable(String name) {
        return null;
    }

    /**
     * deletes the given table. Will error if the table doesn't exists or cannot be found with the given name.
     *
     * @param name the name of the table to be deleted
     */
    public void deleteTable(String name) {
    }

    /**
     * deletes the given table. Will error if the table doesn't exists or cannot be found with the given name.
     *
     * @param azureTable the table to be deleted
     */
    public void deleteTable(AzureTable azureTable) {
    }

    /**
     * gets a given table by name
     *
     * @param name the name of the table
     * @return associated azure table object
     */
    public AzureTable getTable(String name) {
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
