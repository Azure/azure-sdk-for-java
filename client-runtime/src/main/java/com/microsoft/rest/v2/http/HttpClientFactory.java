/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import java.io.Closeable;

/**
 * Creates an HttpClient from a Configuration.
 */
public interface HttpClientFactory extends Closeable {
    /**
     * Creates an HttpClient with the given Configuration.
     * @param configuration the configuration.
     * @return the HttpClient.
     */
    HttpClient create(HttpClientConfiguration configuration);

    /**
     * Awaits completion of in-flight requests, then closes shared resources associated with this HttpClient.Factory.
     * After this method returns, HttpClients created from this Factory can no longer be used.
     */
    void close();
}