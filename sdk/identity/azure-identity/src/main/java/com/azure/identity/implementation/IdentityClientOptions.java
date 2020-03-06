// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.ProxyOptions;

import java.time.Duration;
import java.util.function.Function;

/**
 * Options to configure the IdentityClient.
 */
public final class IdentityClientOptions {
    private static final String DEFAULT_AUTHORITY_HOST = "https://login.microsoftonline.com/";
    private static final int MAX_RETRY_DEFAULT_LIMIT = 3;

    private String authorityHost;
    private int maxRetry;
    private Function<Duration, Duration> retryTimeout;
    private ProxyOptions proxyOptions;
    private HttpPipeline httpPipeline;
    private Duration tokenRefreshOffset = Duration.ofMinutes(2);
    private HttpClient httpClient;

    /**
     * Creates an instance of IdentityClientOptions with default settings.
     */
    public IdentityClientOptions() {
        authorityHost = DEFAULT_AUTHORITY_HOST;
        maxRetry = MAX_RETRY_DEFAULT_LIMIT;
        retryTimeout = i -> Duration.ofSeconds((long) Math.pow(2, i.getSeconds() - 1));
    }

    /**
     * @return the Azure Active Directory endpoint to acquire tokens.
     */
    public String getAuthorityHost() {
        return authorityHost;
    }

    /**
     * Specifies the Azure Active Directory endpoint to acquire tokens.
     * @param authorityHost the Azure Active Directory endpoint
     * @return IdentityClientOptions
     */
    public IdentityClientOptions setAuthorityHost(String authorityHost) {
        this.authorityHost = authorityHost;
        return this;
    }

    /**
     * @return the max number of retries when an authentication request fails.
     */
    public int getMaxRetry() {
        return maxRetry;
    }

    /**
     * Specifies the max number of retries when an authentication request fails.
     * @param maxRetry the number of retries
     * @return IdentityClientOptions
     */
    public IdentityClientOptions setMaxRetry(int maxRetry) {
        this.maxRetry = maxRetry;
        return this;
    }

    /**
     * @return a Function to calculate seconds of timeout on every retried request.
     */
    public Function<Duration, Duration> getRetryTimeout() {
        return retryTimeout;
    }

    /**
     * Specifies a Function to calculate seconds of timeout on every retried request.
     * @param retryTimeout the Function that returns a timeout in seconds given the number of retry
     * @return IdentityClientOptions
     */
    public IdentityClientOptions setRetryTimeout(Function<Duration, Duration> retryTimeout) {
        this.retryTimeout = retryTimeout;
        return this;
    }

    /**
     * @return the options for proxy configuration.
     */
    public ProxyOptions getProxyOptions() {
        return proxyOptions;
    }

    /**
     * Specifies the options for proxy configuration.
     * @param proxyOptions the options for proxy configuration
     * @return IdentityClientOptions
     */
    public IdentityClientOptions setProxyOptions(ProxyOptions proxyOptions) {
        this.proxyOptions = proxyOptions;
        return this;
    }

    /**
     * @return the HttpPipeline to send all requests
     */
    public HttpPipeline getHttpPipeline() {
        return httpPipeline;
    }

    /**
     * @return the HttpClient to use for requests
     */
    public HttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * Specifies the HttpPipeline to send all requests. This setting overrides the others.
     * @param httpPipeline the HttpPipeline to send all requests
     * @return IdentityClientOptions
     */
    public IdentityClientOptions setHttpPipeline(HttpPipeline httpPipeline) {
        this.httpPipeline = httpPipeline;
        return this;
    }

    /**
     * @return how long before the actual token expiry to refresh the token.
     */
    public Duration getTokenRefreshOffset() {
        return tokenRefreshOffset;
    }

    /**
     * Sets how long before the actual token expiry to refresh the token. The
     * token will be considered expired at and after the time of (actual
     * expiry - token refresh offset). The default offset is 2 minutes.
     *
     * This is useful when network is congested and a request containing the
     * token takes longer than normal to get to the server.
     *
     * @param tokenRefreshOffset the duration before the actual expiry of a token to refresh it
     */
    public IdentityClientOptions setTokenRefreshOffset(Duration tokenRefreshOffset) {
        if (tokenRefreshOffset != null) {
            this.tokenRefreshOffset = tokenRefreshOffset;
        }
        return this;
    }

    /**
     * Specifies the HttpClient to send use for requests.
     * @param httpClient the http client to use for requests
     * @return IdentityClientOptions
     */
    public IdentityClientOptions setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }
}
