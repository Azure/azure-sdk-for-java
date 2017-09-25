/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.credentials.http;

import com.microsoft.rest.http.HttpClient;
import com.microsoft.rest.http.HttpRequest;
import com.microsoft.rest.http.HttpResponse;
import rx.Single;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * This HttpClient attempts to mimic the behavior of http://httpbin.org without ever making a network call.
 */
public class MockHttpClient extends HttpClient {
    private final List<HttpRequest> requests;

    public MockHttpClient() {
        requests = new ArrayList<>();
    }

    public List<HttpRequest> requests() {
        return requests;
    }

    @Override
    public HttpClient withProxy(Proxy proxy) {
        return this;
    }

    @Override
    protected Single<HttpResponse> sendRequestInternalAsync(HttpRequest request) {
        requests.add(request);

        return Single.just(null);
    }
}
