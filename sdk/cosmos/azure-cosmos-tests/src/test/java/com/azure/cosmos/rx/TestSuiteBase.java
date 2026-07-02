// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.rx;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncClientTest;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosAsyncUser;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.CosmosDatabaseForTest;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosDiagnosticsContext;
import com.azure.cosmos.CosmosDiagnosticsRequestInfo;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfigBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.CosmosNettyLeakDetectorFactory;
import com.azure.cosmos.CosmosResponseValidator;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.Http2ConnectionConfig;
import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.Database;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.FailureValidator;
import com.azure.cosmos.implementation.FeedResponseListValidator;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.PartitionKeyHelper;
import com.azure.cosmos.implementation.Permission;
import com.azure.cosmos.implementation.QueryFeedOperationState;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.ResourceResponseValidator;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.TestUtils;
import com.azure.cosmos.implementation.User;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.clienttelemetry.ClientTelemetry;
import com.azure.cosmos.implementation.directconnectivity.Protocol;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.guava25.base.CaseFormat;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.cosmos.models.ChangeFeedPolicy;
import com.azure.cosmos.models.CompositePath;
import com.azure.cosmos.models.CompositePathSortOrder;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosDatabaseProperties;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosBulkOperationResponse;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosResponse;
import com.azure.cosmos.models.CosmosStoredProcedureRequestOptions;
import com.azure.cosmos.models.CosmosUserProperties;
import com.azure.cosmos.models.CosmosUserResponse;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.IncludedPath;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.PartitionKeyDefinitionVersion;
import com.azure.cosmos.models.PartitionKind;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.mockito.stubbing.Answer;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.util.retry.Retry;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.azure.cosmos.BridgeInternal.extractConfigs;
import static com.azure.cosmos.BridgeInternal.injectConfigs;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

public abstract class TestSuiteBase extends CosmosAsyncClientTest {

    protected static final String THIN_CLIENT_ENDPOINT_INDICATOR = ":10250/";
    private static final int DEFAULT_BULK_INSERT_CONCURRENCY_LEVEL = 5;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final CosmosItemRequestOptions DEFAULT_DELETE_ITEM_OPTIONS = new CosmosItemRequestOptions()
        .setCosmosEndToEndOperationLatencyPolicyConfig(
            new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofHours(1))
                .build()
        );

    protected static final int TIMEOUT = 40000;
    protected static final int FEED_TIMEOUT = 40000;
    protected static final int SETUP_TIMEOUT = 300_000;
    protected static final int SHUTDOWN_TIMEOUT = 24000;

    private static final int SHARED_SUITE_SETUP_TIMEOUT = 600_000;

    protected static final int SUITE_SHUTDOWN_TIMEOUT = 60000;

    protected static final int WAIT_REPLICA_CATCH_UP_IN_MILLIS = 4000;

    private static final Duration COLLECTION_READINESS_MAX_WAIT = Duration.ofMinutes(2);

    private static final Duration COLLECTION_READINESS_PROBE_TIMEOUT = Duration.ofSeconds(10);

    private static final Duration NOT_FOUND_RETRY_DELAY = Duration.ofSeconds(1);

    private static final int NOT_FOUND_MAX_RETRY_ATTEMPTS = 12;

    private static final Duration TRANSIENT_CLEANUP_RETRY_DELAY = Duration.ofSeconds(1);

    private static final int TRANSIENT_CLEANUP_MAX_RETRY_ATTEMPTS = 30;

    private static final Duration STORED_PROCEDURE_QUERY_RETRY_DELAY = Duration.ofSeconds(1);

    private static final int STORED_PROCEDURE_QUERY_ATTEMPT_TIMEOUT = 5_000;

    private static final Duration STORED_PROCEDURE_QUERY_MAX_RETRY_DURATION = Duration.ofSeconds(30);

    private static final Duration FEED_RANGE_WARMUP_MAX_WAIT = COLLECTION_READINESS_MAX_WAIT;

    private static final Duration FEED_RANGE_WARMUP_ATTEMPT_TIMEOUT = Duration.ofSeconds(30);

    private static boolean isTransientCreateFailure(Throwable t) {
        if (t instanceof CosmosException) {
            int statusCode = ((CosmosException) t).getStatusCode();
            return statusCode == 408 || statusCode == 429;
        }
        return false;
    }

    private static boolean isConflictException(Throwable t) {
        return t instanceof CosmosException && ((CosmosException) t).getStatusCode() == 409;
    }

    /**
     * Executes an action with retry logic for transient failures in @BeforeClass setup methods.
     * Retries up to maxRetries times with increasing backoff (1s, 2s, 3s...).
     *
     * @param action   the action to execute
     * @param maxRetries maximum number of retries
     * @param context  description for logging (e.g., test class name)
     */
    protected static void executeWithRetry(Runnable action, int maxRetries, String context) {
        for (int i = 0; i < maxRetries; i++) {
            try {
                action.run();
                return;
            } catch (Exception e) {
                if (i < maxRetries - 1) {
                    logger.warn("Retrying {} after failure (attempt {}): {}", context, i + 1, e.getMessage());
                    try { Thread.sleep(1000L * (i + 1)); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                } else {
                    throw e;
                }
            }
        }
    }

    protected static <T> Mono<T> retryOnNotFound(Mono<T> responseMono) {

        return responseMono.retryWhen(
            Retry.fixedDelay(NOT_FOUND_MAX_RETRY_ATTEMPTS, NOT_FOUND_RETRY_DELAY)
                .filter(TestSuiteBase::isNotFound)
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> retrySignal.failure()));
    }

    protected static <T> T retryOnNotFound(Supplier<T> responseSupplier) throws InterruptedException {
        for (int attempt = 0; attempt <= NOT_FOUND_MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                return responseSupplier.get();
            } catch (CosmosException cosmosException) {
                if (cosmosException.getStatusCode() != HttpConstants.StatusCodes.NOTFOUND
                    || attempt == NOT_FOUND_MAX_RETRY_ATTEMPTS) {

                    throw cosmosException;
                }

                logger.warn(
                    "Retrying NotFound response after {}. Retry attempt {}.",
                    NOT_FOUND_RETRY_DELAY,
                    attempt + 1);
                Thread.sleep(NOT_FOUND_RETRY_DELAY.toMillis());
            }
        }

        throw new IllegalStateException("Retry loop completed unexpectedly.");
    }

    protected static List<FeedRange> getFeedRangesWithRetry(CosmosAsyncContainer container, String context) {
        return getFeedRangesWithRetry(container, context, FEED_RANGE_WARMUP_MAX_WAIT);
    }

    protected static List<FeedRange> getFeedRangesWithRetry(
        CosmosAsyncContainer container,
        String context,
        Duration maxWait) {

        long deadlineNanos = System.nanoTime() + maxWait.toNanos();
        long backoffMillis = 1_000;
        long maxBackoffMillis = 10_000;
        int attempts = 0;
        Throwable lastError = null;

        while (System.nanoTime() < deadlineNanos) {
            attempts++;
            try {
                long remainingNanos = deadlineNanos - System.nanoTime();
                Duration attemptTimeout = Duration.ofMillis(
                    Math.max(
                        1,
                        Math.min(
                            FEED_RANGE_WARMUP_ATTEMPT_TIMEOUT.toMillis(),
                            TimeUnit.NANOSECONDS.toMillis(remainingNanos))));

                List<FeedRange> feedRanges = container.getFeedRanges().block(attemptTimeout);
                if (feedRanges != null && !feedRanges.isEmpty()) {
                    return feedRanges;
                }

                lastError = new IllegalStateException("Feed ranges were not available for container " + container.getId());
            } catch (Exception error) {
                lastError = error;
            }

            if (!isRetryableFeedRangeWarmupFailure(lastError)) {
                throw new AssertionError(
                    String.format(
                        "Feed ranges for container '%s' failed with a non-retryable error after %d attempt(s) during %s: %s",
                        container.getId(),
                        attempts,
                        context,
                        getFeedRangeWarmupErrorDetails(lastError)),
                    lastError);
            }

            long remainingNanos = deadlineNanos - System.nanoTime();
            if (remainingNanos <= 0) {
                break;
            }

            long retryAfterMillis = getRetryAfterMillis(lastError);
            long sleepMillis = Math.max(backoffMillis, retryAfterMillis);
            sleepMillis = Math.max(1, Math.min(sleepMillis, TimeUnit.NANOSECONDS.toMillis(remainingNanos)));

            logger.warn(
                "Retrying {} after failure (attempt {}, next delay {} ms, max wait {} seconds): {}",
                context,
                attempts,
                sleepMillis,
                maxWait.getSeconds(),
                getFeedRangeWarmupErrorDetails(lastError));

            try {
                TimeUnit.MILLISECONDS.sleep(sleepMillis);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for feed ranges during " + context, interrupted);
            }

            backoffMillis = Math.min(backoffMillis * 2, maxBackoffMillis);
        }

        throw new AssertionError(
            String.format(
                "Feed ranges for container '%s' were not available within %d seconds after %d attempt(s) during %s.",
                container.getId(),
                maxWait.getSeconds(),
                attempts,
                context),
            lastError);
    }

    private static boolean isRetryableFeedRangeWarmupFailure(Throwable error) {
        CosmosException cosmosException = getCosmosException(error);
        if (cosmosException != null) {
            int statusCode = cosmosException.getStatusCode();
            return statusCode == HttpConstants.StatusCodes.REQUEST_TIMEOUT
                || statusCode == HttpConstants.StatusCodes.UNAUTHORIZED
                || statusCode == HttpConstants.StatusCodes.TOO_MANY_REQUESTS
                || statusCode == HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR
                || statusCode == HttpConstants.StatusCodes.SERVICE_UNAVAILABLE
                || statusCode == HttpConstants.StatusCodes.GONE
                || statusCode == HttpConstants.StatusCodes.NOTFOUND;
        }

        Throwable unwrappedException = Exceptions.unwrap(error);
        if (unwrappedException instanceof IllegalStateException) {
            String message = unwrappedException.getMessage();
            return message != null
                && (message.contains("Feed ranges were not available")
                    || message.contains("Timeout on blocking read"));
        }

        return false;
    }

    private static String getFeedRangeWarmupErrorDetails(Throwable error) {
        CosmosException cosmosException = getCosmosException(error);
        if (cosmosException != null) {
            return String.format(
                "statusCode=%d subStatusCode=%d message=%s",
                cosmosException.getStatusCode(),
                cosmosException.getSubStatusCode(),
                cosmosException.getMessage());
        }

        Throwable unwrappedException = Exceptions.unwrap(error);
        if (unwrappedException == null) {
            return "unknown failure";
        }

        return unwrappedException.getClass().getSimpleName() + ": " + unwrappedException.getMessage();
    }

    private static long getRetryAfterMillis(Throwable error) {
        CosmosException cosmosException = getCosmosException(error);
        if (cosmosException != null) {
            Duration retryAfterDuration = cosmosException.getRetryAfterDuration();
            return retryAfterDuration != null ? Math.max(0, retryAfterDuration.toMillis()) : 0;
        }

        return 0;
    }

    private static CosmosException getCosmosException(Throwable error) {
        Throwable currentException = Exceptions.unwrap(error);
        while (currentException != null) {
            if (currentException instanceof CosmosException) {
                return (CosmosException) currentException;
            }

            currentException = currentException.getCause();
        }

        return null;
    }

    private static <T> Mono<T> retryOnTransientCleanupFailure(Mono<T> responseMono) {
        return responseMono.retryWhen(
            Retry.fixedDelay(TRANSIENT_CLEANUP_MAX_RETRY_ATTEMPTS, TRANSIENT_CLEANUP_RETRY_DELAY)
                .filter(TestSuiteBase::isTransientCleanupFailure)
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> retrySignal.failure()));
    }

    private static <T> Flux<T> retryOnTransientCleanupFailure(Flux<T> responseFlux) {
        return responseFlux.retryWhen(
            Retry.fixedDelay(TRANSIENT_CLEANUP_MAX_RETRY_ATTEMPTS, TRANSIENT_CLEANUP_RETRY_DELAY)
                .filter(TestSuiteBase::isTransientCleanupFailure)
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> retrySignal.failure()));
    }

    private static boolean isTransientCleanupFailure(Throwable throwable) {
        Throwable unwrappedException = Exceptions.unwrap(throwable);
        if (!(unwrappedException instanceof CosmosException)) {
            return false;
        }

        int statusCode = ((CosmosException) unwrappedException).getStatusCode();
        return statusCode == HttpConstants.StatusCodes.TOO_MANY_REQUESTS
            || statusCode == HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR
            || statusCode == HttpConstants.StatusCodes.SERVICE_UNAVAILABLE;
    }

    protected static <T> void validateCosmosPagedIterableWithRetry(
        Supplier<CosmosPagedIterable<T>> pagedIterableSupplier,
        Consumer<CosmosPagedIterable<T>> validator,
        String context) throws InterruptedException {

        validateWithRetry(() -> validator.accept(pagedIterableSupplier.get()), context);
    }

    protected static <T extends Resource> FeedResponse<T> readManyWithRetry(
        CosmosAsyncContainer container,
        List<CosmosItemIdentity> cosmosItemIdentities,
        Collection<String> expectedIds,
        Class<T> classType) throws InterruptedException {

        AtomicReference<FeedResponse<T>> feedResponseReference = new AtomicReference<>();

        validateWithRetry(() -> {
            FeedResponse<T> feedResponse = container.readMany(cosmosItemIdentities, classType).block();

            assertThat(feedResponse).isNotNull();
            assertThat(feedResponse.getResults()).isNotNull();
            assertThat(feedResponse.getResults()).hasSize(expectedIds.size());

            for (T fetchedResult : feedResponse.getResults()) {
                assertThat(expectedIds.contains(fetchedResult.getId())).isTrue();
            }

            feedResponseReference.set(feedResponse);
        }, "readMany visibility after item creation");

        return feedResponseReference.get();
    }

    @FunctionalInterface
    protected interface RetryableValidation {
        void validate() throws InterruptedException;
    }

    protected static void validateWithRetry(RetryableValidation validator, String context) throws InterruptedException {

        long retryStartNanos = System.nanoTime();
        AssertionError lastAssertionError;

        do {
            try {
                validator.validate();
                return;
            } catch (AssertionError assertionError) {
                lastAssertionError = assertionError;
                Duration elapsed = Duration.ofNanos(System.nanoTime() - retryStartNanos);
                if (elapsed.compareTo(STORED_PROCEDURE_QUERY_MAX_RETRY_DURATION) >= 0) {
                    throw lastAssertionError;
                }

                logger.warn(
                    "{} did not return expected results yet. Retrying after {}.",
                    context,
                    STORED_PROCEDURE_QUERY_RETRY_DELAY);
                Thread.sleep(STORED_PROCEDURE_QUERY_RETRY_DELAY.toMillis());
            }
        } while (true);
    }

    protected static <T> void validateFeedResponseListWithRetry(
        Supplier<Flux<FeedResponse<T>>> feedResponseSupplier,
        FeedResponseListValidator<T> validator,
        String context) throws InterruptedException {

        validateWithRetry(
            () -> validateQuerySuccess(feedResponseSupplier.get(), validator, STORED_PROCEDURE_QUERY_ATTEMPT_TIMEOUT),
            context);
    }

    private static boolean isNotFound(Throwable throwable) {
        Throwable unwrappedException = Exceptions.unwrap(throwable);
        return unwrappedException instanceof CosmosException
            && ((CosmosException) unwrappedException).getStatusCode() == HttpConstants.StatusCodes.NOTFOUND;
    }

    protected final static ConsistencyLevel accountConsistency;
    protected static final ImmutableList<String> preferredLocations;
    private static final ImmutableList<ConsistencyLevel> desiredConsistencies;
    protected static final ImmutableList<Protocol> protocols;

    protected static final AzureKeyCredential credential;

    protected int subscriberValidationTimeout = TIMEOUT;

    protected static CosmosAsyncDatabase SHARED_DATABASE;
    protected static CosmosAsyncContainer SHARED_MULTI_PARTITION_COLLECTION_WITH_ID_AS_PARTITION_KEY;
    protected static CosmosAsyncContainer SHARED_MULTI_PARTITION_COLLECTION;
    protected static CosmosAsyncContainer SHARED_MULTI_PARTITION_COLLECTION_WITH_COMPOSITE_AND_SPATIAL_INDEXES;
    protected static CosmosAsyncContainer SHARED_SINGLE_PARTITION_COLLECTION;

    // Internal API shared resources for tests using AsyncDocumentClient
    protected static Database SHARED_DATABASE_INTERNAL;
    protected static DocumentCollection SHARED_MULTI_PARTITION_COLLECTION_INTERNAL;
    protected static DocumentCollection SHARED_SINGLE_PARTITION_COLLECTION_INTERNAL;
    protected static DocumentCollection SHARED_MULTI_PARTITION_COLLECTION_WITH_COMPOSITE_AND_SPATIAL_INDEXES_INTERNAL;

    // Field to hold AsyncDocumentClient.Builder for internal API tests
    private final AsyncDocumentClient.Builder internalClientBuilder;

    public TestSuiteBase(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
        this.internalClientBuilder = null;
    }

    /**
     * Constructor for tests that use internal AsyncDocumentClient API.
     * @param clientBuilder the AsyncDocumentClient.Builder
     */
    public TestSuiteBase(AsyncDocumentClient.Builder clientBuilder) {
        super();
        this.internalClientBuilder = clientBuilder;
        logger.debug("Initializing {} with AsyncDocumentClient.Builder ...", this.getClass().getSimpleName());
    }

    /**
     * Default constructor for tests that don't use any client builder.
     */
    protected TestSuiteBase() {
        super();
        this.internalClientBuilder = null;
        logger.debug("Initializing {} ...", this.getClass().getSimpleName());
    }

    /**
     * Returns the AsyncDocumentClient.Builder for internal client tests.
     * @return the AsyncDocumentClient.Builder or null if not configured
     */
    public final AsyncDocumentClient.Builder clientBuilder() {
        return this.internalClientBuilder;
    }

    protected static CosmosAsyncDatabase getSharedCosmosDatabase(CosmosAsyncClient client) {
        return CosmosBridgeInternal.getCosmosDatabaseWithNewClient(SHARED_DATABASE, client);
    }

    protected static CosmosAsyncContainer getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(CosmosAsyncClient client) {
        return CosmosBridgeInternal.getCosmosContainerWithNewClient(SHARED_MULTI_PARTITION_COLLECTION_WITH_ID_AS_PARTITION_KEY, SHARED_DATABASE, client);
    }

    protected static CosmosAsyncContainer getSharedMultiPartitionCosmosContainer(CosmosAsyncClient client) {
        return CosmosBridgeInternal.getCosmosContainerWithNewClient(SHARED_MULTI_PARTITION_COLLECTION, SHARED_DATABASE, client);
    }

    protected static CosmosAsyncContainer getSharedMultiPartitionCosmosContainerWithCompositeAndSpatialIndexes(CosmosAsyncClient client) {
        return CosmosBridgeInternal.getCosmosContainerWithNewClient(SHARED_MULTI_PARTITION_COLLECTION_WITH_COMPOSITE_AND_SPATIAL_INDEXES, SHARED_DATABASE, client);
    }

    protected static CosmosAsyncContainer getSharedSinglePartitionCosmosContainer(CosmosAsyncClient client) {
        return CosmosBridgeInternal.getCosmosContainerWithNewClient(SHARED_SINGLE_PARTITION_COLLECTION, SHARED_DATABASE, client);
    }

    static {
        CosmosNettyLeakDetectorFactory.ingestIntoNetty();
        accountConsistency = parseConsistency(TestConfigurations.CONSISTENCY);
        desiredConsistencies = immutableListOrNull(
            ObjectUtils.defaultIfNull(parseDesiredConsistencies(TestConfigurations.DESIRED_CONSISTENCIES),
                allEqualOrLowerConsistencies(accountConsistency)));
        preferredLocations = immutableListOrNull(parsePreferredLocation(TestConfigurations.PREFERRED_LOCATIONS));
        protocols = ObjectUtils.defaultIfNull(immutableListOrNull(parseProtocols(TestConfigurations.PROTOCOLS)),
            ImmutableList.of(Protocol.TCP));

        //  Object mapper configurations
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_TRAILING_COMMA, true);
        objectMapper.configure(JsonParser.Feature.STRICT_DUPLICATE_DETECTION, true);

        credential = new AzureKeyCredential(TestConfigurations.MASTER_KEY);
    }

    private static <T> ImmutableList<T> immutableListOrNull(List<T> list) {
        return list != null ? ImmutableList.copyOf(list) : null;
    }

    private static class DatabaseManagerImpl implements CosmosDatabaseForTest.DatabaseManager {
        public static DatabaseManagerImpl getInstance(CosmosAsyncClient client) {
            return new DatabaseManagerImpl(client);
        }

        private final CosmosAsyncClient client;

        private DatabaseManagerImpl(CosmosAsyncClient client) {
            this.client = client;
        }

        @Override
        public CosmosPagedFlux<CosmosDatabaseProperties> queryDatabases(SqlQuerySpec query) {
            return client.queryDatabases(query, null);
        }

        @Override
        public Mono<CosmosDatabaseResponse> createDatabase(CosmosDatabaseProperties databaseDefinition) {
            return client.createDatabase(databaseDefinition);
        }

        @Override
        public CosmosAsyncDatabase getDatabase(String id) {
            return client.getDatabase(id);
        }
    }

    @BeforeSuite(groups = {"thinclient", "fast", "long", "direct", "multi-region", "multi-master", "flaky-multi-master", "emulator",
        "emulator-vnext", "split", "query", "cfp-split", "circuit-breaker-misc-gateway", "circuit-breaker-misc-direct",
        "circuit-breaker-read-all-read-many", "fi-multi-master", "fi-customer-workflows", "fi-sm-customer-workflows", "long-emulator", "fi-thinclient-multi-region", "fi-thinclient-multi-master", "multi-region-strong", "manual-http-network-fault", "consistency-overrides"}, timeOut = SHARED_SUITE_SETUP_TIMEOUT)
    public void beforeSuite() {

        logger.info("beforeSuite Started");

        try (CosmosAsyncClient houseKeepingClient = createGatewayHouseKeepingDocumentClient(true).buildAsyncClient()) {
            CosmosDatabaseForTest dbForTest = CosmosDatabaseForTest.create(DatabaseManagerImpl.getInstance(houseKeepingClient));
            SHARED_DATABASE = dbForTest.createdDatabase;
            CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
            SHARED_MULTI_PARTITION_COLLECTION = createCollection(SHARED_DATABASE, getCollectionDefinitionWithRangeRangeIndex(), options, 10100);
            SHARED_MULTI_PARTITION_COLLECTION_WITH_ID_AS_PARTITION_KEY = createCollection(SHARED_DATABASE, getCollectionDefinitionWithRangeRangeIndexWithIdAsPartitionKey(), options, 10100);
            SHARED_MULTI_PARTITION_COLLECTION_WITH_COMPOSITE_AND_SPATIAL_INDEXES = createCollection(SHARED_DATABASE, getCollectionDefinitionMultiPartitionWithCompositeAndSpatialIndexes(), options);
            SHARED_SINGLE_PARTITION_COLLECTION = createCollection(SHARED_DATABASE, getCollectionDefinitionWithRangeRangeIndex(), options, 6000);

            // Initialize internal shared resources for tests using AsyncDocumentClient
            // These need id, resourceId, selfLink, altLink, and partitionKey to be set properly
            String databaseId = SHARED_DATABASE.getId();
            String databaseResourceId = SHARED_DATABASE.read().block().getProperties().getResourceId();

            SHARED_DATABASE_INTERNAL = new Database();
            SHARED_DATABASE_INTERNAL.setId(databaseId);
            SHARED_DATABASE_INTERNAL.setResourceId(databaseResourceId);
            SHARED_DATABASE_INTERNAL.setSelfLink(String.format("dbs/%s", databaseId));
            SHARED_DATABASE_INTERNAL.setAltLink(String.format("dbs/%s", databaseId));

            SHARED_MULTI_PARTITION_COLLECTION_INTERNAL = getInternalDocumentCollection(SHARED_MULTI_PARTITION_COLLECTION, databaseId);
            SHARED_SINGLE_PARTITION_COLLECTION_INTERNAL = getInternalDocumentCollection(SHARED_SINGLE_PARTITION_COLLECTION, databaseId);
            SHARED_MULTI_PARTITION_COLLECTION_WITH_COMPOSITE_AND_SPATIAL_INDEXES_INTERNAL =
                getInternalDocumentCollection(SHARED_MULTI_PARTITION_COLLECTION_WITH_COMPOSITE_AND_SPATIAL_INDEXES, databaseId);
        }
    }

    /**
     * Creates a DocumentCollection with all required properties set for internal API tests.
     * Sets: id, resourceId, selfLink, altLink, and partitionKey.
     */
    private static DocumentCollection getInternalDocumentCollection(CosmosAsyncContainer container, String databaseId) {
        CosmosContainerProperties containerProperties = container.read().block().getProperties();

        DocumentCollection collection = new DocumentCollection();
        collection.setId(container.getId());
        collection.setResourceId(containerProperties.getResourceId());
        collection.setSelfLink(String.format("dbs/%s/colls/%s", databaseId, container.getId()));
        collection.setAltLink(String.format("dbs/%s/colls/%s", databaseId, container.getId()));
        collection.setPartitionKey(containerProperties.getPartitionKeyDefinition());

        return collection;
    }

    @AfterSuite(groups = {"thinclient", "fast", "long", "direct", "multi-region", "multi-master", "flaky-multi-master",
        "emulator", "split", "query", "cfp-split", "circuit-breaker-misc-gateway", "circuit-breaker-misc-direct",
        "circuit-breaker-read-all-read-many", "fi-multi-master", "fi-customer-workflows", "fi-sm-customer-workflows", "long-emulator", "fi-thinclient-multi-region", "fi-thinclient-multi-master", "multi-region-strong", "manual-http-network-fault", "consistency-overrides"}, timeOut = SUITE_SHUTDOWN_TIMEOUT)
    public void afterSuite() {

        logger.info("afterSuite Started");

        try (CosmosAsyncClient houseKeepingClient = createGatewayHouseKeepingDocumentClient(true).buildAsyncClient()) {
            safeDeleteDatabase(SHARED_DATABASE);
            CosmosDatabaseForTest.cleanupStaleTestDatabases(DatabaseManagerImpl.getInstance(houseKeepingClient));
        }
    }

    @AfterSuite(groups = { "emulator-vnext" }, timeOut = SUITE_SHUTDOWN_TIMEOUT)
    public void afterSuitEmulatorVNext() {
        // can not use the after suite method directly as for vnext, query databases is not implemented, so it will error out
        logger.info("afterSuite for emulator vnext group started. ");
        safeDeleteDatabase(SHARED_DATABASE);
    }

    protected static void cleanUpContainer(CosmosAsyncContainer cosmosContainer) {

        try {
            int i = 0;
            while (i < 100) {
                try {
                    cleanUpContainerInternal(cosmosContainer);
                    return;
                } catch (CosmosException exception) {
                    if (exception.getStatusCode() != HttpConstants.StatusCodes.TOO_MANY_REQUESTS
                        || exception.getSubStatusCode() != 3200) {

                        logger.error("No retry of exception", exception);
                        throw exception;
                    }

                    i++;
                    logger.info("Retrying truncation after 100ms - iteration " + i);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } finally {
            // TODO @fabianm - Resetting leak detection - there is some flakiness when tests
            // truncate collection and hit throttling - this will need to be investigated
            // separately
            CosmosNettyLeakDetectorFactory.resetIdentifiedLeaks();
        }
    }

    protected static void expectCount(CosmosAsyncContainer cosmosContainer, int expectedCount) {
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setCosmosEndToEndOperationLatencyPolicyConfig(
            new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofHours(1))
                .build()
        );
        options.setMaxDegreeOfParallelism(-1);
        List<Integer> counts = retryOnTransientCleanupFailure(cosmosContainer
            .queryItems("SELECT VALUE COUNT(0) FROM root", options, Integer.class)
            .byPage())
            .flatMap(page -> Flux.fromIterable(page.getResults()))
            .collectList()
            .block();
        assertThat(counts).hasSize(1);
        assertThat(counts.get(0)).isEqualTo(expectedCount);
    }

    private static void cleanUpContainerInternal(CosmosAsyncContainer cosmosContainer) {
        CosmosContainerProperties cosmosContainerProperties = retryOnTransientCleanupFailure(cosmosContainer.read())
            .block()
            .getProperties();
        String cosmosContainerId = cosmosContainerProperties.getId();
        logger.info("Truncating collection {} ...", cosmosContainerId);
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setCosmosEndToEndOperationLatencyPolicyConfig(
            new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofHours(1))
                .build()
        );
        options.setMaxDegreeOfParallelism(-1);
        int maxItemCount = 100;

        logger.info("Truncating collection {} documents ...", cosmosContainer.getId());

        Flux<CosmosItemOperation> deleteOperations =
            retryOnTransientCleanupFailure(cosmosContainer
                .queryItems("SELECT * FROM root", options, InternalObjectNode.class)
                .byPage(maxItemCount))
                           .publishOn(Schedulers.parallel())
                           .flatMap(page -> Flux.fromIterable(page.getResults()))
                           .map(doc -> {
                               PartitionKey partitionKey =
                                   PartitionKeyHelper.extractPartitionKeyFromDocument(
                                       doc,
                                       cosmosContainerProperties.getPartitionKeyDefinition());

                               if (partitionKey == null) {
                                   partitionKey = PartitionKey.NONE;
                               }

                               return CosmosBulkOperations.getDeleteItemOperation(doc.getId(), partitionKey);
                           });

        CosmosBulkExecutionOptions truncateBulkOptions = new CosmosBulkExecutionOptions();
        ImplementationBridgeHelpers.CosmosBulkExecutionOptionsHelper
            .getCosmosBulkExecutionOptionsAccessor()
            .getImpl(truncateBulkOptions)
            .setCosmosEndToEndLatencyPolicyConfig(
                new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(65))
                    .build());

        retryOnTransientCleanupFailure(cosmosContainer
            .executeBulkOperations(deleteOperations, truncateBulkOptions)
            .flatMap(response -> {
                if (response.getException() != null) {
                    Exception ex = response.getException();
                    if (ex instanceof CosmosException) {
                        CosmosException cosmosException = (CosmosException) ex;
                        if (cosmosException.getStatusCode() == HttpConstants.StatusCodes.NOTFOUND
                            && cosmosException.getSubStatusCode() == 0) {
                            return Mono.empty();
                        }
                    }
                    return Mono.error(ex);
                }
                if (response.getResponse() != null
                    && response.getResponse().getStatusCode() == HttpConstants.StatusCodes.NOTFOUND) {
                    return Mono.empty();
                }
                if (response.getResponse() != null
                    && !response.getResponse().isSuccessStatusCode()) {
                    CosmosException bulkException = BridgeInternal.createCosmosException(
                        response.getResponse().getStatusCode(),
                        "Bulk delete operation failed with status code " + response.getResponse().getStatusCode());
                    BridgeInternal.setSubStatusCode(bulkException, response.getResponse().getSubStatusCode());
                    return Mono.error(bulkException);
                }
                return Mono.just(response);
            }))
            .blockLast();

        expectCount(cosmosContainer, 0);

        logger.info("Truncating collection {} triggers ...", cosmosContainerId);

        retryOnTransientCleanupFailure(cosmosContainer.getScripts()
                   .queryTriggers("SELECT * FROM root", options)
                   .byPage(maxItemCount))
                       .publishOn(Schedulers.parallel())
                       .flatMap(page -> Flux.fromIterable(page.getResults()))
                       .flatMap(trigger -> retryOnTransientCleanupFailure(
                           cosmosContainer.getScripts().getTrigger(trigger.getId()).delete()))
                       .then().block();

        logger.info("Truncating collection {} storedProcedures ...", cosmosContainerId);

        retryOnTransientCleanupFailure(cosmosContainer.getScripts()
                   .queryStoredProcedures("SELECT * FROM root", options)
                   .byPage(maxItemCount))
                       .publishOn(Schedulers.parallel())
                       .flatMap(page -> Flux.fromIterable(page.getResults()))
                       .flatMap(storedProcedure -> retryOnTransientCleanupFailure(
                           cosmosContainer.getScripts().getStoredProcedure(storedProcedure.getId()).delete(new CosmosStoredProcedureRequestOptions())))
                       .then().block();

        logger.info("Truncating collection {} udfs ...", cosmosContainerId);

        retryOnTransientCleanupFailure(cosmosContainer.getScripts()
                   .queryUserDefinedFunctions("SELECT * FROM root", options)
                   .byPage(maxItemCount))
                       .publishOn(Schedulers.parallel())
                       .flatMap(page -> Flux.fromIterable(page.getResults()))
                       .flatMap(udf -> retryOnTransientCleanupFailure(
                           cosmosContainer.getScripts().getUserDefinedFunction(udf.getId()).delete()))
                       .then().block();

        logger.info("Finished truncating collection {}.", cosmosContainerId);
    }

    @SuppressWarnings({"fallthrough"})
    protected static void waitIfNeededForReplicasToCatchUp(CosmosClientBuilder clientBuilder) {
        switch (CosmosBridgeInternal.getConsistencyLevel(clientBuilder)) {
            case EVENTUAL:
            case CONSISTENT_PREFIX:
                logger.info(" additional wait in EVENTUAL mode so the replica catch up");
                // give times to replicas to catch up after a write
                try {
                    TimeUnit.MILLISECONDS.sleep(WAIT_REPLICA_CATCH_UP_IN_MILLIS);
                } catch (Exception e) {
                    logger.error("unexpected failure", e);
                }

            case SESSION:
            case BOUNDED_STALENESS:
            case STRONG:
            default:
                break;
        }
    }

    public static CosmosAsyncContainer createCollection(CosmosAsyncDatabase database, CosmosContainerProperties cosmosContainerProperties,
                                                        CosmosContainerRequestOptions options, int throughput) {
        return createCollection(database, cosmosContainerProperties, options, throughput, /* probeClient */ null);
    }

    /**
     * Overload of {@link #createCollection(CosmosAsyncDatabase, CosmosContainerProperties, CosmosContainerRequestOptions, int)}
     * that runs the post-creation collection-readiness probe using {@code probeClient} instead of the caller's
     * client. Tests that depend on the caller's collection cache remaining stale after a recreate (for example
     * {@code ContainerCreateDeleteWithSameNameTest}) pass a throwaway client here so the probe does not refresh
     * their main client's cache. When {@code probeClient} is null the caller's client is used.
     */
    public static CosmosAsyncContainer createCollection(CosmosAsyncDatabase database, CosmosContainerProperties cosmosContainerProperties,
                                                        CosmosContainerRequestOptions options, int throughput, CosmosAsyncClient probeClient) {
        Runnable ensureContainerExists = () -> createCollectionIfNotExists(
            database,
            cosmosContainerProperties,
            options,
            throughput);

        ensureContainerExists.run();

        // Creating a container is async. Even single-region, low-throughput containers can briefly fail reads
        // with 404/1013 ("Collection is not yet available for read") after create returns. If a concurrent
        // cleanup races with the test and deletes the container before it becomes readable, reissue create on
        // failed readiness attempts and treat 409 as success.
        waitForCollectionToBeAvailableToRead(
            database.getContainer(cosmosContainerProperties.getId()),
            probeClient,
            ensureContainerExists);
        getFeedRangesWithRetry(
            getContainerForReadinessProbe(database, cosmosContainerProperties.getId(), probeClient),
            "post-create feed range readiness for container " + cosmosContainerProperties.getId());

        return database.getContainer(cosmosContainerProperties.getId());
    }

    private static CosmosAsyncContainer getContainerForReadinessProbe(
        CosmosAsyncDatabase database,
        String containerId,
        CosmosAsyncClient probeClient) {

        if (probeClient != null) {
            return probeClient.getDatabase(database.getId()).getContainer(containerId);
        }

        return database.getContainer(containerId);
    }

    private static void createCollectionIfNotExists(
        CosmosAsyncDatabase database,
        CosmosContainerProperties cosmosContainerProperties,
        CosmosContainerRequestOptions options,
        int throughput) {

        database.createContainer(cosmosContainerProperties, ThroughputProperties.createManualThroughput(throughput), options)
            .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(5))
                .filter(TestSuiteBase::isTransientCreateFailure))
            .onErrorResume(e -> isConflictException(e), e -> {
                logger.info("Container {} already exists (409 Conflict), treating as success", cosmosContainerProperties.getId());
                return Mono.empty();
            })
            .block();
    }

    protected static void waitForCollectionToBeAvailableToRead(CosmosAsyncContainer container, CosmosAsyncClient probeClient) {
        waitForCollectionToBeAvailableToRead(container, probeClient, null);
    }

    /**
     * Issues a single collection-readiness probe through the caller's default route and returns once it succeeds,
     * retrying transient / not-yet-ready failures via {@link #isRetryableCollectionReadinessFailure(Throwable)}.
     *
     * <p>Unlike {@link #waitForCollectionToBeAvailableToRead(CosmosAsyncContainer, CosmosAsyncClient)}, this does NOT
     * additionally probe every preferred/readable region - it performs only the one default-route probe and does not
     * pin the probe to a specific region (no excluded-regions filter is applied). A single query is routed by the
     * client to one region; because the caller's own operations use the same client and route, that probe is enough
     * to confirm the routing map is resolvable where the test will operate. Skipping the per-region probes keeps the
     * warm-up cheap for tests that repeatedly create/recreate a dedicated collection, which would otherwise trigger a
     * metadata-request storm (429/3200).
     */
    protected static void waitForCollectionToBeReadableOnDefaultRoute(CosmosAsyncContainer container, CosmosAsyncClient probeClient) {
        CosmosAsyncClient client = probeClient != null
            ? probeClient
            : ImplementationBridgeHelpers
                .CosmosAsyncDatabaseHelper
                .getCosmosAsyncDatabaseAccessor()
                .getCosmosAsyncClient(container.getDatabase());
        CosmosAsyncContainer probeContainer =
            client.getDatabase(container.getDatabase().getId()).getContainer(container.getId());
        Duration maxWait = COLLECTION_READINESS_MAX_WAIT;
        long deadlineNanos = System.nanoTime() + maxWait.toNanos();
        awaitContainerReadableInRegion(
            probeContainer,
            null,
            Collections.emptyList(),
            deadlineNanos,
            maxWait,
            null);
    }

    private static void waitForCollectionToBeAvailableToRead(
        CosmosAsyncContainer container,
        CosmosAsyncClient probeClient,
        Runnable ensureContainerExistsOnReadFailure) {

        // Creating a container is asynchronous - especially on multi-region accounts the new collection can
        // take time to become readable in a routed region. Until then, metadata reads can fail with 404/1013
        // ("Collection is not yet available for read"). Instead of a fixed sleep, verify that the collection is
        // readable through the caller's normal route and, when configured, through the caller's preferred regions.
        // The probe is issued through probeClient when provided (so a throwaway client does not warm the caller's
        // caches); otherwise the container's own client is used.
        CosmosAsyncClient client = probeClient != null
            ? probeClient
            : ImplementationBridgeHelpers
                .CosmosAsyncDatabaseHelper
                .getCosmosAsyncDatabaseAccessor()
                .getCosmosAsyncClient(container.getDatabase());
        DatabaseAccount databaseAccount = getLatestDatabaseAccount(client);
        CosmosAsyncContainer probeContainer =
            client.getDatabase(container.getDatabase().getId()).getContainer(container.getId());

        List<String> allRegions = new ArrayList<>();
        for (DatabaseAccountLocation location : databaseAccount.getReadableLocations()) {
            allRegions.add(location.getName());
        }

        List<String> excludedRegions = getExcludedRegions(client);

        Duration maxWait = COLLECTION_READINESS_MAX_WAIT;
        long deadlineNanos = System.nanoTime() + maxWait.toNanos();

        awaitContainerReadableInRegion(
            probeContainer,
            null,
            Collections.emptyList(),
            deadlineNanos,
            maxWait,
            ensureContainerExistsOnReadFailure);

        List<String> probeRegions = ImplementationBridgeHelpers
            .CosmosAsyncClientHelper
            .getCosmosAsyncClientAccessor()
            .getPreferredRegions(client);
        if (probeRegions == null || probeRegions.isEmpty()) {
            probeRegions = allRegions;
        }

        if (probeRegions == null || probeRegions.isEmpty() || allRegions.isEmpty()) {
            return;
        }

        for (String preferredRegion : probeRegions) {
            boolean readableRegion = allRegions
                .stream()
                .anyMatch(region -> region.equalsIgnoreCase(preferredRegion));

            if (!readableRegion || containsRegion(excludedRegions, preferredRegion)) {
                continue;
            }

            final String target = preferredRegion;
            List<String> perProbeExcludedRegions = allRegions
                .stream()
                .filter(other -> !other.equalsIgnoreCase(target))
                .collect(Collectors.toList());
            awaitContainerReadableInRegion(
                probeContainer,
                preferredRegion,
                perProbeExcludedRegions,
                deadlineNanos,
                maxWait,
                ensureContainerExistsOnReadFailure);
        }
    }

    private static List<String> getExcludedRegions(CosmosAsyncClient client) {
        return ImplementationBridgeHelpers
            .CosmosAsyncClientHelper
            .getCosmosAsyncClientAccessor()
            .getExcludedRegions(client);
    }

    private static boolean containsRegion(List<String> regions, String regionToFind) {
        return regions
            .stream()
            .anyMatch(region -> region.equalsIgnoreCase(regionToFind));
    }

    private static void awaitContainerReadableInRegion(
        CosmosAsyncContainer container,
        String targetRegion,
        List<String> excludedRegions,
        long deadlineNanos,
        Duration maxWait,
        Runnable ensureContainerExistsOnReadFailure) {

        long backoffMillis = 100;
        long maxBackoffMillis = 5000;
        int attempts = 0;
        int createRetryAttempts = 0;
        Throwable lastError = null;

        logger.info(
            "Waiting for container '{}' to become readable{} with excludedRegions={} for up to {} seconds.",
            container.getId(),
            targetRegion != null ? " in region '" + targetRegion + "'" : "",
            excludedRegions,
            maxWait.getSeconds());

        while (true) {
            long remainingNanos = deadlineNanos - System.nanoTime();
            if (remainingNanos <= 0) {
                break;
            }

            attempts++;
            try {
                Duration attemptTimeout = Duration.ofMillis(
                    Math.max(
                        1,
                        Math.min(
                            COLLECTION_READINESS_PROBE_TIMEOUT.toMillis(),
                            TimeUnit.NANOSECONDS.toMillis(remainingNanos))));
                CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
                options.setCosmosEndToEndOperationLatencyPolicyConfig(
                    new CosmosEndToEndOperationLatencyPolicyConfigBuilder(attemptTimeout)
                        .build());
                if (!excludedRegions.isEmpty()) {
                    options.setExcludedRegions(excludedRegions);
                }
                // A successful (possibly empty) page proves the collection is resolvable/readable in the
                // targeted region.
                container.queryItems("SELECT TOP 1 c.id FROM c", options, Object.class)
                    .byPage(1)
                    .blockFirst(attemptTimeout);

                logger.info(
                    "Container '{}' became readable{} after {} attempt(s).",
                    container.getId(),
                    targetRegion != null ? " in region '" + targetRegion + "'" : "",
                    attempts);
                return;
            } catch (Exception error) {
                lastError = error;
                if (!isRetryableCollectionReadinessFailure(lastError)) {
                    throw new AssertionError(
                        String.format(
                            "Container '%s' failed with a non-retryable error while waiting for readability%s after %d attempt(s). Excluded regions: %s. Error: %s",
                            container.getId(),
                            targetRegion != null ? " in region '" + targetRegion + "'" : "",
                            attempts,
                            excludedRegions,
                            getCollectionReadinessErrorDetails(lastError)),
                        lastError);
                }

                if (ensureContainerExistsOnReadFailure != null) {
                    createRetryAttempts++;
                    try {
                        ensureContainerExistsOnReadFailure.run();
                    } catch (RuntimeException recreateError) {
                        logger.warn(
                            "Failed to reissue create for container '{}' while waiting for readability.",
                            container.getId(),
                            recreateError);
                        lastError = recreateError;
                    }
                }
            }

            remainingNanos = deadlineNanos - System.nanoTime();
            if (remainingNanos <= 0) {
                break;
            }

            long sleepMillis = Math.max(1, Math.min(backoffMillis, TimeUnit.NANOSECONDS.toMillis(remainingNanos)));
            try {
                TimeUnit.MILLISECONDS.sleep(sleepMillis);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for collection to be available to read.", interrupted);
            }
            backoffMillis = Math.min(backoffMillis * 2, maxBackoffMillis);
        }

        throw new AssertionError(
            String.format(
                "Container '%s' was not available to read%s within %d seconds (%d attempts, %d create retries). Excluded regions: %s.",
                container.getId(),
                targetRegion != null ? " in region '" + targetRegion + "'" : "",
                maxWait.getSeconds(),
                attempts,
                createRetryAttempts,
                excludedRegions),
            lastError);
    }

    private static boolean isRetryableCollectionReadinessFailure(Throwable error) {
        CosmosException cosmosException = getCosmosException(error);
        if (cosmosException != null) {
            int statusCode = cosmosException.getStatusCode();
            return statusCode == HttpConstants.StatusCodes.REQUEST_TIMEOUT
                || statusCode == HttpConstants.StatusCodes.UNAUTHORIZED
                || statusCode == HttpConstants.StatusCodes.TOO_MANY_REQUESTS
                || statusCode == HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR
                || statusCode == HttpConstants.StatusCodes.SERVICE_UNAVAILABLE
                || statusCode == HttpConstants.StatusCodes.GONE
                || isStaleCollectionRidFailure(cosmosException)
                || (statusCode == HttpConstants.StatusCodes.NOTFOUND
                    && (cosmosException.getSubStatusCode() == HttpConstants.SubStatusCodes.UNKNOWN
                        || cosmosException.getSubStatusCode() == 1013
                        || cosmosException.getSubStatusCode() == HttpConstants.SubStatusCodes.INCORRECT_CONTAINER_RID_SUB_STATUS));
        }

        Throwable unwrappedException = Exceptions.unwrap(error);
        if (unwrappedException instanceof IllegalStateException) {
            String message = unwrappedException.getMessage();
            return message != null && message.contains("Timeout on blocking read");
        }

        return false;
    }

    private static boolean isStaleCollectionRidFailure(CosmosException cosmosException) {
        if (cosmosException.getStatusCode() != HttpConstants.StatusCodes.BADREQUEST
            || cosmosException.getSubStatusCode() != HttpConstants.SubStatusCodes.INCORRECT_CONTAINER_RID_SUB_STATUS) {

            return false;
        }

        String message = cosmosException.getMessage();
        return message != null
            && message.contains("Collection rid provided by the user does not match the existing collection.");
    }

    private static String getCollectionReadinessErrorDetails(Throwable error) {
        CosmosException cosmosException = getCosmosException(error);
        if (cosmosException != null) {
            return String.format(
                "statusCode=%d subStatusCode=%d message=%s",
                cosmosException.getStatusCode(),
                cosmosException.getSubStatusCode(),
                cosmosException.getMessage());
        }

        Throwable unwrappedException = Exceptions.unwrap(error);
        if (unwrappedException == null) {
            return "unknown failure";
        }

        return unwrappedException.getClass().getSimpleName() + ": " + unwrappedException.getMessage();
    }

    private static DatabaseAccount getLatestDatabaseAccount(CosmosAsyncClient client) {
        AsyncDocumentClient asyncDocumentClient = BridgeInternal.getContextClient(client);
        GlobalEndpointManager globalEndpointManager =
            ReflectionUtils.getGlobalEndpointManager((RxDocumentClientImpl) asyncDocumentClient);

        // The latest database account is populated during client initialization; poll briefly to defend against
        // an initialization race.
        DatabaseAccount databaseAccount = globalEndpointManager.getLatestDatabaseAccount();
        long deadlineNanos = System.nanoTime() + Duration.ofSeconds(10).toNanos();
        while (databaseAccount == null && System.nanoTime() < deadlineNanos) {
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while resolving the database account.", interrupted);
            }
            databaseAccount = globalEndpointManager.getLatestDatabaseAccount();
        }

        if (databaseAccount == null) {
            throw new AssertionError("Database account was not available to determine the account's regions.");
        }

        return databaseAccount;
    }

    public static CosmosAsyncContainer createCollection(CosmosAsyncDatabase database, CosmosContainerProperties cosmosContainerProperties,
                                                        CosmosContainerRequestOptions options) {
        return createCollection(database, cosmosContainerProperties, options, /* probeClient */ null);
    }

    public static CosmosAsyncContainer createCollection(CosmosAsyncDatabase database, CosmosContainerProperties cosmosContainerProperties,
                                                        CosmosContainerRequestOptions options, CosmosAsyncClient probeClient) {
        database.createContainer(cosmosContainerProperties, options)
            .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(5))
                .filter(TestSuiteBase::isTransientCreateFailure))
            .onErrorResume(e -> isConflictException(e), e -> {
                logger.info("Container {} already exists (409 Conflict), treating as success", cosmosContainerProperties.getId());
                return Mono.empty();
            })
            .block();
        waitForCollectionToBeAvailableToRead(database.getContainer(cosmosContainerProperties.getId()), probeClient);
        getFeedRangesWithRetry(
            getContainerForReadinessProbe(database, cosmosContainerProperties.getId(), probeClient),
            "post-create feed range readiness for container " + cosmosContainerProperties.getId());
        return database.getContainer(cosmosContainerProperties.getId());
    }

    private static CosmosContainerProperties getCollectionDefinitionMultiPartitionWithCompositeAndSpatialIndexes() {
        final String NUMBER_FIELD = "numberField";
        final String STRING_FIELD = "stringField";
        final String NUMBER_FIELD_2 = "numberField2";
        final String STRING_FIELD_2 = "stringField2";
        final String BOOL_FIELD = "boolField";
        final String NULL_FIELD = "nullField";
        final String OBJECT_FIELD = "objectField";
        final String ARRAY_FIELD = "arrayField";
        final String SHORT_STRING_FIELD = "shortStringField";
        final String MEDIUM_STRING_FIELD = "mediumStringField";
        final String LONG_STRING_FIELD = "longStringField";
        final String PARTITION_KEY = "pk";

        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
        ArrayList<String> partitionKeyPaths = new ArrayList<String>();
        partitionKeyPaths.add("/" + PARTITION_KEY);
        partitionKeyDefinition.setPaths(partitionKeyPaths);

        CosmosContainerProperties cosmosContainerProperties = new CosmosContainerProperties(UUID.randomUUID().toString(), partitionKeyDefinition);

        IndexingPolicy indexingPolicy = new IndexingPolicy();
        List<List<CompositePath>> compositeIndexes = new ArrayList<>();

        //Simple
        ArrayList<CompositePath> compositeIndexSimple = new ArrayList<CompositePath>();
        CompositePath compositePath1 = new CompositePath();
        compositePath1.setPath("/" + NUMBER_FIELD);
        compositePath1.setOrder(CompositePathSortOrder.ASCENDING);

        CompositePath compositePath2 = new CompositePath();
        compositePath2.setPath("/" + STRING_FIELD);
        compositePath2.setOrder(CompositePathSortOrder.DESCENDING);

        compositeIndexSimple.add(compositePath1);
        compositeIndexSimple.add(compositePath2);

        //Max Columns
        ArrayList<CompositePath> compositeIndexMaxColumns = new ArrayList<CompositePath>();
        CompositePath compositePath3 = new CompositePath();
        compositePath3.setPath("/" + NUMBER_FIELD);
        compositePath3.setOrder(CompositePathSortOrder.DESCENDING);

        CompositePath compositePath4 = new CompositePath();
        compositePath4.setPath("/" + STRING_FIELD);
        compositePath4.setOrder(CompositePathSortOrder.ASCENDING);

        CompositePath compositePath5 = new CompositePath();
        compositePath5.setPath("/" + NUMBER_FIELD_2);
        compositePath5.setOrder(CompositePathSortOrder.DESCENDING);

        CompositePath compositePath6 = new CompositePath();
        compositePath6.setPath("/" + STRING_FIELD_2);
        compositePath6.setOrder(CompositePathSortOrder.ASCENDING);

        compositeIndexMaxColumns.add(compositePath3);
        compositeIndexMaxColumns.add(compositePath4);
        compositeIndexMaxColumns.add(compositePath5);
        compositeIndexMaxColumns.add(compositePath6);

        //Primitive Values
        ArrayList<CompositePath> compositeIndexPrimitiveValues = new ArrayList<CompositePath>();
        CompositePath compositePath7 = new CompositePath();
        compositePath7.setPath("/" + NUMBER_FIELD);
        compositePath7.setOrder(CompositePathSortOrder.DESCENDING);

        CompositePath compositePath8 = new CompositePath();
        compositePath8.setPath("/" + STRING_FIELD);
        compositePath8.setOrder(CompositePathSortOrder.ASCENDING);

        CompositePath compositePath9 = new CompositePath();
        compositePath9.setPath("/" + BOOL_FIELD);
        compositePath9.setOrder(CompositePathSortOrder.DESCENDING);

        CompositePath compositePath10 = new CompositePath();
        compositePath10.setPath("/" + NULL_FIELD);
        compositePath10.setOrder(CompositePathSortOrder.ASCENDING);

        compositeIndexPrimitiveValues.add(compositePath7);
        compositeIndexPrimitiveValues.add(compositePath8);
        compositeIndexPrimitiveValues.add(compositePath9);
        compositeIndexPrimitiveValues.add(compositePath10);

        //Long Strings
        ArrayList<CompositePath> compositeIndexLongStrings = new ArrayList<CompositePath>();
        CompositePath compositePath11 = new CompositePath();
        compositePath11.setPath("/" + STRING_FIELD);

        CompositePath compositePath12 = new CompositePath();
        compositePath12.setPath("/" + SHORT_STRING_FIELD);

        CompositePath compositePath13 = new CompositePath();
        compositePath13.setPath("/" + MEDIUM_STRING_FIELD);

        CompositePath compositePath14 = new CompositePath();
        compositePath14.setPath("/" + LONG_STRING_FIELD);

        compositeIndexLongStrings.add(compositePath11);
        compositeIndexLongStrings.add(compositePath12);
        compositeIndexLongStrings.add(compositePath13);
        compositeIndexLongStrings.add(compositePath14);

        compositeIndexes.add(compositeIndexSimple);
        compositeIndexes.add(compositeIndexMaxColumns);
        compositeIndexes.add(compositeIndexPrimitiveValues);
        compositeIndexes.add(compositeIndexLongStrings);

        indexingPolicy.setCompositeIndexes(compositeIndexes);
        cosmosContainerProperties.setIndexingPolicy(indexingPolicy);

        return cosmosContainerProperties;
    }

    public static CosmosAsyncContainer createCollection(CosmosAsyncClient client, String dbId, CosmosContainerProperties collectionDefinition) {
        CosmosAsyncDatabase database = client.getDatabase(dbId);
        return createCollection(database, collectionDefinition, new CosmosContainerRequestOptions());
    }

    public static void deleteCollection(CosmosAsyncClient client, String dbId, String collectionId) {
        client.getDatabase(dbId).getContainer(collectionId).delete().block();
    }

    public static InternalObjectNode createDocument(CosmosAsyncContainer cosmosContainer, InternalObjectNode item) {
        CosmosItemRequestOptions options = new CosmosItemRequestOptions()
            .setCosmosEndToEndOperationLatencyPolicyConfig(
                new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofHours(1))
                    .build()
            );

        return BridgeInternal.getProperties(cosmosContainer.createItem(item, options).block());
    }

    public <T> Flux<CosmosBulkOperationResponse<Object>> bulkInsert(CosmosAsyncContainer cosmosContainer,
                                                                     List<T> documentDefinitionList) {

        CosmosContainerProperties cosmosContainerProperties = cosmosContainer.read().block().getProperties();
        PartitionKeyDefinition pkDef = cosmosContainerProperties.getPartitionKeyDefinition();

        List<CosmosItemOperation> operations = new ArrayList<>(documentDefinitionList.size());
        for (T docDef : documentDefinitionList) {
            InternalObjectNode internalNode = InternalObjectNode.fromObjectToInternalObjectNode(docDef);
            PartitionKey partitionKey = PartitionKeyHelper.extractPartitionKeyFromDocument(internalNode, pkDef);
            if (partitionKey == null) {
                partitionKey = PartitionKey.NONE;
            }
            operations.add(CosmosBulkOperations.getCreateItemOperation(docDef, partitionKey));
        }

        CosmosBulkExecutionOptions bulkOptions = new CosmosBulkExecutionOptions();
        ImplementationBridgeHelpers.CosmosBulkExecutionOptionsHelper
            .getCosmosBulkExecutionOptionsAccessor()
            .getImpl(bulkOptions)
            .setCosmosEndToEndLatencyPolicyConfig(
                new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(65))
                    .build());

        return cosmosContainer
            .executeBulkOperations(Flux.fromIterable(operations), bulkOptions)
            .flatMap(response -> {
                if (response.getException() != null) {
                    Exception ex = response.getException();
                    if (ex instanceof CosmosException) {
                        CosmosException cosmosException = (CosmosException) ex;
                        if (cosmosException.getStatusCode() == HttpConstants.StatusCodes.CONFLICT
                            && cosmosException.getSubStatusCode() == 0) {
                            return Mono.empty();
                        }
                    }
                    return Mono.error(ex);
                }
                if (response.getResponse() != null
                    && !response.getResponse().isSuccessStatusCode()
                    && response.getResponse().getStatusCode() != HttpConstants.StatusCodes.CONFLICT) {
                    CosmosException bulkException = BridgeInternal.createCosmosException(
                        response.getResponse().getStatusCode(),
                        "Bulk insert operation failed with status code " + response.getResponse().getStatusCode());
                    BridgeInternal.setSubStatusCode(bulkException, response.getResponse().getSubStatusCode());
                    return Mono.error(bulkException);
                }
                return Mono.just(response);
            });
    }

    private <T> Flux<CosmosItemResponse<T>> insertUsingPointOperations(CosmosAsyncContainer cosmosContainer,
                                                                       List<T> documentDefinitionList) {
        CosmosItemRequestOptions options = new CosmosItemRequestOptions()
            .setCosmosEndToEndOperationLatencyPolicyConfig(
                new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofHours(1))
                    .build()
            );

        List<Mono<CosmosItemResponse<T>>> result = new ArrayList<>(documentDefinitionList.size());
        for (T docDef : documentDefinitionList) {
            result.add(cosmosContainer.createItem(docDef, options));
        }

        return Flux.merge(Flux.fromIterable(result), DEFAULT_BULK_INSERT_CONCURRENCY_LEVEL);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> insertAllItemsBlocking(CosmosAsyncContainer cosmosContainer,
                                                         List<T> documentDefinitionList,
                                                         boolean bulkEnabled) {
        if (documentDefinitionList == null || documentDefinitionList.isEmpty()) {
            return documentDefinitionList;
        }

        if (!bulkEnabled) {
            return insertUsingPointOperations(cosmosContainer, documentDefinitionList)
                .publishOn(Schedulers.parallel())
                .map(CosmosItemResponse::getItem)
                .collectList()
                .block();
        }

        Class<T> clazz = (Class<T>) documentDefinitionList.get(0).getClass();

        return bulkInsert(cosmosContainer, documentDefinitionList)
            .publishOn(Schedulers.parallel())
            .filter(response -> response.getResponse() != null)
            .map(response -> response.getResponse().getItem(clazz))
            .collectList()
            .block();
    }

    public <T> void voidInsertAllItemsBlocking(CosmosAsyncContainer cosmosContainer,
                                        List<T> documentDefinitionList,
                                        boolean bulkEnabled) {
        if (!bulkEnabled) {
            insertUsingPointOperations(cosmosContainer, documentDefinitionList)
                .publishOn(Schedulers.parallel())
                .then()
                .block();
            return;
        }

        bulkInsert(cosmosContainer, documentDefinitionList)
            .publishOn(Schedulers.parallel())
            .then()
            .block();
    }

    public static CosmosAsyncUser createUser(CosmosAsyncClient client, String databaseId, CosmosUserProperties userSettings) {
        CosmosAsyncDatabase database = client.getDatabase(databaseId);
        CosmosUserResponse userResponse = database.createUser(userSettings).block();
        return database.getUser(userResponse.getProperties().getId());
    }

    public static CosmosAsyncUser safeCreateUser(CosmosAsyncClient client, String databaseId, CosmosUserProperties user) {
        deleteUserIfExists(client, databaseId, user.getId());
        return createUser(client, databaseId, user);
    }

    private static CosmosAsyncContainer safeCreateCollection(CosmosAsyncClient client, String databaseId, CosmosContainerProperties collection, CosmosContainerRequestOptions options) {
        deleteCollectionIfExists(client, databaseId, collection.getId());
        return createCollection(client.getDatabase(databaseId), collection, options);
    }

    static protected CosmosContainerProperties getCollectionDefinitionWithFullFidelity() {
        CosmosContainerProperties cosmosContainerProperties = getCollectionDefinition(UUID.randomUUID().toString());
        cosmosContainerProperties.setChangeFeedPolicy(ChangeFeedPolicy.createAllVersionsAndDeletesPolicy(Duration.ofMinutes(5)));
        return cosmosContainerProperties;
    }

    static protected CosmosContainerProperties getCollectionDefinition() {
        return getCollectionDefinition(UUID.randomUUID().toString());
    }

    static protected CosmosContainerProperties getCollectionDefinition(String collectionId) {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);

        CosmosContainerProperties collectionDefinition = new CosmosContainerProperties(collectionId, partitionKeyDef);

        return collectionDefinition;
    }

    static protected CosmosContainerProperties getCollectionDefinition(String collectionId, PartitionKeyDefinition partitionKeyDefinition) {
        return new CosmosContainerProperties(collectionId, partitionKeyDefinition);
    }

    static protected CosmosContainerProperties getCollectionDefinitionForHashV2(String collectionId) {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);
        partitionKeyDef.setVersion(PartitionKeyDefinitionVersion.V2);

        CosmosContainerProperties collectionDefinition = new CosmosContainerProperties(collectionId, partitionKeyDef);

        return collectionDefinition;
    }

    static protected CosmosContainerProperties getCollectionDefinitionForHashV2WithHpk(String collectionId) {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<>();
        paths.add("/state");
        paths.add("/city");
        paths.add("/zipcode");
        partitionKeyDef.setPaths(paths);
        partitionKeyDef.setVersion(PartitionKeyDefinitionVersion.V2);
        partitionKeyDef.setKind(PartitionKind.MULTI_HASH);

        return new CosmosContainerProperties(collectionId, partitionKeyDef);
    }


    static protected CosmosContainerProperties getCollectionDefinitionWithHpk(String collectionId) {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<>();
        paths.add("/state");
        paths.add("/city");
        paths.add("/zipcode");
        partitionKeyDef.setPaths(paths);
        partitionKeyDef.setKind(PartitionKind.MULTI_HASH);

        return new CosmosContainerProperties(collectionId, partitionKeyDef);
    }

    static protected CosmosContainerProperties getCollectionDefinitionWithRangeRangeIndexWithIdAsPartitionKey() {
        return getCollectionDefinitionWithRangeRangeIndex(Collections.singletonList("/id"));
    }

    static protected CosmosContainerProperties getCollectionDefinitionWithRangeRangeIndex() {
        return getCollectionDefinitionWithRangeRangeIndex(Collections.singletonList("/mypk"));
    }

    static protected CosmosContainerProperties getCollectionDefinitionWithRangeRangeIndex(List<String> partitionKeyPath) {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();

        partitionKeyDef.setPaths(partitionKeyPath);
        IndexingPolicy indexingPolicy = new IndexingPolicy();
        List<IncludedPath> includedPaths = new ArrayList<>();
        IncludedPath includedPath = new IncludedPath("/*");
        includedPaths.add(includedPath);
        indexingPolicy.setIncludedPaths(includedPaths);

        CosmosContainerProperties cosmosContainerProperties = new CosmosContainerProperties(UUID.randomUUID().toString(), partitionKeyDef);
        cosmosContainerProperties.setIndexingPolicy(indexingPolicy);

        return cosmosContainerProperties;
    }

    public static void deleteCollectionIfExists(CosmosAsyncClient client, String databaseId, String collectionId) {
        CosmosAsyncDatabase database = client.getDatabase(databaseId);
        database.read().block();
        List<CosmosContainerProperties> res = database.queryContainers(String.format("SELECT * FROM root r where r.id = '%s'", collectionId), null)
            .collectList()
            .block();

        if (!res.isEmpty()) {
            deleteCollection(database, collectionId);
        }
    }

    public static void deleteCollection(CosmosAsyncDatabase cosmosDatabase, String collectionId) {
        cosmosDatabase.getContainer(collectionId).delete().block();
    }

    public static void deleteCollection(CosmosAsyncContainer cosmosContainer) {
        cosmosContainer.delete().block();
    }

    public static void deleteDocumentIfExists(CosmosAsyncClient client, String databaseId, String collectionId, String docId) {
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setPartitionKey(new PartitionKey(docId));
        CosmosAsyncContainer cosmosContainer = client.getDatabase(databaseId).getContainer(collectionId);

        List<InternalObjectNode> res = cosmosContainer
            .queryItems(String.format("SELECT * FROM root r where r.id = '%s'", docId), options, InternalObjectNode.class)
            .byPage()
            .flatMap(page -> Flux.fromIterable(page.getResults()))
            .collectList().block();

        if (!res.isEmpty()) {
            deleteDocument(cosmosContainer, docId);
        }
    }

    public static void safeDeleteDocument(CosmosAsyncContainer cosmosContainer, String documentId, Object partitionKey) {
        if (cosmosContainer != null && documentId != null) {
            try {
                cosmosContainer.deleteItem(documentId, new PartitionKey(partitionKey), DEFAULT_DELETE_ITEM_OPTIONS).block();
            } catch (Exception e) {
                CosmosException dce = Utils.as(e, CosmosException.class);
                if (dce == null || dce.getStatusCode() != 404) {
                    throw e;
                }
            }
        }
    }

    public static void deleteDocument(CosmosAsyncContainer cosmosContainer, String documentId) {
        cosmosContainer.deleteItem(documentId, PartitionKey.NONE, DEFAULT_DELETE_ITEM_OPTIONS).block();
    }

    public static void deleteUserIfExists(CosmosAsyncClient client, String databaseId, String userId) {
        CosmosAsyncDatabase database = client.getDatabase(databaseId);
        client.getDatabase(databaseId).read().block();
        List<CosmosUserProperties> res = database
            .queryUsers(String.format("SELECT * FROM root r where r.id = '%s'", userId), null)
            .collectList().block();
        if (!res.isEmpty()) {
            deleteUser(database, userId);
        }
    }

    public static void deleteUser(CosmosAsyncDatabase database, String userId) {
        database.getUser(userId).delete().block();
    }

    static private CosmosAsyncDatabase safeCreateDatabase(CosmosAsyncClient client, CosmosDatabaseProperties databaseSettings) {
        safeDeleteDatabase(client.getDatabase(databaseSettings.getId()));
        client.createDatabase(databaseSettings)
            .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(5))
                .filter(TestSuiteBase::isTransientCreateFailure))
            .onErrorResume(e -> isConflictException(e) ? Mono.empty() : Mono.error(e))
            .block();
        return client.getDatabase(databaseSettings.getId());
    }

    static protected CosmosAsyncDatabase createDatabase(CosmosAsyncClient client, String databaseId) {
        CosmosDatabaseProperties databaseSettings = new CosmosDatabaseProperties(databaseId);
        client.createDatabase(databaseSettings)
            .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(5))
                .filter(TestSuiteBase::isTransientCreateFailure))
            .onErrorResume(e -> isConflictException(e) ? Mono.empty() : Mono.error(e))
            .block();
        return client.getDatabase(databaseSettings.getId());
    }

    static protected CosmosDatabase createSyncDatabase(CosmosClient client, String databaseId) {
        CosmosDatabaseProperties databaseSettings = new CosmosDatabaseProperties(databaseId);
        try {
            client.createDatabase(databaseSettings);
            return client.getDatabase(databaseSettings.getId());
        } catch (CosmosException e) {
            e.printStackTrace();
        }
        return null;
    }

    static protected CosmosAsyncDatabase createDatabaseIfNotExists(CosmosAsyncClient client, String databaseId) {
        List<CosmosDatabaseProperties> res = client.queryDatabases(String.format("SELECT * FROM r where r.id = '%s'", databaseId), null)
            .collectList()
            .block();
        if (res.size() != 0) {
            CosmosAsyncDatabase database = client.getDatabase(databaseId);
            database.read().block();
            return database;
        } else {
            CosmosDatabaseProperties databaseSettings = new CosmosDatabaseProperties(databaseId);
            client.createDatabase(databaseSettings)
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(5))
                    .filter(TestSuiteBase::isTransientCreateFailure))
                .block();
            return client.getDatabase(databaseSettings.getId());
        }
    }

    static protected void safeDeleteDatabase(CosmosAsyncDatabase database) {
        if (database != null) {
            try {
                database.delete().block();
            } catch (Exception e) {
            }
        }
    }

    static protected void safeDeleteSyncDatabase(CosmosDatabase database) {
        if (database != null) {
            try {
                logger.info("attempting to delete database ....");
                database.delete();
                logger.info("database deletion completed");
            } catch (Exception e) {
                logger.error("failed to delete sync database", e);
            }
        }
    }

    static protected void safeDeleteAllCollections(CosmosAsyncDatabase database) {
        if (database != null) {
            try {
                List<CosmosContainerProperties> collections = database.readAllContainers()
                    .collectList()
                    .block();

                for (CosmosContainerProperties collection : collections) {
                    safeDeleteCollection(database.getContainer(collection.getId()));
                }
            } catch (Exception e) {
                logger.error("failed to delete all collections", e);
            }
        }
    }

    static protected void safeDeleteCollection(CosmosAsyncContainer collection) {

        if (collection != null) {
            try {
                logger.info("attempting to delete container {}.{}....",
                    collection.getDatabase().getId(),
                    collection.getId());
                collection.delete().block();
                logger.info("Container {}.{} deletion completed",
                    collection.getDatabase().getId(),
                    collection.getId());
            } catch (Exception e) {
                boolean shouldLogAsError = true;
                if (e  instanceof CosmosException) {
                    CosmosException cosmosException = (CosmosException) e;
                    if (cosmosException.getStatusCode() == 404) {
                        shouldLogAsError = false;
                        logger.info(
                            "Container {}.{} does not exist anymore.",
                            collection.getDatabase().getId(),
                            collection.getId());
                    }
                }

                if (shouldLogAsError) {
                    logger.error("failed to delete sync container {}.{}",
                        collection.getDatabase().getId(),
                        collection.getId(),
                        e);
                }
            }
            finally {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    static protected void safeDeleteCollection(CosmosAsyncDatabase database, String collectionId) {
        if (database != null && collectionId != null) {
            try {
                safeDeleteCollection(database.getContainer(collectionId));
            } catch (Exception e) {
            }
        }
    }

    static protected void safeClose(QueryFeedOperationState state) {
        if (state != null) {
            safeClose(state.getClient());
        }
    }

    static protected void safeClose(CosmosAsyncClient client) {
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                logger.error("failed to close client", e);
            }
        }
    }

    static protected void safeCloseSyncClient(CosmosClient client) {
        if (client != null) {
            try {
                logger.info("closing client ...");
                client.close();
                logger.info("closing client completed");
            } catch (Exception e) {
                logger.error("failed to close client", e);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    public <T extends CosmosResponse> void validateSuccess(Mono<T> single, CosmosResponseValidator<T> validator) {
        validateSuccess(single, validator, subscriberValidationTimeout);
    }

    @SuppressWarnings("rawtypes")
    public <T extends CosmosResponse> void validateSuccess(Mono<T> single, CosmosResponseValidator<T> validator, long timeout) {
        validateSuccess(single.flux(), validator, timeout);
    }

    @SuppressWarnings("rawtypes")
    public static <T extends CosmosResponse> void validateSuccess(Flux<T> flowable,
                                                                  CosmosResponseValidator<T> validator, long timeout) {
        StepVerifier.create(flowable)
            .assertNext(validator::validate)
            .expectComplete()
            .verify(Duration.ofMillis(timeout));
    }

    @SuppressWarnings("rawtypes")
    public <T, U extends CosmosResponse> void validateFailure(Mono<U> mono, FailureValidator validator)
        throws InterruptedException {
        validateFailure(mono.flux(), validator, subscriberValidationTimeout);
    }

    @SuppressWarnings("rawtypes")
    public static <T extends Resource, U extends CosmosResponse> void validateFailure(Flux<U> flowable,
                                                                                      FailureValidator validator, long timeout) throws InterruptedException {
        StepVerifier.create(flowable)
            .expectErrorSatisfies(validator::validate)
            .verify(Duration.ofMillis(timeout));
    }

    @SuppressWarnings("rawtypes")
    public <T extends CosmosItemResponse> void validateItemSuccess(
        Mono<T> responseMono, CosmosItemResponseValidator validator) {
        StepVerifier.create(responseMono)
            .assertNext(validator::validate)
            .expectComplete()
            .verify(Duration.ofMillis(subscriberValidationTimeout));
    }

    @SuppressWarnings("rawtypes")
    public <T extends CosmosItemResponse> void validateItemFailure(
        Mono<T> responseMono, FailureValidator validator) {
        StepVerifier.create(responseMono)
            .expectErrorSatisfies(validator::validate)
            .verify(Duration.ofMillis(subscriberValidationTimeout));
    }

    public <T> void validateQuerySuccess(Flux<FeedResponse<T>> flowable,
                                                          FeedResponseListValidator<T> validator) {
        validateQuerySuccess(flowable, validator, subscriberValidationTimeout);
    }

    public static <T> void validateQuerySuccess(Flux<FeedResponse<T>> flowable,
                                                                 FeedResponseListValidator<T> validator, long timeout) {
        StepVerifier.create(flowable.collectList())
            .assertNext(validator::validate)
            .expectComplete()
            .verify(Duration.ofMillis(timeout));
    }

    public static <T> void validateQuerySuccessWithContinuationTokenAndSizes(
        String query,
        CosmosAsyncContainer container,
        int[] pageSizes,
        FeedResponseListValidator<T> validator,
        Class<T> classType) {

        for (int pageSize : pageSizes) {
            List<FeedResponse<T>> receivedDocuments = queryWithContinuationTokens(query, container, pageSize, classType);
            validator.validate(receivedDocuments);
        }
    }

    public static <T> List<FeedResponse<T>> queryWithContinuationTokens(
        String query,
        CosmosAsyncContainer container,
        int pageSize,
        Class<T> classType) {

        String requestContinuation = null;
        List<String> continuationTokens = new ArrayList<String>();
        List<FeedResponse<T>> responseList = new ArrayList<>();
        do {
            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

            options.setMaxDegreeOfParallelism(2);
            CosmosPagedFlux<T> queryObservable = container.queryItems(query, options, classType);

            AtomicReference<FeedResponse<T>> value = new AtomicReference<>();
            StepVerifier.create(queryObservable.byPage(requestContinuation, pageSize))
                .assertNext(value::set)
                .thenConsumeWhile(Objects::nonNull)
                .expectComplete()
                .verify(Duration.ofMillis(TIMEOUT));

            FeedResponse<T> firstPage = value.get();
            requestContinuation = firstPage.getContinuationToken();
            responseList.add(firstPage);

            continuationTokens.add(requestContinuation);
        } while (requestContinuation != null);

        return responseList;
    }

    public <T> void validateQueryFailure(Flux<FeedResponse<T>> flowable, FailureValidator validator) {
        validateQueryFailure(flowable, validator, subscriberValidationTimeout);
    }

    public static <T> void validateQueryFailure(Flux<FeedResponse<T>> flowable,
                                                                 FailureValidator validator, long timeout) {
        StepVerifier.create(flowable)
            .expectErrorSatisfies(validator::validate)
            .verify(Duration.ofMillis(timeout));
    }

    @DataProvider
    public static Object[][] clientBuilders() {
        return new Object[][]{{createGatewayRxDocumentClient(ConsistencyLevel.SESSION, false, null, true, true)}};
    }

    @DataProvider
    public static Object[][] clientBuildersWithGateway() {
        return new Object[][]{{createGatewayRxDocumentClient(ConsistencyLevel.SESSION, false, null, true, true)}};
    }

    @DataProvider
    public static Object[][] clientBuildersWithGatewayAndHttp2() {
        return new Object[][]{
            {createGatewayRxDocumentClient(TestConfigurations.HOST, null, true, null, true, true, true)},
        };
    }

    @DataProvider
    public static Object[][] clientBuildersWithSessionConsistency() {
        return new Object[][]{
            {createDirectRxDocumentClient(ConsistencyLevel.SESSION, Protocol.TCP, false, null, true, true)},
            {createGatewayRxDocumentClient(ConsistencyLevel.SESSION, false, null, true, true)}
        };
    }

    @DataProvider
    public static Object[][] clientBuilderSolelyDirectWithSessionConsistency() {
        return new Object[][]{
                {createDirectRxDocumentClient(ConsistencyLevel.SESSION, Protocol.TCP, false, null, true, true)}
        };
    }

    static ConsistencyLevel parseConsistency(String consistency) {
        if (consistency != null) {
            consistency = CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, consistency).trim();
            return ConsistencyLevel.valueOf(consistency);
        }

        logger.error("INVALID configured test consistency [{}].", consistency);
        throw new IllegalStateException("INVALID configured test consistency " + consistency);
    }

    static List<String> parsePreferredLocation(String preferredLocations) {
        if (StringUtils.isEmpty(preferredLocations)) {
            return null;
        }

        try {
            return objectMapper.readValue(preferredLocations, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            logger.error("INVALID configured test preferredLocations [{}].", preferredLocations);
            throw new IllegalStateException("INVALID configured test preferredLocations " + preferredLocations);
        }
    }

    static List<Protocol> parseProtocols(String protocols) {
        if (StringUtils.isEmpty(protocols)) {
            return null;
        }
        List<Protocol> protocolList = new ArrayList<>();
        try {
            List<String> protocolStrings = objectMapper.readValue(protocols, new TypeReference<List<String>>() {
            });
            for(String protocol : protocolStrings) {
                protocolList.add(Protocol.valueOf(CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, protocol)));
            }
            return protocolList;
        } catch (Exception e) {
            logger.error("INVALID configured test protocols [{}].", protocols);
            throw new IllegalStateException("INVALID configured test protocols " + protocols);
        }
    }

    @DataProvider
    public static Object[][] simpleClientBuildersWithDirect() {
        return simpleClientBuildersWithDirect(true, true, true, toArray(protocols));
    }

    @DataProvider
    public static Object[][] simpleClientBuildersWithDirectHttps() {
        return simpleClientBuildersWithDirect(true, true, true, Protocol.HTTPS);
    }

    @DataProvider
    public static Object[][] simpleClientBuildersWithDirectTcp() {
        return simpleClientBuildersWithDirect(true, true, true, Protocol.TCP);
    }

    @DataProvider
    public static Object[][] simpleClientBuildersWithJustDirectTcp() {
        return simpleClientBuildersWithDirect(false, true, true, Protocol.TCP);
    }

    @DataProvider
    public static Object[][] simpleClientBuildersWithDirectTcpWithContentResponseOnWriteDisabled() {
        return simpleClientBuildersWithDirect(false, true, true, Protocol.TCP);
    }

    @DataProvider
    public static Object[][] simpleClientBuildersWithoutRetryOnThrottledRequests() {
        return new Object[][]{
            { createDirectRxDocumentClient(ConsistencyLevel.SESSION, Protocol.TCP, false, null, true, false) },
            { createGatewayRxDocumentClient(ConsistencyLevel.SESSION, false, null, true, false) }
        };
    }

    @DataProvider
    public static Object[][] simpleGatewayClient() {
        return new Object[][] {
            { createGatewayRxDocumentClient(ConsistencyLevel.SESSION, false, null, true, true) }
        };
    }

    private static Object[][] simpleClientBuildersWithDirect(
        boolean contentResponseOnWriteEnabled,
        Protocol... protocols) {

        return simpleClientBuildersWithDirect(true, contentResponseOnWriteEnabled, true, protocols);
    }

    private static Object[][] simpleClientBuildersWithDirect(
        boolean includeGateway,
        boolean contentResponseOnWriteEnabled,
        boolean retryOnThrottledRequests,
        Protocol... protocols) {

        logger.info("Max test consistency to use is [{}]", accountConsistency);
        List<ConsistencyLevel> testConsistencies = ImmutableList.of(ConsistencyLevel.EVENTUAL);

        boolean isMultiMasterEnabled = preferredLocations != null && accountConsistency == ConsistencyLevel.SESSION;

        List<CosmosClientBuilder> cosmosConfigurations = new ArrayList<>();

        for (Protocol protocol : protocols) {
            testConsistencies.forEach(consistencyLevel -> cosmosConfigurations.add(createDirectRxDocumentClient(
                consistencyLevel,
                protocol,
                isMultiMasterEnabled,
                preferredLocations,
                contentResponseOnWriteEnabled,
                retryOnThrottledRequests)));
        }

        cosmosConfigurations.forEach(c -> {
            ConnectionPolicy connectionPolicy = CosmosBridgeInternal.getConnectionPolicy(c);
            ConsistencyLevel consistencyLevel = CosmosBridgeInternal.getConsistencyLevel(c);
            logger.info("Will Use ConnectionMode [{}], Consistency [{}], Protocol [{}]",
                connectionPolicy.getConnectionMode(),
                consistencyLevel,
                extractConfigs(c).getProtocol()
            );
        });

        if (includeGateway) {
            cosmosConfigurations.add(
                createGatewayRxDocumentClient(
                    ConsistencyLevel.SESSION,
                    false,
                    null,
                    contentResponseOnWriteEnabled,
                    retryOnThrottledRequests));
        }

        return cosmosConfigurations.stream().map(b -> new Object[]{b}).collect(Collectors.toList()).toArray(new Object[0][]);
    }

    @DataProvider
    public static Object[][] clientBuildersWithDirect() {
        return clientBuildersWithDirectAllConsistencies(true, true, toArray(protocols));
    }

    @DataProvider
    public static Object[][] clientBuildersWithDirectHttps() {
        return clientBuildersWithDirectAllConsistencies(true, true, Protocol.HTTPS);
    }

    @DataProvider
    public static Object[][] clientBuildersWithDirectTcp() {
        return clientBuildersWithDirectAllConsistencies(true, true, Protocol.TCP);
    }

    @DataProvider
    public static Object[][] clientBuildersWithDirectTcpWithContentResponseOnWriteDisabled() {
        return clientBuildersWithDirectAllConsistencies(false, true, Protocol.TCP);
    }

    @DataProvider
    public static Object[][] clientBuildersWithContentResponseOnWriteEnabledAndDisabled() {
        Object[][] clientBuildersWithDisabledContentResponseOnWrite =
            clientBuildersWithDirectSession(false, true, Protocol.TCP);
        Object[][] clientBuildersWithEnabledContentResponseOnWrite =
            clientBuildersWithDirectSession(true, true, Protocol.TCP);
        int length = clientBuildersWithDisabledContentResponseOnWrite.length
            + clientBuildersWithEnabledContentResponseOnWrite.length;
        Object[][] clientBuilders = new Object[length][];
        int index = 0;
        for (int i = 0; i < clientBuildersWithDisabledContentResponseOnWrite.length; i++, index++) {
            clientBuilders[index] = clientBuildersWithDisabledContentResponseOnWrite[i];
        }
        for (int i = 0; i < clientBuildersWithEnabledContentResponseOnWrite.length; i++, index++) {
            clientBuilders[index] = clientBuildersWithEnabledContentResponseOnWrite[i];
        }
        return clientBuilders;
    }

    @DataProvider
    public static Object[][] clientBuildersWithDirectSession() {
        return clientBuildersWithDirectSession(true, true, toArray(protocols));
    }

    @DataProvider
    public static Object[][] clientBuildersWithDirectSessionIncludeComputeGateway() {
        Object[][] originalProviders = clientBuildersWithDirectSession(
            true,
            true,
            toArray(protocols));
        List<Object[]> providers = new ArrayList<>(Arrays.asList(originalProviders));
        Object[] injectedProviderParameters = new Object[1];
        CosmosClientBuilder builder = createGatewayRxDocumentClient(
            TestConfigurations.HOST.replace(ROUTING_GATEWAY_EMULATOR_PORT, COMPUTE_GATEWAY_EMULATOR_PORT),
            ConsistencyLevel.SESSION,
            false,
            null,
            true,
            true,
            false);
        injectedProviderParameters[0] = builder;

        providers.add(injectedProviderParameters);

        Object[][] array = new Object[providers.size()][];

        return providers.toArray(array);
    }

    @DataProvider
    public static Object[][] clientBuildersWithDirectTcpSession() {
        return clientBuildersWithDirectSession(true, true, Protocol.TCP);
    }

    @DataProvider
    public static Object[][] simpleClientBuilderGatewaySession() {
        return clientBuildersWithDirectSession(true, true);
    }

    protected static Protocol[] toArray(List<Protocol> protocols) {
        return protocols.toArray(new Protocol[protocols.size()]);
    }

    protected static Object[][] clientBuildersWithDirectSession(boolean contentResponseOnWriteEnabled, boolean retryOnThrottledRequests, Protocol... protocols) {
        return clientBuildersWithDirect(new ArrayList<ConsistencyLevel>() {{
            add(ConsistencyLevel.SESSION);
        }}, contentResponseOnWriteEnabled, retryOnThrottledRequests, protocols);
    }

    private static Object[][] clientBuildersWithDirectAllConsistencies(boolean contentResponseOnWriteEnabled, boolean retryOnThrottledRequests, Protocol... protocols) {
        logger.info("Max test consistency to use is [{}]", accountConsistency);
        return clientBuildersWithDirect(desiredConsistencies, contentResponseOnWriteEnabled, retryOnThrottledRequests, protocols);
    }

    static List<ConsistencyLevel> parseDesiredConsistencies(String consistencies) {
        if (StringUtils.isEmpty(consistencies)) {
            return null;
        }
        List<ConsistencyLevel> consistencyLevels = new ArrayList<>();
        try {
            List<String> consistencyStrings = objectMapper.readValue(consistencies, new TypeReference<List<String>>() {});
            for(String consistency : consistencyStrings) {
                consistencyLevels.add(ConsistencyLevel.valueOf(CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, consistency)));
            }
            return consistencyLevels;
        } catch (Exception e) {
            logger.error("INVALID consistency test desiredConsistencies [{}].", consistencies);
            throw new IllegalStateException("INVALID configured test desiredConsistencies " + consistencies);
        }
    }

    @SuppressWarnings("fallthrough")
    static List<ConsistencyLevel> allEqualOrLowerConsistencies(ConsistencyLevel accountConsistency) {
        List<ConsistencyLevel> testConsistencies = new ArrayList<>();
        switch (accountConsistency) {

            case STRONG:
                testConsistencies.add(ConsistencyLevel.STRONG);
            case BOUNDED_STALENESS:
                testConsistencies.add(ConsistencyLevel.BOUNDED_STALENESS);
            case SESSION:
                testConsistencies.add(ConsistencyLevel.SESSION);
            case CONSISTENT_PREFIX:
                testConsistencies.add(ConsistencyLevel.CONSISTENT_PREFIX);
            case EVENTUAL:
                testConsistencies.add(ConsistencyLevel.EVENTUAL);
                break;
            default:
                throw new IllegalStateException("INVALID configured test consistency " + accountConsistency);
        }
        return testConsistencies;
    }

    private static Object[][] clientBuildersWithDirect(
        List<ConsistencyLevel> testConsistencies,
        boolean contentResponseOnWriteEnabled,
        boolean retryOnThrottledRequests,
        Protocol... protocols) {
        boolean isMultiMasterEnabled = preferredLocations != null && accountConsistency == ConsistencyLevel.SESSION;

        List<CosmosClientBuilder> cosmosConfigurations = new ArrayList<>();

        for (Protocol protocol : protocols) {
            testConsistencies.forEach(consistencyLevel -> cosmosConfigurations.add(createDirectRxDocumentClient(consistencyLevel,
                protocol,
                isMultiMasterEnabled,
                preferredLocations,
                contentResponseOnWriteEnabled,
                retryOnThrottledRequests)));
        }

        cosmosConfigurations.forEach(c -> {
            ConnectionPolicy connectionPolicy = CosmosBridgeInternal.getConnectionPolicy(c);
            ConsistencyLevel consistencyLevel = CosmosBridgeInternal.getConsistencyLevel(c);
            logger.info("Will Use ConnectionMode [{}], Consistency [{}], Protocol [{}]",
                connectionPolicy.getConnectionMode(),
                consistencyLevel,
                extractConfigs(c).getProtocol()
            );
        });

        cosmosConfigurations.add(
            createGatewayRxDocumentClient(
                ConsistencyLevel.SESSION,
                isMultiMasterEnabled,
                preferredLocations,
                contentResponseOnWriteEnabled,
                retryOnThrottledRequests));

        return cosmosConfigurations.stream().map(c -> new Object[]{c}).collect(Collectors.toList()).toArray(new Object[0][]);
    }

    static protected CosmosClientBuilder createGatewayHouseKeepingDocumentClient(boolean contentResponseOnWriteEnabled) {
        ThrottlingRetryOptions options = new ThrottlingRetryOptions();
        // Metadata operations issued by the shared housekeeping client during suite setup/cleanup
        // (create/delete/query databases and containers) can be throttled with 429 / substatus 3200
        // ("high rate of metadata requests"). The SDK default caps throttle retries at 9 attempts, which
        // this client can exceed; allow many more so transient metadata throttling does not fail setup/cleanup.
        options.setMaxRetryAttemptsOnThrottledRequests(200);
        options.setMaxRetryWaitTime(Duration.ofSeconds(SUITE_SETUP_TIMEOUT));
        GatewayConnectionConfig gatewayConnectionConfig = new GatewayConnectionConfig();
        return new CosmosClientBuilder().endpoint(TestConfigurations.HOST)
                                        .credential(credential)
                                        .gatewayMode(gatewayConnectionConfig)
                                        .throttlingRetryOptions(options)
                                        .contentResponseOnWriteEnabled(contentResponseOnWriteEnabled)
                                        .consistencyLevel(ConsistencyLevel.SESSION);
    }

    static protected CosmosClientBuilder createGatewayRxDocumentClient(
        ConsistencyLevel consistencyLevel,
        boolean multiMasterEnabled,
        List<String> preferredRegions,
        boolean contentResponseOnWriteEnabled,
        boolean retryOnThrottledRequests) {

        return createGatewayRxDocumentClient(
            TestConfigurations.HOST,
            consistencyLevel,
            multiMasterEnabled,
            preferredRegions,
            contentResponseOnWriteEnabled,
            retryOnThrottledRequests,
            false);
    }

    static protected CosmosClientBuilder createGatewayRxDocumentClient(
        String endpoint,
        ConsistencyLevel consistencyLevel,
        boolean multiMasterEnabled,
        List<String> preferredRegions,
        boolean contentResponseOnWriteEnabled,
        boolean retryOnThrottledRequests,
        boolean isHttp2TransportRequired) {

        GatewayConnectionConfig gatewayConnectionConfig = new GatewayConnectionConfig();
        if (Configs.isHttp2Enabled() || isHttp2TransportRequired) {
            Http2ConnectionConfig http2ConnectionConfig = new Http2ConnectionConfig()
                .setEnabled(true)
                .setMaxConnectionPoolSize(10)
                .setMinConnectionPoolSize(1)
                .setMaxConcurrentStreams(30);
            gatewayConnectionConfig.setHttp2ConnectionConfig(http2ConnectionConfig);
        }

        CosmosClientBuilder builder = new CosmosClientBuilder().endpoint(endpoint)
            .credential(credential)
            .gatewayMode(gatewayConnectionConfig)
            .multipleWriteRegionsEnabled(multiMasterEnabled)
            .preferredRegions(preferredRegions)
            .contentResponseOnWriteEnabled(contentResponseOnWriteEnabled)
            .consistencyLevel(consistencyLevel);
        ImplementationBridgeHelpers
            .CosmosClientBuilderHelper
            .getCosmosClientBuilderAccessor()
            .buildConnectionPolicy(builder);

        if (!retryOnThrottledRequests) {
            builder.throttlingRetryOptions(new ThrottlingRetryOptions().setMaxRetryAttemptsOnThrottledRequests(0));
        }

        return builder;
    }

    static protected CosmosClientBuilder createGatewayRxDocumentClient() {
        return createGatewayRxDocumentClient(ConsistencyLevel.SESSION, false, null, true, true);
    }

    static protected CosmosClientBuilder createDirectRxDocumentClient(ConsistencyLevel consistencyLevel,
                                                                      Protocol protocol,
                                                                      boolean multiMasterEnabled,
                                                                      List<String> preferredRegions,
                                                                      boolean contentResponseOnWriteEnabled,
                                                                      boolean retryOnThrottledRequests) {
        CosmosClientBuilder builder = new CosmosClientBuilder().endpoint(TestConfigurations.HOST)
                                                               .credential(credential)
                                                               .directMode(DirectConnectionConfig.getDefaultConfig())
                                                               .contentResponseOnWriteEnabled(contentResponseOnWriteEnabled)
                                                               .consistencyLevel(consistencyLevel);

        if (preferredRegions != null) {
            builder.preferredRegions(preferredRegions);
        }

        if (multiMasterEnabled && consistencyLevel == ConsistencyLevel.SESSION) {
            builder.multipleWriteRegionsEnabled(true);
        }

        if (!retryOnThrottledRequests) {
            builder.throttlingRetryOptions(new ThrottlingRetryOptions().setMaxRetryAttemptsOnThrottledRequests(0));
        }

        Configs configs = spy(new Configs());
        doAnswer((Answer<Protocol>)invocation -> protocol).when(configs).getProtocol();

        return injectConfigs(builder, configs);
    }

    protected int expectedNumberOfPages(int totalExpectedResult, int maxPageSize) {
        return Math.max((totalExpectedResult + maxPageSize - 1 ) / maxPageSize, 1);
    }

    @DataProvider(name = "queryMetricsArgProvider")
    public Object[][] queryMetricsArgProvider() {
        return new Object[][]{
            {true},
            {false},
            {null}
        };
    }

    @DataProvider(name = "queryWithOrderByProvider")
    public Object[][] queryWithOrderBy() {
        return new Object[][]{
            // query wit orderby, matchedOrderByQuery
            { "SELECT DISTINCT VALUE c.id from c ORDER BY c.id DESC", true },
            { "SELECT DISTINCT VALUE c.id from c ORDER BY c._ts DESC", false }
        };
    }

    public static CosmosClientBuilder copyCosmosClientBuilder(CosmosClientBuilder builder) {
        return CosmosBridgeInternal.cloneCosmosClientBuilder(builder);
    }

    public byte[] decodeHexString(String string) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (int i = 0; i < string.length(); i+=2) {
            int b = Integer.parseInt(string.substring(i, i + 2), 16);
            outputStream.write(b);
        }
        return outputStream.toByteArray();
    }

    public static String captureNettyLeaks() {
        System.gc();
        try {
            Thread.sleep(5_000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        List<String> nettyLeaks = CosmosNettyLeakDetectorFactory.resetIdentifiedLeaks();
        if (nettyLeaks.size() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("\n");
            for (String leak : nettyLeaks) {
                sb.append(leak).append("\n");
            }

            return "NETTY LEAKS detected: "
                + "\n\n"
                + sb;
        }

        return "";
    }

    // ==================== AsyncDocumentClient (internal API) helper methods ====================

    /**
     * Creates a gateway AsyncDocumentClient.Builder for housekeeping operations.
     * @return the AsyncDocumentClient.Builder
     */
    protected static AsyncDocumentClient.Builder createGatewayHouseKeepingDocumentClient() {
        GatewayConnectionConfig gatewayConnectionConfig = new GatewayConnectionConfig();
        ThrottlingRetryOptions options = new ThrottlingRetryOptions();
        // Allow many more throttle retries than the SDK default (9) so transient metadata throttling
        // (429 / substatus 3200) during setup/cleanup does not fail the housekeeping client.
        options.setMaxRetryAttemptsOnThrottledRequests(200);
        options.setMaxRetryWaitTime(Duration.ofSeconds(SUITE_SETUP_TIMEOUT));
        ConnectionPolicy connectionPolicy = new ConnectionPolicy(gatewayConnectionConfig);
        connectionPolicy.setThrottlingRetryOptions(options);
        return new AsyncDocumentClient.Builder()
                .withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION)
                .withContentResponseOnWriteEnabled(true)
                .withClientTelemetryConfig(
                            new CosmosClientTelemetryConfig()
                                .sendClientTelemetryToService(ClientTelemetry.DEFAULT_CLIENT_TELEMETRY_ENABLED));
    }

    /**
     * Creates a gateway AsyncDocumentClient.Builder.
     * @return the AsyncDocumentClient.Builder
     */
    protected static AsyncDocumentClient.Builder createGatewayRxDocumentClient(
            ConsistencyLevel consistencyLevel, boolean multiMasterEnabled,
            List<String> preferredLocationsList, boolean contentResponseOnWriteEnabled) {
        GatewayConnectionConfig gatewayConnectionConfig = new GatewayConnectionConfig();
        ConnectionPolicy connectionPolicy = new ConnectionPolicy(gatewayConnectionConfig);
        connectionPolicy.setMultipleWriteRegionsEnabled(multiMasterEnabled);
        connectionPolicy.setPreferredRegions(preferredLocationsList);
        return new AsyncDocumentClient.Builder()
                .withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(consistencyLevel)
                .withContentResponseOnWriteEnabled(contentResponseOnWriteEnabled)
                .withClientTelemetryConfig(
                            new CosmosClientTelemetryConfig()
                                .sendClientTelemetryToService(ClientTelemetry.DEFAULT_CLIENT_TELEMETRY_ENABLED));
    }

    protected static AsyncDocumentClient.Builder createInternalGatewayRxDocumentClient() {
        return createGatewayRxDocumentClient(ConsistencyLevel.SESSION, false, null, true);
    }

    /**
     * Creates a direct AsyncDocumentClient.Builder.
     * @return the AsyncDocumentClient.Builder
     */
    protected static AsyncDocumentClient.Builder createDirectRxDocumentClient(
            ConsistencyLevel consistencyLevel,
            Protocol protocol,
            boolean multiMasterEnabled,
            List<String> preferredRegions,
            boolean contentResponseOnWriteEnabled) {
        DirectConnectionConfig directConnectionConfig = new DirectConnectionConfig();

        ConnectionPolicy connectionPolicy = new ConnectionPolicy(directConnectionConfig);
        if (preferredRegions != null) {
            connectionPolicy.setPreferredRegions(preferredRegions);
        }

        if (multiMasterEnabled && consistencyLevel == ConsistencyLevel.SESSION) {
            connectionPolicy.setMultipleWriteRegionsEnabled(true);
        }
        Configs configs = spy(new Configs());
        doAnswer((Answer<Protocol>) invocation -> protocol).when(configs).getProtocol();

        return new AsyncDocumentClient.Builder()
                .withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(consistencyLevel)
                .withConfigs(configs)
                .withContentResponseOnWriteEnabled(contentResponseOnWriteEnabled)
                .withClientTelemetryConfig(
                            new CosmosClientTelemetryConfig()
                                .sendClientTelemetryToService(ClientTelemetry.DEFAULT_CLIENT_TELEMETRY_ENABLED));
    }

    // ==================== Internal API data providers ====================

    @DataProvider
    public static Object[][] internalClientBuilders() {
        return new Object[][]{{createGatewayRxDocumentClient(ConsistencyLevel.SESSION, false, null, true)}};
    }

    @DataProvider
    public static Object[][] internalClientBuildersWithSessionConsistency() {
        return new Object[][]{
                {createGatewayRxDocumentClient(ConsistencyLevel.SESSION, false, null, true)},
                {createDirectRxDocumentClient(ConsistencyLevel.SESSION, Protocol.HTTPS, false, null, true)},
                {createDirectRxDocumentClient(ConsistencyLevel.SESSION, Protocol.TCP, false, null, true)}
        };
    }

    // ==================== Internal API CRUD helper methods ====================

    public static DocumentCollection createCollection(String databaseId,
                                                      DocumentCollection collection,
                                                      RequestOptions options) {
        AsyncDocumentClient client = createGatewayHouseKeepingDocumentClient().build();
        try {
            return client.createCollection("dbs/" + databaseId, collection, options).block().getResource();
        } finally {
            client.close();
        }
    }

    public static Database createDatabase(AsyncDocumentClient client, String databaseId) {
        Database database = new Database();
        database.setId(databaseId);
        return client.createDatabase(database, null).block().getResource();
    }

    public static Database createDatabase(AsyncDocumentClient client, Database database) {
        return client.createDatabase(database, null).block().getResource();
    }

    public static DocumentCollection createCollection(AsyncDocumentClient client, String databaseId,
                                                      DocumentCollection collection, RequestOptions options) {
        return client.createCollection("dbs/" + databaseId, collection, options).block().getResource();
    }

    public static DocumentCollection createCollection(AsyncDocumentClient client, String databaseId,
                                                      DocumentCollection collection) {
        return client.createCollection("dbs/" + databaseId, collection, null).block().getResource();
    }

    public static Document createDocument(AsyncDocumentClient client, String databaseId, String collectionId, Document document) {
        return createDocument(client, databaseId, collectionId, document, null);
    }

    public static Document createDocument(AsyncDocumentClient client, String databaseId, String collectionId, Document document, RequestOptions options) {
        return client.createDocument(TestUtils.getCollectionNameLink(databaseId, collectionId), document, options, false).block().getResource();
    }

    public Flux<ResourceResponse<Document>> bulkInsert(AsyncDocumentClient client,
                                                             String collectionLink,
                                                             List<Document> documentDefinitionList,
                                                             int concurrencyLevel) {
        ArrayList<Mono<ResourceResponse<Document>>> result = new ArrayList<>(documentDefinitionList.size());
        for (Document docDef : documentDefinitionList) {
            result.add(client.createDocument(collectionLink, docDef, null, false));
        }

        return Flux.merge(Flux.fromIterable(result), concurrencyLevel).publishOn(Schedulers.parallel());
    }

    public Flux<ResourceResponse<Document>> bulkInsert(AsyncDocumentClient client,
                                                             String collectionLink,
                                                             List<Document> documentDefinitionList) {
        return bulkInsert(client, collectionLink, documentDefinitionList, DEFAULT_BULK_INSERT_CONCURRENCY_LEVEL);
    }

    public static User createUser(AsyncDocumentClient client, String databaseId, User user) {
        return client.createUser("dbs/" + databaseId, user, null).block().getResource();
    }

    public static User safeCreateUser(AsyncDocumentClient client, String databaseId, User user) {
        deleteUserIfExists(client, databaseId, user.getId());
        return createUser(client, databaseId, user);
    }

    public static Permission createPermission(AsyncDocumentClient client, String userLink, Permission permission, RequestOptions options) {
        return client.createPermission(userLink, permission, options).block().getResource();
    }

    public static String getUserLink(Database database, User user) {
        return database.getSelfLink() + "/users/" + user.getId();
    }

    // ==================== Internal API cleanup methods ====================

    protected static void safeDeleteDatabase(AsyncDocumentClient client, Database database) {
        if (client != null && database != null) {
            try {
                client.deleteDatabase(database.getSelfLink(), null).block();
            } catch (Exception e) {
                // Ignore deletion errors
            }
        }
    }

    protected static void safeDeleteDatabase(AsyncDocumentClient client, String databaseId) {
        if (client != null && databaseId != null) {
            try {
                client.deleteDatabase(TestUtils.getDatabaseNameLink(databaseId), null).block();
            } catch (Exception e) {
                System.err.println("Failed to delete database '" + databaseId + "': " + e.getMessage());
            }
        }
    }

    protected static void safeDeleteCollection(AsyncDocumentClient client, DocumentCollection collection) {
        if (client != null && collection != null) {
            try {
                client.deleteCollection(collection.getSelfLink(), null).block();
            } catch (Exception e) {
                // Ignore deletion errors
            }
        }
    }

    protected static void safeDeleteCollection(AsyncDocumentClient client, String databaseId, String collectionId) {
        if (client != null && databaseId != null && collectionId != null) {
            try {
                client.deleteCollection("/dbs/" + databaseId + "/colls/" + collectionId, null).block();
            } catch (Exception e) {
                // Ignore deletion errors
            }
        }
    }

    protected static void assertThinClientEndpointUsed(CosmosDiagnostics diagnostics) {
        assertThat(diagnostics).isNotNull();

        CosmosDiagnosticsContext ctx = diagnostics.getDiagnosticsContext();
        assertThat(ctx).isNotNull();

        assertThinClientEndpointUsed(ctx);
    }

    protected static void assertThinClientEndpointUsed(CosmosDiagnosticsContext ctx) {
        assertThat(ctx).isNotNull();

        Collection<CosmosDiagnosticsRequestInfo> requests = ctx.getRequestInfo();
        assertThat(requests).isNotNull();
        assertThat(requests.size()).isPositive();

        for (CosmosDiagnosticsRequestInfo requestInfo : requests) {
            if (requestInfo.getEndpoint() != null
                && requestInfo.getEndpoint().contains(THIN_CLIENT_ENDPOINT_INDICATOR)) {
                return;
            }
        }

        assertThat(false)
            .as("No request targeting thin client proxy endpoint (" + THIN_CLIENT_ENDPOINT_INDICATOR + ")")
            .isTrue();
    }

    protected static void safeClose(AsyncDocumentClient client) {
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected static void safeCloseAsync(AsyncDocumentClient client) {
        if (client != null) {
            new Thread(() -> {
                try {
                    client.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    protected static void deleteUserIfExists(AsyncDocumentClient client, String databaseId, String userId) {
        if (client != null) {
            try {
                client.readUser("/dbs/" + databaseId + "/users/" + userId, null).block();
                client.deleteUser("/dbs/" + databaseId + "/users/" + userId, null).block();
            } catch (Exception e) {
                // Ignore if not found
            }
        }
    }

    protected static void deleteCollectionIfExists(AsyncDocumentClient client, String databaseId, String collectionId) {
        if (client != null) {
            try {
                client.readCollection("/dbs/" + databaseId + "/colls/" + collectionId, null).block();
                client.deleteCollection("/dbs/" + databaseId + "/colls/" + collectionId, null).block();
            } catch (Exception e) {
                // Ignore if not found
            }
        }
    }

    protected static void deleteCollection(AsyncDocumentClient client, String collectionLink) {
        if (client != null) {
            try {
                client.deleteCollection(collectionLink, null).block();
            } catch (Exception e) {
                // Ignore if not found
            }
        }
    }

    protected static void truncateCollection(DocumentCollection collection) {
        if (collection == null) {
            logger.warn("truncateCollection called with null collection - skipping. "
                + "This likely indicates @BeforeSuite initialization failed.");
            return;
        }

        logger.info("Truncating DocumentCollection {} ...", collection.getId());

        try (CosmosAsyncClient cosmosClient = new CosmosClientBuilder()
            .key(TestConfigurations.MASTER_KEY)
            .endpoint(TestConfigurations.HOST)
            .buildAsyncClient()) {

            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
            options.setMaxDegreeOfParallelism(-1);

            ModelBridgeInternal.setQueryRequestOptionsMaxItemCount(options, 100);

            logger.info("Truncating DocumentCollection {} documents ...", collection.getId());

            String altLink = collection.getAltLink();
            if (altLink == null) {
                logger.warn("DocumentCollection {} has null altLink - skipping truncation. "
                    + "This likely indicates the collection was not properly initialized.", collection.getId());
                return;
            }
            // Normalize altLink so both "dbs/.../colls/..." and "/dbs/.../colls/..." are handled consistently.
            String normalizedAltLink = StringUtils.strip(altLink, "/");
            String[] altLinkSegments = normalizedAltLink.split("/");
            // altLink format (after normalization): dbs/{dbName}/colls/{collName}
            String databaseName = altLinkSegments[1];
            String containerName = altLinkSegments[3];

            CosmosAsyncContainer container = cosmosClient.getDatabase(databaseName).getContainer(containerName);

            Flux<CosmosItemOperation> deleteOperations =
                container
                    .queryItems( "SELECT * FROM root", options, Document.class)
                    .byPage()
                    .publishOn(Schedulers.parallel())
                    .flatMap(page -> Flux.fromIterable(page.getResults()))
                    .map(doc -> {
                        PartitionKey partitionKey = PartitionKeyHelper.extractPartitionKeyFromDocument(doc, collection.getPartitionKey());
                        if (partitionKey == null) {
                            partitionKey = PartitionKey.NONE;
                        }

                        return CosmosBulkOperations.getDeleteItemOperation(doc.getId(), partitionKey);
                    });

            CosmosBulkExecutionOptions bulkOptions = new CosmosBulkExecutionOptions();
            ImplementationBridgeHelpers.CosmosBulkExecutionOptionsHelper
                .getCosmosBulkExecutionOptionsAccessor()
                .getImpl(bulkOptions)
                .setCosmosEndToEndLatencyPolicyConfig(
                    new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(65))
                        .build());

            cosmosClient.getDatabase(databaseName)
                        .getContainer(containerName)
                        .executeBulkOperations(deleteOperations, bulkOptions)
                        .flatMap(response -> {
                            if (response.getException() != null) {
                                Exception ex = response.getException();
                                if (ex instanceof CosmosException) {
                                    CosmosException cosmosException = (CosmosException) ex;
                                    if (cosmosException.getStatusCode() == HttpConstants.StatusCodes.NOTFOUND
                                        && cosmosException.getSubStatusCode() == 0) {
                                        return Mono.empty();
                                    }
                                }
                                return retryOnTransientCleanupFailure(Mono.error(ex));
                            }
                            if (response.getResponse() != null
                                && response.getResponse().getStatusCode() == HttpConstants.StatusCodes.NOTFOUND) {
                                return Mono.empty();
                            }
                            if (response.getResponse() != null
                                && !response.getResponse().isSuccessStatusCode()) {
                                CosmosException bulkException = BridgeInternal.createCosmosException(
                                    response.getResponse().getStatusCode(),
                                    "Bulk delete operation failed with status code " + response.getResponse().getStatusCode());
                                BridgeInternal.setSubStatusCode(bulkException, response.getResponse().getSubStatusCode());
                                return retryOnTransientCleanupFailure(Mono.error(bulkException));
                            }
                            return Mono.just(response);
                        })
                        .blockLast();

            logger.info("Truncating DocumentCollection {} triggers ...", collection.getId());

            container
                .getScripts()
                .queryTriggers("SELECT * FROM root", new CosmosQueryRequestOptions())
                .byPage()
                .publishOn(Schedulers.parallel())
                .flatMap(page -> Flux.fromIterable(page.getResults()))
                .flatMap(trigger -> retryOnTransientCleanupFailure(
                    container.getScripts().getTrigger(trigger.getId()).delete()))
                .then().block();

            logger.info("Truncating DocumentCollection {} storedProcedures ...", collection.getId());

            container
                .getScripts()
                .queryStoredProcedures("SELECT * FROM root", new CosmosQueryRequestOptions())
                .byPage()
                .publishOn(Schedulers.parallel())
                .flatMap(page -> Flux.fromIterable(page.getResults()))
                .flatMap(storedProcedure -> {
                    return retryOnTransientCleanupFailure(
                        container.getScripts().getStoredProcedure(storedProcedure.getId()).delete());
                })
                .then()
                .block();

            logger.info("Truncating DocumentCollection {} udfs ...", collection.getId());

            container
                .getScripts()
                .queryUserDefinedFunctions("SELECT * FROM root", new CosmosQueryRequestOptions())
                .byPage()
                .publishOn(Schedulers.parallel()).flatMap(page -> Flux.fromIterable(page.getResults()))
                .flatMap(udf -> {
                    return retryOnTransientCleanupFailure(
                        container.getScripts().getUserDefinedFunction(udf.getId()).delete());
                })
                .then()
                .block();
        }

        logger.info("Finished truncating DocumentCollection {}.", collection.getId());
    }

    protected static void deleteDocumentIfExists(AsyncDocumentClient client, String databaseId, String collectionId, String docId) {
        if (client != null) {
            try {
                client.deleteDocument("/dbs/" + databaseId + "/colls/" + collectionId + "/docs/" + docId, null).block();
            } catch (Exception e) {
                // Ignore if not found
            }
        }
    }

    protected static void deleteDocument(AsyncDocumentClient client, String documentLink, PartitionKey partitionKey, String collectionLink) {
        if (client != null) {
            try {
                RequestOptions options = new RequestOptions();
                options.setPartitionKey(partitionKey);
                client.deleteDocument(documentLink, options).block();
            } catch (Exception e) {
                // Log but don't throw
                logger.warn("Failed to delete document: {}", documentLink, e);
            }
        }
    }

    // ==================== Internal API validation methods ====================

    public <T extends Resource> void validateSuccess(Mono<ResourceResponse<T>> observable,
                                                     ResourceResponseValidator<T> validator) {
        validateSuccess(observable, validator, subscriberValidationTimeout);
    }

    public static <T extends Resource> void validateSuccess(Mono<ResourceResponse<T>> observable,
                                                            ResourceResponseValidator<T> validator, long timeout) {
        StepVerifier.create(observable)
            .assertNext(validator::validate)
            .expectComplete()
            .verify(Duration.ofMillis(timeout));
    }

    public <T extends Resource> void validateResourceResponseFailure(Mono<ResourceResponse<T>> observable,
                                                     FailureValidator validator) {
        validateResourceResponseFailure(observable, validator, subscriberValidationTimeout);
    }

    public static <T extends Resource> void validateResourceResponseFailure(Mono<ResourceResponse<T>> observable,
                                                            FailureValidator validator, long timeout) {
        StepVerifier.create(observable)
            .expectErrorSatisfies(validator::validate)
            .verify(Duration.ofMillis(timeout));
    }

    public <T extends Resource> void validateResourceQuerySuccess(Flux<FeedResponse<T>> observable,
                                                          FeedResponseListValidator<T> validator) {
        validateResourceQuerySuccess(observable, validator, subscriberValidationTimeout);
    }

    public static <T extends Resource> void validateResourceQuerySuccess(Flux<FeedResponse<T>> observable,
                                                                 FeedResponseListValidator<T> validator, long timeout) {
        StepVerifier.create(observable.collectList())
            .assertNext(validator::validate)
            .expectComplete()
            .verify(Duration.ofMillis(timeout));
    }

    public <T extends Resource> void validateResourceQueryFailure(Flux<FeedResponse<T>> observable,
                                                          FailureValidator validator) {
        validateResourceQueryFailure(observable, validator, subscriberValidationTimeout);
    }

    public static <T extends Resource> void validateResourceQueryFailure(Flux<FeedResponse<T>> observable,
                                                                 FailureValidator validator, long timeout) {
        StepVerifier.create(observable)
            .expectErrorSatisfies(validator::validate)
            .verify(Duration.ofMillis(timeout));
    }

    // ==================== Internal API collection definitions ====================

    protected static DocumentCollection getInternalCollectionDefinition() {
        return getInternalCollectionDefinition(UUID.randomUUID().toString());
    }

    protected static DocumentCollection getInternalCollectionDefinition(String collectionId) {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);

        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setId(collectionId);
        collectionDefinition.setPartitionKey(partitionKeyDef);

        return collectionDefinition;
    }

    protected static DocumentCollection getInternalCollectionDefinitionWithRangeRangeIndex() {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);

        IndexingPolicy indexingPolicy = new IndexingPolicy();
        List<IncludedPath> includedPaths = new ArrayList<>();
        IncludedPath includedPath = new IncludedPath("/*");
        includedPaths.add(includedPath);
        indexingPolicy.setIncludedPaths(includedPaths);

        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setIndexingPolicy(indexingPolicy);
        collectionDefinition.setId(UUID.randomUUID().toString());
        collectionDefinition.setPartitionKey(partitionKeyDef);

        return collectionDefinition;
    }

    public static String getCollectionLink(DocumentCollection collection) {
        return collection.getSelfLink();
    }

    public static String getDatabaseLink(Database database) {
        return database.getSelfLink();
    }

    @SuppressWarnings("fallthrough")
    protected static void waitIfNeededForReplicasToCatchUp(AsyncDocumentClient.Builder clientBuilder) {
        switch (clientBuilder.getDesiredConsistencyLevel()) {
            case EVENTUAL:
            case CONSISTENT_PREFIX:
                logger.info(" additional wait in EVENTUAL mode so the replica catch up");
                // give times to replicas to catch up after a write
                try {
                    TimeUnit.MILLISECONDS.sleep(WAIT_REPLICA_CATCH_UP_IN_MILLIS);
                } catch (Exception e) {
                    logger.error("unexpected failure", e);
                }

            case SESSION:
            case BOUNDED_STALENESS:
            case STRONG:
            default:
                break;
        }
    }

}
