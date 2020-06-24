// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

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
        String filterString = "$filter=TableName eq 'OfficeSupplies'";
        tableServiceAsyncClient.queryTables(null, null, filterString).subscribe(azureTable -> {
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
        String row = "crayolaMarkers";
        String partitionKey = "markers";
        HashMap<String, Object> tableEntityProperties = new HashMap<>();

        tableAsyncClient.insertEntity(row, partitionKey, tableEntityProperties).subscribe(tableEntity -> {
            System.out.println("Insert Entity Successful. Entity: " + tableEntity);
        }, error -> {
            System.out.println("There was an error inserting the Entity. Error: " + error);
        });


    }

    public void deleteEntity() {

        // Build service client
        TableServiceAsyncClient tableServiceAsyncClient = new TableServiceClientBuilder()
            .connectionString("connectionString")
            .buildAsyncClient();

        TableAsyncClient tableAsyncClient = tableServiceAsyncClient.getClient("OfficeSupplies");
        String filterString = "$filter=RowKey eq 'crayolaMarkers'";

        tableAsyncClient.queryEntity(null, null, filterString).flatMap(tableEntity -> {
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
        String filterString2 = "$filter=RowKey eq 'crayolaMarkers'";

        tableAsyncClient.queryEntity(null, null, filterString2).flatMap(tableEntity -> {
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
        String filterString2 = "$filter=Product eq 'markers'";
        String selectString2 = "$select=Seller, Price";

        tableAsyncClient.queryEntity(null, selectString2, filterString2).subscribe(tableEntity -> {
            System.out.println("Table Entity: " + tableEntity);
        }, error -> {
            System.out.println("There was an error querying the table. Error: " + error);
        });

    }

}
