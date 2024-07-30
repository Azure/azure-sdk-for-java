// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.jdk.httpclient.implementation;

import com.azure.core.http.HttpHeaders;
import com.azure.core.implementation.util.HttpHeadersAccessHelper;
import com.azure.core.util.CoreUtils;

import java.nio.ByteBuffer;
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
    private static final Timer TIMER = new Timer("azure-jdk-httpclient-network-timeout-tracker", true);

    /**
     * Converts the given JDK Http headers to azure-core Http header.
     *
     * @param headers the JDK Http headers
     * @return the azure-core Http headers
     */
    public static HttpHeaders fromJdkHttpHeaders(java.net.http.HttpHeaders headers) {
        final HttpHeaders httpHeaders = new HttpHeaders((int) (headers.map().size() / 0.75F));

        for (Map.Entry<String, List<String>> kvp : headers.map().entrySet()) {
            if (!CoreUtils.isNullOrEmpty(kvp.getValue())) {
                // JDK HttpClient parses headers to lower case, use the access helper to bypass lowercasing the header
                // name (or in this case, just checking that the header name is lowercased).
                HttpHeadersAccessHelper.setInternal(httpHeaders, kvp.getKey(), kvp.getKey(), kvp.getValue());
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

    private JdkHttpUtils() {
    }
}
