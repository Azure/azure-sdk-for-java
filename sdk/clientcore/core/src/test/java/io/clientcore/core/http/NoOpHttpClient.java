// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.HttpResponse;
import io.clientcore.core.http.models.Response;

public class NoOpHttpClient implements HttpClient {
    @Override
    public Response<?> send(HttpRequest request) {
        return new HttpResponse<>(request, 200, new HttpHeaders(), null); // No-op
    }
}
