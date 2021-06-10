// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

/**
 * This sample demonstrates how to create, list and delete tables asynchronously.
 */
public class TableServiceHelloWorldAsync {
    /**
     * Authenticates with the Table service and shows how to create, list and delete tables asynchronously.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        /* Instantiate a TableServiceAsyncClient that will be used to call the service. Notice that the client is using a
        connection string. For information on how to obtain a connection string, please refer to this project samples'
        README.*/
        TableServiceAsyncClient tableServiceAsyncClient = new TableServiceClientBuilder()
            .connectionString("DefaultEndpointsProtocol=https;AccountName=dummyAccount;AccountKey=xyzDummy;EndpointSuffix=core.windows.net")
            .buildAsyncClient();

        // We'll first create a table. This operation produces a TableClient that can be used to perform operations
        // directly on the table.
        String tableName = "myTable";

        tableServiceAsyncClient.createTable(tableName)
            .doOnSuccess(tableClient ->
                System.out.printf("Created table with name '%s'.\n", tableClient.getTableName()))
            .block();

        // We will then retrieve all tables for this account's Table service and print their names.
        tableServiceAsyncClient.listTables()
            .doOnNext(tableItem -> System.out.printf("Retrieved table with name '%s'.\n", tableItem.getName()))
            .blockLast();

        // Finally, let's delete the table we created above.
        tableServiceAsyncClient.deleteTable(tableName)
            .doOnSuccess(unused -> System.out.printf("Deleted table with name '%s'.\n", tableName))
            .block();
    }
}
