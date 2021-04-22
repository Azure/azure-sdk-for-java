// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables;

import com.azure.core.http.rest.PagedIterable;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableItem;

import java.util.Map;

/**
 * Sample that demonstrates how to create, list, and delete tables. It also shows how to create, list, update and
 * delete table entities.
 */
public class HelloWorld {
    /**
     * Authenticates with the service and shows how to create, list, and delete tables. It also shows how to create,
     * get, list, update and delete table entities.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        // First instantiate a client that will be used to call the service.
        TableServiceClient tableServiceClient = new TableServiceClientBuilder()
            .connectionString("<your-connection-string>")
            .buildClient();

        String firstTableName = "OfficeSupplies";
        String secondTableName = "Employees";

        // Let's create a couple tables using a connection string.
        tableServiceClient.createTable(firstTableName);
        System.out.printf("Created table with name: %s\n", firstTableName);

        tableServiceClient.createTable(secondTableName);
        System.out.printf("Created table with name: %s\n", secondTableName);

        // Now, let's list our existing tables and print their properties.
        PagedIterable<TableItem> tableItems = tableServiceClient.listTables();

        for (TableItem tableItem : tableItems) {
            System.out.printf("Retrieved table with name: %s\n", tableItem.getName());
        }

        // Let's delete the second table.
        tableServiceClient.deleteTable(secondTableName);
        System.out.printf("Deleted table with name: %s\n", secondTableName);

        // Now let's create a couple entities in the first table, for which a TableClient will be needed. A TableClient
        // can be obtained by using a builder like with TableServiceClient or via the getTableClient() method. For
        // the sake of simplicity, we will use the latter.
        String partitionKey = "markers";
        String firstEntityRowKey = "m001";
        String secondEntityRowKey = "m002";

        TableClient tableClient = tableServiceClient.getTableClient(firstTableName);
        TableEntity firstEntity = new TableEntity(partitionKey, firstEntityRowKey)
            .addProperty("Brand", "Crayola")
            .addProperty("Color", "Red");
        tableClient.createEntity(firstEntity);
        System.out.println("Created entity for red marker.");

        TableEntity secondEntity = new TableEntity(partitionKey, secondEntityRowKey)
            .addProperty("Brand", "Crayola")
            .addProperty("Color", "Blue");
        tableClient.createEntity(secondEntity);
        System.out.println("Created entity for blue marker.");

        // Let's list our entities.
        PagedIterable<TableEntity> tableEntities = tableClient.listEntities();

        for (TableEntity tableEntity : tableEntities) {
            System.out.println("Retrieved entity with the following properties:");

            Map<String, Object> properties = tableEntity.getProperties();

            for (Map.Entry<String, Object> property : properties.entrySet()) {
                System.out.printf("Name: %s, Value: %s\n", property.getKey(), property.getValue());
            }
        }

        // And then update one of them, for which we will first retrieve the entity.
        TableEntity retrievedEntity = tableClient.getEntity(partitionKey, firstEntityRowKey);
        System.out.printf("Retrieved entity with partition key: %s, and row key: %s\n", partitionKey,
            firstEntityRowKey);

        // The default for UpdateMode is UpdateMode.MERGE, which means the provided entity's properties will be merged
        // into the existing entity. If the entity does not exist in the table, then the operation will fail.
        tableClient.updateEntity(retrievedEntity);
        System.out.println("Updated entity");

        // Finally, let's delete the second entity we created.
        tableClient.deleteEntity(partitionKey, secondEntityRowKey);
    }
}
