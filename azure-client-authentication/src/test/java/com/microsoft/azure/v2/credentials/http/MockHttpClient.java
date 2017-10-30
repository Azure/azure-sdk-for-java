/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.v2.credentials.http;

import com.microsoft.rest.v2.http.HttpClient;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import rx.Single;

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
    protected Single<HttpResponse> sendRequestInternalAsync(HttpRequest request) {
        requests.add(request);

        return Single.just(null);
    }
}
