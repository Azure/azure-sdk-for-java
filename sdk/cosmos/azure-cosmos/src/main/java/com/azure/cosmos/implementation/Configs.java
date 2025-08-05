// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.clienttelemetry.AttributeNamingScheme;
import com.azure.cosmos.implementation.perPartitionCircuitBreaker.PartitionLevelCircuitBreakerConfig;
import com.azure.cosmos.implementation.directconnectivity.Protocol;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.net.URI;
import java.time.Duration;
import java.util.EnumSet;
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

    // The names we use are consistent with the:
    // * Azure environment variable naming conventions documented at https://azure.github.io/azure-sdk/java_implementation.html and
    // * Java property naming conventions as illustrated by the name/value pairs returned by System.getProperties.

    private static final String PROTOCOL_ENVIRONMENT_VARIABLE = "AZURE_COSMOS_DIRECT_MODE_PROTOCOL";
    private static final String PROTOCOL_PROPERTY = "azure.cosmos.directModeProtocol";
    private static final Protocol DEFAULT_PROTOCOL = Protocol.TCP;

    private static final String UNAVAILABLE_LOCATIONS_EXPIRATION_TIME_IN_SECONDS = "COSMOS.UNAVAILABLE_LOCATIONS_EXPIRATION_TIME_IN_SECONDS";
    private static final String GLOBAL_ENDPOINT_MANAGER_INITIALIZATION_TIME_IN_SECONDS = "COSMOS.GLOBAL_ENDPOINT_MANAGER_MAX_INIT_TIME_IN_SECONDS";
    private static final String DEFAULT_THINCLIENT_ENDPOINT = "";
    private static final String THINCLIENT_ENDPOINT = "COSMOS.THINCLIENT_ENDPOINT";
    private static final String THINCLIENT_ENDPOINT_VARIABLE = "COSMOS_THINCLIENT_ENDPOINT";
    private static final boolean DEFAULT_THINCLIENT_ENABLED = false;
    private static final String THINCLIENT_ENABLED = "COSMOS.THINCLIENT_ENABLED";
    private static final String THINCLIENT_ENABLED_VARIABLE = "COSMOS_THINCLIENT_ENABLED";

    private static final String MAX_HTTP_BODY_LENGTH_IN_BYTES = "COSMOS.MAX_HTTP_BODY_LENGTH_IN_BYTES";
    private static final String MAX_HTTP_INITIAL_LINE_LENGTH_IN_BYTES = "COSMOS.MAX_HTTP_INITIAL_LINE_LENGTH_IN_BYTES";
    private static final String MAX_HTTP_CHUNK_SIZE_IN_BYTES = "COSMOS.MAX_HTTP_CHUNK_SIZE_IN_BYTES";
    private static final String MAX_HTTP_HEADER_SIZE_IN_BYTES = "COSMOS.MAX_HTTP_HEADER_SIZE_IN_BYTES";
    private static final String MAX_DIRECT_HTTPS_POOL_SIZE = "COSMOS.MAX_DIRECT_HTTP_CONNECTION_LIMIT";
    private static final String HTTP_RESPONSE_TIMEOUT_IN_SECONDS = "COSMOS.HTTP_RESPONSE_TIMEOUT_IN_SECONDS";

    public static final int DEFAULT_HTTP_DEFAULT_CONNECTION_POOL_SIZE = 1000;
    public static final String HTTP_DEFAULT_CONNECTION_POOL_SIZE = "COSMOS.DEFAULT_HTTP_CONNECTION_POOL_SIZE";
    public static final String HTTP_DEFAULT_CONNECTION_POOL_SIZE_VARIABLE = "COSMOS_DEFAULT_HTTP_CONNECTION_POOL_SIZE";

    public static final String HTTP_PENDING_ACQUIRE_MAX_COUNT = "COSMOS.HTTP_PENDING_ACQUIRE_MAX_COUNT";
    public static final String HTTP_PENDING_ACQUIRE_MAX_COUNT_VARIABLE = "COSMOS_HTTP_PENDING_ACQUIRE_MAX_COUNT";

    public static final String ITEM_SERIALIZATION_INCLUSION_MODE = "COSMOS.ITEM_SERIALIZATION_INCLUSION_MODE";
    public static final String ITEM_SERIALIZATION_INCLUSION_MODE_VARIABLE = "COSMOS_ITEM_SERIALIZATION_INCLUSION_MODE";

    public static final String DEFAULT_ITEM_SERIALIZATION_INCLUSION_MODE = "Always";

    public static final boolean DEFAULT_E2E_FOR_NON_POINT_DISABLED_DEFAULT = false;
    public static final String DEFAULT_E2E_FOR_NON_POINT_DISABLED = "COSMOS.E2E_FOR_NON_POINT_DISABLED";
    public static final String DEFAULT_E2E_FOR_NON_POINT_DISABLED_VARIABLE = "COSMOS_E2E_FOR_NON_POINT_DISABLED";

    public static final int DEFAULT_HTTP_MAX_REQUEST_TIMEOUT = 60;
    public static final String HTTP_MAX_REQUEST_TIMEOUT = "COSMOS.HTTP_MAX_REQUEST_TIMEOUT";
    public static final String HTTP_MAX_REQUEST_TIMEOUT_VARIABLE = "COSMOS_HTTP_MAX_REQUEST_TIMEOUT";

    private static final String QUERY_PLAN_RESPONSE_TIMEOUT_IN_SECONDS = "COSMOS.QUERY_PLAN_RESPONSE_TIMEOUT_IN_SECONDS";
    private static final String ADDRESS_REFRESH_RESPONSE_TIMEOUT_IN_SECONDS = "COSMOS.ADDRESS_REFRESH_RESPONSE_TIMEOUT_IN_SECONDS";

    private static final String AAD_SCOPE_OVERRIDE = "COSMOS.AAD_SCOPE_OVERRIDE";
    private static final String AAD_SCOPE_OVERRIDE_VARIABLE = "COSMOS_AAD_SCOPE_OVERRIDE";
    private static final String DEFAULT_AAD_SCOPE_OVERRIDE = "";

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

    private static final String MAX_ITEM_COUNT_FOR_HYBRID_SEARCH = "COSMOS.MAX_ITEM_SIZE_FOR_HYBRID_SEARCH";
    private static final String MAX_ITEM_COUNT_FOR_HYBRID_SEARCH_VARIABLE = "COSMOS_MAX_ITEM_SIZE_FOR_HYBRID_SEARCH";
    public static final int DEFAULT_MAX_ITEM_COUNT_FOR_HYBRID_SEARCH = 1000;

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

    // Bulk default settings
    public static final String MIN_TARGET_BULK_MICRO_BATCH_SIZE = "COSMOS.MIN_TARGET_BULK_MICRO_BATCH_SIZE";
    public static final String MIN_TARGET_BULK_MICRO_BATCH_SIZE_VARIABLE = "COSMOS_MIN_TARGET_BULK_MICRO_BATCH_SIZE";
    public static final int DEFAULT_MIN_TARGET_BULK_MICRO_BATCH_SIZE = 1;

    public static final String MAX_BULK_MICRO_BATCH_CONCURRENCY = "COSMOS.MAX_BULK_MICRO_BATCH_CONCURRENCY";
    public static final String MAX_BULK_MICRO_BATCH_CONCURRENCY_VARIABLE = "COSMOS_MAX_BULK_MICRO_BATCH_CONCURRENCY";
    public static final int DEFAULT_MAX_BULK_MICRO_BATCH_CONCURRENCY = 1;

    public static final String MAX_BULK_MICRO_BATCH_FLUSH_INTERVAL_IN_MILLISECONDS = "COSMOS.MAX_BULK_MICRO_BATCH_FLUSH_INTERVAL_IN_MILLISECONDS";
    public static final String MAX_BULK_MICRO_BATCH_FLUSH_INTERVAL_IN_MILLISECONDS_VARIABLE = "COSMOS_MAX_BULK_MICRO_BATCH_FLUSH_INTERVAL_IN_MILLISECONDS";
    public static final int DEFAULT_MAX_BULK_MICRO_BATCH_FLUSH_INTERVAL_IN_MILLISECONDS = 1000;

    // Config of CodingErrorAction on charset decoder for malformed input
    public static final String CHARSET_DECODER_ERROR_ACTION_ON_MALFORMED_INPUT = "COSMOS.CHARSET_DECODER_ERROR_ACTION_ON_MALFORMED_INPUT";
    public static final String DEFAULT_CHARSET_DECODER_ERROR_ACTION_ON_MALFORMED_INPUT = StringUtils.EMPTY;

    // Config of CodingErrorAction on charset decoder for unmapped character
    public static final String CHARSET_DECODER_ERROR_ACTION_ON_UNMAPPED_CHARACTER = "COSMOS.CHARSET_DECODER_ERROR_ACTION_ON_UNMAPPED_CHARACTER";
    public static final String DEFAULT_CHARSET_DECODER_ERROR_ACTION_ON_UNMAPPED_CHARACTER = StringUtils.EMPTY;

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

    // For partition-level circuit breaker, below config will set the tolerated consecutive exception counts
    // for reads and writes for a given partition before being marked as Unavailable
    private static final String DEFAULT_PARTITION_LEVEL_CIRCUIT_BREAKER_CONFIG = PartitionLevelCircuitBreakerConfig.DEFAULT.toJson();
    private static final String PARTITION_LEVEL_CIRCUIT_BREAKER_CONFIG = "COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_CONFIG";
    private static final String STALE_COLLECTION_CACHE_REFRESH_RETRY_COUNT = "COSMOS.STALE_COLLECTION_CACHE_REFRESH_RETRY_COUNT";
    private static final int DEFAULT_STALE_COLLECTION_CACHE_REFRESH_RETRY_COUNT = 2;
    private static final String STALE_COLLECTION_CACHE_REFRESH_RETRY_INTERVAL_IN_SECONDS = "COSMOS.STALE_COLLECTION_CACHE_REFRESH_RETRY_INTERVAL_IN_SECONDS";
    private static final int DEFAULT_STALE_COLLECTION_CACHE_REFRESH_RETRY_INTERVAL_IN_SECONDS = 1;

    // For partition-level circuit breaker, a background thread will run periodically every y seconds at a minimum
    // in an attempt to recover Unavailable partitions
    private static final String STALE_PARTITION_UNAVAILABILITY_REFRESH_INTERVAL_IN_SECONDS = "COSMOS.STALE_PARTITION_UNAVAILABILITY_REFRESH_INTERVAL_IN_SECONDS";
    private static final int DEFAULT_STALE_PARTITION_UNAVAILABILITY_REFRESH_INTERVAL_IN_SECONDS = 60;

    // For partition-level circuit breaker, a partition can be allowed to be Unavailable for minimum of x seconds
    // as specified by the below setting after which a background thread will attempt to recover the partition
    private static final String ALLOWED_PARTITION_UNAVAILABILITY_DURATION_IN_SECONDS = "COSMOS.ALLOWED_PARTITION_UNAVAILABILITY_DURATION_IN_SECONDS";
    private static final int DEFAULT_ALLOWED_PARTITION_UNAVAILABILITY_DURATION_IN_SECONDS = 30;

    // For partition-level circuit breaker, in order to recover a partition in a region, the SDK when configured
    // in the direct connectivity mode, establishes connections to replicas to attempt to recover a region
    // Below sets a time limit on how long these connection establishments be attempted for
    private static final int DEFAULT_CONNECTION_ESTABLISHMENT_TIMEOUT_FOR_PARTITION_RECOVERY_IN_SECONDS = 10;
    private static final String CONNECTION_ESTABLISHMENT_TIMEOUT_FOR_PARTITION_RECOVERY_IN_SECONDS = "COSMOS.CONNECTION_ESTABLISHMENT_TIMEOUT_FOR_PARTITION_RECOVERY_IN_SECONDS";

    private static final boolean DEFAULT_SHOULD_LOG_INCORRECTLY_MAPPED_SESSION_TOKEN = true;
    private static final String SHOULD_LOG_INCORRECTLY_MAPPED_SESSION_TOKEN = "COSMOS.SHOULD_LOG_INCORRECTLY_MAPPED_USER_SESSION_TOKEN";

    private static final boolean DEFAULT_PARTITION_LEVEL_CIRCUIT_BREAKER_DEFAULT_CONFIG_OPT_IN = false;
    private static final String PARTITION_LEVEL_CIRCUIT_BREAKER_DEFAULT_CONFIG_OPT_IN = "COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_DEFAULT_CONFIG_OPT_IN";

    private static final String DEFAULT_IS_PER_PARTITION_AUTOMATIC_FAILOVER_ENABLED = "";
    private static final String IS_PER_PARTITION_AUTOMATIC_FAILOVER_ENABLED = "COSMOS.IS_PER_PARTITION_AUTOMATIC_FAILOVER_ENABLED";
    private static final String IS_PER_PARTITION_AUTOMATIC_FAILOVER_ENABLED_VARIABLE = "COSMOS_IS_PER_PARTITION_AUTOMATIC_FAILOVER_ENABLED";

    private static final boolean DEFAULT_SESSION_TOKEN_FALSE_PROGRESS_MERGE_ENABLED = true;
    private static final String IS_SESSION_TOKEN_FALSE_PROGRESS_MERGE_ENABLED = "COSMOS.IS_SESSION_TOKEN_FALSE_PROGRESS_MERGE_ENABLED";
    private static final String IS_SESSION_TOKEN_FALSE_PROGRESS_MERGE_ENABLED_VARIABLE = "COSMOS_IS_SESSION_TOKEN_FALSE_PROGRESS_MERGE_ENABLED";

    private static final int DEFAULT_E2E_TIMEOUT_ERROR_HIT_THRESHOLD_FOR_PPAF = 10;
    private static final String E2E_TIMEOUT_ERROR_HIT_THRESHOLD_FOR_PPAF = "COSMOS.E2E_TIMEOUT_ERROR_HIT_THRESHOLD_FOR_PPAF";
    private static final String E2E_TIMEOUT_ERROR_HIT_THRESHOLD_FOR_PPAF_VARIABLE = "COSMOS_E2E_TIMEOUT_ERROR_HIT_THRESHOLD_FOR_PPAF";

    private static final int DEFAULT_E2E_TIMEOUT_ERROR_HIT_TIME_WINDOW_IN_SECONDS_FOR_PPAF = 60;
    private static final String E2E_TIMEOUT_ERROR_HIT_TIME_WINDOW_IN_SECONDS_FOR_PPAF = "COSMOS.E2E_TIMEOUT_ERROR_HIT_TIME_WINDOW_IN_SECONDS_FOR_PPAF";
    private static final String E2E_TIMEOUT_ERROR_HIT_TIME_WINDOW_IN_SECONDS_FOR_PPAF_VARIABLE = "COSMOS_E2E_TIMEOUT_ERROR_HIT_TIME_WINDOW_IN_SECONDS_FOR_PPAF";

    private static final String DEFAULT_IS_READ_AVAILABILITY_STRATEGY_ENABLED_WITH_PPAF = "true";
    private static final String IS_READ_AVAILABILITY_STRATEGY_ENABLED_WITH_PPAF = "COSMOS.IS_READ_AVAILABILITY_STRATEGY_ENABLED_WITH_PPAF";
    private static final String IS_READ_AVAILABILITY_STRATEGY_ENABLED_WITH_PPAF_VARIABLE = "COSMOS_IS_READ_AVAILABILITY_STRATEGY_ENABLED_WITH_PPAF";

    private static final int DEFAULT_WARN_LEVEL_LOGGING_THRESHOLD_FOR_PPAF = 25;
    private static final String WARN_LEVEL_LOGGING_THRESHOLD_FOR_PPAF = "COSMOS.WARN_LEVEL_LOGGING_THRESHOLD_FOR_PPAF";
    private static final String WARN_LEVEL_LOGGING_THRESHOLD_FOR_PPAF_VARIABLE = "COSMOS_WARN_LEVEL_LOGGING_THRESHOLD_FOR_PPAF_VARIABLE";

    private static final String COSMOS_DISABLE_IMDS_ACCESS = "COSMOS.DISABLE_IMDS_ACCESS";
    private static final String COSMOS_DISABLE_IMDS_ACCESS_VARIABLE = "COSMOS_DISABLE_IMDS_ACCESS";
    private static final boolean COSMOS_DISABLE_IMDS_ACCESS_DEFAULT = false;

    // Config to indicate whether allow http connections
    // Please note that this config should only during development or test, please do not use in prod env
    private static final boolean DEFAULT_HTTP_CONNECTION_WITHOUT_TLS_ALLOWED = false;
    private static final String HTTP_CONNECTION_WITHOUT_TLS_ALLOWED = "COSMOS.HTTP_CONNECTION_WITHOUT_TLS_ALLOWED";
    private static final String HTTP_CONNECTION_WITHOUT_TLS_ALLOWED_VARIABLE = "COSMOS_HTTP_CONNECTION_WITHOUT_TLS_ALLOWED";

    // Config to indicate whether disable server certificate validation for emulator
    // Please note that this config should only during development or test, please do not use in prod env
    private static final boolean DEFAULT_EMULATOR_SERVER_CERTIFICATE_VALIDATION_DISABLED = false;
    private static final String EMULATOR_SERVER_CERTIFICATE_VALIDATION_DISABLED = "COSMOS.EMULATOR_SERVER_CERTIFICATE_VALIDATION_DISABLED";
    private static final String EMULATOR_SERVER_CERTIFICATE_VALIDATION_DISABLED_VARIABLE = "COSMOS_EMULATOR_SERVER_CERTIFICATE_VALIDATION_DISABLED";

    // Config to indicate emulator host name
    // Please note that this config should only during development or test, please do not use in prod env
    private static final String DEFAULT_EMULATOR_HOST = StringUtils.EMPTY;
    private static final String EMULATOR_HOST = "COSMOS.EMULATOR_HOST";
    private static final String EMULATOR_HOST_VARIABLE = "COSMOS_EMULATOR_HOST";

    // Flag to indicate whether enabled http2 for gateway
    private static final boolean DEFAULT_HTTP2_ENABLED = false;
    private static final String HTTP2_ENABLED = "COSMOS.HTTP2_ENABLED";
    private static final String HTTP2_ENABLED_VARIABLE = "COSMOS_HTTP2_ENABLED";

    // Config to indicate the maximum number of live connections to keep in the pool for http2
    private static final int DEFAULT_HTTP2_MAX_CONNECTION_POOL_SIZE = 1000;
    private static final String HTTP2_MAX_CONNECTION_POOL_SIZE = "COSMOS.HTTP2_MAX_CONNECTION_POOL_SIZE";
    private static final String HTTP2_MAX_CONNECTION_POOL_SIZE_VARIABLE = "COSMOS_HTTP2_MAX_CONNECTION_POOL_SIZE";

    // Config to indicate the minimum number of live connections to keep in the pool for http2
    private static final int DEFAULT_HTTP2_MIN_CONNECTION_POOL_SIZE = Math.max(CPU_CNT, 8);
    private static final String HTTP2_MIN_CONNECTION_POOL_SIZE = "COSMOS.HTTP2_MIN_CONNECTION_POOL_SIZE";
    private static final String HTTP2_MIN_CONNECTION_POOL_SIZE_VARIABLE = "COSMOS_HTTP2_MIN_CONNECTION_POOL_SIZE";

    // Config to indicate the maximum number of the concurrent streams that can be opened to the remote peer for http2
    private static final int DEFAULT_HTTP2_MAX_CONCURRENT_STREAMS = 30;
    private static final String HTTP2_MAX_CONCURRENT_STREAMS = "COSMOS.HTTP2_MAX_CONCURRENT_STREAMS";
    private static final String HTTP2_MAX_CONCURRENT_STREAMS_VARIABLE = "COSMOS_HTTP2_MAX_CONCURRENT_STREAMS";

    public static final String APPLICATIONINSIGHTS_CONNECTION_STRING = "applicationinsights.connection.string";
    public static final String APPLICATIONINSIGHTS_CONNECTION_STRING_VARIABLE = "APPLICATIONINSIGHTS_CONNECTION_STRING";

    // Config to indicate whether to emit Open Telemetry traces with attribute names following the
    // original implementation (`PRE_V1_RELEASE`) or the official semantic convention (`V1`) or both (`ALL`)
    public static final String OTEL_SPAN_ATTRIBUTE_NAMING_SCHEME = "COSMOS.OTEL_SPAN_ATTRIBUTE_NAMING_SCHEME";
    public static final String OTEL_SPAN_ATTRIBUTE_NAMING_SCHEME_VARIABLE = "COSMOS_OTEL_SPAN_ATTRIBUTE_NAMING_SCHEME";

    public static final String DEFAULT_OTEL_SPAN_ATTRIBUTE_NAMING_SCHEME = "All";

    public static int getCPUCnt() {
        return CPU_CNT;
    }

    private SslContext sslContextInit(boolean serverCertVerificationDisabled, boolean http2Enabled) {
        try {
            SslContextBuilder sslContextBuilder =
                SslContextBuilder
                    .forClient()
                    .sslProvider(SslContext.defaultClientProvider());

            if (serverCertVerificationDisabled) {
                sslContextBuilder.trustManager(InsecureTrustManagerFactory.INSTANCE); // disable cert verification
            }

            if (http2Enabled) {
                sslContextBuilder
                    .applicationProtocolConfig(
                        new ApplicationProtocolConfig(
                            ApplicationProtocolConfig.Protocol.ALPN,
                            ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                            ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                            ApplicationProtocolNames.HTTP_2,
                            ApplicationProtocolNames.HTTP_1_1
                        )
                    );
            }
            return sslContextBuilder.build();
        } catch (SSLException sslException) {
            logger.error("Fatal error cannot instantiate ssl context due to {}", sslException.getMessage(), sslException);
            throw new IllegalStateException(sslException);
        }
    }

    public SslContext getSslContext(boolean serverCertValidationDisabled, boolean http2Enabled) {
        return sslContextInit(serverCertValidationDisabled, http2Enabled);
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

    public int getGlobalEndpointManagerMaxInitializationTimeInSeconds() {
        return getJVMConfigAsInt(GLOBAL_ENDPOINT_MANAGER_INITIALIZATION_TIME_IN_SECONDS, DEFAULT_GLOBAL_ENDPOINT_MANAGER_INITIALIZATION_TIME_IN_SECONDS);
    }

    public URI getThinclientEndpoint() {
        String valueFromSystemProperty = System.getProperty(THINCLIENT_ENDPOINT);
        if (valueFromSystemProperty != null && !valueFromSystemProperty.isEmpty()) {
            return URI.create(valueFromSystemProperty);
        }

        String valueFromEnvVariable = System.getenv(THINCLIENT_ENDPOINT_VARIABLE);
        if (valueFromEnvVariable != null && !valueFromEnvVariable.isEmpty()) {
            return URI.create(valueFromEnvVariable);
        }

        return URI.create(DEFAULT_THINCLIENT_ENDPOINT);
    }

    public static boolean isThinClientEnabled() {
        String valueFromSystemProperty = System.getProperty(THINCLIENT_ENABLED);
        if (valueFromSystemProperty != null && !valueFromSystemProperty.isEmpty()) {
            return Boolean.parseBoolean(valueFromSystemProperty);
        }

        String valueFromEnvVariable = System.getenv(THINCLIENT_ENABLED_VARIABLE);
        if (valueFromEnvVariable != null && !valueFromEnvVariable.isEmpty()) {
            return Boolean.parseBoolean(valueFromEnvVariable);
        }

        return DEFAULT_THINCLIENT_ENABLED;
    }

    public int getUnavailableLocationsExpirationTimeInSeconds() {
        return getJVMConfigAsInt(UNAVAILABLE_LOCATIONS_EXPIRATION_TIME_IN_SECONDS, DEFAULT_UNAVAILABLE_LOCATIONS_EXPIRATION_TIME_IN_SECONDS);
    }

    public static int getMaxHttpHeaderSize() {
        return getJVMConfigAsInt(MAX_HTTP_HEADER_SIZE_IN_BYTES, DEFAULT_MAX_HTTP_REQUEST_HEADER_SIZE);
    }

    public static int getMaxHttpInitialLineLength() {
        return getJVMConfigAsInt(MAX_HTTP_INITIAL_LINE_LENGTH_IN_BYTES, DEFAULT_MAX_HTTP_INITIAL_LINE_LENGTH);
    }

    public static int getMaxHttpChunkSize() {
        return getJVMConfigAsInt(MAX_HTTP_CHUNK_SIZE_IN_BYTES, DEFAULT_MAX_HTTP_CHUNK_SIZE_IN_BYTES);
    }

    public static int getMaxHttpBodyLength() {
        return getJVMConfigAsInt(MAX_HTTP_BODY_LENGTH_IN_BYTES, DEFAULT_MAX_HTTP_BODY_LENGTH_IN_BYTES);
    }

    public static int getClientTelemetrySchedulingInSec() {
        return getJVMConfigAsInt(CLIENT_TELEMETRY_SCHEDULING_IN_SECONDS, DEFAULT_CLIENT_TELEMETRY_SCHEDULING_IN_SECONDS);
    }

    public static String getReactorNettyConnectionPoolName() {
        return REACTOR_NETTY_CONNECTION_POOL_NAME;
    }

    public static Duration getMaxIdleConnectionTimeout() {
        return MAX_IDLE_CONNECTION_TIMEOUT;
    }

    public static Duration getConnectionAcquireTimeout() {
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
            return Integer.parseInt(valueFromSystemProperty);
        }

        String valueFromEnvVariable = System.getenv(HTTP_DEFAULT_CONNECTION_POOL_SIZE_VARIABLE);
        if (valueFromEnvVariable != null && !valueFromEnvVariable.isEmpty()) {
            return Integer.parseInt(valueFromEnvVariable);
        }

        return DEFAULT_HTTP_DEFAULT_CONNECTION_POOL_SIZE;
    }

    public static Integer getPendingAcquireMaxCount() {
        String valueFromSystemProperty = System.getProperty(HTTP_PENDING_ACQUIRE_MAX_COUNT);
        if (valueFromSystemProperty != null && !valueFromSystemProperty.isEmpty()) {
            return Integer.parseInt(valueFromSystemProperty);
        }

        String valueFromEnvVariable = System.getenv(HTTP_PENDING_ACQUIRE_MAX_COUNT_VARIABLE);
        if (valueFromEnvVariable != null && !valueFromEnvVariable.isEmpty()) {
            return Integer.parseInt(valueFromEnvVariable);
        }

        return null;
    }

    private static String validateSerializationInclusionMode(String serializationInclusionMode) {
        if (!Strings.isNullOrEmpty(serializationInclusionMode)) {
            if ("Always".equalsIgnoreCase(serializationInclusionMode)
                || "NonNull".equalsIgnoreCase(serializationInclusionMode)
                || "NonEmpty".equalsIgnoreCase(serializationInclusionMode)
                || "NonDefault".equalsIgnoreCase(serializationInclusionMode)) {

                return serializationInclusionMode;
            }
        }

        throw new IllegalArgumentException(
            "Invalid serialization inclusion mode '"
                + serializationInclusionMode != null ? serializationInclusionMode : "null"
                + "'.");
    }

    public static String getItemSerializationInclusionMode() {
        String valueFromSystemProperty = System.getProperty(ITEM_SERIALIZATION_INCLUSION_MODE);
        if (valueFromSystemProperty != null && !valueFromSystemProperty.isEmpty()) {
            return validateSerializationInclusionMode(valueFromSystemProperty);
        }

        String valueFromEnvVariable = System.getenv(ITEM_SERIALIZATION_INCLUSION_MODE_VARIABLE);
        if (valueFromEnvVariable != null && !valueFromEnvVariable.isEmpty()) {
            return validateSerializationInclusionMode(valueFromEnvVariable);
        }

        return DEFAULT_ITEM_SERIALIZATION_INCLUSION_MODE;
    }

    public static boolean isDefaultE2ETimeoutDisabledForNonPointOperations() {
        String valueFromSystemProperty = System.getProperty(DEFAULT_E2E_FOR_NON_POINT_DISABLED);
        if (valueFromSystemProperty != null && !valueFromSystemProperty.isEmpty()) {
            return Boolean.parseBoolean(valueFromSystemProperty);
        }

        String valueFromEnvVariable = System.getenv(DEFAULT_E2E_FOR_NON_POINT_DISABLED_VARIABLE);
        if (valueFromEnvVariable != null && !valueFromEnvVariable.isEmpty()) {
            return Boolean.parseBoolean(valueFromEnvVariable);
        }

        return DEFAULT_E2E_FOR_NON_POINT_DISABLED_DEFAULT;
    }

    public static boolean isIdValueValidationEnabled() {
        String valueFromSystemProperty = System.getProperty(PREVENT_INVALID_ID_CHARS);
        if (valueFromSystemProperty != null && !valueFromSystemProperty.isEmpty()) {
            return !Boolean.parseBoolean(valueFromSystemProperty);
        }

        String valueFromEnvVariable = System.getenv(PREVENT_INVALID_ID_CHARS_VARIABLE);
        if (valueFromEnvVariable != null && !valueFromEnvVariable.isEmpty()) {
            return !Boolean.parseBoolean(valueFromEnvVariable);
        }

        return DEFAULT_PREVENT_INVALID_ID_CHARS;
    }

    public static int getMinTargetBulkMicroBatchSize() {
        String valueFromSystemProperty = System.getProperty(MIN_TARGET_BULK_MICRO_BATCH_SIZE);
        if (valueFromSystemProperty != null && !valueFromSystemProperty.isEmpty()) {
            return Integer.parseInt(valueFromSystemProperty);
        }

        String valueFromEnvVariable = System.getenv(MIN_TARGET_BULK_MICRO_BATCH_SIZE_VARIABLE);
        if (valueFromEnvVariable != null && !valueFromEnvVariable.isEmpty()) {
            return Integer.parseInt(valueFromEnvVariable);
        }

        return DEFAULT_MIN_TARGET_BULK_MICRO_BATCH_SIZE;
    }

    public static int getMaxBulkMicroBatchConcurrency() {
        String valueFromSystemProperty = System.getProperty(MAX_BULK_MICRO_BATCH_CONCURRENCY);
        if (valueFromSystemProperty != null && !valueFromSystemProperty.isEmpty()) {
            return Integer.parseInt(valueFromSystemProperty);
        }

        String valueFromEnvVariable = System.getenv(MAX_BULK_MICRO_BATCH_CONCURRENCY_VARIABLE);
        if (valueFromEnvVariable != null && !valueFromEnvVariable.isEmpty()) {
            return Integer.parseInt(valueFromEnvVariable);
        }

        return DEFAULT_MAX_BULK_MICRO_BATCH_CONCURRENCY;
    }

    public static int getMaxBulkMicroBatchFlushIntervalInMs() {
        String valueFromSystemProperty = System.getProperty(MAX_BULK_MICRO_BATCH_FLUSH_INTERVAL_IN_MILLISECONDS);
        if (valueFromSystemProperty != null && !valueFromSystemProperty.isEmpty()) {
            return Integer.parseInt(valueFromSystemProperty);
        }

        String valueFromEnvVariable = System.getenv(MAX_BULK_MICRO_BATCH_FLUSH_INTERVAL_IN_MILLISECONDS_VARIABLE);
        if (valueFromEnvVariable != null && !valueFromEnvVariable.isEmpty()) {
            return Integer.parseInt(valueFromEnvVariable);
        }

        return DEFAULT_MAX_BULK_MICRO_BATCH_FLUSH_INTERVAL_IN_MILLISECONDS;
    }

    public static int getMaxHttpRequestTimeout() {
        String valueFromSystemProperty = System.getProperty(HTTP_MAX_REQUEST_TIMEOUT);
        if (valueFromSystemProperty != null && !valueFromSystemProperty.isEmpty()) {
            return Integer.parseInt(valueFromSystemProperty);
        }

        String valueFromEnvVariable = System.getenv(HTTP_MAX_REQUEST_TIMEOUT_VARIABLE);
        if (valueFromEnvVariable != null && !valueFromEnvVariable.isEmpty()) {
            return Integer.parseInt(valueFromEnvVariable);
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
            return Integer.parseInt(val);
        }
    }

    private static boolean getBooleanValue(String val, boolean defaultValue) {
        if (StringUtils.isEmpty(val)) {
            return defaultValue;
        } else {
            return Boolean.parseBoolean(val);
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

    public static int getMaxItemCountForHybridSearchSearch() {
        String valueFromSystemProperty = System.getProperty(MAX_ITEM_COUNT_FOR_HYBRID_SEARCH);
        if (valueFromSystemProperty != null && !valueFromSystemProperty.isEmpty()) {
            return Integer.parseInt(valueFromSystemProperty);
        }

        String valueFromSystemVariable = System.getenv(MAX_ITEM_COUNT_FOR_HYBRID_SEARCH_VARIABLE);
        if (valueFromSystemVariable != null && !valueFromSystemVariable.isEmpty()) {
            return Integer.parseInt(valueFromSystemVariable);
        }
        return DEFAULT_MAX_ITEM_COUNT_FOR_HYBRID_SEARCH;
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

    public static boolean shouldLogIncorrectlyMappedSessionToken() {
        String shouldSystemExit =
            System.getProperty(
                SHOULD_LOG_INCORRECTLY_MAPPED_SESSION_TOKEN,
                firstNonNull(
                    emptyToNull(System.getenv().get(SHOULD_LOG_INCORRECTLY_MAPPED_SESSION_TOKEN)),
                    String.valueOf(DEFAULT_SHOULD_LOG_INCORRECTLY_MAPPED_SESSION_TOKEN)));

        return Boolean.parseBoolean(shouldSystemExit);
    }

    public static boolean shouldOptInDefaultCircuitBreakerConfig() {

        String shouldOptInDefaultPartitionLevelCircuitBreakerConfig =
            System.getProperty(
                PARTITION_LEVEL_CIRCUIT_BREAKER_DEFAULT_CONFIG_OPT_IN,
                firstNonNull(
                    emptyToNull(System.getenv().get(PARTITION_LEVEL_CIRCUIT_BREAKER_DEFAULT_CONFIG_OPT_IN)),
                    String.valueOf(DEFAULT_PARTITION_LEVEL_CIRCUIT_BREAKER_DEFAULT_CONFIG_OPT_IN)));

        return Boolean.parseBoolean(shouldOptInDefaultPartitionLevelCircuitBreakerConfig);
    }

    public static String isPerPartitionAutomaticFailoverEnabled() {
        return System.getProperty(
            IS_PER_PARTITION_AUTOMATIC_FAILOVER_ENABLED,
            firstNonNull(
                emptyToNull(System.getenv().get(IS_PER_PARTITION_AUTOMATIC_FAILOVER_ENABLED_VARIABLE)),
                DEFAULT_IS_PER_PARTITION_AUTOMATIC_FAILOVER_ENABLED));
    }

    public static boolean isSessionTokenFalseProgressMergeEnabled() {
        String isSessionTokenFalseProgressMergeDisabledAsString =
            System.getProperty(
                IS_SESSION_TOKEN_FALSE_PROGRESS_MERGE_ENABLED,
                firstNonNull(
                    emptyToNull(System.getenv().get(IS_SESSION_TOKEN_FALSE_PROGRESS_MERGE_ENABLED_VARIABLE)),
                    String.valueOf(DEFAULT_SESSION_TOKEN_FALSE_PROGRESS_MERGE_ENABLED)));

        return Boolean.parseBoolean(isSessionTokenFalseProgressMergeDisabledAsString);
    }

    public static int getAllowedE2ETimeoutHitCountForPPAF() {
        String allowedE2ETimeoutHitCountForPPAF =
            System.getProperty(
                E2E_TIMEOUT_ERROR_HIT_THRESHOLD_FOR_PPAF,
                firstNonNull(
                    emptyToNull(System.getenv().get(E2E_TIMEOUT_ERROR_HIT_THRESHOLD_FOR_PPAF_VARIABLE)),
                    String.valueOf(DEFAULT_E2E_TIMEOUT_ERROR_HIT_THRESHOLD_FOR_PPAF)));

        return Integer.parseInt(allowedE2ETimeoutHitCountForPPAF);
    }

    public static int getAllowedTimeWindowForE2ETimeoutHitCountTrackingInSecsForPPAF() {
        String timeWindowForE2ETimeoutHitCountTrackingInSecsForPPAF =
            System.getProperty(
                E2E_TIMEOUT_ERROR_HIT_TIME_WINDOW_IN_SECONDS_FOR_PPAF,
                firstNonNull(
                    emptyToNull(System.getenv().get(E2E_TIMEOUT_ERROR_HIT_TIME_WINDOW_IN_SECONDS_FOR_PPAF_VARIABLE)),
                    String.valueOf(DEFAULT_E2E_TIMEOUT_ERROR_HIT_TIME_WINDOW_IN_SECONDS_FOR_PPAF)));

        return Integer.parseInt(timeWindowForE2ETimeoutHitCountTrackingInSecsForPPAF);
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

    public static PartitionLevelCircuitBreakerConfig getPartitionLevelCircuitBreakerConfig() {
        String partitionLevelCircuitBreakerConfigAsString =
            System.getProperty(
                PARTITION_LEVEL_CIRCUIT_BREAKER_CONFIG,
                firstNonNull(
                    emptyToNull(System.getenv().get(PARTITION_LEVEL_CIRCUIT_BREAKER_CONFIG)),
                    DEFAULT_PARTITION_LEVEL_CIRCUIT_BREAKER_CONFIG));

        PartitionLevelCircuitBreakerConfig partitionLevelCircuitBreakerConfig
            = PartitionLevelCircuitBreakerConfig.fromJsonString(partitionLevelCircuitBreakerConfigAsString);

        if (partitionLevelCircuitBreakerConfig.getConsecutiveExceptionCountToleratedForReads() < 10) {
            return PartitionLevelCircuitBreakerConfig.DEFAULT;
        }

        if (partitionLevelCircuitBreakerConfig.getConsecutiveExceptionCountToleratedForWrites() < 5) {
            return PartitionLevelCircuitBreakerConfig.DEFAULT;
        }

        return partitionLevelCircuitBreakerConfig;
    }

    public static int getStaleCollectionCacheRefreshRetryCount() {

        String valueFromSystemProperty = System.getProperty(STALE_COLLECTION_CACHE_REFRESH_RETRY_COUNT);

        if (StringUtils.isNotEmpty(valueFromSystemProperty)) {
            return Math.max(Integer.parseInt(valueFromSystemProperty), DEFAULT_STALE_COLLECTION_CACHE_REFRESH_RETRY_COUNT);
        }

        String valueFromEnvVariable = System.getenv(STALE_COLLECTION_CACHE_REFRESH_RETRY_COUNT);

        if (StringUtils.isNotEmpty(valueFromEnvVariable)) {
            return Math.max(Integer.parseInt(valueFromEnvVariable), DEFAULT_STALE_COLLECTION_CACHE_REFRESH_RETRY_COUNT);
        }

        return DEFAULT_STALE_COLLECTION_CACHE_REFRESH_RETRY_COUNT;
    }

    public static int getStaleCollectionCacheRefreshRetryIntervalInSeconds() {

        String valueFromSystemProperty = System.getProperty(STALE_COLLECTION_CACHE_REFRESH_RETRY_INTERVAL_IN_SECONDS);

        if (StringUtils.isNotEmpty(valueFromSystemProperty)) {
            return Math.max(Integer.parseInt(valueFromSystemProperty), DEFAULT_STALE_PARTITION_UNAVAILABILITY_REFRESH_INTERVAL_IN_SECONDS);
        }

        String valueFromEnvVariable = System.getenv(STALE_COLLECTION_CACHE_REFRESH_RETRY_INTERVAL_IN_SECONDS);

        if (StringUtils.isNotEmpty(valueFromEnvVariable)) {
            return Math.max(Integer.parseInt(valueFromEnvVariable), DEFAULT_STALE_PARTITION_UNAVAILABILITY_REFRESH_INTERVAL_IN_SECONDS);
        }

        return DEFAULT_STALE_COLLECTION_CACHE_REFRESH_RETRY_INTERVAL_IN_SECONDS;
    }

    public static int getStalePartitionUnavailabilityRefreshIntervalInSeconds() {

        String valueFromSystemProperty = System.getProperty(STALE_PARTITION_UNAVAILABILITY_REFRESH_INTERVAL_IN_SECONDS);

        if (StringUtils.isNotEmpty(valueFromSystemProperty)) {
            return Math.max(Integer.parseInt(valueFromSystemProperty), DEFAULT_STALE_PARTITION_UNAVAILABILITY_REFRESH_INTERVAL_IN_SECONDS);
        }

        String valueFromEnvVariable = System.getenv(STALE_PARTITION_UNAVAILABILITY_REFRESH_INTERVAL_IN_SECONDS);

        if (StringUtils.isNotEmpty(valueFromEnvVariable)) {
            return Math.max(Integer.parseInt(valueFromEnvVariable), DEFAULT_STALE_PARTITION_UNAVAILABILITY_REFRESH_INTERVAL_IN_SECONDS);
        }

        return DEFAULT_STALE_PARTITION_UNAVAILABILITY_REFRESH_INTERVAL_IN_SECONDS;
    }

    public static int getAllowedPartitionUnavailabilityDurationInSeconds() {

        String valueFromSystemProperty = System.getProperty(ALLOWED_PARTITION_UNAVAILABILITY_DURATION_IN_SECONDS);

        if (StringUtils.isNotEmpty(valueFromSystemProperty)) {
            return Math.max(Integer.parseInt(valueFromSystemProperty), DEFAULT_ALLOWED_PARTITION_UNAVAILABILITY_DURATION_IN_SECONDS);
        }

        String valueFromEnvVariable = System.getenv(ALLOWED_PARTITION_UNAVAILABILITY_DURATION_IN_SECONDS);

        if (StringUtils.isNotEmpty(valueFromEnvVariable)) {
            return Math.max(Integer.parseInt(valueFromEnvVariable), DEFAULT_ALLOWED_PARTITION_UNAVAILABILITY_DURATION_IN_SECONDS);
        }

        return DEFAULT_ALLOWED_PARTITION_UNAVAILABILITY_DURATION_IN_SECONDS;
    }

    public static int getConnectionEstablishmentTimeoutForPartitionRecoveryInSeconds() {

        String valueFromSystemProperty = System.getProperty(CONNECTION_ESTABLISHMENT_TIMEOUT_FOR_PARTITION_RECOVERY_IN_SECONDS);

        if (StringUtils.isNotEmpty(valueFromSystemProperty)) {
            return Math.max(Integer.parseInt(valueFromSystemProperty), DEFAULT_CONNECTION_ESTABLISHMENT_TIMEOUT_FOR_PARTITION_RECOVERY_IN_SECONDS);
        }

        String valueFromEnvVariable = System.getenv(CONNECTION_ESTABLISHMENT_TIMEOUT_FOR_PARTITION_RECOVERY_IN_SECONDS);

        if (StringUtils.isNotEmpty(valueFromEnvVariable)) {
            return Math.max(Integer.parseInt(valueFromEnvVariable), DEFAULT_CONNECTION_ESTABLISHMENT_TIMEOUT_FOR_PARTITION_RECOVERY_IN_SECONDS);
        }

        return DEFAULT_CONNECTION_ESTABLISHMENT_TIMEOUT_FOR_PARTITION_RECOVERY_IN_SECONDS;
    }

    public static String getCharsetDecoderErrorActionOnMalformedInput() {
        return System.getProperty(
                CHARSET_DECODER_ERROR_ACTION_ON_MALFORMED_INPUT,
                firstNonNull(
                    emptyToNull(System.getenv().get(CHARSET_DECODER_ERROR_ACTION_ON_MALFORMED_INPUT)),
                    DEFAULT_CHARSET_DECODER_ERROR_ACTION_ON_MALFORMED_INPUT));
    }

    public static String getCharsetDecoderErrorActionOnUnmappedCharacter() {
        return System.getProperty(
                CHARSET_DECODER_ERROR_ACTION_ON_UNMAPPED_CHARACTER,
                firstNonNull(
                    emptyToNull(System.getenv().get(CHARSET_DECODER_ERROR_ACTION_ON_UNMAPPED_CHARACTER)),
                    DEFAULT_CHARSET_DECODER_ERROR_ACTION_ON_UNMAPPED_CHARACTER));
    }

    public static boolean shouldDisableIMDSAccess() {
        String shouldDisableIMDSAccess =
            System.getProperty(
                COSMOS_DISABLE_IMDS_ACCESS,
                firstNonNull(
                    emptyToNull(System.getenv().get(COSMOS_DISABLE_IMDS_ACCESS_VARIABLE)),
                    String.valueOf(COSMOS_DISABLE_IMDS_ACCESS_DEFAULT)));

        return Boolean.parseBoolean(shouldDisableIMDSAccess);
    }

    public static boolean isHttpConnectionWithoutTLSAllowed() {
        String httpForEmulatorAllowed = System.getProperty(
            HTTP_CONNECTION_WITHOUT_TLS_ALLOWED,
            firstNonNull(
                emptyToNull(System.getenv().get(HTTP_CONNECTION_WITHOUT_TLS_ALLOWED_VARIABLE)),
                String.valueOf(DEFAULT_HTTP_CONNECTION_WITHOUT_TLS_ALLOWED)));

        return Boolean.parseBoolean(httpForEmulatorAllowed);
    }

    public static boolean isHttp2Enabled() {
        String httpEnabledConfig = System.getProperty(
            HTTP2_ENABLED,
            firstNonNull(
                emptyToNull(System.getenv().get(HTTP2_ENABLED_VARIABLE)),
                String.valueOf(DEFAULT_HTTP2_ENABLED)));

        return Boolean.parseBoolean(httpEnabledConfig);
    }

    public static int getHttp2MaxConnectionPoolSize() {
        String http2MaxConnectionPoolSize = System.getProperty(
            HTTP2_MAX_CONNECTION_POOL_SIZE,
            firstNonNull(
                emptyToNull(System.getenv().get(HTTP2_MAX_CONNECTION_POOL_SIZE_VARIABLE)),
                String.valueOf(DEFAULT_HTTP2_MAX_CONNECTION_POOL_SIZE)));

        return Integer.parseInt(http2MaxConnectionPoolSize);
    }

    public static int getHttp2MinConnectionPoolSize() {
        String http2MinConnectionPoolSize = System.getProperty(
            HTTP2_MIN_CONNECTION_POOL_SIZE,
            firstNonNull(
                emptyToNull(System.getenv().get(HTTP2_MIN_CONNECTION_POOL_SIZE_VARIABLE)),
                String.valueOf(DEFAULT_HTTP2_MIN_CONNECTION_POOL_SIZE)));

        return Integer.parseInt(http2MinConnectionPoolSize);
    }

    public static int getHttp2MaxConcurrentStreams() {
        String http2MaxConcurrentStreams = System.getProperty(
            HTTP2_MAX_CONCURRENT_STREAMS,
            firstNonNull(
                emptyToNull(System.getenv().get(HTTP2_MAX_CONCURRENT_STREAMS_VARIABLE)),
                String.valueOf(DEFAULT_HTTP2_MAX_CONCURRENT_STREAMS)));

        return Integer.parseInt(http2MaxConcurrentStreams);
    }

    public static boolean isEmulatorServerCertValidationDisabled() {
        String certVerificationDisabledConfig = System.getProperty(
            EMULATOR_SERVER_CERTIFICATE_VALIDATION_DISABLED,
            firstNonNull(
                emptyToNull(System.getenv().get(EMULATOR_SERVER_CERTIFICATE_VALIDATION_DISABLED_VARIABLE)),
                String.valueOf(DEFAULT_EMULATOR_SERVER_CERTIFICATE_VALIDATION_DISABLED)));

        return Boolean.parseBoolean(certVerificationDisabledConfig);
    }

    public static String getEmulatorHost() {
        return System.getProperty(
            EMULATOR_HOST,
            firstNonNull(
                emptyToNull(System.getenv().get(EMULATOR_HOST_VARIABLE)),
                DEFAULT_EMULATOR_HOST));
    }

    public static boolean isReadAvailabilityStrategyEnabledWithPpaf() {
        String isReadAvailabilityStrategyEnabledWithPpaf = System.getProperty(
            IS_READ_AVAILABILITY_STRATEGY_ENABLED_WITH_PPAF,
            firstNonNull(
                emptyToNull(System.getenv().get(IS_READ_AVAILABILITY_STRATEGY_ENABLED_WITH_PPAF_VARIABLE)),
                DEFAULT_IS_READ_AVAILABILITY_STRATEGY_ENABLED_WITH_PPAF));

        return Boolean.parseBoolean(isReadAvailabilityStrategyEnabledWithPpaf);
    }

    public static String getAadScopeOverride() {
        return System.getProperty(
            AAD_SCOPE_OVERRIDE,
            firstNonNull(
                emptyToNull(System.getenv().get(AAD_SCOPE_OVERRIDE_VARIABLE)),
                DEFAULT_AAD_SCOPE_OVERRIDE));
    }

    public static int getWarnLevelLoggingThresholdForPpaf() {
        String warnLevelLoggingThresholdForPpaf = System.getProperty(
            WARN_LEVEL_LOGGING_THRESHOLD_FOR_PPAF,
            firstNonNull(
                emptyToNull(System.getenv().get(WARN_LEVEL_LOGGING_THRESHOLD_FOR_PPAF_VARIABLE)),
                String.valueOf(DEFAULT_WARN_LEVEL_LOGGING_THRESHOLD_FOR_PPAF)));

        return Integer.parseInt(warnLevelLoggingThresholdForPpaf);
    }

    public static String getAzureMonitorConnectionString() {
        return System.getProperty(
            APPLICATIONINSIGHTS_CONNECTION_STRING,
            System.getenv(APPLICATIONINSIGHTS_CONNECTION_STRING_VARIABLE)
        );
    }

    public static EnumSet<AttributeNamingScheme> getDefaultOtelSpanAttributeNamingScheme() {
        String valueFromSystemProperty = System.getProperty(OTEL_SPAN_ATTRIBUTE_NAMING_SCHEME);
        if (valueFromSystemProperty != null && !valueFromSystemProperty.isEmpty()) {
            return AttributeNamingScheme.parse(valueFromSystemProperty);
        }

        String valueFromEnvVariable = System.getenv(OTEL_SPAN_ATTRIBUTE_NAMING_SCHEME_VARIABLE);
        if (valueFromEnvVariable != null && !valueFromEnvVariable.isEmpty()) {
            return AttributeNamingScheme.parse(valueFromEnvVariable);
        }

        return AttributeNamingScheme.parse(DEFAULT_OTEL_SPAN_ATTRIBUTE_NAMING_SCHEME);
    }
}
