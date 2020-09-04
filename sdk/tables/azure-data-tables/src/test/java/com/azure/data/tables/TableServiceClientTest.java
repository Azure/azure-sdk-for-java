// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.TestBase;
import com.azure.data.tables.implementation.models.TableServiceErrorException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TableServiceClientTest extends TestBase {
    private CosmosThrottled<TableServiceClient> runner;

    @Override
    protected void beforeTest() {
        final String connectionString = TestUtils.getConnectionString(interceptorManager.isPlaybackMode());
        final TableServiceClientBuilder builder = new TableServiceClientBuilder()
            .connectionString(connectionString)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        if (interceptorManager.isPlaybackMode()) {
            builder.httpClient(interceptorManager.getPlaybackClient());
        } else {
            builder.httpClient(HttpClient.createDefault());
            if (!interceptorManager.isLiveMode()) {
                builder.addPolicy(interceptorManager.getRecordPolicy());
            }
            builder.addPolicy(new RetryPolicy());
        }
        runner = CosmosThrottled.get(builder.buildClient(), interceptorManager.isPlaybackMode());
    }

    @Test
    void serviceCreateTable() {
        // Arrange
        String tableName = testResourceNamer.randomName("test", 20);

        // Act & Assert
        runner.runVoid(serviceClient -> serviceClient.createTable(tableName));
    }

    @Test
    void serviceCreateTableFailsIfExists() {
        // Arrange
        String tableName = testResourceNamer.randomName("test", 20);
        runner.runVoid(serviceClient -> serviceClient.createTable(tableName));

        // Act & Assert
        Assertions.assertThrows(TableServiceErrorException.class,
            () -> runner.runVoid(serviceClient -> serviceClient.createTable(tableName)));
    }

    @Test
    void serviceCreateTableIfNotExists() {
        // Arrange
        String tableName = testResourceNamer.randomName("test", 20);

        // Act & Assert
        runner.runVoid(serviceClient -> serviceClient.createTableIfNotExists(tableName));
    }

    @Test
    void serviceCreateTableIfNotExistsSucceedsIfExists() {
        // Arrange
        String tableName = testResourceNamer.randomName("test", 20);
        runner.runVoid(serviceClient -> serviceClient.createTable(tableName));

        //Act & Assert
        runner.runVoid(serviceClient -> serviceClient.createTableIfNotExists(tableName));
    }

    @Test
    void serviceDeleteTable() {
        // Arrange
        String tableName = testResourceNamer.randomName("test", 20);

        // Act & Assert
    }

    @Test
    void serviceDeleteTableWithResponse() {
        // Arrange
        String tableName = testResourceNamer.randomName("test", 20);
        int expectedStatusCode = 204;
    }
}
