// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.client.urlconnection;

import com.generic.core.http.client.HttpClient;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.models.Context;
import com.generic.core.util.logging.ClientLogger;

import java.net.HttpURLConnection;
import java.time.Duration;

/**
 * HttpClient implementation using {@link HttpURLConnection} to send requests and receive responses.
 */
public class UrlConnectionClient implements HttpClient {
    private static final ClientLogger LOGGER = new ClientLogger(UrlConnectionClient.class);
    private final long connectionTimeout;
    private final long readTimeout;
    private final Duration writeTimeout;
    private final Duration responseTimeout;

    UrlConnectionClient(Duration connectionTimeout, Duration readTimeout, Duration writeTimeout,
                        Duration responseTimeout) {
        this.connectionTimeout = connectionTimeout == null ? -1 : connectionTimeout.toMillis();
        this.readTimeout = readTimeout == null ? -1 : readTimeout.toMillis();
        this.writeTimeout = writeTimeout;
        this.responseTimeout = responseTimeout;

    }


    /**
     * Synchronously send the HttpRequest.
     *
     * @param httpRequest The HTTP request being sent
     * @param context     The context of the request, for any additional changes
     * @return The HttpResponse object
     */
    @Override
    public HttpResponse send(HttpRequest httpRequest, Context context) {
        return null;
    }

}
