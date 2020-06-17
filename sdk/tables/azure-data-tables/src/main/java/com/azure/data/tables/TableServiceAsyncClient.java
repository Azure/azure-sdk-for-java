package com.azure.data.tables;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class TableServiceAsyncClient {

    public TableServiceAsyncClient() {
    }
    //public Mono<Void> createTable(String name){return  Mono.empty(); }

    public Mono<TableAsyncClient> createTable(String name) {
        return null;
    }

    public Mono<Void> createTableIfNotExist(String name) {
        return Mono.empty();
    }

    public Mono<Void> deleteTable(String name) {
        return Mono.empty();
    }

    public Flux<AzureTable> queryTables(String filterString) {
        return null;
    }

    public Mono<TableAsyncClient> getTable(String tableName) {
        return null;
    }

    public TableAsyncClient getClient(String tableName) {
        return null;
    }

}
