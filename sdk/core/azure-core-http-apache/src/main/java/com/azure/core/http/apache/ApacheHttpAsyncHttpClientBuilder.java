// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.apache;

import com.azure.core.http.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.util.Objects;


/**
 * Builder class responsible for createing instance of {@link com.azure.core.http.HttpClient} backed by
 * Apache Http Client.
 */
public class ApacheHttpAsyncHttpClientBuilder {
    private final CloseableHttpClient apacheHttpClient;

    /**
     * Creates ApacheHttpAsyncHttpClientBuilder.
     */
    public ApacheHttpAsyncHttpClientBuilder() {
        apacheHttpClient = HttpClients.createDefault();
    }

    /**
     * Creates ApacheHttpAsyncHttpClientBuilder from the builder of an existing Apache HttpClient.
     * @param apacheHttpClient
     */
    public ApacheHttpAsyncHttpClientBuilder(CloseableHttpClient apacheHttpClient) {
        this.apacheHttpClient = Objects.requireNonNull(apacheHttpClient, "'apacheHttpClient' cannot be null.");
    }

    /**
     * Creates a new Apache Http backed {@link com.azure.core.http.HttpClient} instance on every call, using the
     * configuration set in the builder at the time of the build method call.
     *
     * @return a new Apache-Http backed {@link com.azure.core.http.HttpClient} instance.
     */
    public HttpClient build() {


        return new ApacheHttpAsyncHttpClient(httpClientBuilder.build());
    }

}
