// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.*;
import com.azure.core.test.TestBase;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.tables.implementation.AzureTableImpl;
import com.azure.data.tables.implementation.AzureTableImplBuilder;
import com.azure.data.tables.implementation.models.OdataMetadataFormat;
import com.azure.data.tables.implementation.models.QueryOptions;
import com.azure.data.tables.implementation.models.ResponseFormat;
import com.azure.data.tables.implementation.models.TableProperties;
import com.azure.storage.common.implementation.connectionstring.StorageAuthenticationSettings;
import com.azure.storage.common.implementation.connectionstring.StorageConnectionString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.*;

/**
 * This class tests the Autorest code for the Tables track 2 SDK
 */
public class AzureTableImplTest extends TestBase {
    private static final String PARTITION_KEY = "PartitionKey";
    private static final String ROW_KEY = "RowKey";
    private static final int TIMEOUT = 5000;
    private AzureTableImpl azureTable;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        String connectionString = interceptorManager.isPlaybackMode()
            ? "DefaultEndpointsProtocol=https;AccountName=dummyAccount;AccountKey=xyzDummy==;EndpointSuffix=core.windows.net"
            : System.getenv("AZURE_TABLES_CONNECTION_STRING");
        StorageConnectionString storageConnectionString
            = StorageConnectionString.create(connectionString, new ClientLogger(AzureTableImplTest.class));

        StorageAuthenticationSettings authSettings = storageConnectionString.getStorageAuthSettings();
        TablesSharedKeyCredential sharedKeyCredential = new TablesSharedKeyCredential(authSettings.getAccount().getName(),
            authSettings.getAccount().getAccessKey());

        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new AddDatePolicy());
        policies.add(new AddHeadersPolicy(new HttpHeaders().put("Accept",
            OdataMetadataFormat.APPLICATION_JSON_ODATA_MINIMALMETADATA.toString())));
        policies.add(new TablesSharedKeyCredentialPolicy(sharedKeyCredential));
        policies.add(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)));

        final HttpClient httpClientToUse;
        if (interceptorManager.isPlaybackMode()) {
            httpClientToUse = interceptorManager.getPlaybackClient();
        } else {
            httpClientToUse = HttpClient.createDefault();
            policies.add(interceptorManager.getRecordPolicy());
            policies.add(new RetryPolicy());
        }
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClientToUse)
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .build();
        azureTable = new AzureTableImplBuilder()
            .pipeline(pipeline)
            .version("2019-02-02")
            .url(storageConnectionString.getTableEndpoint().getPrimaryUri())
            .buildClient();
    }

    @Override
    protected void afterTest() {
        QueryOptions queryOptions = new QueryOptions()
            .setFormat(OdataMetadataFormat.APPLICATION_JSON_ODATA_MINIMALMETADATA);

        Mono.when(azureTable.getTables().queryWithResponseAsync(UUID.randomUUID().toString(), null,
            queryOptions, Context.NONE).flatMapMany(tablesQueryResponse -> {
            return Flux.fromIterable(tablesQueryResponse.getValue().getValue()).flatMap(tableResponseProperty -> {
                return azureTable.getTables().deleteWithResponseAsync(tableResponseProperty.getTableName(),
                    testResourceNamer.randomUuid(), Context.NONE);
            });
        })).block();
    }

    void createTable(String tableName) {
        TableProperties tableProperties = new TableProperties().setTableName(tableName);
        String requestId = testResourceNamer.randomUuid();

        azureTable.getTables().createWithResponseAsync(tableProperties, requestId,
            ResponseFormat.RETURN_CONTENT, null, Context.NONE).block();


    }

    void insertNoETag(String tableName, Map<String, Object> properties) {
        String requestId = testResourceNamer.randomUuid();

        azureTable.getTables().insertEntityWithResponseAsync(tableName, TIMEOUT,
            requestId, ResponseFormat.RETURN_CONTENT, properties, null, Context.NONE).log().block();
    }

    String randomCharOnlyName(String prefix, int length) {
        Random rnd = new Random();
        String result = prefix;
        while (result.length() < length) {
            char c = (char) (rnd.nextInt(26) + 'a');
            result += c;
        }
        return result;
    }

    @Test
    void createTable() {
        // Arrange
        String tableName = "tableA"; // randomCharOnlyName("test", 20);
        TableProperties tableProperties = new TableProperties().setTableName(tableName);
        int expectedStatusCode = 201;
        String requestId = testResourceNamer.randomUuid();

        // Act & Assert
        StepVerifier.create(azureTable.getTables().createWithResponseAsync(tableProperties,
            requestId, ResponseFormat.RETURN_CONTENT, null, Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(expectedStatusCode, response.getStatusCode());
            })
            .expectComplete()
            .verify();
    }

    @Test
    void createTableDuplicateName() {
        // Arrange
        String tableName = randomCharOnlyName("test", 20);
        TableProperties tableProperties = new TableProperties().setTableName(tableName);
        createTable(tableName);
        String requestId = testResourceNamer.randomUuid();

        // Act & Assert
        StepVerifier.create(azureTable.getTables().createWithResponseAsync(tableProperties,
            requestId, ResponseFormat.RETURN_CONTENT, null, Context.NONE))
            .expectError(com.azure.data.tables.implementation.models.TableServiceErrorException.class)
            .verify();
    }

    @Test
    void deleteTable() {
        // Arrange
        String tableName = randomCharOnlyName("test", 20);
        createTable(tableName);
        int expectedStatusCode = 204;
        String requestId = testResourceNamer.randomUuid();

        // Act & Assert
        StepVerifier.create(azureTable.getTables().deleteWithResponseAsync(tableName, requestId,
            Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(expectedStatusCode, response.getStatusCode());
            })
            .expectComplete()
            .verify();
    }

    @Test
    void deleteNonExistentTable() {
        // Arrange
        String tableName = randomCharOnlyName("test", 20);
        String requestId = testResourceNamer.randomUuid();

        // Act & Assert
        StepVerifier.create(azureTable.getTables().deleteWithResponseAsync(tableName, requestId,
            Context.NONE))
            .expectError(com.azure.data.tables.implementation.models.TableServiceErrorException.class)
            .verify();
    }

    @Test
    void queryTable() {
        // Arrange
        QueryOptions queryOptions = new QueryOptions()
            .setFormat(OdataMetadataFormat.APPLICATION_JSON_ODATA_MINIMALMETADATA);
        String tableA = randomCharOnlyName("AtestA", 20);
        String tableB = randomCharOnlyName("BtestB", 20);
        createTable(tableA);
        createTable(tableB);
        int expectedStatusCode = 200;
        String requestId = testResourceNamer.randomUuid();

        // Act & Assert
        StepVerifier.create(azureTable.getTables().queryWithResponseAsync(requestId, null,
            queryOptions, Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(expectedStatusCode, response.getStatusCode());
                Assertions.assertEquals(response.getValue().getValue().get(0).getTableName(), tableA);
                Assertions.assertEquals(response.getValue().getValue().get(1).getTableName(), tableB);
            })
            .expectComplete()
            .verify();
    }

    @Test
    void queryTablewithTop() {
        // Arrange
        QueryOptions queryOptions = new QueryOptions()
            .setFormat(OdataMetadataFormat.APPLICATION_JSON_ODATA_MINIMALMETADATA);
        String tableA = randomCharOnlyName("AtestA", 20);
        String tableB = randomCharOnlyName("BtestB", 20);
        createTable(tableA);
        createTable(tableB);
        int expectedStatusCode = 200;
        int expectedSize = 1;
        String requestId = testResourceNamer.randomUuid();
        queryOptions.setTop(1);

        // Act & Assert
        StepVerifier.create(azureTable.getTables().queryWithResponseAsync(requestId, null,
            queryOptions, Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(expectedStatusCode, response.getStatusCode());
                Assertions.assertEquals(expectedSize, response.getValue().getValue().size());
                Assertions.assertEquals(tableA, response.getValue().getValue().get(0).getTableName());
            })
            .expectComplete()
            .verify();
    }

    @Test
    void insertNoEtag() {
        // Arrange
        String tableName = randomCharOnlyName("test", 20);
        createTable(tableName);
        Map<String, Object> properties = new HashMap<>();
        String partitionKeyValue = randomCharOnlyName("partitionKey", 20);
        String rowKeyValue = randomCharOnlyName("rowKey", 20);
        properties.put(PARTITION_KEY, partitionKeyValue);
        properties.put(ROW_KEY, rowKeyValue);
        int expectedStatusCode = 201;
        String requestId = testResourceNamer.randomUuid();

        // Act & Assert
        StepVerifier.create(azureTable.getTables().insertEntityWithResponseAsync(tableName, TIMEOUT,
            requestId, ResponseFormat.RETURN_CONTENT, properties, null, Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(expectedStatusCode, response.getStatusCode());
            })
            .expectComplete()
            .verify();
    }

    @Test
    void mergeEntity() {
        // Arrange
        String tableName = randomCharOnlyName("test", 20);
        createTable(tableName);
        Map<String, Object> properties = new HashMap<>();
        String partitionKeyValue = randomCharOnlyName("partitionKey", 20);
        String rowKeyValue = randomCharOnlyName("rowKey", 20);
        properties.put(PARTITION_KEY, partitionKeyValue);
        properties.put(ROW_KEY, rowKeyValue);
        int expectedStatusCode = 204;
        String requestId = testResourceNamer.randomUuid();
        insertNoETag(tableName, properties);
        properties.put("extraProperty", randomCharOnlyName("extraProperty", 16));

        // Act & Assert
        StepVerifier.create(azureTable.getTables().mergeEntityWithResponseAsync(tableName, partitionKeyValue,
            rowKeyValue, TIMEOUT, requestId, "*", properties, null, Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(expectedStatusCode, response.getStatusCode());
            })
            .expectComplete()
            .verify();
    }

    @Test
    void mergeNonExistentEntity() {
        // Arrange
        String tableName = randomCharOnlyName("test", 20);
        createTable(tableName);
        Map<String, Object> properties = new HashMap<>();
        String partitionKeyValue = randomCharOnlyName("partitionKey", 20);
        String rowKeyValue = randomCharOnlyName("rowKey", 20);
        String requestId = testResourceNamer.randomUuid();

        // Act & Assert
        StepVerifier.create(azureTable.getTables().mergeEntityWithResponseAsync(tableName, partitionKeyValue,
            rowKeyValue, TIMEOUT, requestId, "*", properties, null, Context.NONE))
            .expectError(com.azure.data.tables.implementation.models.TableServiceErrorException.class)
            .verify();
    }

    @Test
    void updateEntity() {
        // Arrange
        String tableName = randomCharOnlyName("test", 20);
        createTable(tableName);
        Map<String, Object> properties = new HashMap<>();
        String partitionKeyValue = randomCharOnlyName("partitionKey", 20);
        String rowKeyValue = randomCharOnlyName("rowKey", 20);
        properties.put(PARTITION_KEY, partitionKeyValue);
        properties.put(ROW_KEY, rowKeyValue);
        int expectedStatusCode = 204;
        String requestId = testResourceNamer.randomUuid();
        insertNoETag(tableName, properties);
        properties.put("extraProperty", randomCharOnlyName("extraProperty", 16));

        // Act & Assert
        StepVerifier.create(azureTable.getTables().updateEntityWithResponseAsync(tableName, partitionKeyValue,
            rowKeyValue, TIMEOUT, requestId, "*", properties, null, Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(expectedStatusCode, response.getStatusCode());
            })
            .expectComplete()
            .verify();
    }

    @Test
    void updateNonExistentEntity() {
        // Arrange
        String tableName = randomCharOnlyName("test", 20);
        createTable(tableName);
        Map<String, Object> properties = new HashMap<>();
        String partitionKeyValue = randomCharOnlyName("partitionKey", 20);
        String rowKeyValue = randomCharOnlyName("rowKey", 20);
        String requestId = testResourceNamer.randomUuid();

        // Act & Assert
        StepVerifier.create(azureTable.getTables().updateEntityWithResponseAsync(tableName, partitionKeyValue,
            rowKeyValue, TIMEOUT, requestId, "*", properties, null, Context.NONE))
            .expectError(com.azure.data.tables.implementation.models.TableServiceErrorException.class)
            .verify();
    }

    @Test
    void deleteEntity() {
        // Arrange
        String tableName = randomCharOnlyName("test", 20);
        createTable(tableName);
        Map<String, Object> properties = new HashMap<>();
        String partitionKeyValue = randomCharOnlyName("partitionKey", 20);
        String rowKeyValue = randomCharOnlyName("rowKey", 20);
        properties.put(PARTITION_KEY, partitionKeyValue);
        properties.put(ROW_KEY, rowKeyValue);
        int expectedStatusCode = 204;
        String requestId = testResourceNamer.randomUuid();
        insertNoETag(tableName, properties);

        // Act & Assert
        StepVerifier.create(azureTable.getTables().deleteEntityWithResponseAsync(tableName, partitionKeyValue,
            rowKeyValue, "*", TIMEOUT, requestId, null, Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(expectedStatusCode, response.getStatusCode());
            })
            .expectComplete()
            .verify();
    }

    @Test
    void deleteNonExistentEntity() {
        // Arrange
        String tableName = randomCharOnlyName("test", 20);
        createTable(tableName);
        String partitionKeyValue = randomCharOnlyName("partitionKey", 20);
        String rowKeyValue = randomCharOnlyName("rowKey", 20);
        String requestId = testResourceNamer.randomUuid();

        // Act & Assert
        StepVerifier.create(azureTable.getTables().deleteEntityWithResponseAsync(tableName, partitionKeyValue,
            rowKeyValue, "*", TIMEOUT, requestId, null, Context.NONE))
            .expectError(com.azure.data.tables.implementation.models.TableServiceErrorException.class)
            .verify();
    }

    @Test
    void queryEntity() {
        // Arrange
        String requestId = testResourceNamer.randomUuid();
        QueryOptions queryOptions = new QueryOptions().setFormat(OdataMetadataFormat.APPLICATION_JSON_ODATA_FULLMETADATA);
        String tableName = randomCharOnlyName("test", 20);
        createTable(tableName);
        //insert entity A
        Map<String, Object> entityA = new HashMap<>();
        String partitionKeyEntityA = randomCharOnlyName("partitionKeyA", 20);
        entityA.put(PARTITION_KEY, partitionKeyEntityA);
        entityA.put(ROW_KEY, randomCharOnlyName("rowKeyA", 20));
        insertNoETag(tableName, entityA);
        //insert entity B
        Map<String, Object> entityB = new HashMap<>();
        String partitionKeyEntityB = randomCharOnlyName("partitionKeyB", 20);
        entityB.put(PARTITION_KEY, partitionKeyEntityB);
        entityB.put(ROW_KEY, randomCharOnlyName("rowKeyB", 20));
        insertNoETag(tableName, entityB);
        int expectedStatusCode = 200;

        // Act & Assert
        StepVerifier.create(azureTable.getTables().queryEntitiesWithResponseAsync(tableName, TIMEOUT,
            requestId, null, null, queryOptions, Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(expectedStatusCode, response.getStatusCode());
                Assertions.assertEquals(true, response.getValue().getValue().get(0).containsValue(partitionKeyEntityA));
                Assertions.assertEquals(true, response.getValue().getValue().get(1).containsValue(partitionKeyEntityB));

            })
            .expectComplete()
            .verify();
    }

    @Test
    void queryEntityWithSelect() {
        // Arrange
        String requestId = testResourceNamer.randomUuid();
        QueryOptions queryOptions = new QueryOptions().setFormat(OdataMetadataFormat.APPLICATION_JSON_ODATA_FULLMETADATA);
        String tableName = randomCharOnlyName("test", 20);
        createTable(tableName);
        //insert entity A
        Map<String, Object> entityA = new HashMap<>();
        String partitionKeyEntityA = randomCharOnlyName("partitionKeyA", 20);
        String rowKeyEntityA = randomCharOnlyName("rowKeyA", 20);
        entityA.put(PARTITION_KEY, partitionKeyEntityA);
        entityA.put(ROW_KEY, rowKeyEntityA);
        insertNoETag(tableName, entityA);
        //insert entity B
        Map<String, Object> entityB = new HashMap<>();
        String partitionKeyEntityB = randomCharOnlyName("partitionKeyB", 20);
        String rowKeyEntityB = randomCharOnlyName("rowKeyB", 20);
        entityB.put(PARTITION_KEY, partitionKeyEntityB);
        entityB.put(ROW_KEY, rowKeyEntityB);
        insertNoETag(tableName, entityB);
        int expectedStatusCode = 200;
        queryOptions.setSelect(ROW_KEY);

        // Act & Assert
        StepVerifier.create(azureTable.getTables().queryEntitiesWithResponseAsync(tableName, TIMEOUT,
            requestId, null, null, queryOptions, Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(expectedStatusCode, response.getStatusCode());
                Assertions.assertEquals(true, response.getValue().getValue().get(0).containsValue(rowKeyEntityA));
                Assertions.assertEquals(true, response.getValue().getValue().get(1).containsValue(rowKeyEntityB));

            })
            .expectComplete()
            .verify();
    }

    @Test
    void queryEntityWithFilter() {
        // Arrange
        String requestId = testResourceNamer.randomUuid();
        QueryOptions queryOptions = new QueryOptions().setFormat(OdataMetadataFormat.APPLICATION_JSON_ODATA_FULLMETADATA);
        String tableName = randomCharOnlyName("test", 20);
        createTable(tableName);
        Map<String, Object> entityA = new HashMap<>();
        String partitionKeyEntityA = randomCharOnlyName("partitionKeyA", 20);
        entityA.put(PARTITION_KEY, partitionKeyEntityA);
        entityA.put(ROW_KEY, randomCharOnlyName("rowKeyA", 20));
        insertNoETag(tableName, entityA);
        int expectedStatusCode = 200;
        queryOptions.setSelect(PARTITION_KEY + "eq" + partitionKeyEntityA);

        // Act & Assert
        StepVerifier.create(azureTable.getTables().queryEntitiesWithResponseAsync(tableName, TIMEOUT,
            requestId, null, null, queryOptions, Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(expectedStatusCode, response.getStatusCode());

            })
            .expectComplete()
            .verify();
    }

    @Test
    void queryEntityWithTop() {
        // Arrange
        String requestId = testResourceNamer.randomUuid();
        QueryOptions queryOptions = new QueryOptions().setFormat(OdataMetadataFormat.APPLICATION_JSON_ODATA_FULLMETADATA);
        String tableName = randomCharOnlyName("test", 20);
        createTable(tableName);
        int expectedStatusCode = 200;
        queryOptions.setTop(0);

        // Act & Assert
        StepVerifier.create(azureTable.getTables().queryEntitiesWithResponseAsync(tableName, TIMEOUT,
            requestId, null, null, queryOptions, Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(expectedStatusCode, response.getStatusCode());

            })
            .expectComplete()
            .verify();
    }


    @Test
    void queryEntitiesWithPartitionAndRowKey() {
        // Arrange
        QueryOptions queryOptions = new QueryOptions().setFormat(OdataMetadataFormat.APPLICATION_JSON_ODATA_FULLMETADATA);
        String tableName = randomCharOnlyName("test", 20);
        createTable(tableName);
        Map<String, Object> properties = new HashMap<>();
        String partitionKeyValue = randomCharOnlyName("partitionKey", 20);
        String rowKeyValue = randomCharOnlyName("rowKey", 20);
        properties.put(PARTITION_KEY, partitionKeyValue);
        properties.put(ROW_KEY, rowKeyValue);
        insertNoETag(tableName, properties);

        // Act & Assert
        StepVerifier.create(azureTable.getTables().queryEntitiesWithPartitionAndRowKeyWithResponseAsync(tableName, partitionKeyValue,
            rowKeyValue, TIMEOUT, UUID.randomUUID().toString(), queryOptions, Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(200, response.getStatusCode());
            })
            .expectComplete()
            .verify();
    }

}
