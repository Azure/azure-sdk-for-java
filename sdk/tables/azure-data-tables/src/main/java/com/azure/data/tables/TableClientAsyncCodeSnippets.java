// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;


public class TableClientAsyncCodeSnippets {


    public void AsyncSnippets() {

        // Build service client
        TableServiceAsyncClient tableServiceAsyncClient = new TableServiceClientBuilder()
            .connectionString("connectionString")
            .buildAsyncClient();

        // Add a table
        tableServiceAsyncClient.createTable("OfficeSupplies").subscribe(Void -> {
            System.out.println("Table creation successful.");
        }, error -> {
            System.out.println("There was an error creating the table. Error: " + error);
        });


        // Delete a table
        tableServiceAsyncClient.deleteTable("OfficeSupplies").subscribe(Void -> {
            System.out.println("Table deletion successful");
        }, error -> {
            System.out.println("There was an error deleting the table. Error: " + error);
        });


        // Query tables
        String selectString = "$select= TableName eq 'OfficeSupplies'";
        tableServiceAsyncClient.queryTables(selectString).subscribe(azureTable -> {
            System.out.println(azureTable.getName());
        }, error -> {
            System.out.println("There was an error querying the service. Error: " + error);
        });
    }

    public void InsertEntity() {

        // Build service client
        TableServiceAsyncClient tableServiceAsyncClient = new TableServiceClientBuilder()
            .connectionString("connectionString")
            .buildAsyncClient();

        TableAsyncClient tableAsyncClient = tableServiceAsyncClient.getClient("OfficeSupplies");
        String tableName = "OfficeSupplies";
        String row = "crayolaMarkers";
        String partitionKey = "markers";
        HashMap<String, Object> tableEntityProperties = new HashMap<>();

        tableAsyncClient.insertEntity(tableName, row, partitionKey,
            tableEntityProperties).subscribe(tableEntity -> {
            System.out.println("Insert Entity Successful. Entity: " + tableEntity);
        }, error -> {
            System.out.println("There was an error inserting the Entity. Error: " + error);
        });


    }

    public void DeleteEntity() {

        // Build service client
        TableServiceAsyncClient tableServiceAsyncClient = new TableServiceClientBuilder()
            .connectionString("connectionString")
            .buildAsyncClient();

        TableAsyncClient tableAsyncClient = tableServiceAsyncClient.getClient("OfficeSupplies");
        String selectString = "$select = RowKey eq 'crayolaMarkers'";

        tableAsyncClient.queryEntity("OfficeSupplies", selectString).flatMap(tableEntity -> {
            System.out.println("Table Entity: " + tableEntity);
            Mono<Void> deleteEntityMono = tableAsyncClient.deleteEntity(tableEntity);
            return deleteEntityMono;
        }).subscribe(Void -> {
            System.out.println("Delete Entity Successful.");
        }, error -> {
            System.out.println("There was an error deleting the Entity. Error: " + error);
        });
    }

    public void UpdateEntity() {

        // Build service client
        TableServiceAsyncClient tableServiceAsyncClient = new TableServiceClientBuilder()
            .connectionString("connectionString")
            .buildAsyncClient();

        TableAsyncClient tableAsyncClient = tableServiceAsyncClient.getClient("OfficeSupplies");
        String selectString2 = "$select = RowKey eq 'crayolaMarkers'";

        tableAsyncClient.queryEntity("OfficeSupplies", selectString2).flatMap(tableEntity -> {
            System.out.println("Table Entity: " + tableEntity);
            tableEntity.addProperty("Price", "5");
            Mono<Void> updateEntityMono = tableAsyncClient.updateEntity(tableEntity);
            return updateEntityMono;
        }).subscribe(Void -> {
            System.out.println("Update Entity Successful.");
        }, error -> {
            System.out.println("There was an error updating the Entity. Error: " + error);
        });


    }

    public void QueryEntities() {

        // Build service client
        TableServiceAsyncClient tableServiceAsyncClient = new TableServiceClientBuilder()
            .connectionString("connectionString")
            .buildAsyncClient();

        TableAsyncClient tableAsyncClient = tableServiceAsyncClient.getClient("OfficeSupplies");
        String filterString2 = "$filter = price eq '5'";
        String selectString2 = "$select = PartitionKey eq 'markers'";

        tableAsyncClient.queryEntity("OfficeSupplies", filterString2, selectString2).subscribe(tableEntity -> {
            System.out.println("Table Entity: " + tableEntity);
        }, error -> {
            System.out.println("There was an error querying the table. Error: " + error);
        });

    }

}
