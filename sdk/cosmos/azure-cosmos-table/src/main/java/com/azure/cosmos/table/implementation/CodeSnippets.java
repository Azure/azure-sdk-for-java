package com.azure.cosmos.table.implementation;

import com.azure.cosmos.table.implementation.models.TableEntityQueryResponse;

import javax.swing.*;
import java.util.Observable;

public class CodeSnippets {
    //add a table

    //sync
    TableClient tableClientBuilder = new TableClientBuilder();
        .endpoint(foo)
        .credential(new DefaultAzureCredential()
        .build();

     .connectionString

    AzureTable azureTable = tableClientBuilder.createTableIfNotExists("tableName");

    //aysnc
    TableAysncClientBuilder tableAysncClient = new TableAsyncClientBuilder();
        .pipeline("pipline")
        .build();
     AzureTable  azureTable2;
        Mono<AzureTable> createTableMono = tableAysncClient.createTable("tableName");
        createTableMono
            .subscribe(
                response -> {
                    azureTable2 =
                },
                error -> {

                }
            )


    //delete a table
    //sync
    azureTable.delete();

    //async



    //query a table

    //sync
    TableEntityQueryResponse tableEntityQueryResponse = azureTable.query(Filter);


    //aysnc
}
