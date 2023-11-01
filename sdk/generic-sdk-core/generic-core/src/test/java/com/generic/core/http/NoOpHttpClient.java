// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http;

import com.generic.core.http.client.HttpClient;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.models.Context;

public class NoOpHttpClient implements HttpClient {
    @Override
    public HttpResponse send(HttpRequest request, Context context) {
        return null; // No-op
    }
}
