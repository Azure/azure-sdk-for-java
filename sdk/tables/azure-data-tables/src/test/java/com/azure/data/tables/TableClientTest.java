// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.ExponentialBackoff;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.test.utils.TestResourceNamer;
import com.azure.core.util.Configuration;
import com.azure.data.tables.models.ListEntitiesOptions;
import com.azure.data.tables.models.TableAccessPolicies;
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
import org.junit.jupiter.api.Test;

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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests {@link TableClient}.
 */
public class TableClientTest extends TableClientTestBase {
    private TableClient tableClient;

    protected HttpClient buildAssertingClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient)
            .skipRequest((ignored1, ignored2) -> false)
            .assertSync()
            .build();
    }

    protected void beforeTest() {
        final String tableName = testResourceNamer.randomName("tableName", 20);
        final String connectionString = TestUtils.getConnectionString(interceptorManager.isPlaybackMode());
        tableClient = getClientBuilder(tableName, connectionString).buildClient();
        tableClient.createTable();
    }

    protected void afterTest() {
        tableClient.deleteTable();
    }

    @Test
    public void createTable() {
        // Arrange
        final String tableName2 = testResourceNamer.randomName("tableName", 20);
        final String connectionString = TestUtils.getConnectionString(interceptorManager.isPlaybackMode());
        final TableClient tableClient2 = getClientBuilder(tableName2, connectionString).buildClient();

        // Act & Assert
        assertNotNull(tableClient2.createTable());
    }

    /**
     * Tests that a table and entity can be created while having a different tenant ID than the one that will be
     * provided in the authentication challenge.
     */
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

        final TableClient tableClient2 =
            getClientBuilder(tableName2, Configuration.getGlobalConfiguration().get("TABLES_ENDPOINT",
                "https://tablestests.table.core.windows.com"), credential, true).buildClient();
        // Act & Assert
        // This request will use the tenant ID extracted from the previous request.

        assertNotNull(tableClient2.createTable());

        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final TableEntity tableEntity = new TableEntity(partitionKeyValue, rowKeyValue);

        // All other requests will also use the tenant ID obtained from the auth challenge.
        assertDoesNotThrow(() -> tableClient2.createEntity(tableEntity));
    }

    @Test
    public void createTableWithResponse() {
        // Arrange
        final String tableName2 = testResourceNamer.randomName("tableName", 20);
        final String connectionString = TestUtils.getConnectionString(interceptorManager.isPlaybackMode());
        final TableClient tableClient2 = getClientBuilder(tableName2, connectionString).buildClient();
        final int expectedStatusCode = 204;

        // Act & Assert
        assertEquals(expectedStatusCode, tableClient2.createTableWithResponse(null, null).getStatusCode());
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
        assertDoesNotThrow(() -> tableClient.createEntity(tableEntity));
    }

    @Test
    public void createEntityWithResponse() {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final TableEntity entity = new TableEntity(partitionKeyValue, rowKeyValue);
        final int expectedStatusCode = 204;

        // Act & Assert
        assertEquals(expectedStatusCode, tableClient.createEntityWithResponse(entity, null, null).getStatusCode());
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

        tableClient.createEntity(tableEntity);

        // Act & Assert
        final Response<TableEntity> response =
            tableClient.getEntityWithResponse(partitionKeyValue, rowKeyValue, null, null, null);

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

        tableClient.createEntity(tableEntity);

        // Act & Assert
        final Response<TableEntity> response =
            tableClient.getEntityWithResponse(partitionKeyValue, rowKeyValue, null, null, null);

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
    }*/

    @Test
    public void deleteTable() {
        // Act & Assert
        assertDoesNotThrow(() -> tableClient.deleteTable());
    }

    @Test
    public void deleteNonExistingTable() {
        // Arrange
        tableClient.deleteTable();

        // Act & Assert
        assertDoesNotThrow(() -> tableClient.deleteTable());
    }

    @Test
    public void deleteTableWithResponse() {
        // Arrange
        final int expectedStatusCode = 204;

        // Act & Assert
        assertEquals(expectedStatusCode, tableClient.deleteTableWithResponse(null, null).getStatusCode());
    }

    @Test
    public void deleteNonExistingTableWithResponse() {
        // Arrange
        final int expectedStatusCode = 404;
        tableClient.deleteTableWithResponse(null, null);

        // Act & Assert
        assertEquals(expectedStatusCode, tableClient.deleteTableWithResponse(null, null).getStatusCode());
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

        tableClient.createEntity(tableEntity);
        final TableEntity createdEntity = tableClient.getEntity(partitionKeyValue, rowKeyValue);
        assertNotNull(createdEntity, "'createdEntity' should not be null.");
        assertNotNull(createdEntity.getETag(), "'eTag' should not be null.");

        // Act & Assert
        assertDoesNotThrow(() -> tableClient.deleteEntity(partitionKeyValue, rowKeyValue));
    }

    @Test
    public void deleteNonExistingEntity() {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);

        // Act & Assert
        assertDoesNotThrow(() -> tableClient.deleteEntity(partitionKeyValue, rowKeyValue));
    }

    @Test
    public void deleteEntityWithResponse() {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final TableEntity tableEntity = new TableEntity(partitionKeyValue, rowKeyValue);
        final int expectedStatusCode = 204;

        tableClient.createEntity(tableEntity);
        final TableEntity createdEntity = tableClient.getEntity(partitionKeyValue, rowKeyValue);
        assertNotNull(createdEntity, "'createdEntity' should not be null.");
        assertNotNull(createdEntity.getETag(), "'eTag' should not be null.");

        // Act & Assert
        assertEquals(expectedStatusCode,
            tableClient.deleteEntityWithResponse(createdEntity, false, null, null).getStatusCode());
    }

    @Test
    public void deleteNonExistingEntityWithResponse() {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final TableEntity entity = new TableEntity(partitionKeyValue, rowKeyValue);
        final int expectedStatusCode = 404;

        // Act & Assert
        assertEquals(expectedStatusCode,
            tableClient.deleteEntityWithResponse(entity, false, null, null).getStatusCode());
    }

    @Test
    public void deleteEntityWithResponseMatchETag() {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final TableEntity tableEntity = new TableEntity(partitionKeyValue, rowKeyValue);
        final int expectedStatusCode = 204;

        tableClient.createEntity(tableEntity);
        final TableEntity createdEntity = tableClient.getEntity(partitionKeyValue, rowKeyValue);
        assertNotNull(createdEntity, "'createdEntity' should not be null.");
        assertNotNull(createdEntity.getETag(), "'eTag' should not be null.");

        // Act & Assert
        assertEquals(expectedStatusCode,
            tableClient.deleteEntityWithResponse(createdEntity, true, null, null).getStatusCode());
    }

    @Test
    public void getEntityWithSingleQuotesInPartitionKey() {
        getEntityWithResponseImpl(tableClient, testResourceNamer, "partition'Key", "rowKey");
    }

    @Test
    public void getEntityWithSingleQuotesInRowKey() {
        getEntityWithResponseImpl(tableClient, testResourceNamer, "partitionKey", "row'Key");
    }

    @Test
    public void getEntityWithResponse() {
        getEntityWithResponseImpl(tableClient, testResourceNamer, "partitionKey", "rowKey");
    }

    static void getEntityWithResponseImpl(TableClient tableClient, TestResourceNamer testResourceNamer,
                                          String partitionKeyPrefix, String rowKeyPrefix) {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName(partitionKeyPrefix, 20);
        final String rowKeyValue = testResourceNamer.randomName(rowKeyPrefix, 20);
        final TableEntity tableEntity = new TableEntity(partitionKeyValue, rowKeyValue);
        final int expectedStatusCode = 200;
        tableClient.createEntity(tableEntity);

        // Act & Assert
        final Response<TableEntity> response =
            tableClient.getEntityWithResponse(partitionKeyValue, rowKeyValue, null, null, null);

        final TableEntity entity = response.getValue();

        assertEquals(expectedStatusCode, response.getStatusCode());

        assertNotNull(entity);
        assertEquals(tableEntity.getPartitionKey(), entity.getPartitionKey());
        assertEquals(tableEntity.getRowKey(), entity.getRowKey());

        assertNotNull(entity.getTimestamp());
        assertNotNull(entity.getETag());
        assertNotNull(entity.getProperties());
    }

    @Test
    public void getEntityWithResponseWithSelect() {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final TableEntity tableEntity = new TableEntity(partitionKeyValue, rowKeyValue);
        tableEntity.addProperty("Test", "Value");
        final int expectedStatusCode = 200;
        tableClient.createEntity(tableEntity);
        List<String> propertyList = new ArrayList<>();
        propertyList.add("Test");

        // Act & Assert
        final Response<TableEntity> response =
            tableClient.getEntityWithResponse(partitionKeyValue, rowKeyValue, propertyList, null, null);

        final TableEntity entity = response.getValue();
        assertEquals(expectedStatusCode, response.getStatusCode());

        assertNotNull(entity);
        assertNull(entity.getPartitionKey());
        assertNull(entity.getRowKey());
        assertNull(entity.getTimestamp());
        assertNotNull(entity.getETag());
        assertEquals(entity.getProperties().get("Test"), "Value");
    }

    @Test
    public void updateEntityWithSingleQuotesInPartitionKey() {
        updateEntityWithResponseImpl(TableEntityUpdateMode.MERGE, testResourceNamer.randomName("partition'Key", 20),
            testResourceNamer.randomName("rowKey", 20));
    }

    @Test
    public void updateEntityWithSingleQuotesInRowKey() {
        updateEntityWithResponseImpl(TableEntityUpdateMode.MERGE, testResourceNamer.randomName("partitionKey", 20),
            testResourceNamer.randomName("row'Key", 20));
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
        tableClient.createEntity(tableEntity);

        // Act & Assert
        final Response<TableEntity> response =
            tableClient.getEntityWithResponse(partitionKeyValue, rowKeyValue, null, SampleEntity.class, null, null);

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
    }*/

    @Test
    public void updateEntityWithResponseReplace() {
        updateEntityWithResponseImpl(TableEntityUpdateMode.REPLACE, "partitionKey", "rowKey");
    }

    @Test
    public void updateEntityWithResponseMerge() {
        updateEntityWithResponseImpl(TableEntityUpdateMode.MERGE, "partitionKey", "rowKey");
    }

    /**
     * In the case of {@link TableEntityUpdateMode#MERGE}, we expect both properties to exist.
     * In the case of {@link TableEntityUpdateMode#REPLACE}, we only expect {@code newPropertyKey} to exist.
     */
    void updateEntityWithResponseImpl(TableEntityUpdateMode mode, String partitionKeyPrefix, String rowKeyPrefix) {
        // Arrange
        final boolean expectOldProperty = mode == TableEntityUpdateMode.MERGE;
        final String partitionKeyValue = testResourceNamer.randomName(partitionKeyPrefix, 20);
        final String rowKeyValue = testResourceNamer.randomName(rowKeyPrefix, 20);
        final int expectedStatusCode = 204;
        final String oldPropertyKey = "propertyA";
        final String newPropertyKey = "propertyB";
        final TableEntity tableEntity = new TableEntity(partitionKeyValue, rowKeyValue)
            .addProperty(oldPropertyKey, "valueA");

        tableClient.createEntity(tableEntity);
        final TableEntity createdEntity = tableClient.getEntity(partitionKeyValue, rowKeyValue);
        assertNotNull(createdEntity, "'createdEntity' should not be null.");
        assertNotNull(createdEntity.getETag(), "'eTag' should not be null.");

        createdEntity.getProperties().remove(oldPropertyKey);
        createdEntity.addProperty(newPropertyKey, "valueB");

        // Act & Assert
        assertEquals(expectedStatusCode,
            tableClient.updateEntityWithResponse(createdEntity, mode, true, null, null).getStatusCode());

        // Assert and verify that the new properties are in there.
        TableEntity entity = tableClient.getEntity(partitionKeyValue, rowKeyValue);

        final Map<String, Object> properties = entity.getProperties();
        assertTrue(properties.containsKey(newPropertyKey));
        assertEquals(expectOldProperty, properties.containsKey(oldPropertyKey));
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
        tableClient.createEntity(tableEntity);

        // Act & Assert
        tableEntity.setSubclassProperty("UpdatedValue");
        assertEquals(expectedStatusCode,
            tableClient.updateEntityWithResponse(tableEntity, TableEntityUpdateMode.REPLACE, true, null, null)
                .getStatusCode()));

        TableEntity entity = tableClient.getEntity(partitionKeyValue, rowKeyValue);

        final Map<String, Object> properties = entity.getProperties();
        assertTrue(properties.containsKey("SubclassProperty"));
        assertEquals("UpdatedValue", properties.get("SubclassProperty"));
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
        tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValue));
        tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValue2));

        // Act & Assert
        Iterator<PagedResponse<TableEntity>> iterator =
            tableClient.listEntities().iterableByPage().iterator();

        assertTrue(iterator.hasNext());

        List<TableEntity> retrievedEntities = iterator.next().getValue();

        assertEquals(2, retrievedEntities.size());
    }

    @Test
    public void listEntitiesWithFilter() {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final String rowKeyValue2 = testResourceNamer.randomName("rowKey", 20);
        ListEntitiesOptions options = new ListEntitiesOptions().setFilter("RowKey eq '" + rowKeyValue + "'");
        tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValue));
        tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValue2));

        // Act & Assert
        tableClient.listEntities(options, null, null).forEach(tableEntity -> {
            assertEquals(partitionKeyValue, tableEntity.getPartitionKey());
            assertEquals(rowKeyValue, tableEntity.getRowKey());
        });
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
        tableClient.createEntity(entity);

        // Act & Assert
        Iterator<PagedResponse<TableEntity>> iterator =
            tableClient.listEntities(options, null, null).iterableByPage().iterator();

        assertTrue(iterator.hasNext());

        TableEntity retrievedEntity = iterator.next().getValue().get(0);

        assertNull(retrievedEntity.getPartitionKey());
        assertNull(retrievedEntity.getRowKey());
        assertEquals("valueC", retrievedEntity.getProperties().get("propertyC"));
        assertNull(retrievedEntity.getProperties().get("propertyD"));
    }

    @Test
    public void listEntitiesWithTop() {
        // Arrange
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final String rowKeyValue2 = testResourceNamer.randomName("rowKey", 20);
        final String rowKeyValue3 = testResourceNamer.randomName("rowKey", 20);
        ListEntitiesOptions options = new ListEntitiesOptions().setTop(2);
        tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValue));
        tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValue2));
        tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValue3));

        // Act & Assert
        Iterator<PagedResponse<TableEntity>> iterator =
            tableClient.listEntities(options, null, null).iterableByPage().iterator();

        assertTrue(iterator.hasNext());
        assertEquals(2, iterator.next().getValue().size());
    }

    // Support for subclassing TableEntity was removed for the time being, although having it back is not 100%
    // discarded. -vicolina
    /*@Test
    public void listEntitiesSubclass() {
        // Arrange
        String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        String rowKeyValue2 = testResourceNamer.randomName("rowKey", 20);
        tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValue));
        tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValue2));

        // Act & Assert
        Iterator<PagedResponse<TableEntity>> iterator =
            tableClient.listEntities(SampleEntity.class).iterableByPage().iterator();

        assertTrue(iterator.hasNext());

        List<TableEntity> retrievedEntities = iterator.next().getValue();

        TableEntity retrievedEntity = retrievedEntities.get(0);
        TableEntity retrievedEntity2 = retrievedEntities.get(1);

        assertEquals(partitionKeyValue, retrievedEntity.getPartitionKey());
        assertEquals(rowKeyValue, retrievedEntity.getRowKey());
        assertEquals(partitionKeyValue, retrievedEntity2.getPartitionKey());
        assertEquals(rowKeyValue2, retrievedEntity2.getRowKey());
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
            tableClient.submitTransactionWithResponse(transactionalBatch, null, null);

        assertNotNull(result);
        assertEquals(expectedBatchStatusCode, result.getStatusCode());
        assertEquals(transactionalBatch.size(), result.getValue().getTransactionActionResponses().size());
        assertEquals(expectedOperationStatusCode,
            result.getValue().getTransactionActionResponses().get(0).getStatusCode());
        assertEquals(expectedOperationStatusCode,
            result.getValue().getTransactionActionResponses().get(1).getStatusCode());

        final Response<TableEntity> response =
            tableClient.getEntityWithResponse(partitionKeyValue, rowKeyValue, null, null, null);

        final TableEntity entity = response.getValue();

        assertNotNull(entity);
        assertEquals(partitionKeyValue, entity.getPartitionKey());
        assertEquals(rowKeyValue, entity.getRowKey());

        assertNotNull(entity.getTimestamp());
        assertNotNull(entity.getETag());
        assertNotNull(entity.getProperties());
    }

    @Test
    public void submitTransactionAllActions() {
        Runnable func = () -> submitTransactionAllActionsImpl("partitionKey", "rowKey");
        func.run();
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

        tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValueUpsertMerge));
        tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValueUpsertReplace));
        tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValueUpdateMerge));
        tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValueUpdateReplace));
        tableClient.createEntity(new TableEntity(partitionKeyValue, rowKeyValueDelete));

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
        final Response<TableTransactionResult> response =
            tableClient.submitTransactionWithResponse(transactionalBatch, null, null);

        assertNotNull(response);
        assertEquals(expectedBatchStatusCode, response.getStatusCode());

        TableTransactionResult result = response.getValue();

        assertEquals(transactionalBatch.size(), result.getTransactionActionResponses().size());

        for (TableTransactionActionResponse subResponse : result.getTransactionActionResponses()) {
            assertEquals(expectedOperationStatusCode, subResponse.getStatusCode());
        }
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
        try {
            tableClient.submitTransactionWithResponse(transactionalBatch, null, null);
        } catch (TableTransactionFailedException e) {
            assertTrue(e.getMessage().contains("An action within the operation failed"));
            assertTrue(e.getMessage().contains("The failed operation was"));
            assertTrue(e.getMessage().contains("DeleteEntity"));
            assertTrue(e.getMessage().contains("partitionKey='" + partitionKeyValue));
            assertTrue(e.getMessage().contains("rowKey='" + rowKeyValue2));

            return;
        }

        // Fail if exception was not thrown.
        fail();
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
        try {
            tableClient.submitTransactionWithResponse(transactionalBatch, null, null);
        } catch (TableTransactionFailedException e) {
            assertTrue(e.getMessage().contains("An action within the operation failed"));
            assertTrue(e.getMessage().contains("The failed operation was"));
            assertTrue(e.getMessage().contains("CreateEntity"));
            assertTrue(e.getMessage().contains("partitionKey='" + partitionKeyValue));
            assertTrue(e.getMessage().contains("rowKey='" + rowKeyValue));

            return;
        } catch (TableServiceException e) {
            assertTrue(IS_COSMOS_TEST);
            assertEquals(400, e.getResponse().getStatusCode());
            assertTrue(e.getMessage().contains("InvalidDuplicateRow"));

            return;
        }

        // Fail if exception was not thrown.
        fail();
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
        try {
            tableClient.submitTransactionWithResponse(transactionalBatch, null, null);
        } catch (TableTransactionFailedException e) {
            if (IS_COSMOS_TEST) {
                // For some reason Cosmos names the first entity's keys while Storage does so with the second entity.
                // It is possible that Cosmos ensures there will be no conflict between a transaction's operations
                // before executing them and Storage executes them without pre-checking for conflicts.
                assertTrue(e.getMessage().contains("An action within the operation failed"));
                assertTrue(e.getMessage().contains("The failed operation was"));
                assertTrue(e.getMessage().contains("CreateEntity"));
                assertTrue(e.getMessage().contains("partitionKey='" + partitionKeyValue));
                assertTrue(e.getMessage().contains("rowKey='" + rowKeyValue));
            } else {
                assertTrue(e.getMessage().contains("An action within the operation failed"));
                assertTrue(e.getMessage().contains("The failed operation was"));
                assertTrue(e.getMessage().contains("CreateEntity"));
                assertTrue(e.getMessage().contains("partitionKey='" + partitionKeyValue2));
                assertTrue(e.getMessage().contains("rowKey='" + rowKeyValue2));
            }

            return;
        }

        // Fail if exception was not thrown.
        fail();
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
    // Disabling as this currently fails and prevents merging https://github.com/Azure/azure-sdk-for-java/pull/28522.
    // TODO: Will fix in a separate PR. -vicolina
    public void canUseSasTokenToCreateValidTableClient() {
        // SAS tokens at the table level have not been working with Cosmos endpoints.
        // TODO: Will re-enable once the above is fixed. -vicolina
        Assumptions.assumeFalse(IS_COSMOS_TEST, "Skipping Cosmos test.");

        final OffsetDateTime expiryTime = OffsetDateTime.of(2023, 12, 12, 0, 0, 0, 0, ZoneOffset.UTC);
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
        final TableClient newTableClient = tableClientBuilder.buildClient();
        final String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        final String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        final TableEntity entity = new TableEntity(partitionKeyValue, rowKeyValue);
        final int expectedStatusCode = 204;

        assertEquals(expectedStatusCode, newTableClient.createEntityWithResponse(entity, null, null).getStatusCode());
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
        final int expectedStatusCode = 204;

        assertEquals(expectedStatusCode,
            tableClient.setAccessPoliciesWithResponse(Collections.singletonList(tableSignedIdentifier), null, null)
                .getStatusCode());

        TableAccessPolicies tableAccessPolicies = tableClient.getAccessPolicies();

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
    }

    @Test
    public void setAndListMultipleAccessPolicies() {
        Assumptions.assumeFalse(IS_COSMOS_TEST,
            "Setting and listing access policies is not supported on Cosmos endpoints.");

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
        final int expectedStatusCode = 204;

        assertEquals(expectedStatusCode,
            tableClient.setAccessPoliciesWithResponse(tableSignedIdentifiers, null, null).getStatusCode());

        TableAccessPolicies tableAccessPolicies = tableClient.getAccessPolicies();

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
    }

    @Test
    public void allowsCreationOfEntityWithEmptyStringPrimaryKey() {
        Assumptions.assumeFalse(IS_COSMOS_TEST,
            "Empty row or partition keys are not supported on Cosmos endpoints.");
        Assertions.assertDoesNotThrow(() -> {
            TableEntity entity = new TableEntity("", "");
            tableClient.createEntity(entity);
        });
    }

    @Test
    public void allowListEntitiesWithEmptyPrimaryKey() {
        Assumptions.assumeFalse(IS_COSMOS_TEST,
            "Empty row or partition keys are not supported on Cosmos endpoints.");
        TableEntity entity = new TableEntity("", "");
        String entityName = testResourceNamer.randomName("name", 10);
        entity.addProperty("Name", entityName);
        tableClient.createEntity(entity);
        ListEntitiesOptions options = new ListEntitiesOptions();
        options.setFilter("PartitionKey eq '' and RowKey eq ''");
        PagedIterable<TableEntity> response = tableClient.listEntities(options, Duration.ofSeconds(10), null);
        ArrayList<TableEntity> responseArray = new ArrayList<TableEntity>();
        for (TableEntity responseEntity : response) {
            responseArray.add(responseEntity);
        }
        assertEquals(1, responseArray.size());
        assertEquals(entityName, responseArray.get(0).getProperty("Name"));
    }

    // tests that you can delete a table entity with an empty string partition key and empty string row key
    @Test
    public void allowDeleteEntityWithEmptyPrimaryKey() {
        Assumptions.assumeFalse(IS_COSMOS_TEST,
            "Empty row or partition keys are not supported on Cosmos endpoints.");
        TableEntity entity = new TableEntity("", "");
        String entityName = testResourceNamer.randomName("name", 10);
        entity.addProperty("Name", entityName);
        tableClient.createEntity(entity);
        tableClient.deleteEntity(entity);
    }


}
