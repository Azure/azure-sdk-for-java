package com.azure.data.tables;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public class  TableAsyncServiceClient{

    public TableAsyncServiceClient(){ }
    public Mono<Void> createTable(String name){return  Mono.empty(); }

    public Mono<Void> createTableIfNotExist(String name) {return  Mono.empty(); }

    public Mono<Void> deleteTable(String name) {return  Mono.empty(); }

    public Flux<String> queryTables(String filterString){
        return null;
    }

}
