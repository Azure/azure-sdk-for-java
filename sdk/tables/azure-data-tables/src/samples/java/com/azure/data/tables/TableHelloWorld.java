// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.http.rest.PagedIterable;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableItem;

/**
 * This sample demonstrates how to create, update, upsert, get, list and delete table entities synchronously on a table.
 */
public class TableHelloWorld {
    /**
     * Authenticates with the Table service and shows how to create, update, upsert, get, list and delete table entities
     * synchronously.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        /* Instantiate a TableClient that will be used to call the service. Notice that the client is using a
        connection string. For information on how to obtain a connection string, please refer to this project samples'
        README.*/
        TableClient tableClient = new TableClientBuilder()
            .connectionString("DefaultEndpointsProtocol=https;AccountName=dummyAccount;AccountKey=xyzDummy;EndpointSuffix=core.windows.net")
            .tableName("myTable")
            .buildClient();

        // We'll first create the table this client will be associated with.
        TableItem tableItem = tableClient.createTable();

        System.out.printf("Created table with name '%s'.%n", tableItem.getName());

        // We will then create an entity on the table.
        String partitionKey = "OfficeSupplies";
        String rowKey = "s001";
        TableEntity entityToCreate = new TableEntity(partitionKey, rowKey)
            .addProperty("Type", "Pen");

        tableClient.createEntity(entityToCreate);

        System.out.printf("Created entity with partition key '%s' and row key '%s'.%n", partitionKey, rowKey);

        // Let's now update said entity on the table. By default, the provided entity will be merged with the one on
        // the table.
        TableEntity entityForUpdate = new TableEntity(partitionKey, rowKey)
            .addProperty("Type", "Pen")
            .addProperty("Color", "Red");

        tableClient.updateEntity(entityForUpdate);

        System.out.printf("Updated entity with partition key '%s' and row key '%s'.%n", partitionKey, rowKey);

        // Let's retrieve our updated entity to see how it looks now.
        TableEntity retrievedEntity = tableClient.getEntity(partitionKey, rowKey);

        System.out.printf("Retrieved entity with partition key '%s' and row key '%s'.%n",
            retrievedEntity.getPartitionKey(), retrievedEntity.getRowKey());

        // Let's now use the upsert command to update an entity (or insert it, if the entity does not exist on the
        // table already).
        String anotherRowKey = "s002";

        TableEntity entityForUpsert = new TableEntity(partitionKey, anotherRowKey)
            .addProperty("Type", "Marker")
            .addProperty("Color", "Blue");

        tableClient.updateEntity(entityForUpsert);

        System.out.printf("Upserted entity with partition key '%s' and row key '%s'.%n", partitionKey, rowKey);

        // Let's now retrieve all the entities on the table and print their partition and row keys.
        PagedIterable<TableEntity> entities = tableClient.listEntities();

        entities.forEach(entity ->
            System.out.printf("Retrieved entity with partition key '%s' and row key '%s'.%n", entity.getPartitionKey(),
                entity.getRowKey()));

        // Finally, let's delete the entity we upserted above.
        tableClient.deleteEntity(partitionKey, anotherRowKey);

        System.out.printf("Deleted entity with partition key '%s' and row key '%s'.%n", partitionKey, anotherRowKey);
    }
}
