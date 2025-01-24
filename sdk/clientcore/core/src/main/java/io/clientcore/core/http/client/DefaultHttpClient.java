// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.client;

import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.Duration;
import java.util.Set;

import static io.clientcore.core.http.client.DefaultHttpClientBuilder.ERROR_MESSAGE;

/**
 * HttpClient implementation using {@link HttpURLConnection} to send requests and receive responses.
 */
class DefaultHttpClient implements HttpClient {
    private static final ClientLogger LOGGER = new ClientLogger(DefaultHttpClient.class);

    DefaultHttpClient(Object httpClient, Set<String> restrictedHeaders, Duration writeTimeout, Duration responseTimeout,
        Duration readTimeout) {
        throw LOGGER.logThrowableAsError(new UnsupportedOperationException(ERROR_MESSAGE));
    }

    @Override
    public Response<?> send(HttpRequest request) throws IOException {
        throw LOGGER.logThrowableAsError(new UnsupportedOperationException(ERROR_MESSAGE));
    }
}
