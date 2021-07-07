// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables;

import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableTransactionAction;
import com.azure.data.tables.models.TableTransactionActionType;
import com.azure.data.tables.models.TableTransactionResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Sample that demonstrates how to create and submit a transactional batch of actions.
 */
public class TransactionalBatchOperation {
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

        // Now let's create a list to add transactional batch actions to.
        List<TableTransactionAction> transactionActions = new ArrayList<>();

        // Let's create a couple entities.
        String partitionKey = "markers4";
        String firstEntityRowKey = "m001";
        String secondEntityRowKey = "m002";

        TableEntity firstEntity = new TableEntity(partitionKey, firstEntityRowKey)
            .addProperty("Brand", "Crayola")
            .addProperty("Color", "Red");
        transactionActions.add(new TableTransactionAction(TableTransactionActionType.CREATE, firstEntity));
        System.out.printf("Added create action for entity with partition key '%s', and row key '%s'.%n", partitionKey,
            firstEntityRowKey);

        TableEntity secondEntity = new TableEntity(partitionKey, secondEntityRowKey)
            .addProperty("Brand", "Crayola")
            .addProperty("Color", "Blue");
        transactionActions.add(new TableTransactionAction(TableTransactionActionType.CREATE, secondEntity));

        System.out.printf("Added create action for entity with partition key '%s', and row key '%s'.%n", partitionKey,
            secondEntityRowKey);

        // Now let's update a different entity.
        String rowKeyForUpdate = "m003";
        TableEntity entityToUpdate = new TableEntity(partitionKey, rowKeyForUpdate)
            .addProperty("Brand", "Crayola")
            .addProperty("Color", "Blue");
        transactionActions.add(new TableTransactionAction(TableTransactionActionType.UPDATE_MERGE, entityToUpdate));

        System.out.printf("Added update action for entity with partition key '%s', and row key '%s'.%n", partitionKey,
            rowKeyForUpdate);

        // And delete another one.
        String rowKeyForDelete = "m004";
        TableEntity entityToDelete = new TableEntity(partitionKey, rowKeyForDelete)
            .addProperty("Brand", "Crayola")
            .addProperty("Color", "Blue");
        transactionActions.add(new TableTransactionAction(TableTransactionActionType.DELETE, entityToDelete));

        System.out.printf("Added delete action for entity with partition key '%s', and row key '%s'.%n", partitionKey,
            rowKeyForDelete);

        // Finally, let's submit the batch of operations and inspect the status codes for every action.
        TableTransactionResult tableTransactionResult = tableClient.submitTransaction(transactionActions);

        tableTransactionResult.getTransactionActionResponses().forEach(tableTransactionActionResponse ->
            System.out.printf("%n%d", tableTransactionActionResponse.getStatusCode()));
    }
}
