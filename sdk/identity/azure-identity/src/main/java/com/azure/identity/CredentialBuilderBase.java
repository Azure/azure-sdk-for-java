// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.http.ProxyOptions;
import com.azure.identity.implementation.IdentityClientOptions;

import java.time.Duration;
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
     * @return itself
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
     * @return itself
     */
    @SuppressWarnings("unchecked")
    public T retryTimeout(Function<Duration, Duration> retryTimeout) {
        this.identityClientOptions.setRetryTimeout(retryTimeout);
        return (T) this;
    }

    /**
     * Specifies he options for proxy configuration.
     *
     * @param proxyOptions the options for proxy configuration
     * @return itself
     */
    @SuppressWarnings("unchecked")
    public T proxyOptions(ProxyOptions proxyOptions) {
        this.identityClientOptions.setProxyOptions(proxyOptions);
        return (T) this;
    }
}
