// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.logging.ClientLogger;
import com.azure.cs.textanalytics.implementation.TextAnalyticsAPIBuilder;
import com.azure.cs.textanalytics.implementation.TextAnalyticsAPIImpl;
import com.azure.cs.textanalytics.models.TextAnalyticsClientOptions;

import javax.security.auth.login.Configuration;
import java.util.ArrayList;
import java.util.List;

@ServiceClientBuilder(serviceClients = {TextAnalyticsAsyncClient.class, TextAnalyticsClient.class})
public final class TextAnalyticsClientBuilder {
    private static final String ECHO_REQUEST_ID_HEADER = "x-ms-return-client-request-id";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String CONTENT_TYPE_HEADER_VALUE = "application/json";
    private static final String ACCEPT_HEADER = "Accept";
    private static final String ACCEPT_HEADER_VALUE = "application/vnd.microsoft.azconfig.kv+json";

    private final ClientLogger logger = new ClientLogger(TextAnalyticsClientBuilder.class);
    private final List<HttpPipelinePolicy> policies;

    private String endpoint;
    private String connectionString;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private HttpPipeline httpPipeline;
    private Configuration configuration;
    private TextAnalyticsServiceVersion version;

    public TextAnalyticsClientBuilder() {
        policies = new ArrayList<>();
        httpLogOptions = new HttpLogOptions();
//        headers = new HttpHeaders()
//            .put(ECHO_REQUEST_ID_HEADER, "true")
//            .put(CONTENT_TYPE_HEADER, CONTENT_TYPE_HEADER_VALUE)
//            .put(ACCEPT_HEADER, ACCEPT_HEADER_VALUE);
    }

    public TextAnalyticsClient buildClient() {
        return new TextAnalyticsClient(buildAsyncClient());
    }

    public TextAnalyticsAsyncClient buildAsyncClient() {
        TextAnalyticsServiceVersion serviceVersion = version != null ? version : TextAnalyticsServiceVersion.getLatest();
        // TODO: build a pipeline.
        // (1) AAD Token, BearerTokenAuthenticationPolicy
        HttpPipeline pipeline = (httpPipeline != null) ? httpPipeline : null;

        // TODO: missing the version of the operation to use for this request.
        TextAnalyticsAPIImpl textAnalyticsAPI = new TextAnalyticsAPIBuilder()
            .endpoint(endpoint)
            .pipeline(pipeline)
            .build();

        return new TextAnalyticsAsyncClient(textAnalyticsAPI, serviceVersion);
//        Configuration buildConfiguration =
//            (configuration == null) ? Configuration.getGlobalConfiguration().clone() : configuration;
//        ConfigurationClientCredentials configurationCredentials = getConfigurationCredentials(buildConfiguration);
//        String buildEndpoint = getBuildEndpoint(configurationCredentials);
//
//        Objects.requireNonNull(buildEndpoint);
//
//        if (pipeline != null) {
//            return new TextAnalyticsAsyncClient(buildEndpoint, pipeline);
//        }
//
//        TextAnalyticsClientCredentials buildCredential = (credential == null) ? configurationCredentials : credential;
//        if (buildCredential == null) {
//            throw logger.logExceptionAsWarning(new IllegalStateException("'credential' is required."));
//        }
//
//        // Closest to API goes first, closest to wire goes last.
//        final List<HttpPipelinePolicy> policies = new ArrayList<>();
//
////        policies.add(new UserAgentPolicy(AzureConfiguration.NAME, AzureConfiguration.VERSION, buildConfiguration));
//        policies.add(new RequestIdPolicy());
//        policies.add(new AddHeadersPolicy(headers));
//        policies.add(new AddDatePolicy());
////        policies.add(new ConfigurationCredentialsPolicy(buildCredential));
//        HttpPolicyProviders.addBeforeRetryPolicies(policies);
//
//        policies.add(retryPolicy == null ? new RetryPolicy() : retryPolicy);
//
//        policies.addAll(this.policies);
//        HttpPolicyProviders.addAfterRetryPolicies(policies);
//        policies.add(new HttpLoggingPolicy(httpLogOptions));
//
//        HttpPipeline pipeline = new HttpPipelineBuilder()
//            .policies(policies.toArray(new HttpPipelinePolicy[0]))
//            .httpClient(httpClient)
//            .build();
//
//        return new ConfigurationAsyncClient(buildEndpoint, pipeline);
    }

    public TextAnalyticsClientBuilder endpoint(String endpoint) {
        return this;
    }

    public TextAnalyticsClientBuilder subscriptionKey(String subscriptionKey) {
        return this;
    }

    public TextAnalyticsClientBuilder credential(TokenCredential tokenCredential) {
        return this;
    }

    public TextAnalyticsClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        return this;
    }

    public TextAnalyticsClientBuilder addPolicy(HttpPipelinePolicy policy) {
        return this;
    }

    public TextAnalyticsClientBuilder httpClient(HttpClient client) {
        return this;
    }

    public TextAnalyticsClientBuilder pipeline(HttpPipeline pipeline) {
        return this;
    }

    public TextAnalyticsClientBuilder configuration(Configuration configuration) {
        return this;
    }

    public TextAnalyticsClientBuilder retryPolicy(HttpPipelinePolicy retryPolicy) {
        return this;
    }

    public TextAnalyticsClientBuilder serviceVersion(TextAnalyticsServiceVersion version) { return this;}

    public TextAnalyticsClientBuilder clientOptions(TextAnalyticsClientOptions clientOptions) {
        return this;
    }
}
