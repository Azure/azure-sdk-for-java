// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.ExponentialBackoff;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.TestBase;
import com.azure.data.tables.models.TableServiceException;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TableServiceClientTest extends TestBase {
    private TableServiceClient serviceClient;

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
            builder.addPolicy(new RetryPolicy(new ExponentialBackoff(6, Duration.ofMillis(1500),
                Duration.ofSeconds(100))));
        }
        serviceClient = builder.buildClient();
    }

    @Test
    void serviceCreateTable() {
        // Arrange
        String tableName = testResourceNamer.randomName("test", 20);

        // Act & Assert
        assertNotNull(serviceClient.createTable(tableName));
    }

    @Test
    void serviceCreateTableWithResponse() {
        // Arrange
        String tableName = testResourceNamer.randomName("test", 20);

        // Act & Assert
        assertNotNull(serviceClient.createTableWithResponse(tableName, null, null).getValue());
    }

    @Test
    void serviceCreateTableFailsIfExists() {
        // Arrange
        String tableName = testResourceNamer.randomName("test", 20);
        serviceClient.createTable(tableName);

        // Act & Assert
        assertThrows(TableServiceException.class, () -> serviceClient.createTable(tableName));
    }

    @Test
    void serviceCreateTableIfNotExists() {
        // Arrange
        String tableName = testResourceNamer.randomName("test", 20);

        // Act & Assert
        assertNotNull(serviceClient.createTableIfNotExists(tableName));
    }

    @Test
    void serviceCreateTableIfNotExistsSucceedsIfExists() {
        // Arrange
        String tableName = testResourceNamer.randomName("test", 20);
        serviceClient.createTable(tableName);

        //Act & Assert
        assertNull(serviceClient.createTableIfNotExists(tableName));
    }
}
