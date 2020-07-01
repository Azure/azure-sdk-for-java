// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

/**
 * async code snippets for the table service
 */
public class TableServiceAsyncClientCodeSnippets {
    final ClientLogger logger = new ClientLogger("TableServiceAsyncClientCodeSnippets");

    /**
     * all methods on tables in the Tables SDK (Add, Delete, Query)
     */
    public void TableLevelMethods() {

        // Build service client
        TableServiceAsyncClient tableServiceAsyncClient = new TableServiceClientBuilder()
            .connectionString("connectionString")
            .buildAsyncClient();

        // Add a table
        tableServiceAsyncClient.createTable("OfficeSupplies").subscribe(Void -> {
            logger.info("Table creation successful.");
        }, error -> {
            logger.error("There was an error creating the table. Error: " + error);
        });


        // Delete a table
        tableServiceAsyncClient.deleteTable("OfficeSupplies").subscribe(Void -> {
            logger.info("Table deletion successful");
        }, error -> {
            logger.error("There was an error deleting the table. Error: " + error);
        });


        // Query tables
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.setFilter("TableName eq OfficeSupplies");
        tableServiceAsyncClient.queryTables(queryOptions).subscribe(azureTable -> {
            logger.info(azureTable.getName());
        }, error -> {
            logger.error("There was an error querying the service. Error: " + error);
        });
    }

    /**
     * insert entity code snippet
     */
    private void insertEntity() {

        // Build service client
        TableServiceAsyncClient tableServiceAsyncClient = new TableServiceClientBuilder()
            .connectionString("connectionString")
            .buildAsyncClient();

        TableAsyncClient tableAsyncClient = tableServiceAsyncClient.getClient("OfficeSupplies");
        String row = "crayolaMarkers";
        String partitionKey = "markers";

        tableAsyncClient.insertEntity(new TableEntity(row, partitionKey, null)).subscribe(tableEntity -> {
            logger.info("Insert Entity Successful. Entity: " + tableEntity);
        }, error -> {
            logger.error("There was an error inserting the Entity. Error: " + error);
        });
    }

    /**
     * delete entity code snippet
     */
    private void deleteEntity() {

        // Build service client
        TableServiceAsyncClient tableServiceAsyncClient = new TableServiceClientBuilder()
            .connectionString("connectionString")
            .buildAsyncClient();

        TableAsyncClient tableAsyncClient = tableServiceAsyncClient.getClient("OfficeSupplies");
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.setFilter("RowKey eq crayolaMarkers");

        tableAsyncClient.queryEntity(queryOptions).flatMap(tableEntity -> {
            logger.info("Table Entity: " + tableEntity);
            Mono<Void> deleteEntityMono = tableAsyncClient.deleteEntity(tableEntity);
            return deleteEntityMono;
        }).subscribe(Void -> {
            logger.info("Delete Entity Successful.");
        }, error -> {
            logger.error("There was an error deleting the Entity. Error: " + error);
        });
    }

    /**
     * update entity code snippet
     */
    private void updateEntity() {

        // Build service client
        TableServiceAsyncClient tableServiceAsyncClient = new TableServiceClientBuilder()
            .connectionString("connectionString")
            .buildAsyncClient();

        TableAsyncClient tableAsyncClient = tableServiceAsyncClient.getClient("OfficeSupplies");
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.setFilter("RowKey eq crayolaMarkers");

        tableAsyncClient.queryEntity(queryOptions).flatMap(tableEntity -> {
            logger.info("Table Entity: " + tableEntity);
            tableEntity.addProperty("Price", "5");
            Mono<Void> updateEntityMono = tableAsyncClient.updateEntity(tableEntity);
            return updateEntityMono;
        }).subscribe(Void -> {
            logger.info("Update Entity Successful.");
        }, error -> {
            logger.error("There was an error updating the Entity. Error: " + error);
        });
    }

    /**
     * query entity code snippet
     */
    private void queryEntities() {

        // Build service client
        TableServiceAsyncClient tableServiceAsyncClient = new TableServiceClientBuilder()
            .connectionString("connectionString")
            .buildAsyncClient();

        TableAsyncClient tableAsyncClient = tableServiceAsyncClient.getClient("OfficeSupplies");
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.setFilter("Product eq markers");
        queryOptions.setSelect("Seller, Price");

        tableAsyncClient.queryEntity(queryOptions).subscribe(tableEntity -> {
            logger.info("Table Entity: " + tableEntity);
        }, error -> {
            logger.error("There was an error querying the table. Error: " + error);
        });
    }

}
