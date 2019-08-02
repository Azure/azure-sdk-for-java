// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.http.ProxyOptions;

import java.util.function.Function;

/**
 * Options to configure the IdentityClient.
 */
public final class IdentityClientOptions {
    private static final String DEFAULT_AUTHORITY_HOST = "https://login.microsoftonline.com/";
    private static final int MAX_RETRY_DEFAULT_LIMIT = 3;

    private String authorityHost;
    private int maxRetry;
    private Function<Integer, Integer> retryTimeout;
    private ProxyOptions proxyOptions;

    /**
     * Creates an instance of IdentityClientOptions with default settings.
     */
    public IdentityClientOptions() {
        authorityHost = DEFAULT_AUTHORITY_HOST;
        maxRetry = MAX_RETRY_DEFAULT_LIMIT;
        retryTimeout = i -> (int) Math.pow(2, i - 1);
    }

    /**
     * @return the Azure Active Directory endpoint to acquire tokens.
     */
    public String authorityHost() {
        return authorityHost;
    }

    /**
     * Specifies the Azure Active Directory endpoint to acquire tokens.
     * @param authorityHost the Azure Active Directory endpoint
     * @return IdentityClientOptions
     */
    public IdentityClientOptions authorityHost(String authorityHost) {
        this.authorityHost = authorityHost;
        return this;
    }

    /**
     * @return the max number of retries when an authentication request fails.
     */
    public int maxRetry() {
        return maxRetry;
    }

    /**
     * Specifies the max number of retries when an authentication request fails.
     * @param maxRetry the number of retries
     * @return IdentityClientOptions
     */
    public IdentityClientOptions maxRetry(int maxRetry) {
        this.maxRetry = maxRetry;
        return this;
    }

    /**
     * @return a Function to calculate seconds of timeout on every retried request.
     */
    public Function<Integer, Integer> retryTimeout() {
        return retryTimeout;
    }

    /**
     * Specifies a Function to calculate seconds of timeout on every retried request.
     * @param retryTimeout the Function that returns a timeout in seconds given the number of retry
     * @return IdentityClientOptions
     */
    public IdentityClientOptions retryTimeout(Function<Integer, Integer> retryTimeout) {
        this.retryTimeout = retryTimeout;
        return this;
    }

    /**
     * @return the options for proxy configuration.
     */
    public ProxyOptions proxyOptions() {
        return proxyOptions;
    }

    /**
     * Specifies he options for proxy configuration.
     * @param proxyOptions the options for proxy configuration
     * @return IdentityClientOptions
     */
    public IdentityClientOptions proxyOptions(ProxyOptions proxyOptions) {
        this.proxyOptions = proxyOptions;
        return this;
    }
}
