/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v3.policy;

import com.microsoft.rest.v3.http.HttpClient;
import com.microsoft.rest.v3.http.HttpRequest;
import com.microsoft.rest.v3.http.HttpResponse;
import reactor.core.publisher.Mono;

/**
 * An adapter that converts an HttpClient to a RequestPolicy.
 */
public class HttpClientRequestPolicyAdapter implements RequestPolicy {
    private final HttpClient httpClient;

    /**
     * Create a new HttpClientRequestPolicyAdapter that will use the provided HttpClient to send
     * HTTP requests.
     * @param httpClient The HttpClient to use.
     */
    public HttpClientRequestPolicyAdapter(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Get the HttpClient that this RequestPolicy uses to send HttpRequests.
     * @return the HttpClient that this RequestPolicy uses to send HttpRequests.
     */
    public HttpClient httpClient() {
        return httpClient;
    }

    @Override
    public Mono<HttpResponse> sendAsync(HttpRequest request) {
        return httpClient.sendRequestAsync(request);
    }
}
