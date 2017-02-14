/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest;

import com.microsoft.rest.protocol.SerializerAdapter;
import com.microsoft.rest.serializer.JacksonAdapter;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

/**
 * ServiceClient is the abstraction for accessing REST operations and their payload data types.
 */
public abstract class ServiceClient {
    /**
     * The RestClient instance storing all information needed for making REST calls.
     */
    private RestClient restClient;

    /**
     * Initializes a new instance of the ServiceClient class.
     *
     * @param baseUrl the service endpoint
     */
    protected ServiceClient(String baseUrl) {
        this(baseUrl, new OkHttpClient.Builder(), new Retrofit.Builder());
    }

    /**
     * Initializes a new instance of the ServiceClient class.
     *
     * @param baseUrl the service base uri
     * @param clientBuilder the http client builder
     * @param restBuilder the retrofit rest client builder
     */
    protected ServiceClient(String baseUrl, OkHttpClient.Builder clientBuilder, Retrofit.Builder restBuilder) {
        this(new RestClient.Builder(clientBuilder, restBuilder)
                .withBaseUrl(baseUrl)
                .withResponseBuilderFactory(new ServiceResponseBuilder.Factory())
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
     * @return the Retrofit instance.
     */
    public Retrofit retrofit() {
        return restClient.retrofit();
    }

    /**
     * @return the HTTP client.
     */
    public OkHttpClient httpClient() {
        return this.restClient.httpClient();
    }

    /**
     * @return the adapter to a Jackson {@link com.fasterxml.jackson.databind.ObjectMapper}.
     */
    public SerializerAdapter<?> serializerAdapter() {
        return this.restClient.serializerAdapter();
    }
}
