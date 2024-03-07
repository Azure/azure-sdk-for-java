// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.client;

import com.generic.core.http.Response;
import com.generic.core.http.models.HttpRequest;

/**
 * A generic interface for sending HTTP requests and getting responses.
 */
public interface HttpClient {
    /**
     * Sends the provided request synchronously with contextual information.
     *
     * @param request The HTTP request to send.
     *
     * @return The response.
     */
    Response<?> send(HttpRequest request);

    /**
     * Creates a new {@link HttpClient} instance.
     *
     * @return A new {@link HttpClient} instance.
     */
    static HttpClient createDefault() {
        return HttpClientProviders.createInstance();
    }
}
