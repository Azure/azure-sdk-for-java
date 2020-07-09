// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.util.logging.ClientLogger;
import java.util.HashMap;
import java.util.Map;
import reactor.core.publisher.Mono;

/**
 * async code snippets for the table service
 */
public class TableServiceAsyncClientCodeSnippets {


    /**
     * create table code snippet
     */
    public void createTable() {
        TableServiceAsyncClient tableServiceAsyncClient = new TableServiceClientBuilder()
            .connectionString("connectionString")
            .buildAsyncClient();

        tableServiceAsyncClient.createTable("OfficeSupplies").subscribe(
            Void -> {},
            error -> System.err.println("There was an error creating the table. Error: " + error),
            () -> System.out.println("Table creation successful."));
    }

    /**
     * delete table code snippet
     */
    public void deleteTable() {
        TableServiceAsyncClient tableServiceAsyncClient = new TableServiceClientBuilder()
            .connectionString("connectionString")
            .buildAsyncClient();

        tableServiceAsyncClient.deleteTable("OfficeSupplies").subscribe(
            Void -> {},
            error -> System.err.println("There was an error deleting the table. Error: " + error),
            () -> System.out.println("Table deletion successful."));
    }

    /**
     * query tables code snippet
     */
    public void queryTable() {
        TableServiceAsyncClient tableServiceAsyncClient = new TableServiceClientBuilder()
            .connectionString("connectionString")
            .buildAsyncClient();
        QueryOptions queryOptions = new QueryOptions().setFilter("TableName eq OfficeSupplies");

        tableServiceAsyncClient.queryTables(queryOptions).subscribe(azureTable -> {
            System.out.println(azureTable.getName());
        }, error -> {
            System.err.println("There was an error querying the service. Error: " + error);
        });
    }

    /**
     * insert entity code snippet
     */
    private void insertEntity() {
        TableServiceAsyncClient tableServiceAsyncClient = new TableServiceClientBuilder()
            .connectionString("connectionString")
            .buildAsyncClient();

        TableAsyncClient tableAsyncClient = tableServiceAsyncClient.getTableAsyncClient("OfficeSupplies");
        Map<String, Object> properties = new HashMap<>();
        properties.put("RowKey", "crayolaMarkers");
        properties.put("PartitionKey", "markers");

        tableAsyncClient.createEntity(properties).subscribe(tableEntity -> {
            System.out.println("Insert Entity Successful. Entity: " + tableEntity);
        }, error -> {
            System.err.println("There was an error inserting the Entity. Error: " + error);
        });
    }

    /**
     * delete entity code snippet
     */
    private void deleteEntity() {
        TableServiceAsyncClient tableServiceAsyncClient = new TableServiceClientBuilder()
            .connectionString("connectionString")
            .buildAsyncClient();

        TableAsyncClient tableAsyncClient = tableServiceAsyncClient.getTableAsyncClient("OfficeSupplies");
        String rowKey = "crayolaMarkers";
        String partitionKey = "markers";

        tableAsyncClient.get(rowKey, partitionKey).flatMap(tableEntity -> {
            System.out.println("Table Entity: " + tableEntity);
            return tableAsyncClient.deleteEntity(tableEntity);
        }).subscribe(
            Void -> {},
            error -> System.err.println("There was an error deleting the Entity. Error: " + error),
            () -> System.out.println("Delete Entity Successful."));
    }

    /**
     * upsert entity code snippet
     */
    private void upsert() {
        TableServiceAsyncClient tableServiceAsyncClient = new TableServiceClientBuilder()
            .connectionString("connectionString")
            .buildAsyncClient();

        TableAsyncClient tableAsyncClient = tableServiceAsyncClient.getTableAsyncClient("OfficeSupplies");
        String rowKey = "crayolaMarkers";
        String partitionKey = "markers";

        tableAsyncClient.get(rowKey, partitionKey).flatMap(tableEntity -> {
            System.out.println("Table Entity: " + tableEntity);
            tableEntity.addProperty("Price", "5");
            Mono<Void> updateEntityMono = tableAsyncClient.upsertEntity(UpdateMode.MERGE, tableEntity);
            return updateEntityMono;
        }).subscribe(
            Void -> {},
            error -> System.err.println("There was an error upserting the Entity. Error: " + error),
            () -> System.out.println("Upsert Entity Successful."));
    }

    /**
     * update entity code snippet
     */
    private void update() {
        TableServiceAsyncClient tableServiceAsyncClient = new TableServiceClientBuilder()
            .connectionString("connectionString")
            .buildAsyncClient();

        TableAsyncClient tableAsyncClient = tableServiceAsyncClient.getTableAsyncClient("OfficeSupplies");
        String rowKey = "crayolaMarkers";
        String partitionKey = "markers";

        tableAsyncClient.get(rowKey, partitionKey).flatMap(tableEntity -> {
            System.out.println("Table Entity: " + tableEntity);
            tableEntity.addProperty("Price", "5");
            Mono<Void> updateEntityMono = tableAsyncClient.updateEntity(UpdateMode.REPLACE, tableEntity);
            return updateEntityMono;
        }).subscribe(
            Void -> {},
            error -> System.err.println("There was an error updating the Entity. Error: " + error),
            () -> System.out.println("Update Entity Successful."));
    }

    /**
     * query entity code snippet
     */
    private void queryEntities() {
        TableServiceAsyncClient tableServiceAsyncClient = new TableServiceClientBuilder()
            .connectionString("connectionString")
            .buildAsyncClient();

        TableAsyncClient tableAsyncClient = tableServiceAsyncClient.getTableAsyncClient("OfficeSupplies");
        QueryOptions queryOptions = new QueryOptions()
            .setFilter("Product eq markers")
            .setSelect("Seller, Price");

        tableAsyncClient.queryEntities(queryOptions).subscribe(tableEntity -> {
            System.out.println("Table Entity: " + tableEntity);
        }, error -> {
            System.err.println("There was an error querying the table. Error: " + error);
        });
    }

    /**
     * checks to see if an entity exists code snippet
     */
    private void existsEntity() {
        TableServiceAsyncClient tableServiceAsyncClient = new TableServiceClientBuilder()
            .connectionString("connectionString")
            .buildAsyncClient();

        TableAsyncClient tableAsyncClient = tableServiceAsyncClient.getTableAsyncClient("OfficeSupplies");

        tableAsyncClient.get("crayolaMarkers", "markers")
            .subscribe(tableEntity -> {
            System.out.println("Table Entity exists: " + tableEntity);
        }, error -> {
            System.err.println("There was an error getting the entity. Error: " + error);
        });
    }
}
