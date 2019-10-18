// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.internal.directconnectivity.Protocol;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Strings.emptyToNull;

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

    private static final String MAX_HTTP_BODY_LENGTH_IN_BYTES = "COSMOS.MAX_HTTP_BODY_LENGTH_IN_BYTES";
    private static final String MAX_HTTP_INITIAL_LINE_LENGTH_IN_BYTES = "COSMOS.MAX_HTTP_INITIAL_LINE_LENGTH_IN_BYTES";
    private static final String MAX_HTTP_CHUNK_SIZE_IN_BYTES = "COSMOS.MAX_HTTP_CHUNK_SIZE_IN_BYTES";
    private static final String MAX_HTTP_HEADER_SIZE_IN_BYTES = "COSMOS.MAX_HTTP_HEADER_SIZE_IN_BYTES";
    private static final String MAX_DIRECT_HTTPS_POOL_SIZE = "COSMOS.MAX_DIRECT_HTTP_CONNECTION_LIMIT";

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

    //  Reactor Netty Constants
    private static final int MAX_IDLE_CONNECTION_TIMEOUT_IN_MILLIS = 60 * 1000;
    private static final int CONNECTION_ACQUIRE_TIMEOUT_IN_MILLIS = 45 * 1000;
    private static final int REACTOR_NETTY_MAX_CONNECTION_POOL_SIZE = 1000;
    private static final String REACTOR_NETTY_CONNECTION_POOL_NAME = "reactor-netty-connection-pool";

    public Configs() {
        this.sslContext = sslContextInit();
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
            return Protocol.valueOf(protocol.toUpperCase());
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

    public String getReactorNettyConnectionPoolName() {
        return REACTOR_NETTY_CONNECTION_POOL_NAME;
    }

    public int getMaxIdleConnectionTimeoutInMillis() {
        return MAX_IDLE_CONNECTION_TIMEOUT_IN_MILLIS;
    }

    public int getConnectionAcquireTimeoutInMillis() {
        return CONNECTION_ACQUIRE_TIMEOUT_IN_MILLIS;
    }

    public int getReactorNettyMaxConnectionPoolSize() {
        return REACTOR_NETTY_MAX_CONNECTION_POOL_SIZE;
    }

    private static String getJVMConfigAsString(String propName, String defaultValue) {
        String propValue = System.getProperty(propName);
        return StringUtils.defaultString(propValue, defaultValue);
    }

    private static int getJVMConfigAsInt(String propName, int defaultValue) {
        String propValue = System.getProperty(propName);
        return getIntValue(propValue, defaultValue);
    }

    private static int getIntValue(String val, int defaultValue) {
        if (StringUtils.isEmpty(val)) {
            return defaultValue;
        } else {
            return Integer.valueOf(val);
        }
    }
}
