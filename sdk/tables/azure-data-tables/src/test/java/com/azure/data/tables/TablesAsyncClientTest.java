// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.TestBase;
import com.azure.data.tables.models.ListEntitiesOptions;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.UpdateMode;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link TableAsyncClient}.
 */
public class TablesAsyncClientTest extends TestBase {
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private TableAsyncClient tableClient;
    private HttpPipelinePolicy recordPolicy;
    private HttpClient playbackClient;

    @Override
    protected void beforeTest() {
        final String tableName = testResourceNamer.randomName("tableName", 20);
        final String connectionString = TestUtils.getConnectionString(interceptorManager.isPlaybackMode());
        final TableClientBuilder builder = new TableClientBuilder()
            .connectionString(connectionString)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .tableName(tableName);

        if (interceptorManager.isPlaybackMode()) {
            playbackClient = interceptorManager.getPlaybackClient();
            builder.httpClient(playbackClient);
        } else {
            recordPolicy = interceptorManager.getRecordPolicy();
            builder.httpClient(HttpClient.createDefault())
                .addPolicy(recordPolicy)
                .addPolicy(new RetryPolicy());
        }

        tableClient = builder.buildAsyncClient();
        tableClient.create().block(TIMEOUT);
    }

    @Test
    void createTableAsync() {
        // Arrange
        final String tableName2 = testResourceNamer.randomName("tableName", 20);
        final String connectionString = TestUtils.getConnectionString(interceptorManager.isPlaybackMode());
        final TableClientBuilder builder = new TableClientBuilder()
            .connectionString(connectionString)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .tableName(tableName2);

        if (interceptorManager.isPlaybackMode()) {
            builder.httpClient(playbackClient);
        } else {
            builder.httpClient(HttpClient.createDefault())
                .addPolicy(recordPolicy)
                .addPolicy(new RetryPolicy());
        }

        final TableAsyncClient asyncClient2 = builder.buildAsyncClient();

        // Act & Assert
        StepVerifier.create(asyncClient2.create())
            .expectComplete()
            .verify();
    }

    @Test
    void createTableWithResponseAsync() {
        // Arrange
        final String tableName2 = testResourceNamer.randomName("tableName", 20);
        final String connectionString = TestUtils.getConnectionString(interceptorManager.isPlaybackMode());
        final TableClientBuilder builder = new TableClientBuilder()
            .connectionString(connectionString)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .tableName(tableName2);

        if (interceptorManager.isPlaybackMode()) {
            builder.httpClient(playbackClient);
        } else {
            builder.httpClient(HttpClient.createDefault())
                .addPolicy(recordPolicy)
                .addPolicy(new RetryPolicy());
        }

        final TableAsyncClient asyncClient2 = builder.buildAsyncClient();
        final int expectedStatusCode = 204;

        // Act & Assert
        StepVerifier.create(asyncClient2.createWithResponse())
            .assertNext(response -> {
                assertEquals(expectedStatusCode, response.getStatusCode());
            })
            .expectComplete()
            .verify();
    }

    @Test
    void createEntityAsync() {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final TableEntity tableEntity = new TableEntity(partitionKeyValue, rowKeyValue);

        // Act & Assert
        StepVerifier.create(tableClient.createEntity(tableEntity))
            .expectComplete()
            .verify();
    }

    @Test
    void createEntityWithResponseAsync() {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final TableEntity entity = new TableEntity(partitionKeyValue, rowKeyValue);
        final int expectedStatusCode = 204;

        // Act & Assert
        StepVerifier.create(tableClient.createEntityWithResponse(entity))
            .assertNext(response -> {
                assertEquals(expectedStatusCode, response.getStatusCode());
            })
            .expectComplete()
            .verify();
    }

    @Test
    void deleteTableAsync() {
        // Act & Assert
        StepVerifier.create(tableClient.delete())
            .expectComplete()
            .verify();
    }

    @Test
    void deleteTableWithResponseAsync() {
        // Arrange
        final int expectedStatusCode = 204;

        // Act & Assert
        StepVerifier.create(tableClient.deleteWithResponse())
            .assertNext(response -> {
                assertEquals(expectedStatusCode, response.getStatusCode());
            })
            .expectComplete()
            .verify();
    }

    @Test
    void deleteEntityAsync() {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final TableEntity tableEntity = new TableEntity(partitionKeyValue, rowKeyValue);

        tableClient.createEntity(tableEntity).block(TIMEOUT);
        final TableEntity createdEntity = tableClient.getEntity(partitionKeyValue, rowKeyValue).block(TIMEOUT);
        assertNotNull(createdEntity, "'createdEntity' should not be null.");
        assertNotNull(createdEntity.getETag(), "'eTag' should not be null.");

        // Act & Assert
        StepVerifier.create(tableClient.deleteEntity(partitionKeyValue, rowKeyValue))
            .expectComplete()
            .verify();
    }

    @Test
    void deleteEntityWithResponseAsync() {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final TableEntity tableEntity = new TableEntity(partitionKeyValue, rowKeyValue);
        final int expectedStatusCode = 204;

        tableClient.createEntity(tableEntity).block(TIMEOUT);
        final TableEntity createdEntity = tableClient.getEntity(partitionKeyValue, rowKeyValue).block(TIMEOUT);
        assertNotNull(createdEntity, "'createdEntity' should not be null.");
        assertNotNull(createdEntity.getETag(), "'eTag' should not be null.");

        // Act & Assert
        StepVerifier.create(tableClient.deleteEntityWithResponse(partitionKeyValue, rowKeyValue, null))
            .assertNext(response -> {
                assertEquals(expectedStatusCode, response.getStatusCode());
            })
            .expectComplete()
            .verify();
    }

    @Test
    void deleteEntityWithResponseMatchETagAsync() {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final TableEntity tableEntity = new TableEntity(partitionKeyValue, rowKeyValue);
        final int expectedStatusCode = 204;

        tableClient.createEntity(tableEntity).block(TIMEOUT);
        final TableEntity createdEntity = tableClient.getEntity(partitionKeyValue, rowKeyValue).block(TIMEOUT);
        assertNotNull(createdEntity, "'createdEntity' should not be null.");
        assertNotNull(createdEntity.getETag(), "'eTag' should not be null.");

        // Act & Assert
        StepVerifier.create(tableClient.deleteEntityWithResponse(partitionKeyValue, rowKeyValue, createdEntity.getETag()))
            .assertNext(response -> {
                assertEquals(expectedStatusCode, response.getStatusCode());
            })
            .expectComplete()
            .verify();
    }

    @Test
    void getEntityWithResponseAsync() {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final TableEntity tableEntity = new TableEntity(partitionKeyValue, rowKeyValue);
        final int expectedStatusCode = 200;
        tableClient.createEntity(tableEntity).block(TIMEOUT);

        // Act & Assert
        StepVerifier.create(tableClient.getEntityWithResponse(partitionKeyValue, rowKeyValue))
            .assertNext(response -> {
                final TableEntity entity = response.getValue();
                assertEquals(expectedStatusCode, response.getStatusCode());

                assertNotNull(entity);
                assertEquals(tableEntity.getPartitionKey(), entity.getPartitionKey());
                assertEquals(tableEntity.getRowKey(), entity.getRowKey());

                assertNotNull(entity.getTimestamp());
                assertNotNull(entity.getETag());
                assertNotNull(entity.getProperties());
            })
            .expectComplete()
            .verify();
    }

    @Test
    void updateEntityWithResponseReplaceAsync() {
        updateEntityWithResponseAsync(UpdateMode.REPLACE);
    }

    @Test
    void updateEntityWithResponseMergeAsync() {
        updateEntityWithResponseAsync(UpdateMode.MERGE);
    }

    /**
     * In the case of {@link UpdateMode#MERGE}, we expect both properties to exist.
     * In the case of {@link UpdateMode#REPLACE}, we only expect {@code newPropertyKey} to exist.
     */
    void updateEntityWithResponseAsync(UpdateMode mode) {
        // Arrange
        final boolean expectOldProperty = mode == UpdateMode.MERGE;
        final String partitionKeyValue = testResourceNamer.randomName("APartitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("ARowKey", 20);
        final int expectedStatusCode = 204;
        final String oldPropertyKey = "propertyA";
        final String newPropertyKey = "propertyB";
        final TableEntity tableEntity = new TableEntity(partitionKeyValue, rowKeyValue)
            .addProperty(oldPropertyKey, "valueA");

        tableClient.createEntity(tableEntity).block(TIMEOUT);
        final TableEntity createdEntity = tableClient.getEntity(partitionKeyValue, rowKeyValue).block(TIMEOUT);
        assertNotNull(createdEntity, "'createdEntity' should not be null.");
        assertNotNull(createdEntity.getETag(), "'eTag' should not be null.");

        createdEntity.getProperties().remove(oldPropertyKey);
        createdEntity.addProperty(newPropertyKey, "valueB");

        // Act
        StepVerifier.create(tableClient.updateEntityWithResponse(createdEntity, true, mode))
            .assertNext(response -> assertEquals(expectedStatusCode, response.getStatusCode()))
            .expectComplete()
            .verify();

        // Assert and verify that the new properties are in there.
        StepVerifier.create(tableClient.getEntity(partitionKeyValue, rowKeyValue))
            .assertNext(entity -> {
                final Map<String, Object> properties = entity.getProperties();
                assertTrue(properties.containsKey(newPropertyKey));
                assertEquals(expectOldProperty, properties.containsKey(oldPropertyKey));
            })
            .verifyComplete();
    }

    @Test
    @Tag("ListEntities")
    void listEntitiesAsync() {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final String rowKeyValue2 = testResourceNamer.randomName("rowKey", 20);
        Mono.when(
            tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValue)),
            tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValue2))
        ).block(TIMEOUT);

        // Act & Assert
        StepVerifier.create(tableClient.listEntities())
            .expectNextCount(2)
            .thenConsumeWhile(x -> true)
            .expectComplete()
            .verify();
    }

    @Test
    @Tag("ListEntities")
    void listEntitiesWithFilterAsync() {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final String rowKeyValue2 = testResourceNamer.randomName("rowKey", 20);
        ListEntitiesOptions options = new ListEntitiesOptions().setFilter("RowKey eq '" + rowKeyValue + "'");
        Mono.when(
            tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValue)),
            tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValue2))
        ).block(TIMEOUT);

        // Act & Assert
        StepVerifier.create(tableClient.listEntities(options))
            .assertNext(returnEntity -> {
                assertEquals(partitionKeyValue, returnEntity.getPartitionKey());
                assertEquals(rowKeyValue, returnEntity.getRowKey());
            })
            .expectNextCount(0)
            .thenConsumeWhile(x -> true)
            .expectComplete()
            .verify();
    }

    @Test
    @Tag("ListEntities")
    void listEntitiesWithSelectAsync() {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final TableEntity entity = new TableEntity(partitionKeyValue, rowKeyValue)
            .addProperty("propertyC", "valueC")
            .addProperty("propertyD", "valueD");
        ListEntitiesOptions options = new ListEntitiesOptions()
            .setSelect("propertyC");
        tableClient.createEntity(entity).block(TIMEOUT);

        // Act & Assert
        StepVerifier.create(tableClient.listEntities(options))
            .assertNext(returnEntity -> {
                assertNull(returnEntity.getRowKey());
                assertNull(returnEntity.getPartitionKey());
                assertEquals("valueC", returnEntity.getProperties().get("propertyC"));
                assertNull(returnEntity.getProperties().get("propertyD"));
            })
            .expectComplete()
            .verify();
    }

    @Test
    @Tag("ListEntities")
    void listEntitiesWithTopAsync() {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final String rowKeyValue2 = testResourceNamer.randomName("rowKey", 20);
        final String rowKeyValue3 = testResourceNamer.randomName("rowKey", 20);
        ListEntitiesOptions options = new ListEntitiesOptions().setTop(2);
        Mono.when(
            tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValue)),
            tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValue2)),
            tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValue3))
        ).block(TIMEOUT);

        // Act & Assert
        StepVerifier.create(tableClient.listEntities(options))
            .expectNextCount(2)
            .thenConsumeWhile(x -> true)
            .expectComplete()
            .verify();
    }
}
