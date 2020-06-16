package com.azure.data.tables;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;


public class CodeSnippetsAysnc {


    public void AsyncSnippets() {

        //build service client
        TableServiceAsyncClient tableServiceAsyncClient = new TableServiceAsyncClientBuilder()
            .connectionString("connectionString")
            .build();

        //add table
        tableServiceAsyncClient.createTable("OfficeSupplies").subscribe(Void -> {
            System.out.println("Table creation successful.");
        }, error -> {
            System.out.println("There was an error creating the table. Error: " + error);
        });


        //delete a table
        Mono<Void> deleteTableMono = tableServiceAsyncClient.deleteTable("OfficeSupplies");

        deleteTableMono.subscribe(Void -> {
            System.out.println("Table deletion successful");
        }, error -> {
            System.out.println("There was an error deleting the table. Error: " + error);
        });


        //query tables
        String selectString = "$select= TableName eq 'OfficeSupplies'";
        Flux<AzureTable> queryTableFlux = tableServiceAsyncClient.queryTables(selectString);
        queryTableFlux.subscribe(azureTable -> {
            System.out.println(azureTable.getName());
        }, error -> {
            System.out.println("There was an error querying the service. Error: " + error);
        });
    }

    public void InsertEntity() {

        //build service client
        TableServiceAsyncClient tableServiceAsyncClient = new TableServiceAsyncClientBuilder()
            .connectionString("connectionString")
            .build();

        TableAsyncClient tableAsyncClient = tableServiceAsyncClient.getClient("OfficeSupplies");


        String tableName = "OfficeSupplies";
        String row = "crayolaMarkers";
        String partitionKey = "markers";
        HashMap<String, Object> tableEntityProperties = new HashMap<>();
        Mono<TableEntity> insertEntityMono = tableAsyncClient.insertEntity(tableName, row, partitionKey,
            tableEntityProperties);
        insertEntityMono.subscribe(tableEntity -> {
            System.out.println("Insert Entity Successful. Entity: " + tableEntity);
        }, error -> {
            System.out.println("There was an error inserting the Entity. Error: " + error);
        });


    }

    public void DeleteEntity() {

        //build service client
        TableServiceAsyncClient tableServiceAsyncClient = new TableServiceAsyncClientBuilder()
            .connectionString("connectionString")
            .build();

        TableAsyncClient tableAsyncClient = tableServiceAsyncClient.getClient("OfficeSupplies");

        String selectString = "$select = RowKey eq 'crayolaMarkers'";
        Flux<TableEntity> queryTableEntity = tableAsyncClient.queryEntity("OfficeSupplies", selectString);

        queryTableEntity.flatMap(tableEntity -> {
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

        //build service client
        TableServiceAsyncClient tableServiceAsyncClient = new TableServiceAsyncClientBuilder()
            .connectionString("connectionString")
            .build();

        TableAsyncClient tableAsyncClient = tableServiceAsyncClient.getClient("OfficeSupplies");

        String selectString2 = "$select = RowKey eq 'crayolaMarkers'";
        Flux<TableEntity> queryTableEntity = tableAsyncClient.queryEntity("OfficeSupplies", selectString2);

        queryTableEntity.flatMap(tableEntity -> {
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

        //build service client
        TableServiceAsyncClient tableServiceAsyncClient = new TableServiceAsyncClientBuilder()
            .connectionString("connectionString")
            .build();

        TableAsyncClient tableAsyncClient = tableServiceAsyncClient.getClient("OfficeSupplies");

        String filterString2 = "$filter = price eq '5'";
        String selectString2 = "$select = PartitionKey eq 'markers'";
        Flux<TableEntity> queryTableEntity = tableAsyncClient.queryEntity("OfficeSupplies", filterString2, selectString2);

        queryTableEntity.subscribe(tableEntity -> {
            System.out.println("Table Entity: " + tableEntity);
        }, error -> {
            System.out.println("There was an error querying the table. Error: " + error);
        });

    }

}
