/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v3.http;

import com.microsoft.rest.v3.annotations.Beta;

/**
 * The set of parameters used to create an HTTP client.
 */
public class HttpClientConfiguration {
    private ProxyOptions proxy;
    private final SharedChannelPoolOptions poolOptions;

    /**
     * @return The optional proxy to use.
     */
    public ProxyOptions proxy() {
        return proxy;
    }

    /**
     * Creates an HttpClientConfiguration.
     */
    public HttpClientConfiguration() {
        this.poolOptions = new SharedChannelPoolOptions();
    }

    public HttpClientConfiguration withProxy(ProxyOptions proxyOptions) {
        this.proxy = proxyOptions;
        return this;
    }

    /**
     * Sets the duration in sec to keep the connection alive in available pool before closing it.
     *
     * @param duration duration in seconds
     * @return HttpClientConfiguration
     */
    @Beta(since = "2.0.0")
    public HttpClientConfiguration withIdleConnectionKeepAliveDurationInSec(long duration) {
        this.poolOptions.withIdleChannelKeepAliveDurationInSec(duration);
        return this;
    }
}
