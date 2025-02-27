// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.test.http;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;

import java.io.IOException;

/**
 * An HttpClient instance that does not do anything.
 */
public class NoOpHttpClient implements HttpClient {
    /**
     * Creates a new NoOpHttpClient.
     */
    public NoOpHttpClient() {
    }

    @Override
    public Response<?> send(HttpRequest request) throws IOException {
        return null;
    }
}
