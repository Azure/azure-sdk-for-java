// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.cs.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.cs.textanalytics.implementation.TextAnalyticsClientImplBuilder;
import com.azure.cs.textanalytics.models.TextAnalyticsClientOptions;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@ServiceClientBuilder(serviceClients = {TextAnalyticsAsyncClient.class, TextAnalyticsClient.class})
public final class TextAnalyticsClientBuilder {
    private static final String ECHO_REQUEST_ID_HEADER = "x-ms-return-client-request-id";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String CONTENT_TYPE_HEADER_VALUE = "application/json";
    private static final String ACCEPT_HEADER = "Accept";
    private static final String ACCEPT_HEADER_VALUE = "application/json";
    private static final String TEXT_ANALYTICS_PROPERTIES = "azure-textanalytics.properties";
    private static final String OCP_APIM_SUBSCRIPTION_KEY = "Ocp-Apim-Subscription-Key";
    private static final String NAME = "name";
    private static final String VERSION = "version";
    private static final RetryPolicy DEFAULT_RETRY_POLICY = new RetryPolicy("retry-after-ms", ChronoUnit.MILLIS);
    private static final String DEFAULT_SCOPE = "https://cognitiveservices.azure.com/.default";

    private final ClientLogger logger = new ClientLogger(TextAnalyticsClientBuilder.class);
    private final List<HttpPipelinePolicy> policies;
    private final HttpHeaders headers;
    private final String clientName;
    private final String clientVersion;

    private String endpoint;
    private String subscriptionKey;
    private TokenCredential tokenCredential;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private HttpPipeline httpPipeline;
    private Configuration configuration;
    private HttpPipelinePolicy retryPolicy;
    private TextAnalyticsServiceVersion version;

    public TextAnalyticsClientBuilder() {
        policies = new ArrayList<>();
        httpLogOptions = new HttpLogOptions();

        Map<String, String> properties = CoreUtils.getProperties(TEXT_ANALYTICS_PROPERTIES);
        clientName = properties.getOrDefault(NAME, "UnknownName");
        clientVersion = properties.getOrDefault(VERSION, "UnknownVersion");

        headers = new HttpHeaders()
            .put(ECHO_REQUEST_ID_HEADER, "true")
            .put(CONTENT_TYPE_HEADER, CONTENT_TYPE_HEADER_VALUE)
            .put(ACCEPT_HEADER, ACCEPT_HEADER_VALUE);
    }

    public TextAnalyticsClient buildClient() {
        return new TextAnalyticsClient(buildAsyncClient());
    }

    public TextAnalyticsAsyncClient buildAsyncClient() {
        // Global Env configuration store
        Configuration buildConfiguration = (configuration == null)
            ? Configuration.getGlobalConfiguration().clone() : configuration;
        // Service Version
        TextAnalyticsServiceVersion serviceVersion =
            version != null ? version : TextAnalyticsServiceVersion.getLatest();

        // Endpoint
        String buildEndpoint = endpoint;

        // endpoint cannot be null, which is required in request authentication
        Objects.requireNonNull(buildEndpoint, "'Endpoint' is required and can not be null.");

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        if (tokenCredential != null) {
            // User token based policy
            policies.add(
                new BearerTokenAuthenticationPolicy(tokenCredential, DEFAULT_SCOPE));
        } else if (subscriptionKey != null) {
            headers.put(OCP_APIM_SUBSCRIPTION_KEY, subscriptionKey);
        } else {
            // Throw exception that credential and tokenCredential cannot be null
            logger.logExceptionAsError(
                new IllegalArgumentException("Missing credential information while building a client."));
        }


        policies.add(new UserAgentPolicy(httpLogOptions.getApplicationId(), clientName, clientVersion,
            buildConfiguration));
        policies.add(new RequestIdPolicy());
        policies.add(new AddHeadersPolicy(headers));
        policies.add(new AddDatePolicy());

        HttpPolicyProviders.addBeforeRetryPolicies(policies);

        policies.add(retryPolicy == null ? DEFAULT_RETRY_POLICY : retryPolicy);

        policies.addAll(this.policies);
        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(httpLogOptions));

        // customized pipeline
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();

        TextAnalyticsClientImpl textAnalyticsAPI = new TextAnalyticsClientImplBuilder()
            .endpoint(endpoint)
            .pipeline(pipeline)
            .build();

        return new TextAnalyticsAsyncClient(textAnalyticsAPI, serviceVersion);
    }

    public TextAnalyticsClientBuilder endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public TextAnalyticsClientBuilder subscriptionKey(String subscriptionKey) {
        this.subscriptionKey = subscriptionKey;
        return this;
    }

    public TextAnalyticsClientBuilder credential(TokenCredential tokenCredential) {
        this.tokenCredential = tokenCredential;
        return this;
    }

    public TextAnalyticsClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        this.httpLogOptions = logOptions;
        return this;
    }

    public TextAnalyticsClientBuilder addPolicy(HttpPipelinePolicy policy) {
        Objects.requireNonNull(policy);
        policies.add(policy);
        return this;
    }

    public TextAnalyticsClientBuilder httpClient(HttpClient client) {
        if (this.httpClient != null && client == null) {
            logger.info("HttpClient is being set to 'null' when it was previously configured.");
        }

        this.httpClient = client;
        return this;
    }

    public TextAnalyticsClientBuilder pipeline(HttpPipeline httpPipeline) {
        if (this.httpPipeline != null && httpPipeline == null) {
            logger.info("HttpPipeline is being set to 'null' when it was previously configured.");
        }

        this.httpPipeline = httpPipeline;
        return this;
    }

    public TextAnalyticsClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    public TextAnalyticsClientBuilder retryPolicy(HttpPipelinePolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
        return this;
    }

    public TextAnalyticsClientBuilder serviceVersion(TextAnalyticsServiceVersion version) {
        this.version = version;
        return this;
    }

    public TextAnalyticsClientBuilder clientOptions(TextAnalyticsClientOptions clientOptions) {
        return this;
    }
}
