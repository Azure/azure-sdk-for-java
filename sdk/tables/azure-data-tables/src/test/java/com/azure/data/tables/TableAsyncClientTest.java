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
import com.azure.data.tables.models.TableServiceException;
import com.azure.data.tables.models.ListEntitiesOptions;
import com.azure.data.tables.models.TableAccessPolicy;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableEntityUpdateMode;
import com.azure.data.tables.models.TableSignedIdentifier;
import com.azure.data.tables.models.TableTransactionAction;
import com.azure.data.tables.models.TableTransactionActionResponse;
import com.azure.data.tables.models.TableTransactionActionType;
import com.azure.data.tables.models.TableTransactionFailedException;
import com.azure.data.tables.models.TableTransactionResult;
import com.azure.data.tables.sas.TableSasIpRange;
import com.azure.data.tables.sas.TableSasPermission;
import com.azure.data.tables.sas.TableSasProtocol;
import com.azure.data.tables.sas.TableSasSignatureValues;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link TableAsyncClient}.
 */
public class TableAsyncClientTest extends TestBase {
    private static final Duration TIMEOUT = Duration.ofSeconds(100);
    private static final HttpClient DEFAULT_HTTP_CLIENT = HttpClient.createDefault();
    private static final boolean IS_COSMOS_TEST = System.getenv("AZURE_TABLES_CONNECTION_STRING") != null
        && System.getenv("AZURE_TABLES_CONNECTION_STRING").contains("cosmos.azure.com");

    private TableAsyncClient tableClient;
    private HttpPipelinePolicy recordPolicy;
    private HttpClient playbackClient;

    private TableClientBuilder getClientBuilder(String tableName, String connectionString) {
        final TableClientBuilder builder = new TableClientBuilder()
            .connectionString(connectionString)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .tableName(tableName);

        if (interceptorManager.isPlaybackMode()) {
            playbackClient = interceptorManager.getPlaybackClient();

            builder.httpClient(playbackClient);
        } else {
            builder.httpClient(DEFAULT_HTTP_CLIENT);

            if (!interceptorManager.isLiveMode()) {
                recordPolicy = interceptorManager.getRecordPolicy();

                builder.addPolicy(recordPolicy);
            }
        }

        return builder;
    }

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
        tableClient = getClientBuilder(tableName, connectionString).buildAsyncClient();

        tableClient.createTable().block(TIMEOUT);
    }

    @Test
    void createTableAsync() {
        // Arrange
        final String tableName2 = testResourceNamer.randomName("tableName", 20);
        final String connectionString = TestUtils.getConnectionString(interceptorManager.isPlaybackMode());
        final TableAsyncClient tableClient2 = getClientBuilder(tableName2, connectionString).buildAsyncClient();

        // Act & Assert
        StepVerifier.create(tableClient2.createTable())
            .assertNext(Assertions::assertNotNull)
            .expectComplete()
            .verify();
    }

    @Test
    void createTableWithResponseAsync() {
        // Arrange
        final String tableName2 = testResourceNamer.randomName("tableName", 20);
        final String connectionString = TestUtils.getConnectionString(interceptorManager.isPlaybackMode());
        final TableAsyncClient tableClient2 = getClientBuilder(tableName2, connectionString).buildAsyncClient();
        final int expectedStatusCode = 204;

        // Act & Assert
        StepVerifier.create(tableClient2.createTableWithResponse())
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
            .assertNext(response -> assertEquals(expectedStatusCode, response.getStatusCode()))
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
                final Map<String, Object> properties = entity.getProperties();
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

    // Will not be supporting subclasses of TableEntity for the time being.
    /*@Test
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
    }*/

    @Test
    void deleteTableAsync() {
        // Act & Assert
        StepVerifier.create(tableClient.deleteTable())
            .expectComplete()
            .verify();
    }

    @Test
    void deleteNonExistingTableAsync() {
        // Act & Assert
        tableClient.deleteTable().block();

        StepVerifier.create(tableClient.deleteTable())
            .expectComplete()
            .verify();
    }

    @Test
    void deleteTableWithResponseAsync() {
        // Arrange
        final int expectedStatusCode = 204;

        // Act & Assert
        StepVerifier.create(tableClient.deleteTableWithResponse())
            .assertNext(response -> {
                assertEquals(expectedStatusCode, response.getStatusCode());
            })
            .expectComplete()
            .verify();
    }

    @Test
    void deleteNonExistingTableWithResponseAsync() {
        // Arrange
        final int expectedStatusCode = 404;

        // Act & Assert
        tableClient.deleteTableWithResponse().block();

        StepVerifier.create(tableClient.deleteTableWithResponse())
            .assertNext(response -> assertEquals(expectedStatusCode, response.getStatusCode()))
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
    void deleteNonExistingEntityAsync() {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);

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
        StepVerifier.create(tableClient.deleteEntityWithResponse(createdEntity, false))
            .assertNext(response -> assertEquals(expectedStatusCode, response.getStatusCode()))
            .expectComplete()
            .verify();
    }

    @Test
    void deleteNonExistingEntityWithResponseAsync() {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final TableEntity entity = new TableEntity(partitionKeyValue, rowKeyValue);
        final int expectedStatusCode = 404;

        // Act & Assert
        StepVerifier.create(tableClient.deleteEntityWithResponse(entity, false))
            .assertNext(response -> assertEquals(expectedStatusCode, response.getStatusCode()))
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
        StepVerifier.create(tableClient.deleteEntityWithResponse(createdEntity, true))
            .assertNext(response -> assertEquals(expectedStatusCode, response.getStatusCode()))
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
        List<String> propertyList = new ArrayList<>();
        propertyList.add("Test");

        // Act & Assert
        StepVerifier.create(tableClient.getEntityWithResponse(partitionKeyValue, rowKeyValue, propertyList))
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

    // Will not be supporting subclasses of TableEntity for the time being.
    /*@Test
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

        final Map<String, Object> props = new HashMap<>();
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
        tableEntity.setProperties(props);

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
    }*/

    @Test
    void updateEntityWithResponseReplaceAsync() {
        updateEntityWithResponseAsync(TableEntityUpdateMode.REPLACE);
    }

    @Test
    void updateEntityWithResponseMergeAsync() {
        updateEntityWithResponseAsync(TableEntityUpdateMode.MERGE);
    }

    /**
     * In the case of {@link TableEntityUpdateMode#MERGE}, we expect both properties to exist.
     * In the case of {@link TableEntityUpdateMode#REPLACE}, we only expect {@code newPropertyKey} to exist.
     */
    void updateEntityWithResponseAsync(TableEntityUpdateMode mode) {
        // Arrange
        final boolean expectOldProperty = mode == TableEntityUpdateMode.MERGE;
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

    // Will not be supporting subclasses of TableEntity for the time being.
    /*@Test
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
        StepVerifier.create(tableClient.updateEntityWithResponse(tableEntity, TableEntityUpdateMode.REPLACE, true))
            .assertNext(response -> assertEquals(expectedStatusCode, response.getStatusCode()))
            .expectComplete()
            .verify();

        StepVerifier.create(tableClient.getEntity(partitionKeyValue, rowKeyValue))
            .assertNext(entity -> {
                final Map<String, Object> properties = entity.getProperties();
                assertTrue(properties.containsKey("SubclassProperty"));
                assertEquals("UpdatedValue", properties.get("SubclassProperty"));
            })
            .verifyComplete();
    }*/

    @Test
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
    void listEntitiesWithSelectAsync() {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final TableEntity entity = new TableEntity(partitionKeyValue, rowKeyValue)
            .addProperty("propertyC", "valueC")
            .addProperty("propertyD", "valueD");
        List<String> propertyList = new ArrayList<>();
        propertyList.add("propertyC");
        ListEntitiesOptions options = new ListEntitiesOptions()
            .setSelect(propertyList);
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

    // Will not be supporting subclasses of TableEntity for the time being.
    /*@Test
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
    }*/

    @Test
    void submitTransactionAsync() {
        String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        String rowKeyValue2 = testResourceNamer.randomName("rowKey", 20);
        int expectedBatchStatusCode = 202;
        int expectedOperationStatusCode = 204;

        List<TableTransactionAction> transactionalBatch = new ArrayList<>();
        transactionalBatch.add(new TableTransactionAction(
            TableTransactionActionType.CREATE, new TableEntity(partitionKeyValue, rowKeyValue)));
        transactionalBatch.add(new TableTransactionAction(
            TableTransactionActionType.CREATE, new TableEntity(partitionKeyValue, rowKeyValue2)));

        // Act & Assert
        final Response<TableTransactionResult> result =
            tableClient.submitTransactionWithResponse(transactionalBatch).block(TIMEOUT);

        assertNotNull(result);
        assertEquals(expectedBatchStatusCode, result.getStatusCode());
        assertEquals(transactionalBatch.size(), result.getValue().getTransactionActionResponses().size());
        assertEquals(expectedOperationStatusCode,
            result.getValue().getTransactionActionResponses().get(0).getStatusCode());
        assertEquals(expectedOperationStatusCode,
            result.getValue().getTransactionActionResponses().get(1).getStatusCode());

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
    void submitTransactionAsyncAllActions() {
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

        List<TableTransactionAction> transactionalBatch = new ArrayList<>();
        transactionalBatch.add(new TableTransactionAction(TableTransactionActionType.CREATE,
            new TableEntity(partitionKeyValue, rowKeyValueCreate)));
        transactionalBatch.add(new TableTransactionAction(TableTransactionActionType.UPSERT_MERGE,
            new TableEntity(partitionKeyValue, rowKeyValueUpsertInsert)));
        transactionalBatch.add(new TableTransactionAction(TableTransactionActionType.UPSERT_MERGE, toUpsertMerge));
        transactionalBatch.add(new TableTransactionAction(TableTransactionActionType.UPSERT_REPLACE, toUpsertReplace));
        transactionalBatch.add(new TableTransactionAction(TableTransactionActionType.UPDATE_MERGE, toUpdateMerge));
        transactionalBatch.add(new TableTransactionAction(TableTransactionActionType.UPDATE_REPLACE, toUpdateReplace));
        transactionalBatch.add(new TableTransactionAction(TableTransactionActionType.DELETE,
            new TableEntity(partitionKeyValue, rowKeyValueDelete)));

        // Act & Assert
        StepVerifier.create(tableClient.submitTransactionWithResponse(transactionalBatch))
            .assertNext(response -> {
                assertNotNull(response);
                assertEquals(expectedBatchStatusCode, response.getStatusCode());
                TableTransactionResult result = response.getValue();
                assertEquals(transactionalBatch.size(), result.getTransactionActionResponses().size());
                for (TableTransactionActionResponse subResponse : result.getTransactionActionResponses()) {
                    assertEquals(expectedOperationStatusCode, subResponse.getStatusCode());
                }
            })
            .expectComplete()
            .verify();
    }

    @Test
    void submitTransactionAsyncWithFailingAction() {
        String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        String rowKeyValue2 = testResourceNamer.randomName("rowKey", 20);

        List<TableTransactionAction> transactionalBatch = new ArrayList<>();
        transactionalBatch.add(new TableTransactionAction(TableTransactionActionType.CREATE,
            new TableEntity(partitionKeyValue, rowKeyValue)));
        transactionalBatch.add(new TableTransactionAction(TableTransactionActionType.DELETE,
            new TableEntity(partitionKeyValue, rowKeyValue2)));

        // Act & Assert
        StepVerifier.create(tableClient.submitTransactionWithResponse(transactionalBatch))
            .expectErrorMatches(e -> e instanceof TableTransactionFailedException
                && e.getMessage().contains("An action within the operation failed")
                && e.getMessage().contains("The failed operation was")
                && e.getMessage().contains("DeleteEntity")
                && e.getMessage().contains("partitionKey='" + partitionKeyValue)
                && e.getMessage().contains("rowKey='" + rowKeyValue2))
            .verify();
    }

    @Test
    void submitTransactionAsyncWithSameRowKeys() {
        String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        String rowKeyValue = testResourceNamer.randomName("rowKey", 20);

        List<TableTransactionAction> transactionalBatch = new ArrayList<>();
        transactionalBatch.add(new TableTransactionAction(
            TableTransactionActionType.CREATE, new TableEntity(partitionKeyValue, rowKeyValue)));
        transactionalBatch.add(new TableTransactionAction(
            TableTransactionActionType.CREATE, new TableEntity(partitionKeyValue, rowKeyValue)));

        // Act & Assert
        if (IS_COSMOS_TEST) {
            StepVerifier.create(tableClient.submitTransactionWithResponse(transactionalBatch))
                .expectErrorMatches(e -> e instanceof TableServiceException
                    && e.getMessage().contains("Status code 400")
                    && e.getMessage().contains("InvalidDuplicateRow")
                    && e.getMessage().contains("The batch request contains multiple changes with same row key.")
                    && e.getMessage().contains("An entity can appear only once in a batch request."))
                .verify();
        } else {
            StepVerifier.create(tableClient.submitTransactionWithResponse(transactionalBatch))
                .expectErrorMatches(e -> e instanceof TableTransactionFailedException
                    && e.getMessage().contains("An action within the operation failed")
                    && e.getMessage().contains("The failed operation was")
                    && e.getMessage().contains("CreateEntity")
                    && e.getMessage().contains("partitionKey='" + partitionKeyValue)
                    && e.getMessage().contains("rowKey='" + rowKeyValue))
                .verify();
        }
    }

    @Test
    void submitTransactionAsyncWithDifferentPartitionKeys() {
        String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        String partitionKeyValue2 = testResourceNamer.randomName("partitionKey", 20);
        String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        String rowKeyValue2 = testResourceNamer.randomName("rowKey", 20);

        List<TableTransactionAction> transactionalBatch = new ArrayList<>();
        transactionalBatch.add(new TableTransactionAction(
            TableTransactionActionType.CREATE, new TableEntity(partitionKeyValue, rowKeyValue)));
        transactionalBatch.add(new TableTransactionAction(
            TableTransactionActionType.CREATE, new TableEntity(partitionKeyValue2, rowKeyValue2)));

        // Act & Assert
        if (IS_COSMOS_TEST) {
            // For some reason Cosmos names the first entity's keys while Storage does so with the second entity. It is
            // possible that Cosmos ensures there will be no conflict between a transaction's operations before
            // executing them and Storage executes them without pre-checking for conflicts.
            StepVerifier.create(tableClient.submitTransactionWithResponse(transactionalBatch))
                .expectErrorMatches(e -> e instanceof TableTransactionFailedException
                    && e.getMessage().contains("An action within the operation failed")
                    && e.getMessage().contains("The failed operation was")
                    && e.getMessage().contains("CreateEntity")
                    && e.getMessage().contains("partitionKey='" + partitionKeyValue)
                    && e.getMessage().contains("rowKey='" + rowKeyValue))
                .verify();
        } else {
            StepVerifier.create(tableClient.submitTransactionWithResponse(transactionalBatch))
                .expectErrorMatches(e -> e instanceof TableTransactionFailedException
                    && e.getMessage().contains("An action within the operation failed")
                    && e.getMessage().contains("The failed operation was")
                    && e.getMessage().contains("CreateEntity")
                    && e.getMessage().contains("partitionKey='" + partitionKeyValue2)
                    && e.getMessage().contains("rowKey='" + rowKeyValue2))
                .verify();
        }
    }

    @Test
    public void generateSasTokenWithMinimumParameters() {
        final OffsetDateTime expiryTime = OffsetDateTime.of(2021, 12, 12, 0, 0, 0, 0, ZoneOffset.UTC);
        final TableSasPermission permissions = TableSasPermission.parse("r");
        final TableSasProtocol protocol = TableSasProtocol.HTTPS_ONLY;

        final TableSasSignatureValues sasSignatureValues =
            new TableSasSignatureValues(expiryTime, permissions)
                .setProtocol(protocol)
                .setVersion(TableServiceVersion.V2019_02_02.getVersion());

        final String sas = tableClient.generateSas(sasSignatureValues);

        assertTrue(
            sas.startsWith(
                "sv=2019-02-02"
                    + "&se=2021-12-12T00%3A00%3A00Z"
                    + "&tn=" + tableClient.getTableName()
                    + "&sp=r"
                    + "&spr=https"
                    + "&sig="
            )
        );
    }

    @Test
    public void generateSasTokenWithAllParameters() {
        final OffsetDateTime expiryTime = OffsetDateTime.of(2021, 12, 12, 0, 0, 0, 0, ZoneOffset.UTC);
        final TableSasPermission permissions = TableSasPermission.parse("raud");
        final TableSasProtocol protocol = TableSasProtocol.HTTPS_HTTP;

        final OffsetDateTime startTime = OffsetDateTime.of(2015, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        final TableSasIpRange ipRange = TableSasIpRange.parse("a-b");
        final String startPartitionKey = "startPartitionKey";
        final String startRowKey = "startRowKey";
        final String endPartitionKey = "endPartitionKey";
        final String endRowKey = "endRowKey";

        final TableSasSignatureValues sasSignatureValues =
            new TableSasSignatureValues(expiryTime, permissions)
                .setProtocol(protocol)
                .setVersion(TableServiceVersion.V2019_02_02.getVersion())
                .setStartTime(startTime)
                .setSasIpRange(ipRange)
                .setStartPartitionKey(startPartitionKey)
                .setStartRowKey(startRowKey)
                .setEndPartitionKey(endPartitionKey)
                .setEndRowKey(endRowKey);

        final String sas = tableClient.generateSas(sasSignatureValues);

        assertTrue(
            sas.startsWith(
                "sv=2019-02-02"
                    + "&st=2015-01-01T00%3A00%3A00Z"
                    + "&se=2021-12-12T00%3A00%3A00Z"
                    + "&tn=" + tableClient.getTableName()
                    + "&sp=raud"
                    + "&spk=startPartitionKey"
                    + "&srk=startRowKey"
                    + "&epk=endPartitionKey"
                    + "&erk=endRowKey"
                    + "&sip=a-b"
                    + "&spr=https%2Chttp"
                    + "&sig="
            )
        );
    }

    @Test
    public void canUseSasTokenToCreateValidTableClient() {
        Assumptions.assumeFalse(IS_COSMOS_TEST, "SAS Tokens are not supported for Cosmos endpoints.");

        final OffsetDateTime expiryTime = OffsetDateTime.of(2021, 12, 12, 0, 0, 0, 0, ZoneOffset.UTC);
        final TableSasPermission permissions = TableSasPermission.parse("a");
        final TableSasProtocol protocol = TableSasProtocol.HTTPS_HTTP;

        final TableSasSignatureValues sasSignatureValues =
            new TableSasSignatureValues(expiryTime, permissions)
                .setProtocol(protocol)
                .setVersion(TableServiceVersion.V2019_02_02.getVersion());

        final String sas = tableClient.generateSas(sasSignatureValues);

        final TableClientBuilder tableClientBuilder = new TableClientBuilder()
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .endpoint(tableClient.getTableEndpoint())
            .sasToken(sas)
            .tableName(tableClient.getTableName());

        if (interceptorManager.isPlaybackMode()) {
            tableClientBuilder.httpClient(playbackClient);
        } else {
            tableClientBuilder.httpClient(DEFAULT_HTTP_CLIENT);

            if (!interceptorManager.isLiveMode()) {
                tableClientBuilder.addPolicy(recordPolicy);
            }

            tableClientBuilder.addPolicy(new RetryPolicy(new ExponentialBackoff(6, Duration.ofMillis(1500),
                Duration.ofSeconds(100))));
        }

        // Create a new client authenticated with the SAS token.
        final TableAsyncClient tableAsyncClient = tableClientBuilder.buildAsyncClient();
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final TableEntity entity = new TableEntity(partitionKeyValue, rowKeyValue);
        final int expectedStatusCode = 204;

        StepVerifier.create(tableAsyncClient.createEntityWithResponse(entity))
            .assertNext(response -> assertEquals(expectedStatusCode, response.getStatusCode()))
            .expectComplete()
            .verify();
    }

    @Test
    public void setAndListAccessPolicies() {
        Assumptions.assumeFalse(IS_COSMOS_TEST,
            "Setting and listing access policies is not supported on Cosmos endpoints.");

        OffsetDateTime startTime = OffsetDateTime.of(2021, 12, 12, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime expiryTime = OffsetDateTime.of(2022, 12, 12, 0, 0, 0, 0, ZoneOffset.UTC);
        String permissions = "r";
        TableAccessPolicy tableAccessPolicy = new TableAccessPolicy()
            .setStartsOn(startTime)
            .setExpiresOn(expiryTime)
            .setPermissions(permissions);
        String id = "testPolicy";
        TableSignedIdentifier tableSignedIdentifier = new TableSignedIdentifier(id).setAccessPolicy(tableAccessPolicy);

        StepVerifier.create(tableClient.setAccessPoliciesWithResponse(Collections.singletonList(tableSignedIdentifier)))
            .assertNext(response -> assertEquals(204, response.getStatusCode()))
            .expectComplete()
            .verify();

        StepVerifier.create(tableClient.getAccessPolicies())
            .assertNext(tableAccessPolicies -> {
                assertNotNull(tableAccessPolicies);
                assertNotNull(tableAccessPolicies.getIdentifiers());

                TableSignedIdentifier signedIdentifier = tableAccessPolicies.getIdentifiers().get(0);

                assertNotNull(signedIdentifier);

                TableAccessPolicy accessPolicy = signedIdentifier.getAccessPolicy();

                assertNotNull(accessPolicy);
                assertEquals(startTime, accessPolicy.getStartsOn());
                assertEquals(expiryTime, accessPolicy.getExpiresOn());
                assertEquals(permissions, accessPolicy.getPermissions());
                assertEquals(id, signedIdentifier.getId());
            })
            .expectComplete()
            .verify();
    }

    @Test
    public void setAndListMultipleAccessPolicies() {
        Assumptions.assumeFalse(IS_COSMOS_TEST,
            "Setting and listing access policies is not supported on Cosmos endpoints");

        OffsetDateTime startTime = OffsetDateTime.of(2021, 12, 12, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime expiryTime = OffsetDateTime.of(2022, 12, 12, 0, 0, 0, 0, ZoneOffset.UTC);
        String permissions = "r";
        TableAccessPolicy tableAccessPolicy = new TableAccessPolicy()
            .setStartsOn(startTime)
            .setExpiresOn(expiryTime)
            .setPermissions(permissions);
        String id1 = "testPolicy1";
        String id2 = "testPolicy2";
        List<TableSignedIdentifier> tableSignedIdentifiers = new ArrayList<>();
        tableSignedIdentifiers.add(new TableSignedIdentifier(id1).setAccessPolicy(tableAccessPolicy));
        tableSignedIdentifiers.add(new TableSignedIdentifier(id2).setAccessPolicy(tableAccessPolicy));

        StepVerifier.create(tableClient.setAccessPoliciesWithResponse(tableSignedIdentifiers))
            .assertNext(response -> assertEquals(204, response.getStatusCode()))
            .expectComplete()
            .verify();

        StepVerifier.create(tableClient.getAccessPolicies())
            .assertNext(tableAccessPolicies -> {
                assertNotNull(tableAccessPolicies);
                assertNotNull(tableAccessPolicies.getIdentifiers());

                assertEquals(2, tableAccessPolicies.getIdentifiers().size());
                assertEquals(id1, tableAccessPolicies.getIdentifiers().get(0).getId());
                assertEquals(id2, tableAccessPolicies.getIdentifiers().get(1).getId());

                for (TableSignedIdentifier signedIdentifier : tableAccessPolicies.getIdentifiers()) {
                    assertNotNull(signedIdentifier);

                    TableAccessPolicy accessPolicy = signedIdentifier.getAccessPolicy();

                    assertNotNull(accessPolicy);
                    assertEquals(startTime, accessPolicy.getStartsOn());
                    assertEquals(expiryTime, accessPolicy.getExpiresOn());
                    assertEquals(permissions, accessPolicy.getPermissions());
                }
            })
            .expectComplete()
            .verify();
    }
}
