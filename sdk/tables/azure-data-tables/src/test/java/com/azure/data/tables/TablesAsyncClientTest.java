// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.ExponentialBackoff;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestBase;
import com.azure.core.test.utils.TestResourceNamer;
import com.azure.data.tables.models.BatchOperationResponse;
import com.azure.data.tables.models.ListEntitiesOptions;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableServiceErrorException;
import com.azure.data.tables.models.UpdateMode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        StepVerifier.create(tableClient.getEntityWithResponse(partitionKeyValue, rowKeyValue, null))
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
    void createEntitySubclassAsync() {
        // Arrange
        String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        byte[] bytes = new byte[]{1, 2, 3};
        boolean b = true;
        OffsetDateTime dateTime = OffsetDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        double d = 1.23D;
        UUID uuid = UUID.fromString("11111111-2222-3333-4444-555555555555");
        int i = 123;
        long l = 123L;
        String s = "Test";
        SampleEntity.Color color = SampleEntity.Color.GREEN;

        SampleEntity tableEntity = new SampleEntity(partitionKeyValue, rowKeyValue);
        tableEntity.setByteField(bytes);
        tableEntity.setBooleanField(b);
        tableEntity.setDateTimeField(dateTime);
        tableEntity.setDoubleField(d);
        tableEntity.setUuidField(uuid);
        tableEntity.setIntField(i);
        tableEntity.setLongField(l);
        tableEntity.setStringField(s);
        tableEntity.setEnumField(color);

        tableClient.createEntity(tableEntity).block(TIMEOUT);

        // Act & Assert
        StepVerifier.create(tableClient.getEntityWithResponse(partitionKeyValue, rowKeyValue, null))
            .assertNext(response -> {
                TableEntity entity = response.getValue();
                assertArrayEquals((byte[]) entity.getProperties().get("ByteField"), bytes);
                assertEquals(entity.getProperties().get("BooleanField"), b);
                assertTrue(dateTime.isEqual((OffsetDateTime) entity.getProperties().get("DateTimeField")));
                assertEquals(entity.getProperties().get("DoubleField"), d);
                assertEquals(0, uuid.compareTo((UUID) entity.getProperties().get("UuidField")));
                assertEquals(entity.getProperties().get("IntField"), i);
                assertEquals(entity.getProperties().get("LongField"), l);
                assertEquals(entity.getProperties().get("StringField"), s);
                assertEquals(entity.getProperties().get("EnumField"), color.name());
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
        getEntityWithResponseAsyncImpl(this.tableClient, this.testResourceNamer);
    }

    static void getEntityWithResponseAsyncImpl(TableAsyncClient tableClient, TestResourceNamer testResourceNamer) {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final TableEntity tableEntity = new TableEntity(partitionKeyValue, rowKeyValue);
        final int expectedStatusCode = 200;
        tableClient.createEntity(tableEntity).block(TIMEOUT);

        // Act & Assert
        StepVerifier.create(tableClient.getEntityWithResponse(partitionKeyValue, rowKeyValue, null))
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
    void getEntityWithResponseWithSelectAsync() {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final TableEntity tableEntity = new TableEntity(partitionKeyValue, rowKeyValue);
        tableEntity.addProperty("Test", "Value");
        final int expectedStatusCode = 200;
        tableClient.createEntity(tableEntity).block(TIMEOUT);

        // Act & Assert
        StepVerifier.create(tableClient.getEntityWithResponse(partitionKeyValue, rowKeyValue, "Test"))
            .assertNext(response -> {
                final TableEntity entity = response.getValue();
                assertEquals(expectedStatusCode, response.getStatusCode());

                assertNotNull(entity);
                assertNull(entity.getPartitionKey());
                assertNull(entity.getRowKey());
                assertNull(entity.getTimestamp());
                assertNotNull(entity.getETag());
                assertEquals(entity.getProperties().get("Test"), "Value");
            })
            .expectComplete()
            .verify();
    }

    @Test
    void getEntityWithResponseSubclassAsync() {
        // Arrange
        String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        byte[] bytes = new byte[]{1, 2, 3};
        boolean b = true;
        OffsetDateTime dateTime = OffsetDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        double d = 1.23D;
        UUID uuid = UUID.fromString("11111111-2222-3333-4444-555555555555");
        int i = 123;
        long l = 123L;
        String s = "Test";
        SampleEntity.Color color = SampleEntity.Color.GREEN;

        Map<String, Object> props = new HashMap<>();
        props.put("ByteField", bytes);
        props.put("BooleanField", b);
        props.put("DateTimeField", dateTime);
        props.put("DoubleField", d);
        props.put("UuidField", uuid);
        props.put("IntField", i);
        props.put("LongField", l);
        props.put("StringField", s);
        props.put("EnumField", color);

        TableEntity tableEntity = new TableEntity(partitionKeyValue, rowKeyValue);
        tableEntity.addProperties(props);

        int expectedStatusCode = 200;
        tableClient.createEntity(tableEntity).block(TIMEOUT);

        // Act & Assert
        StepVerifier.create(tableClient.getEntityWithResponse(partitionKeyValue, rowKeyValue, null, SampleEntity.class))
            .assertNext(response -> {
                SampleEntity entity = response.getValue();
                assertEquals(expectedStatusCode, response.getStatusCode());

                assertNotNull(entity);
                assertEquals(tableEntity.getPartitionKey(), entity.getPartitionKey());
                assertEquals(tableEntity.getRowKey(), entity.getRowKey());

                assertNotNull(entity.getTimestamp());
                assertNotNull(entity.getETag());

                assertArrayEquals(bytes, entity.getByteField());
                assertEquals(b, entity.getBooleanField());
                assertTrue(dateTime.isEqual(entity.getDateTimeField()));
                assertEquals(d, entity.getDoubleField());
                assertEquals(0, uuid.compareTo(entity.getUuidField()));
                assertEquals(i, entity.getIntField());
                assertEquals(l, entity.getLongField());
                assertEquals(s, entity.getStringField());
                assertEquals(color, entity.getEnumField());
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
        StepVerifier.create(tableClient.updateEntityWithResponse(createdEntity, mode, true))
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
    void updateEntityWithResponseSubclassAsync() {
        // Arrange
        String partitionKeyValue = testResourceNamer.randomName("APartitionKey", 20);
        String rowKeyValue = testResourceNamer.randomName("ARowKey", 20);
        int expectedStatusCode = 204;

        SingleFieldEntity tableEntity = new SingleFieldEntity(partitionKeyValue, rowKeyValue);
        tableEntity.setSubclassProperty("InitialValue");
        tableClient.createEntity(tableEntity).block(TIMEOUT);

        // Act & Assert
        tableEntity.setSubclassProperty("UpdatedValue");
        StepVerifier.create(tableClient.updateEntityWithResponse(tableEntity, UpdateMode.REPLACE, true))
            .assertNext(response -> assertEquals(expectedStatusCode, response.getStatusCode()))
            .expectComplete()
            .verify();

        StepVerifier.create(tableClient.getEntity(partitionKeyValue, rowKeyValue))
            .assertNext(entity -> {
                Map<String, Object> properties = entity.getProperties();
                assertTrue(properties.containsKey("SubclassProperty"));
                assertEquals("UpdatedValue", properties.get("SubclassProperty"));
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

    @Test
    @Tag("ListEntities")
    void listEntitiesSubclassAsync() {
        // Arrange
        String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        String rowKeyValue2 = testResourceNamer.randomName("rowKey", 20);
        tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValue)).block(TIMEOUT);
        tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValue2)).block(TIMEOUT);

        // Act & Assert
        StepVerifier.create(tableClient.listEntities(SampleEntity.class))
            .expectNextCount(2)
            .thenConsumeWhile(x -> true)
            .expectComplete()
            .verify();
    }

    @Test
    @Tag("Batch")
    void batchAsync() {
        String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        String rowKeyValue2 = testResourceNamer.randomName("rowKey", 20);
        int expectedBatchStatusCode = 202;
        int expectedOperationStatusCode = 204;

        TableAsyncBatch batch = tableClient.createBatch(partitionKeyValue);
        batch.createEntity(new TableEntity(partitionKeyValue, rowKeyValue))
            .createEntity(new TableEntity(partitionKeyValue, rowKeyValue2));

        // Act & Assert
        final Response<List<BatchOperationResponse>> result = batch.submitTransactionWithResponse().block(TIMEOUT);

        assertNotNull(result);
        assertEquals(expectedBatchStatusCode, result.getStatusCode());
        assertEquals(batch.getOperations().size(), result.getValue().size());
        assertEquals(expectedOperationStatusCode, result.getValue().get(0).getStatusCode());
        assertEquals(expectedOperationStatusCode, result.getValue().get(1).getStatusCode());

        StepVerifier.create(tableClient.getEntityWithResponse(partitionKeyValue, rowKeyValue, null))
            .assertNext(response -> {
                final TableEntity entity = response.getValue();
                assertNotNull(entity);
                assertEquals(partitionKeyValue, entity.getPartitionKey());
                assertEquals(rowKeyValue, entity.getRowKey());

                assertNotNull(entity.getTimestamp());
                assertNotNull(entity.getETag());
                assertNotNull(entity.getProperties());
            })
            .expectComplete()
            .verify();
    }

    @Test
    @Tag("Batch")
    void batchAsyncAllOperations() {
        String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        String rowKeyValueCreate = testResourceNamer.randomName("rowKey", 20);
        String rowKeyValueUpsertInsert = testResourceNamer.randomName("rowKey", 20);
        String rowKeyValueUpsertMerge = testResourceNamer.randomName("rowKey", 20);
        String rowKeyValueUpsertReplace = testResourceNamer.randomName("rowKey", 20);
        String rowKeyValueUpdateMerge = testResourceNamer.randomName("rowKey", 20);
        String rowKeyValueUpdateReplace = testResourceNamer.randomName("rowKey", 20);
        String rowKeyValueDelete = testResourceNamer.randomName("rowKey", 20);

        int expectedBatchStatusCode = 202;
        int expectedOperationStatusCode = 204;

        tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValueUpsertMerge)).block(TIMEOUT);
        tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValueUpsertReplace)).block(TIMEOUT);
        tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValueUpdateMerge)).block(TIMEOUT);
        tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValueUpdateReplace)).block(TIMEOUT);
        tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValueDelete)).block(TIMEOUT);


        TableEntity toUpsertMerge = new TableEntity(partitionKeyValue, rowKeyValueUpsertMerge);
        toUpsertMerge.addProperty("Test", "MergedValue");

        TableEntity toUpsertReplace = new TableEntity(partitionKeyValue, rowKeyValueUpsertReplace);
        toUpsertReplace.addProperty("Test", "ReplacedValue");

        TableEntity toUpdateMerge = new TableEntity(partitionKeyValue, rowKeyValueUpdateMerge);
        toUpdateMerge.addProperty("Test", "MergedValue");

        TableEntity toUpdateReplace = new TableEntity(partitionKeyValue, rowKeyValueUpdateReplace);
        toUpdateReplace.addProperty("Test", "MergedValue");

        TableAsyncBatch batch = tableClient.createBatch(partitionKeyValue);
        batch.createEntity(new TableEntity(partitionKeyValue, rowKeyValueCreate))
            .upsertEntity(new TableEntity(partitionKeyValue, rowKeyValueUpsertInsert))
            .upsertEntity(toUpsertMerge, UpdateMode.MERGE)
            .upsertEntity(toUpsertReplace, UpdateMode.REPLACE)
            .updateEntity(toUpdateMerge, UpdateMode.MERGE)
            .updateEntity(toUpdateReplace, UpdateMode.REPLACE)
            .deleteEntity(rowKeyValueDelete);

        // Act & Assert
        StepVerifier.create(batch.submitTransactionWithResponse())
            .assertNext(response -> {
                assertNotNull(response);
                assertEquals(expectedBatchStatusCode, response.getStatusCode());
                List<BatchOperationResponse> subResponses = response.getValue();
                assertEquals(batch.getOperations().size(), subResponses.size());
                for (BatchOperationResponse subResponse : subResponses) {
                    assertEquals(expectedOperationStatusCode, subResponse.getStatusCode());
                }
            })
            .expectComplete()
            .verify();
    }

    @Test
    @Tag("Batch")
    void batchAsyncWithFailingOperation() {
        String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        String rowKeyValue2 = testResourceNamer.randomName("rowKey", 20);

        TableAsyncBatch batch = tableClient.createBatch(partitionKeyValue);
        batch.createEntity(new TableEntity(partitionKeyValue, rowKeyValue))
            .deleteEntity(rowKeyValue2);

        // Act & Assert
        StepVerifier.create(batch.submitTransactionWithResponse())
            .expectErrorMatches(e -> e instanceof TableServiceErrorException
                && e.getMessage().contains("An operation within the batch failed")
                && e.getMessage().contains("The failed operation was")
                && e.getMessage().contains("DeleteEntity")
                && e.getMessage().contains("partitionKey='" + partitionKeyValue)
                && e.getMessage().contains("rowKey='" + rowKeyValue2))
            .verify();
    }

    @Test
    @Tag("Batch")
    void batchRequiresSamePartitionKey() {
        String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        String partitionKeyValue2 = testResourceNamer.randomName("partitionKey", 20);
        String rowKeyValue = testResourceNamer.randomName("rowKey", 20);

        TableAsyncBatch batch = tableClient.createBatch(partitionKeyValue);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
            () -> batch.createEntity(new TableEntity(partitionKeyValue2, rowKeyValue)));
    }

    @Test
    @Tag("Batch")
    void batchRequiresUniqueRowKey() {
        String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        String rowKeyValue = testResourceNamer.randomName("rowKey", 20);

        TableAsyncBatch batch = tableClient.createBatch(partitionKeyValue);
        batch.createEntity(new TableEntity(partitionKeyValue, rowKeyValue));

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
            () -> batch.createEntity(new TableEntity(partitionKeyValue, rowKeyValue)));
    }

    @Test
    @Tag("Batch")
    void batchRequiresOperationsOnSubmit() {
        String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);

        TableAsyncBatch batch = tableClient.createBatch(partitionKeyValue);

        // Act & Assert
        StepVerifier.create(batch.submitTransaction())
            .expectError(IllegalStateException.class)
            .verify();
    }

    @Test
    @Tag("Batch")
    void batchImmutableAfterSubmit() {
        String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        String rowKeyValue2 = testResourceNamer.randomName("rowKey", 20);

        TableAsyncBatch batch = tableClient.createBatch(partitionKeyValue);
        batch.createEntity(new TableEntity(partitionKeyValue, rowKeyValue));
        batch.submitTransaction().block(TIMEOUT);

        // Act & Assert
        assertThrows(IllegalStateException.class,
            () -> batch.createEntity(new TableEntity(partitionKeyValue, rowKeyValue2)));
    }

}
