/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import io.reactivex.Completable;

/**
 * Creates an HttpClient from a Configuration.
 */
public interface HttpClientFactory {
    /**
     * Creates an HttpClient with the given Configuration.
     * @param configuration the configuration.
     * @return the HttpClient.
     */
    HttpClient create(HttpClientConfiguration configuration);

    /**
     * Asynchronously awaits completion of in-flight requests,
     * then closes shared resources associated with this HttpClient.Factory.
     * After this Completable completes, HttpClients created from this Factory can no longer be used.
     *
     * @return a Completable which shuts down the factory when subscribed to.
     */
    Completable shutdown();
}