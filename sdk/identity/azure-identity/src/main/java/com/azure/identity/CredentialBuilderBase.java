// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

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
     * Allows to log the pii information in the application level logs.
     *
     * @return An updated instance of this builder with pii logging enabled.
     */
    @SuppressWarnings("unchecked")
    public T allowPiiLogging() {
        this.identityClientOptions.setAllowPiiLogging(true);
        return (T) this;
    }
}
