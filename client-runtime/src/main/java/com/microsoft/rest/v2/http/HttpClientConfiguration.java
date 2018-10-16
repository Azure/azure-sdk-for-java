/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import com.microsoft.rest.v2.annotations.Beta;

import java.net.Proxy;

/**
 * The set of parameters used to create an HTTP client.
 */
public class HttpClientConfiguration {
    private final Proxy proxy;
    private SharedChannelPoolOptions poolOptions;

    /**
     * @return The optional proxy to use.
     */
    public Proxy proxy() {
        return proxy;
    }

    /**
     * Creates an HttpClientConfiguration.
     * @param proxy The optional proxy to use.
     */
    public HttpClientConfiguration(Proxy proxy) {
        this.proxy = proxy;
        this.poolOptions = new SharedChannelPoolOptions();
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
