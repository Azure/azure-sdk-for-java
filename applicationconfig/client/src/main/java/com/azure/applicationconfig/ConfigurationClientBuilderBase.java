package com.azure.applicationconfig;

import com.azure.applicationconfig.credentials.ConfigurationClientCredentials;
import com.azure.common.http.HttpClient;
import com.azure.common.http.HttpHeaders;
import com.azure.common.http.HttpPipeline;
import com.azure.common.http.policy.HttpLogDetailLevel;
import com.azure.common.http.policy.HttpPipelinePolicy;
import com.azure.common.http.policy.RetryPolicy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

abstract class ConfigurationClientBuilderBase<T> {
    // This header tells the server to return the request id in the HTTP response. Useful for correlation with what
    // request was sent.
    private static final String ECHO_REQUEST_ID_HEADER = "x-ms-return-client-request-id";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String CONTENT_TYPE_HEADER_VALUE = "application/json";
    private static final String ACCEPT_HEADER = "Accept";
    private static final String ACCEPT_HEADER_VALUE = "application/vnd.microsoft.azconfig.kv+json";

    final List<HttpPipelinePolicy> policies;
    final HttpHeaders headers;

    ConfigurationClientCredentials credentials;
    URL serviceEndpoint;
    HttpClient httpClient;
    HttpLogDetailLevel httpLogDetailLevel;
    HttpPipeline pipeline;
    RetryPolicy retryPolicy;
    String userAgent;

    ConfigurationClientBuilderBase() {
        userAgent = AzureConfiguration.getUserAgentHeader(AzureConfiguration.NAME, AzureConfiguration.VERSION);
        retryPolicy = new RetryPolicy();
        httpLogDetailLevel = HttpLogDetailLevel.NONE;
        policies = new ArrayList<>();

        headers = new HttpHeaders();
        headers.set(ECHO_REQUEST_ID_HEADER, "true");
        headers.set(CONTENT_TYPE_HEADER, CONTENT_TYPE_HEADER_VALUE);
        headers.set(ACCEPT_HEADER, ACCEPT_HEADER_VALUE);
    }

    /**
     * Constructs a new instance of T every time build
     * @return
     */
    abstract T build();

    abstract ConfigurationClientBuilderBase serviceEndpoint(String serviceEndpoint) throws MalformedURLException;

    abstract ConfigurationClientBuilderBase credentials(ConfigurationClientCredentials credentials);

    abstract ConfigurationClientBuilderBase httpLogDetailLevel(HttpLogDetailLevel logLevel);

    abstract ConfigurationClientBuilderBase addPolicy(HttpPipelinePolicy policy);

    abstract ConfigurationClientBuilderBase httpClient(HttpClient client);

    abstract ConfigurationClientBuilderBase pipeline(HttpPipeline pipeline);
}
