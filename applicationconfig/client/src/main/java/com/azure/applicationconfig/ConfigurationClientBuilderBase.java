// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.applicationconfig;

import com.azure.applicationconfig.credentials.ConfigurationClientCredentials;
import com.azure.applicationconfig.policy.ConfigurationCredentialsPolicy;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.AsyncCredentialsPolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Base constructor that enables creation of {@link ConfigurationRawAsyncClient}, {@link ConfigurationRawClient},
 * {@link ConfigurationAsyncClient}, and {@link ConfigurationClient}.
 */
class ConfigurationClientBuilderBase {
    // This header tells the server to return the request id in the HTTP response. Useful for correlation with what
    // request was sent.
    private static final String ECHO_REQUEST_ID_HEADER = "x-ms-return-client-request-id";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String CONTENT_TYPE_HEADER_VALUE = "application/json";
    private static final String ACCEPT_HEADER = "Accept";
    private static final String ACCEPT_HEADER_VALUE = "application/vnd.microsoft.azconfig.kv+json";

    private final List<HttpPipelinePolicy> policies;
    private final HttpHeaders headers;

    private ConfigurationClientCredentials credentials;
    private URL serviceEndpoint;
    private HttpClient httpClient;
    private HttpLogDetailLevel httpLogDetailLevel;
    private HttpPipeline pipeline;
    private RetryPolicy retryPolicy;

    ConfigurationClientBuilderBase() {
        retryPolicy = new RetryPolicy();
        httpLogDetailLevel = HttpLogDetailLevel.NONE;
        policies = new ArrayList<>();

        headers = new HttpHeaders()
            .put(ECHO_REQUEST_ID_HEADER, "true")
            .put(CONTENT_TYPE_HEADER, CONTENT_TYPE_HEADER_VALUE)
            .put(ACCEPT_HEADER, ACCEPT_HEADER_VALUE);
    }

    ConfigurationRawAsyncClient build() {
        Objects.requireNonNull(serviceEndpoint);

        if (pipeline != null) {
            return new ConfigurationRawAsyncClient(serviceEndpoint, pipeline);
        }

        if (credentials == null) {
            throw new IllegalStateException("'credentials' is required.");
        }

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(new UserAgentPolicy(AzureConfiguration.NAME, AzureConfiguration.VERSION));
        policies.add(new RequestIdPolicy());
        policies.add(new AddHeadersPolicy(headers));
        policies.add(new AddDatePolicy());
        policies.add(new ConfigurationCredentialsPolicy());
        policies.add(new AsyncCredentialsPolicy(credentials));
        policies.add(retryPolicy);

        policies.addAll(this.policies);

        policies.add(new HttpLoggingPolicy(httpLogDetailLevel));

        HttpPipeline pipeline = HttpPipeline.builder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();

        return new ConfigurationRawAsyncClient(serviceEndpoint, pipeline);
    }

    ConfigurationClientBuilderBase serviceEndpoint(String serviceEndpoint) throws MalformedURLException {
        this.serviceEndpoint = new URL(serviceEndpoint);
        return this;
    }

    ConfigurationClientBuilderBase credentials(ConfigurationClientCredentials credentials) {
        Objects.requireNonNull(credentials);
        this.credentials = credentials;
        this.serviceEndpoint = credentials.baseUri();
        return this;
    }

    ConfigurationClientBuilderBase httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        httpLogDetailLevel = logLevel;
        return this;
    }

    ConfigurationClientBuilderBase addPolicy(HttpPipelinePolicy policy) {
        Objects.requireNonNull(policy);
        policies.add(policy);
        return this;
    }

    ConfigurationClientBuilderBase httpClient(HttpClient client) {
        Objects.requireNonNull(client);
        this.httpClient = client;
        return this;
    }

    ConfigurationClientBuilderBase pipeline(HttpPipeline pipeline) {
        Objects.requireNonNull(pipeline);
        this.pipeline = pipeline;
        return this;
    }
}
