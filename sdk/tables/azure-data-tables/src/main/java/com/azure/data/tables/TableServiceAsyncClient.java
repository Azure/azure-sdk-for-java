// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.annotation.ServiceClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ServiceClient(
    builder = TableServiceClientBuilder.class,
    isAsync = true)
public class TableServiceAsyncClient {

    TableServiceAsyncClient() {
    }

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
