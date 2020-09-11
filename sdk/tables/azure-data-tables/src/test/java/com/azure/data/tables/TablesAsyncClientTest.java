// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.ExponentialBackoff;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.TestBase;
import com.azure.data.tables.models.ListEntitiesOptions;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.UpdateMode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link TableAsyncClient}.
 */
public class TablesAsyncClientTest extends TestBase {
    private static final Duration TIMEOUT = Duration.ofSeconds(100);

    private TableAsyncClient tableClient;
    private HttpPipelinePolicy recordPolicy;
    private HttpClient playbackClient;

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
            builder.httpClient(HttpClient.createDefault());
            if (!interceptorManager.isLiveMode()) {
                recordPolicy = interceptorManager.getRecordPolicy();
                builder.addPolicy(recordPolicy);
            }
            builder.addPolicy(new RetryPolicy(new ExponentialBackoff(6, Duration.ofMillis(1500),
                Duration.ofSeconds(100))));
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
            builder.httpClient(HttpClient.createDefault());
            if (!interceptorManager.isLiveMode()) {
                builder.addPolicy(recordPolicy);
            }
            builder.addPolicy(new RetryPolicy(new ExponentialBackoff(6, Duration.ofMillis(1500),
                Duration.ofSeconds(100))));
        }

        final TableAsyncClient tableClient2 = builder.buildAsyncClient();

        // Act & Assert
        StepVerifier.create(tableClient2.create())
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
            builder.httpClient(HttpClient.createDefault());
            if (!interceptorManager.isLiveMode()) {
                builder.addPolicy(recordPolicy);
            }
            builder.addPolicy(new RetryPolicy(new ExponentialBackoff(6, Duration.ofMillis(1500),
                Duration.ofSeconds(100))));
        }

        final TableAsyncClient tableClient2 = builder.buildAsyncClient();
        final int expectedStatusCode = 204;

        // Act & Assert
        StepVerifier.create(tableClient2.createWithResponse())
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
    void createEntityWithAllSupportedDataTypesAsync() {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final TableEntity tableEntity = new TableEntity(partitionKeyValue, rowKeyValue);

        final boolean booleanValue = true;
        final byte[] binaryValue = "Test value".getBytes();
        final Date dateValue = new Date();
        final OffsetDateTime offsetDateTimeValue = OffsetDateTime.now();
        final double doubleValue = 2.0d;
        final UUID guidValue = UUID.randomUUID();
        final int int32Value = 1337;
        final long int64Value = 1337L;
        final String stringValue = "This is table entity";

        tableEntity.addProperty("BinaryTypeProperty", binaryValue);
        tableEntity.addProperty("BooleanTypeProperty", booleanValue);
        tableEntity.addProperty("DateTypeProperty", dateValue);
        tableEntity.addProperty("OffsetDateTimeTypeProperty", offsetDateTimeValue);
        tableEntity.addProperty("DoubleTypeProperty", doubleValue);
        tableEntity.addProperty("GuidTypeProperty", guidValue);
        tableEntity.addProperty("Int32TypeProperty", int32Value);
        tableEntity.addProperty("Int64TypeProperty", int64Value);
        tableEntity.addProperty("StringTypeProperty", stringValue);

        tableClient.createEntity(tableEntity).block(TIMEOUT);

        // Act & Assert
        StepVerifier.create(tableClient.getEntityWithResponse(partitionKeyValue, rowKeyValue))
            .assertNext(response -> {
                final TableEntity entity = response.getValue();
                Map<String, Object> properties = entity.getProperties();
                assertTrue(properties.get("BinaryTypeProperty") instanceof byte[]);
                assertTrue(properties.get("BooleanTypeProperty") instanceof Boolean);
                assertTrue(properties.get("DateTypeProperty") instanceof OffsetDateTime);
                assertTrue(properties.get("OffsetDateTimeTypeProperty") instanceof OffsetDateTime);
                assertTrue(properties.get("DoubleTypeProperty") instanceof Double);
                assertTrue(properties.get("GuidTypeProperty") instanceof UUID);
                assertTrue(properties.get("Int32TypeProperty") instanceof Integer);
                assertTrue(properties.get("Int64TypeProperty") instanceof Long);
                assertTrue(properties.get("StringTypeProperty") instanceof String);
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
        StepVerifier.create(tableClient.deleteEntityWithResponse(partitionKeyValue, rowKeyValue,
            createdEntity.getETag()))
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

        // Act & Assert
        if (mode == UpdateMode.MERGE && tableClient.getTableUrl().contains("cosmos.azure.com")) {
            // This scenario is currently broken when using the CosmosDB Table API
            StepVerifier.create(tableClient.updateEntityWithResponse(createdEntity, true, mode))
                .expectError(com.azure.data.tables.implementation.models.TableServiceErrorException.class)
                .verify();
        } else {
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
    }

    @Test
    @Tag("ListEntities")
    void listEntitiesAsync() {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final String rowKeyValue2 = testResourceNamer.randomName("rowKey", 20);
        tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValue)).block(TIMEOUT);
        tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValue2)).block(TIMEOUT);

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
        tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValue)).block(TIMEOUT);
        tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValue2)).block(TIMEOUT);

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
        tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValue)).block(TIMEOUT);
        tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValue2)).block(TIMEOUT);
        tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValue3)).block(TIMEOUT);

        // Act & Assert
        StepVerifier.create(tableClient.listEntities(options))
            .expectNextCount(2)
            .thenConsumeWhile(x -> true)
            .expectComplete()
            .verify();
    }
}
