package com.azure.data.tables;

import reactor.core.publisher.Mono;

import java.time.Duration;

public class AzureTable {
    private final String name;

    AzureTable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Mono<AzureTableEntity> createEntity(String key, Object value) {
        AzureTableEntity azureTableEntity = new AzureTableEntity(key, value);
        System.out.println("Creating entity with key: " +  key + ". Value: " + value);
        return Mono.delay(Duration.ofSeconds(3)).thenReturn(azureTableEntity);
    }
}
