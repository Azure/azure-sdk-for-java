// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.http.rest.PagedIterable;
import com.azure.data.tables.models.TableItem;

/**
 * This sample demonstrates how to create, list and delete tables synchronously.
 */
public class TableServiceHelloWorld {
    /**
     * Authenticates with the Table service and shows how to create, list and delete tables synchronously.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        /* Instantiate a TableServiceClient that will be used to call the service. Notice that the client is using a
        connection string. For information on how to obtain a connection string, please refer to this project samples'
        README.*/
        TableServiceClient tableServiceClient = new TableServiceClientBuilder()
            .connectionString("DefaultEndpointsProtocol=https;AccountName=dummyAccount;AccountKey=xyzDummy;EndpointSuffix=core.windows.net")
            .buildClient();

        // We'll first create a table. This operation produces a TableClient that can be used to perform operations
        // directly on the table.
        String tableName = "myTable";

        TableClient tableClient = tableServiceClient.createTable(tableName);

        System.out.printf("Created table with name '%s'.%n", tableClient.getTableName());

        // We will then retrieve all tables for this account's Table service and print their names.
        PagedIterable<TableItem> tableItems = tableServiceClient.listTables();

        tableItems.forEach(tableItem -> System.out.printf("Retrieved table with name '%s'.%n", tableItem.getName()));

        // Finally, let's delete the table we created above.
        tableServiceClient.deleteTable(tableName);

        System.out.printf("Deleted table with name '%s'.%n", tableName);
    }
}
