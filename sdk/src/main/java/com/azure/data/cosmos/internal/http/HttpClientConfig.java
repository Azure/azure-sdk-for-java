/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.azure.data.cosmos.internal.http;

import com.azure.data.cosmos.internal.Configs;

import java.net.InetSocketAddress;

/**
 * Helper class internally used for instantiating reactor netty http client.
 */
public class HttpClientConfig {
    public final static String REACTOR_NETWORK_LOG_CATEGORY = "com.azure.data.cosmos.netty-network";

    private final Configs configs;
    private Integer maxPoolSize;
    private Integer maxIdleConnectionTimeoutInMillis;
    private Integer requestTimeoutInMillis;
    private InetSocketAddress proxy;

    public HttpClientConfig(Configs configs) {
        this.configs = configs;
    }

    public HttpClientConfig withPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
        return this;
    }

    public HttpClientConfig withHttpProxy(InetSocketAddress proxy) {
        this.proxy = proxy;
        return this;
    }

    public HttpClientConfig withMaxIdleConnectionTimeoutInMillis(int maxIdleConnectionTimeoutInMillis) {
        this.maxIdleConnectionTimeoutInMillis = maxIdleConnectionTimeoutInMillis;
        return this;
    }

    public HttpClientConfig withRequestTimeoutInMillis(int requestTimeoutInMillis) {
        this.requestTimeoutInMillis = requestTimeoutInMillis;
        return this;
    }

    public Configs getConfigs() {
        return configs;
    }

    public Integer getMaxPoolSize() {
        return maxPoolSize;
    }

    public Integer getMaxIdleConnectionTimeoutInMillis() {
        return maxIdleConnectionTimeoutInMillis;
    }

    public Integer getRequestTimeoutInMillis() {
        return requestTimeoutInMillis;
    }

    public InetSocketAddress getProxy() {
        return proxy;
    }
}
