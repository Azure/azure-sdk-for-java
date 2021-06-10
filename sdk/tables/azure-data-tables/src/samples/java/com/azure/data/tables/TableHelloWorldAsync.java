// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.data.tables.models.TableEntity;

import java.util.concurrent.CountDownLatch;

/**
 * This sample demonstrates how to create, update, upsert, get, list and delete table entities asynchronously on a
 * table.
 */
public class TableHelloWorldAsync {
    /**
     * Authenticates with the Table service and shows how to create, update, upsert, get, list and delete table entities
     * asynchronously.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) throws InterruptedException {
        /* Instantiate a TableAsyncClient that will be used to call the service. Notice that the client is using a
        connection string. For information on how to obtain a connection string, please refer to this project samples'
        README.*/
        TableAsyncClient tableAsyncClient = new TableClientBuilder()
            .connectionString("DefaultEndpointsProtocol=https;AccountName=dummyAccount;AccountKey=xyzDummy;EndpointSuffix=core.windows.net")
            .tableName("myTable")
            .buildAsyncClient();

        String partitionKey = "OfficeSupplies";
        String rowKey = "s001";

        TableEntity entityToCreate = new TableEntity(partitionKey, rowKey)
            .addProperty("Type", "Pen");

        TableEntity entityForUpdate = new TableEntity(partitionKey, rowKey)
            .addProperty("Type", "Pen")
            .addProperty("Color", "Red");

        String anotherRowKey = "s002";

        TableEntity entityForUpsert = new TableEntity(partitionKey, anotherRowKey)
            .addProperty("Type", "Marker")
            .addProperty("Color", "Blue");

        CountDownLatch latch = new CountDownLatch(1);

        // We'll first create the table this client will be associated with.
        tableAsyncClient.createTable()
            .map(tableItem -> System.out.printf("Created table with name '%s'.%n", tableItem.getName()))
            // We will then create an entity on the table.
            .then(tableAsyncClient.createEntity(entityToCreate))
            .map(unused ->
                System.out.printf("Created entity with partition key '%s' and row key '%s'.%n", partitionKey, rowKey))
            // Let's now update said entity on the table. By default, the provided entity will be merged with the one on
            // the table.
            .then(tableAsyncClient.updateEntity(entityForUpdate))
            .map(unused ->
                System.out.printf("Updated entity with partition key '%s' and row key '%s'.%n", partitionKey, rowKey))
            // Let's retrieve our updated entity to see how it looks now.
            .then(tableAsyncClient.getEntity(partitionKey, rowKey))
            .map(retrievedEntity ->
                System.out.printf("Retrieved entity with partition key '%s' and row key '%s'.%n",
                    retrievedEntity.getPartitionKey(), retrievedEntity.getRowKey()))
            // Let's now use the upsert command to update an entity (or insert it, if the entity does not exist on the
            // table already).
            .then(tableAsyncClient.upsertEntity(entityForUpsert))
            .map(unused ->
                System.out.printf("Upserted entity with partition key '%s' and row key '%s'.%n", partitionKey, rowKey))
            // Let's now retrieve all the entities on the table and print their partition and row keys.
            .thenMany(tableAsyncClient.listEntities())
            .map(currentEntity -> {
                System.out.printf("Retrieved entity with partition key '%s' and row key '%s'.%n",
                    currentEntity.getPartitionKey(), currentEntity.getRowKey());
                return currentEntity;
            })
            .filter(tableEntity ->
                tableEntity.getPartitionKey().equals(partitionKey) && tableEntity.getRowKey().equals(rowKey))
            // Finally, let's delete the entity we upserted above.
            .flatMap(tableAsyncClient::deleteEntity)
            .subscribe(
                ignored ->
                    System.out.printf("Deleted entity with partition key '%s' and row key '%s'.%n", partitionKey,
                        anotherRowKey),
                ex -> {
                    System.out.println("Got an error running the sample: " + ex.getMessage());
                    latch.countDown();
                },
                () -> {
                    System.out.println("Successfully completed running the sample.");
                    latch.countDown();
                });

        latch.await();
    }
}
