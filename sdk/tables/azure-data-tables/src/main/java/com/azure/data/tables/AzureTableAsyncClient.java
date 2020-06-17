package com.azure.data.tables;

import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

public class AzureTableAsyncClient {
    private final Set<String> existingTables = new HashSet<>();

    /**
     * Creates a table. This artificially takes 3 seconds.
     *
     * @param name The name of the table.
     * @return A Mono that completes when the table is created.
     */
    public Mono<AzureTable> createTable(String name) {
        return Mono.delay(Duration.ofSeconds(3)).flatMap(delay -> {
            if (existingTables.add(name)) {
                System.out.printf("CREATING TABLE '%s'.%n", name);
                final AzureTable table = new AzureTable(name);
                return Mono.just(table);
            } else {
                System.err.printf("TABLE '%s' ALREADY EXISTS.%n", name);
                return Mono.error(new IllegalArgumentException(
                    String.format("Table with name '%s' already exists.", name)));
            }
        });
    }
}
