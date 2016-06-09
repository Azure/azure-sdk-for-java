/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm;

import okhttp3.Interceptor;
import okhttp3.logging.HttpLoggingInterceptor;

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
}
