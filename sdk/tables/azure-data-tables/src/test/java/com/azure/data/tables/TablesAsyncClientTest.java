// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.TestBase;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableQueryParams;
import com.azure.data.tables.models.TableUpdateMode;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link TableAsyncClient}.
 */
public class TablesAsyncClientTest extends TestBase {
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private TableAsyncClient asyncClient;
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

        asyncClient = builder.buildAsyncClient();

        asyncClient.create().block(TIMEOUT);
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
            .assertNext(response -> assertEquals(tableName2, response.getName()))
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
        StepVerifier.create(asyncClient.createEntity(tableEntity))
            .assertNext(response -> {
                assertEquals(response.getPartitionKey(), partitionKeyValue);
                assertEquals(response.getRowKey(), rowKeyValue);
                assertNotNull(response.getETag());
            })
            .expectComplete()
            .verify();
    }

    @Test
    void createEntityWithResponse() {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final TableEntity entity = new TableEntity(partitionKeyValue, rowKeyValue);
        entity.addProperty("prop", false);
        entity.addProperty("int", 32);
        //entity.addProperty("prop@odata.type", "Edm.Boolean");
        final int expectedStatusCode = 201;

        // Act & Assert
        StepVerifier.create(asyncClient.createEntityWithResponse(entity))
            .assertNext(response -> {
                String s = response.toString();
                assertEquals(response.getValue().getPartitionKey(), partitionKeyValue);
                assertEquals(response.getValue().getRowKey(), rowKeyValue);
                assertNotNull(response.getValue().getETag());
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

        final TableEntity createdEntity = asyncClient.createEntity(tableEntity).block(TIMEOUT);
        assertNotNull(createdEntity, "'createdEntity' should not be null.");
        assertNotNull(createdEntity.getETag(), "'eTag' should not be null.");

        // Act & Assert
        StepVerifier.create(asyncClient.deleteEntity(createdEntity))
            .expectComplete()
            .verify();
    }

    @Test
    void deleteEntityWithResponse() {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final TableEntity tableEntity = new TableEntity(partitionKeyValue, rowKeyValue);
        final int expectedStatusCode = 204;

        final TableEntity createdEntity = asyncClient.createEntity(tableEntity).block(TIMEOUT);
        assertNotNull(createdEntity, "'createdEntity' should not be null.");
        assertNotNull(createdEntity.getETag(), "'eTag' should not be null.");

        // Act & Assert
        StepVerifier.create(asyncClient.deleteEntityWithResponse(createdEntity, false))
            .assertNext(response -> {
                assertEquals(expectedStatusCode, response.getStatusCode());
            })
            .expectComplete()
            .verify();
    }

    @Test
    void deleteEntityWithResponseMatchETag() {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final TableEntity tableEntity = new TableEntity(partitionKeyValue, rowKeyValue);
        final int expectedStatusCode = 204;

        final TableEntity createdEntity = asyncClient.createEntity(tableEntity).block(TIMEOUT);
        assertNotNull(createdEntity, "'createdEntity' should not be null.");
        assertNotNull(createdEntity.getETag(), "'eTag' should not be null.");

        // Act & Assert
        StepVerifier.create(asyncClient.deleteEntityWithResponse(createdEntity, true))
            .assertNext(response -> {
                assertEquals(expectedStatusCode, response.getStatusCode());
            })
            .expectComplete()
            .verify();
    }

    @Test
    void getEntityWithResponse() {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final TableEntity tableEntity = new TableEntity(partitionKeyValue, rowKeyValue);
        final int expectedStatusCode = 200;

        final TableEntity createdEntity = asyncClient.createEntity(tableEntity).block(TIMEOUT);
        assertNotNull(createdEntity, "'createdEntity' should not be null.");
        assertNotNull(createdEntity.getETag(), "'eTag' should not be null.");

        // Act & Assert
        StepVerifier.create(asyncClient.getEntityWithResponse(createdEntity.getPartitionKey(), createdEntity.getRowKey()))
            .assertNext(response -> {
                final TableEntity entity = response.getValue();
                assertEquals(expectedStatusCode, response.getStatusCode());

                assertNotNull(entity);
                assertEquals(tableEntity.getPartitionKey(), entity.getPartitionKey());
                assertEquals(tableEntity.getRowKey(), entity.getRowKey());

                assertEquals(createdEntity.getETag(), entity.getETag());

                assertNotNull(entity.getProperties());
            })
            .expectComplete()
            .verify();
    }

    @Test
    void updateEntityWithResponseReplace() {
        updateEntityWithResponse(TableUpdateMode.REPLACE);
    }

    @Test
    void updateEntityWithResponseMerge() {
        updateEntityWithResponse(TableUpdateMode.MERGE);
    }

    /**
     * In the case of {@link TableUpdateMode#MERGE}, we expect both properties to exist.
     * In the case of {@link TableUpdateMode#REPLACE}, we only expect {@code newPropertyKey} to exist.
     */
    void updateEntityWithResponse(TableUpdateMode mode) {
        // Arrange
        final boolean expectOldProperty = mode == TableUpdateMode.MERGE;
        final String partitionKeyValue = testResourceNamer.randomName("APartitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("ARowKey", 20);
        final int expectedStatusCode = 204;
        final String oldPropertyKey = "propertyA";
        final String newPropertyKey = "propertyB";
        final TableEntity tableEntity = new TableEntity(partitionKeyValue, rowKeyValue)
            .addProperty(oldPropertyKey, "valueA");

        final TableEntity createdEntity = asyncClient.createEntity(tableEntity).block(TIMEOUT);
        assertNotNull(createdEntity, "'createdEntity' should not be null.");
        assertNotNull(createdEntity.getETag(), "'eTag' should not be null.");

        createdEntity.getProperties().remove(oldPropertyKey);
        createdEntity.addProperty(newPropertyKey, "valueB");

        // Act
        StepVerifier.create(asyncClient.updateEntityWithResponse(createdEntity, true, mode))
            .assertNext(response -> assertEquals(expectedStatusCode, response.getStatusCode()))
            .expectComplete()
            .verify();

        // Assert and verify that the new properties are in there.
        StepVerifier.create(asyncClient.getEntity(partitionKeyValue, rowKeyValue))
            .assertNext(entity -> {
                final Map<String, Object> properties = entity.getProperties();
                assertTrue(properties.containsKey(newPropertyKey));
                assertEquals(expectOldProperty, properties.containsKey(oldPropertyKey));
            })
            .verifyComplete();
    }

    @Disabled("List not working yet.")
    @Test
    void listEntityWithFilter() {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final TableEntity entity = new TableEntity(partitionKeyValue, rowKeyValue);
        TableQueryParams queryParams1 = new TableQueryParams().setFilter("PartitionKey eq '" + entity.getPartitionKey() + "'");
        asyncClient.createEntity(entity).block(TIMEOUT);

        // Act & Assert
        StepVerifier.create(asyncClient.listEntities(queryParams1))
            .assertNext(returnEntity -> {
                assertEquals(partitionKeyValue, returnEntity.getPartitionKey());
                assertEquals(entity.getRowKey(), returnEntity.getRowKey());
            })
            .expectComplete()
            .verify();
    }

    @Disabled("List not working yet.")
    @Test
    void listEntityWithSelect() {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final TableEntity entity = new TableEntity(partitionKeyValue, rowKeyValue)
            .addProperty("propertyC", "valueC")
            .addProperty("propertyD", "valueD");
        TableQueryParams queryParams = new TableQueryParams()
            .setFilter("PartitionKey eq '" + entity.getPartitionKey() + "'")
            .setSelect("propertyC");
        asyncClient.createEntity(entity).block(TIMEOUT);

        // Act & Assert
        StepVerifier.create(asyncClient.listEntities(queryParams))
            .assertNext(returnEntity -> {
                assertEquals(entity.getRowKey(), returnEntity.getRowKey());
                assertEquals(entity.getPartitionKey(), returnEntity.getPartitionKey());
                assertEquals("valueC", returnEntity.getProperties().get("propertyC"));
                assertEquals(3, returnEntity.getProperties().size());
            })
            .expectComplete()
            .verify();
    }

    @Test
    void listEntityWithTop() {
        // Arrange
        TableQueryParams queryParams = new TableQueryParams()
            .setTop(1);

        // Act & Assert
        StepVerifier.create(asyncClient.listEntities(queryParams))
            .expectNextCount(2);
    }
}
