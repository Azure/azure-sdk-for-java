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
    public void createTable(String name) {
    }

    /**
     * deletes the given table. Will error if the table doesn't exists or cannot be found with the given name.
     *
     * @param name
     */
    public void deleteTable(String name) {
    }

    /**
     * query all the tables under the storage account and return them
     *
     * @param filterString the odata filter string
     * @return a list of tables that meet the query
     */
    public List<AzureTable> queryTables(String filterString) {
        return null;
    }

}
