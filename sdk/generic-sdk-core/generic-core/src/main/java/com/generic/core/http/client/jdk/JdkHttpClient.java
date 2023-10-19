// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.client.jdk;

import com.generic.core.http.client.HttpClient;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.models.Context;

import java.util.Set;

public class JdkHttpClient implements HttpClient {
    JdkHttpClient(java.net.http.HttpClient httpClient, Set<String> restrictedHeaders) {
    }

    @Override
    public HttpResponse send(HttpRequest request, Context context) {
        return null;
    }
}
