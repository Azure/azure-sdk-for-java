// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import java.util.concurrent.CountDownLatch;

/**
 * This sample demonstrates how to create, list and delete tables asynchronously.
 */
public class TableServiceHelloWorldAsync {
    /**
     * Authenticates with the Table service and shows how to create, list and delete tables asynchronously.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) throws InterruptedException {
        /* Instantiate a TableServiceAsyncClient that will be used to call the service. Notice that the client is using
        a connection string. For information on how to obtain a connection string, please refer to this project samples'
        README.*/
        TableServiceAsyncClient tableServiceAsyncClient = new TableServiceClientBuilder()
            .connectionString("DefaultEndpointsProtocol=https;AccountName=dummyAccount;AccountKey=xyzDummy;EndpointSuffix=core.windows.net")
            .buildAsyncClient();

        String tableName = "myTable";
        CountDownLatch latch = new CountDownLatch(1);

        // We'll first create a table. This operation produces a TableClient that can be used to perform operations
        // directly on the table.
        tableServiceAsyncClient.createTable(tableName)
            .map(tableAsyncClient ->
                System.out.printf("Created table with name '%s'.%n", tableAsyncClient.getTableName()))
            // We will then retrieve all tables for this account's Table service and print their names.
            .thenMany(tableServiceAsyncClient.listTables())
            .map(tableItem -> {
                System.out.printf("Retrieved table with name '%s'.%n", tableItem.getName());
                return tableItem;
            })
            .filter(tableItem -> tableItem.getName().equals(tableName))
            // Finally, let's delete the table we created above.
            .flatMap(tableItem -> tableServiceAsyncClient.deleteTable(tableItem.getName()))
            .subscribe(
                ignored -> System.out.printf("Deleted table with name '%s'.%n", tableName),
                ex -> {
                    System.out.println("An error occurred running the sample: " + ex.getMessage());
                    latch.countDown();
                },
                () -> {
                    System.out.println("Successfully completed running the sample");
                    latch.countDown();
                }
            );

        latch.await();
    }
}
