// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.*;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.tables.implementation.AzureTableImpl;
import com.azure.data.tables.implementation.AzureTableImplBuilder;
import com.azure.data.tables.implementation.models.*;
import com.azure.storage.common.implementation.connectionstring.StorageAuthenticationSettings;
import com.azure.storage.common.implementation.connectionstring.StorageConnectionString;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import com.azure.core.test.TestBase;
import java.util.*;

/**
 * This class tests the Autorest code for the Tables track 2 SDK
 */
public class AzureTableImplTest extends TestBase {
    private static final String PARTITION_KEY = "PartitionKey";
    private static final String ROW_KEY = "RowKey";
    private AzureTableImpl azureTable;


        AzureTableImpl auth() {
        String connectionString = interceptorManager.isPlaybackMode()
            ? "DefaultEndpointsProtocol=https;=AccountName=dummyAccount;AccountKey=xyzDummy;EndpointSuffix=core.windows.net"
            : System.getenv("azure_tables_connection_string");
        StorageConnectionString storageConnectionString
            = StorageConnectionString.create(connectionString, new ClientLogger(AzureTableImplTest.class));

        StorageAuthenticationSettings authSettings = storageConnectionString.getStorageAuthSettings();
        TablesSharedKeyCredential sharedKeyCredential = new TablesSharedKeyCredential(authSettings.getAccount().getName(),
            authSettings.getAccount().getAccessKey());

        final List<HttpPipelinePolicy> policies = Arrays.asList(
            new AddDatePolicy(),
            new AddHeadersPolicy(new HttpHeaders().put("Accept", OdataMetadataFormat.APPLICATION_JSON_ODATA_MINIMALMETADATA.toString())),
            new TablesSharedKeyCredentialPolicy(sharedKeyCredential),
            new HttpLoggingPolicy(new HttpLogOptions()
                .setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
        );
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .build();
        AzureTableImpl azureTable = new AzureTableImplBuilder()
            .pipeline(pipeline)
            .version("2019-02-02")
            .url(storageConnectionString.getTableEndpoint().getPrimaryUri())
            .buildClient();
        return azureTable;
    }

    @Override
    protected void beforeTest() {
        super.beforeTest();
    }

    @BeforeEach
    void before() {
        azureTable = auth();

    }

    @AfterEach
    void after() {
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

        azureTable.getTables().createWithResponseAsync(tableProperties, testResourceNamer.randomUuid(),
            ResponseFormat.RETURN_CONTENT, null, Context.NONE).block();


    }

    void insertNoEtag(String tableName, Map<String, Object> properties) {
        azureTable.getTables().insertEntityWithResponseAsync(tableName, 500,
            UUID.randomUUID().toString(), ResponseFormat.RETURN_CONTENT, properties, null, Context.NONE).log().block();
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
        String tableName = randomCharOnlyName("test", 10);
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
        String tableName = randomCharOnlyName("test", 10);
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
        String tableName = randomCharOnlyName("test", 10);
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
        String tableName = randomCharOnlyName("test", 10);
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
        String tableA = randomCharOnlyName("AtestA", 10);
        String tableB = randomCharOnlyName("BtestB", 10);
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
    void insertNoEtag() {
        // Arrange
        String tableName = randomCharOnlyName("test", 10);
        createTable(tableName);
        Map<String, Object> properties = new HashMap<>();
        String pkName = randomCharOnlyName("pk", 10);
        String rkName = randomCharOnlyName("rk", 10);
        properties.put(PARTITION_KEY, pkName);
        properties.put(ROW_KEY, rkName);
        int expectedStatusCode = 201;
        String requestId = testResourceNamer.randomUuid();

        // Act & Assert
        StepVerifier.create(azureTable.getTables().insertEntityWithResponseAsync(tableName, 500,
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
        String tableName = randomCharOnlyName("test", 10);
        createTable(tableName);
        Map<String, Object> properties = new HashMap<>();
        String pkName = randomCharOnlyName("pk", 10);
        String rkName = randomCharOnlyName("rk", 10);
        properties.put(PARTITION_KEY, pkName);
        properties.put(ROW_KEY, rkName);
        int expectedStatusCode = 204;
        String requestId = testResourceNamer.randomUuid();
        insertNoEtag(tableName, properties);
        properties.put("additionalProperty", randomCharOnlyName("ap", 10));

        // Act & Assert
        StepVerifier.create(azureTable.getTables().mergeEntityWithResponseAsync(tableName, pkName,
            rkName, 500, requestId, "*", properties, null, Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(expectedStatusCode, response.getStatusCode());
            })
            .expectComplete()
            .verify();
    }

    @Test
    void mergeNonExistentEntity() {
        // Arrange
        String tableName = randomCharOnlyName("test", 10);
        createTable(tableName);
        Map<String, Object> properties = new HashMap<>();
        String pkName = randomCharOnlyName("pk", 10);
        String rkName = randomCharOnlyName("rk", 10);
        String requestId = testResourceNamer.randomUuid();

        // Act & Assert
        StepVerifier.create(azureTable.getTables().mergeEntityWithResponseAsync(tableName, pkName,
            rkName, 500, requestId, "*", properties, null, Context.NONE))
            .expectError(com.azure.data.tables.implementation.models.TableServiceErrorException.class)
            .verify();
    }

    @Test
    void updateEntity() {
        // Arrange
        String tableName = randomCharOnlyName("test", 10);
        createTable(tableName);
        Map<String, Object> properties = new HashMap<>();
        String pkName = randomCharOnlyName("pk", 10);
        String rkName = randomCharOnlyName("rk", 10);
        properties.put(PARTITION_KEY, pkName);
        properties.put(ROW_KEY, rkName);
        int expectedStatusCode = 204;
        String requestId = testResourceNamer.randomUuid();
        insertNoEtag(tableName, properties);
        properties.put("additionalProperty", randomCharOnlyName("ap", 10));

        // Act & Assert
        StepVerifier.create(azureTable.getTables().updateEntityWithResponseAsync(tableName, pkName,
            rkName, 500, requestId, "*", properties, null, Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(expectedStatusCode, response.getStatusCode());
            })
            .expectComplete()
            .verify();
    }

    @Test
    void updateNonExistentEntity() {
        // Arrange
        String tableName = randomCharOnlyName("test", 10);
        createTable(tableName);
        Map<String, Object> properties = new HashMap<>();
        String pkName = randomCharOnlyName("pk", 10);
        String rkName = randomCharOnlyName("rk", 10);
        String requestId = testResourceNamer.randomUuid();

        // Act & Assert
        StepVerifier.create(azureTable.getTables().updateEntityWithResponseAsync(tableName, pkName,
            rkName, 500, requestId, "*", properties, null, Context.NONE))
            .expectError(com.azure.data.tables.implementation.models.TableServiceErrorException.class)
            .verify();
    }

    @Test
    void deleteEntity() {
        // Arrange
        String tableName = randomCharOnlyName("test", 10);
        createTable(tableName);
        Map<String, Object> properties = new HashMap<>();
        String pkName = randomCharOnlyName("pk", 10);
        String rkName = randomCharOnlyName("rk", 10);
        properties.put(PARTITION_KEY, pkName);
        properties.put(ROW_KEY, rkName);
        int expectedStatusCode = 204;
        String requestId = testResourceNamer.randomUuid();
        insertNoEtag(tableName, properties);

        // Act & Assert
        StepVerifier.create(azureTable.getTables().deleteEntityWithResponseAsync(tableName, pkName,
            rkName, "*", 500, requestId, null, Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(expectedStatusCode, response.getStatusCode());
            })
            .expectComplete()
            .verify();
    }

    @Test
    void deleteNonExistentEntity() {
        // Arrange
        String tableName = randomCharOnlyName("test", 10);
        createTable(tableName);
        String pkName = randomCharOnlyName("pk", 10);
        String rkName = randomCharOnlyName("rk", 10);
        String requestId = testResourceNamer.randomUuid();

        // Act & Assert
        StepVerifier.create(azureTable.getTables().deleteEntityWithResponseAsync(tableName, pkName,
            rkName, "*", 500, requestId, null, Context.NONE))
            .expectError(com.azure.data.tables.implementation.models.TableServiceErrorException.class)
            .verify();
    }

    @Test
    void queryEntity() {
        // Arrange
        String requestId = testResourceNamer.randomUuid();
        QueryOptions queryOptions = new QueryOptions().setFormat(OdataMetadataFormat.APPLICATION_JSON_ODATA_FULLMETADATA);
        String tableName = randomCharOnlyName("test", 10);
        createTable(tableName);
        Map<String, Object> propertiesA = new HashMap<>();
        String pkNameA = randomCharOnlyName("pkA", 10);
        String rkNameA = randomCharOnlyName("rkA", 10);
        propertiesA.put(PARTITION_KEY, pkNameA);
        propertiesA.put(ROW_KEY, rkNameA);
        insertNoEtag(tableName, propertiesA);
        Map<String, Object> propertiesB = new HashMap<>();
        String pkNameB = randomCharOnlyName("pkB", 10);
        String rkNameB = randomCharOnlyName("rkB", 10);
        propertiesB.put(PARTITION_KEY, pkNameB);
        propertiesB.put(ROW_KEY, rkNameB);
        insertNoEtag(tableName, propertiesB);
        int expectedStatusCode = 200;

        // Act & Assert
        StepVerifier.create(azureTable.getTables().queryEntitiesWithResponseAsync(tableName, null,
            requestId, null, null, queryOptions, Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(expectedStatusCode, response.getStatusCode());
                Assertions.assertEquals(true, response.getValue().getValue().get(0).containsValue(pkNameA));
                Assertions.assertEquals(true, response.getValue().getValue().get(1).containsValue(pkNameB));

            })
            .expectComplete()
            .verify();
    }

    @Test
    void queryEntityWithSelect() {
        // Arrange
        String ap = "additionalProperty";
        String requestId = testResourceNamer.randomUuid();
        QueryOptions queryOptions = new QueryOptions().setFormat(OdataMetadataFormat.APPLICATION_JSON_ODATA_FULLMETADATA);
        String tableName = randomCharOnlyName("test", 10);
        createTable(tableName);
        Map<String, Object> propertiesA = new HashMap<>();
        String apNameA = randomCharOnlyName("apA", 10);
        propertiesA.put(PARTITION_KEY, randomCharOnlyName("pkA", 10));
        propertiesA.put(ROW_KEY, randomCharOnlyName("rkA", 10));
        propertiesA.put(ap, apNameA);
        insertNoEtag(tableName, propertiesA);
        Map<String, Object> propertiesB = new HashMap<>();
        String apNameB = randomCharOnlyName("apA", 10);
        propertiesB.put(PARTITION_KEY, randomCharOnlyName("pkB", 10));
        propertiesB.put(ROW_KEY, randomCharOnlyName("rkB", 10));
        propertiesB.put(ap, apNameB);
        insertNoEtag(tableName, propertiesB);
        int expectedStatusCode = 200;
        queryOptions.setSelect(ap);

        // Act & Assert
        StepVerifier.create(azureTable.getTables().queryEntitiesWithResponseAsync(tableName, null,
            requestId, null, null, queryOptions, Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(expectedStatusCode, response.getStatusCode());
                Assertions.assertEquals(true, response.getValue().getValue().get(0).containsValue(apNameA));
                Assertions.assertEquals(true, response.getValue().getValue().get(1).containsValue(apNameB));

            })
            .expectComplete()
            .verify();
    }

    @Test
    void queryEntityWithFilter() {
        // Arrange
        String ap = "additionalProperty";
        String requestId = testResourceNamer.randomUuid();
        QueryOptions queryOptions = new QueryOptions().setFormat(OdataMetadataFormat.APPLICATION_JSON_ODATA_FULLMETADATA);
        String tableName = randomCharOnlyName("test", 10);
        createTable(tableName);
        Map<String, Object> propertiesA = new HashMap<>();
        String apNameA = randomCharOnlyName("apA", 10);
        propertiesA.put(PARTITION_KEY, randomCharOnlyName("pkA", 10));
        propertiesA.put(ROW_KEY, randomCharOnlyName("rkA", 10));
        propertiesA.put(ap, apNameA);
        insertNoEtag(tableName, propertiesA);
        Map<String, Object> propertiesB = new HashMap<>();
        String apNameB = randomCharOnlyName("apA", 10);
        propertiesB.put(PARTITION_KEY, randomCharOnlyName("pkB", 10));
        propertiesB.put(ROW_KEY, randomCharOnlyName("rkB", 10));
        propertiesB.put(ap, apNameB);
        insertNoEtag(tableName, propertiesB);
        int expectedStatusCode = 200;
        queryOptions.setFilter(ap + " eq " + apNameA);

        // Act & Assert
        StepVerifier.create(azureTable.getTables().queryEntitiesWithResponseAsync(tableName, null,
            requestId, null, null, queryOptions, Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(expectedStatusCode, response.getStatusCode());
                Assertions.assertEquals(true, response.getValue().getValue().get(0).containsValue(apNameA));
                // Assertions.assertEquals(false, response.getValue().getValue().get(1).containsValue(apNameB));

            })
            .expectComplete()
            .verify();
    }

    @Test
    void queryEntitiesWithPartitionAndRowKey() {
        // Arrange
        QueryOptions queryOptions = new QueryOptions().setFormat(OdataMetadataFormat.APPLICATION_JSON_ODATA_FULLMETADATA);
        String tableName = randomCharOnlyName("test", 10);
        createTable(tableName);
        Map<String, Object> properties = new HashMap<>();
        String pkName = randomCharOnlyName("pk", 10);
        String rkName = randomCharOnlyName("rk", 10);
        properties.put(PARTITION_KEY, pkName);
        properties.put(ROW_KEY, rkName);
        insertNoEtag(tableName, properties);

        // Act & Assert
        StepVerifier.create(azureTable.getTables().queryEntitiesWithPartitionAndRowKeyWithResponseAsync(tableName, pkName,
            rkName, 1000, UUID.randomUUID().toString(), queryOptions, Context.NONE))
            .assertNext(response -> {
                Assertions.assertEquals(200, response.getStatusCode());
            })
            .expectComplete()
            .verify();
    }

}
