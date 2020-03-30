// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.SerializerAdapter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@ServiceClientBuilder(serviceClients = RestClient.class)
public final class RestClientBuilder {

    private final ClientLogger logger = new ClientLogger(RestClientBuilder.class);

    private final List<HttpPipelinePolicy> policies;
    private TokenCredential credential;
    private HttpPipeline pipeline;
    private URL baseUrl;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private Configuration configuration;
    private SerializerAdapter serializerAdapter;
    private List<String> scopes;

    private final RetryPolicy retryPolicy;

    /**
     * The constructor with defaults.
     */
    public RestClientBuilder() {
        retryPolicy = new RetryPolicy();
        httpLogOptions = new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS);
        policies = new ArrayList<>();
        scopes = new ArrayList<>();
    }

    public RestClient buildClient() {
        if (pipeline != null) {
            return new RestClient(baseUrl, pipeline, this);
        }

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new UserAgentPolicy(httpLogOptions, configuration));
        if (this.credential != null) {
            policies.add(new AuthenticationPolicy(this.credential, this.getScopes()));
        }
        policies.add(new HttpLoggingPolicy(httpLogOptions));

        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        policies.add(retryPolicy);
        policies.addAll(this.policies);
        HttpPolicyProviders.addAfterRetryPolicies(policies);

//        httpClient = new NettyAsyncHttpClientBuilder()
//                .build();
        HttpPipeline pipeline = new HttpPipelineBuilder()
                .policies(policies.toArray(new HttpPipelinePolicy[0]))
                .httpClient(httpClient)
                .build();

        return new RestClient(baseUrl, pipeline, this);
    }

    public RestClientBuilder withBaseUrl(String baseUrl) {
        try {
            this.baseUrl = new URL(baseUrl);
        } catch (MalformedURLException e) {
            throw logger.logExceptionAsError(new IllegalArgumentException("The Azure Key Vault url is malformed.", e));
        }
        return this;
    }

    public RestClientBuilder withBaseUrl(AzureEnvironment environment, AzureEnvironment.Endpoint endpoint) {
        try {
            this.baseUrl = new URL(environment.url(endpoint));
        } catch (MalformedURLException e) {
            throw logger.logExceptionAsError(new IllegalArgumentException("The Azure Key Vault url is malformed.", e));
        }
        return this;
    }

    public RestClientBuilder withCredential(TokenCredential credential) {
        Objects.requireNonNull(credential);
        this.credential = credential;
        return this;
    }

    public RestClientBuilder withHttpLogOptions(HttpLogOptions logOptions) {
        httpLogOptions = logOptions;
        return this;
    }

    public RestClientBuilder withSerializerAdapter(SerializerAdapter serializerAdapter) {
        this.serializerAdapter = serializerAdapter;
        return this;
    }

    public RestClientBuilder withUserAgent(String userAgent) {
        throw new UnsupportedOperationException();
    }

    public RestClientBuilder withPolicy(HttpPipelinePolicy policy) {
        Objects.requireNonNull(policy);
        policies.add(policy);
        return this;
    }

    public RestClientBuilder withHttpClient(HttpClient client) {
        Objects.requireNonNull(client);
        this.httpClient = client;
        return this;
    }

    public RestClientBuilder withHttpPipeline(HttpPipeline pipeline) {
        Objects.requireNonNull(pipeline);
        this.pipeline = pipeline;
        return this;
    }

    public RestClientBuilder withConfiguration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    public RestClientBuilder withScope(String scope) {
        this.scopes.add(scope);
        return this;
    }

    public String[] getScopes() {
        return this.scopes.isEmpty() ? null : this.scopes.toArray(new String[0]);
    }

    public SerializerAdapter getSerializerAdapter() {
        return this.serializerAdapter;
    }

    /**
     * @return the credentials attached to this REST client
     */
    public TokenCredential getCredential() {
        return this.credential;
    }

    public RestClientBuilder clone() {
        RestClientBuilder builder = new RestClientBuilder();
        builder.baseUrl = this.baseUrl;
        builder.credential = this.credential;
        builder.pipeline = this.pipeline;
        builder.policies.addAll(this.policies);
        builder.httpClient = this.httpClient;
        builder.httpLogOptions = this.httpLogOptions;
        builder.configuration = this.configuration;
        builder.serializerAdapter = this.serializerAdapter;
        builder.scopes.addAll(this.scopes);
        return builder;
    }
}
