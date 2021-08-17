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
import com.azure.data.tables.models.ListTablesOptions;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableServiceCorsRule;
import com.azure.data.tables.models.TableServiceException;
import com.azure.data.tables.models.TableServiceLogging;
import com.azure.data.tables.models.TableServiceMetrics;
import com.azure.data.tables.models.TableServiceProperties;
import com.azure.data.tables.models.TableServiceRetentionPolicy;
import com.azure.data.tables.sas.TableAccountSasPermission;
import com.azure.data.tables.sas.TableAccountSasResourceType;
import com.azure.data.tables.sas.TableAccountSasService;
import com.azure.data.tables.sas.TableAccountSasSignatureValues;
import com.azure.data.tables.sas.TableSasIpRange;
import com.azure.data.tables.sas.TableSasProtocol;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import static com.azure.data.tables.TestUtils.assertPropertiesEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests methods for {@link TableServiceAsyncClient}.
 */
public class TableServiceAsyncClientTest extends TestBase {
    private static final Duration TIMEOUT = Duration.ofSeconds(100);
    private static final HttpClient DEFAULT_HTTP_CLIENT = HttpClient.createDefault();
    private static final boolean IS_COSMOS_TEST = System.getenv("AZURE_TABLES_CONNECTION_STRING") != null
        && System.getenv("AZURE_TABLES_CONNECTION_STRING").contains("cosmos.azure.com");

    private TableServiceAsyncClient serviceClient;
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
        final String connectionString = TestUtils.getConnectionString(interceptorManager.isPlaybackMode());
        final TableServiceClientBuilder builder = new TableServiceClientBuilder()
            .connectionString(connectionString)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

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

        serviceClient = builder.buildAsyncClient();
    }

    @Test
    void serviceCreateTableAsync() {
        // Arrange
        String tableName = testResourceNamer.randomName("test", 20);

        //Act & Assert
        StepVerifier.create(serviceClient.createTable(tableName))
            .assertNext(Assertions::assertNotNull)
            .expectComplete()
            .verify();
    }

    @Test
    void serviceCreateTableWithResponseAsync() {
        // Arrange
        String tableName = testResourceNamer.randomName("test", 20);
        int expectedStatusCode = 204;

        //Act & Assert
        StepVerifier.create(serviceClient.createTableWithResponse(tableName))
            .assertNext(response -> {
                assertEquals(expectedStatusCode, response.getStatusCode());
                assertNotNull(response.getValue());
            })
            .expectComplete()
            .verify();
    }

    @Test
    void serviceCreateTableFailsIfExistsAsync() {
        // Arrange
        String tableName = testResourceNamer.randomName("test", 20);
        serviceClient.createTable(tableName).block(TIMEOUT);

        //Act & Assert
        StepVerifier.create(serviceClient.createTable(tableName))
            .expectErrorMatches(e -> e instanceof TableServiceException
                && ((TableServiceException) e).getResponse().getStatusCode() == 409)
            .verify();
    }

    @Test
    void serviceCreateTableIfNotExistsAsync() {
        // Arrange
        String tableName = testResourceNamer.randomName("test", 20);

        //Act & Assert
        StepVerifier.create(serviceClient.createTableIfNotExists(tableName))
            .assertNext(Assertions::assertNotNull)
            .expectComplete()
            .verify();
    }

    @Test
    void serviceCreateTableIfNotExistsSucceedsIfExistsAsync() {
        // Arrange
        String tableName = testResourceNamer.randomName("test", 20);
        serviceClient.createTable(tableName).block(TIMEOUT);

        //Act & Assert
        StepVerifier.create(serviceClient.createTableIfNotExists(tableName))
            .expectComplete()
            .verify();
    }

    @Test
    void serviceCreateTableIfNotExistsWithResponseAsync() {
        // Arrange
        String tableName = testResourceNamer.randomName("test", 20);
        int expectedStatusCode = 204;

        //Act & Assert
        StepVerifier.create(serviceClient.createTableIfNotExistsWithResponse(tableName))
            .assertNext(response -> {
                assertEquals(expectedStatusCode, response.getStatusCode());
                assertNotNull(response.getValue());
            })
            .expectComplete()
            .verify();
    }

    @Test
    void serviceCreateTableIfNotExistsWithResponseSucceedsIfExistsAsync() {
        // Arrange
        String tableName = testResourceNamer.randomName("test", 20);
        int expectedStatusCode = 409;
        serviceClient.createTable(tableName).block(TIMEOUT);

        //Act & Assert
        StepVerifier.create(serviceClient.createTableIfNotExistsWithResponse(tableName))
            .assertNext(response -> {
                assertEquals(expectedStatusCode, response.getStatusCode());
                assertNull(response.getValue());
            })
            .expectComplete()
            .verify();
    }

    @Test
    void serviceDeleteTableAsync() {
        // Arrange
        final String tableName = testResourceNamer.randomName("test", 20);
        serviceClient.createTable(tableName).block(TIMEOUT);

        //Act & Assert
        StepVerifier.create(serviceClient.deleteTable(tableName))
            .expectComplete()
            .verify();
    }

    @Test
    void serviceDeleteNonExistingTableAsync() {
        // Arrange
        final String tableName = testResourceNamer.randomName("test", 20);

        //Act & Assert
        StepVerifier.create(serviceClient.deleteTable(tableName))
            .expectComplete()
            .verify();
    }

    @Test
    void serviceDeleteTableWithResponseAsync() {
        // Arrange
        String tableName = testResourceNamer.randomName("test", 20);
        int expectedStatusCode = 204;
        serviceClient.createTable(tableName).block();

        //Act & Assert
        StepVerifier.create(serviceClient.deleteTableWithResponse(tableName))
            .assertNext(response -> assertEquals(expectedStatusCode, response.getStatusCode()))
            .expectComplete()
            .verify();
    }

    @Test
    void serviceDeleteNonExistingTableWithResponseAsync() {
        // Arrange
        String tableName = testResourceNamer.randomName("test", 20);
        int expectedStatusCode = 404;

        //Act & Assert
        StepVerifier.create(serviceClient.deleteTableWithResponse(tableName))
            .assertNext(response -> assertEquals(expectedStatusCode, response.getStatusCode()))
            .expectComplete()
            .verify();
    }

    @Test
    void serviceListTablesAsync() {
        // Arrange
        final String tableName = testResourceNamer.randomName("test", 20);
        final String tableName2 = testResourceNamer.randomName("test", 20);
        serviceClient.createTable(tableName).block(TIMEOUT);
        serviceClient.createTable(tableName2).block(TIMEOUT);

        // Act & Assert
        StepVerifier.create(serviceClient.listTables())
            .expectNextCount(2)
            .thenConsumeWhile(x -> true)
            .expectComplete()
            .verify();
    }

    @Test
    void serviceListTablesWithFilterAsync() {
        // Arrange
        final String tableName = testResourceNamer.randomName("test", 20);
        final String tableName2 = testResourceNamer.randomName("test", 20);
        ListTablesOptions options = new ListTablesOptions().setFilter("TableName eq '" + tableName + "'");
        serviceClient.createTable(tableName).block(TIMEOUT);
        serviceClient.createTable(tableName2).block(TIMEOUT);

        // Act & Assert
        StepVerifier.create(serviceClient.listTables(options))
            .assertNext(table -> assertEquals(tableName, table.getName()))
            .expectNextCount(0)
            .thenConsumeWhile(x -> true)
            .expectComplete()
            .verify();
    }

    @Test
    void serviceListTablesWithTopAsync() {
        // Arrange
        final String tableName = testResourceNamer.randomName("test", 20);
        final String tableName2 = testResourceNamer.randomName("test", 20);
        final String tableName3 = testResourceNamer.randomName("test", 20);
        ListTablesOptions options = new ListTablesOptions().setTop(2);
        serviceClient.createTable(tableName).block(TIMEOUT);
        serviceClient.createTable(tableName2).block(TIMEOUT);
        serviceClient.createTable(tableName3).block(TIMEOUT);

        // Act & Assert
        StepVerifier.create(serviceClient.listTables(options))
            .expectNextCount(2)
            .thenConsumeWhile(x -> true)
            .expectComplete()
            .verify();
    }

    @Test
    void serviceGetTableClientAsync() {
        // Arrange
        final String tableName = testResourceNamer.randomName("test", 20);
        serviceClient.createTable(tableName).block(TIMEOUT);

        TableAsyncClient tableClient = serviceClient.getTableClient(tableName);

        // Act & Assert
        TableAsyncClientTest.getEntityWithResponseAsyncImpl(tableClient, this.testResourceNamer);
    }

    @Test
    public void generateAccountSasTokenWithMinimumParameters() {
        final OffsetDateTime expiryTime = OffsetDateTime.of(2021, 12, 12, 0, 0, 0, 0, ZoneOffset.UTC);
        final TableAccountSasPermission permissions = TableAccountSasPermission.parse("r");
        final TableAccountSasService services = new TableAccountSasService().setTableAccess(true);
        final TableAccountSasResourceType resourceTypes = new TableAccountSasResourceType().setObject(true);
        final TableSasProtocol protocol = TableSasProtocol.HTTPS_ONLY;

        final TableAccountSasSignatureValues sasSignatureValues =
            new TableAccountSasSignatureValues(expiryTime, permissions, services, resourceTypes)
                .setProtocol(protocol)
                .setVersion(TableServiceVersion.V2019_02_02.getVersion());

        final String sas = serviceClient.generateAccountSas(sasSignatureValues);

        assertTrue(
            sas.startsWith(
                "sv=2019-02-02"
                    + "&ss=t"
                    + "&srt=o"
                    + "&se=2021-12-12T00%3A00%3A00Z"
                    + "&sp=r"
                    + "&spr=https"
                    + "&sig="
            )
        );
    }

    @Test
    public void generateAccountSasTokenWithAllParameters() {
        final OffsetDateTime expiryTime = OffsetDateTime.of(2021, 12, 12, 0, 0, 0, 0, ZoneOffset.UTC);
        final TableAccountSasPermission permissions = TableAccountSasPermission.parse("rdau");
        final TableAccountSasService services = new TableAccountSasService().setTableAccess(true);
        final TableAccountSasResourceType resourceTypes = new TableAccountSasResourceType().setObject(true);
        final TableSasProtocol protocol = TableSasProtocol.HTTPS_HTTP;

        final OffsetDateTime startTime = OffsetDateTime.of(2015, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        final TableSasIpRange ipRange = TableSasIpRange.parse("a-b");

        final TableAccountSasSignatureValues sasSignatureValues =
            new TableAccountSasSignatureValues(expiryTime, permissions, services, resourceTypes)
                .setProtocol(protocol)
                .setVersion(TableServiceVersion.V2019_02_02.getVersion())
                .setStartTime(startTime)
                .setSasIpRange(ipRange);

        final String sas = serviceClient.generateAccountSas(sasSignatureValues);

        assertTrue(
            sas.startsWith(
                "sv=2019-02-02"
                    + "&ss=t"
                    + "&srt=o"
                    + "&st=2015-01-01T00%3A00%3A00Z"
                    + "&se=2021-12-12T00%3A00%3A00Z"
                    + "&sp=rdau"
                    + "&sip=a-b"
                    + "&spr=https%2Chttp"
                    + "&sig="
            )
        );
    }

    @Test
    public void canUseSasTokenToCreateValidTableClient() {
        final OffsetDateTime expiryTime = OffsetDateTime.of(2021, 12, 12, 0, 0, 0, 0, ZoneOffset.UTC);
        final TableAccountSasPermission permissions = TableAccountSasPermission.parse("a");
        final TableAccountSasService services = new TableAccountSasService().setTableAccess(true);
        final TableAccountSasResourceType resourceTypes = new TableAccountSasResourceType().setObject(true);
        final TableSasProtocol protocol = TableSasProtocol.HTTPS_ONLY;

        final TableAccountSasSignatureValues sasSignatureValues =
            new TableAccountSasSignatureValues(expiryTime, permissions, services, resourceTypes)
                .setProtocol(protocol)
                .setVersion(TableServiceVersion.V2019_02_02.getVersion());

        final String sas = serviceClient.generateAccountSas(sasSignatureValues);
        final String tableName = testResourceNamer.randomName("test", 20);

        serviceClient.createTable(tableName).block(TIMEOUT);

        final TableClientBuilder tableClientBuilder = new TableClientBuilder()
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .endpoint(serviceClient.getServiceEndpoint())
            .sasToken(sas)
            .tableName(tableName);

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

        //Act & Assert
        StepVerifier.create(tableAsyncClient.createEntityWithResponse(entity))
            .assertNext(response -> assertEquals(expectedStatusCode, response.getStatusCode()))
            .expectComplete()
            .verify();
    }

    @Test
    public void setGetProperties() {
        Assumptions.assumeFalse(IS_COSMOS_TEST,
            "Setting and getting properties is not supported on Cosmos endpoints.");

        TableServiceRetentionPolicy retentionPolicy = new TableServiceRetentionPolicy()
            .setDaysToRetain(5)
            .setEnabled(true);

        TableServiceLogging logging = new TableServiceLogging()
            .setReadLogged(true)
            .setAnalyticsVersion("1.0")
            .setRetentionPolicy(retentionPolicy);

        List<TableServiceCorsRule> corsRules = new ArrayList<>();
        corsRules.add(new TableServiceCorsRule()
            .setAllowedMethods("GET,PUT,HEAD")
            .setAllowedOrigins("*")
            .setAllowedHeaders("x-ms-version")
            .setExposedHeaders("x-ms-client-request-id")
            .setMaxAgeInSeconds(10));

        TableServiceMetrics hourMetrics = new TableServiceMetrics()
            .setEnabled(true)
            .setVersion("1.0")
            .setRetentionPolicy(retentionPolicy)
            .setIncludeApis(true);

        TableServiceMetrics minuteMetrics = new TableServiceMetrics()
            .setEnabled(true)
            .setVersion("1.0")
            .setRetentionPolicy(retentionPolicy)
            .setIncludeApis(true);

        TableServiceProperties sentProperties = new TableServiceProperties()
            .setLogging(logging)
            .setCorsRules(corsRules)
            .setMinuteMetrics(minuteMetrics)
            .setHourMetrics(hourMetrics);

        StepVerifier.create(serviceClient.setPropertiesWithResponse(sentProperties))
            .assertNext(response -> {
                assertNotNull(response.getHeaders().getValue("x-ms-request-id"));
                assertNotNull(response.getHeaders().getValue("x-ms-version"));
            })
            .expectComplete()
            .verify();

        // Service properties may take up to 30s to take effect. If they weren't already in place, wait.
        sleepIfRunningAgainstService(30000);

        StepVerifier.create(serviceClient.getProperties())
            .assertNext(retrievedProperties -> assertPropertiesEquals(sentProperties, retrievedProperties))
            .expectComplete()
            .verify();
    }

    @Test
    public void getStatistics() throws URISyntaxException {
        Assumptions.assumeFalse(IS_COSMOS_TEST, "Getting statistics is not supported on Cosmos endpoints.");

        URI primaryEndpoint = new URI(serviceClient.getServiceEndpoint());
        String[] hostParts = primaryEndpoint.getHost().split("\\.");
        StringJoiner secondaryHostJoiner = new StringJoiner(".");
        secondaryHostJoiner.add(hostParts[0] + "-secondary");

        for (int i = 1; i < hostParts.length; i++) {
            secondaryHostJoiner.add(hostParts[i]);
        }

        String secondaryEndpoint = primaryEndpoint.getScheme() + "://" + secondaryHostJoiner;

        TableServiceAsyncClient secondaryClient = new TableServiceClientBuilder()
            .endpoint(secondaryEndpoint)
            .serviceVersion(serviceClient.getServiceVersion())
            .pipeline(serviceClient.getHttpPipeline())
            .buildAsyncClient();

        StepVerifier.create(secondaryClient.getStatistics())
            .assertNext(statistics -> {
                assertNotNull(statistics);
                assertNotNull(statistics.getGeoReplication());
                assertNotNull(statistics.getGeoReplication().getStatus());
                assertNotNull(statistics.getGeoReplication().getLastSyncTime());
            })
            .expectComplete()
            .verify();
    }
}
