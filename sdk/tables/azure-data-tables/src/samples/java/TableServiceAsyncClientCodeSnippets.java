// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.data.tables.models.ListEntitiesOptions;
import com.azure.data.tables.models.ListTablesOptions;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.UpdateMode;

/**
 * async code snippets for the table service
 */
public class TableServiceAsyncClientCodeSnippets {


    /**
     * create table code snippet
    **/
    public void createTable() {
        TableServiceAsyncClient tableServiceAsyncClient = new TableServiceClientBuilder()
            .connectionString("connectionString")
            .buildAsyncClient();

        tableServiceAsyncClient.createTable("OfficeSupplies").subscribe(
            Void -> { },
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
            Void -> { },
            error -> System.err.println("There was an error deleting the table. Error: " + error),
            () -> System.out.println("Table deletion successful."));
    }

    /**
     * query tables code snippet
     */
    public void listTable() {
        TableServiceAsyncClient tableServiceAsyncClient = new TableServiceClientBuilder()
            .connectionString("connectionString")
            .buildAsyncClient();
        ListTablesOptions options = new ListTablesOptions().setFilter("TableName eq OfficeSupplies");

        tableServiceAsyncClient.listTables(options).subscribe(azureTable -> {
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

        TableAsyncClient tableAsyncClient = tableServiceAsyncClient.getTableClient("OfficeSupplies");
        TableEntity entity = new TableEntity("markers", "crayolaMarkers");

        tableAsyncClient.createEntity(entity).subscribe(tableEntity -> {
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

        TableAsyncClient tableAsyncClient = tableServiceAsyncClient.getTableClient("OfficeSupplies");
        String partitionKey = "markers";
        String rowKey = "crayolaMarkers";

        tableAsyncClient.getEntity(partitionKey, rowKey).flatMap(tableEntity -> {
            System.out.println("Table Entity: " + tableEntity);

            //delete entity without an eTag param will perform an unconditional delete
            //(using "*" as match condition in request)
            return tableAsyncClient.deleteEntity(partitionKey, rowKey);
        }).subscribe(
            Void -> { },
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

        TableAsyncClient tableAsyncClient = tableServiceAsyncClient.getTableClient("OfficeSupplies");
        String partitionKey = "markers";
        String rowKey = "crayolaMarkers";

        tableAsyncClient.getEntity(partitionKey, rowKey).flatMap(tableEntity -> {
            System.out.println("Table Entity: " + tableEntity);
            tableEntity.getProperties().put("Price", "5");

            //default is for UpdateMode is UpdateMode.MERGE, which means it merges if exists; inserts if not
            //ifUnchanged being true means the eTags must match to upsert
            return tableAsyncClient.upsertEntity(tableEntity);
        }).subscribe(
            Void -> { },
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

        TableAsyncClient tableAsyncClient = tableServiceAsyncClient.getTableClient("OfficeSupplies");
        String partitionKey = "markers";
        String rowKey = "crayolaMarkers";

        tableAsyncClient.getEntity(partitionKey, rowKey).flatMap(tableEntity -> {
            System.out.println("Table Entity: " + tableEntity);
            tableEntity.getProperties().put("Price", "5");

            //UpdateMode.REPLACE: so the entity will be replaced if it exists or the request fails if not found
            //ifUnchanged being false means the eTags must not match
            return tableAsyncClient.updateEntity(tableEntity, false, UpdateMode.REPLACE);
        }).subscribe(
            Void -> { },
            error -> System.err.println("There was an error updating the Entity. Error: " + error),
            () -> System.out.println("Update Entity Successful."));
    }

    /**
     * query entity code snippet
     */
    private void listEntities() {
        TableServiceAsyncClient tableServiceAsyncClient = new TableServiceClientBuilder()
            .connectionString("connectionString")
            .buildAsyncClient();

        TableAsyncClient tableAsyncClient = tableServiceAsyncClient.getTableClient("OfficeSupplies");
        ListEntitiesOptions options = new ListEntitiesOptions()
            .setFilter("Product eq markers")
            .setSelect("Seller, Price");

        tableAsyncClient.listEntities(options).subscribe(tableEntity -> {
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

        TableAsyncClient tableAsyncClient = tableServiceAsyncClient.getTableClient("OfficeSupplies");

        tableAsyncClient.getEntity("crayolaMarkers", "markers")
            .subscribe(tableEntity -> {
                System.out.println("Table Entity exists: " + tableEntity);
            }, error -> {
                    System.err.println("There was an error getting the entity. Error: " + error);
                });
    }
}
