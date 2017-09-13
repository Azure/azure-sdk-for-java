/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import com.microsoft.rest.v2.policy.RequestPolicy;
import rx.Single;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A generic interface for sending HTTP requests and getting responses.
 */
public abstract class HttpClient {
    private final List<RequestPolicy.Factory> policyFactories;

    private final RequestPolicy lastRequestPolicy = new RequestPolicy() {
        @Override
        public Single<? extends HttpResponse> sendAsync(HttpRequest request) {
            return sendRequestInternalAsync(request);
        }
    };

    protected HttpClient() {
        this.policyFactories = Collections.emptyList();
    }

    protected HttpClient(List<RequestPolicy.Factory> policyFactories) {
        this.policyFactories = new ArrayList<>(policyFactories);

        // Reversing the list facilitates the creation of the RequestPolicy linked list per-request.
        Collections.reverse(this.policyFactories);
    }

    /**
     * Send the provided request asynchronously, applying any request policies provided to the HttpClient instance.
     * @param request The HTTP request to send.
     * @return A {@link Single} representing the HTTP response that will arrive asynchronously.
     */
    public final Single<? extends HttpResponse> sendRequestAsync(HttpRequest request) {
        // Builds a linked list starting from the end.
        RequestPolicy next = lastRequestPolicy;
        for (RequestPolicy.Factory factory : policyFactories) {
            next = factory.create(next);
        }
        return next.sendAsync(request);
    }

    /**
     * Send the provided request and block until the response is received.
     * @param request The HTTP request to send.
     * @return The HTTP response received.
     * @throws IOException On network issues.
     */
    public final HttpResponse sendRequest(HttpRequest request) throws IOException {
        final Single<? extends HttpResponse> asyncResult = sendRequestAsync(request);
        return asyncResult.toBlocking().value();
    }

    /**
     * Send the provided request asynchronously through the concrete HTTP client implementation.
     * @param request The HTTP request to send.
     * @return A {@link Single} representing the HTTP response that will arrive asynchronously.
     */
    protected abstract Single<? extends HttpResponse> sendRequestInternalAsync(HttpRequest request);
}
