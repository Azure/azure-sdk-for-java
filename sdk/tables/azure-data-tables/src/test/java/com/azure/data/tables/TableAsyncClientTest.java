// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.ExponentialBackoff;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.test.utils.TestResourceNamer;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.data.tables.models.ListEntitiesOptions;
import com.azure.data.tables.models.TableAccessPolicy;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableEntityUpdateMode;
import com.azure.data.tables.models.TableServiceException;
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
import com.azure.identity.ClientSecretCredentialBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
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
@Execution(ExecutionMode.SAME_THREAD)
public class TableAsyncClientTest extends TableClientTestBase {
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(100);

    private TableAsyncClient tableClient;

    protected HttpClient buildAssertingClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient)
            .skipRequest((ignored1, ignored2) -> false)
            .assertAsync()
            .build();
    }

    protected void beforeTest() {
        final String tableName = testResourceNamer.randomName("tableName", 20);
        final String connectionString = TestUtils.getConnectionString(interceptorManager.isPlaybackMode());
        tableClient = getClientBuilder(tableName, connectionString).buildAsyncClient();

        tableClient.createTable().block(DEFAULT_TIMEOUT);
    }

    @Test
    public void createTable() {
        // Arrange
        final String tableName2 = testResourceNamer.randomName("tableName", 20);
        final String connectionString = TestUtils.getConnectionString(interceptorManager.isPlaybackMode());
        final TableAsyncClient tableClient2 = getClientBuilder(tableName2, connectionString).buildAsyncClient();

        // Act & Assert
        StepVerifier.create(tableClient2.createTable())
            .assertNext(Assertions::assertNotNull)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    /**
     * Tests that a table and entity can be created while having a different tenant ID than the one that will be
     * provided in the authentication challenge.
     */
    @LiveOnly
    @Test
    public void createTableWithMultipleTenants() {
        // This feature works only in Storage endpoints with service version 2020_12_06.
        Assumptions.assumeTrue(tableClient.getTableEndpoint().contains("core.windows.net")
            && tableClient.getServiceVersion() == TableServiceVersion.V2020_12_06);

        // Arrange
        final String tableName2 = testResourceNamer.randomName("tableName", 20);

        TokenCredential credential = null;
        if (interceptorManager.isPlaybackMode()) {
            credential = new MockTokenCredential();
        } else {
        // The tenant ID does not matter as the correct on will be extracted from the authentication challenge in
        // contained in the response the server provides to a first "naive" unauthenticated request.
            credential = new ClientSecretCredentialBuilder()
                .clientId(Configuration.getGlobalConfiguration().get("TABLES_CLIENT_ID", "clientId"))
                .clientSecret(Configuration.getGlobalConfiguration().get("TABLES_CLIENT_SECRET", "clientSecret"))
                .tenantId(testResourceNamer.randomUuid())
                .additionallyAllowedTenants("*")
                .build();
        }

        final TableAsyncClient tableClient2 =
            getClientBuilder(tableName2, Configuration.getGlobalConfiguration().get("TABLES_ENDPOINT",
                "https://tablestests.table.core.windows.com"), credential, true).buildAsyncClient();

        // Act & Assert
        // This request will use the tenant ID extracted from the previous request.
        StepVerifier.create(tableClient2.createTable())
            .assertNext(Assertions::assertNotNull)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final TableEntity tableEntity = new TableEntity(partitionKeyValue, rowKeyValue);

        // All other requests will also use the tenant ID obtained from the auth challenge.
        StepVerifier.create(tableClient2.createEntity(tableEntity))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void createTableWithResponse() {
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
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void createEntity() {
        createEntityImpl("partitionKey", "rowKey");
    }

    @Test
    public void createEntityWithSingleQuotesInPartitionKey() {
        createEntityImpl("partition'Key", "rowKey");
    }

    @Test
    public void createEntityWithSingleQuotesInRowKey() {
        createEntityImpl("partitionKey", "row'Key");
    }

    private void createEntityImpl(String partitionKeyPrefix, String rowKeyPrefix) {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName(partitionKeyPrefix, 20);
        final String rowKeyValue = testResourceNamer.randomName(rowKeyPrefix, 20);
        final TableEntity tableEntity = new TableEntity(partitionKeyValue, rowKeyValue);

        // Act & Assert
        StepVerifier.create(tableClient.createEntity(tableEntity))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void createEntityWithResponse() {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final TableEntity entity = new TableEntity(partitionKeyValue, rowKeyValue);
        final int expectedStatusCode = 204;

        // Act & Assert
        StepVerifier.create(tableClient.createEntityWithResponse(entity))
            .assertNext(response -> assertEquals(expectedStatusCode, response.getStatusCode()))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void createEntityWithAllSupportedDataTypes() {
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

        tableClient.createEntity(tableEntity).block(DEFAULT_TIMEOUT);

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
            .verify(DEFAULT_TIMEOUT);
    }

    // Support for subclassing TableEntity was removed for the time being, although having it back is not 100%
    // discarded. -vicolina
    /*@Test
    public void createEntitySubclass() {
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
            .verify(DEFAULT_TIMEOUT);
    }*/

    @Test
    public void deleteTable() {
        // Act & Assert
        StepVerifier.create(tableClient.deleteTable())
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void deleteNonExistingTable() {
        // Act & Assert
        tableClient.deleteTable().block(DEFAULT_TIMEOUT);

        StepVerifier.create(tableClient.deleteTable())
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void deleteTableWithResponse() {
        // Arrange
        final int expectedStatusCode = 204;

        // Act & Assert
        StepVerifier.create(tableClient.deleteTableWithResponse())
            .assertNext(response -> {
                assertEquals(expectedStatusCode, response.getStatusCode());
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void deleteNonExistingTableWithResponse() {
        // Arrange
        final int expectedStatusCode = 404;

        // Act & Assert
        tableClient.deleteTableWithResponse().block(DEFAULT_TIMEOUT);

        StepVerifier.create(tableClient.deleteTableWithResponse())
            .assertNext(response -> assertEquals(expectedStatusCode, response.getStatusCode()))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void deleteEntity() {
        deleteEntityImpl("partitionKey", "rowKey");
    }

    @Test
    public void deleteEntityWithSingleQuotesInPartitionKey() {
        deleteEntityImpl("partition'Key", "rowKey");
    }

    @Test
    public void deleteEntityWithSingleQuotesInRowKey() {
        deleteEntityImpl("partitionKey", "row'Key");
    }

    private void deleteEntityImpl(String partitionKeyPrefix, String rowKeyPrefix) {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName(partitionKeyPrefix, 20);
        final String rowKeyValue = testResourceNamer.randomName(rowKeyPrefix, 20);
        final TableEntity tableEntity = new TableEntity(partitionKeyValue, rowKeyValue);

        tableClient.createEntity(tableEntity).block(DEFAULT_TIMEOUT);
        final TableEntity createdEntity = tableClient.getEntity(partitionKeyValue, rowKeyValue).block(DEFAULT_TIMEOUT);
        assertNotNull(createdEntity, "'createdEntity' should not be null.");
        assertNotNull(createdEntity.getETag(), "'eTag' should not be null.");

        // Act & Assert
        StepVerifier.create(tableClient.deleteEntity(partitionKeyValue, rowKeyValue))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void deleteNonExistingEntity() {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);

        // Act & Assert
        StepVerifier.create(tableClient.deleteEntity(partitionKeyValue, rowKeyValue))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void deleteEntityWithResponse() {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final TableEntity tableEntity = new TableEntity(partitionKeyValue, rowKeyValue);
        final int expectedStatusCode = 204;

        tableClient.createEntity(tableEntity).block(DEFAULT_TIMEOUT);
        final TableEntity createdEntity = tableClient.getEntity(partitionKeyValue, rowKeyValue).block(DEFAULT_TIMEOUT);
        assertNotNull(createdEntity, "'createdEntity' should not be null.");
        assertNotNull(createdEntity.getETag(), "'eTag' should not be null.");

        // Act & Assert
        StepVerifier.create(tableClient.deleteEntityWithResponse(createdEntity, false))
            .assertNext(response -> assertEquals(expectedStatusCode, response.getStatusCode()))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void deleteNonExistingEntityWithResponse() {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final TableEntity entity = new TableEntity(partitionKeyValue, rowKeyValue);
        final int expectedStatusCode = 404;

        // Act & Assert
        StepVerifier.create(tableClient.deleteEntityWithResponse(entity, false))
            .assertNext(response -> assertEquals(expectedStatusCode, response.getStatusCode()))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void deleteEntityWithResponseMatchETag() {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final TableEntity tableEntity = new TableEntity(partitionKeyValue, rowKeyValue);
        final int expectedStatusCode = 204;

        tableClient.createEntity(tableEntity).block(DEFAULT_TIMEOUT);
        final TableEntity createdEntity = tableClient.getEntity(partitionKeyValue, rowKeyValue).block(DEFAULT_TIMEOUT);
        assertNotNull(createdEntity, "'createdEntity' should not be null.");
        assertNotNull(createdEntity.getETag(), "'eTag' should not be null.");

        // Act & Assert
        StepVerifier.create(tableClient.deleteEntityWithResponse(createdEntity, true))
            .assertNext(response -> assertEquals(expectedStatusCode, response.getStatusCode()))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void getEntityWithSingleQuotesInPartitionKey() {
        getEntityWithResponseAsyncImpl(this.tableClient, this.testResourceNamer, "partition'Key", "rowKey");
    }

    @Test
    public void getEntityWithSingleQuotesInRowKey() {
        getEntityWithResponseAsyncImpl(this.tableClient, this.testResourceNamer, "partitionKey", "row'Key");
    }

    @Test
    public void getEntityWithResponse() {
        getEntityWithResponseAsyncImpl(this.tableClient, this.testResourceNamer, "partitionKey", "rowKey");
    }

    static void getEntityWithResponseAsyncImpl(TableAsyncClient tableClient, TestResourceNamer testResourceNamer,
                                               String partitionKeyPrefix, String rowKeyPrefix) {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName(partitionKeyPrefix, 20);
        final String rowKeyValue = testResourceNamer.randomName(rowKeyPrefix, 20);
        final TableEntity tableEntity = new TableEntity(partitionKeyValue, rowKeyValue);
        final int expectedStatusCode = 200;
        tableClient.createEntity(tableEntity).block(DEFAULT_TIMEOUT);

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
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void getEntityWithResponseWithSelect() {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final TableEntity tableEntity = new TableEntity(partitionKeyValue, rowKeyValue);
        tableEntity.addProperty("Test", "Value");
        final int expectedStatusCode = 200;
        tableClient.createEntity(tableEntity).block(DEFAULT_TIMEOUT);
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
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void updateEntityWithSingleQuotesInPartitionKey() {
        updateEntityWithResponseAsync(TableEntityUpdateMode.REPLACE, "partition'Key", "rowKey");
    }

    @Test
    public void updateEntityWithSingleQuotesInRowKey() {
        updateEntityWithResponseAsync(TableEntityUpdateMode.REPLACE, "partitionKey", "row'Key");
    }

    // Support for subclassing TableEntity was removed for the time being, although having it back is not 100%
    // discarded. -vicolina
    /*@Test
    public void getEntityWithResponseSubclass() {
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
            .verify(DEFAULT_TIMEOUT);
    }*/

    @Test
    public void updateEntityWithResponseReplace() {
        updateEntityWithResponseAsync(TableEntityUpdateMode.REPLACE, "partitionKey", "rowKey");
    }

    @Test
    public void updateEntityWithResponseMerge() {
        updateEntityWithResponseAsync(TableEntityUpdateMode.MERGE, "partitionKey", "rowKey");
    }

    /**
     * In the case of {@link TableEntityUpdateMode#MERGE}, we expect both properties to exist.
     * In the case of {@link TableEntityUpdateMode#REPLACE}, we only expect {@code newPropertyKey} to exist.
     */
    void updateEntityWithResponseAsync(TableEntityUpdateMode mode, String partitionKeyPrefix, String rowKeyPrefix) {
        // Arrange
        final boolean expectOldProperty = mode == TableEntityUpdateMode.MERGE;
        final String partitionKeyValue = testResourceNamer.randomName(partitionKeyPrefix, 20);
        final String rowKeyValue = testResourceNamer.randomName(rowKeyPrefix, 20);
        final int expectedStatusCode = 204;
        final String oldPropertyKey = "propertyA";
        final String newPropertyKey = "propertyB";
        final TableEntity tableEntity = new TableEntity(partitionKeyValue, rowKeyValue)
            .addProperty(oldPropertyKey, "valueA");

        tableClient.createEntity(tableEntity).block(DEFAULT_TIMEOUT);
        final TableEntity createdEntity = tableClient.getEntity(partitionKeyValue, rowKeyValue).block(DEFAULT_TIMEOUT);
        assertNotNull(createdEntity, "'createdEntity' should not be null.");
        assertNotNull(createdEntity.getETag(), "'eTag' should not be null.");

        createdEntity.getProperties().remove(oldPropertyKey);
        createdEntity.addProperty(newPropertyKey, "valueB");

        // Act & Assert
        StepVerifier.create(tableClient.updateEntityWithResponse(createdEntity, mode, true))
            .assertNext(response -> assertEquals(expectedStatusCode, response.getStatusCode()))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        // Assert and verify that the new properties are in there.
        StepVerifier.create(tableClient.getEntity(partitionKeyValue, rowKeyValue))
            .assertNext(entity -> {
                final Map<String, Object> properties = entity.getProperties();
                assertTrue(properties.containsKey(newPropertyKey));
                assertEquals(expectOldProperty, properties.containsKey(oldPropertyKey));
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Support for subclassing TableEntity was removed for the time being, although having it back is not 100%
    // discarded. -vicolina
    /*@Test
    public void updateEntityWithResponseSubclass() {
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
            .verify(DEFAULT_TIMEOUT);

        StepVerifier.create(tableClient.getEntity(partitionKeyValue, rowKeyValue))
            .assertNext(entity -> {
                final Map<String, Object> properties = entity.getProperties();
                assertTrue(properties.containsKey("SubclassProperty"));
                assertEquals("UpdatedValue", properties.get("SubclassProperty"));
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }*/

    @Test
    public void listEntities() {
        listEntitiesImpl("partitionKey", "rowKey");
    }

    @Test
    public void listEntitiesWithSingleQuotesInPartitionKey() {
        listEntitiesImpl("partition'Key", "rowKey");
    }

    @Test
    public void listEntitiesWithSingleQuotesInRowKey() {
        listEntitiesImpl("partitionKey", "row'Key");
    }

    private void listEntitiesImpl(String partitionKeyPrefix, String rowKeyPrefix) {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName(partitionKeyPrefix, 20);
        final String rowKeyValue = testResourceNamer.randomName(rowKeyPrefix, 20);
        final String rowKeyValue2 = testResourceNamer.randomName(rowKeyPrefix, 20);
        tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValue)).block(DEFAULT_TIMEOUT);
        tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValue2)).block(DEFAULT_TIMEOUT);

        // Act & Assert
        StepVerifier.create(tableClient.listEntities())
            .expectNextCount(2)
            .thenConsumeWhile(x -> true)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void listEntitiesWithFilter() {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final String rowKeyValue2 = testResourceNamer.randomName("rowKey", 20);
        ListEntitiesOptions options = new ListEntitiesOptions().setFilter("RowKey eq '" + rowKeyValue + "'");
        tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValue)).block(DEFAULT_TIMEOUT);
        tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValue2)).block(DEFAULT_TIMEOUT);

        // Act & Assert
        StepVerifier.create(tableClient.listEntities(options))
            .assertNext(returnEntity -> {
                assertEquals(partitionKeyValue, returnEntity.getPartitionKey());
                assertEquals(rowKeyValue, returnEntity.getRowKey());
            })
            .expectNextCount(0)
            .thenConsumeWhile(x -> true)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void listEntitiesWithSelect() {
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
        tableClient.createEntity(entity).block(DEFAULT_TIMEOUT);

        // Act & Assert
        StepVerifier.create(tableClient.listEntities(options))
            .assertNext(returnEntity -> {
                assertNull(returnEntity.getRowKey());
                assertNull(returnEntity.getPartitionKey());
                assertEquals("valueC", returnEntity.getProperties().get("propertyC"));
                assertNull(returnEntity.getProperties().get("propertyD"));
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void listEntitiesWithTop() {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final String rowKeyValue2 = testResourceNamer.randomName("rowKey", 20);
        final String rowKeyValue3 = testResourceNamer.randomName("rowKey", 20);
        ListEntitiesOptions options = new ListEntitiesOptions().setTop(2);
        tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValue)).block(DEFAULT_TIMEOUT);
        tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValue2)).block(DEFAULT_TIMEOUT);
        tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValue3)).block(DEFAULT_TIMEOUT);

        // Act & Assert
        StepVerifier.create(tableClient.listEntities(options))
            .expectNextCount(2)
            .thenConsumeWhile(x -> true)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Support for subclassing TableEntity was removed for the time being, although having it back is not 100%
    // discarded. -vicolina
    /*@Test
    public void listEntitiesSubclass() {
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
            .verify(DEFAULT_TIMEOUT);
    }*/

    @Test
    public void submitTransaction() {
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
            tableClient.submitTransactionWithResponse(transactionalBatch).block(DEFAULT_TIMEOUT);

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
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void submitTransactionAllActions() {
        submitTransactionAllActionsImpl("partitionKey", "rowKey");
    }

    @Test
    public void submitTransactionAllActionsForEntitiesWithSingleQuotesInPartitionKey() {
        submitTransactionAllActionsImpl("partition'Key", "rowKey");
    }

    @Test
    public void submitTransactionAllActionsForEntitiesWithSingleQuotesInRowKey() {
        submitTransactionAllActionsImpl("partitionKey", "row'Key");
    }

    private void submitTransactionAllActionsImpl(String partitionKeyPrefix, String rowKeyPrefix) {
        String partitionKeyValue = testResourceNamer.randomName(partitionKeyPrefix, 20);
        String rowKeyValueCreate = testResourceNamer.randomName(rowKeyPrefix, 20);
        String rowKeyValueUpsertInsert = testResourceNamer.randomName(rowKeyPrefix, 20);
        String rowKeyValueUpsertMerge = testResourceNamer.randomName(rowKeyPrefix, 20);
        String rowKeyValueUpsertReplace = testResourceNamer.randomName(rowKeyPrefix, 20);
        String rowKeyValueUpdateMerge = testResourceNamer.randomName(rowKeyPrefix, 20);
        String rowKeyValueUpdateReplace = testResourceNamer.randomName(rowKeyPrefix, 20);
        String rowKeyValueDelete = testResourceNamer.randomName(rowKeyPrefix, 20);

        int expectedBatchStatusCode = 202;
        int expectedOperationStatusCode = 204;

        tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValueUpsertMerge)).block(DEFAULT_TIMEOUT);
        tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValueUpsertReplace)).block(DEFAULT_TIMEOUT);
        tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValueUpdateMerge)).block(DEFAULT_TIMEOUT);
        tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValueUpdateReplace)).block(DEFAULT_TIMEOUT);
        tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValueDelete)).block(DEFAULT_TIMEOUT);

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
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void submitTransactionWithFailingAction() {
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
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void submitTransactionWithSameRowKeys() {
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
                .verify(DEFAULT_TIMEOUT);
        } else {
            StepVerifier.create(tableClient.submitTransactionWithResponse(transactionalBatch))
                .expectErrorMatches(e -> e instanceof TableTransactionFailedException
                    && e.getMessage().contains("An action within the operation failed")
                    && e.getMessage().contains("The failed operation was")
                    && e.getMessage().contains("CreateEntity")
                    && e.getMessage().contains("partitionKey='" + partitionKeyValue)
                    && e.getMessage().contains("rowKey='" + rowKeyValue))
                .verify(DEFAULT_TIMEOUT);
        }
    }

    @Test
    public void submitTransactionWithDifferentPartitionKeys() {
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
                .verify(DEFAULT_TIMEOUT);
        } else {
            StepVerifier.create(tableClient.submitTransactionWithResponse(transactionalBatch))
                .expectErrorMatches(e -> e instanceof TableTransactionFailedException
                    && e.getMessage().contains("An action within the operation failed")
                    && e.getMessage().contains("The failed operation was")
                    && e.getMessage().contains("CreateEntity")
                    && e.getMessage().contains("partitionKey='" + partitionKeyValue2)
                    && e.getMessage().contains("rowKey='" + rowKeyValue2))
                .verify(DEFAULT_TIMEOUT);
        }
    }

    @LiveOnly
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

    @LiveOnly
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

    @LiveOnly
    @Test
    public void canUseSasTokenToCreateValidTableClient() {
        // SAS tokens at the table level have not been working with Cosmos endpoints.
        // TODO: Will re-enable once the above is fixed. -vicolina
        Assumptions.assumeFalse(IS_COSMOS_TEST, "Skipping Cosmos test.");


        final OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
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
            .verify(DEFAULT_TIMEOUT);
    }

    @LiveOnly
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
            .verify(DEFAULT_TIMEOUT);

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
            .verify(DEFAULT_TIMEOUT);
    }

    @LiveOnly
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
            .verify(DEFAULT_TIMEOUT);

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
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void allowsCreationOfEntityWithEmptyStringPrimaryKey() {
        Assumptions.assumeFalse(IS_COSMOS_TEST,
            "Empty row or partition keys are not supported on Cosmos endpoints.");
        String entityName = testResourceNamer.randomName("name", 10);
        TableEntity entity = new TableEntity("", "");
        entity.addProperty("Name", entityName);
        StepVerifier.create(tableClient.createEntityWithResponse(entity))
            .assertNext(response -> assertEquals(204, response.getStatusCode()))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void allowListEntitiesWithEmptyPrimaryKey() {
        Assumptions.assumeFalse(IS_COSMOS_TEST,
            "Empty row or partition keys are not supported on Cosmos endpoints.");
        TableEntity entity = new TableEntity("", "");
        String entityName = testResourceNamer.randomName("name", 10);
        entity.addProperty("Name", entityName);
        tableClient.createEntity(entity).block(DEFAULT_TIMEOUT);
        ListEntitiesOptions options = new ListEntitiesOptions();
        options.setFilter("PartitionKey eq '' and RowKey eq ''");
        StepVerifier.create(tableClient.listEntities(options))
            .assertNext(en -> assertEquals(entityName, en.getProperties().get("Name")))
            .expectNextCount(0)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // tests that you can delete a table entity with an empty string partition key and empty string row key
    @Test
    public void allowDeleteEntityWithEmptyPrimaryKey() {
        Assumptions.assumeFalse(IS_COSMOS_TEST,
            "Empty row or partition keys are not supported on Cosmos endpoints.");
        TableEntity entity = new TableEntity("", "");
        String entityName = testResourceNamer.randomName("name", 10);
        entity.addProperty("Name", entityName);
        tableClient.createEntity(entity).block(DEFAULT_TIMEOUT);
        StepVerifier.create(tableClient.deleteEntityWithResponse("", "", "*", false, null))
            .assertNext(response -> assertEquals(204, response.getStatusCode()))
            .expectComplete()
            .verify();
    }

    /**
     * Create an entity with a property for each supported type and verify that getProperty returns the correct type for each.
     */
    @Disabled("This test is disabled because it is failing in playback mode. It is not clear why it is failing.")
    @Test
    public void createEntityWithAllSupportedTypes() {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final byte[] bytes = new byte[]{4, 5, 6};
        final boolean b = true;
        final OffsetDateTime dateTime = OffsetDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        final double d = 1.23D;
        final UUID uuid = UUID.fromString("11111111-2222-3333-4444-555555555555");
        final int i = 123;
        final long l = 123L;
        final String s = "Test";

        final TableEntity tableEntity = new TableEntity(partitionKeyValue, rowKeyValue)
            .addProperty("byteField", bytes)
            .addProperty("booleanField", b)
            .addProperty("dateTimeField", dateTime)
            .addProperty("doubleField", d)
            .addProperty("uuidField", uuid)
            .addProperty("intField", i)
            .addProperty("longField", l)
            .addProperty("stringField", s);

        // Act
        tableClient.createEntity(tableEntity).block(DEFAULT_TIMEOUT);

        // Assert
        final TableEntity retrievedEntity = tableClient.getEntity(partitionKeyValue, rowKeyValue).block(DEFAULT_TIMEOUT);

        Assertions.assertArrayEquals(bytes, (byte[]) retrievedEntity.getProperties().get("byteField"));
        assertEquals(b, (boolean) retrievedEntity.getProperties().get("booleanField"));
        assertTrue(dateTime.isEqual((OffsetDateTime) retrievedEntity.getProperties().get("dateTimeField")));
        assertEquals(d, (double) retrievedEntity.getProperties().get("doubleField"));
        assertEquals(0, uuid.compareTo((UUID) retrievedEntity.getProperties().get("uuidField")));
        assertEquals(i, (int) retrievedEntity.getProperties().get("intField"));
        assertEquals(l, (long) retrievedEntity.getProperties().get("longField"));
        assertEquals(s, (String) retrievedEntity.getProperties().get("stringField"));
    }

    /**
     * Create an entity with a property for each supported type, retrieve the entity using listEntities, and verify that getProperty returns the correct type for each.
     */
    @Test
    public void listEntitiesWithAllSupportedTypes() {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final BinaryData binaryData = BinaryData.fromString("This is string bytes.");
        final byte[] bytes = new byte[]{4, 5, 6, 7};
        final boolean b = true;
        final OffsetDateTime dateTime = OffsetDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        final double d = 1.23D;
        final UUID uuid = UUID.fromString("11111111-2222-3333-4444-555555555555");
        final int i = 123;
        final long l = 123L;
        final String s = "Test";

        final TableEntity tableEntity = new TableEntity(partitionKeyValue, rowKeyValue)
            .addProperty("binaryField", binaryData.toBytes())
            .addProperty("byteField", bytes)
            .addProperty("booleanField", b)
            .addProperty("dateTimeField", dateTime)
            .addProperty("doubleField", d)
            .addProperty("uuidField", uuid)
            .addProperty("intField", i)
            .addProperty("longField", l)
            .addProperty("stringField", s);

        tableClient.createEntity(tableEntity).block(DEFAULT_TIMEOUT);

        // Act
        StepVerifier.create(tableClient.listEntities())
            //.expectNextCount(1)
            .assertNext(returnEntity -> {
                //assertEquals(binaryData, (BinaryData) returnEntity.getProperties().get("binaryField"));
                Assertions.assertArrayEquals(bytes, (byte[]) returnEntity.getProperties().get("byteField"));
                assertEquals(b, (boolean) returnEntity.getProperties().get("booleanField"));
                assertTrue(dateTime.isEqual((OffsetDateTime) returnEntity.getProperties().get("dateTimeField")));
                assertEquals(d, (double) returnEntity.getProperties().get("doubleField"));
                assertEquals(0, uuid.compareTo((UUID) returnEntity.getProperties().get("uuidField")));
                assertEquals(i, (int) returnEntity.getProperties().get("intField"));
                assertEquals(l, (long) returnEntity.getProperties().get("longField"));
                assertEquals(s, (String) returnEntity.getProperties().get("stringField"));
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    /**
     * Create an entity with all supported types, and verify that both listEntities and getEntity return the correct and same type for each.
     */
    @Test
    public void listAndGetEntitiesWithAllSupportedTypes() {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        // final BinaryData binaryData = BinaryData.fromString("This is string bytes.");
        final byte[] bytes = new byte[]{4, 5, 6, 7};
        final boolean b = true;
        final OffsetDateTime dateTime = OffsetDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        final double d = 1.23D;
        final UUID uuid = UUID.fromString("11111111-2222-3333-4444-555555555555");
        final int i = 123;
        final long l = 123L;
        final String s = "Test";

        final TableEntity tableEntity = new TableEntity(partitionKeyValue, rowKeyValue)
            //.addProperty("binaryField", binaryData.toBytes())
            .addProperty("byteField", bytes)
            .addProperty("booleanField", b)
            .addProperty("dateTimeField", dateTime)
            .addProperty("doubleField", d)
            .addProperty("uuidField", uuid)
            .addProperty("intField", i)
            .addProperty("longField", l)
            .addProperty("stringField", s);

        tableClient.createEntity(tableEntity).block(DEFAULT_TIMEOUT);

        // Act
        final TableEntity retrievedEntity = tableClient.getEntity(partitionKeyValue, rowKeyValue).block(DEFAULT_TIMEOUT);
        final Iterator<TableEntity> iterator = tableClient.listEntities().toIterable().iterator();
        assertTrue(iterator.hasNext());

        final TableEntity listedEntity = iterator.next();

        // Assert
        //assertEquals(binaryData, (BinaryData) retrievedEntity.getProperties().get("binaryField"));
        //assertEquals(binaryData, (BinaryData) listedEntity.getProperties().get("binaryField"));

        Assertions.assertArrayEquals(bytes, (byte[]) retrievedEntity.getProperties().get("byteField"));
        Assertions.assertArrayEquals(bytes, (byte[]) listedEntity.getProperties().get("byteField"));

        assertEquals(b, (boolean) retrievedEntity.getProperties().get("booleanField"));
        assertEquals(b, (boolean) listedEntity.getProperties().get("booleanField"));

        assertTrue(dateTime.isEqual((OffsetDateTime) retrievedEntity.getProperties().get("dateTimeField")));
        assertTrue(dateTime.isEqual((OffsetDateTime) listedEntity.getProperties().get("dateTimeField")));

        assertEquals(d, (double) retrievedEntity.getProperties().get("doubleField"));
        assertEquals(d, (double) listedEntity.getProperties().get("doubleField"));

        assertEquals(0, uuid.compareTo((UUID) retrievedEntity.getProperties().get("uuidField")));
        assertEquals(0, uuid.compareTo((UUID) listedEntity.getProperties().get("uuidField")));

        assertEquals(i, (int) retrievedEntity.getProperties().get("intField"));
        assertEquals(i, (int) listedEntity.getProperties().get("intField"));

        assertEquals(l, (long) retrievedEntity.getProperties().get("longField"));
        assertEquals(l, (long) listedEntity.getProperties().get("longField"));

        assertEquals(s, (String) retrievedEntity.getProperties().get("stringField"));
        assertEquals(s, (String) listedEntity.getProperties().get("stringField"));

    }

}
