/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2;

import com.microsoft.rest.v2.protocol.SerializerAdapter;
import com.microsoft.rest.v2.serializer.JacksonAdapter;
import com.microsoft.rest.v2.http.HttpClient;

/**
 * The base class for generated service clients.
 */
public abstract class ServiceClient {
    /**
     * The RestClient instance storing configuration for service clients.
     */
    private RestClient restClient;

    /**
     * Initializes a new instance of the ServiceClient class.
     *
     * @param baseUrl the service base uri
     */
    protected ServiceClient(String baseUrl) {
        this(new RestClient.Builder()
                .withBaseUrl(baseUrl)
                .withLogLevel(LogLevel.BODY_AND_HEADERS)
                .withSerializerAdapter(new JacksonAdapter())
                .build());
    }

    /**
     * Initializes a new instance of the ServiceClient class.
     *
     * @param restClient the REST client
     */
    protected ServiceClient(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * @return the {@link RestClient} instance.
     */
    public RestClient restClient() {
        return restClient;
    }

    /**
     * @return the {@link HttpClient} instance.
     */
    public HttpClient httpClient() {
        return this.restClient.httpClient();
    }

    /**
     * @return the adapter to a Jackson {@link com.fasterxml.jackson.databind.ObjectMapper}.
     */
    public SerializerAdapter<?> serializerAdapter() {
        return this.restClient.serializerAdapter();
    }
}
