// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables;

import com.azure.data.tables.models.BatchOperationResponse;
import com.azure.data.tables.models.TableEntity;

import java.util.List;

/**
 * Sample that demonstrates how to create and submit a batch of operations.
 */
public class TableBatchOperations {
    /**
     * Authenticates with the service and shows how to create and submit a batch of operations.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service and interact with a given table.
        TableClient tableClient = new TableClientBuilder()
            .tableName("OfficeSupplies")
            .connectionString("<your-connection-string>")
            .buildClient();

        // Now let's create a batch to add operations to.
        String partitionKey = "markers4";
        TableBatch tableBatch = tableClient.createBatch(partitionKey);

        // Let's create a couple entities.
        String firstEntityRowKey = "m001";
        String secondEntityRowKey = "m002";

        TableEntity firstEntity = new TableEntity(partitionKey, firstEntityRowKey)
            .addProperty("Brand", "Crayola")
            .addProperty("Color", "Red");
        tableBatch.createEntity(firstEntity);
        System.out.printf("Added create operation to batch for entity with partition key: %s, and row key: %s\n",
            partitionKey, firstEntityRowKey);

        TableEntity secondEntity = new TableEntity(partitionKey, secondEntityRowKey)
            .addProperty("Brand", "Crayola")
            .addProperty("Color", "Blue");
        tableBatch.createEntity(secondEntity);
        System.out.printf("Added create operation to batch for entity with partition key: %s, and row key: %s\n",
            partitionKey, secondEntityRowKey);

        // Now let's update a different entity.
        String rowKeyForUpdate = "m003";
        TableEntity entityToUpdate = new TableEntity(partitionKey, rowKeyForUpdate)
            .addProperty("Brand", "Crayola")
            .addProperty("Color", "Blue");
        tableBatch.updateEntity(entityToUpdate);
        System.out.printf("Added update operation to batch for entity with partition key: %s, and row key: %s\n",
            partitionKey, rowKeyForUpdate);

        // And delete another one.
        String rowKeyForDelete = "m004";
        TableEntity entityToDelete = new TableEntity(partitionKey, rowKeyForDelete)
            .addProperty("Brand", "Crayola")
            .addProperty("Color", "Blue");
        tableBatch.updateEntity(entityToDelete);
        System.out.printf("Added delete operation to batch for entity with partition key: %s, and row key: %s\n",
            partitionKey, rowKeyForDelete);

        // Finally, let's submit the batch of operations and inspect all the responses.
        List<BatchOperationResponse> batchOperationResponses = tableBatch.submitTransaction();
    }
}
