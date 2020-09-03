// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.TestBase;
import com.azure.data.tables.implementation.models.TableServiceErrorException;
import com.azure.data.tables.models.ListTablesOptions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests methods for {@link TableServiceAsyncClient}.
 */
public class TableServiceAsyncClientTest extends TestBase {
    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private TableServiceAsyncClient serviceClient;

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(TIMEOUT);
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @Override
    protected void beforeTest() {
        final String connectionString = TestUtils.getConnectionString(interceptorManager.isPlaybackMode());
        final TableServiceClientBuilder builder = new TableServiceClientBuilder()
            .connectionString(connectionString)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        if (interceptorManager.isPlaybackMode()) {
            builder.httpClient(interceptorManager.getPlaybackClient());
        } else {
            builder.httpClient(HttpClient.createDefault())
                .addPolicy(interceptorManager.getRecordPolicy())
                .addPolicy(new RetryPolicy());
        }

        serviceClient = builder.buildAsyncClient();
    }

    @Test
    void serviceCreateTableAsync() {
        // Arrange
        String tableName = testResourceNamer.randomName("test", 20);

        //Act & Assert
        StepVerifier.create(serviceClient.createTable(tableName))
            .expectComplete()
            .verify();
    }

    @Test
    void serviceCreateTableFailsIfExistsAsync() {
        // Arrange
        String tableName = testResourceNamer.randomName("test", 20);
        serviceClient.createTable(tableName).block(TIMEOUT);

        //Act & Assert
        StepVerifier.create(serviceClient.createTable(tableName))
            .expectErrorMatches(e -> e instanceof TableServiceErrorException
                && ((TableServiceErrorException) e).getResponse().getStatusCode() == 409)
            .verify();
    }

    @Test
    void serviceCreateTableWithResponseAsync() {
        // Arrange
        String tableName = testResourceNamer.randomName("test", 20);
        int expectedStatusCode = 204;

        //Act & Assert
        StepVerifier.create(serviceClient.createTableWithResponse(tableName))
            .assertNext(response -> {
                Assertions.assertEquals(expectedStatusCode, response.getStatusCode());
            })
            .expectComplete()
            .verify();
    }

    @Test
    void serviceCreateTableIfNotExistsAsync() {
        // Arrange
        String tableName = testResourceNamer.randomName("test", 20);

        //Act & Assert
        StepVerifier.create(serviceClient.createTableIfNotExists(tableName))
            .expectComplete()
            .verify();
    }

    @Test
    void serviceCreateTableIfNotExistsSucceedsIfExistsAsync() {
        // Arrange
        String tableName = testResourceNamer.randomName("test", 20);
        serviceClient.createTable(tableName).block(TIMEOUT);

        //Act & Assert
        StepVerifier.create(serviceClient.createTableIfNotExists(tableName))
            .expectComplete()
            .verify();
    }

    @Test
    void serviceCreateTableIfNotExistsWithResponseAsync() {
        // Arrange
        String tableName = testResourceNamer.randomName("test", 20);
        int expectedStatusCode = 204;

        //Act & Assert
        StepVerifier.create(serviceClient.createTableIfNotExistsWithResponse(tableName))
            .assertNext(response -> {
                Assertions.assertEquals(expectedStatusCode, response.getStatusCode());
            })
            .expectComplete()
            .verify();
    }

    @Test
    void serviceCreateTableIfNotExistsWithResponseSucceedsIfExistsAsync() {
        // Arrange
        String tableName = testResourceNamer.randomName("test", 20);
        int expectedStatusCode = 409;
        serviceClient.createTable(tableName).block(TIMEOUT);

        //Act & Assert
        StepVerifier.create(serviceClient.createTableIfNotExistsWithResponse(tableName))
            .assertNext(response -> {
                Assertions.assertEquals(expectedStatusCode, response.getStatusCode());
            })
            .expectComplete()
            .verify();
    }

    @Test
    void serviceDeleteTableAsync() {
        // Arrange
        final String tableName = testResourceNamer.randomName("test", 20);
        serviceClient.createTable(tableName).block(TIMEOUT);

        //Act & Assert
        StepVerifier.create(serviceClient.deleteTable(tableName))
            .expectComplete()
            .verify();
    }

    @Test
    void serviceDeleteTableWithResponseAsync() {
        // Arrange
        String tableName = testResourceNamer.randomName("test", 20);
        int expectedStatusCode = 204;
        serviceClient.createTable(tableName).block();

        //Act & Assert
        StepVerifier.create(serviceClient.deleteTableWithResponse(tableName))
            .assertNext(response -> {
                Assertions.assertEquals(expectedStatusCode, response.getStatusCode());
            })
            .expectComplete()
            .verify();
    }

    @Test
    @Tag("ListTables")
    void serviceListTablesAsync() {
        // Arrange
        final String tableName = testResourceNamer.randomName("test", 20);
        final String tableName2 = testResourceNamer.randomName("test", 20);
        Mono.when(
            serviceClient.createTable(tableName),
            serviceClient.createTable(tableName2)
        ).block(TIMEOUT);

        // Act & Assert
        StepVerifier.create(serviceClient.listTables())
            .expectNextCount(2)
            .thenConsumeWhile(x -> true)
            .expectComplete()
            .verify();
    }

    @Test
    @Tag("ListTables")
    void serviceListTablesWithFilterAsync() {
        // Arrange
        final String tableName = testResourceNamer.randomName("test", 20);
        final String tableName2 = testResourceNamer.randomName("test", 20);
        ListTablesOptions options = new ListTablesOptions().setFilter("TableName eq '" + tableName + "'");
        Mono.when(
            serviceClient.createTable(tableName),
            serviceClient.createTable(tableName2)
        ).block(TIMEOUT);

        // Act & Assert
        StepVerifier.create(serviceClient.listTables(options))
            .assertNext(table -> {
                assertEquals(tableName, table.getName());
            })
            .expectNextCount(0)
            .thenConsumeWhile(x -> true)
            .expectComplete()
            .verify();
    }

    @Test
    @Tag("ListTables")
    void serviceListTablesWithTopAsync() {
        // Arrange
        final String tableName = testResourceNamer.randomName("test", 20);
        final String tableName2 = testResourceNamer.randomName("test", 20);
        final String tableName3 = testResourceNamer.randomName("test", 20);
        ListTablesOptions options = new ListTablesOptions().setTop(2);
        Mono.when(
            serviceClient.createTable(tableName),
            serviceClient.createTable(tableName2),
            serviceClient.createTable(tableName3)
        ).block(TIMEOUT);
        
        // Act & Assert
        StepVerifier.create(serviceClient.listTables(options))
            .expectNextCount(2)
            .thenConsumeWhile(x -> true)
            .expectComplete()
            .verify();
    }
}
