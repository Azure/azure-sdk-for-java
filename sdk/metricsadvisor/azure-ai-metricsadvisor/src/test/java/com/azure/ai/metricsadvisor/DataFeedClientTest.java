// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient;
import com.azure.ai.metricsadvisor.models.DataFeed;
import com.azure.ai.metricsadvisor.models.DataFeedGranularityType;
import com.azure.ai.metricsadvisor.models.DataFeedSourceType;
import com.azure.ai.metricsadvisor.models.DataFeedStatus;
import com.azure.ai.metricsadvisor.models.ErrorCode;
import com.azure.ai.metricsadvisor.models.ErrorCodeException;
import com.azure.ai.metricsadvisor.models.ListDataFeedFilter;
import com.azure.ai.metricsadvisor.models.ListDataFeedOptions;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorServiceVersion;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.azure.ai.metricsadvisor.TestUtils.DATAFEED_ID_REQUIRED_ERROR;
import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.metricsadvisor.TestUtils.INCORRECT_UUID;
import static com.azure.ai.metricsadvisor.TestUtils.INCORRECT_UUID_ERROR;
import static com.azure.ai.metricsadvisor.models.DataFeedGranularityType.DAILY;
import static com.azure.ai.metricsadvisor.models.DataFeedSourceType.AZURE_APP_INSIGHTS;
import static com.azure.ai.metricsadvisor.models.DataFeedSourceType.AZURE_BLOB;
import static com.azure.ai.metricsadvisor.models.DataFeedSourceType.AZURE_COSMOS_DB;
import static com.azure.ai.metricsadvisor.models.DataFeedSourceType.AZURE_DATA_EXPLORER;
import static com.azure.ai.metricsadvisor.models.DataFeedSourceType.AZURE_DATA_LAKE_STORAGE_GEN2;
import static com.azure.ai.metricsadvisor.models.DataFeedSourceType.AZURE_TABLE;
import static com.azure.ai.metricsadvisor.models.DataFeedSourceType.ELASTIC_SEARCH;
import static com.azure.ai.metricsadvisor.models.DataFeedSourceType.HTTP_REQUEST;
import static com.azure.ai.metricsadvisor.models.DataFeedSourceType.INFLUX_DB;
import static com.azure.ai.metricsadvisor.models.DataFeedSourceType.MONGO_DB;
import static com.azure.ai.metricsadvisor.models.DataFeedSourceType.MYSQL_DB;
import static com.azure.ai.metricsadvisor.models.DataFeedSourceType.POSTGRE_SQL_DB;
import static com.azure.ai.metricsadvisor.models.DataFeedSourceType.SQL_SERVER_DB;
import static com.azure.ai.metricsadvisor.models.DataFeedStatus.ACTIVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataFeedClientTest extends DataFeedTestBase {
    private MetricsAdvisorAdministrationClient client;

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    /**
     * Verifies the result of the list data feed method when no options specified.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    void testListDataFeed(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();

        listDataFeedRunner(inputDataFeedList -> {
            List<DataFeed> actualDataFeedList = new ArrayList<>();
            List<DataFeed> expectedDataFeedList =
                inputDataFeedList.stream().map(dataFeed -> client.createDataFeed(dataFeed.getName(),
                    dataFeed.getSource(), dataFeed.getGranularity(),
                    dataFeed.getSchema(), dataFeed.getIngestionSettings(), dataFeed.getOptions()))
                    .collect(Collectors.toList());

            // Act & Assert
            client.listDataFeeds().forEach(actualDataFeedList::add);

            final List<String> expectedDataFeedIdList = expectedDataFeedList.stream().map(DataFeed::getId)
                .collect(Collectors.toList());
            final List<DataFeed> actualList =
                actualDataFeedList.stream().filter(dataFeed -> expectedDataFeedIdList.contains(dataFeed.getId()))
                    .collect(Collectors.toList());

            assertEquals(inputDataFeedList.size(), actualList.size());
            expectedDataFeedList.sort(Comparator.comparing(DataFeed::getSourceType));
            actualList.sort(Comparator.comparing(DataFeed::getSourceType));
            final AtomicInteger i = new AtomicInteger(-1);
            final List<DataFeedSourceType> dataFeedSourceTypes = Arrays.asList(AZURE_BLOB, SQL_SERVER_DB);
            expectedDataFeedList.forEach(expectedDataFeed -> validateDataFeedResult(expectedDataFeed,
                actualList.get(i.incrementAndGet()), dataFeedSourceTypes.get(i.get())));

            expectedDataFeedIdList.forEach(client::deleteDataFeed);
        });
    }

    /**
     * Verifies the result of the list data feed method to return only 3 results using
     * {@link ListDataFeedOptions#setTop(Integer)}.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    void testListDataFeedTop3(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();

        // Act & Assert
        for (PagedResponse<DataFeed> dataFeedPagedResponse : client.listDataFeeds(new ListDataFeedOptions().setTop(3),
            Context.NONE)
            .iterableByPage()) {
            assertTrue(3 >= dataFeedPagedResponse.getValue().size());
        }
    }

    /**
     * Verifies the result of the list data feed method to filter results using
     * {@link ListDataFeedFilter#setCreator(String)}.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    void testListDataFeedFilterByCreator(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();

        creatDataFeedRunner(expectedDataFeed -> {
            // Act & Assert
            final DataFeed createdDataFeed = client.createDataFeed(expectedDataFeed.getName(),
                expectedDataFeed.getSource(), expectedDataFeed.getGranularity(),
                expectedDataFeed.getSchema(), expectedDataFeed.getIngestionSettings(), expectedDataFeed.getOptions());

            // Act & Assert
            client.listDataFeeds(new ListDataFeedOptions().setListDataFeedFilter(new ListDataFeedFilter()
                .setCreator(createdDataFeed.getCreator())), Context.NONE)
                .forEach(dataFeed -> assertEquals(createdDataFeed.getCreator(), dataFeed.getCreator()));

            client.deleteDataFeed(createdDataFeed.getId());

        }, POSTGRE_SQL_DB);
    }

    /**
     * Verifies the result of the list data feed method using skip and top options.
     */
    // TODO (savaity) Need concrete list results for testing skip
    // @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    // @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    // void testListDataFeedSkip(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
    //     // Arrange
    //     client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();
    //     final ArrayList<DataFeed> actualDataFeedList = new ArrayList<>();
    //     final ArrayList<DataFeed> expectedList = new ArrayList<>();
    //
    //     client.listDataFeeds().stream().iterator().forEachRemaining(expectedList::add);
    //
    //     // Act & Assert
    //     client.listDataFeeds(new ListDataFeedOptions().setSkip(3), Context.NONE)
    //         .stream().iterator().forEachRemaining(actualDataFeedList::add);
    //
    //     assertEquals(expectedList.size() - 3, actualDataFeedList.size());
    // }

    /**
     * Verifies the result of the list data feed method to filter results using
     * {@link ListDataFeedFilter#setDataFeedSourceType(DataFeedSourceType)}.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    void testListDataFeedFilterBySourceType(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();

        // Act & Assert
        client.listDataFeeds(
            new ListDataFeedOptions().setListDataFeedFilter(new ListDataFeedFilter()
                .setDataFeedSourceType(AZURE_BLOB)), Context.NONE)
            .stream().iterator().forEachRemaining(dataFeed -> assertEquals(AZURE_BLOB, dataFeed.getSourceType()));
    }

    /**
     * Verifies the result of the list data feed method to filter results using
     * {@link ListDataFeedFilter#setDataFeedStatus(DataFeedStatus)}.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    void testListDataFeedFilterByStatus(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();

        // Act & Assert
        client.listDataFeeds(
            new ListDataFeedOptions().setListDataFeedFilter(new ListDataFeedFilter()
                .setDataFeedStatus(ACTIVE)), Context.NONE)
            .stream().iterator().forEachRemaining(dataFeed -> assertEquals(ACTIVE, dataFeed.getStatus()));
    }

    /**
     * Verifies the result of the list data feed method to filter results using
     * {@link ListDataFeedFilter#setDataFeedGranularityType(DataFeedGranularityType)}.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    void testListDataFeedFilterByGranularityType(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();

        // Act & Assert
        client.listDataFeeds(
            new ListDataFeedOptions().setListDataFeedFilter(new ListDataFeedFilter()
                .setDataFeedGranularityType(DAILY)), Context.NONE)
            .stream().iterator()
            .forEachRemaining(dataFeed -> assertEquals(DAILY, dataFeed.getGranularity().getGranularityType()));
    }

    /**
     * Verifies the result of the list data feed method to filter results using
     * {@link ListDataFeedFilter#setName(String)}.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    void testListDataFeedFilterByName(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();

        creatDataFeedRunner(inputDataFeed -> {
            final DataFeed createdDataFeed = client.createDataFeed("test_filter_by_name",
                inputDataFeed.getSource(), inputDataFeed.getGranularity(),
                inputDataFeed.getSchema(), inputDataFeed.getIngestionSettings(), inputDataFeed.getOptions());

            // Act & Assert
            client.listDataFeeds(
                new ListDataFeedOptions().setListDataFeedFilter(new ListDataFeedFilter()
                    .setName("test_filter_by_name")), Context.NONE)
                .stream().iterator().forEachRemaining(dataFeed ->
                assertEquals("test_filter_by_name", createdDataFeed.getName()));
            client.deleteDataFeed(createdDataFeed.getId());
        }, SQL_SERVER_DB);

    }

    // Get Data feed

    /**
     * Verifies that an exception is thrown for null data feed Id parameter.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void getDataFeedNullId(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();

        // Act & Assert
        Exception exception = assertThrows(NullPointerException.class, () -> client.getDataFeed(null));
        assertEquals(DATAFEED_ID_REQUIRED_ERROR, exception.getMessage());
    }

    /**
     * Verifies that an exception is thrown for invalid data feed Id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void getDataFeedInvalidId(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> client.getDataFeed(INCORRECT_UUID));
        assertEquals(INCORRECT_UUID_ERROR, exception.getMessage());
    }

    /**
     * Verifies data feed info returned with response for a valid data feed Id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void getDataFeedValidId(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();

        creatDataFeedRunner(dataFeed -> {
            final DataFeed createdDataFeed = client.createDataFeed(dataFeed.getName(),
                dataFeed.getSource(), dataFeed.getGranularity(),
                dataFeed.getSchema(), dataFeed.getIngestionSettings(), dataFeed.getOptions());

            // Act & Assert
            final Response<DataFeed> dataFeedResponse =
                client.getDataFeedWithResponse(createdDataFeed.getId(), Context.NONE);
            assertEquals(dataFeedResponse.getStatusCode(), HttpResponseStatus.OK.code());
            validateDataFeedResult(createdDataFeed, dataFeedResponse.getValue(), SQL_SERVER_DB);
            client.deleteDataFeed(createdDataFeed.getId());
        }, SQL_SERVER_DB);
    }

    // Create data feed

    /**
     * Verifies valid sql data feed created for required data feed details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void createSQLServerDataFeed(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();
        creatDataFeedRunner(expectedDataFeed -> {
            // Act & Assert
            final DataFeed createdDataFeed = client.createDataFeed(expectedDataFeed.getName(),
                expectedDataFeed.getSource(), expectedDataFeed.getGranularity(),
                expectedDataFeed.getSchema(), expectedDataFeed.getIngestionSettings(), expectedDataFeed.getOptions());
            validateDataFeedResult(expectedDataFeed, createdDataFeed, SQL_SERVER_DB);
            client.deleteDataFeed(createdDataFeed.getId());
        }, SQL_SERVER_DB);
    }

    /**
     * Verifies valid sql data feed created for required data feed details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void createBlobDataFeed(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();

        creatDataFeedRunner(expectedDataFeed -> {
            // Act & Assert
            final DataFeed createdDataFeed = client.createDataFeed(expectedDataFeed.getName(),
                expectedDataFeed.getSource(), expectedDataFeed.getGranularity(),
                expectedDataFeed.getSchema(), expectedDataFeed.getIngestionSettings(), expectedDataFeed.getOptions());

            validateDataFeedResult(expectedDataFeed, createdDataFeed, AZURE_BLOB);
            client.deleteDataFeed(createdDataFeed.getId());
        }, AZURE_BLOB);
    }

    /**
     * Verifies valid cosmos data feed created for required data feed details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void createCosmosDataFeed(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();

        creatDataFeedRunner(expectedDataFeed -> {

            // Act & Assert
            final DataFeed createdDataFeed = client.createDataFeed(expectedDataFeed.getName(),
                expectedDataFeed.getSource(), expectedDataFeed.getGranularity(),
                expectedDataFeed.getSchema(), expectedDataFeed.getIngestionSettings(), expectedDataFeed.getOptions());
            validateDataFeedResult(expectedDataFeed, createdDataFeed, AZURE_COSMOS_DB);
            client.deleteDataFeed(createdDataFeed.getId());
        }, AZURE_COSMOS_DB);
    }

    /**
     * Verifies valid app insights data feed created for required data feed details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void createAppInsightsDataFeed(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();

        creatDataFeedRunner(expectedDataFeed -> {

            // Act & Assert
            final DataFeed createdDataFeed = client.createDataFeed(expectedDataFeed.getName(),
                expectedDataFeed.getSource(), expectedDataFeed.getGranularity(),
                expectedDataFeed.getSchema(), expectedDataFeed.getIngestionSettings(), expectedDataFeed.getOptions());
            validateDataFeedResult(expectedDataFeed, createdDataFeed, AZURE_APP_INSIGHTS);
            client.deleteDataFeed(createdDataFeed.getId());
        }, AZURE_APP_INSIGHTS);
    }

    /**
     * Verifies valid data explorer data feed created for required data feed details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void createExplorerDataFeed(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();

        creatDataFeedRunner(expectedDataFeed -> {
            // Act & Assert
            final DataFeed createdDataFeed = client.createDataFeed(expectedDataFeed.getName(),
                expectedDataFeed.getSource(), expectedDataFeed.getGranularity(),
                expectedDataFeed.getSchema(), expectedDataFeed.getIngestionSettings(), expectedDataFeed.getOptions());
            validateDataFeedResult(expectedDataFeed, createdDataFeed, AZURE_DATA_EXPLORER);

            client.deleteDataFeed(createdDataFeed.getId());
        }, AZURE_DATA_EXPLORER);
    }

    /**
     * Verifies valid azure table data feed created for required data feed details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void createAzureTableDataFeed(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();

        creatDataFeedRunner(expectedDataFeed -> {
            // Act & Assert
            final DataFeed createdDataFeed = client.createDataFeed(expectedDataFeed.getName(),
                expectedDataFeed.getSource(), expectedDataFeed.getGranularity(),
                expectedDataFeed.getSchema(), expectedDataFeed.getIngestionSettings(), expectedDataFeed.getOptions());
            validateDataFeedResult(expectedDataFeed, createdDataFeed, AZURE_TABLE);

            client.deleteDataFeed(createdDataFeed.getId());
        }, AZURE_TABLE);
    }

    /**
     * Verifies valid azure http data feed created for required data feed details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void createHttpRequestDataFeed(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();

        creatDataFeedRunner(expectedDataFeed -> {

            // Act & Assert
            final DataFeed createdDataFeed = client.createDataFeed(expectedDataFeed.getName(),
                expectedDataFeed.getSource(), expectedDataFeed.getGranularity(),
                expectedDataFeed.getSchema(), expectedDataFeed.getIngestionSettings(), expectedDataFeed.getOptions());
            validateDataFeedResult(expectedDataFeed, createdDataFeed, HTTP_REQUEST);

            client.deleteDataFeed(createdDataFeed.getId());
        }, HTTP_REQUEST);
    }

    /**
     * Verifies valid influx data feed created for required data feed details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void createInfluxDataFeed(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();

        creatDataFeedRunner(expectedDataFeed -> {
            // Act & Assert
            final DataFeed createdDataFeed = client.createDataFeed(expectedDataFeed.getName(),
                expectedDataFeed.getSource(), expectedDataFeed.getGranularity(),
                expectedDataFeed.getSchema(), expectedDataFeed.getIngestionSettings(), expectedDataFeed.getOptions());
            validateDataFeedResult(expectedDataFeed, createdDataFeed, INFLUX_DB);

            client.deleteDataFeed(createdDataFeed.getId());
        }, INFLUX_DB);
    }

    /**
     * Verifies valid mongo db data feed created for required data feed details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void createMongoDBDataFeed(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();

        creatDataFeedRunner(expectedDataFeed -> {
            // Act & Assert
            final DataFeed createdDataFeed = client.createDataFeed(expectedDataFeed.getName(),
                expectedDataFeed.getSource(), expectedDataFeed.getGranularity(),
                expectedDataFeed.getSchema(), expectedDataFeed.getIngestionSettings(), expectedDataFeed.getOptions());
            validateDataFeedResult(expectedDataFeed, createdDataFeed, MONGO_DB);

            client.deleteDataFeed(createdDataFeed.getId());
        }, MONGO_DB);
    }

    /**
     * Verifies valid My SQL db data feed created for required data feed details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void createMYSQLDataFeed(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();

        creatDataFeedRunner(expectedDataFeed -> {
            // Act & Assert
            final DataFeed createdDataFeed = client.createDataFeed(expectedDataFeed.getName(),
                expectedDataFeed.getSource(), expectedDataFeed.getGranularity(),
                expectedDataFeed.getSchema(), expectedDataFeed.getIngestionSettings(), expectedDataFeed.getOptions());
            validateDataFeedResult(expectedDataFeed, createdDataFeed, MYSQL_DB);

            client.deleteDataFeed(createdDataFeed.getId());
        }, MYSQL_DB);
    }

    /**
     * Verifies valid mongo db data feed created for required data feed details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void createPostgreSQLDataFeed(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();

        creatDataFeedRunner(expectedDataFeed -> {
            // Act & Assert
            final DataFeed createdDataFeed = client.createDataFeed(expectedDataFeed.getName(),
                expectedDataFeed.getSource(), expectedDataFeed.getGranularity(),
                expectedDataFeed.getSchema(), expectedDataFeed.getIngestionSettings(), expectedDataFeed.getOptions());
            validateDataFeedResult(expectedDataFeed, createdDataFeed, POSTGRE_SQL_DB);

            client.deleteDataFeed(createdDataFeed.getId());
        }, POSTGRE_SQL_DB);
    }

    /**
     * Verifies valid mongo db data feed created for required data feed details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void createDataLakeStorageDataFeed(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();

        creatDataFeedRunner(expectedDataFeed -> {
            // Act & Assert
            final DataFeed createdDataFeed = client.createDataFeed(expectedDataFeed.getName(),
                expectedDataFeed.getSource(), expectedDataFeed.getGranularity(),
                expectedDataFeed.getSchema(), expectedDataFeed.getIngestionSettings(), expectedDataFeed.getOptions());
            validateDataFeedResult(expectedDataFeed, createdDataFeed, AZURE_DATA_LAKE_STORAGE_GEN2);

            client.deleteDataFeed(createdDataFeed.getId());
        }, AZURE_DATA_LAKE_STORAGE_GEN2);
    }

    /**
     * Verifies valid mongo db data feed created for required data feed details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void createElasticsearchDataFeed(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();

        creatDataFeedRunner(expectedDataFeed -> {
            // Act & Assert
            final DataFeed createdDataFeed = client.createDataFeed(expectedDataFeed.getName(),
                expectedDataFeed.getSource(), expectedDataFeed.getGranularity(),
                expectedDataFeed.getSchema(), expectedDataFeed.getIngestionSettings(), expectedDataFeed.getOptions());
            validateDataFeedResult(expectedDataFeed, createdDataFeed, ELASTIC_SEARCH);

            client.deleteDataFeed(createdDataFeed.getId());
        }, ELASTIC_SEARCH);
    }

    /**
     * Verifies valid data feed created for required data feed details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    void createDataFeedRequiredParams(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();
        creatDataFeedRunner(expectedDataFeed -> {
            // Act & Assert
            Exception ex = assertThrows(NullPointerException.class, () -> client.createDataFeed(null,
                expectedDataFeed.getSource(), expectedDataFeed.getGranularity(),
                expectedDataFeed.getSchema(), expectedDataFeed.getIngestionSettings(), expectedDataFeed.getOptions()));
            assertEquals("'dataFeedName' cannot be null or empty.", ex.getMessage());

            ex = assertThrows(NullPointerException.class, () -> client.createDataFeed(expectedDataFeed.getName(),
                null, expectedDataFeed.getGranularity(),
                expectedDataFeed.getSchema(), expectedDataFeed.getIngestionSettings(), expectedDataFeed.getOptions()));
            assertEquals("'dataFeedSource' cannot be null or empty.", ex.getMessage());

            ex = assertThrows(NullPointerException.class, () -> client.createDataFeed(expectedDataFeed.getName(),
                expectedDataFeed.getSource(), null,
                expectedDataFeed.getSchema(), expectedDataFeed.getIngestionSettings(), expectedDataFeed.getOptions()));
            assertEquals("'dataFeedGranularity.granularityType' cannot be null or empty.", ex.getMessage());

            ex = assertThrows(NullPointerException.class, () -> client.createDataFeed(expectedDataFeed.getName(),
                expectedDataFeed.getSource(), expectedDataFeed.getGranularity(),
                null, expectedDataFeed.getIngestionSettings(), expectedDataFeed.getOptions()));
            assertEquals("'dataFeedSchema.metrics' cannot be null or empty.", ex.getMessage());

            ex = assertThrows(NullPointerException.class, () -> client.createDataFeed(expectedDataFeed.getName(),
                expectedDataFeed.getSource(), expectedDataFeed.getGranularity(),
                expectedDataFeed.getSchema(), null, expectedDataFeed.getOptions()));
            assertEquals("'dataFeedIngestionSettings.ingestionStartTime' cannot be null or empty.",
                ex.getMessage());

        }, SQL_SERVER_DB);
    }

    // Delete data feed

    /**
     * Verifies that an exception is thrown for incorrect format data feed Id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void deleteIncorrectDataFeedId(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.getDataFeed(INCORRECT_UUID));
        assertEquals(INCORRECT_UUID_ERROR, exception.getMessage());
    }

    /**
     * Verifies happy path for delete data feed.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void deleteDataFeedIdWithResponse(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();
        creatDataFeedRunner(dataFeed -> {
            final DataFeed createdDataFeed = client.createDataFeed(dataFeed.getName(),
                dataFeed.getSource(), dataFeed.getGranularity(),
                dataFeed.getSchema(), dataFeed.getIngestionSettings(), dataFeed.getOptions());

            assertEquals(HttpResponseStatus.NO_CONTENT.code(),
                client.deleteDataFeedWithResponse(createdDataFeed.getId(), Context.NONE).getStatusCode());

            // Act & Assert
            Exception exception = assertThrows(ErrorCodeException.class, () ->
                client.getDataFeedWithResponse(createdDataFeed.getId(), Context.NONE));
            final ErrorCode errorCode = ((ErrorCodeException) exception).getValue();
            assertEquals(errorCode.getCode(), "ERROR_INVALID_PARAMETER");
            assertEquals(errorCode.getMessage(), "datafeedId is invalid.");
        }, SQL_SERVER_DB);
    }

    // Update data feed

    /**
     * Verifies previously created data feed can be updated successfully.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void updateDataFeedHappyPath(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();
        creatDataFeedRunner(expectedDataFeed -> {
            final DataFeed createdDataFeed = client.createDataFeed(expectedDataFeed.getName(),
                expectedDataFeed.getSource(), expectedDataFeed.getGranularity(),
                expectedDataFeed.getSchema(), expectedDataFeed.getIngestionSettings(), expectedDataFeed.getOptions());
            // Act & Assert
            final DataFeed updatedDataFeed = client.updateDataFeed(createdDataFeed.setName("test_updated_dataFeed_name"));
            assertEquals("test_updated_dataFeed_name", updatedDataFeed.getName());
            validateDataFeedResult(expectedDataFeed, updatedDataFeed, SQL_SERVER_DB);
            client.deleteDataFeed(createdDataFeed.getId());
        }, SQL_SERVER_DB);
    }
}
