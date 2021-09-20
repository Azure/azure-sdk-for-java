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
    private static final String QUERY_PLAN_RESPONSE_TIMEOUT_IN_SECONDS = "COSMOS.QUERY_PLAN_RESPONSE_TIMEOUT_IN_SECONDS";
    private static final String ADDRESS_REFRESH_RESPONSE_TIMEOUT_IN_SECONDS = "COSMOS.ADDRESS_REFRESH_RESPONSE_TIMEOUT_IN_SECONDS";
    private static final String CLIENT_TELEMETRY_ENABLED = "COSMOS.CLIENT_TELEMETRY_ENABLED";
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
    private static final int REACTOR_NETTY_MAX_CONNECTION_POOL_SIZE = 1000;
    private static final String REACTOR_NETTY_CONNECTION_POOL_NAME = "reactor-netty-connection-pool";
    private static final int DEFAULT_HTTP_RESPONSE_TIMEOUT_IN_SECONDS = 60;
    private static final int DEFAULT_QUERY_PLAN_RESPONSE_TIMEOUT_IN_SECONDS = 5;
    private static final int DEFAULT_ADDRESS_REFRESH_RESPONSE_TIMEOUT_IN_SECONDS = 5;

    // SessionTokenMismatchRetryPolicy Constants
    private static final String DEFAULT_SESSION_TOKEN_MISMATCH_WAIT_TIME_IN_MILLISECONDS_NAME =
        "COSMOS.DEFAULT_SESSION_TOKEN_MISMATCH_WAIT_TIME_IN_MILLISECONDS";
    private static final int DEFAULT_SESSION_TOKEN_MISMATCH_WAIT_TIME_IN_MILLISECONDS = 5000;

    private static final String DEFAULT_SESSION_TOKEN_MISMATCH_INITIAL_BACKOFF_TIME_IN_MILLISECONDS_NAME =
        "COSMOS.DEFAULT_SESSION_TOKEN_MISMATCH_INITIAL_BACKOFF_TIME_IN_MILLISECONDS";
    private static final int DEFAULT_SESSION_TOKEN_MISMATCH_INITIAL_BACKOFF_TIME_IN_MILLISECONDS = 5;

    private static final String DEFAULT_SESSION_TOKEN_MISMATCH_MAXIMUM_BACKOFF_TIME_IN_MILLISECONDS_NAME =
        "COSMOS.DEFAULT_SESSION_TOKEN_MISMATCH_MAXIMUM_BACKOFF_TIME_IN_MILLISECONDS";
    private static final int DEFAULT_SESSION_TOKEN_MISMATCH_MAXIMUM_BACKOFF_TIME_IN_MILLISECONDS = 50;

    // Whether to process the response on a different thread
    private static final String DEFAULT_SWITCH_OFF_IO_THREAD_FOR_RESPONSE_NAME = "COSMOS.SWITCH_OFF_IO_THREAD_FOR_RESPONSE";
    private static final boolean DEFAULT_SWITCH_OFF_IO_THREAD_FOR_RESPONSE = false;

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

    public int getReactorNettyMaxConnectionPoolSize() {
        return REACTOR_NETTY_MAX_CONNECTION_POOL_SIZE;
    }

    public static int getHttpResponseTimeoutInSeconds() {
        return getJVMConfigAsInt(HTTP_RESPONSE_TIMEOUT_IN_SECONDS, DEFAULT_HTTP_RESPONSE_TIMEOUT_IN_SECONDS);
    }

    public static int getQueryPlanResponseTimeoutInSeconds() {
        return getJVMConfigAsInt(QUERY_PLAN_RESPONSE_TIMEOUT_IN_SECONDS, DEFAULT_QUERY_PLAN_RESPONSE_TIMEOUT_IN_SECONDS);
    }

    public static boolean isClientTelemetryEnabled(boolean defaultValue) {
        return getJVMConfigAsBoolean(CLIENT_TELEMETRY_ENABLED, defaultValue);
    }

    public static String getClientTelemetryEndpoint() {
        return System.getProperty(CLIENT_TELEMETRY_ENDPOINT);
    }

    public static String getEnvironmentName() {
        return System.getProperty(ENVIRONMENT_NAME);
    }

    public static boolean isQueryPlanCachingEnabled() {
        // Queryplan caching will be disabled by default
        return getJVMConfigAsBoolean(QUERYPLAN_CACHING_ENABLED, false);
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

    public static boolean shouldSwitchOffIOThreadForResponse() {
        return getJVMConfigAsBoolean(
            DEFAULT_SWITCH_OFF_IO_THREAD_FOR_RESPONSE_NAME,
            DEFAULT_SWITCH_OFF_IO_THREAD_FOR_RESPONSE);
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
}
