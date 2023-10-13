// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http;

import com.generic.core.util.Context;
import com.generic.core.util.HttpClientOptions;

/**
 * A generic interface for sending HTTP requests and getting responses.
 */
public interface HttpClient {
    /**
     * Sends the provided request synchronously with contextual information.
     *
     * @param request The HTTP request to send.
     * @param context Contextual information about the request.
     * @return The response.
     */
    HttpResponse sendSync(HttpRequest request, Context context);

    /**
     * Creates a new {@link HttpClient} instance.
     *
     * @return A new {@link HttpClient} instance.
     */
    static HttpClient createDefault() {
        return createDefault(null);
    }

    /**
     * Creates a new {@link HttpClient} instance.
     *
     * @param clientOptions Configuration options applied to the created {@link HttpClient}.
     * @return A new {@link HttpClient} instance.
     */
    static HttpClient createDefault(HttpClientOptions clientOptions) {
        return null;
        // return HttpClientProviders.createInstance(clientOptions);
    }
}
