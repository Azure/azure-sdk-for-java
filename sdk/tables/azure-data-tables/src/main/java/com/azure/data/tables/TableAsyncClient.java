// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.annotation.ServiceClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@ServiceClient(
    builder = TableClientBuilder.class,
    isAsync = true)
public class TableAsyncClient {
    String tableName;

    TableAsyncClient(String tableName) {
        this.tableName = tableName;
    }

    public Flux<TableEntity> queryEntity(String az, String selectString, String filterString) {
        return null;
    }

    public Flux<TableEntity> queryEntity(String az, String filterString) {
        return null;
    }

    public Mono<TableEntity> insertEntity(String tableName, String row, String partition, Map<String, Object> tableEntityProperties) {
        return null;
    }

    public Mono<TableEntity> insertEntity(TableEntity te) {
        return null;
    }

    public Mono<Void> deleteEntity(TableEntity tableEntity) {
        return Mono.empty();
    }

    public Mono<Void> updateEntity(TableEntity te) {
        return Mono.empty();
    }

    public Mono<TableEntity> upsertEntity(TableEntity te) {
        return null;
    }

}
