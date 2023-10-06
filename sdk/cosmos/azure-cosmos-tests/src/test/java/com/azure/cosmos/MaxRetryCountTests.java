// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.ImmutablePair;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchItemRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.test.faultinjection.CosmosFaultInjectionHelper;
import com.azure.cosmos.test.faultinjection.FaultInjectionCondition;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionType;
import com.azure.cosmos.test.faultinjection.FaultInjectionEndpointBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorResult;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;

@SuppressWarnings("SameParameterValue")
public class MaxRetryCountTests extends TestSuiteBase {
    private static final int PHYSICAL_PARTITION_COUNT = 3;
    private final static Logger logger = LoggerFactory.getLogger(FaultInjectionWithAvailabilityStrategyTests.class);

    private final static Integer NO_QUERY_PAGE_SUB_STATUS_CODE = 9999;

    private final static String sameDocumentIdJustCreated = null;

    private final static Boolean notSpecifiedWhetherIdempotentWriteRetriesAreEnabled = null;

    private final static CosmosRegionSwitchHint noRegionSwitchHint = null;
    private final static ThresholdBasedAvailabilityStrategy noAvailabilityStrategy = null;

    private final static BiConsumer<Integer, Integer> validateStatusCodeIsReadSessionNotAvailableError =
        (statusCode, subStatusCode) -> {
            assertThat(statusCode).isEqualTo(HttpConstants.StatusCodes.NOTFOUND);
            assertThat(subStatusCode).isEqualTo(HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE);
        };

    private final static BiConsumer<Integer, Integer> validateStatusCodeIsOperationCancelled =
        (statusCode, subStatusCode) -> {
            assertThat(statusCode).isEqualTo(HttpConstants.StatusCodes.REQUEST_TIMEOUT);
            assertThat(subStatusCode).isEqualTo(HttpConstants.SubStatusCodes.CLIENT_OPERATION_TIMEOUT);
        };


    private final static BiConsumer<Integer, Integer> validateStatusCodeIsInternalServerError =
        (statusCode, subStatusCode) -> {
            assertThat(statusCode).isEqualTo(HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR);
            assertThat(subStatusCode).isEqualTo(HttpConstants.SubStatusCodes.UNKNOWN);
        };

    private final static BiConsumer<Integer, Integer> validateStatusCodeIsServiceUnavailable =
        (statusCode, subStatusCode) -> {
            assertThat(statusCode).isEqualTo(HttpConstants.StatusCodes.SERVICE_UNAVAILABLE);
            assertThat(subStatusCode).isEqualTo(HttpConstants.SubStatusCodes.SERVER_GENERATED_503);
        };

    private final static BiConsumer<Integer, Integer> validateStatusCodeIsRetryWith =
        (statusCode, subStatusCode) -> {
            assertThat(statusCode).isEqualTo(HttpConstants.StatusCodes.RETRY_WITH);
            assertThat(subStatusCode).isEqualTo(HttpConstants.SubStatusCodes.UNKNOWN);
        };

    private final static BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> noFailureInjection =
        (container, operationType) -> {};

    private BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> injectReadSessionNotAvailableIntoAllRegions = null;

    private BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> injectTransitTimeoutIntoAllRegions = null;

    private BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> injectServiceUnavailableIntoAllRegions = null;

    private BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> injectInternalServerErrorIntoAllRegions = null;

    private BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> injectRetryWithErrorIntoAllRegions = null;

    private String FIRST_REGION_NAME = null;
    private String SECOND_REGION_NAME = null;

    private List<String> writeableRegions;

    private String testDatabaseId;
    private String testContainerId;

    @Override
    public String resolveTestNameSuffix(Object[] row) {
        if (row == null || row.length == 0) {
            return "";
        }

        return (String)row[0];
    }

    @BeforeClass(groups = { "multi-master" })
    public void beforeClass() {
        CosmosClientBuilder clientBuilder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .contentResponseOnWriteEnabled(true)
            .directMode();

        CosmosAsyncClient dummyClient = null;

        try {

            dummyClient = clientBuilder.buildAsyncClient();

            AsyncDocumentClient asyncDocumentClient = ReflectionUtils.getAsyncDocumentClient(dummyClient);
            RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) asyncDocumentClient;
            GlobalEndpointManager globalEndpointManager =
                ReflectionUtils.getGlobalEndpointManager(rxDocumentClient);

            DatabaseAccount databaseAccount = globalEndpointManager.getLatestDatabaseAccount();

            Map<String, String> writeRegionMap = this.getRegionMap(databaseAccount, true);

            this.writeableRegions = new ArrayList<>(writeRegionMap.keySet());
            assertThat(this.writeableRegions).isNotNull();
            assertThat(this.writeableRegions.size()).isGreaterThanOrEqualTo(2);

            FIRST_REGION_NAME = this.writeableRegions.get(0).toLowerCase(Locale.ROOT);
            SECOND_REGION_NAME = this.writeableRegions.get(1).toLowerCase(Locale.ROOT);

            FeedRange ALL_PARTITIONS = null;

            this.injectReadSessionNotAvailableIntoAllRegions =
                (c, operationType) -> injectReadSessionNotAvailableError(c, this.writeableRegions, operationType, ALL_PARTITIONS);

            this.injectTransitTimeoutIntoAllRegions =
                (c, operationType) -> injectTransitTimeout(c, this.writeableRegions, operationType);

            this.injectServiceUnavailableIntoAllRegions =
                (c, operationType) -> injectServiceUnavailable(c, this.writeableRegions, operationType);

            this.injectInternalServerErrorIntoAllRegions =
                (c, operationType) -> injectInternalServerError(c, this.writeableRegions, operationType);

            this.injectRetryWithErrorIntoAllRegions =
                (c, operationType) -> injectRetryWithServerError(c, this.writeableRegions, operationType);

            CosmosAsyncContainer container = this.createTestContainer(dummyClient);
            this.testDatabaseId = container.getDatabase().getId();
            this.testContainerId = container.getId();

            // Creating a container is an async task - especially with multiple regions it can
            // take some time until the container is available in the remote regions as well
            // When the container does not exist yet, you would see 401 for example for point reads etc.
            // So, adding this delay after container creation to minimize risk of hitting these errors
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        } finally {
            safeClose(dummyClient);
        }
    }
    @AfterClass(groups = { "multi-master" })
    public void afterClass() {
        CosmosClientBuilder clientBuilder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .contentResponseOnWriteEnabled(true)
            .directMode();

        CosmosAsyncClient dummyClient = null;
        if (testDatabaseId != null) {
            try {

                dummyClient = clientBuilder.buildAsyncClient();
                CosmosAsyncDatabase testDatabase = dummyClient
                    .getDatabase(this.testDatabaseId);

                safeDeleteDatabase(testDatabase);
            } finally {
                safeClose(dummyClient);
            }
        }
    }

    private List<String> getFirstRegion() {
        ArrayList<String> regions = new ArrayList<>();
        regions.add(this.writeableRegions.get(0));
        return regions;
    }

    private List<String> getAllRegionsExceptFirst() {
        ArrayList<String> regions = new ArrayList<>();
        for (int i = 1; i < this.writeableRegions.size(); i++) {
            regions.add(this.writeableRegions.get(i));
        }

        return regions;
    }

    private static int expectedMaxNumberOfRetriesForLocalRegionPreferred(
        int sessionTokenMismatchWaitTime,
        int sessionTokenMismatchInitialBackoff,
        int sessionTokenMismatchMaxBackoff
    ) {
        int totalRetryPolicyDuration = sessionTokenMismatchWaitTime;
        int currentBackoff = sessionTokenMismatchInitialBackoff;
        int waitTime = 0;
        int count = 1;
        while (waitTime <= totalRetryPolicyDuration) {
            waitTime += currentBackoff;
            currentBackoff = Math.min(currentBackoff * 2, sessionTokenMismatchMaxBackoff);
            count += 1;
        }

        logger.info(
            "expectedMaxNumberOfRetriesForLocalRegionPreferred {}, {}, {} == {}",
            sessionTokenMismatchWaitTime,
            sessionTokenMismatchInitialBackoff,
            sessionTokenMismatchMaxBackoff,
            count);

        return count + 1;
    }

    private static int expectedMaxNumberOfRetriesForRetryWith(
        int maxWaitTimeInSeconds,
        int maxBackoffTimeInMs,
        int initialBackoffTimeInMs,
        int backoffMultiplier) {
        int currentBackoff = initialBackoffTimeInMs;
        int waitTime = 0;
        int count = 1;
        while (waitTime <= maxWaitTimeInSeconds) {
            waitTime += currentBackoff;
            currentBackoff = Math.min(currentBackoff * backoffMultiplier, maxBackoffTimeInMs);
            count += 1;
        }

        logger.info(
            "expectedMaxNumberOfRetriesForRetryWith [maxWaitTimeInSeconds {}, maxBackoffTimeInMs {}, initialBackoffTimeInMs {}] == {}",
            maxWaitTimeInSeconds,
            maxBackoffTimeInMs,
            initialBackoffTimeInMs,
            count);

        return count + 1;
    }

    @DataProvider(name = "readMaxRetryCount_readSessionNotAvailable")
    public Object[][] testConfigs_readMaxRetryCount_readSessionNotAvailable() {
        final Integer MAX_LOCAL_RETRY_COUNT_DEFAULT = null;
        final Integer MAX_LOCAL_RETRY_COUNT_ONE = 1;
        final Integer MAX_LOCAL_RETRY_COUNT_ZERO = 0;
        final Integer MAX_LOCAL_RETRY_COUNT_THREE = 3;

        final Integer SESSION_TOKEN_MISMATCH_WAIT_TIME_DEFAULT = null;
        final Integer SESSION_TOKEN_MISMATCH_WAIT_TIME_FIVE_SECONDS = 5000;
        final Integer SESSION_TOKEN_MISMATCH_WAIT_TIME_ONE_SECOND = 1000;

        final Integer SESSION_TOKEN_MISMATCH_INITIAL_BACKOFF_DEFAULT = null;
        final Integer SESSION_TOKEN_MISMATCH_INITIAL_BACKOFF_FIVE_MS = 5;
        final Integer SESSION_TOKEN_MISMATCH_INITIAL_BACKOFF_ONE_SECOND = 1000;

        final Integer SESSION_TOKEN_MISMATCH_MAX_BACKOFF_DEFAULT = null;
        final Integer SESSION_TOKEN_MISMATCH_MAX_BACKOFF_FIVE_HUNDRED_MS = 500;
        final Integer SESSION_TOKEN_MISMATCH_MAX_BACKOFF_FIVE_SECONDS = 5000;

        return new Object[][] {
            // CONFIG description
            // new Object[] {
            //    TestId - name identifying the test case
            //    End-to-end timeout
            //    Region switch hint (404/1002 prefer local or remote retries)
            //    optional documentId used for reads (instead of the just created doc id) - this can be used to trigger 404/0
            //    Failure injection callback
            //    Status code/sub status code validation callback
            //    maxExpectedRetryCount
            // },

            // This test injects 404/1002 across all regions for the read operation after the initial creation
            // The region switch hint for 404/1002 is remote - meaning no local retries are happening
            // It is expected to fail with a 404/1002 - the validation will make sure that cross regional
            // execution via availability strategy was happening (but also failed)
            new Object[] {
                "404-1002_AllRegions_RemotePreferred_DefaultLocalRetryCountOf1",
                Duration.ofSeconds(60),
                CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED,
                sameDocumentIdJustCreated,
                injectReadSessionNotAvailableIntoAllRegions,
                validateStatusCodeIsReadSessionNotAvailableError,
                (Consumer<Integer>)(requestCount) -> assertThat(requestCount).isEqualTo(
                    Math.max(1, MAX_LOCAL_RETRY_COUNT_ONE) * (1 + (4 * writeableRegions.size()))
                ),
                MAX_LOCAL_RETRY_COUNT_DEFAULT, // DEFAULT is 1
                SESSION_TOKEN_MISMATCH_WAIT_TIME_DEFAULT, // DEFAULT is 5 seconds,
                SESSION_TOKEN_MISMATCH_INITIAL_BACKOFF_DEFAULT, // DEFAULT is 5 ms
                SESSION_TOKEN_MISMATCH_MAX_BACKOFF_DEFAULT, // DEFAULT is 500 ms
            },

            new Object[] {
                "404-1002_AllRegions_RemotePreferred_LocalRetryCount0",
                Duration.ofSeconds(60),
                CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED,
                sameDocumentIdJustCreated,
                injectReadSessionNotAvailableIntoAllRegions,
                validateStatusCodeIsReadSessionNotAvailableError,
                (Consumer<Integer>)(requestCount) -> assertThat(requestCount).isEqualTo(
                    Math.max(1, MAX_LOCAL_RETRY_COUNT_ZERO) * (1 + (4 * writeableRegions.size()))
                ),
                MAX_LOCAL_RETRY_COUNT_ZERO,
                SESSION_TOKEN_MISMATCH_WAIT_TIME_DEFAULT, // DEFAULT is 5 seconds
                SESSION_TOKEN_MISMATCH_INITIAL_BACKOFF_DEFAULT, // DEFAULT is 5 ms
                SESSION_TOKEN_MISMATCH_MAX_BACKOFF_DEFAULT, // DEFAULT is 500 ms
            },

            new Object[] {
                "404-1002_AllRegions_RemotePreferred_LocalRetryCount3",
                Duration.ofSeconds(60),
                CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED,
                sameDocumentIdJustCreated,
                injectReadSessionNotAvailableIntoAllRegions,
                validateStatusCodeIsReadSessionNotAvailableError,
                (Consumer<Integer>)(requestCount) -> assertThat(requestCount).isEqualTo(
                    Math.max(1, MAX_LOCAL_RETRY_COUNT_THREE) * (1 + (4 * writeableRegions.size()))
                ),
                MAX_LOCAL_RETRY_COUNT_THREE,
                SESSION_TOKEN_MISMATCH_WAIT_TIME_DEFAULT, // DEFAULT is 5 seconds
                SESSION_TOKEN_MISMATCH_INITIAL_BACKOFF_DEFAULT, // DEFAULT is 5 ms
                SESSION_TOKEN_MISMATCH_MAX_BACKOFF_DEFAULT, // DEFAULT is 500 ms
            },

            new Object[] {
                "404-1002_AllRegions_LocalPreferred_DefaultsSessionTokenMismatch",
                Duration.ofSeconds(60),
                CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
                sameDocumentIdJustCreated,
                injectReadSessionNotAvailableIntoAllRegions,
                validateStatusCodeIsReadSessionNotAvailableError,
                (Consumer<Integer>)(requestCount) -> assertThat(requestCount).isLessThanOrEqualTo(
                    expectedMaxNumberOfRetriesForLocalRegionPreferred(
                        SESSION_TOKEN_MISMATCH_WAIT_TIME_FIVE_SECONDS,
                        SESSION_TOKEN_MISMATCH_INITIAL_BACKOFF_FIVE_MS,
                        SESSION_TOKEN_MISMATCH_MAX_BACKOFF_FIVE_HUNDRED_MS
                     ) * Math.max(1, MAX_LOCAL_RETRY_COUNT_ONE) * (1 + (4 * writeableRegions.size()))
                ),
                MAX_LOCAL_RETRY_COUNT_DEFAULT, // DEFAULT is 1
                SESSION_TOKEN_MISMATCH_WAIT_TIME_DEFAULT, // DEFAULT is 5 seconds
                SESSION_TOKEN_MISMATCH_INITIAL_BACKOFF_DEFAULT, // DEFAULT is 5 ms
                SESSION_TOKEN_MISMATCH_MAX_BACKOFF_DEFAULT, // DEFAULT is 500 ms
            },

            new Object[] {
                "404-1002_AllRegions_LocalPreferred_HighBackoff",
                Duration.ofSeconds(60),
                CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
                sameDocumentIdJustCreated,
                injectReadSessionNotAvailableIntoAllRegions,
                validateStatusCodeIsReadSessionNotAvailableError,
                (Consumer<Integer>)(requestCount) -> assertThat(requestCount).isLessThanOrEqualTo(
                    expectedMaxNumberOfRetriesForLocalRegionPreferred(
                        SESSION_TOKEN_MISMATCH_WAIT_TIME_ONE_SECOND,
                        SESSION_TOKEN_MISMATCH_INITIAL_BACKOFF_ONE_SECOND,
                        SESSION_TOKEN_MISMATCH_MAX_BACKOFF_FIVE_SECONDS
                    ) * (1 + (4 * writeableRegions.size()))
                ),
                MAX_LOCAL_RETRY_COUNT_DEFAULT, // DEFAULT is 1
                SESSION_TOKEN_MISMATCH_WAIT_TIME_ONE_SECOND, // DEFAULT is 5 seconds
                SESSION_TOKEN_MISMATCH_INITIAL_BACKOFF_ONE_SECOND, // DEFAULT is 5 ms
                SESSION_TOKEN_MISMATCH_MAX_BACKOFF_FIVE_SECONDS, // DEFAULT is 500 ms
            },

            new Object[] {
                "404-1002_AllRegions_LocalPreferred_Defaults_LocalRetryCount3",
                Duration.ofSeconds(60),
                CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
                sameDocumentIdJustCreated,
                injectReadSessionNotAvailableIntoAllRegions,
                validateStatusCodeIsReadSessionNotAvailableError,
                (Consumer<Integer>)(requestCount) -> assertThat(requestCount).isLessThanOrEqualTo(
                    (expectedMaxNumberOfRetriesForLocalRegionPreferred(
                        SESSION_TOKEN_MISMATCH_WAIT_TIME_FIVE_SECONDS,
                        SESSION_TOKEN_MISMATCH_INITIAL_BACKOFF_FIVE_MS,
                        SESSION_TOKEN_MISMATCH_MAX_BACKOFF_FIVE_HUNDRED_MS
                    ) + 1) * (1 + (4 * writeableRegions.size()))
                ),
                MAX_LOCAL_RETRY_COUNT_THREE, // DEFAULT is 1
                SESSION_TOKEN_MISMATCH_WAIT_TIME_DEFAULT, // DEFAULT is 5 seconds
                SESSION_TOKEN_MISMATCH_INITIAL_BACKOFF_DEFAULT, // DEFAULT is 5 ms
                SESSION_TOKEN_MISMATCH_MAX_BACKOFF_DEFAULT, // DEFAULT is 500 ms
            },
        };
    }

    @DataProvider(name = "readMaxRetryCount_retryWith")
    public Object[][] testConfigs_readMaxRetryCount_retryWith() {
        final int DEFAULT_WAIT_TIME_IN_MS = 30 * 1000;
        final int DEFAULT_MAXIMUM_BACKOFF_TIME_IN_MS = 1000;
        final int DEFAULT_INITIAL_BACKOFF_TIME_MS = 10;
        final int DEFAULT_BACK_OFF_MULTIPLIER = 2;

        return new Object[][] {
            // CONFIG description
            // new Object[] {
            //    TestId - name identifying the test case
            //    End-to-end timeout,
            //    OperationType,
            //    FaultInjectionOperationType,
            //    optional documentId used for reads (instead of the just created doc id) - this can be used to trigger 404/0
            //    Failure injection callback
            //    Status code/sub status code validation callback
            //    maxExpectedRetryCount
            // },

            // This test injects 449/0 across all regions for the read operation after the initial creation
            // It is expected to fail with a 449/0
            // There is no cross region retry for 449/0
            new Object[] {
                "449-0_AllRegions_Read",
                Duration.ofSeconds(60),
                OperationType.Read,
                FaultInjectionOperationType.READ_ITEM,
                sameDocumentIdJustCreated,
                injectRetryWithErrorIntoAllRegions,
                validateStatusCodeIsRetryWith,
                (Consumer<Integer>)(requestCount) -> assertThat(requestCount).isLessThanOrEqualTo(
                    expectedMaxNumberOfRetriesForRetryWith(
                        DEFAULT_WAIT_TIME_IN_MS,
                        DEFAULT_MAXIMUM_BACKOFF_TIME_IN_MS,
                        DEFAULT_INITIAL_BACKOFF_TIME_MS,
                        DEFAULT_BACK_OFF_MULTIPLIER
                    )
                ),
            },
            new Object[] {
                "449-0_AllRegions_Create",
                Duration.ofSeconds(60),
                OperationType.Create,
                FaultInjectionOperationType.CREATE_ITEM,
                sameDocumentIdJustCreated,
                injectRetryWithErrorIntoAllRegions,
                validateStatusCodeIsRetryWith,
                (Consumer<Integer>)(requestCount) -> assertThat(requestCount).isLessThanOrEqualTo(
                    expectedMaxNumberOfRetriesForRetryWith(
                        DEFAULT_WAIT_TIME_IN_MS,
                        DEFAULT_MAXIMUM_BACKOFF_TIME_IN_MS,
                        DEFAULT_INITIAL_BACKOFF_TIME_MS,
                        DEFAULT_BACK_OFF_MULTIPLIER
                    )
                ),
            }
        };
    }

    @Test(groups = {"multi-master"}, dataProvider = "readMaxRetryCount_readSessionNotAvailable")
    public void readMaxRetryCount_readSessionNotAvailable(
        String testCaseId,
        Duration endToEndTimeout,
        CosmosRegionSwitchHint regionSwitchHint,
        String readItemDocumentIdOverride,
        BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> faultInjectionCallback,
        BiConsumer<Integer, Integer> validateStatusCode,
        Consumer<Integer> maxExpectedRequestCountValidation,
        Integer maxRetriesInLocalRegion,
        Integer sessionTokenRetriesWaitTime,
        Integer sessionTokenRetriesInitialBackoff,
        Integer sessionTokenRetriesMaxBackoff) {

        final int TWO_REGIONS = 2;

        if (maxRetriesInLocalRegion != null) {
            System.setProperty(
                Configs.MAX_RETRIES_IN_LOCAL_REGION_WHEN_REMOTE_REGION_PREFERRED,
                String.valueOf(maxRetriesInLocalRegion));
        } else {
            System.clearProperty(Configs.MAX_RETRIES_IN_LOCAL_REGION_WHEN_REMOTE_REGION_PREFERRED);
        }

        if (sessionTokenRetriesWaitTime != null) {
            System.setProperty(
                Configs.DEFAULT_SESSION_TOKEN_MISMATCH_WAIT_TIME_IN_MILLISECONDS_NAME,
                String.valueOf(sessionTokenRetriesWaitTime));
        } else {
            System.clearProperty(Configs.DEFAULT_SESSION_TOKEN_MISMATCH_WAIT_TIME_IN_MILLISECONDS_NAME);
        }

        if (sessionTokenRetriesInitialBackoff != null) {
            System.setProperty(
                Configs.DEFAULT_SESSION_TOKEN_MISMATCH_INITIAL_BACKOFF_TIME_IN_MILLISECONDS_NAME,
                String.valueOf(sessionTokenRetriesInitialBackoff));
        } else {
            System.clearProperty(Configs.DEFAULT_SESSION_TOKEN_MISMATCH_INITIAL_BACKOFF_TIME_IN_MILLISECONDS_NAME);
        }

        if (sessionTokenRetriesMaxBackoff != null) {
            System.setProperty(
                Configs.DEFAULT_SESSION_TOKEN_MISMATCH_MAXIMUM_BACKOFF_TIME_IN_MILLISECONDS_NAME,
                String.valueOf(sessionTokenRetriesMaxBackoff));
        } else {
            System.clearProperty(Configs.DEFAULT_SESSION_TOKEN_MISMATCH_MAXIMUM_BACKOFF_TIME_IN_MILLISECONDS_NAME);
        }

        Function<ItemOperationInvocationParameters, CosmosResponseWrapper> readItemCallback =
            getRequestCallBack(OperationType.Read, readItemDocumentIdOverride);

        BiConsumer<CosmosDiagnosticsContext, Integer> validateCtxRegions =
            (ctx, expectedNumberOfRegionsContacted) -> {
                assertThat(ctx).isNotNull();
                if (ctx != null) {
                    assertThat(ctx.getContactedRegionNames().size()).isGreaterThanOrEqualTo(expectedNumberOfRegionsContacted);
                }
            };

        Consumer<CosmosDiagnosticsContext> logCtx =
            (ctx) -> {
                assertThat(ctx).isNotNull();

                logger.info(
                    "MAX_RETRIES_IN_LOCAL_REGION_WHEN_REMOTE_REGION_PREFERRED: {}",
                    Configs.getMaxRetriesInLocalRegionWhenRemoteRegionPreferred());
                logger.info(
                    "DIAGNOSTICS CONTEXT: {} Json: {}",
                    ctx.toString(),
                    ctx.toJson());
            };

        Consumer<CosmosDiagnosticsContext> validateCtxTwoRegions =
            (ctx) -> validateCtxRegions.accept(ctx, TWO_REGIONS);

        Consumer<CosmosDiagnosticsContext> ctxValidation = ctx -> {
            assertThat(ctx.getDiagnostics()).isNotNull();
            assertThat(ctx.getDiagnostics().size()).isEqualTo(1);
            CosmosDiagnostics diagnostics = ctx.getDiagnostics().iterator().next();
            assertThat(diagnostics.getClientSideRequestStatistics()).isNotNull();
            assertThat(diagnostics.getClientSideRequestStatistics().size()).isEqualTo(1);

            ClientSideRequestStatistics clientStats = diagnostics.getClientSideRequestStatistics().iterator().next();
            assertThat(clientStats.getResponseStatisticsList()).isNotNull();
            int actualRequestCount = clientStats.getResponseStatisticsList().size();

            if (maxExpectedRequestCountValidation != null) {
                logger.info(
                    "ACTUAL REQUEST COUNT: {}",
                    actualRequestCount);

                maxExpectedRequestCountValidation.accept(actualRequestCount);
            }
        };

        try {
            execute(
                testCaseId,
                endToEndTimeout,
                noAvailabilityStrategy,
                regionSwitchHint,
                notSpecifiedWhetherIdempotentWriteRetriesAreEnabled,
                ArrayUtils.toArray(FaultInjectionOperationType.READ_ITEM),
                readItemCallback,
                faultInjectionCallback,
                validateStatusCode,
                1,
                ArrayUtils.toArray(logCtx, validateCtxTwoRegions, ctxValidation),
                null,
                null,
                0,
                0,
                false,
                ConnectionMode.DIRECT);
        } finally {
            System.clearProperty(Configs.MAX_RETRIES_IN_LOCAL_REGION_WHEN_REMOTE_REGION_PREFERRED);
        }
    }

    @Test(groups = {"multi-master"}, dataProvider = "readMaxRetryCount_retryWith")
    public void readMaxRetryCount_retryWith(
        String testCaseId,
        Duration endToEndTimeout,
        OperationType operationType,
        FaultInjectionOperationType faultInjectionOperationType,
        String readItemDocumentIdOverride,
        BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> faultInjectionCallback,
        BiConsumer<Integer, Integer> validateStatusCode,
        Consumer<Integer> maxExpectedRequestCountValidation) {

        final int ONE_REGION = 1; // there is no cross region retry for 449
        Function<ItemOperationInvocationParameters, CosmosResponseWrapper> readItemCallback =
            this.getRequestCallBack(operationType, readItemDocumentIdOverride);

        BiConsumer<CosmosDiagnosticsContext, Integer> validateCtxRegions =
            (ctx, expectedNumberOfRegionsContacted) -> {
                assertThat(ctx).isNotNull();
                if (ctx != null) {
                    assertThat(ctx.getContactedRegionNames().size()).isGreaterThanOrEqualTo(expectedNumberOfRegionsContacted);
                }
            };

        Consumer<CosmosDiagnosticsContext> logCtx =
            (ctx) -> {
                assertThat(ctx).isNotNull();
                logger.info(
                    "DIAGNOSTICS CONTEXT: {} Json: {}",
                    ctx.toString(),
                    ctx.toJson());
            };

        Consumer<CosmosDiagnosticsContext> validateCtxOneRegions =
            (ctx) -> validateCtxRegions.accept(ctx, ONE_REGION);

        Consumer<CosmosDiagnosticsContext> ctxValidation = ctx -> {
            assertThat(ctx.getDiagnostics()).isNotNull();
            assertThat(ctx.getDiagnostics().size()).isEqualTo(1);
            CosmosDiagnostics diagnostics = ctx.getDiagnostics().iterator().next();
            assertThat(diagnostics.getClientSideRequestStatistics()).isNotNull();
            assertThat(diagnostics.getClientSideRequestStatistics().size()).isEqualTo(1);

            ClientSideRequestStatistics clientStats = diagnostics.getClientSideRequestStatistics().iterator().next();
            assertThat(clientStats.getResponseStatisticsList()).isNotNull();
            int actualRequestCount = clientStats.getResponseStatisticsList().size();

            if (maxExpectedRequestCountValidation != null) {
                logger.info(
                    "ACTUAL REQUEST COUNT: {}",
                    actualRequestCount);

                maxExpectedRequestCountValidation.accept(actualRequestCount);
            }
        };

        execute(
            testCaseId,
            endToEndTimeout,
            noAvailabilityStrategy,
            CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
            notSpecifiedWhetherIdempotentWriteRetriesAreEnabled,
            ArrayUtils.toArray(faultInjectionOperationType),
            readItemCallback,
            faultInjectionCallback,
            validateStatusCode,
            1,
            ArrayUtils.toArray(logCtx, validateCtxOneRegions, ctxValidation),
            null,
            null,
            0,
            0,
            false,
            ConnectionMode.DIRECT);
    }

    private CosmosAsyncContainer createTestContainer(CosmosAsyncClient clientWithPreferredRegions) {
        String dbId = UUID.randomUUID().toString();
        return createTestContainer(clientWithPreferredRegions, dbId);
    }

    private CosmosAsyncContainer createTestContainer(CosmosAsyncClient clientWithPreferredRegions, String dbId) {
        String containerId = UUID.randomUUID().toString();

        clientWithPreferredRegions.createDatabaseIfNotExists(dbId).block();
        CosmosAsyncDatabase databaseWithSeveralWriteableRegions = clientWithPreferredRegions.getDatabase(dbId);

        // setup db and container and pass their ids accordingly
        // ensure the container has a partition key definition of /mypk

        databaseWithSeveralWriteableRegions
            .createContainerIfNotExists(
                new CosmosContainerProperties(
                    containerId,
                    new PartitionKeyDefinition().setPaths(Arrays.asList("/mypk"))),
                // for PHYSICAL_PARTITION_COUNT partitions
                ThroughputProperties.createManualThroughput(6_000 * PHYSICAL_PARTITION_COUNT))
            .block();

        return databaseWithSeveralWriteableRegions.getContainer(containerId);
    }

    private static void inject(
        String ruleName,
        CosmosAsyncContainer containerWithSeveralWriteableRegions,
        List<String> applicableRegions,
        FaultInjectionOperationType applicableOperationType,
        FaultInjectionServerErrorResult toBeInjectedServerErrorResult,
        FeedRange applicableFeedRange) {

        inject(
            ruleName,
            containerWithSeveralWriteableRegions,
            applicableRegions,
            applicableOperationType,
            toBeInjectedServerErrorResult,
            applicableFeedRange,
            FaultInjectionConnectionType.DIRECT
        );
    }

    private static void inject(
        String ruleName,
        CosmosAsyncContainer containerWithSeveralWriteableRegions,
        List<String> applicableRegions,
        FaultInjectionOperationType applicableOperationType,
        FaultInjectionServerErrorResult toBeInjectedServerErrorResult,
        FeedRange applicableFeedRange,
        FaultInjectionConnectionType connectionType) {

        FaultInjectionRuleBuilder ruleBuilder = new FaultInjectionRuleBuilder(ruleName);

        List<FaultInjectionRule> faultInjectionRules = new ArrayList<>();

        // inject 404/1002s in all regions
        // configure in accordance with preferredRegions on the client
        for (String region : applicableRegions) {
            FaultInjectionConditionBuilder conditionBuilder = new FaultInjectionConditionBuilder()
                .operationType(applicableOperationType)
                .connectionType(connectionType)
                .region(region);

            if (applicableFeedRange != null) {
                conditionBuilder = conditionBuilder.endpoints(
                    new FaultInjectionEndpointBuilder(applicableFeedRange)
                        .replicaCount(4)
                        .includePrimary(true)
                        .build()
                );
            }

            FaultInjectionCondition faultInjectionConditionForReads = conditionBuilder.build();

            // sustained fault injection
            FaultInjectionRule readSessionUnavailableRule = ruleBuilder
                .condition(faultInjectionConditionForReads)
                .result(toBeInjectedServerErrorResult)
                .duration(Duration.ofSeconds(120))
                .build();

            faultInjectionRules.add(readSessionUnavailableRule);
        }

        CosmosFaultInjectionHelper
            .configureFaultInjectionRules(containerWithSeveralWriteableRegions, faultInjectionRules)
            .block();

        logger.info(
            "FAULT INJECTION - Applied rule '{}' for regions '{}'.",
            ruleName,
            String.join(", ", applicableRegions));
    }

    private static void injectReadSessionNotAvailableError(
        CosmosAsyncContainer containerWithSeveralWriteableRegions,
        List<String> applicableRegions,
        FaultInjectionOperationType operationType,
        FeedRange applicableFeedRange) {

        String ruleName = "serverErrorRule-read-session-unavailable-" + UUID.randomUUID();
        FaultInjectionServerErrorResult badSessionTokenServerErrorResult = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
            .build();

        inject(
            ruleName,
            containerWithSeveralWriteableRegions,
            applicableRegions,
            operationType,
            badSessionTokenServerErrorResult,
            applicableFeedRange
        );
    }

    private static void injectTransitTimeout(
        CosmosAsyncContainer containerWithSeveralWriteableRegions,
        List<String> applicableRegions,
        FaultInjectionOperationType faultInjectionOperationType) {

        String ruleName = "serverErrorRule-transitTimeout-" + UUID.randomUUID();
        FaultInjectionServerErrorResult timeoutResult = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
            .delay(Duration.ofSeconds(6))
            .build();

        inject(
            ruleName,
            containerWithSeveralWriteableRegions,
            applicableRegions,
            faultInjectionOperationType,
            timeoutResult,
            null
        );
    }

    private static void injectGatewayTransitTimeout(
        CosmosAsyncContainer containerWithSeveralWriteableRegions,
        List<String> applicableRegions,
        FaultInjectionOperationType faultInjectionOperationType) {

        String ruleName = "serverErrorRule-gatewayTransitTimeout-" + UUID.randomUUID();
        FaultInjectionServerErrorResult timeoutResult = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
            .delay(Duration.ofSeconds(6))
            .build();

        inject(
            ruleName,
            containerWithSeveralWriteableRegions,
            applicableRegions,
            faultInjectionOperationType,
            timeoutResult,
            null,
            FaultInjectionConnectionType.GATEWAY
        );
    }

    private static void injectServiceUnavailable(
        CosmosAsyncContainer containerWithSeveralWriteableRegions,
        List<String> applicableRegions,
        FaultInjectionOperationType faultInjectionOperationType) {

        String ruleName = "serverErrorRule-serviceUnavailable-" + UUID.randomUUID();
        FaultInjectionServerErrorResult serviceUnavailableResult = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
            .delay(Duration.ofMillis(5))
            .build();

        inject(
            ruleName,
            containerWithSeveralWriteableRegions,
            applicableRegions,
            faultInjectionOperationType,
            serviceUnavailableResult,
            null
        );
    }

    private static void injectInternalServerError(
        CosmosAsyncContainer containerWithSeveralWriteableRegions,
        List<String> applicableRegions,
        FaultInjectionOperationType faultInjectionOperationType) {

        String ruleName = "serverErrorRule-internalServerError-" + UUID.randomUUID();
        FaultInjectionServerErrorResult serviceUnavailableResult = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.INTERNAL_SERVER_ERROR)
            .build();

        inject(
            ruleName,
            containerWithSeveralWriteableRegions,
            applicableRegions,
            faultInjectionOperationType,
            serviceUnavailableResult,
            null
        );
    }

    private static void injectRetryWithServerError(
        CosmosAsyncContainer containerWithSeveralWriteableRegions,
        List<String> applicableRegions,
        FaultInjectionOperationType faultInjectionOperationType) {

        String ruleName = "serverErrorRule-retryWithError-" + UUID.randomUUID();
        FaultInjectionServerErrorResult serviceUnavailableResult = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.RETRY_WITH)
            .build();

        inject(
            ruleName,
            containerWithSeveralWriteableRegions,
            applicableRegions,
            faultInjectionOperationType,
            serviceUnavailableResult,
            null
        );
    }

    private void execute(
        String testCaseId,
        Duration endToEndTimeout,
        ThresholdBasedAvailabilityStrategy availabilityStrategy,
        CosmosRegionSwitchHint regionSwitchHint,
        Boolean nonIdempotentWriteRetriesEnabled,
        FaultInjectionOperationType[] faultInjectionOperationTypes,
        Function<ItemOperationInvocationParameters, CosmosResponseWrapper> actionAfterInitialCreation,
        BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> faultInjectionCallback,
        BiConsumer<Integer, Integer> validateStatusCode,
        int expectedDiagnosticsContextCount,
        Consumer<CosmosDiagnosticsContext>[] firstDiagnosticsContextValidations,
        Consumer<CosmosDiagnosticsContext>[] otherDiagnosticsContextValidations,
        Consumer<CosmosResponseWrapper> validateResponse,
        int numberOfOtherDocumentsWithSameId,
        int numberOfOtherDocumentsWithSamePk,
        boolean clearContainerBeforeExecution,
        ConnectionMode connectionMode) {

        logger.info("START {}", testCaseId);

        CosmosAsyncClient clientWithPreferredRegions = buildCosmosClient(
            this.writeableRegions,
            regionSwitchHint,
            nonIdempotentWriteRetriesEnabled,
            connectionMode);

        try {

            if (clearContainerBeforeExecution) {
                CosmosAsyncContainer newTestContainer =
                    this.createTestContainer(clientWithPreferredRegions, this.testDatabaseId);
                this.testContainerId = newTestContainer.getId();
                // Creating a container is an async task - especially with multiple regions it can
                // take some time until the container is available in the remote regions as well
                // When the container does not exist yet, you would see 401 for example for point reads etc.
                // So, adding this delay after container creation to minimize risk of hitting these errors
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            String documentId = UUID.randomUUID().toString();
            Pair<String, String> idAndPkValPair = new ImmutablePair<>(documentId, documentId);

            CosmosDiagnosticsTest.TestItem createdItem = new CosmosDiagnosticsTest.TestItem(documentId, documentId);
            CosmosAsyncContainer testContainer = clientWithPreferredRegions
                .getDatabase(this.testDatabaseId)
                .getContainer(this.testContainerId);

            testContainer.createItem(createdItem).block();

            List<Pair<String, String>> otherIdAndPkValues = new ArrayList<>();
            for (int i = 0; i < numberOfOtherDocumentsWithSameId; i++) {
                String additionalPK = UUID.randomUUID().toString();
                testContainer.createItem(new CosmosDiagnosticsTest.TestItem(documentId, additionalPK)).block();
                otherIdAndPkValues.add(Pair.of(documentId, additionalPK));
            }

            for (int i = 0; i < numberOfOtherDocumentsWithSamePk; i++) {
                String sharedPK = documentId;
                String additionalDocumentId = UUID.randomUUID().toString();
                testContainer.createItem(new CosmosDiagnosticsTest.TestItem(additionalDocumentId, sharedPK)).block();
                otherIdAndPkValues.add(Pair.of(additionalDocumentId, sharedPK));
            }

            if (faultInjectionCallback != null) {
                for (FaultInjectionOperationType faultInjectionOperationType: faultInjectionOperationTypes) {
                    faultInjectionCallback.accept(testContainer, faultInjectionOperationType);
                }
            }

            CosmosEndToEndOperationLatencyPolicyConfigBuilder e2ePolicyBuilder =
                new CosmosEndToEndOperationLatencyPolicyConfigBuilder(endToEndTimeout)
                    .enable(true);
            CosmosEndToEndOperationLatencyPolicyConfig endToEndOperationLatencyPolicyConfig =
                availabilityStrategy != null
                    ? e2ePolicyBuilder.availabilityStrategy(availabilityStrategy).build()
                    : e2ePolicyBuilder.build();

            CosmosPatchItemRequestOptions itemRequestOptions = new CosmosPatchItemRequestOptions();

            if (endToEndOperationLatencyPolicyConfig != null) {
                itemRequestOptions.setCosmosEndToEndOperationLatencyPolicyConfig(endToEndOperationLatencyPolicyConfig);
            }

            try {

                ItemOperationInvocationParameters params = new ItemOperationInvocationParameters();
                params.container = testContainer;
                params.options = itemRequestOptions;
                params.idAndPkValuePair = idAndPkValPair;
                params.otherDocumentIdAndPkValuePairs = otherIdAndPkValues;
                params.nonIdempotentWriteRetriesEnabled = nonIdempotentWriteRetriesEnabled;

                CosmosResponseWrapper response = actionAfterInitialCreation.apply(params);

                CosmosDiagnosticsContext[] diagnosticsContexts = response.getDiagnosticsContexts();
                assertThat(diagnosticsContexts).isNotNull();

                logger.info(
                    "DIAGNOSTICS CONTEXT COUNT: {}",
                    diagnosticsContexts.length);
                for (CosmosDiagnosticsContext diagnosticsContext: diagnosticsContexts) {
                    logger.info(
                        "DIAGNOSTICS CONTEXT: {} {}",
                        diagnosticsContext != null ? diagnosticsContext.toString() : "n/a",
                        diagnosticsContext != null ? diagnosticsContext.toJson() : "NULL");
                }

                assertThat(diagnosticsContexts.length).isEqualTo(expectedDiagnosticsContextCount);

                if (response == null) {
                    fail("Response is null");
                } else {
                    validateStatusCode.accept(response.getStatusCode(), null);
                    if (validateResponse != null) {
                        validateResponse.accept(response);
                    }
                }

                for (Consumer<CosmosDiagnosticsContext> ctxValidation : firstDiagnosticsContextValidations) {
                    ctxValidation.accept(diagnosticsContexts[0]);
                }

                for (int i = 1; i < diagnosticsContexts.length; i++) {
                    CosmosDiagnosticsContext currentCtx = diagnosticsContexts[i];

                    for (Consumer<CosmosDiagnosticsContext> ctxValidation : otherDiagnosticsContextValidations) {
                        ctxValidation.accept(currentCtx);
                    }
                }
            } catch (Exception e) {
                if (e instanceof CosmosException) {
                    CosmosException cosmosException = Utils.as(e, CosmosException.class);
                    CosmosDiagnosticsContext diagnosticsContext = null;
                    if (cosmosException.getDiagnostics() != null) {
                        diagnosticsContext = cosmosException.getDiagnostics().getDiagnosticsContext();
                    }

                    logger.info("EXCEPTION: ", e);
                    logger.info(
                        "DIAGNOSTICS CONTEXT: {} {}",
                        diagnosticsContext != null ? diagnosticsContext.toString() : "n/a",
                        diagnosticsContext != null ? diagnosticsContext.toJson(): "NULL");

                    validateStatusCode.accept(cosmosException.getStatusCode(), cosmosException.getSubStatusCode());
                    if (firstDiagnosticsContextValidations != null) {
                        assertThat(expectedDiagnosticsContextCount).isEqualTo(1);
                        for (Consumer<CosmosDiagnosticsContext> ctxValidation : firstDiagnosticsContextValidations) {
                            ctxValidation.accept(diagnosticsContext);
                        }
                    }
                } else {
                    fail("A CosmosException instance should have been thrown.", e);
                }
            }
        } finally {
            safeClose(clientWithPreferredRegions);
        }
    }

    private static CosmosAsyncClient buildCosmosClient(
        List<String> preferredRegions,
        CosmosRegionSwitchHint regionSwitchHint,
        Boolean nonIdempotentWriteRetriesEnabled,
        ConnectionMode connectionMode) {

        CosmosClientTelemetryConfig telemetryConfig = new CosmosClientTelemetryConfig()
            .diagnosticsHandler(new CosmosDiagnosticsLogger());

        CosmosClientBuilder builder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .consistencyLevel(ConsistencyLevel.SESSION)
            .preferredRegions(preferredRegions)
            .sessionRetryOptions(new SessionRetryOptions(regionSwitchHint))
            .multipleWriteRegionsEnabled(true)
            .clientTelemetryConfig(telemetryConfig);

        if (connectionMode == ConnectionMode.GATEWAY) {
            builder.gatewayMode();
        } else {
            builder.directMode();
        }

        if (nonIdempotentWriteRetriesEnabled != null) {
            builder.setNonIdempotentWriteRetryPolicy(
                nonIdempotentWriteRetriesEnabled, true);
        }

        return builder.buildAsyncClient();
    }

    private Map<String, String> getRegionMap(DatabaseAccount databaseAccount, boolean writeOnly) {
        Iterator<DatabaseAccountLocation> locationIterator =
            writeOnly ? databaseAccount.getWritableLocations().iterator() : databaseAccount.getReadableLocations().iterator();
        Map<String, String> regionMap = new ConcurrentHashMap<>();

        while (locationIterator.hasNext()) {
            DatabaseAccountLocation accountLocation = locationIterator.next();
            regionMap.put(accountLocation.getName(), accountLocation.getEndpoint());
        }

        return regionMap;
    }

    private Function<ItemOperationInvocationParameters, CosmosResponseWrapper> getRequestCallBack(
        OperationType operationType,
        String readItemDocumentIdOverride) {

        switch (operationType) {
            case Read:
                return (params) ->
                    new CosmosResponseWrapper(params.container
                        .readItem(
                            readItemDocumentIdOverride != null
                                ? readItemDocumentIdOverride
                                : params.idAndPkValuePair.getLeft(),
                            new PartitionKey(params.idAndPkValuePair.getRight()),
                            params.options,
                            ObjectNode.class)
                        .block());
            case Create:
                return (params) ->
                    new CosmosResponseWrapper(params.container
                        .createItem(TestObject.create())
                        .block());
            default:
                throw new IllegalArgumentException("Request operation is not supported: " + operationType);
        }
    }

    private static class CosmosResponseWrapper {
        private final CosmosDiagnosticsContext[] diagnosticsContexts;
        private final Integer statusCode;
        private final Integer subStatusCode;

        private final Long totalRecordCount;

        public CosmosResponseWrapper(CosmosItemResponse<?> itemResponse) {
            if (itemResponse.getDiagnostics() != null &&
                itemResponse.getDiagnostics().getDiagnosticsContext() != null) {

                this.diagnosticsContexts = ArrayUtils.toArray(itemResponse.getDiagnostics().getDiagnosticsContext());
            } else {
                this.diagnosticsContexts = null;
            }

            this.statusCode = itemResponse.getStatusCode();
            this.subStatusCode = null;
            this.totalRecordCount = itemResponse.getItem() != null ? 1L : 0L;
        }

        public CosmosResponseWrapper(CosmosDiagnosticsContext[] ctxs, int statusCode, Integer subStatusCode, Long totalRecordCount) {
            this.diagnosticsContexts = ctxs;
            this.statusCode = statusCode;
            this.subStatusCode = subStatusCode;
            this.totalRecordCount = totalRecordCount;
        }

        public CosmosDiagnosticsContext[] getDiagnosticsContexts() {
            return this.diagnosticsContexts;
        }

        public Integer getStatusCode() {
            return this.statusCode;
        }

        public Integer getSubStatusCode() {
            return this.subStatusCode;
        }

        public Long getTotalRecordCount() {
            return this.totalRecordCount;
        }
    }

    private static class ItemOperationInvocationParameters {
        public CosmosPatchItemRequestOptions options;
        public CosmosAsyncContainer container;
        public Pair<String, String> idAndPkValuePair;

        public List<Pair<String, String>> otherDocumentIdAndPkValuePairs;
        public Boolean nonIdempotentWriteRetriesEnabled;
    }
}
