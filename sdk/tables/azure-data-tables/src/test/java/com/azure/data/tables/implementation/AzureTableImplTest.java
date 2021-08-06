// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.implementation;

import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.ExponentialBackoff;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.TestBase;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.tables.TableAzureNamedKeyCredentialPolicy;
import com.azure.data.tables.TableServiceVersion;
import com.azure.data.tables.TestUtils;
import com.azure.data.tables.implementation.models.OdataMetadataFormat;
import com.azure.data.tables.implementation.models.QueryOptions;
import com.azure.data.tables.implementation.models.ResponseFormat;
import com.azure.data.tables.implementation.models.TableProperties;
import com.azure.data.tables.implementation.models.TableResponseProperties;
import com.azure.data.tables.implementation.models.TableServiceErrorException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.azure.data.tables.implementation.TablesConstants.PARTITION_KEY;
import static com.azure.data.tables.implementation.TablesConstants.ROW_KEY;
import static com.azure.data.tables.implementation.TablesConstants.TABLE_NAME_KEY;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class tests the Autorest code for the Tables track 2 SDK
 */
public class AzureTableImplTest extends TestBase {
    private static final int TIMEOUT_IN_MS = 100_000;

    private final QueryOptions defaultQueryOptions = new QueryOptions()
        .setFormat(OdataMetadataFormat.APPLICATION_JSON_ODATA_FULLMETADATA);

    private final ClientLogger logger = new ClientLogger(AzureTableImplTest.class);
    private AzureTableImpl azureTable;

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofMillis(TIMEOUT_IN_MS));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @Override
    protected void beforeTest() {
        final String connectionString = TestUtils.getConnectionString(interceptorManager.isPlaybackMode());
        final StorageConnectionString storageConnectionString
            = StorageConnectionString.create(connectionString, logger);

        Assertions.assertNotNull(connectionString, "Cannot continue test if connectionString is not set.");

        StorageAuthenticationSettings authSettings = storageConnectionString.getStorageAuthSettings();
        AzureNamedKeyCredential azureNamedKeyCredential = new AzureNamedKeyCredential(
            authSettings.getAccount().getName(), authSettings.getAccount().getAccessKey());

        List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new AddDatePolicy());
        policies.add(new TableAzureNamedKeyCredentialPolicy(azureNamedKeyCredential));
        policies.add(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)));

        // Add Accept header so we don't get back XML.
        // Can be removed when this is fixed. https://github.com/Azure/autorest.modelerfour/issues/324
        policies.add(new AddHeadersPolicy(new HttpHeaders().put("Accept", "application/json")));

        HttpClient httpClientToUse;
        if (interceptorManager.isPlaybackMode()) {
            httpClientToUse = interceptorManager.getPlaybackClient();
        } else {
            httpClientToUse = HttpClient.createDefault();
            if (!interceptorManager.isLiveMode()) {
                HttpPipelinePolicy recordPolicy = interceptorManager.getRecordPolicy();
                policies.add(recordPolicy);
            }
            policies.add(new RetryPolicy(new ExponentialBackoff(6, Duration.ofMillis(1500), Duration.ofSeconds(100))));
        }

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClientToUse)
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .build();
        azureTable = new AzureTableImplBuilder()
                .pipeline(pipeline)
                .serializerAdapter(new TablesJacksonSerializer())
                .version(TableServiceVersion.getLatest().getVersion())
                .url(storageConnectionString.getTableEndpoint().getPrimaryUri())
                .buildClient();
    }

    @Override
    protected void afterTest() {
        QueryOptions queryOptions = new QueryOptions()
            .setFormat(OdataMetadataFormat.APPLICATION_JSON_ODATA_MINIMALMETADATA);

        List<TableResponseProperties> result = azureTable.getTables().queryWithResponseAsync(
            testResourceNamer.randomUuid(), null, queryOptions, Context.NONE).block().getValue().getValue();

        Mono.when(Flux.fromIterable(result).flatMap(tableResponseProperty ->
            azureTable.getTables().deleteWithResponseAsync(tableResponseProperty.getTableName(),
                testResourceNamer.randomUuid(), Context.NONE))).block();
    }

    void createTable(String tableName) {
        TableProperties tableProperties = new TableProperties().setTableName(tableName);
        String requestId = testResourceNamer.randomUuid();

        azureTable.getTables().createWithResponseAsync(tableProperties, requestId, ResponseFormat.RETURN_NO_CONTENT,
            null, Context.NONE).block();
    }

    void insertNoETag(String tableName, Map<String, Object> properties) {
        String requestId = testResourceNamer.randomUuid();

        azureTable.getTables().insertEntityWithResponseAsync(tableName, TIMEOUT_IN_MS, requestId,
            ResponseFormat.RETURN_NO_CONTENT, properties, null, Context.NONE).log().block();
    }

    @Test
    void createTableImpl() {
        // Arrange
        String tableName = testResourceNamer.randomName("test", 20);
        TableProperties tableProperties = new TableProperties().setTableName(tableName);
        int expectedStatusCode = 204;
        String requestId = testResourceNamer.randomUuid();

        // Act & Assert
        StepVerifier.create(azureTable.getTables().createWithResponseAsync(tableProperties, requestId,
            ResponseFormat.RETURN_NO_CONTENT, null, Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(expectedStatusCode, response.getStatusCode());
            })
            .expectComplete()
            .verify();
    }

    @Test
    void createTableDuplicateNameImpl() {
        // Arrange
        String expectedErrorCode = "TableAlreadyExists";
        String tableName = testResourceNamer.randomName("test", 20);
        TableProperties tableProperties = new TableProperties().setTableName(tableName);
        createTable(tableName);
        String requestId = testResourceNamer.randomUuid();

        // Act & Assert
        StepVerifier.create(azureTable.getTables().createWithResponseAsync(tableProperties, requestId,
            ResponseFormat.RETURN_NO_CONTENT, defaultQueryOptions, Context.NONE))
            .expectErrorSatisfies(error -> {
                assertTrue(error instanceof TableServiceErrorException);

                final TableServiceErrorException exception = (TableServiceErrorException) error;

                assertTrue(exception.getMessage().contains(expectedErrorCode));
            })
            .verify();
    }

    @Test
    void deleteTableImpl() {
        // Arrange
        String tableName = testResourceNamer.randomName("test", 20);
        createTable(tableName);
        int expectedStatusCode = 204;
        String requestId = testResourceNamer.randomUuid();

        // Act & Assert
        StepVerifier.create(azureTable.getTables().deleteWithResponseAsync(tableName, requestId, Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(expectedStatusCode, response.getStatusCode());
            })
            .expectComplete()
            .verify();
    }

    @Test
    void deleteNonExistentTableImpl() {
        // Arrange
        String tableName = testResourceNamer.randomName("test", 20);
        String requestId = testResourceNamer.randomUuid();

        // Act & Assert
        StepVerifier.create(azureTable.getTables().deleteWithResponseAsync(tableName, requestId, Context.NONE))
            .expectError(com.azure.data.tables.implementation.models.TableServiceErrorException.class)
            .verify();
    }

    @Test
    void queryTableImpl() {
        // Arrange
        afterTest(); // Clean up any tables that may have been made by other tests before this one

        QueryOptions queryOptions = new QueryOptions()
            .setFormat(OdataMetadataFormat.APPLICATION_JSON_ODATA_MINIMALMETADATA);
        String tableA = testResourceNamer.randomName("AtestA", 20);
        String tableB = testResourceNamer.randomName("BtestB", 20);
        createTable(tableA);
        createTable(tableB);
        int expectedStatusCode = 200;
        int expectedSize = 2;
        String requestId = testResourceNamer.randomUuid();

        // Act & Assert
        StepVerifier.create(azureTable.getTables().queryWithResponseAsync(requestId, null, queryOptions, Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(expectedStatusCode, response.getStatusCode());
                List<TableResponseProperties> results = response.getValue().getValue();
                Assertions.assertEquals(expectedSize, results.size());
                assertTrue(results.stream().anyMatch(p -> tableA.equals(p.getTableName())));
                assertTrue(results.stream().anyMatch(p -> tableB.equals(p.getTableName())));
            })
            .expectComplete()
            .verify();
    }

    @Test
    void queryTableWithFilterImpl() {
        // Arrange
        afterTest(); // Clean up any tables that may have been made by other tests before this one

        QueryOptions queryOptions = new QueryOptions()
            .setFormat(OdataMetadataFormat.APPLICATION_JSON_ODATA_MINIMALMETADATA);
        String tableA = testResourceNamer.randomName("AtestA", 20);
        String tableB = testResourceNamer.randomName("BtestB", 20);
        createTable(tableA);
        createTable(tableB);
        int expectedStatusCode = 200;
        int expectedSize = 1;
        String requestId = testResourceNamer.randomUuid();
        queryOptions.setFilter(TABLE_NAME_KEY + " eq '" + tableA + "'");

        // Act & Assert
        StepVerifier.create(azureTable.getTables().queryWithResponseAsync(requestId, null, queryOptions, Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(expectedStatusCode, response.getStatusCode());
                Assertions.assertEquals(expectedSize, response.getValue().getValue().size());
                Assertions.assertEquals(tableA, response.getValue().getValue().get(0).getTableName());
            })
            .expectComplete()
            .verify();
    }

    @Test
    void queryTableWithTopImpl() {
        // Arrange
        afterTest(); // Clean up any tables that may have been made by other tests before this one

        QueryOptions queryOptions = new QueryOptions()
            .setFormat(OdataMetadataFormat.APPLICATION_JSON_ODATA_MINIMALMETADATA);
        String tableA = testResourceNamer.randomName("AtestA", 20);
        String tableB = testResourceNamer.randomName("BtestB", 20);
        createTable(tableA);
        createTable(tableB);
        int expectedStatusCode = 200;
        int expectedSize = 1;
        String requestId = testResourceNamer.randomUuid();
        queryOptions.setTop(1);

        // Act & Assert
        StepVerifier.create(azureTable.getTables().queryWithResponseAsync(requestId, null, queryOptions, Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(expectedStatusCode, response.getStatusCode());
                Assertions.assertEquals(expectedSize, response.getValue().getValue().size());
                String tableName = response.getValue().getValue().get(0).getTableName();
                // Query results returned by the Table API aren't sorted in partition key/row key order as they are
                // in Azure Table storage.
                Assertions.assertTrue(tableA.equals(tableName) || tableB.equals(tableName));
            })
            .expectComplete()
            .verify();
    }

    @Test
    void insertNoETagImpl() {
        // Arrange
        String tableName = testResourceNamer.randomName("test", 20);
        createTable(tableName);
        Map<String, Object> properties = new HashMap<>();
        String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        properties.put(PARTITION_KEY, partitionKeyValue);
        properties.put(ROW_KEY, rowKeyValue);
        int expectedStatusCode = 204;
        String requestId = testResourceNamer.randomUuid();

        // Act & Assert
        StepVerifier.create(azureTable.getTables().insertEntityWithResponseAsync(tableName, TIMEOUT_IN_MS, requestId,
            ResponseFormat.RETURN_NO_CONTENT, properties, null, Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(expectedStatusCode, response.getStatusCode());
            })
            .expectComplete()
            .verify();
    }

    @Test
    void mergeEntityImpl() {
        // Arrange
        String tableName = testResourceNamer.randomName("test", 20);
        createTable(tableName);
        Map<String, Object> properties = new HashMap<>();
        String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        properties.put(PARTITION_KEY, partitionKeyValue);
        properties.put(ROW_KEY, rowKeyValue);
        int expectedStatusCode = 204;
        String requestId = testResourceNamer.randomUuid();
        insertNoETag(tableName, properties);
        properties.put("extraProperty", testResourceNamer.randomName("extraProperty", 16));

        // Act & Assert
        if (azureTable.getUrl().contains("cosmos.azure.com")) {
            // This scenario is currently broken when using the CosmosDB Table API
            StepVerifier.create(azureTable.getTables().mergeEntityWithResponseAsync(tableName, partitionKeyValue,
                rowKeyValue, TIMEOUT_IN_MS, requestId, "*", properties, null, Context.NONE))
                .expectError(com.azure.data.tables.implementation.models.TableServiceErrorException.class)
                .verify();
        } else {
            StepVerifier.create(azureTable.getTables().mergeEntityWithResponseAsync(tableName, partitionKeyValue,
                rowKeyValue, TIMEOUT_IN_MS, requestId, "*", properties, null, Context.NONE))
                .assertNext(response -> {
                    Assertions.assertEquals(expectedStatusCode, response.getStatusCode());
                })
                .expectComplete()
                .verify();
        }
    }

    @Test
    void mergeNonExistentEntityImpl() {
        // Arrange
        String tableName = testResourceNamer.randomName("test", 20);
        createTable(tableName);
        Map<String, Object> properties = new HashMap<>();
        String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        String requestId = testResourceNamer.randomUuid();

        // Act & Assert
        StepVerifier.create(azureTable.getTables().mergeEntityWithResponseAsync(tableName, partitionKeyValue,
            rowKeyValue, TIMEOUT_IN_MS, requestId, "*", properties, null, Context.NONE))
            .expectError(com.azure.data.tables.implementation.models.TableServiceErrorException.class)
            .verify();
    }

    @Test
    void updateEntityImpl() {
        // Arrange
        String tableName = testResourceNamer.randomName("test", 20);
        createTable(tableName);
        Map<String, Object> properties = new HashMap<>();
        String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        properties.put(PARTITION_KEY, partitionKeyValue);
        properties.put(ROW_KEY, rowKeyValue);
        int expectedStatusCode = 204;
        String requestId = testResourceNamer.randomUuid();
        insertNoETag(tableName, properties);
        properties.put("extraProperty", testResourceNamer.randomName("extraProperty", 16));

        // Act & Assert
        StepVerifier.create(azureTable.getTables().updateEntityWithResponseAsync(tableName, partitionKeyValue,
            rowKeyValue, TIMEOUT_IN_MS, requestId, "*", properties, null, Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(expectedStatusCode, response.getStatusCode());
            })
            .expectComplete()
            .verify();
    }

    @Test
    void updateNonExistentEntityImpl() {
        // Arrange
        String tableName = testResourceNamer.randomName("test", 20);
        createTable(tableName);
        Map<String, Object> properties = new HashMap<>();
        String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        String requestId = testResourceNamer.randomUuid();

        // Act & Assert
        StepVerifier.create(azureTable.getTables().updateEntityWithResponseAsync(tableName, partitionKeyValue,
            rowKeyValue, TIMEOUT_IN_MS, requestId, "*", properties, null, Context.NONE))
            .expectError(com.azure.data.tables.implementation.models.TableServiceErrorException.class)
            .verify();
    }

    @Test
    void deleteEntityImpl() {
        // Arrange
        String tableName = testResourceNamer.randomName("test", 20);
        createTable(tableName);
        Map<String, Object> properties = new HashMap<>();
        String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        properties.put(PARTITION_KEY, partitionKeyValue);
        properties.put(ROW_KEY, rowKeyValue);
        int expectedStatusCode = 204;
        String requestId = testResourceNamer.randomUuid();
        insertNoETag(tableName, properties);

        // Act & Assert
        StepVerifier.create(azureTable.getTables().deleteEntityWithResponseAsync(tableName, partitionKeyValue,
            rowKeyValue, "*", TIMEOUT_IN_MS, requestId, null, Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(expectedStatusCode, response.getStatusCode());
            })
            .expectComplete()
            .verify();
    }

    @Test
    void deleteNonExistentEntityImpl() {
        // Arrange
        String tableName = testResourceNamer.randomName("test", 20);
        createTable(tableName);
        String partitionKeyValue = testResourceNamer.randomName("partitionKey", 20);
        String rowKeyValue = testResourceNamer.randomName("rowKey", 20);
        String requestId = testResourceNamer.randomUuid();

        // Act & Assert
        StepVerifier.create(azureTable.getTables().deleteEntityWithResponseAsync(tableName, partitionKeyValue,
            rowKeyValue, "*", TIMEOUT_IN_MS, requestId, null, Context.NONE))
            .expectError(com.azure.data.tables.implementation.models.TableServiceErrorException.class)
            .verify();
    }

    @Test
    void queryEntityImpl() {
        // Arrange
        String requestId = testResourceNamer.randomUuid();
        QueryOptions queryOptions = new QueryOptions()
            .setFormat(OdataMetadataFormat.APPLICATION_JSON_ODATA_FULLMETADATA);
        String tableName = testResourceNamer.randomName("test", 20);
        createTable(tableName);
        //insert entity A
        Map<String, Object> entityA = new HashMap<>();
        String partitionKeyEntityA = testResourceNamer.randomName("partitionKeyA", 20);
        entityA.put(PARTITION_KEY, partitionKeyEntityA);
        entityA.put(ROW_KEY, testResourceNamer.randomName("rowKeyA", 20));
        insertNoETag(tableName, entityA);
        //insert entity B
        Map<String, Object> entityB = new HashMap<>();
        String partitionKeyEntityB = testResourceNamer.randomName("partitionKeyB", 20);
        entityB.put(PARTITION_KEY, partitionKeyEntityB);
        entityB.put(ROW_KEY, testResourceNamer.randomName("rowKeyB", 20));
        insertNoETag(tableName, entityB);
        int expectedStatusCode = 200;

        // Act & Assert
        StepVerifier.create(azureTable.getTables().queryEntitiesWithResponseAsync(tableName, TIMEOUT_IN_MS, requestId,
            null, null, queryOptions, Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(expectedStatusCode, response.getStatusCode());
                List<Map<String, Object>> results = response.getValue().getValue();
                assertTrue(results.stream().anyMatch(p -> p.containsValue(partitionKeyEntityA)));
                assertTrue(results.stream().anyMatch(p -> p.containsValue(partitionKeyEntityB)));
            })
            .expectComplete()
            .verify();
    }

    @Test
    void queryEntityImplWithSelect() {
        // Arrange
        String requestId = testResourceNamer.randomUuid();
        QueryOptions queryOptions = new QueryOptions()
            .setFormat(OdataMetadataFormat.APPLICATION_JSON_ODATA_FULLMETADATA);
        String tableName = testResourceNamer.randomName("test", 20);
        createTable(tableName);

        //insert entity A
        Map<String, Object> entityA = new HashMap<>();
        String partitionKeyEntityA = testResourceNamer.randomName("partitionKeyA", 20);
        String rowKeyEntityA = testResourceNamer.randomName("rowKeyA", 20);
        entityA.put(PARTITION_KEY, partitionKeyEntityA);
        entityA.put(ROW_KEY, rowKeyEntityA);
        insertNoETag(tableName, entityA);

        //insert entity B
        Map<String, Object> entityB = new HashMap<>();
        String partitionKeyEntityB = testResourceNamer.randomName("partitionKeyB", 20);
        String rowKeyEntityB = testResourceNamer.randomName("rowKeyB", 20);
        entityB.put(PARTITION_KEY, partitionKeyEntityB);
        entityB.put(ROW_KEY, rowKeyEntityB);
        insertNoETag(tableName, entityB);

        int expectedStatusCode = 200;
        int expectedSize = 2;
        queryOptions.setSelect(ROW_KEY);

        // Act & Assert
        StepVerifier.create(azureTable.getTables().queryEntitiesWithResponseAsync(tableName, TIMEOUT_IN_MS, requestId,
            null, null, queryOptions, Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(expectedStatusCode, response.getStatusCode());
                List<Map<String, Object>> results = response.getValue().getValue();
                Assertions.assertEquals(expectedSize, results.size());
                assertTrue(results.stream().anyMatch(p -> p.containsValue(rowKeyEntityA)));
                assertTrue(results.stream().anyMatch(p -> p.containsValue(rowKeyEntityB)));
            })
            .expectComplete()
            .verify();
    }

    @Test
    void queryEntityImplWithFilter() {
        // Arrange
        String requestId = testResourceNamer.randomUuid();
        QueryOptions queryOptions = new QueryOptions()
            .setFormat(OdataMetadataFormat.APPLICATION_JSON_ODATA_FULLMETADATA);
        String tableName = testResourceNamer.randomName("test", 20);
        createTable(tableName);

        //insert entity A
        Map<String, Object> entityA = new HashMap<>();
        String partitionKeyEntityA = testResourceNamer.randomName("partitionKeyA", 20);
        entityA.put(PARTITION_KEY, partitionKeyEntityA);
        entityA.put(ROW_KEY, testResourceNamer.randomName("rowKeyA", 20));
        insertNoETag(tableName, entityA);

        //insert entity B
        Map<String, Object> entityB = new HashMap<>();
        String partitionKeyEntityB = testResourceNamer.randomName("partitionKeyB", 20);
        String rowKeyEntityB = testResourceNamer.randomName("rowKeyB", 20);
        entityB.put(PARTITION_KEY, partitionKeyEntityB);
        entityB.put(ROW_KEY, rowKeyEntityB);
        insertNoETag(tableName, entityB);

        int expectedStatusCode = 200;
        int expectedSize = 1;
        queryOptions.setFilter(PARTITION_KEY + " eq '" + partitionKeyEntityA + "'");

        // Act & Assert
        StepVerifier.create(azureTable.getTables().queryEntitiesWithResponseAsync(tableName, TIMEOUT_IN_MS, requestId,
            null, null, queryOptions, Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(expectedStatusCode, response.getStatusCode());
                Assertions.assertEquals(expectedSize, response.getValue().getValue().size());
                assertTrue(response.getValue().getValue().get(0).containsValue(partitionKeyEntityA));
            })
            .expectComplete()
            .verify();
    }

    @Test
    void queryEntityImplWithTop() {
        // Arrange
        String requestId = testResourceNamer.randomUuid();
        QueryOptions queryOptions = new QueryOptions()
            .setFormat(OdataMetadataFormat.APPLICATION_JSON_ODATA_FULLMETADATA);
        String tableName = testResourceNamer.randomName("test", 20);
        createTable(tableName);

        //insert entity A
        Map<String, Object> entityA = new HashMap<>();
        String partitionKeyEntityA = testResourceNamer.randomName("partitionKeyA", 20);
        entityA.put(PARTITION_KEY, partitionKeyEntityA);
        entityA.put(ROW_KEY, testResourceNamer.randomName("rowKeyA", 20));
        insertNoETag(tableName, entityA);

        //insert entity B
        Map<String, Object> entityB = new HashMap<>();
        String partitionKeyEntityB = testResourceNamer.randomName("partitionKeyB", 20);
        String rowKeyEntityB = testResourceNamer.randomName("rowKeyB", 20);
        entityB.put(PARTITION_KEY, partitionKeyEntityB);
        entityB.put(ROW_KEY, rowKeyEntityB);
        insertNoETag(tableName, entityB);

        int expectedStatusCode = 200;
        int expectedSize = 1;
        queryOptions.setTop(1);

        // Act & Assert
        StepVerifier.create(azureTable.getTables().queryEntitiesWithResponseAsync(tableName, TIMEOUT_IN_MS, requestId,
            null, null, queryOptions, Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(expectedStatusCode, response.getStatusCode());
                Assertions.assertEquals(expectedSize, response.getValue().getValue().size());
                Map<String, Object> properties = response.getValue().getValue().get(0);
                // Query results returned by the Table API aren't sorted in partition key/row key order as they are
                // in Azure Table storage.
                Assertions.assertTrue(properties.containsValue(partitionKeyEntityA)
                    || properties.containsValue(partitionKeyEntityB));
            })
            .expectComplete()
            .verify();
    }
}
