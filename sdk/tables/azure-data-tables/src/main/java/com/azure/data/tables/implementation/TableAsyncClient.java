package com.azure.data.tables.implementation;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public class TableAsyncClient {

    public TableAsyncClient(){

    }

    public Mono<Void> createTable(String name){ }

    public Mono<Void> createTableIfNotExist(String name) { }

    public Mono<Void> deleteTable(String name) { }

    public List<String> queryTables(String selectString, String filterString){
        return null;
    }

    public List<TableEntity> queryEntity(String az, String selectString, String filterString){
        return null;
    }

    public TableEntity insertEntity(String tableName, String row, String partition, Map<String, Object> tableEntityProperties){
        return new TableEntity();
    }
    public TableEntity insertEntity(TableEntity te){
        return te;
    }
    public Mono<Void> deleteEntity(String tableName, TableEntity tableEntity){ }

    public Mono<Void> updateEntity(TableEntity te){ }
    public TableEntity upsertEntity(TableEntity te){ return new TableEntity(); }

}
