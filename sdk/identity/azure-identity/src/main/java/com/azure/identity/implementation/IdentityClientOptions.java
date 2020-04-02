// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.ProxyOptions;
import com.azure.core.util.Configuration;
import com.azure.identity.KnownAuthorityHosts;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;

/**
 * Options to configure the IdentityClient.
 */
public final class IdentityClientOptions {
    private static final int MAX_RETRY_DEFAULT_LIMIT = 3;

    private String authorityHost;
    private int maxRetry;
    private Function<Duration, Duration> retryTimeout;
    private ProxyOptions proxyOptions;
    private HttpPipeline httpPipeline;
    private ExecutorService executorService;
    private Duration tokenRefreshOffset = Duration.ofMinutes(2);
    private HttpClient httpClient;

    /**
     * Creates an instance of IdentityClientOptions with default settings.
     */
    public IdentityClientOptions() {
        Configuration configuration = Configuration.getGlobalConfiguration();
        authorityHost = configuration.get(Configuration.PROPERTY_AZURE_AUTHORITY_HOST, KnownAuthorityHosts.AZURE_CLOUD);
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
     * Specifies the ExecutorService to be used to execute the authentication requests.
     * Developer is responsible for maintaining the lifecycle of the ExecutorService.
     *
     * <p>
     * If this is not configured, the {@link ForkJoinPool#commonPool()} will be used which is
     * also shared with other application tasks. If the common pool is heavily used for other tasks, authentication
     * requests might starve and setting up this executor service should be considered.
     * </p>
     *
     * <p> The executor service and can be safely shutdown if the TokenCredential is no longer being used by the
     * Azure SDK clients and should be shutdown before the application exits. </p>
     *
     * @param executorService the executor service to use for executing authentication requests.
     * @return IdentityClientOptions
     */
    public IdentityClientOptions setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
        return this;
    }

    /**
     * @return the ExecutorService to execute authentication requests.
     */
    public ExecutorService getExecutorService() {
        return executorService;
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
     * @return IdentityClientOptions
     * @throws NullPointerException If {@code tokenRefreshOffset} is null.
     */
    public IdentityClientOptions setTokenRefreshOffset(Duration tokenRefreshOffset) {
        Objects.requireNonNull(tokenRefreshOffset, "The token refresh offset cannot be null.");
        this.tokenRefreshOffset = tokenRefreshOffset;
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
