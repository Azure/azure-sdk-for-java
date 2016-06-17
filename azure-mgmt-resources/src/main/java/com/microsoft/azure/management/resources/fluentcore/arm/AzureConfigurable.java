/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm;

import okhttp3.Interceptor;
import okhttp3.logging.HttpLoggingInterceptor;

import java.net.Proxy;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * The base interface for allowing configurations to be made on the HTTP client.
 *
 * @param <T> the actual type of the interface extending this interface
 */
public interface AzureConfigurable<T extends AzureConfigurable<T>> {
    /**
     * Set the logging level on the HTTP client.
     *
     * @param level the OkHttp logging level
     * @return the configurable object itself
     */
    T withLogLevel(HttpLoggingInterceptor.Level level);

    /**
     * Plug in an interceptor into the HTTP pipeline.
     *
     * @param interceptor the interceptor to plug in
     * @return the configurable object itself
     */
    T withInterceptor(Interceptor interceptor);

    /**
     * Specify the user agent header.
     *
     * @param userAgent the user agent to use
     * @return the configurable object itself
     */
    T withUserAgent(String userAgent);

    /**
     * Set the read timeout on the HTTP client. Default is 10 seconds.
     *
     * @param timeout the timeout numeric value
     * @param unit the time unit for the numeric value
     * @return the configurable object itself for chaining
     */
    T withReadTimeout(long timeout, TimeUnit unit);

    /**
     * Set the connection timeout on the HTTP client. Default is 10 seconds.
     *
     * @param timeout the timeout numeric value
     * @param unit the time unit for the numeric value
     * @return the configurable object itself for chaining
     */
    T withConnectionTimeout(long timeout, TimeUnit unit);

    /**
     * Set the maximum idle connections for the HTTP client. Default is 5.
     *
     * @param maxIdleConnections the maximum idle connections
     * @return the configurable object itself for chaining
     */
    T withMaxIdleConnections(int maxIdleConnections);

    /**
     * Sets the executor for async callbacks to run on.
     *
     * @param executor the executor to execute the callbacks.
     * @return the configurable object itself for chaining
     */
    T withCallbackExecutor(Executor executor);

    /**
     * Sets the proxy for the HTTP client.
     *
     * @param proxy the proxy to use
     * @return the configurable object itself for chaining
     */
    T withProxy(Proxy proxy);
}
