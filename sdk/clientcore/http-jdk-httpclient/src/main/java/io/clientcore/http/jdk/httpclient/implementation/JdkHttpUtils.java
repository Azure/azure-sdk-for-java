// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.jdk.httpclient.implementation;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.util.configuration.Configuration;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Utility class for JDK HttpClient.
 */
public final class JdkHttpUtils {
    // Singleton timer to schedule timeout tasks.
    // TODO (alzimmer): Make sure one thread is sufficient for all timeout tasks.
    private static final Timer TIMER = new Timer("clientcore-jdk-httpclient-network-timeout-tracker", true);

    /**
     * Converts the given JDK Http headers to clientcore Http header.
     *
     * @param headers the JDK Http headers
     * @return the clientcore Http headers
     */
    public static HttpHeaders fromJdkHttpHeaders(java.net.http.HttpHeaders headers) {
        final HttpHeaders httpHeaders = new HttpHeaders((int) (headers.map().size() / 0.75F));

        for (Map.Entry<String, List<String>> kvp : headers.map().entrySet()) {
            List<String> value = kvp.getValue();
            if (value != null && !value.isEmpty()) {
                httpHeaders.set(HttpHeaderName.fromString(kvp.getKey()), kvp.getValue());
            }
        }

        return httpHeaders;
    }

    /**
     * Gets the size of the given list of ByteBuffers.
     * <p>
     * If the size of buffers is greater than {@link Integer#MAX_VALUE} an {@link IllegalStateException} will be thrown.
     * This is done as this is used to create a {@code byte[]} and this could result in an integer overflow.
     *
     * @param buffers The list of ByteBuffers to get the size of.
     * @return The size of the buffers.
     * @throws IllegalStateException If the size of the buffers is greater than {@link Integer#MAX_VALUE}.
     */
    public static int getSizeOfBuffers(List<ByteBuffer> buffers) {
        long size = 0;
        for (ByteBuffer buffer : buffers) {
            size += buffer.remaining();

            if (size > Integer.MAX_VALUE) {
                throw new IllegalStateException("The size of the buffers is greater than Integer.MAX_VALUE.");
            }
        }

        return (int) size;
    }

    /**
     * Schedules a timeout task to be executed after the given timeout.
     *
     * @param task The task to be executed.
     * @param timeoutMillis The timeout in milliseconds.
     */
    public static void scheduleTimeoutTask(TimerTask task, long timeoutMillis) {
        TIMER.schedule(task, timeoutMillis);
    }

    /**
     * Attempts to load an environment configured default timeout.
     * <p>
     * If the environment default timeout isn't configured, {@code defaultTimeout} will be returned. If the environment
     * default timeout is a string that isn't parseable by {@link Long#parseLong(String)}, {@code defaultTimeout} will
     * be returned. If the environment default timeout is less than 0, {@link Duration#ZERO} will be returned indicated
     * that there is no timeout period.
     *
     * @param configuration The environment configurations.
     * @param timeoutPropertyName The default timeout property name.
     * @param defaultTimeout The fallback timeout to be used.
     * @param logger A {@link ClientLogger} to log exceptions.
     * @return Either the environment configured default timeout, {@code defaultTimeoutMillis}, or 0.
     */
    public static Duration getDefaultTimeoutFromEnvironment(Configuration configuration, String timeoutPropertyName,
        Duration defaultTimeout, ClientLogger logger) {
        String environmentTimeout = configuration.get(timeoutPropertyName);

        // Environment wasn't configured with the timeout property.
        if (environmentTimeout == null || environmentTimeout.isEmpty()) {
            return defaultTimeout;
        }

        try {
            long timeoutMillis = Long.parseLong(environmentTimeout);
            if (timeoutMillis < 0) {
                logger.atVerbose()
                    .addKeyValue(timeoutPropertyName, timeoutMillis)
                    .log("Negative timeout values are not allowed. Using 'Duration.ZERO' to indicate no timeout.");
                return Duration.ZERO;
            }

            return Duration.ofMillis(timeoutMillis);
        } catch (NumberFormatException ex) {
            logger.atInfo()
                .addKeyValue(timeoutPropertyName, environmentTimeout)
                .addKeyValue("defaultTimeout", defaultTimeout)
                .log("Timeout is not valid number. Using default value.", ex);

            return defaultTimeout;
        }
    }

    private JdkHttpUtils() {
    }
}
