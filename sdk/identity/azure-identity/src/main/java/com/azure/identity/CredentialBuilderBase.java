// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.SimpleTokenCache;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.ProxyOptions;
import com.azure.identity.implementation.IdentityClientOptions;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Function;

/**
 * The base class for all the credential builders.
 * @param <T> the type of the credential builder
 */
public abstract class CredentialBuilderBase<T extends CredentialBuilderBase<T>> {
    IdentityClientOptions identityClientOptions;

    CredentialBuilderBase() {
        this.identityClientOptions = new IdentityClientOptions();
    }

    /**
     * Specifies the max number of retries when an authentication request fails.
     *
     * @param maxRetry the number of retries
     * @return An updated instance of this builder with the max retry set as specified.
     */
    @SuppressWarnings("unchecked")
    public T maxRetry(int maxRetry) {
        this.identityClientOptions.setMaxRetry(maxRetry);
        return (T) this;
    }

    /**
     * Specifies a Function to calculate seconds of timeout on every retried request.
     *
     * @param retryTimeout the Function that returns a timeout in seconds given the number of retry
     * @return An updated instance of this builder with the retry timeout set as specified.
     */
    @SuppressWarnings("unchecked")
    public T retryTimeout(Function<Duration, Duration> retryTimeout) {
        this.identityClientOptions.setRetryTimeout(retryTimeout);
        return (T) this;
    }


    /**
     * Specifies the options for proxy configuration.
     *
     * @deprecated Configure the proxy options on the {@link HttpClient} instead and then set that
     * client on the credential using {@link #httpClient(HttpClient)}.
     *
     * @param proxyOptions the options for proxy configuration
     * @return An updated instance of this builder with the proxy options set as specified.
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public T proxyOptions(ProxyOptions proxyOptions) {
        this.identityClientOptions.setProxyOptions(proxyOptions);
        return (T) this;
    }

    /**
     * Specifies the HttpPipeline to send all requests. This setting overrides the others.
     *
     * @param httpPipeline the HttpPipeline to send all requests
     * @return An updated instance of this builder with the http pipeline set as specified.
     */
    @SuppressWarnings("unchecked")
    public T httpPipeline(HttpPipeline httpPipeline) {
        this.identityClientOptions.setHttpPipeline(httpPipeline);
        return (T) this;
    }

    /**
     * Sets the HTTP client to use for sending and receiving requests to and from the service.
     *
     * @param client The HTTP client to use for requests.
     * @return An updated instance of this builder with the http client set as specified.
     * @throws NullPointerException If {@code client} is {@code null}.
     */
    @SuppressWarnings("unchecked")
    public T httpClient(HttpClient client) {
        Objects.requireNonNull(client);
        this.identityClientOptions.setHttpClient(client);
        return (T) this;
    }

    /**
     * Sets how long before the actual token expiry to refresh the token. The
     * token will be considered expired at and after the time of (actual
     * expiry - token refresh offset). The default offset is 2 minutes.
     *
     * This is used in {@link SimpleTokenCache} and {@link com.azure.core.http.policy.BearerTokenAuthenticationPolicy}
     * to proactively retrieve a more up-to-date token before the cached token gets too close to its expiry.
     *
     * Extending this offset is recommended if it takes > 2 minutes to reach the service (application is running on
     * high load), or you would like to simply keep a more up-to-date token in the cache (more robust against token
     * refresh API down times). The user is responsible for specifying a valid offset.
     *
     * When a proactive token refresh fails but the previously cached token is still valid,
     * {@link com.azure.core.http.policy.BearerTokenAuthenticationPolicy} will NOT fail but return the previous valid
     * token. Another proactive refresh will be attempted in 30 seconds.
     *
     * @param tokenRefreshOffset the duration before the actual expiry of a token to refresh it
     * @return An updated instance of this builder with the token refresh offset set as specified.
     */
    @SuppressWarnings("unchecked")
    public T tokenRefreshOffset(Duration tokenRefreshOffset) {
        this.identityClientOptions.setTokenRefreshOffset(tokenRefreshOffset);
        return (T) this;
    }
}
