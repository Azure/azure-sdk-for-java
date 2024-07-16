// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.directconnectivity.Protocol;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.time.Duration;
import java.util.Locale;

import static com.azure.cosmos.implementation.guava25.base.MoreObjects.firstNonNull;
import static com.azure.cosmos.implementation.guava25.base.Strings.emptyToNull;

public class Configs {
    private static final Logger logger = LoggerFactory.getLogger(Configs.class);

    /**
     * Integer value specifying the speculation type
     * <pre>
     * 0 - No speculation
     * 1 - Threshold based speculation
     * </pre>
     */
    public static final String SPECULATION_TYPE = "COSMOS_SPECULATION_TYPE";
    public static final String SPECULATION_THRESHOLD = "COSMOS_SPECULATION_THRESHOLD";
    public static final String SPECULATION_THRESHOLD_STEP = "COSMOS_SPECULATION_THRESHOLD_STEP";
    private final SslContext sslContext;

    // The names we use are consistent with the:
    // * Azure environment variable naming conventions documented at https://azure.github.io/azure-sdk/java_implementation.html and
    // * Java property naming conventions as illustrated by the name/value pairs returned by System.getProperties.

    private static final String PROTOCOL_ENVIRONMENT_VARIABLE = "AZURE_COSMOS_DIRECT_MODE_PROTOCOL";
    private static final String PROTOCOL_PROPERTY = "azure.cosmos.directModeProtocol";
    private static final Protocol DEFAULT_PROTOCOL = Protocol.TCP;

    private static final String UNAVAILABLE_LOCATIONS_EXPIRATION_TIME_IN_SECONDS = "COSMOS.UNAVAILABLE_LOCATIONS_EXPIRATION_TIME_IN_SECONDS";
    private static final String GLOBAL_ENDPOINT_MANAGER_INITIALIZATION_TIME_IN_SECONDS = "COSMOS.GLOBAL_ENDPOINT_MANAGER_MAX_INIT_TIME_IN_SECONDS";

    private static final String MAX_HTTP_BODY_LENGTH_IN_BYTES = "COSMOS.MAX_HTTP_BODY_LENGTH_IN_BYTES";
    private static final String MAX_HTTP_INITIAL_LINE_LENGTH_IN_BYTES = "COSMOS.MAX_HTTP_INITIAL_LINE_LENGTH_IN_BYTES";
    private static final String MAX_HTTP_CHUNK_SIZE_IN_BYTES = "COSMOS.MAX_HTTP_CHUNK_SIZE_IN_BYTES";
    private static final String MAX_HTTP_HEADER_SIZE_IN_BYTES = "COSMOS.MAX_HTTP_HEADER_SIZE_IN_BYTES";
    private static final String MAX_DIRECT_HTTPS_POOL_SIZE = "COSMOS.MAX_DIRECT_HTTP_CONNECTION_LIMIT";
    private static final String HTTP_RESPONSE_TIMEOUT_IN_SECONDS = "COSMOS.HTTP_RESPONSE_TIMEOUT_IN_SECONDS";

    public static final int DEFAULT_HTTP_DEFAULT_CONNECTION_POOL_SIZE = 1000;
    public static final String HTTP_DEFAULT_CONNECTION_POOL_SIZE = "COSMOS.DEFAULT_HTTP_CONNECTION_POOL_SIZE";
    public static final String HTTP_DEFAULT_CONNECTION_POOL_SIZE_VARIABLE = "COSMOS_DEFAULT_HTTP_CONNECTION_POOL_SIZE";

    public static final boolean DEFAULT_E2E_FOR_NON_POINT_DISABLED_DEFAULT = false;
    public static final String DEFAULT_E2E_FOR_NON_POINT_DISABLED = "COSMOS.E2E_FOR_NON_POINT_DISABLED";
    public static final String DEFAULT_E2E_FOR_NON_POINT_DISABLED_VARIABLE = "COSMOS_E2E_FOR_NON_POINT_DISABLED";

    public static final int DEFAULT_HTTP_MAX_REQUEST_TIMEOUT = 60;
    public static final String HTTP_MAX_REQUEST_TIMEOUT = "COSMOS.HTTP_MAX_REQUEST_TIMEOUT";
    public static final String HTTP_MAX_REQUEST_TIMEOUT_VARIABLE = "COSMOS_HTTP_MAX_REQUEST_TIMEOUT";

    private static final String QUERY_PLAN_RESPONSE_TIMEOUT_IN_SECONDS = "COSMOS.QUERY_PLAN_RESPONSE_TIMEOUT_IN_SECONDS";
    private static final String ADDRESS_REFRESH_RESPONSE_TIMEOUT_IN_SECONDS = "COSMOS.ADDRESS_REFRESH_RESPONSE_TIMEOUT_IN_SECONDS";

    public static final String NON_IDEMPOTENT_WRITE_RETRY_POLICY = "COSMOS.WRITE_RETRY_POLICY";
    public static final String NON_IDEMPOTENT_WRITE_RETRY_POLICY_VARIABLE = "COSMOS_WRITE_RETRY_POLICY";

    // Example for customer how to setup the proxy:
    // System.setProperty(
    //  "COSMOS.CLIENT_TELEMETRY_PROXY_OPTIONS_CONFIG","{\"type\":\"HTTP\", \"host\": \"localhost\", \"port\": 8080}")
    private static final String CLIENT_TELEMETRY_PROXY_OPTIONS_CONFIG = "COSMOS.CLIENT_TELEMETRY_PROXY_OPTIONS_CONFIG";
    // In the future, the following two client telemetry related configs will be part of the database account info
    // Before that day comes, use JVM properties
    private static final String CLIENT_TELEMETRY_SCHEDULING_IN_SECONDS = "COSMOS.CLIENT_TELEMETRY_SCHEDULING_IN_SECONDS";
    private static final String CLIENT_TELEMETRY_ENDPOINT = "COSMOS.CLIENT_TELEMETRY_ENDPOINT";

    private static final String ENVIRONMENT_NAME = "COSMOS.ENVIRONMENT_NAME";
    private static final String QUERYPLAN_CACHING_ENABLED = "COSMOS.QUERYPLAN_CACHING_ENABLED";

    private static final int DEFAULT_CLIENT_TELEMETRY_SCHEDULING_IN_SECONDS = 10 * 60;
    private static final int DEFAULT_UNAVAILABLE_LOCATIONS_EXPIRATION_TIME_IN_SECONDS = 5 * 60;

    private static final int DEFAULT_MAX_HTTP_BODY_LENGTH_IN_BYTES = 6 * 1024 * 1024; //6MB
    private static final int DEFAULT_MAX_HTTP_INITIAL_LINE_LENGTH = 4096; //4KB
    private static final int DEFAULT_MAX_HTTP_CHUNK_SIZE_IN_BYTES = 8192; //8KB
    private static final int DEFAULT_MAX_HTTP_REQUEST_HEADER_SIZE = 32 * 1024; //32 KB

    private static final int MAX_NUMBER_OF_READ_BARRIER_READ_RETRIES = 6;
    private static final int MAX_NUMBER_OF_PRIMARY_READ_RETRIES = 6;
    private static final int MAX_NUMBER_OF_READ_QUORUM_RETRIES = 6;
    private static final int DELAY_BETWEEN_READ_BARRIER_CALLS_IN_MS = 5;

    private static final int MAX_BARRIER_RETRIES_FOR_MULTI_REGION = 30;
    private static final int BARRIER_RETRY_INTERVAL_IN_MS_FOR_MULTI_REGION = 30;

    private static final int MAX_SHORT_BARRIER_RETRIES_FOR_MULTI_REGION = 4;
    private static final int SHORT_BARRIER_RETRY_INTERVAL_IN_MS_FOR_MULTI_REGION = 10;
    private static final int CPU_CNT = Runtime.getRuntime().availableProcessors();
    private static final int DEFAULT_DIRECT_HTTPS_POOL_SIZE = CPU_CNT * 500;
    private static final int DEFAULT_GLOBAL_ENDPOINT_MANAGER_INITIALIZATION_TIME_IN_SECONDS = 2 * 60;

    //  Reactor Netty Constants
    private static final Duration MAX_IDLE_CONNECTION_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration CONNECTION_ACQUIRE_TIMEOUT = Duration.ofSeconds(45);
    private static final String REACTOR_NETTY_CONNECTION_POOL_NAME = "reactor-netty-connection-pool";
    private static final int DEFAULT_HTTP_RESPONSE_TIMEOUT_IN_SECONDS = 60;
    private static final int DEFAULT_QUERY_PLAN_RESPONSE_TIMEOUT_IN_SECONDS = 5;
    private static final int DEFAULT_ADDRESS_REFRESH_RESPONSE_TIMEOUT_IN_SECONDS = 5;

    // SessionTokenMismatchRetryPolicy Constants
    public static final String DEFAULT_SESSION_TOKEN_MISMATCH_WAIT_TIME_IN_MILLISECONDS_NAME =
        "COSMOS.DEFAULT_SESSION_TOKEN_MISMATCH_WAIT_TIME_IN_MILLISECONDS";
    private static final int DEFAULT_SESSION_TOKEN_MISMATCH_WAIT_TIME_IN_MILLISECONDS = 5000;

    public static final String DEFAULT_SESSION_TOKEN_MISMATCH_INITIAL_BACKOFF_TIME_IN_MILLISECONDS_NAME =
        "COSMOS.DEFAULT_SESSION_TOKEN_MISMATCH_INITIAL_BACKOFF_TIME_IN_MILLISECONDS";
    private static final int DEFAULT_SESSION_TOKEN_MISMATCH_INITIAL_BACKOFF_TIME_IN_MILLISECONDS = 5;

    public static final String DEFAULT_SESSION_TOKEN_MISMATCH_MAXIMUM_BACKOFF_TIME_IN_MILLISECONDS_NAME =
        "COSMOS.DEFAULT_SESSION_TOKEN_MISMATCH_MAXIMUM_BACKOFF_TIME_IN_MILLISECONDS";
    private static final int DEFAULT_SESSION_TOKEN_MISMATCH_MAXIMUM_BACKOFF_TIME_IN_MILLISECONDS = 500;

    public static final int MIN_MIN_IN_REGION_RETRY_TIME_FOR_WRITES_MS = 100;

    private static final String DEFAULT_MIN_IN_REGION_RETRY_TIME_FOR_WRITES_MS_NAME =
        "COSMOS.DEFAULT_SESSION_TOKEN_MISMATCH_IN_REGION-RETRY_TIME_IN_MILLISECONDS";
    private static final int DEFAULT_MIN_IN_REGION_RETRY_TIME_FOR_WRITES_MS = 500;

    // RegionScopedSessionContainer related constants
    public static final String SESSION_CAPTURING_TYPE = "COSMOS.SESSION_CAPTURING_TYPE";
    public static final String DEFAULT_SESSION_CAPTURING_TYPE = StringUtils.EMPTY;
    public static final String PK_BASED_BLOOM_FILTER_EXPECTED_INSERTION_COUNT_NAME = "COSMOS.PK_BASED_BLOOM_FILTER_EXPECTED_INSERTION_COUNT";
    private static final long DEFAULT_PK_BASED_BLOOM_FILTER_EXPECTED_INSERTION_COUNT = 5_000_000;
    public static final String PK_BASED_BLOOM_FILTER_EXPECTED_FFP_RATE_NAME = "COSMOS.PK_BASED_BLOOM_FILTER_EXPECTED_FFP_RATE";
    private static final double DEFAULT_PK_BASED_BLOOM_FILTER_EXPECTED_FFP_RATE = 0.001;

    // Whether to process the response on a different thread
    private static final String SWITCH_OFF_IO_THREAD_FOR_RESPONSE_NAME = "COSMOS.SWITCH_OFF_IO_THREAD_FOR_RESPONSE";
    private static final boolean DEFAULT_SWITCH_OFF_IO_THREAD_FOR_RESPONSE = false;

    // whether to allow query empty page diagnostics logging
    private static final String QUERY_EMPTY_PAGE_DIAGNOSTICS_ENABLED = "COSMOS.QUERY_EMPTY_PAGE_DIAGNOSTICS_ENABLED";
    private static final boolean DEFAULT_QUERY_EMPTY_PAGE_DIAGNOSTICS_ENABLED = false;

    // whether to use old tracing format instead of semantic profile
    private static final String USE_LEGACY_TRACING = "COSMOS.USE_LEGACY_TRACING";
    private static final boolean DEFAULT_USE_LEGACY_TRACING = false;

    // whether to enable replica addresses validation
    private static final String REPLICA_ADDRESS_VALIDATION_ENABLED = "COSMOS.REPLICA_ADDRESS_VALIDATION_ENABLED";
    private static final boolean DEFAULT_REPLICA_ADDRESS_VALIDATION_ENABLED = true;

    // Rntbd health check related config
    private static final String TCP_HEALTH_CHECK_TIMEOUT_DETECTION_ENABLED = "COSMOS.TCP_HEALTH_CHECK_TIMEOUT_DETECTION_ENABLED";
    private static final boolean DEFAULT_TCP_HEALTH_CHECK_TIMEOUT_DETECTION_ENABLED = true;

    private static final String MIN_CONNECTION_POOL_SIZE_PER_ENDPOINT = "COSMOS.MIN_CONNECTION_POOL_SIZE_PER_ENDPOINT";
    private static final int DEFAULT_MIN_CONNECTION_POOL_SIZE_PER_ENDPOINT = 1;

    private static final String MAX_TRACE_MESSAGE_LENGTH = "COSMOS.MAX_TRACE_MESSAGE_LENGTH";
    private static final int DEFAULT_MAX_TRACE_MESSAGE_LENGTH = 32 * 1024;

    private static final int MIN_MAX_TRACE_MESSAGE_LENGTH = 8 * 1024;

    private static final String AGGRESSIVE_WARMUP_CONCURRENCY = "COSMOS.AGGRESSIVE_WARMUP_CONCURRENCY";
    private static final int DEFAULT_AGGRESSIVE_WARMUP_CONCURRENCY = Configs.getCPUCnt();

    private static final String OPEN_CONNECTIONS_CONCURRENCY = "COSMOS.OPEN_CONNECTIONS_CONCURRENCY";
    private static final int DEFAULT_OPEN_CONNECTIONS_CONCURRENCY = 1;

    public static final String MAX_RETRIES_IN_LOCAL_REGION_WHEN_REMOTE_REGION_PREFERRED = "COSMOS.MAX_RETRIES_IN_LOCAL_REGION_WHEN_REMOTE_REGION_PREFERRED";
    private static final int DEFAULT_MAX_RETRIES_IN_LOCAL_REGION_WHEN_REMOTE_REGION_PREFERRED = 1;

    private static final String MAX_ITEM_COUNT_FOR_VECTOR_SEARCH = "COSMOS.MAX_ITEM_SIZE_FOR_VECTOR_SEARCH";
    public static final int DEFAULT_MAX_ITEM_COUNT_FOR_VECTOR_SEARCH = 50000;

    private static final String AZURE_COSMOS_DISABLE_NON_STREAMING_ORDER_BY = "COSMOS.AZURE_COSMOS_DISABLE_NON_STREAMING_ORDER_BY";

    private static final boolean DEFAULT_AZURE_COSMOS_DISABLE_NON_STREAMING_ORDER_BY = false;

    public static final int MIN_MAX_RETRIES_IN_LOCAL_REGION_WHEN_REMOTE_REGION_PREFERRED = 1;

    public static final String TCP_CONNECTION_ACQUISITION_TIMEOUT_IN_MS = "COSMOS.TCP_CONNECTION_ACQUISITION_TIMEOUT_IN_MS";

    // Error handling strategy in diagnostics provider
    public static final String DIAGNOSTICS_PROVIDER_SYSTEM_EXIT_ON_ERROR = "COSMOS.DIAGNOSTICS_PROVIDER_SYSTEM_EXIT_ON_ERROR";
    public static final boolean DEFAULT_DIAGNOSTICS_PROVIDER_SYSTEM_EXIT_ON_ERROR = true;

    // Out-of-the-box it is possible to create documents with invalid character '/' in the id field
    // Client and service will just allow creating these documents - but no read, replace, patch or delete operation
    // can be done for these documents because the resulting request uri
    // "dbs/DBNAME/cols/CONTAINERNAME/docs/IDVALUE" would become invalid
    // Adding a validation to prevent the '/' in the id value would be breaking (for service and client)
    // but for some workloads there is a vested interest in failing early if someone tries to create documents
    // with invalid id value = the environment variable changes below
    // allow opting into a validation client-side. If this becomes used more frequently we might need to create
    // a public API for it as well.
    public static final String PREVENT_INVALID_ID_CHARS = "COSMOS.PREVENT_INVALID_ID_CHARS";
    public static final String PREVENT_INVALID_ID_CHARS_VARIABLE = "COSMOS_PREVENT_INVALID_ID_CHARS";
    public static final boolean DEFAULT_PREVENT_INVALID_ID_CHARS = false;


    // Metrics
    // Samples:
    //            System.setProperty(
    //                "COSMOS.METRICS_CONFIG",
    //                "{\"metricCategories\":\"[OperationSummary, RequestSummary]\","
    //                + "\"tagNames\":\"[Container, Operation]\","
    //                + "\"sampleRate\":0.5,"
    //                + "\"percentiles\":[0.90,0.99],"
    //                + "\"enableHistograms\":false,"
    //                + "\"applyDiagnosticThresholdsForTransportLevelMeters\":true}");
    public static final String METRICS_CONFIG = "COSMOS.METRICS_CONFIG";
    public static final String DEFAULT_METRICS_CONFIG = CosmosMicrometerMetricsConfig.DEFAULT.toJson();

    public Configs() {
        this.sslContext = sslContextInit();
    }

    public static int getCPUCnt() {
        return CPU_CNT;
    }

    private SslContext sslContextInit() {
        try {
            SslProvider sslProvider = SslContext.defaultClientProvider();
            return SslContextBuilder.forClient().sslProvider(sslProvider).build();
        } catch (SSLException sslException) {
            logger.error("Fatal error cannot instantiate ssl context due to {}", sslException.getMessage(), sslException);
            throw new IllegalStateException(sslException);
        }
    }

    public SslContext getSslContext() {
        return this.sslContext;
    }

    public Protocol getProtocol() {
        String protocol = System.getProperty(PROTOCOL_PROPERTY, firstNonNull(
            emptyToNull(System.getenv().get(PROTOCOL_ENVIRONMENT_VARIABLE)),
            DEFAULT_PROTOCOL.name()));
        try {
            return Protocol.valueOf(protocol.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            logger.error("Parsing protocol {} failed. Using the default {}.", protocol, DEFAULT_PROTOCOL, e);
            return DEFAULT_PROTOCOL;
        }
    }

    public int getMaxNumberOfReadBarrierReadRetries() {
        return MAX_NUMBER_OF_READ_BARRIER_READ_RETRIES;
    }

    public int getMaxNumberOfPrimaryReadRetries() {
        return MAX_NUMBER_OF_PRIMARY_READ_RETRIES;
    }

    public int getMaxNumberOfReadQuorumRetries() {
        return MAX_NUMBER_OF_READ_QUORUM_RETRIES;
    }

    public int getDelayBetweenReadBarrierCallsInMs() {
        return DELAY_BETWEEN_READ_BARRIER_CALLS_IN_MS;
    }

    public int getMaxBarrierRetriesForMultiRegion() {
        return MAX_BARRIER_RETRIES_FOR_MULTI_REGION;
    }

    public int getBarrierRetryIntervalInMsForMultiRegion() {
        return BARRIER_RETRY_INTERVAL_IN_MS_FOR_MULTI_REGION;
    }

    public int getMaxShortBarrierRetriesForMultiRegion() {
        return MAX_SHORT_BARRIER_RETRIES_FOR_MULTI_REGION;
    }

    public int getShortBarrierRetryIntervalInMsForMultiRegion() {
        return SHORT_BARRIER_RETRY_INTERVAL_IN_MS_FOR_MULTI_REGION;
    }

    public int getDirectHttpsMaxConnectionLimit() {
        return getJVMConfigAsInt(MAX_DIRECT_HTTPS_POOL_SIZE, DEFAULT_DIRECT_HTTPS_POOL_SIZE);
    }

    public int getMaxHttpHeaderSize() {
        return getJVMConfigAsInt(MAX_HTTP_HEADER_SIZE_IN_BYTES, DEFAULT_MAX_HTTP_REQUEST_HEADER_SIZE);
    }

    public int getMaxHttpInitialLineLength() {
        return getJVMConfigAsInt(MAX_HTTP_INITIAL_LINE_LENGTH_IN_BYTES, DEFAULT_MAX_HTTP_INITIAL_LINE_LENGTH);
    }

    public int getMaxHttpChunkSize() {
        return getJVMConfigAsInt(MAX_HTTP_CHUNK_SIZE_IN_BYTES, DEFAULT_MAX_HTTP_CHUNK_SIZE_IN_BYTES);
    }

    public int getMaxHttpBodyLength() {
        return getJVMConfigAsInt(MAX_HTTP_BODY_LENGTH_IN_BYTES, DEFAULT_MAX_HTTP_BODY_LENGTH_IN_BYTES);
    }

    public int getUnavailableLocationsExpirationTimeInSeconds() {
        return getJVMConfigAsInt(UNAVAILABLE_LOCATIONS_EXPIRATION_TIME_IN_SECONDS, DEFAULT_UNAVAILABLE_LOCATIONS_EXPIRATION_TIME_IN_SECONDS);
    }

    public static int getClientTelemetrySchedulingInSec() {
        return getJVMConfigAsInt(CLIENT_TELEMETRY_SCHEDULING_IN_SECONDS, DEFAULT_CLIENT_TELEMETRY_SCHEDULING_IN_SECONDS);
    }

    public int getGlobalEndpointManagerMaxInitializationTimeInSeconds() {
        return getJVMConfigAsInt(GLOBAL_ENDPOINT_MANAGER_INITIALIZATION_TIME_IN_SECONDS, DEFAULT_GLOBAL_ENDPOINT_MANAGER_INITIALIZATION_TIME_IN_SECONDS);
    }

    public String getReactorNettyConnectionPoolName() {
        return REACTOR_NETTY_CONNECTION_POOL_NAME;
    }

    public Duration getMaxIdleConnectionTimeout() {
        return MAX_IDLE_CONNECTION_TIMEOUT;
    }

    public Duration getConnectionAcquireTimeout() {
        return CONNECTION_ACQUIRE_TIMEOUT;
    }

    public static int getHttpResponseTimeoutInSeconds() {
        return getJVMConfigAsInt(HTTP_RESPONSE_TIMEOUT_IN_SECONDS, DEFAULT_HTTP_RESPONSE_TIMEOUT_IN_SECONDS);
    }

    public static int getQueryPlanResponseTimeoutInSeconds() {
        return getJVMConfigAsInt(QUERY_PLAN_RESPONSE_TIMEOUT_IN_SECONDS, DEFAULT_QUERY_PLAN_RESPONSE_TIMEOUT_IN_SECONDS);
    }

    public static String getClientTelemetryEndpoint() {
        return System.getProperty(CLIENT_TELEMETRY_ENDPOINT);
    }

    public static String getClientTelemetryProxyOptionsConfig() {
        return System.getProperty(CLIENT_TELEMETRY_PROXY_OPTIONS_CONFIG);
    }

    public static String getNonIdempotentWriteRetryPolicy() {
        String valueFromSystemProperty = System.getProperty(NON_IDEMPOTENT_WRITE_RETRY_POLICY);
        if (valueFromSystemProperty != null && !valueFromSystemProperty.isEmpty()) {
            return valueFromSystemProperty;
        }

        return System.getenv(NON_IDEMPOTENT_WRITE_RETRY_POLICY_VARIABLE);
    }

    public static int getDefaultHttpPoolSize() {
        String valueFromSystemProperty = System.getProperty(HTTP_DEFAULT_CONNECTION_POOL_SIZE);
        if (valueFromSystemProperty != null && !valueFromSystemProperty.isEmpty()) {
            return Integer.valueOf(valueFromSystemProperty);
        }

        String valueFromEnvVariable = System.getenv(HTTP_DEFAULT_CONNECTION_POOL_SIZE_VARIABLE);
        if (valueFromEnvVariable != null && !valueFromEnvVariable.isEmpty()) {
            return Integer.valueOf(valueFromEnvVariable);
        }

        return DEFAULT_HTTP_DEFAULT_CONNECTION_POOL_SIZE;
    }

    public static boolean isDefaultE2ETimeoutDisabledForNonPointOperations() {
        String valueFromSystemProperty = System.getProperty(DEFAULT_E2E_FOR_NON_POINT_DISABLED);
        if (valueFromSystemProperty != null && !valueFromSystemProperty.isEmpty()) {
            return Boolean.valueOf(valueFromSystemProperty);
        }

        String valueFromEnvVariable = System.getenv(DEFAULT_E2E_FOR_NON_POINT_DISABLED_VARIABLE);
        if (valueFromEnvVariable != null && !valueFromEnvVariable.isEmpty()) {
            return Boolean.valueOf(valueFromEnvVariable);
        }

        return DEFAULT_E2E_FOR_NON_POINT_DISABLED_DEFAULT;
    }

    public static boolean isIdValueValidationEnabled() {
        String valueFromSystemProperty = System.getProperty(PREVENT_INVALID_ID_CHARS);
        if (valueFromSystemProperty != null && !valueFromSystemProperty.isEmpty()) {
            return !Boolean.valueOf(valueFromSystemProperty);
        }

        String valueFromEnvVariable = System.getenv(PREVENT_INVALID_ID_CHARS_VARIABLE);
        if (valueFromEnvVariable != null && !valueFromEnvVariable.isEmpty()) {
            return!Boolean.valueOf(valueFromEnvVariable);
        }

        return DEFAULT_PREVENT_INVALID_ID_CHARS;
    }

    public static int getMaxHttpRequestTimeout() {
        String valueFromSystemProperty = System.getProperty(HTTP_MAX_REQUEST_TIMEOUT);
        if (valueFromSystemProperty != null && !valueFromSystemProperty.isEmpty()) {
            return Integer.valueOf(valueFromSystemProperty);
        }

        String valueFromEnvVariable = System.getenv(HTTP_MAX_REQUEST_TIMEOUT_VARIABLE);
        if (valueFromEnvVariable != null && !valueFromEnvVariable.isEmpty()) {
            return Integer.valueOf(valueFromEnvVariable);
        }

        return DEFAULT_HTTP_MAX_REQUEST_TIMEOUT;
    }

    public static String getEnvironmentName() {
        return System.getProperty(ENVIRONMENT_NAME);
    }

    public static boolean isQueryPlanCachingEnabled() {
        // Queryplan caching is enabled by default
        return getJVMConfigAsBoolean(QUERYPLAN_CACHING_ENABLED, true);
    }

    public static int getAddressRefreshResponseTimeoutInSeconds() {
        return getJVMConfigAsInt(ADDRESS_REFRESH_RESPONSE_TIMEOUT_IN_SECONDS, DEFAULT_ADDRESS_REFRESH_RESPONSE_TIMEOUT_IN_SECONDS);
    }

    public static int getSessionTokenMismatchDefaultWaitTimeInMs() {
        return getJVMConfigAsInt(
            DEFAULT_SESSION_TOKEN_MISMATCH_WAIT_TIME_IN_MILLISECONDS_NAME,
            DEFAULT_SESSION_TOKEN_MISMATCH_WAIT_TIME_IN_MILLISECONDS);
    }

    public static int getSessionTokenMismatchInitialBackoffTimeInMs() {
        return getJVMConfigAsInt(
            DEFAULT_SESSION_TOKEN_MISMATCH_INITIAL_BACKOFF_TIME_IN_MILLISECONDS_NAME,
            DEFAULT_SESSION_TOKEN_MISMATCH_INITIAL_BACKOFF_TIME_IN_MILLISECONDS);
    }

    public static int getSessionTokenMismatchMaximumBackoffTimeInMs() {
        return getJVMConfigAsInt(
            DEFAULT_SESSION_TOKEN_MISMATCH_MAXIMUM_BACKOFF_TIME_IN_MILLISECONDS_NAME,
            DEFAULT_SESSION_TOKEN_MISMATCH_MAXIMUM_BACKOFF_TIME_IN_MILLISECONDS);
    }

    public static int getSpeculationType() {
        return getJVMConfigAsInt(SPECULATION_TYPE, 0);
    }

    public static int speculationThreshold() {
        return getJVMConfigAsInt(SPECULATION_THRESHOLD, 500);
    }

    public static int speculationThresholdStep() {
        return getJVMConfigAsInt(SPECULATION_THRESHOLD_STEP, 100);
    }

    public static boolean shouldSwitchOffIOThreadForResponse() {
        return getJVMConfigAsBoolean(
            SWITCH_OFF_IO_THREAD_FOR_RESPONSE_NAME,
            DEFAULT_SWITCH_OFF_IO_THREAD_FOR_RESPONSE);
    }

    public static boolean isEmptyPageDiagnosticsEnabled() {
        return getJVMConfigAsBoolean(
            QUERY_EMPTY_PAGE_DIAGNOSTICS_ENABLED,
            DEFAULT_QUERY_EMPTY_PAGE_DIAGNOSTICS_ENABLED);
    }

    public static boolean useLegacyTracing() {
        return getJVMConfigAsBoolean(
            USE_LEGACY_TRACING,
            DEFAULT_USE_LEGACY_TRACING);
    }

    private static int getJVMConfigAsInt(String propName, int defaultValue) {
        String propValue = System.getProperty(propName);
        return getIntValue(propValue, defaultValue);
    }

    private static boolean getJVMConfigAsBoolean(String propName, boolean defaultValue) {
        String propValue = System.getProperty(propName);
        return getBooleanValue(propValue, defaultValue);
    }

    private static int getIntValue(String val, int defaultValue) {
        if (StringUtils.isEmpty(val)) {
            return defaultValue;
        } else {
            return Integer.valueOf(val);
        }
    }

    private static boolean getBooleanValue(String val, boolean defaultValue) {
        if (StringUtils.isEmpty(val)) {
            return defaultValue;
        } else {
            return Boolean.valueOf(val);
        }
    }

    public static boolean isReplicaAddressValidationEnabled() {
        return getJVMConfigAsBoolean(
                REPLICA_ADDRESS_VALIDATION_ENABLED,
                DEFAULT_REPLICA_ADDRESS_VALIDATION_ENABLED);
    }

    public static boolean isTcpHealthCheckTimeoutDetectionEnabled() {
        return getJVMConfigAsBoolean(
            TCP_HEALTH_CHECK_TIMEOUT_DETECTION_ENABLED,
            DEFAULT_TCP_HEALTH_CHECK_TIMEOUT_DETECTION_ENABLED);
    }

    public static int getMinConnectionPoolSizePerEndpoint() {
        return getIntValue(System.getProperty(MIN_CONNECTION_POOL_SIZE_PER_ENDPOINT), DEFAULT_MIN_CONNECTION_POOL_SIZE_PER_ENDPOINT);
    }

    public static int getOpenConnectionsConcurrency() {
        return getIntValue(System.getProperty(OPEN_CONNECTIONS_CONCURRENCY), DEFAULT_OPEN_CONNECTIONS_CONCURRENCY);
    }

    public static int getAggressiveWarmupConcurrency() {
        return getIntValue(System.getProperty(AGGRESSIVE_WARMUP_CONCURRENCY), DEFAULT_AGGRESSIVE_WARMUP_CONCURRENCY);
    }

    public static int getMaxRetriesInLocalRegionWhenRemoteRegionPreferred() {
        return
            Math.max(
                getIntValue(
                    System.getProperty(MAX_RETRIES_IN_LOCAL_REGION_WHEN_REMOTE_REGION_PREFERRED),
                    DEFAULT_MAX_RETRIES_IN_LOCAL_REGION_WHEN_REMOTE_REGION_PREFERRED),
                MIN_MAX_RETRIES_IN_LOCAL_REGION_WHEN_REMOTE_REGION_PREFERRED);
    }

    public static int getMaxItemCountForVectorSearch() {
        return Integer.parseInt(System.getProperty(MAX_ITEM_COUNT_FOR_VECTOR_SEARCH,
             firstNonNull(
                emptyToNull(System.getenv().get(MAX_ITEM_COUNT_FOR_VECTOR_SEARCH)),
                String.valueOf(DEFAULT_MAX_ITEM_COUNT_FOR_VECTOR_SEARCH))));
    }

    public static boolean getAzureCosmosNonStreamingOrderByDisabled() {
        if(logger.isTraceEnabled()) {
            logger.trace(
                "AZURE_COSMOS_DISABLE_NON_STREAMING_ORDER_BY property is: {}",
                System.getProperty(AZURE_COSMOS_DISABLE_NON_STREAMING_ORDER_BY));
            logger.trace(
                "AZURE_COSMOS_DISABLE_NON_STREAMING_ORDER_BY env variable is: {}",
                System.getenv().get(AZURE_COSMOS_DISABLE_NON_STREAMING_ORDER_BY));
        }
        return Boolean.parseBoolean(System.getProperty(AZURE_COSMOS_DISABLE_NON_STREAMING_ORDER_BY,
            firstNonNull(
                emptyToNull(System.getenv().get(AZURE_COSMOS_DISABLE_NON_STREAMING_ORDER_BY)),
                String.valueOf(DEFAULT_AZURE_COSMOS_DISABLE_NON_STREAMING_ORDER_BY))));
    }

    public static Duration getMinRetryTimeInLocalRegionWhenRemoteRegionPreferred() {
        return
            Duration.ofMillis(Math.max(
                getIntValue(
                    System.getProperty(DEFAULT_MIN_IN_REGION_RETRY_TIME_FOR_WRITES_MS_NAME),
                    DEFAULT_MIN_IN_REGION_RETRY_TIME_FOR_WRITES_MS),
                MIN_MIN_IN_REGION_RETRY_TIME_FOR_WRITES_MS));
    }

    public static int getMaxTraceMessageLength() {
        return
            Math.max(
                getIntValue(
                    System.getProperty(MAX_TRACE_MESSAGE_LENGTH),
                    DEFAULT_MAX_TRACE_MESSAGE_LENGTH),
                MIN_MAX_TRACE_MESSAGE_LENGTH);
    }

    public static Duration getTcpConnectionAcquisitionTimeout(int defaultValueInMs) {
        return Duration.ofMillis(
            getIntValue(
                System.getProperty(TCP_CONNECTION_ACQUISITION_TIMEOUT_IN_MS),
                defaultValueInMs
            )
        );
    }

    public static String getSessionCapturingType() {

        return System.getProperty(
            SESSION_CAPTURING_TYPE,
            firstNonNull(
                emptyToNull(System.getenv().get(SESSION_CAPTURING_TYPE)),
                DEFAULT_SESSION_CAPTURING_TYPE));
    }

    public static long getPkBasedBloomFilterExpectedInsertionCount() {

        String pkBasedBloomFilterExpectedInsertionCount = System.getProperty(
            PK_BASED_BLOOM_FILTER_EXPECTED_INSERTION_COUNT_NAME,
            firstNonNull(
                emptyToNull(System.getenv().get(PK_BASED_BLOOM_FILTER_EXPECTED_INSERTION_COUNT_NAME)),
                String.valueOf(DEFAULT_PK_BASED_BLOOM_FILTER_EXPECTED_INSERTION_COUNT)));

        return Long.parseLong(pkBasedBloomFilterExpectedInsertionCount);
    }

    public static double getPkBasedBloomFilterExpectedFfpRate() {
        String pkBasedBloomFilterExpectedFfpRate = System.getProperty(
            PK_BASED_BLOOM_FILTER_EXPECTED_FFP_RATE_NAME,
            firstNonNull(
                emptyToNull(System.getenv().get(PK_BASED_BLOOM_FILTER_EXPECTED_FFP_RATE_NAME)),
                String.valueOf(DEFAULT_PK_BASED_BLOOM_FILTER_EXPECTED_FFP_RATE)));

        return Double.parseDouble(pkBasedBloomFilterExpectedFfpRate);
    }

    public static boolean shouldDiagnosticsProviderSystemExitOnError() {
        String shouldSystemExit =
            System.getProperty(
                DIAGNOSTICS_PROVIDER_SYSTEM_EXIT_ON_ERROR,
                firstNonNull(
                    emptyToNull(System.getenv().get(DIAGNOSTICS_PROVIDER_SYSTEM_EXIT_ON_ERROR)),
                    String.valueOf(DEFAULT_DIAGNOSTICS_PROVIDER_SYSTEM_EXIT_ON_ERROR)));

        return Boolean.parseBoolean(shouldSystemExit);
    }

    public static CosmosMicrometerMetricsConfig getMetricsConfig() {
        String metricsConfig =
            System.getProperty(
                METRICS_CONFIG,
                firstNonNull(
                    emptyToNull(System.getenv().get(METRICS_CONFIG)),
                    DEFAULT_METRICS_CONFIG));

        return CosmosMicrometerMetricsConfig.fromJsonString(metricsConfig);
    }
}
