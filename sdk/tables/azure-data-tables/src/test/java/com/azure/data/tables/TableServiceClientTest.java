// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.TestBase;
import com.azure.data.tables.models.TableItem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
            builder.httpClient(HttpClient.createDefault())
                .addPolicy(interceptorManager.getRecordPolicy())
                .addPolicy(new RetryPolicy());
        }

        serviceClient = builder.buildClient();
    }

    @Test
    void serviceCreateTable() {
        // Arrange
        String tableName = testResourceNamer.randomName("test", 20);

        // Act
        serviceClient.createTable(tableName);
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
