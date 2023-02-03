// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.ProxyOptions;
import com.azure.core.util.Configuration;
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
     * Sets the configuration store that is used during construction of the credential.
     *
     * The default configuration store is a clone of the {@link Configuration#getGlobalConfiguration() global
     * configuration store}.
     *
     * @param configuration The configuration store used to load Env variables and/or properties from.
     *
     * @return An updated instance of this builder with the configuration store set as specified.
     */
    @SuppressWarnings("unchecked")
    public T configuration(Configuration configuration) {
        identityClientOptions.setConfiguration(configuration);
        return (T) this;
    }

    /**
     * Enables account identifiers to be logged on client side for debugging/monitoring purposes.
     * By default, it is disabled.
     * <p>
     * The Account Identifier logs can contain sensitive information and should be enabled on protected machines only.
     * Enabling this logs Application ID, Object ID, Tenant ID and User Principal Name at INFO level when an
     * access token is successfully retrieved. Ensure that INFO level logs are enabled to
     * see the account identifier logs.
     * </p>
     *
     * @return An updated instance of this builder.
     */
    @SuppressWarnings("unchecked")
    public T enableAccountIdentifierLogging() {
        identityClientOptions
            .getIdentityLogOptionsImpl()
            .setLoggingAccountIdentifiersAllowed(true);
        return (T) this;
    }
}
