// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.implementation.http.client;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.utils.SharedExecutorService;
import io.clientcore.core.utils.configuration.Configuration;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for JDK HttpClient.
 */
public final class JdkHttpUtils {
    /**
     * Converts the given JDK Http headers to clientcore Http header.
     *
     * @param headers the JDK Http headers
     * @return the clientcore Http headers
     */
    static HttpHeaders fromJdkHttpHeaders(java.net.http.HttpHeaders headers) {
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
    static int getSizeOfBuffers(List<ByteBuffer> buffers) {
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
     * @return The scheduled future for the task.
     */
    static ScheduledFuture<?> scheduleTimeoutTask(Runnable task, long timeoutMillis) {
        return SharedExecutorService.getInstance().schedule(task, timeoutMillis, TimeUnit.MILLISECONDS);
    }

    private JdkHttpUtils() {
    }
}
