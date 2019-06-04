package com.azure.identity;

import com.azure.core.http.ProxyOptions;

import java.util.function.Function;

/**
 * Options to configure the IdentityClient.
 */
public class IdentityClientOptions {
    private static final String DEFAULT_AUTHORITY_HOST = "https://login.microsoftonline.com/";
    private static final int MAX_RETRY_DEFAULT_LIMIT = 20;

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

    public String authorityHost() {
        return authorityHost;
    }

    public IdentityClientOptions authorityHost(String authorityHost) {
        this.authorityHost = authorityHost;
        return this;
    }

    public int maxRetry() {
        return maxRetry;
    }

    public IdentityClientOptions maxRetry(int maxRetry) {
        this.maxRetry = maxRetry;
        return this;
    }

    public Function<Integer, Integer> retryTimeout() {
        return retryTimeout;
    }

    public IdentityClientOptions retryTimeout(Function<Integer, Integer> retryTimeout) {
        this.retryTimeout = retryTimeout;
        return this;
    }

    public ProxyOptions proxyOptions() {
        return proxyOptions;
    }

    public IdentityClientOptions proxyOptions(ProxyOptions proxyOptions) {
        this.proxyOptions = proxyOptions;
        return this;
    }
}
