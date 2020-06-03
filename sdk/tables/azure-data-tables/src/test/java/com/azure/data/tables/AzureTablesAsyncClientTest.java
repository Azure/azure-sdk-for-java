package com.azure.data.tables;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class AzureTablesAsyncClientTest {
    @Test
    void createTableTest() {
        // Ideally, we'd use a builder to create this rather than a constructor.
        AzureTableAsyncClient client = new AzureTableAsyncClient();

        // Every time this variable is used in a downstream subscriber, it will invoke the `createTable` operation.
        Mono<AzureTable> createTableMono = client.createTable("my-table");

        // This is a subscription to the Mono. `subscribe` is a non-blocking call.
        createTableMono.subscribe(table -> {
            System.out.println("Table created is: " + table.getName());
        }, error -> {
            System.err.println("There was an error creating the table. " + error);
        });

        // Since .subscribe is a non-blocking call, after it hooks up the asynchronous operation, it will move
        // onto the next statement. If `client.createTable` takes 30 seconds, the program would have ended before we
        // ever know the response because it will leave this method.
        // You'll see that there is no "System.out.println" console output because the program has ended.
    }

    /**
     * Notice how "CREATING TABLE with name: my-table" errors when we try to add another entrythe second time? This is
     * because, every time we chain the `createTableMono`, it invokes that `createTable` operation again.
     * After an error occurs in one of the operations upstream in the chain, it will call the (error) -> { } handler.
     * You'll see that we didn't try to add another entity (ie. new-key-2).
     *
     * See {@link #createAndUpdateTableFixed()} for a resolved version.
     */
    @Test
    void createAndUpdateTable() throws InterruptedException {
        AzureTableAsyncClient client = new AzureTableAsyncClient();

        // Every time this variable is used in a downstream subscriber, it will invoke the `createTable` operation.
        Mono<AzureTable> createTableMono = client.createTable("my-table");

        // FirstMono -> SecondFlatMap -> Map -> Subscribe
        createTableMono.flatMap(azureTable -> {
            // We are chaining another operation to this table creation. We want to use the resulting table and
            // create an entity in it. We use `flatMap` because it is asynchronous (ie. returns Mono or Flux).
            Mono<AzureTableEntity> entity = azureTable.createEntity("my-key", "my-value");
            // We return the `createEntity` operation.
            return entity;
        }).map(azureTableEntity -> {
            // This is a transformation, maybe we only care about the value of that entity we added.
            return azureTableEntity.getValue();
        }).subscribe(theValue -> {
            System.out.println("This was added: " + theValue);
        }, error -> {
            System.err.println("Error: " + error);
        });

        createTableMono.flatMap(azureTable -> {
            // We are chaining another operation to this table creation. We want to use the resulting table and
            // create an entity in it. We use `flatMap` because it is asynchronous (ie. returns Mono or Flux).
            return azureTable.createEntity("my-key-2", "my-value-2");
        }).map(azureTableEntity -> {
            // This is a transformation, maybe we only care about the value of that entity we added.
            return azureTableEntity.getValue();
        }).subscribe(theValue -> {
            System.out.println("This was added: " + theValue);
        }, error -> {
            System.err.println("Error: " + error);
        });

        TimeUnit.SECONDS.sleep(20);
    }

    /**
     * We've fixed this by caching the result of the `createTable` operation if it is successful. So we don't try to
     * create the table again.
     *
     * See {@link #createAndUpdateTable()}
     */
    @Test
    void createAndUpdateTableFixed() throws InterruptedException {
        AzureTableAsyncClient client = new AzureTableAsyncClient();

        // Every time this variable is used in a downstream subscriber, it will invoke the `createTable` operation.
        Mono<AzureTable> createTableMono = client.createTable("my-table")
            .cache(success -> {
                System.out.println("--- Table added. Caching value.");
                return Duration.ofSeconds(Long.MAX_VALUE);
            }, error -> {
                System.out.println("--- Error while adding table. Not caching value.");
                return Duration.ZERO;
            }, () -> {
                // This can occur because Monos can output 0 or 1 items. This would be the case where it output 0 items.
                // For example, Mono.empty() will complete, but not output any items.
                System.out.println("--- Expected a table to be output, not an empty value. Not caching.");
                return Duration.ZERO;
            });

        // FirstMono -> SecondFlatMap -> Map -> Subscribe
        createTableMono.flatMap(azureTable -> {
            // We are chaining another operation to this table creation. We want to use the resulting table and
            // create an entity in it. We use `flatMap` because it is asynchronous (ie. returns Mono or Flux).
            Mono<AzureTableEntity> entity = azureTable.createEntity("my-new-key", "my-new-value");
            // We return the `createEntity` operation.
            return entity;
        }).map(azureTableEntity -> {
            // This is a transformation, maybe we only care about the value of that entity we added.
            return azureTableEntity.getValue();
        }).subscribe(theValue -> {
            System.out.println("This was added: " + theValue);
        }, error -> {
            System.err.println("ERROR: " + error);
        });

        createTableMono.flatMap(azureTable -> {
            // We are chaining another operation to this table creation. We want to use the resulting table and
            // create an entity in it. We use `flatMap` because it is asynchronous (ie. returns Mono or Flux).
            return azureTable.createEntity("my-new-key-2", "my-new-value-2");
        }).map(azureTableEntity -> {
            // This is a transformation, maybe we only care about the value of that entity we added.
            return azureTableEntity.getValue();
        }).subscribe(theValue -> {
            System.out.println("This was added: " + theValue);
        }, error -> {
            System.err.println("ERROR: " + error);
        });

        TimeUnit.SECONDS.sleep(20);
    }
}
