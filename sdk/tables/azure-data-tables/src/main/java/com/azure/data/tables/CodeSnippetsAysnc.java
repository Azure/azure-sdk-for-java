package com.azure.data.tables;

import com.azure.core.exception.HttpResponseException;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;


public class CodeSnippetsAysnc {


    public void AsyncSnippets() {

        //client-builder pattern
        TableAsyncClientBuilder tableAsyncClientBuilder = new TableAsyncClientBuilder();
        TableAsyncClient tableAsyncClient = new TableAsyncClientBuilder()
            .connectionString("connectionString")
            .build();

        //add table
        Mono<Void> createTableMono = tableAsyncClient.createTable("tableName");

        createTableMono.subscribe(Void -> {
            System.out.println("Table creation successful.");
        }, error -> {
            System.out.println("There was an error creating the table. Error: " + error);
        });

        //delete a table
        Mono<Void> deleteTableMono = tableAsyncClient.deleteTable("tableName");

        deleteTableMono.subscribe(Void -> {
            System.out.println("Table deletion successful");
        }, error -> {
            System.out.println("There was an error deleting the table. Error: " + error);
        });


        //query tables
        String filterString = "$filter= name eq 'Office Supplies'";
        String selectString = "$select= Product, Price";
        Flux<String> queryTableFlux = tableAsyncClient.queryTables(selectString, filterString);
        Disposable subscription = queryTableFlux.subscribe(s -> {
            System.out.println(s);
        }, error -> {
            System.out.println("There was an error querying the table. Error: " + error);
        });
    }

    public void InsertEntity(){

        //client-builder pattern
        TableAsyncClientBuilder tableAsyncClientBuilder = new TableAsyncClientBuilder();
        TableAsyncClient tableAsyncClient = new TableAsyncClientBuilder()
            .connectionString("connectionString")
            .build();


        Mono<Void> createTableMono = tableAsyncClient.createTable("tableName");

        createTableMono.flatMap(Void -> {
            System.out.println("Table creation successful.");

            String tableName = "Office Supplies";
            String row = "crayola markers";
            String partitionKey = "markers";
            HashMap<String, Object> tableEntityProperties = new HashMap<>();
            Mono<TableEntity> insertEntityMono = tableAsyncClient.insertEntity(tableName, row, partitionKey, tableEntityProperties);
            return insertEntityMono;
        }).subscribe(tableEntity1 -> {
            System.out.println("Insert Entity Successful. Entity: " + tableEntity1);
        }, error -> {
            System.out.println("There was an error inserting the Entity. Error: " + error);
        });


    }

    public void DeleteEntity(){

        //client-builder pattern
        TableAsyncClientBuilder tableAsyncClientBuilder = new TableAsyncClientBuilder();
        TableAsyncClient tableAsyncClient = new TableAsyncClientBuilder()
            .connectionString("connectionString")
            .build();

        Mono<Void> createTableMono = tableAsyncClient.createTable("tableName");
        createTableMono.flatMap(Void -> {
            System.out.println("Table creation successful.");

            String tableName = "Office Supplies";
            String row = "crayola markers";
            String partitionKey = "markers";
            HashMap<String, Object> tableEntityProperties = new HashMap<>();
            Mono<TableEntity> insertEntityMono = tableAsyncClient.insertEntity(tableName, row, partitionKey, tableEntityProperties);
            return insertEntityMono;
        }).flatMap(tableEntity1 -> {
            System.out.println("Insert Entity Successful. Table Entity: " + tableEntity1);
            Mono<Void> deleteEntityMono = tableAsyncClient.deleteEntity(tableEntity1);
            return deleteEntityMono;
        }).subscribe(tableEntity1 -> {
            System.out.println("Delete Entity Successful. Entity: " + tableEntity1);
        }, error -> {
            System.out.println("There was an error deleting the Entity. Error: " + error);
        });


    }
    public void UpdateEntity(){

        //client-builder pattern
        TableAsyncClientBuilder tableAsyncClientBuilder = new TableAsyncClientBuilder();
        TableAsyncClient tableAsyncClient = new TableAsyncClientBuilder()
            .connectionString("connectionString")
            .build();

        Mono<Void> createTableMono = tableAsyncClient.createTable("tableName");
        createTableMono.flatMap(Void -> {
            System.out.println("Table creation successful.");

            String tableName = "Office Supplies";
            String row = "crayola markers";
            String partitionKey = "markers";
            HashMap<String, Object> tableEntityProperties = new HashMap<>();
            Mono<TableEntity> insertEntityMono = tableAsyncClient.insertEntity(tableName, row, partitionKey, tableEntityProperties);
            return insertEntityMono;
        }).flatMap(tableEntity1 -> {
            System.out.println("Insert Entity Successful. Table Entity: " + tableEntity1);

            tableEntity1.addProperty("Seller","Crayola");
            Mono<Void> updateEntityMono = tableAsyncClient.updateEntity(tableEntity1);
            return updateEntityMono;
        }).subscribe(Void -> {
            System.out.println("Update Entity Successful.");
        }, error -> {
            System.out.println("There was an error updating the Entity. Error: " + error);
        });


    }

    public void UpsertEntity(){

        //client-builder pattern
        TableAsyncClientBuilder tableAsyncClientBuilder = new TableAsyncClientBuilder();
        TableAsyncClient tableAsyncClient = new TableAsyncClientBuilder()
            .connectionString("connectionString")
            .build();


        Mono<Void> createTableMono = tableAsyncClient.createTable("tableName");
        createTableMono.flatMap(Void -> {
            System.out.println("Table creation successful.");

            String tableName = "Office Supplies";
            String row = "crayola markers";
            String partitionKey = "markers";
            HashMap<String, Object> tableEntityProperties = new HashMap<>();
            Mono<TableEntity> insertEntityMono = tableAsyncClient.insertEntity(tableName, row, partitionKey, tableEntityProperties);
            return insertEntityMono;
        }).flatMap(tableEntity1 -> {
            System.out.println("Insert Entity Successful. Table Entity: " + tableEntity1);

            tableEntity1.addProperty("Price","$5");
            Mono<TableEntity> upsertEntityMono = tableAsyncClient.upsertEntity(tableEntity1);
            return upsertEntityMono;
        }).subscribe(tableEntity1 -> {
            System.out.println("Upsert Entity Successful. Entity: " + tableEntity1);
        }, error -> {
            System.out.println("There was an error upserting the Entity. Error: " + error);
        });


    }

    public void QueryEntities(){

        //client-builder pattern
        TableAsyncClientBuilder tableAsyncClientBuilder = new TableAsyncClientBuilder();
        TableAsyncClient tableAsyncClient = new TableAsyncClientBuilder()
            .connectionString("connectionString")
            .build();


        Mono<Void> createTableMono = tableAsyncClient.createTable("tableName");
        createTableMono.then(Mono.fromCallable(()-> {
                System.out.println("Table creation successful.");

                String filterString2 = "$filter = Product eq 'markers'";
                String selectString2 = "$select = Seller eq 'crayola'";
                Flux<TableEntity> queryTableEntity = tableAsyncClient.queryEntity("tableName", filterString2, selectString2);
                return queryTableEntity;
            })).subscribe(tableEntity1 -> {
                System.out.println("Table Entity: " + tableEntity1);
            }, error -> {
                System.out.println("There was an error querying the table. Error: " + error);
            });
        }

    }
