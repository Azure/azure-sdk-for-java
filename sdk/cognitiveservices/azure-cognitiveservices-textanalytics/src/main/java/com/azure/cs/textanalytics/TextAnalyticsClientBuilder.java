// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.logging.ClientLogger;

import javax.security.auth.login.Configuration;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ServiceClientBuilder(serviceClients = {TextAnalyticsAsyncClient.class, TextAnalyticsClient.class})
public final class TextAnalyticsClientBuilder {

    private static final String ECHO_REQUEST_ID_HEADER = "x-ms-return-client-request-id";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String CONTENT_TYPE_HEADER_VALUE = "application/json";
    private static final String ACCEPT_HEADER = "Accept";
    private static final String ACCEPT_HEADER_VALUE = "application/vnd.microsoft.azconfig.kv+json";

    private final ClientLogger logger = new ClientLogger(TextAnalyticsClientBuilder.class);
    private final List<HttpPipelinePolicy> policies;
    private final HttpHeaders headers;

    private String endpoint;
    private String connectionString;
    private HttpLogOptions httpLogOptions;
    private HttpClient httpClient;
    private HttpPipeline pipeline;
    private Configuration configuration;
    private RetryPolicy retryPolicy;

    public TextAnalyticsClientBuilder() {
        policies = new ArrayList<>();
        httpLogOptions = new HttpLogOptions();
        headers = new HttpHeaders()
            .put(ECHO_REQUEST_ID_HEADER, "true")
            .put(CONTENT_TYPE_HEADER, CONTENT_TYPE_HEADER_VALUE)
            .put(ACCEPT_HEADER, ACCEPT_HEADER_VALUE);
    }

    public TextAnalyticsClient buildClient() {
        return new TextAnalyticsClient(buildAsyncClient());
    }

    public TextAnalyticsAsyncClient buildAsyncClient() {
        Configuration buildConfiguration =
            (configuration == null) ? Configuration.getGlobalConfiguration().clone() : configuration;
        ConfigurationClientCredentials configurationCredentials = getConfigurationCredentials(buildConfiguration);
        String buildEndpoint = getBuildEndpoint(configurationCredentials);

        Objects.requireNonNull(buildEndpoint);

        if (pipeline != null) {
            return new TextAnalyticsAsyncClient(buildEndpoint, pipeline);
        }

        TextAnalyticsClientCredentials buildCredential = (credential == null) ? configurationCredentials : credential;
        if (buildCredential == null) {
            throw logger.logExceptionAsWarning(new IllegalStateException("'credential' is required."));
        }

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(new UserAgentPolicy(AzureConfiguration.NAME, AzureConfiguration.VERSION, buildConfiguration));
        policies.add(new RequestIdPolicy());
        policies.add(new AddHeadersPolicy(headers));
        policies.add(new AddDatePolicy());
        policies.add(new ConfigurationCredentialsPolicy(buildCredential));
        HttpPolicyProviders.addBeforeRetryPolicies(policies);

        policies.add(retryPolicy == null ? new RetryPolicy() : retryPolicy);

        policies.addAll(this.policies);
        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(httpLogOptions));

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();

        return new ConfigurationAsyncClient(buildEndpoint, pipeline);
    }

    // Properties
    public TextAnalyticsClientBuilder endpoint(String endpoint) {
        try {
            new URL(endpoint);
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("'endpoint' must be a valid URL"));
        }
        this.endpoint = endpoint;
        return this;
    }

    public TextAnalyticsClientBuilder connectionString(String connectionString) {
        this.connectionString = connectionString;
        return this;
    }

    public TextAnalyticsClientBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
        httpLogOptions = httpLogOptions;
        return this;
    }

    public TextAnalyticsClientBuilder addPolicy(HttpPipelinePolicy policy) {
        Objects.requireNonNull(policy);
        policies.add(policy);
        return this;
    }

    public TextAnalyticsClientBuilder httpClient(HttpClient httpClient) {
        if (this.httpClient != null && client == null) {
            logger.info("HttpClient is being set to 'null' when it was previously configured.");
        }

        this.httpClient = client;
        return this;
    }

    public TextAnalyticsClientBuilder pipeline(HttpPipeline pipeline) {
        if (this.pipeline != null && pipeline == null) {
            logger.info("HttpPipeline is being set to 'null' when it was previously configured.");
        }

        this.pipeline = pipeline;
        return this;
    }

    public TextAnalyticsClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    public TextAnalyticsClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
        return this;
    }

}
