package com.azure.data.tables.implementation;

import reactor.core.publisher.Mono;
import reactor.core.*;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class CodeSnippetsAysnc {

    //client-builder pattern
    TableAsyncClientBuilder tableAsyncClientBuilder = new TableAsyncClientBuilder();
    TableAsyncClient tableAsyncClient = new TableAsyncClientBuilder()
        .connectionString("connectionString")
        .build();

    //add table
    Mono<Void> createTableMono = tableAsyncClient.createTable("tableName");

    createTableMono.subscribe(void -> {
        System.out.println("Table creation successful");
    }, error -> {
        System.out.println("There was an error creating the table. Error: " + error);
    });

}
