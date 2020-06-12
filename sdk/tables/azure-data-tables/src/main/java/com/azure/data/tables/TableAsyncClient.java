package com.azure.data.tables;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public class TableAsyncClient {

    public TableAsyncClient(){

    }

    public Mono<Void> createTable(String name){return  Mono.empty(); }

    public Mono<Void> createTableIfNotExist(String name) {return  Mono.empty(); }

    public Mono<Void> deleteTable(String name) {return  Mono.empty(); }

    public Flux<String> queryTables(String selectString, String filterString){
        return null;
    }

    public Flux<TableEntity> queryEntity(String az, String selectString, String filterString){
        return null;
    }

    public Mono<TableEntity> insertEntity(String tableName, String row, String partition, Map<String, Object> tableEntityProperties){
        return null;
    }
    public  Mono<TableEntity> insertEntity(TableEntity te){
        return null;
    }
    public Mono<Void> deleteEntity(TableEntity tableEntity){return  Mono.empty(); }

    public Mono<Void> updateEntity(TableEntity te){ return  Mono.empty(); }
    public Mono<TableEntity> upsertEntity(TableEntity te){ return null; }

}
