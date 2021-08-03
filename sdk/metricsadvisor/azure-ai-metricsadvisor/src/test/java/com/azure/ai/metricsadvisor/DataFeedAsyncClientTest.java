// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient;
import com.azure.ai.metricsadvisor.administration.models.DataFeed;
import com.azure.ai.metricsadvisor.administration.models.DataFeedGranularityType;
import com.azure.ai.metricsadvisor.administration.models.DataFeedSourceType;
import com.azure.ai.metricsadvisor.administration.models.DataFeedStatus;
import com.azure.ai.metricsadvisor.administration.models.ListDataFeedFilter;
import com.azure.ai.metricsadvisor.administration.models.ListDataFeedOptions;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorError;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.test.TestBase;
import com.azure.core.util.CoreUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.azure.ai.metricsadvisor.TestUtils.DATAFEED_ID_REQUIRED_ERROR;
import static com.azure.ai.metricsadvisor.TestUtils.DEFAULT_SUBSCRIBER_TIMEOUT_SECONDS;
import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.metricsadvisor.TestUtils.INCORRECT_UUID;
import static com.azure.ai.metricsadvisor.TestUtils.INCORRECT_UUID_ERROR;
import static com.azure.ai.metricsadvisor.administration.models.DataFeedGranularityType.DAILY;
import static com.azure.ai.metricsadvisor.administration.models.DataFeedSourceType.AZURE_APP_INSIGHTS;
import static com.azure.ai.metricsadvisor.administration.models.DataFeedSourceType.AZURE_BLOB;
import static com.azure.ai.metricsadvisor.administration.models.DataFeedSourceType.AZURE_COSMOS_DB;
import static com.azure.ai.metricsadvisor.administration.models.DataFeedSourceType.AZURE_DATA_EXPLORER;
import static com.azure.ai.metricsadvisor.administration.models.DataFeedSourceType.AZURE_DATA_LAKE_STORAGE_GEN2;
import static com.azure.ai.metricsadvisor.administration.models.DataFeedSourceType.AZURE_LOG_ANALYTICS;
import static com.azure.ai.metricsadvisor.administration.models.DataFeedSourceType.AZURE_TABLE;
import static com.azure.ai.metricsadvisor.administration.models.DataFeedSourceType.INFLUX_DB;
import static com.azure.ai.metricsadvisor.administration.models.DataFeedSourceType.MONGO_DB;
import static com.azure.ai.metricsadvisor.administration.models.DataFeedSourceType.MYSQL_DB;
import static com.azure.ai.metricsadvisor.administration.models.DataFeedSourceType.POSTGRE_SQL_DB;
import static com.azure.ai.metricsadvisor.administration.models.DataFeedSourceType.SQL_SERVER_DB;
import static com.azure.ai.metricsadvisor.administration.models.DataFeedStatus.ACTIVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DataFeedAsyncClientTest extends DataFeedTestBase {
    private MetricsAdvisorAdministrationAsyncClient client;

    @BeforeAll
    static void beforeAll() {
        TestBase.setupClass();
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(DEFAULT_SUBSCRIBER_TIMEOUT_SECONDS));
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
        final AtomicReference<List<String>> expectedDataFeedIdList = new AtomicReference<List<String>>();
        try {
            // Arrange
            client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();

            listDataFeedRunner(inputDataFeedList -> {
                DataFeed[] dataFeedList  = new DataFeed[2];
                dataFeedList[0] = client.createDataFeed(inputDataFeedList.get(0)).block();
                dataFeedList[1] = client.createDataFeed(inputDataFeedList.get(1)).block();
                List<DataFeed> actualDataFeedList = new ArrayList<>();
                List<DataFeed> expectedDataFeedList = new ArrayList<>();
                expectedDataFeedIdList.set(Arrays.asList(dataFeedList[0].getId(), dataFeedList[1].getId()));

                // Act
                StepVerifier.create(client.listDataFeeds(new ListDataFeedOptions()
                    .setListDataFeedFilter(new ListDataFeedFilter().setDataFeedGranularityType(DAILY)
                        .setName("java_"))))
                    .thenConsumeWhile(actualDataFeedList::add)
                    .verifyComplete();

                assertNotNull(actualDataFeedList);
                final List<DataFeed> actualList = actualDataFeedList.stream()
                    .filter(dataFeed -> expectedDataFeedIdList.get().contains(dataFeed.getId()))
                    .collect(Collectors.toList());

                // Assert
                assertNotNull(actualList);
                assertEquals(inputDataFeedList.size(), actualList.size());
                expectedDataFeedList.sort(Comparator.comparing(dataFeed -> dataFeed.getSourceType().toString()));
                actualList.sort(Comparator.comparing(dataFeed -> dataFeed.getSourceType().toString()));
                final AtomicInteger i = new AtomicInteger(-1);
                final List<DataFeedSourceType> dataFeedSourceTypes = Arrays.asList(AZURE_BLOB, SQL_SERVER_DB);
                expectedDataFeedList.forEach(expectedDataFeed -> validateDataFeedResult(expectedDataFeed,
                    actualList.get(i.incrementAndGet()), dataFeedSourceTypes.get(i.get())));
            });
        } finally {
            if (!CoreUtils.isNullOrEmpty(expectedDataFeedIdList.get())) {
                expectedDataFeedIdList.get().forEach(dataFeedId ->
                    StepVerifier.create(client.deleteDataFeed(dataFeedId)).verifyComplete());
            }
        }
    }

    /**
     * Verifies the result of the list data feed method to return only 3 results using
     * {@link ListDataFeedOptions#setMaxPageSize(Integer)}.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    void testListDataFeedTop3(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();

        // Act & Assert
        StepVerifier.create(client.listDataFeeds(new ListDataFeedOptions().setMaxPageSize(3)).byPage().take(4))
            .thenConsumeWhile(dataFeedPagedResponse -> 3 >= dataFeedPagedResponse.getValue().size())
            // page size should be less than or equal to 3
            .verifyComplete();
    }

    /**
     * Verifies the result of the list data feed method to filter results using
     * {@link ListDataFeedFilter#setCreator(String)}.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    void testListDataFeedFilterByCreator(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        final AtomicReference<String> dataFeedId = new AtomicReference<String>();
        try {
            // Arrange
            client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();
            creatDataFeedRunner(expectedDataFeed -> {
                // Act & Assert
                final DataFeed createdDataFeed = client.createDataFeed(expectedDataFeed)
                    .block();

                assertNotNull(createdDataFeed);
                dataFeedId.set(createdDataFeed.getId());


                // Act & Assert
                StepVerifier.create(client.listDataFeeds(new ListDataFeedOptions()
                        .setListDataFeedFilter(new ListDataFeedFilter()
                        .setCreator(createdDataFeed.getCreator()))).byPage().take(4))
                    .thenConsumeWhile(dataFeedPagedResponse -> {
                        dataFeedPagedResponse.getValue()
                            .forEach(dataFeed -> createdDataFeed.getCreator().equals(dataFeed.getCreator()));
                        return true;
                    })
                    .verifyComplete();
            }, POSTGRE_SQL_DB);
        } finally {
            if (!CoreUtils.isNullOrEmpty(dataFeedId.get())) {
                Mono<Void> deleteDataFeed = client.deleteDataFeed(dataFeedId.get());

                StepVerifier.create(deleteDataFeed).verifyComplete();
            }
        }
    }

    // TODO (savaity) Flakey test
    // /**
    //  * Verifies the result of the list data feed method using skip options.
    //  */
    // @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    // @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    // void testListDataFeedSkip(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
    //     // Arrange
    //     client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();
    //     final ArrayList<DataFeed> actualDataFeedList = new ArrayList<>();
    //     final ArrayList<DataFeed> expectedList = new ArrayList<>();
    //
    //     StepVerifier.create(client.listDataFeeds())
    //         .thenConsumeWhile(expectedList::add)
    //         .verifyComplete();
    //
    //     // Act & Assert
    //     StepVerifier.create(client.listDataFeeds(new ListDataFeedOptions().setSkip(3)))
    //         .thenConsumeWhile(actualDataFeedList::add)
    //         .verifyComplete();
    //
    //     assertEquals(expectedList.size(), actualDataFeedList.size() + 3);
    // }

    /**
     * Verifies the result of the list data feed method to filter results using
     * {@link ListDataFeedFilter#setDataFeedSourceType(DataFeedSourceType)}.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    void testListDataFeedFilterBySourceType(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();

        // Act & Assert
        StepVerifier.create(client.listDataFeeds(
            new ListDataFeedOptions()
                .setListDataFeedFilter(new ListDataFeedFilter()
                    .setDataFeedSourceType(AZURE_BLOB))))
            .thenConsumeWhile(dataFeed -> AZURE_BLOB.equals(dataFeed.getSourceType()))
            .verifyComplete();
    }

    /**
     * Verifies the result of the list data feed method to filter results using
     * {@link ListDataFeedFilter#setDataFeedStatus(DataFeedStatus)}.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    void testListDataFeedFilterByStatus(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();

        // Act & Assert
        StepVerifier.create(client.listDataFeeds(
            new ListDataFeedOptions()
                .setListDataFeedFilter(new ListDataFeedFilter()
                    .setDataFeedStatus(ACTIVE))).byPage().take(4))
            .thenConsumeWhile(dataFeedPagedResponse -> {
                dataFeedPagedResponse.getValue().forEach(dataFeed-> ACTIVE.equals(dataFeed.getStatus()));
            return true;
            })
            .verifyComplete();
    }

    /**
     * Verifies the result of the list data feed method to filter results using
     * {@link ListDataFeedFilter#setDataFeedGranularityType(DataFeedGranularityType)}.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    void testListDataFeedFilterByGranularityType(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();

        // Act & Assert
        StepVerifier.create(client.listDataFeeds(new ListDataFeedOptions()
            .setListDataFeedFilter(new ListDataFeedFilter().setDataFeedGranularityType(DAILY))).byPage().take(4))
            .thenConsumeWhile(dataFeedPagedResponse -> {
                dataFeedPagedResponse.getValue()
                    .forEach(dataFeed -> DAILY.equals(dataFeed.getGranularity().getGranularityType()));
                return true;
            })
            .verifyComplete();
    }

    /**
     * Verifies the result of the list data feed method to filter results using
     * {@link ListDataFeedFilter#setName(String)}.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    void testListDataFeedFilterByName(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        final AtomicReference<String> dataFeedId = new AtomicReference<>();
        try {
            client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();
            // Arrange
            String filterName = "test_filter_by_name";
            creatDataFeedRunner(inputDataFeed -> {
                final DataFeed createdDataFeed = client.createDataFeed(inputDataFeed.setName(filterName)).block();

                assertNotNull(createdDataFeed);
                dataFeedId.set(createdDataFeed.getId());
                // Act & Assert
                StepVerifier.create(client.listDataFeeds(new ListDataFeedOptions()
                    .setListDataFeedFilter(new ListDataFeedFilter()
                        .setName(filterName))))
                    .assertNext(dataFeed -> {
                        dataFeedId.set(dataFeed.getId());
                        assertEquals(filterName, dataFeed.getName());
                    })
                    .verifyComplete();
            }, SQL_SERVER_DB);
        } finally {
            if (!CoreUtils.isNullOrEmpty(dataFeedId.get())) {
                Mono<Void> deleteDataFeed = client.deleteDataFeed(dataFeedId.get());
                StepVerifier.create(deleteDataFeed)
                    .verifyComplete();
            }
        }
    }

    // Get Data feed

    /**
     * Verifies that an exception is thrown for null data feed Id parameter.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void getDataFeedNullId(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();

        // Act & Assert
        StepVerifier.create(client.getDataFeed(null))
            .expectErrorMatches(throwable -> throwable instanceof NullPointerException
                && throwable.getMessage().equals(DATAFEED_ID_REQUIRED_ERROR))
            .verify();
    }

    /**
     * Verifies that an exception is thrown for invalid data feed Id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void getDataFeedInvalidId(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();

        // Act & Assert
        StepVerifier.create(client.getDataFeed(INCORRECT_UUID))
            .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                && throwable.getMessage().equals(INCORRECT_UUID_ERROR))
            .verify();
    }

    /**
     * Verifies data feed info returned with response for a valid data feed Id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void getDataFeedValidId(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        final AtomicReference<String> dataFeedId = new AtomicReference<>();
        try {
            client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();
            // Arrange
            creatDataFeedRunner(dataFeed -> {
                final DataFeed createdDataFeed = client.createDataFeed(dataFeed).block();

                assertNotNull(createdDataFeed);
                dataFeedId.set(createdDataFeed.getId());

                // Act & Assert
                StepVerifier.create(client.getDataFeedWithResponse(createdDataFeed.getId()))
                    .assertNext(dataFeedResponse -> {
                        assertEquals(dataFeedResponse.getStatusCode(), HttpResponseStatus.OK.code());
                        validateDataFeedResult(createdDataFeed, dataFeedResponse.getValue(), SQL_SERVER_DB);
                    });
            }, SQL_SERVER_DB);
        } finally {
            if (!CoreUtils.isNullOrEmpty(dataFeedId.get())) {
                Mono<Void> deleteDataFeed = client.deleteDataFeed(dataFeedId.get());
                StepVerifier.create(deleteDataFeed)
                    .verifyComplete();
            }
        }
    }

    // Create data feed

    /**
     * Verifies valid sql data feed created for required data feed details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void createSQLServerDataFeed(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        final AtomicReference<String> dataFeedId = new AtomicReference<>();
        try {
            // Arrange
            client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();
            creatDataFeedRunner(expectedDataFeed ->

                // Act & Assert
                StepVerifier.create(client.createDataFeed(expectedDataFeed))
                    .assertNext(createdDataFeed -> {
                        dataFeedId.set(createdDataFeed.getId());
                        validateDataFeedResult(expectedDataFeed, createdDataFeed, SQL_SERVER_DB);
                    })
                    .verifyComplete(), SQL_SERVER_DB);

        } finally {
            if (!CoreUtils.isNullOrEmpty(dataFeedId.get())) {
                Mono<Void> deleteDataFeed = client.deleteDataFeed(dataFeedId.get());
                StepVerifier.create(deleteDataFeed)
                    .verifyComplete();
            }
        }
    }

    /**
     * Verifies valid sql data feed created for required data feed details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void createBlobDataFeed(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        final AtomicReference<String> dataFeedId = new AtomicReference<>();
        try {
            // Arrange
            client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();
            creatDataFeedRunner(expectedDataFeed ->
                // Act & Assert
                StepVerifier.create(client.createDataFeed(expectedDataFeed))
                    .assertNext(createdDataFeed -> {
                        assertNotNull(createdDataFeed);
                        dataFeedId.set(createdDataFeed.getId());
                        validateDataFeedResult(expectedDataFeed, createdDataFeed, AZURE_BLOB);
                    })
                    .verifyComplete(), AZURE_BLOB);
        } finally {
            if (!CoreUtils.isNullOrEmpty(dataFeedId.get())) {
                Mono<Void> deleteDataFeed = client.deleteDataFeed(dataFeedId.get());

                StepVerifier.create(deleteDataFeed)
                    .verifyComplete();
            }
        }
    }

    /**
     * Verifies valid cosmos data feed created for required data feed details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void createCosmosDataFeed(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        final AtomicReference<String> dataFeedId = new AtomicReference<>();
        try {
            // Arrange
            client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();
            creatDataFeedRunner(expectedDataFeed ->
                // Act & Assert
                StepVerifier.create(client.createDataFeed(expectedDataFeed))
                    .assertNext(createdDataFeed -> {
                        assertNotNull(createdDataFeed);
                        dataFeedId.set(createdDataFeed.getId());
                        validateDataFeedResult(expectedDataFeed, createdDataFeed, AZURE_COSMOS_DB);
                    })
                    .verifyComplete(), AZURE_COSMOS_DB);
        } finally {
            if (!CoreUtils.isNullOrEmpty(dataFeedId.get())) {
                Mono<Void> deleteDataFeed = client.deleteDataFeed(dataFeedId.get());
                StepVerifier.create(deleteDataFeed)
                    .verifyComplete();
            }
        }
    }

    /**
     * Verifies valid app insights data feed created for required data feed details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void createAppInsightsDataFeed(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        final AtomicReference<String> dataFeedId = new AtomicReference<>();
        try {
            // Arrange
            client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();
            creatDataFeedRunner(expectedDataFeed ->
                // Act & Assert
                StepVerifier.create(client.createDataFeed(expectedDataFeed))
                    .assertNext(createdDataFeed -> {
                        assertNotNull(createdDataFeed);
                        dataFeedId.set(createdDataFeed.getId());
                        validateDataFeedResult(expectedDataFeed, createdDataFeed, AZURE_APP_INSIGHTS);
                    })
                    .verifyComplete(), AZURE_APP_INSIGHTS);
        } finally {
            if (!CoreUtils.isNullOrEmpty(dataFeedId.get())) {
                Mono<Void> deleteDataFeed = client.deleteDataFeed(dataFeedId.get());
                StepVerifier.create(deleteDataFeed)
                    .verifyComplete();
            }
        }
    }

    /**
     * Verifies valid data explorer data feed created for required data feed details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void createExplorerDataFeed(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        final AtomicReference<String> dataFeedId = new AtomicReference<>();
        try {
            // Arrange
            client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();
            creatDataFeedRunner(expectedDataFeed ->
                // Act & Assert
                StepVerifier.create(client.createDataFeed(expectedDataFeed))
                    .assertNext(createdDataFeed -> {
                        assertNotNull(createdDataFeed);
                        dataFeedId.set(createdDataFeed.getId());
                        validateDataFeedResult(expectedDataFeed, createdDataFeed, AZURE_DATA_EXPLORER);
                    })
                    .verifyComplete(), AZURE_DATA_EXPLORER);
        } finally {
            if (!CoreUtils.isNullOrEmpty(dataFeedId.get())) {
                Mono<Void> deleteDataFeed = client.deleteDataFeed(dataFeedId.get());
                StepVerifier.create(deleteDataFeed)
                    .verifyComplete();
            }
        }
    }

    /**
     * Verifies valid azure table data feed created for required data feed details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void createAzureTableDataFeed(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        final AtomicReference<String> dataFeedId = new AtomicReference<>();
        try {
            // Arrange
            client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();
            creatDataFeedRunner(expectedDataFeed ->

                // Act & Assert
                StepVerifier.create(client.createDataFeed(expectedDataFeed))
                    .assertNext(createdDataFeed -> {
                        assertNotNull(createdDataFeed);
                        dataFeedId.set(createdDataFeed.getId());
                        validateDataFeedResult(expectedDataFeed, createdDataFeed, AZURE_TABLE);
                    })
                    .verifyComplete(), AZURE_TABLE);
        } finally {
            if (!CoreUtils.isNullOrEmpty(dataFeedId.get())) {
                Mono<Void> deleteDataFeed = client.deleteDataFeed(dataFeedId.get());
                StepVerifier.create(deleteDataFeed)
                    .verifyComplete();
            }
        }
    }

    /**
     * Verifies valid influx data feed created for required data feed details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void createInfluxDataFeed(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();
        final AtomicReference<String> dataFeedId = new AtomicReference<>();
        try {
            creatDataFeedRunner(expectedDataFeed ->
                // Act & Assert
                StepVerifier.create(client.createDataFeed(expectedDataFeed))
                    .assertNext(createdDataFeed -> {
                        assertNotNull(createdDataFeed);
                        dataFeedId.set(createdDataFeed.getId());
                        validateDataFeedResult(expectedDataFeed, createdDataFeed, INFLUX_DB);
                    })
                    .verifyComplete(), INFLUX_DB);
        } finally {
            if (!CoreUtils.isNullOrEmpty(dataFeedId.get())) {
                Mono<Void> deleteDataFeed = client.deleteDataFeed(dataFeedId.get());

                StepVerifier.create(deleteDataFeed)
                    .verifyComplete();
            }
        }
    }

    /**
     * Verifies valid mongo db data feed created for required data feed details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void createMongoDBDataFeed(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        final AtomicReference<String> dataFeedId = new AtomicReference<>();
        try {
            // Arrange
            client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();
            creatDataFeedRunner(expectedDataFeed ->

                // Act & Assert
                StepVerifier.create(client.createDataFeed(expectedDataFeed))
                    .assertNext(createdDataFeed -> {
                        assertNotNull(createdDataFeed);
                        dataFeedId.set(createdDataFeed.getId());
                        validateDataFeedResult(expectedDataFeed, createdDataFeed, MONGO_DB);
                    })
                    .verifyComplete(), MONGO_DB);
        } finally {
            if (!CoreUtils.isNullOrEmpty(dataFeedId.get())) {
                Mono<Void> deleteDataFeed = client.deleteDataFeed(dataFeedId.get());

                StepVerifier.create(deleteDataFeed)
                    .verifyComplete();
            }
        }
    }

    /**
     * Verifies valid My SQL db data feed created for required data feed details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void createMYSQLDataFeed(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        final AtomicReference<String> dataFeedId = new AtomicReference<>();
        try {
            // Arrange
            client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();
            creatDataFeedRunner(expectedDataFeed ->
                // Act & Assert
                StepVerifier.create(client.createDataFeed(expectedDataFeed))
                    .assertNext(createdDataFeed -> {
                        assertNotNull(createdDataFeed);
                        dataFeedId.set(createdDataFeed.getId());
                        validateDataFeedResult(expectedDataFeed, createdDataFeed, MYSQL_DB);
                    })
                    .verifyComplete(), MYSQL_DB);
        } finally {
            if (!CoreUtils.isNullOrEmpty(dataFeedId.get())) {
                Mono<Void> deleteDataFeed = client.deleteDataFeed(dataFeedId.get());

                StepVerifier.create(deleteDataFeed)
                    .verifyComplete();
            }
        }
    }

    /**
     * Verifies valid PostgreSQL data feed created for required data feed details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void createPostgreSQLDataFeed(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        final AtomicReference<String> dataFeedId = new AtomicReference<>();
        try {
            // Arrange
            client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();
            creatDataFeedRunner(expectedDataFeed ->

                // Act & Assert
                StepVerifier.create(client.createDataFeed(expectedDataFeed))
                    .assertNext(createdDataFeed -> {
                        assertNotNull(createdDataFeed);
                        dataFeedId.set(createdDataFeed.getId());
                        validateDataFeedResult(expectedDataFeed, createdDataFeed, POSTGRE_SQL_DB);
                    })
                    .verifyComplete(), POSTGRE_SQL_DB);
        } finally {
            if (!CoreUtils.isNullOrEmpty(dataFeedId.get())) {
                Mono<Void> deleteDataFeed = client.deleteDataFeed(dataFeedId.get());

                StepVerifier.create(deleteDataFeed)
                    .verifyComplete();
            }
        }
    }

    /**
     * Verifies valid data lake storage data feed created for required data feed details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void createDataLakeStorageDataFeed(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        final AtomicReference<String> dataFeedId = new AtomicReference<>();
        try {
            // Arrange
            client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();
            creatDataFeedRunner(expectedDataFeed ->

                // Act & Assert
                StepVerifier.create(client.createDataFeed(expectedDataFeed))
                    .assertNext(createdDataFeed -> {
                        assertNotNull(createdDataFeed);
                        dataFeedId.set(createdDataFeed.getId());
                        validateDataFeedResult(expectedDataFeed, createdDataFeed, AZURE_DATA_LAKE_STORAGE_GEN2);
                    })
                    .verifyComplete(), AZURE_DATA_LAKE_STORAGE_GEN2);
        } finally {
            if (!CoreUtils.isNullOrEmpty(dataFeedId.get())) {
                Mono<Void> deleteDataFeed = client.deleteDataFeed(dataFeedId.get());
                StepVerifier.create(deleteDataFeed)
                    .verifyComplete();
            }
        }
    }

    /**
     * Verifies valid log analytics data feed created for required data feed details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void createLogAnalyticsDataFeed(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        final AtomicReference<String> dataFeedId = new AtomicReference<>();
        try {
            // Arrange
            client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();
            creatDataFeedRunner(expectedDataFeed ->

                // Act & Assert
                StepVerifier.create(client.createDataFeed(expectedDataFeed))
                    .assertNext(createdDataFeed -> {
                        assertNotNull(createdDataFeed);
                        dataFeedId.set(createdDataFeed.getId());
                        validateDataFeedResult(expectedDataFeed, createdDataFeed, AZURE_LOG_ANALYTICS);
                    })
                    .verifyComplete(), AZURE_LOG_ANALYTICS);
        } finally {
            if (!CoreUtils.isNullOrEmpty(dataFeedId.get())) {
                Mono<Void> deleteDataFeed = client.deleteDataFeed(dataFeedId.get());
                StepVerifier.create(deleteDataFeed)
                    .verifyComplete();
            }
        }
    }

    // Delete data feed

    /**
     * Verifies that an exception is thrown for incorrect format data feed Id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void deleteIncorrectDataFeedId(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();

        // Act & Assert
        StepVerifier.create(client.deleteDataFeed(INCORRECT_UUID))
            .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                && throwable.getMessage().equals(INCORRECT_UUID_ERROR))
            .verify();
    }

    /**
     * Verifies happy path for delete data feed.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void deleteDataFeedIdWithResponse(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();
        creatDataFeedRunner(dataFeed -> {
            final DataFeed createdDataFeed = client.createDataFeed(dataFeed).block();

            assertNotNull(createdDataFeed);
            StepVerifier.create(client.deleteDataFeedWithResponse(createdDataFeed.getId()))
                .assertNext(response -> assertEquals(response.getStatusCode(), HttpResponseStatus.NO_CONTENT.code()))
                .verifyComplete();

            // Act & Assert
            StepVerifier.create(client.getDataFeedWithResponse(createdDataFeed.getId()))
                .verifyErrorSatisfies(throwable -> {
                    assertEquals(MetricsAdvisorResponseException.class, throwable.getClass());
                    final MetricsAdvisorError errorCode = ((MetricsAdvisorResponseException) throwable).getValue();
                    assertEquals(errorCode.getMessage(), "datafeedId is invalid.");
                });
        }, SQL_SERVER_DB);
    }

    // Update data feed

    /**
     * Verifies previously created data feed can be updated successfully.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void updateDataFeedHappyPath(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        final AtomicReference<String> dataFeedId = new AtomicReference<>();
        String updatedName = "test_updated_dataFeed_name";
        try {
            // Arrange
            client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();
            creatDataFeedRunner(expectedDataFeed -> {
                final DataFeed createdDataFeed = client.createDataFeed(expectedDataFeed)
                    .block();

                assertNotNull(createdDataFeed);
                dataFeedId.set(createdDataFeed.getId());

                // Act & Assert
                StepVerifier.create(client.updateDataFeed(createdDataFeed.setName(updatedName)))
                    .assertNext(updatedDataFeed -> {
                        assertEquals(updatedName, updatedDataFeed.getName());
                        validateDataFeedResult(expectedDataFeed, updatedDataFeed, SQL_SERVER_DB);
                    }).verifyComplete();
            }, SQL_SERVER_DB);
        } finally {
            if (!CoreUtils.isNullOrEmpty(dataFeedId.get())) {
                Mono<Void> deleteDataFeed = client.deleteDataFeed(dataFeedId.get());

                StepVerifier.create(deleteDataFeed)
                    .verifyComplete();
            }
        }
    }
}
