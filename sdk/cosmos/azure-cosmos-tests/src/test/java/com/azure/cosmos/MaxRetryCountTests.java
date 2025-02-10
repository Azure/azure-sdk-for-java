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
import org.apache.logging.log4j.util.TriConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    private final static String sameDocumentIdJustCreated = null;

    private final static Boolean notSpecifiedWhetherIdempotentWriteRetriesAreEnabled = null;
    private final static ThresholdBasedAvailabilityStrategy noAvailabilityStrategy = null;
    private final static Duration defaultNetworkRequestTimeoutDuration = Duration.ofSeconds(5);
    private final static Duration minNetworkRequestTimeoutDuration = Duration.ofSeconds(1);
    private final static ThrottlingRetryOptions defaultThrottlingRetryOptions = new ThrottlingRetryOptions();

    private final static BiConsumer<Integer, Integer> validateStatusCodeIsReadSessionNotAvailableError =
        (statusCode, subStatusCode) -> {
            assertThat(statusCode).isEqualTo(HttpConstants.StatusCodes.NOTFOUND);
            assertThat(subStatusCode).isEqualTo(HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE);
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

    // TODO: currently there is an issue where the subStatus code is missed in read especially when all replicas are failed with 410
    // StoreReader line #486
    private final static BiConsumer<Integer, Integer> validateStatusCodeIsServerGoneGenerated503ForRead =
        (statusCode, subStatusCode) -> {
            assertThat(statusCode).isEqualTo(HttpConstants.StatusCodes.SERVICE_UNAVAILABLE);
            assertThat(subStatusCode).isEqualTo(HttpConstants.SubStatusCodes.UNKNOWN);
        };

    private final static BiConsumer<Integer, Integer> validateStatusCodeIsServerTimeoutGenerated503ForRead =
        (statusCode, subStatusCode) -> {
            assertThat(statusCode).isEqualTo(HttpConstants.StatusCodes.SERVICE_UNAVAILABLE);
            assertThat(subStatusCode).isEqualTo(HttpConstants.SubStatusCodes.UNKNOWN);
        };

    private final static BiConsumer<Integer, Integer> validateStatusCodeIsServerGoneGenerated503ForWrite =
        (statusCode, subStatusCode) -> {
            assertThat(statusCode).isEqualTo(HttpConstants.StatusCodes.SERVICE_UNAVAILABLE);
            assertThat(subStatusCode).isEqualTo(HttpConstants.SubStatusCodes.SERVER_GENERATED_410);
        };

    private final static BiConsumer<Integer, Integer> validateStatusCodeIsServerTimeoutGenerated410ForWrite =
        (statusCode, subStatusCode) -> {
            assertThat(statusCode).isEqualTo(HttpConstants.StatusCodes.GONE);
            assertThat(subStatusCode).isEqualTo(HttpConstants.SubStatusCodes.SERVER_GENERATED_408);
        };

    private final static BiConsumer<Integer, Integer> validateStatusCodeIsServerTimeoutGenerated503ForWrite =
        (statusCode, subStatusCode) -> {
            assertThat(statusCode).isEqualTo(HttpConstants.StatusCodes.SERVICE_UNAVAILABLE);
            assertThat(subStatusCode).isEqualTo(HttpConstants.SubStatusCodes.SERVER_GENERATED_408);
        };

    private final static BiConsumer<Integer, Integer> validateStatusCodeIsTransitTimeout =
        (statusCode, subStatusCode) -> {
            assertThat(statusCode).isEqualTo(HttpConstants.StatusCodes.REQUEST_TIMEOUT);
            assertThat(subStatusCode).isEqualTo(HttpConstants.SubStatusCodes.TRANSIT_TIMEOUT);
        };

    private final static BiConsumer<Integer, Integer> validateStatusCodeIsTransitTimeoutGenerated503ForWrite =
        (statusCode, subStatusCode) -> {
            assertThat(statusCode).isEqualTo(HttpConstants.StatusCodes.SERVICE_UNAVAILABLE);
            assertThat(subStatusCode).isEqualTo(HttpConstants.SubStatusCodes.TRANSPORT_GENERATED_410);
        };

    private final static BiConsumer<Integer, Integer> validateStatusCodeIsRequestRateTooLarge =
        (statusCode, subStatusCode) -> {
            assertThat(statusCode).isEqualTo(HttpConstants.StatusCodes.TOO_MANY_REQUESTS);
            assertThat(subStatusCode).isEqualTo(HttpConstants.SubStatusCodes.USER_REQUEST_RATE_TOO_LARGE);
        };

    private final static BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> noFailureInjection =
        (container, operationType) -> {};

    private BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> injectReadSessionNotAvailableIntoAllRegions = null;

    private BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> injectServiceUnavailableIntoAllRegions = null;

    private BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> injectInternalServerErrorIntoAllRegions = null;

    private BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> injectRetryWithErrorIntoAllRegions = null;
    private BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> injectServerGoneErrorIntoAllRegions = null;
    private Function<Duration, BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType>> injectTransitTimeoutIntoAllRegions = null;
    private BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> injectServerTimeoutErrorIntoAllRegions = null;
    private BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> injectRequestRateTooLargeIntoAllRegions = null;

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

            AccountLevelLocationContext accountLevelWriteableLocationContext = getAccountLevelLocationContext(databaseAccount, true);
            validate(accountLevelWriteableLocationContext, true);

            this.writeableRegions = accountLevelWriteableLocationContext.serviceOrderedWriteableRegions;
            assertThat(this.writeableRegions).isNotNull();
            assertThat(this.writeableRegions.size()).isGreaterThanOrEqualTo(2);

            FIRST_REGION_NAME = this.writeableRegions.get(0).toLowerCase(Locale.ROOT);
            SECOND_REGION_NAME = this.writeableRegions.get(1).toLowerCase(Locale.ROOT);

            FeedRange ALL_PARTITIONS = null;

            this.injectReadSessionNotAvailableIntoAllRegions =
                (c, operationType) -> injectReadSessionNotAvailableError(c, this.writeableRegions, operationType, ALL_PARTITIONS);

            this.injectServiceUnavailableIntoAllRegions =
                (c, operationType) -> injectServiceUnavailable(c, this.writeableRegions, operationType);

            this.injectInternalServerErrorIntoAllRegions =
                (c, operationType) -> injectInternalServerError(c, this.writeableRegions, operationType);

            this.injectRetryWithErrorIntoAllRegions =
                (c, operationType) -> injectRetryWithServerError(c, this.writeableRegions, operationType);

            this.injectServerGoneErrorIntoAllRegions =
                (c, operationType) -> injectServerGoneError(c, this.writeableRegions, operationType);

            this.injectTransitTimeoutIntoAllRegions =
                (networkRequestTimeoutDuration) ->
                    (c, operationType) -> injectTransitTimeoutError(c, this.writeableRegions, operationType, networkRequestTimeoutDuration);

            this.injectServerTimeoutErrorIntoAllRegions =
                (c, operationType) -> injectServerTimeoutError(c, this.writeableRegions, operationType);

            this.injectRequestRateTooLargeIntoAllRegions =
                (c, operationType) -> injectServerRequestRateTooLargeError(c, this.writeableRegions, operationType);

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
        int maxWaitTimeInMS,
        int maxBackoffTimeInMs,
        int initialBackoffTimeInMs,
        int backoffMultiplier) {
        int currentBackoff = initialBackoffTimeInMs;
        int waitTime = 0;
        int count = 1;

        while (waitTime <= maxWaitTimeInMS) {
            waitTime += currentBackoff;
            currentBackoff = Math.min(currentBackoff * backoffMultiplier, maxBackoffTimeInMs);
            count += 1;
        }

        logger.info(
            "expectedMaxNumberOfRetriesForRetryWith [maxWaitTimeInMS {}, maxBackoffTimeInMs {}, initialBackoffTimeInMs {}] == {}",
            maxWaitTimeInMS,
            maxBackoffTimeInMs,
            initialBackoffTimeInMs,
            count);

        return count + 1;
    }

    private static int expectedMaxNumberOfRetriesForGone(
        int maxWaitTimeInMs,
        int maxBackoffTimeInMs,
        int initialBackoffTimeInMs,
        int backoffMultiplier,
        ConsistencyLevel consistencyLevel,
        OperationType operationType) {

        int requestsInFirstRetryCycle = 0;
        int requestsInFollowingRetryCycle = 0;
        if (operationType.isWriteOperation()) {
            requestsInFirstRetryCycle = 1;
            requestsInFollowingRetryCycle = 1;
        } else {
            switch (consistencyLevel) {
                case EVENTUAL:
                case CONSISTENT_PREFIX:
                    requestsInFirstRetryCycle = 1;
                    requestsInFollowingRetryCycle = 1;
                    break;
                case SESSION:
                    requestsInFirstRetryCycle = 4;
                    requestsInFollowingRetryCycle = 4;
                    break;
                case BOUNDED_STALENESS:
                case STRONG:
                    requestsInFirstRetryCycle = 4 * 2; // QuorumNotSelected, SDK will retry 2 times before bubble up the exception
                    requestsInFollowingRetryCycle = 4;
                    break;
                default:
                    throw new IllegalArgumentException("Consistency level is not supported " + consistencyLevel);
            }
        }

        int currentBackoff = 0;
        long remainingTimeInMs = maxWaitTimeInMs;
        int count = requestsInFirstRetryCycle;
        boolean firstRetryAttempt = true;
        int maxRetriesInLocalRegion = 1; // includes the first try

        while (remainingTimeInMs > 0 || firstRetryAttempt) {
            maxRetriesInLocalRegion ++;
            if (firstRetryAttempt && remainingTimeInMs < 0) {
                // SDK will always retry on the local region one more time
                remainingTimeInMs = maxBackoffTimeInMs;
            }

            firstRetryAttempt = false;
            remainingTimeInMs -= currentBackoff;
            count += requestsInFollowingRetryCycle;

            if (currentBackoff == 0) {
                currentBackoff = initialBackoffTimeInMs;
            } else {
                currentBackoff = Math.min(currentBackoff * backoffMultiplier, maxBackoffTimeInMs);
            }
        }

        logger.info(
            "expectedMaxNumberOfRetriesForGone [maxWaitTimeInMs {}, maxBackoffTimeInMs {}, initialBackoffTimeInMs {}, " +
                "consistencyLevel {}, OperationType {}] == {}, maxRetriesInLocalRegion {}",
            maxWaitTimeInMs,
            maxBackoffTimeInMs,
            initialBackoffTimeInMs,
            consistencyLevel,
            operationType,
            count,
            maxRetriesInLocalRegion);

        return count;
    }

    private static int expectedMaxNumberOfRetriesForTransientTimeout(
        int maxWaitTimeInMs,
        int maxBackoffTimeInMs,
        int initialBackoffTimeInMs,
        int backoffMultiplier,
        ConsistencyLevel consistencyLevel,
        OperationType operationType,
        Duration networkRequestTimeout,
        Boolean idempotentWriteRetriesAreEnabled) {

        int count = 0;
        int maxRetriesInLocalRegion = 1; // includes the first try

        if (operationType.isWriteOperation()
            && (idempotentWriteRetriesAreEnabled == null || !idempotentWriteRetriesAreEnabled)) {
            count = 1;
        } else {
            long latencyInFirstRetryCycleInMs = 0;
            int requestsInFirstRetryCycle = 0;

            long latencyInFollowingRetryCycleInMs = 0;
            int requestsInFollowingRetryCycle = 0;

            if (operationType.isWriteOperation()) {
                latencyInFirstRetryCycleInMs = networkRequestTimeout.toMillis();
                requestsInFirstRetryCycle = 1;
                requestsInFollowingRetryCycle = 1;
                latencyInFollowingRetryCycleInMs = networkRequestTimeout.toMillis();
            } else {
                switch (consistencyLevel) {
                    case EVENTUAL:
                    case CONSISTENT_PREFIX:
                        latencyInFirstRetryCycleInMs = networkRequestTimeout.toMillis();
                        requestsInFirstRetryCycle = 1;
                        latencyInFollowingRetryCycleInMs = networkRequestTimeout.toMillis();
                        requestsInFollowingRetryCycle = 1;
                        break;
                    case SESSION:
                        latencyInFirstRetryCycleInMs = networkRequestTimeout.toMillis() * 4;
                        requestsInFirstRetryCycle = 4;
                        latencyInFollowingRetryCycleInMs = networkRequestTimeout.toMillis() * 4;
                        requestsInFollowingRetryCycle = 4;
                        break;
                    case BOUNDED_STALENESS:
                    case STRONG:
                        latencyInFirstRetryCycleInMs = networkRequestTimeout.toMillis() * 3 * 2; // for quorum reads, SDK will send two requests in parallel
                        requestsInFirstRetryCycle = 4 * 2;// QuorumNotSelected, SDK will retry 2 times before bubble up the exception
                        latencyInFollowingRetryCycleInMs = networkRequestTimeout.toMillis() * 3; // for quorum reads, SDK will send two requests in parallel
                        requestsInFollowingRetryCycle = 4; // after the first cycle, forceRefreshHeader on the requests has been set, so instead of 2, it will only retry 1 time
                        break;
                    default:
                        throw new IllegalArgumentException("Consistency level is not supported " + consistencyLevel);
                }
            }

            int currentBackoff = 0;
            long remainingTimeInMs = maxWaitTimeInMs;
            count = requestsInFirstRetryCycle;
            remainingTimeInMs -= latencyInFirstRetryCycleInMs;
            boolean firstRetryAttempt = true;

            while (remainingTimeInMs > 0 || firstRetryAttempt) {
                maxRetriesInLocalRegion++;
                if (firstRetryAttempt && remainingTimeInMs < 0) {
                    // SDK will always retry on the local region one more time
                    remainingTimeInMs = maxBackoffTimeInMs;
                }

                firstRetryAttempt = false;
                remainingTimeInMs -= currentBackoff;
                count += requestsInFollowingRetryCycle;
                remainingTimeInMs -= latencyInFollowingRetryCycleInMs;

                if (currentBackoff == 0) {
                    currentBackoff = initialBackoffTimeInMs;
                } else {
                    currentBackoff = Math.min(currentBackoff * backoffMultiplier, maxBackoffTimeInMs);
                }
            }
        }

        logger.info(
            "expectedMaxNumberOfRetriesForTransitTimeout [maxWaitTimeInMs {}, maxBackoffTimeInMs {}, initialBackoffTimeInMs {}, " +
                "consistencyLevel {}, OperationType {},  networkRequestTimeout {}, idempotentWriteRetriesAreEnabled {}] == {}, maxRetriesInLocalRegion {}",
            maxWaitTimeInMs,
            maxBackoffTimeInMs,
            initialBackoffTimeInMs,
            consistencyLevel,
            operationType,
            networkRequestTimeout,
            idempotentWriteRetriesAreEnabled,
            count,
            maxRetriesInLocalRegion
        );

        return count;
    }

    private static int expectedMaxNumberOfRetriesForServerInternalServerError(
        ConsistencyLevel consistencyLevel,
        OperationType operationType) {

        if (operationType.isWriteOperation()) {
            return 1;
        }

        switch (consistencyLevel) {
            case EVENTUAL:
            case CONSISTENT_PREFIX:
            case SESSION:
                return 1;
            case BOUNDED_STALENESS:
            case STRONG:
                return 2; // SDK do quorum reads
            default:
                throw new IllegalArgumentException("Consitency level is not supported " + consistencyLevel);
        }
    }

    private static int expectedMaxNumberOfRetriesForServerServiceUnavailable(
        ConsistencyLevel consistencyLevel,
        OperationType operationType) {

        if (operationType.isWriteOperation()) {
            return 1;
        }

        switch (consistencyLevel) {
            case EVENTUAL:
            case CONSISTENT_PREFIX:
            case SESSION:
                return 1;
            case BOUNDED_STALENESS:
            case STRONG:
                return 2; // SDK do quorum reads
            default:
                throw new IllegalArgumentException("Consitency level is not supported " + consistencyLevel);
        }
    }

    private static int expectedMaxNumberOfRetriesForServerRequestRateTooLarge(
        ConsistencyLevel consistencyLevel,
        OperationType operationType,
        ThrottlingRetryOptions retryOptions) {

        if (operationType.isWriteOperation()) {
            return retryOptions.getMaxRetryAttemptsOnThrottledRequests() + 1;
        }

        switch (consistencyLevel) {
            case EVENTUAL:
            case CONSISTENT_PREFIX:
            case SESSION:
                return retryOptions.getMaxRetryAttemptsOnThrottledRequests() + 1;
            case BOUNDED_STALENESS:
            case STRONG:
                return 2 * (retryOptions.getMaxRetryAttemptsOnThrottledRequests() + 1); // SDK do quorum reads
            default:
                throw new IllegalArgumentException("Consitency level is not supported " + consistencyLevel);
        }
    }

    @DataProvider(name = "readMaxRetryCount_readSessionNotAvailable")
    public Object[][] testConfigs_readMaxRetryCount_readSessionNotAvailable() {
        final Integer MAX_LOCAL_RETRY_COUNT_DEFAULT = null;
        final Integer MAX_LOCAL_RETRY_COUNT_ONE = 1;
        final Integer MAX_LOCAL_RETRY_COUNT_TWO = 2;
        final Integer MAX_LOCAL_RETRY_COUNT_ZERO = 0;
        final Integer MAX_LOCAL_RETRY_COUNT_THREE = 3;
        final Integer MAX_LOCAL_RETRY_COUNT_FOUR = 4;

        final Integer SESSION_TOKEN_MISMATCH_WAIT_TIME_DEFAULT = null;
        final Integer SESSION_TOKEN_MISMATCH_WAIT_TIME_FIVE_SECONDS = 5000;
        final Integer SESSION_TOKEN_MISMATCH_WAIT_TIME_ONE_SECOND = 1000;

        final Integer SESSION_TOKEN_MISMATCH_INITIAL_BACKOFF_DEFAULT = null;
        final Integer SESSION_TOKEN_MISMATCH_INITIAL_BACKOFF_FIVE_MS = 5;
        final Integer SESSION_TOKEN_MISMATCH_INITIAL_BACKOFF_ONE_SECOND = 1000;

        final Integer SESSION_TOKEN_MISMATCH_MAX_BACKOFF_DEFAULT = null;
        final Integer SESSION_TOKEN_MISMATCH_MAX_BACKOFF_FIVE_HUNDRED_MS = 500;
        final Integer SESSION_TOKEN_MISMATCH_MAX_BACKOFF_FIVE_SECONDS = 5000;

        Object[][] testConfigs_readMaxRetryCount_readSessionNotAvailable = new Object[][] {
            // CONFIG description
            // new Object[] {
            //    TestId - name identifying the test case
            //    End-to-end timeout
            //    Region switch hint (404/1002 prefer local or remote retries)
            //    optional documentId used for reads (instead of the just created doc id) - this can be used to trigger 404/0
            //    Failure injection callback
            //    Status code/sub status code validation callback
            //    maxExpectedRetryCount
            //    Should inject preferred regions
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
                (Consumer<Integer>)(requestCount) -> assertThat(requestCount).isLessThanOrEqualTo(
                    Math.max(1, MAX_LOCAL_RETRY_COUNT_TWO) * (1 + (4 * writeableRegions.size()))
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
                (Consumer<Integer>)(requestCount) -> assertThat(requestCount).isLessThanOrEqualTo(
                    // even though maxRetryCount is being set to 0, but internally MIN_MAX_RETRIES_IN_LOCAL_REGION_WHEN_REMOTE_REGION_PREFERRED will be used
                    Math.max(1, MAX_LOCAL_RETRY_COUNT_TWO) * (1 + (4 * writeableRegions.size()))
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
                (Consumer<Integer>)(requestCount) -> assertThat(requestCount).isLessThanOrEqualTo(
                    Math.max(1, MAX_LOCAL_RETRY_COUNT_FOUR) * (1 + (4 * writeableRegions.size()))
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

        return addBooleanFlagsToAllTestConfigs(testConfigs_readMaxRetryCount_readSessionNotAvailable);
    }

    @DataProvider(name = "readMaxRetryCount_retryWith")
    public Object[][] testConfigs_readMaxRetryCount_retryWith() {
        final int DEFAULT_WAIT_TIME_IN_MS = 30 * 1000;
        final int DEFAULT_MAXIMUM_BACKOFF_TIME_IN_MS = 1000;
        final int DEFAULT_INITIAL_BACKOFF_TIME_MS = 10;
        final int DEFAULT_BACK_OFF_MULTIPLIER = 2;

        Object[][] testConfigs_readMaxRetryCount_retryWith = new Object[][] {
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
            //    Should inject preferred regions
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

        return addBooleanFlagsToAllTestConfigs(testConfigs_readMaxRetryCount_retryWith);
    }

    @DataProvider(name = "readMaxRetryCount_serverGone")
    public Object[][] testConfigs_readMaxRetryCount_serverGone() {
        final int DEFAULT_WAIT_TIME_IN_MS = 30 * 1000;
        final int DEFAULT_MAXIMUM_BACKOFF_TIME_IN_MS = 15 * 1000;
        final int DEFAULT_INITIAL_BACKOFF_TIME_IN_MS = 1000;
        final int DEFAULT_BACK_OFF_MULTIPLIER = 2;

        Object[][] testConfigs_readMaxRetryCount_serverGone = new Object[][] {
            // CONFIG description
            // new Object[] {
            //    TestId - name identifying the test case
            //    End-to-end timeout,
            //    OperationType,
            //    FaultInjectionOperationType,
            //    Flag to indicate whether IdempotentWriteRetries are enabled
            //    optional documentId used for reads (instead of the just created doc id) - this can be used to trigger 404/0
            //    Failure injection callback
            //    Status code/sub status code validation callback
            //    maxExpectedRetryCount
            // },

            // This test injects server generated 410/0 across all regions for the read operation after the initial creation
            // It is expected to fail with a 503/21005
            new Object[] {
                "410-0_AllRegions_Read",
                Duration.ofSeconds(300),
                OperationType.Read,
                FaultInjectionOperationType.READ_ITEM,
                notSpecifiedWhetherIdempotentWriteRetriesAreEnabled,
                sameDocumentIdJustCreated,
                injectServerGoneErrorIntoAllRegions,
                validateStatusCodeIsServerGoneGenerated503ForRead, // SDK will wrap into 503 exceptions after exhausting all retries
                (TriConsumer<Integer, ConsistencyLevel, OperationType>)(requestCount, consistencyLevel, operationType) ->
                    assertThat(requestCount).isLessThanOrEqualTo(
                        expectedMaxNumberOfRetriesForGone(
                            DEFAULT_WAIT_TIME_IN_MS,
                            DEFAULT_MAXIMUM_BACKOFF_TIME_IN_MS,
                            DEFAULT_INITIAL_BACKOFF_TIME_IN_MS,
                            DEFAULT_BACK_OFF_MULTIPLIER,
                            consistencyLevel,
                            operationType
                        ) * (1 + this.writeableRegions.size())
                )
            },
            new Object[] {
                "410-0_AllRegions_Create",
                Duration.ofSeconds(60),
                OperationType.Create,
                FaultInjectionOperationType.CREATE_ITEM,
                notSpecifiedWhetherIdempotentWriteRetriesAreEnabled,
                sameDocumentIdJustCreated,
                injectServerGoneErrorIntoAllRegions,
                validateStatusCodeIsServerGoneGenerated503ForWrite,
                (TriConsumer<Integer, ConsistencyLevel, OperationType>)(requestCount, consistencyLevel, operationType) ->
                    assertThat(requestCount).isLessThanOrEqualTo(
                        expectedMaxNumberOfRetriesForGone(
                            DEFAULT_WAIT_TIME_IN_MS,
                            DEFAULT_MAXIMUM_BACKOFF_TIME_IN_MS,
                            DEFAULT_INITIAL_BACKOFF_TIME_IN_MS,
                            DEFAULT_BACK_OFF_MULTIPLIER,
                            consistencyLevel,
                            operationType
                        ) * (1 + this.writeableRegions.size())
                )
            },
            new Object[] {
                "410-0_AllRegions_Create",
                Duration.ofSeconds(60),
                OperationType.Create,
                FaultInjectionOperationType.CREATE_ITEM,
                true, // IdempotentWriteRetries is enabled
                sameDocumentIdJustCreated,
                injectServerGoneErrorIntoAllRegions,
                validateStatusCodeIsServerGoneGenerated503ForWrite,
                (TriConsumer<Integer, ConsistencyLevel, OperationType>)(requestCount, consistencyLevel, operationType) ->
                    assertThat(requestCount).isLessThanOrEqualTo(
                        expectedMaxNumberOfRetriesForGone(
                            DEFAULT_WAIT_TIME_IN_MS,
                            DEFAULT_MAXIMUM_BACKOFF_TIME_IN_MS,
                            DEFAULT_INITIAL_BACKOFF_TIME_IN_MS,
                            DEFAULT_BACK_OFF_MULTIPLIER,
                            consistencyLevel,
                            operationType
                        ) * (1 + this.writeableRegions.size())
                    )
            }
        };

        return addBooleanFlagsToAllTestConfigs(testConfigs_readMaxRetryCount_serverGone);
    }

    @DataProvider(name = "readMaxRetryCount_transitTimeout")
    public Object[][] testConfigs_readMaxRetryCount_transitTimeout() {
        final int DEFAULT_WAIT_TIME_IN_MS = 30 * 1000;
        final int DEFAULT_MAXIMUM_BACKOFF_TIME_IN_MS = 15 * 1000;
        final int DEFAULT_INITIAL_BACKOFF_TIME_IN_MS = 1000;
        final int DEFAULT_BACK_OFF_MULTIPLIER = 2;

        // TODO: add max networkRequestTimeout test
        Object[][] testConfigs_readMaxRetryCount_transitTimeout = new Object[][] {
            // CONFIG description
            // new Object[] {
            //    TestId - name identifying the test case
            //    End-to-end timeout,
            //    NetworkRequestTimeout,
            //    OperationType,
            //    FaultInjectionOperationType,
            //    Flag to indicate whether IdempotentWriteRetries are enabled
            //    optional documentId used for reads (instead of the just created doc id) - this can be used to trigger 404/0
            //    Failure injection callback
            //    Status code/sub status code validation callback
            //    maxExpectedRetryCount
            //    Should inject preferred regions
            // },

            // This test injects transient timeout across all regions for the read operation after the initial creation
            // For read, it is expected to fail with 503/20001
            // For write with Idempotent retries being enabled, it is expected to fail with 503/20001
            // For write with idempotent retries being disabled, it is expected to fail with 408
            new Object[] {
                "410-20001_AllRegions_Read_MinNetworkTimeout",
                Duration.ofSeconds(300),
                minNetworkRequestTimeoutDuration,
                OperationType.Read,
                FaultInjectionOperationType.READ_ITEM,
                notSpecifiedWhetherIdempotentWriteRetriesAreEnabled,
                sameDocumentIdJustCreated,
                injectTransitTimeoutIntoAllRegions.apply(minNetworkRequestTimeoutDuration),
                validateStatusCodeIsServerGoneGenerated503ForRead, // SDK will wrap into 503 exceptions after exhausting all retries
                (TriConsumer<Integer, ConsistencyLevel, OperationType>)(requestCount, consistencyLevel, operationType) ->
                    assertThat(requestCount).isLessThanOrEqualTo(
                        expectedMaxNumberOfRetriesForTransientTimeout(
                            DEFAULT_WAIT_TIME_IN_MS,
                            DEFAULT_MAXIMUM_BACKOFF_TIME_IN_MS,
                            DEFAULT_INITIAL_BACKOFF_TIME_IN_MS,
                            DEFAULT_BACK_OFF_MULTIPLIER,
                            consistencyLevel,
                            operationType,
                            minNetworkRequestTimeoutDuration,
                            notSpecifiedWhetherIdempotentWriteRetriesAreEnabled
                        ) * (1 + this.writeableRegions.size())
                    )
            },
            new Object[] {
                "410-20001_AllRegions_Read_DefaultNetworkTimeout",
                Duration.ofSeconds(300),
                defaultNetworkRequestTimeoutDuration,
                OperationType.Read,
                FaultInjectionOperationType.READ_ITEM,
                notSpecifiedWhetherIdempotentWriteRetriesAreEnabled,
                sameDocumentIdJustCreated,
                injectTransitTimeoutIntoAllRegions.apply(defaultNetworkRequestTimeoutDuration),
                validateStatusCodeIsServerGoneGenerated503ForRead, // SDK will wrap into 503 exceptions after exhausting all retries
                (TriConsumer<Integer, ConsistencyLevel, OperationType>)(requestCount, consistencyLevel, operationType) ->
                    assertThat(requestCount).isLessThanOrEqualTo(
                        expectedMaxNumberOfRetriesForTransientTimeout(
                            DEFAULT_WAIT_TIME_IN_MS,
                            DEFAULT_MAXIMUM_BACKOFF_TIME_IN_MS,
                            DEFAULT_INITIAL_BACKOFF_TIME_IN_MS,
                            DEFAULT_BACK_OFF_MULTIPLIER,
                            consistencyLevel,
                            operationType,
                            defaultNetworkRequestTimeoutDuration,
                            notSpecifiedWhetherIdempotentWriteRetriesAreEnabled
                        ) * (1 + this.writeableRegions.size())
                    )
            },
            new Object[] {
                "410-20001_AllRegions_Create",
                Duration.ofSeconds(60),
                minNetworkRequestTimeoutDuration,
                OperationType.Create,
                FaultInjectionOperationType.CREATE_ITEM,
                notSpecifiedWhetherIdempotentWriteRetriesAreEnabled,
                sameDocumentIdJustCreated,
                injectTransitTimeoutIntoAllRegions.apply(minNetworkRequestTimeoutDuration),
                validateStatusCodeIsTransitTimeout, // when idempotent write is disabled, SDK will not retry for write operation, 408 will be bubbled up
                (TriConsumer<Integer, ConsistencyLevel, OperationType>)(requestCount, consistencyLevel, operationType) ->
                    assertThat(requestCount).isLessThanOrEqualTo(
                        expectedMaxNumberOfRetriesForTransientTimeout(
                            DEFAULT_WAIT_TIME_IN_MS,
                            DEFAULT_MAXIMUM_BACKOFF_TIME_IN_MS,
                            DEFAULT_INITIAL_BACKOFF_TIME_IN_MS,
                            DEFAULT_BACK_OFF_MULTIPLIER,
                            consistencyLevel,
                            operationType,
                            minNetworkRequestTimeoutDuration,
                            notSpecifiedWhetherIdempotentWriteRetriesAreEnabled
                        )
                    )
            },
            new Object[] {
                "410-20001_AllRegions_Create",
                Duration.ofSeconds(60),
                minNetworkRequestTimeoutDuration,
                OperationType.Create,
                FaultInjectionOperationType.CREATE_ITEM,
                true, // IdempotentWriteRetries is enabled
                sameDocumentIdJustCreated,
                injectTransitTimeoutIntoAllRegions.apply(minNetworkRequestTimeoutDuration),
                validateStatusCodeIsTransitTimeoutGenerated503ForWrite, // when idempotent write is enabled, write will retry in reach region and bubble as 503/20001
                (TriConsumer<Integer, ConsistencyLevel, OperationType>)(requestCount, consistencyLevel, operationType) ->
                    assertThat(requestCount).isLessThanOrEqualTo(
                        expectedMaxNumberOfRetriesForTransientTimeout(
                            DEFAULT_WAIT_TIME_IN_MS,
                            DEFAULT_MAXIMUM_BACKOFF_TIME_IN_MS,
                            DEFAULT_INITIAL_BACKOFF_TIME_IN_MS,
                            DEFAULT_BACK_OFF_MULTIPLIER,
                            consistencyLevel,
                            operationType,
                            minNetworkRequestTimeoutDuration,
                            true
                        ) * (1 + this.writeableRegions.size())
                    )
            }
        };

        return addBooleanFlagsToAllTestConfigs(testConfigs_readMaxRetryCount_transitTimeout);
    }

    @DataProvider(name = "readMaxRetryCount_serverTimeout")
    public Object[][] testConfigs_readMaxRetryCount_serverTimeout() {
        final int DEFAULT_WAIT_TIME_IN_MS = 30 * 1000;
        final int DEFAULT_MAXIMUM_BACKOFF_TIME_IN_MS = 15 * 1000;
        final int DEFAULT_INITIAL_BACKOFF_TIME_IN_MS = 1000;
        final int DEFAULT_BACK_OFF_MULTIPLIER = 2;

        Object[][] testConfigs_readMaxRetryCount_serverTimeout = new Object[][] {
            // CONFIG description
            // new Object[] {
            //    TestId - name identifying the test case
            //    End-to-end timeout,
            //    OperationType,
            //    FaultInjectionOperationType,
            //    Flag to indicate whether IdempotentWriteRetries are enabled
            //    optional documentId used for reads (instead of the just created doc id) - this can be used to trigger 404/0
            //    Failure injection callback
            //    Status code/sub status code validation callback
            //    maxExpectedRetryCount
            // },

            // This test injects transient timeout across all regions for the read operation after the initial creation
            // For read, it is expected to fail with 503/20001
            // For write with Idempotent retries being enabled, it is expected to fail with 503/20001
            // For write with idempotent retries being disabled, it is expected to fail with 408
            new Object[] {
                "408-0_AllRegions_Read",
                Duration.ofSeconds(300),
                OperationType.Read,
                FaultInjectionOperationType.READ_ITEM,
                notSpecifiedWhetherIdempotentWriteRetriesAreEnabled,
                sameDocumentIdJustCreated,
                injectServerTimeoutErrorIntoAllRegions,
                validateStatusCodeIsServerTimeoutGenerated503ForRead, // SDK will translate 408 into 410, and then follow 410 retry rules
                (TriConsumer<Integer, ConsistencyLevel, OperationType>)(requestCount, consistencyLevel, operationType) ->
                    assertThat(requestCount).isLessThanOrEqualTo(
                        expectedMaxNumberOfRetriesForTransientTimeout(
                            DEFAULT_WAIT_TIME_IN_MS,
                            DEFAULT_MAXIMUM_BACKOFF_TIME_IN_MS,
                            DEFAULT_INITIAL_BACKOFF_TIME_IN_MS,
                            DEFAULT_BACK_OFF_MULTIPLIER,
                            consistencyLevel,
                            operationType,
                            Duration.ofSeconds(5),
                            notSpecifiedWhetherIdempotentWriteRetriesAreEnabled
                        ) * (1 + this.writeableRegions.size())
                    )
            },
            new Object[] {
                "408-0_AllRegions_Create",
                Duration.ofSeconds(60),
                OperationType.Create,
                FaultInjectionOperationType.CREATE_ITEM,
                notSpecifiedWhetherIdempotentWriteRetriesAreEnabled,
                sameDocumentIdJustCreated,
                injectServerTimeoutErrorIntoAllRegions,
                validateStatusCodeIsServerTimeoutGenerated410ForWrite, // when idempotent write is disabled, SDK will not retry for write operation, 410 will be bubbled up
                (TriConsumer<Integer, ConsistencyLevel, OperationType>)(requestCount, consistencyLevel, operationType) ->
                    assertThat(requestCount).isLessThanOrEqualTo(
                        expectedMaxNumberOfRetriesForTransientTimeout(
                            DEFAULT_WAIT_TIME_IN_MS,
                            DEFAULT_MAXIMUM_BACKOFF_TIME_IN_MS,
                            DEFAULT_INITIAL_BACKOFF_TIME_IN_MS,
                            DEFAULT_BACK_OFF_MULTIPLIER,
                            consistencyLevel,
                            operationType,
                            Duration.ofSeconds(5),
                            notSpecifiedWhetherIdempotentWriteRetriesAreEnabled
                        )
                    )
            },
            new Object[] {
                "408-0_AllRegions_Create",
                Duration.ofSeconds(60),
                OperationType.Create,
                FaultInjectionOperationType.CREATE_ITEM,
                true, // IdempotentWriteRetries is enabled
                sameDocumentIdJustCreated,
                injectServerTimeoutErrorIntoAllRegions,
                validateStatusCodeIsServerTimeoutGenerated503ForWrite, // when idempotent write is enabled, write will retry in reach region and bubble as 503/21010
                (TriConsumer<Integer, ConsistencyLevel, OperationType>)(requestCount, consistencyLevel, operationType) ->
                    assertThat(requestCount).isLessThanOrEqualTo(
                        expectedMaxNumberOfRetriesForTransientTimeout(
                            DEFAULT_WAIT_TIME_IN_MS,
                            DEFAULT_MAXIMUM_BACKOFF_TIME_IN_MS,
                            DEFAULT_INITIAL_BACKOFF_TIME_IN_MS,
                            DEFAULT_BACK_OFF_MULTIPLIER,
                            consistencyLevel,
                            operationType,
                            Duration.ofSeconds(5),
                            true
                        ) * (1 + this.writeableRegions.size())
                    )
            }
        };

        return addBooleanFlagsToAllTestConfigs(testConfigs_readMaxRetryCount_serverTimeout);
    }

    @DataProvider(name = "readMaxRetryCount_serverServiceUnavailable")
    public Object[][] testConfigs_readMaxRetryCount_serverServiceUnavailable() {
         Object[][] testConfigs_readMaxRetryCount_serverServiceUnavailable = new Object[][] {
            // CONFIG description
            // new Object[] {
            //    TestId - name identifying the test case
            //    End-to-end timeout,
            //    OperationType,
            //    FaultInjectionOperationType,
            //    Flag to indicate whether IdempotentWriteRetries are enabled
            //    optional documentId used for reads (instead of the just created doc id) - this can be used to trigger 404/0
            //    Failure injection callback
            //    Status code/sub status code validation callback
            //    maxExpectedRetryCount
            //    Should inject preferred regions
            // },

            // This test injects server generated 503/0 across all regions for the read operation after the initial creation
            // It is expected to fail with a 503/0
            new Object[] {
                "503-0_AllRegions_Read",
                Duration.ofSeconds(60),
                OperationType.Read,
                FaultInjectionOperationType.READ_ITEM,
                notSpecifiedWhetherIdempotentWriteRetriesAreEnabled,
                sameDocumentIdJustCreated,
                injectServiceUnavailableIntoAllRegions,
                validateStatusCodeIsServiceUnavailable,
                (TriConsumer<Integer, ConsistencyLevel, OperationType>)(requestCount, consistencyLevel, operationType) ->
                    assertThat(requestCount).isLessThanOrEqualTo(
                        expectedMaxNumberOfRetriesForServerServiceUnavailable(
                            consistencyLevel,
                            operationType) * (1 + this.writeableRegions.size()))
            },
            new Object[] {
                "503-0_AllRegions_Create",
                Duration.ofSeconds(60),
                OperationType.Create,
                FaultInjectionOperationType.CREATE_ITEM,
                notSpecifiedWhetherIdempotentWriteRetriesAreEnabled,
                sameDocumentIdJustCreated,
                injectServiceUnavailableIntoAllRegions,
                validateStatusCodeIsServiceUnavailable,
                (TriConsumer<Integer, ConsistencyLevel, OperationType>)(requestCount, consistencyLevel, operationType) ->
                    assertThat(requestCount).isLessThanOrEqualTo(
                        expectedMaxNumberOfRetriesForServerServiceUnavailable(
                            consistencyLevel,
                            operationType) * (1 + this.writeableRegions.size()))
            },
            new Object[] {
                "503-0_AllRegions_Create",
                Duration.ofSeconds(60),
                OperationType.Create,
                FaultInjectionOperationType.CREATE_ITEM,
                true, // IdempotentWriteRetries is enabled
                sameDocumentIdJustCreated,
                injectServiceUnavailableIntoAllRegions,
                validateStatusCodeIsServiceUnavailable,
                (TriConsumer<Integer, ConsistencyLevel, OperationType>)(requestCount, consistencyLevel, operationType) ->
                    assertThat(requestCount).isLessThanOrEqualTo(
                        expectedMaxNumberOfRetriesForServerServiceUnavailable(
                            consistencyLevel,
                            operationType) * (1 + this.writeableRegions.size()))
            }
        };

        return addBooleanFlagsToAllTestConfigs(testConfigs_readMaxRetryCount_serverServiceUnavailable);
    }

    @DataProvider(name = "readMaxRetryCount_serverRequestRateTooLarge")
    public Object[][] testConfigs_readMaxRetryCount_serverRequestRateTooLarge() {
        final ThrottlingRetryOptions customizedThrottlingRetryOptions =
            new ThrottlingRetryOptions().setMaxRetryAttemptsOnThrottledRequests(2).setMaxRetryWaitTime(Duration.ofSeconds(2));

        Object[][] testConfigs_readMaxRetryCount_serverRequestRateTooLarge = new Object[][] {
            // CONFIG description
            // new Object[] {
            //    TestId - name identifying the test case
            //    End-to-end timeout,
            //    OperationType,
            //    FaultInjectionOperationType,
            //    Flag to indicate whether IdempotentWriteRetries are enabled
            //    optional documentId used for reads (instead of the just created doc id) - this can be used to trigger 404/0
            //    Failure injection callback
            //    Status code/sub status code validation callback
            //    maxExpectedRetryCount
            // },

            // This test injects server generated 503/0 across all regions for the read operation after the initial creation
            // It is expected to fail with a 503/0
            new Object[] {
                "429-3200_AllRegions_Read",
                Duration.ofSeconds(60),
                defaultThrottlingRetryOptions,
                OperationType.Read,
                FaultInjectionOperationType.READ_ITEM,
                notSpecifiedWhetherIdempotentWriteRetriesAreEnabled,
                sameDocumentIdJustCreated,
                injectRequestRateTooLargeIntoAllRegions,
                validateStatusCodeIsRequestRateTooLarge,
                (TriConsumer<Integer, ConsistencyLevel, OperationType>)(requestCount, consistencyLevel, operationType) ->
                    assertThat(requestCount).isLessThanOrEqualTo(
                        expectedMaxNumberOfRetriesForServerRequestRateTooLarge(
                            consistencyLevel,
                            operationType,
                            defaultThrottlingRetryOptions))
            },
            new Object[] {
                "429-3200_AllRegions_Read",
                Duration.ofSeconds(60),
                customizedThrottlingRetryOptions,
                OperationType.Read,
                FaultInjectionOperationType.READ_ITEM,
                notSpecifiedWhetherIdempotentWriteRetriesAreEnabled,
                sameDocumentIdJustCreated,
                injectRequestRateTooLargeIntoAllRegions,
                validateStatusCodeIsRequestRateTooLarge,
                (TriConsumer<Integer, ConsistencyLevel, OperationType>)(requestCount, consistencyLevel, operationType) ->
                    assertThat(requestCount).isLessThanOrEqualTo(
                        expectedMaxNumberOfRetriesForServerRequestRateTooLarge(
                            consistencyLevel,
                            operationType,
                            customizedThrottlingRetryOptions))
            },
            new Object[] {
                "429-3200_AllRegions_Create",
                Duration.ofSeconds(60),
                defaultThrottlingRetryOptions,
                OperationType.Create,
                FaultInjectionOperationType.CREATE_ITEM,
                notSpecifiedWhetherIdempotentWriteRetriesAreEnabled,
                sameDocumentIdJustCreated,
                injectRequestRateTooLargeIntoAllRegions,
                validateStatusCodeIsRequestRateTooLarge,
                (TriConsumer<Integer, ConsistencyLevel, OperationType>)(requestCount, consistencyLevel, operationType) ->
                    assertThat(requestCount).isLessThanOrEqualTo(
                        expectedMaxNumberOfRetriesForServerRequestRateTooLarge(
                            consistencyLevel,
                            operationType,
                            defaultThrottlingRetryOptions))
            },
            new Object[] {
                "429-3200_AllRegions_Create",
                Duration.ofSeconds(60),
                defaultThrottlingRetryOptions,
                OperationType.Create,
                FaultInjectionOperationType.CREATE_ITEM,
                true, // IdempotentWriteRetries is enabled
                sameDocumentIdJustCreated,
                injectRequestRateTooLargeIntoAllRegions,
                validateStatusCodeIsRequestRateTooLarge,
                (TriConsumer<Integer, ConsistencyLevel, OperationType>)(requestCount, consistencyLevel, operationType) ->
                    assertThat(requestCount).isLessThanOrEqualTo(
                        expectedMaxNumberOfRetriesForServerRequestRateTooLarge(
                            consistencyLevel,
                            operationType,
                            defaultThrottlingRetryOptions))
            }
        };

        return addBooleanFlagsToAllTestConfigs(testConfigs_readMaxRetryCount_serverRequestRateTooLarge);
    }

    @DataProvider(name = "readMaxRetryCount_serverInternalServerError")
    public Object[][] testConfigs_readMaxRetryCount_serverInternalServerError() {
        Object[][] testConfigs_readMaxRetryCount_serverInternalServerError = new Object[][] {
            // CONFIG description
            // new Object[] {
            //    TestId - name identifying the test case
            //    End-to-end timeout,
            //    OperationType,
            //    FaultInjectionOperationType,
            //    Flag to indicate whether IdempotentWriteRetries are enabled
            //    optional documentId used for reads (instead of the just created doc id) - this can be used to trigger 404/0
            //    Failure injection callback
            //    Status code/sub status code validation callback
            //    maxExpectedRetryCount
            //    Should preferred regions be injected into client
            // },

            // This test injects server generated 500/0 across all regions for the read operation after the initial creation
            // It is expected to fail with a 500/0
            new Object[] {
                "500-0_AllRegions_Read",
                Duration.ofSeconds(60),
                OperationType.Read,
                FaultInjectionOperationType.READ_ITEM,
                notSpecifiedWhetherIdempotentWriteRetriesAreEnabled,
                sameDocumentIdJustCreated,
                injectInternalServerErrorIntoAllRegions,
                validateStatusCodeIsInternalServerError,
                (TriConsumer<Integer, ConsistencyLevel, OperationType>)(requestCount, consistencyLevel, operationType) ->
                    assertThat(requestCount).isLessThanOrEqualTo(
                        expectedMaxNumberOfRetriesForServerInternalServerError(consistencyLevel, operationType))
            },
            new Object[] {
                "500-0_AllRegions_Create",
                Duration.ofSeconds(60),
                OperationType.Create,
                FaultInjectionOperationType.CREATE_ITEM,
                notSpecifiedWhetherIdempotentWriteRetriesAreEnabled,
                sameDocumentIdJustCreated,
                injectInternalServerErrorIntoAllRegions,
                validateStatusCodeIsInternalServerError,
                (TriConsumer<Integer, ConsistencyLevel, OperationType>)(requestCount, consistencyLevel, operationType) ->
                    assertThat(requestCount).isLessThanOrEqualTo(
                        expectedMaxNumberOfRetriesForServerInternalServerError(consistencyLevel, operationType))
            },
            new Object[] {
                "500-0_AllRegions_Create",
                Duration.ofSeconds(60),
                OperationType.Create,
                FaultInjectionOperationType.CREATE_ITEM,
                true, // IdempotentWriteRetries is enabled
                sameDocumentIdJustCreated,
                injectInternalServerErrorIntoAllRegions,
                validateStatusCodeIsInternalServerError,
                (TriConsumer<Integer, ConsistencyLevel, OperationType>)(requestCount, consistencyLevel, operationType) ->
                    assertThat(requestCount).isLessThanOrEqualTo(
                        expectedMaxNumberOfRetriesForServerInternalServerError(consistencyLevel, operationType))
            }
        };

        return addBooleanFlagsToAllTestConfigs(testConfigs_readMaxRetryCount_serverInternalServerError);
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
        Integer sessionTokenRetriesMaxBackoff,
        boolean shouldInjectPreferredRegions) {

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
                defaultNetworkRequestTimeoutDuration,
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
                ConnectionMode.DIRECT,
                defaultThrottlingRetryOptions,
                shouldInjectPreferredRegions);
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
        Consumer<Integer> maxExpectedRequestCountValidation,
        boolean shouldInjectPreferredRegions) {

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
            defaultNetworkRequestTimeoutDuration,
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
            ConnectionMode.DIRECT,
            defaultThrottlingRetryOptions,
            shouldInjectPreferredRegions);
    }

    @Test(groups = {"multi-master"}, dataProvider = "readMaxRetryCount_serverGone")
    public void readMaxRetryCount_serverGone(
        String testCaseId,
        Duration endToEndTimeout,
        OperationType operationType,
        FaultInjectionOperationType faultInjectionOperationType,
        Boolean isIdempotentWriteRetriesEnabled,
        String readItemDocumentIdOverride,
        BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> faultInjectionCallback,
        BiConsumer<Integer, Integer> validateStatusCode,
        TriConsumer<Integer, ConsistencyLevel, OperationType> maxExpectedRequestCountValidation,
        boolean shouldInjectPreferredRegions) {

        final int ONE_REGION = 1;
        final int TWO_REGIONS = 2;
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
            (ctx) -> {
                if (operationType.isReadOnlyOperation()) {
                    validateCtxRegions.accept(ctx, TWO_REGIONS);
                } else if (isIdempotentWriteRetriesEnabled != null && !isIdempotentWriteRetriesEnabled) {
                    validateCtxRegions.accept(ctx, TWO_REGIONS);
                } else {
                    validateCtxRegions.accept(ctx, ONE_REGION);
                }
            };

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

                // TODO: expand into other consistencies
                maxExpectedRequestCountValidation.accept(actualRequestCount, ConsistencyLevel.SESSION, operationType);
            }
        };

        execute(
            testCaseId,
            endToEndTimeout,
            noAvailabilityStrategy,
            CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
            isIdempotentWriteRetriesEnabled,
            defaultNetworkRequestTimeoutDuration,
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
            ConnectionMode.DIRECT,
            defaultThrottlingRetryOptions,
            shouldInjectPreferredRegions);
    }

    @Test(groups = {"multi-master"}, dataProvider = "readMaxRetryCount_transitTimeout")
    public void readMaxRetryCount_transitTimeout(
        String testCaseId,
        Duration endToEndTimeout,
        Duration networkRequestTimeout,
        OperationType operationType,
        FaultInjectionOperationType faultInjectionOperationType,
        Boolean isIdempotentWriteRetriesEnabled,
        String readItemDocumentIdOverride,
        BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> faultInjectionCallback,
        BiConsumer<Integer, Integer> validateStatusCode,
        TriConsumer<Integer, ConsistencyLevel, OperationType> maxExpectedRequestCountValidation,
        boolean shouldInjectPreferredRegions) {

        final int ONE_REGION = 1;
        final int TWO_REGIONS = 2;
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
            (ctx) -> {
                if (operationType.isReadOnlyOperation()) {
                    validateCtxRegions.accept(ctx, TWO_REGIONS);
                } else if (isIdempotentWriteRetriesEnabled != null && !isIdempotentWriteRetriesEnabled) {
                    validateCtxRegions.accept(ctx, TWO_REGIONS);
                } else {
                    validateCtxRegions.accept(ctx, ONE_REGION);
                }
            };

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

                // TODO: expand into other consistencies
                maxExpectedRequestCountValidation.accept(actualRequestCount, ConsistencyLevel.SESSION, operationType);
            }
        };

        execute(
            testCaseId,
            endToEndTimeout,
            noAvailabilityStrategy,
            CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
            isIdempotentWriteRetriesEnabled,
            networkRequestTimeout,
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
            ConnectionMode.DIRECT,
            defaultThrottlingRetryOptions,
            shouldInjectPreferredRegions);
    }

    @Test(groups = {"multi-master"}, dataProvider = "readMaxRetryCount_serverTimeout")
    public void readMaxRetryCount_serverTimeout(
        String testCaseId,
        Duration endToEndTimeout,
        OperationType operationType,
        FaultInjectionOperationType faultInjectionOperationType,
        Boolean isIdempotentWriteRetriesEnabled,
        String readItemDocumentIdOverride,
        BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> faultInjectionCallback,
        BiConsumer<Integer, Integer> validateStatusCode,
        TriConsumer<Integer, ConsistencyLevel, OperationType> maxExpectedRequestCountValidation,
        boolean shouldInjectPreferredRegions) {

        final int ONE_REGION = 1;
        final int TWO_REGIONS = 2;
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
            (ctx) -> {
                if (operationType.isReadOnlyOperation()) {
                    validateCtxRegions.accept(ctx, TWO_REGIONS);
                } else if (isIdempotentWriteRetriesEnabled != null && !isIdempotentWriteRetriesEnabled) {
                    validateCtxRegions.accept(ctx, TWO_REGIONS);
                } else {
                    validateCtxRegions.accept(ctx, ONE_REGION);
                }
            };

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

                // TODO: expand into other consistencies
                // TODO: currently, fault injection does not support 408 + delay, so the error is being injected without delay
                //  Will add the support in fault injection, and then will uncomment the following check
                // maxExpectedRequestCountValidation.accept(actualRequestCount, ConsistencyLevel.SESSION, operationType);
            }
        };

        execute(
            testCaseId,
            endToEndTimeout,
            noAvailabilityStrategy,
            CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
            isIdempotentWriteRetriesEnabled,
            defaultNetworkRequestTimeoutDuration,
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
            ConnectionMode.DIRECT,
            defaultThrottlingRetryOptions,
            shouldInjectPreferredRegions);
    }

    @Test(groups = {"multi-master"}, dataProvider = "readMaxRetryCount_serverServiceUnavailable")
    public void readMaxRetryCount_serverServiceUnavailable(
        String testCaseId,
        Duration endToEndTimeout,
        OperationType operationType,
        FaultInjectionOperationType faultInjectionOperationType,
        Boolean isIdempotentWriteRetriesEnabled,
        String readItemDocumentIdOverride,
        BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> faultInjectionCallback,
        BiConsumer<Integer, Integer> validateStatusCode,
        TriConsumer<Integer, ConsistencyLevel, OperationType> maxExpectedRequestCountValidation,
        boolean shouldInjectPreferredRegions) {

        final int ONE_REGION = 1;
        final int TWO_REGIONS = 2;
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
            (ctx) -> {
                if (operationType.isReadOnlyOperation()) {
                    validateCtxRegions.accept(ctx, TWO_REGIONS);
                } else if (isIdempotentWriteRetriesEnabled != null && !isIdempotentWriteRetriesEnabled) {
                    validateCtxRegions.accept(ctx, TWO_REGIONS);
                } else {
                    validateCtxRegions.accept(ctx, ONE_REGION);
                }
            };

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

                // TODO: expand into other consistencies
                maxExpectedRequestCountValidation.accept(actualRequestCount, ConsistencyLevel.SESSION, operationType);
            }
        };

        execute(
            testCaseId,
            endToEndTimeout,
            noAvailabilityStrategy,
            CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
            isIdempotentWriteRetriesEnabled,
            defaultNetworkRequestTimeoutDuration,
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
            ConnectionMode.DIRECT,
            defaultThrottlingRetryOptions,
            shouldInjectPreferredRegions);
    }

    @Test(groups = {"multi-master"}, dataProvider = "readMaxRetryCount_serverInternalServerError")
    public void readMaxRetryCount_serverInternalServerError(
        String testCaseId,
        Duration endToEndTimeout,
        OperationType operationType,
        FaultInjectionOperationType faultInjectionOperationType,
        Boolean isIdempotentWriteRetriesEnabled,
        String readItemDocumentIdOverride,
        BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> faultInjectionCallback,
        BiConsumer<Integer, Integer> validateStatusCode,
        TriConsumer<Integer, ConsistencyLevel, OperationType> maxExpectedRequestCountValidation,
        boolean shouldInjectPreferredRegions) {

        final int ONE_REGION = 1;
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

                // TODO: expand into other consistencies
                maxExpectedRequestCountValidation.accept(actualRequestCount, ConsistencyLevel.SESSION, operationType);
            }
        };

        execute(
            testCaseId,
            endToEndTimeout,
            noAvailabilityStrategy,
            CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
            isIdempotentWriteRetriesEnabled,
            defaultNetworkRequestTimeoutDuration,
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
            ConnectionMode.DIRECT,
            defaultThrottlingRetryOptions,
            shouldInjectPreferredRegions);
    }

    @Test(groups = {"multi-master"}, dataProvider = "readMaxRetryCount_serverRequestRateTooLarge")
    public void readMaxRetryCount_serverRequestRateTooLarge(
        String testCaseId,
        Duration endToEndTimeout,
        ThrottlingRetryOptions throttlingRetryOptions,
        OperationType operationType,
        FaultInjectionOperationType faultInjectionOperationType,
        Boolean isIdempotentWriteRetriesEnabled,
        String readItemDocumentIdOverride,
        BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> faultInjectionCallback,
        BiConsumer<Integer, Integer> validateStatusCode,
        TriConsumer<Integer, ConsistencyLevel, OperationType> maxExpectedRequestCountValidation,
        boolean shouldInjectPreferredRegions) {

        final int ONE_REGION = 1;
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

                // TODO: expand into other consistencies
                maxExpectedRequestCountValidation.accept(actualRequestCount, ConsistencyLevel.SESSION, operationType);
            }
        };

        execute(
            testCaseId,
            endToEndTimeout,
            noAvailabilityStrategy,
            CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
            isIdempotentWriteRetriesEnabled,
            defaultNetworkRequestTimeoutDuration,
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
            ConnectionMode.DIRECT,
            throttlingRetryOptions,
            shouldInjectPreferredRegions);
    }

    // Once validate the algorithm by using the e2e tests, this is a quick method to only log the max count
    // can be removed
    private void logMaxCount() {
        // gone
        final int DEFAULT_WAIT_TIME_IN_MS = 30 * 1000;
        final int DEFAULT_MAXIMUM_BACKOFF_TIME_IN_MS = 15 * 1000;
        final int DEFAULT_INITIAL_BACKOFF_TIME_IN_MS = 1000;
        final int DEFAULT_BACK_OFF_MULTIPLIER = 2;

        for (OperationType operationType : Arrays.asList(OperationType.Read, OperationType.Create)) {
            for (ConsistencyLevel consistencyLevel : ConsistencyLevel.values()) {
                expectedMaxNumberOfRetriesForGone(
                    DEFAULT_WAIT_TIME_IN_MS,
                    DEFAULT_MAXIMUM_BACKOFF_TIME_IN_MS,
                    DEFAULT_INITIAL_BACKOFF_TIME_IN_MS,
                    DEFAULT_BACK_OFF_MULTIPLIER,
                    consistencyLevel,
                    operationType
                );
            }
        }


        // transitTimeout
        for (OperationType operationType : Arrays.asList(OperationType.Read, OperationType.Create)) {
            for (ConsistencyLevel consistencyLevel : ConsistencyLevel.values()) {
                expectedMaxNumberOfRetriesForTransientTimeout(
                    DEFAULT_WAIT_TIME_IN_MS,
                    DEFAULT_MAXIMUM_BACKOFF_TIME_IN_MS,
                    DEFAULT_INITIAL_BACKOFF_TIME_IN_MS,
                    DEFAULT_BACK_OFF_MULTIPLIER,
                    consistencyLevel,
                    operationType,
                    Duration.ofSeconds(1),
                    false
                );

                if (operationType.isWriteOperation()) {
                    expectedMaxNumberOfRetriesForTransientTimeout(
                        DEFAULT_WAIT_TIME_IN_MS,
                        DEFAULT_MAXIMUM_BACKOFF_TIME_IN_MS,
                        DEFAULT_INITIAL_BACKOFF_TIME_IN_MS,
                        DEFAULT_BACK_OFF_MULTIPLIER,
                        consistencyLevel,
                        operationType,
                        Duration.ofSeconds(1),
                        true
                    );
                }
            }
        }
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
        FaultInjectionServerErrorResult retryWithResult = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.RETRY_WITH)
            .build();

        inject(
            ruleName,
            containerWithSeveralWriteableRegions,
            applicableRegions,
            faultInjectionOperationType,
            retryWithResult,
            null
        );
    }

    private static void injectServerGoneError(
        CosmosAsyncContainer containerWithSeveralWriteableRegions,
        List<String> applicableRegions,
        FaultInjectionOperationType faultInjectionOperationType) {

        String ruleName = "serverErrorRule-serverGoneError-" + UUID.randomUUID();
        FaultInjectionServerErrorResult serverGoneResult = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.GONE)
            .build();

        inject(
            ruleName,
            containerWithSeveralWriteableRegions,
            applicableRegions,
            faultInjectionOperationType,
            serverGoneResult,
            null
        );
    }

    private static void injectTransitTimeoutError(
        CosmosAsyncContainer containerWithSeveralWriteableRegions,
        List<String> applicableRegions,
        FaultInjectionOperationType faultInjectionOperationType,
        Duration networkRequestTimeout) {

        String ruleName = "serverErrorRule-transitTimeoutError-" + UUID.randomUUID();
        FaultInjectionServerErrorResult transitTimeoutResult = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
            .delay(networkRequestTimeout.plus(Duration.ofMillis(100)))
            .build();

        inject(
            ruleName,
            containerWithSeveralWriteableRegions,
            applicableRegions,
            faultInjectionOperationType,
            transitTimeoutResult,
            null
        );
    }

    private static void injectServerTimeoutError(
        CosmosAsyncContainer containerWithSeveralWriteableRegions,
        List<String> applicableRegions,
        FaultInjectionOperationType faultInjectionOperationType) {

        String ruleName = "serverErrorRule-serverTimeout-" + UUID.randomUUID();
        FaultInjectionServerErrorResult serverTimeoutResult = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.TIMEOUT)
            .build();

        inject(
            ruleName,
            containerWithSeveralWriteableRegions,
            applicableRegions,
            faultInjectionOperationType,
            serverTimeoutResult,
            null
        );
    }

    private static void injectServerRequestRateTooLargeError(
        CosmosAsyncContainer containerWithSeveralWriteableRegions,
        List<String> applicableRegions,
        FaultInjectionOperationType faultInjectionOperationType) {

        String ruleName = "serverErrorRule-server429-" + UUID.randomUUID();
        FaultInjectionServerErrorResult serverRequestRateTooLargeResult = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.TOO_MANY_REQUEST)
            .build();

        inject(
            ruleName,
            containerWithSeveralWriteableRegions,
            applicableRegions,
            faultInjectionOperationType,
            serverRequestRateTooLargeResult,
            null
        );
    }

    private void execute(
        String testCaseId,
        Duration endToEndTimeout,
        ThresholdBasedAvailabilityStrategy availabilityStrategy,
        CosmosRegionSwitchHint regionSwitchHint,
        Boolean nonIdempotentWriteRetriesEnabled,
        Duration networkRequestTimeout,
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
        ConnectionMode connectionMode,
        ThrottlingRetryOptions throttlingRetryOptions,
        boolean shouldInjectPreferredRegions) {

        logger.info("START {}", testCaseId);

        CosmosAsyncClient clientWithPreferredRegions = buildCosmosClient(
            this.writeableRegions,
            regionSwitchHint,
            nonIdempotentWriteRetriesEnabled,
            connectionMode,
            networkRequestTimeout,
            throttlingRetryOptions,
            shouldInjectPreferredRegions);

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
        ConnectionMode connectionMode,
        Duration networkRequestTimeout,
        ThrottlingRetryOptions throttlingRetryOptions,
        Boolean shouldInjectPreferredRegions) {

        CosmosClientTelemetryConfig telemetryConfig = new CosmosClientTelemetryConfig()
            .diagnosticsHandler(new CosmosDiagnosticsLogger());

        CosmosRegionSwitchHint effectiveRegionSwitchHint = regionSwitchHint != null
            ? regionSwitchHint
            : CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED;
        SessionRetryOptionsBuilder retryOptionsBuilder = new SessionRetryOptionsBuilder()
            .regionSwitchHint(effectiveRegionSwitchHint);

        CosmosClientBuilder builder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .consistencyLevel(ConsistencyLevel.SESSION)
            .preferredRegions(shouldInjectPreferredRegions ? preferredRegions : Collections.emptyList())
            .sessionRetryOptions(retryOptionsBuilder.build())
            .multipleWriteRegionsEnabled(true)
            .clientTelemetryConfig(telemetryConfig);

        if (throttlingRetryOptions != null) {
            builder.throttlingRetryOptions(throttlingRetryOptions);
        }

        if (connectionMode == ConnectionMode.GATEWAY) {
            builder.gatewayMode();
        } else {
            DirectConnectionConfig directConnectionConfig = DirectConnectionConfig.getDefaultConfig();
            if (networkRequestTimeout != null) {
                directConnectionConfig.setNetworkRequestTimeout(networkRequestTimeout);
            }
            builder.directMode(directConnectionConfig);
        }

        if (nonIdempotentWriteRetriesEnabled != null) {
            builder.nonIdempotentWriteRetryOptions(
                new NonIdempotentWriteRetryOptions()
                    .setEnabled(nonIdempotentWriteRetriesEnabled)
                    .setTrackingIdUsed(true));
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

    private Object[][] addBooleanFlagsToAllTestConfigs(Object[][] testConfigs) {
        List<List<Object>> intermediateTestConfigList = new ArrayList<>();
        boolean[] possibleBooleans = new boolean[]{true, false};

        for (boolean possibleBoolean : possibleBooleans) {
            for (Object[] testConfigForSingleTest : testConfigs) {
                List<Object> testConfigForSingleTestAsMutableList = new ArrayList<>(Arrays.asList(testConfigForSingleTest));
                testConfigForSingleTestAsMutableList.add(possibleBoolean);
                intermediateTestConfigList.add(testConfigForSingleTestAsMutableList);
            }
        }

        testConfigs = intermediateTestConfigList.stream()
            .map(l -> l.stream().toArray(Object[]::new))
            .toArray(Object[][]::new);

        return testConfigs;
    }

    private AccountLevelLocationContext getAccountLevelLocationContext(DatabaseAccount databaseAccount, boolean writeOnly) {
        Iterator<DatabaseAccountLocation> locationIterator =
            writeOnly ? databaseAccount.getWritableLocations().iterator() : databaseAccount.getReadableLocations().iterator();

        List<String> serviceOrderedReadableRegions = new ArrayList<>();
        List<String> serviceOrderedWriteableRegions = new ArrayList<>();
        Map<String, String> regionMap = new ConcurrentHashMap<>();

        while (locationIterator.hasNext()) {
            DatabaseAccountLocation accountLocation = locationIterator.next();
            regionMap.put(accountLocation.getName(), accountLocation.getEndpoint());

            if (writeOnly) {
                serviceOrderedWriteableRegions.add(accountLocation.getName());
            } else {
                serviceOrderedReadableRegions.add(accountLocation.getName());
            }
        }

        return new AccountLevelLocationContext(
            serviceOrderedReadableRegions,
            serviceOrderedWriteableRegions,
            regionMap);
    }

    private static void validate(AccountLevelLocationContext accountLevelLocationContext, boolean isWriteOnly) {

        assertThat(accountLevelLocationContext).isNotNull();

        if (isWriteOnly) {
            assertThat(accountLevelLocationContext.serviceOrderedWriteableRegions).isNotNull();
            assertThat(accountLevelLocationContext.serviceOrderedWriteableRegions.size()).isGreaterThanOrEqualTo(1);
        } else {
            assertThat(accountLevelLocationContext.serviceOrderedReadableRegions).isNotNull();
            assertThat(accountLevelLocationContext.serviceOrderedReadableRegions.size()).isGreaterThanOrEqualTo(1);
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

    private static class AccountLevelLocationContext {
        private final List<String> serviceOrderedReadableRegions;
        private final List<String> serviceOrderedWriteableRegions;
        private final Map<String, String> regionNameToEndpoint;

        public AccountLevelLocationContext(
            List<String> serviceOrderedReadableRegions,
            List<String> serviceOrderedWriteableRegions,
            Map<String, String> regionNameToEndpoint) {

            this.serviceOrderedReadableRegions = serviceOrderedReadableRegions;
            this.serviceOrderedWriteableRegions = serviceOrderedWriteableRegions;
            this.regionNameToEndpoint = regionNameToEndpoint;
        }
    }
}
