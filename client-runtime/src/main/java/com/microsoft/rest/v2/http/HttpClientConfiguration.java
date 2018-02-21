/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import java.net.Proxy;

/**
 * The set of parameters used to create an HTTP client.
 */
public class HttpClientConfiguration {
    private final Proxy proxy;

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
    }
}
