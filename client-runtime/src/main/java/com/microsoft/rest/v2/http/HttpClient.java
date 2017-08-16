/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import rx.Single;

import java.io.IOException;

/**
 * A generic interface for sending HTTP requests and getting responses.
 */
public abstract class HttpClient {
    /**
     * Send the provided request and block until the response is received.
     * @param request The HTTP request to send.
     * @return The HTTP response received.
     * @throws IOException On network issues.
     */
    public HttpResponse sendRequest(HttpRequest request) throws IOException {
        final Single<? extends HttpResponse> asyncResult = sendRequestAsync(request);
        return asyncResult.toBlocking().value();
    }

    /**
     * Send the provided request and block until the response is received.
     * @param request The HTTP request to send.
     * @return The HTTP response received.
     * @throws IOException On network issues.
     */
    public abstract Single<? extends HttpResponse> sendRequestAsync(HttpRequest request);
}
