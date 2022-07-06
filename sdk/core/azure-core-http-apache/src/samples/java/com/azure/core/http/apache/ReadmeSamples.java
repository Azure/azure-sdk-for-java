// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.apache;

import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;

import java.net.InetSocketAddress;

/**
 * Class containing code snippets that will be injected to README.md.
 */
public class ReadmeSamples {

    /**
     * Sample code for creating async Apache HTTP client.
     */
    public void createBasicClient() {
        // BEGIN: readme-sample-createBasicClient
        HttpClient client = new ApacheHttpAsyncHttpClientBuilder().build();
        // END: readme-sample-createBasicClient
    }

    /**
     * Sample code for creating async Apache HTTP client with proxy.
     */
    public void createProxyClient() {
        // BEGIN: readme-sample-createProxyClient
        HttpClient client = new ApacheHttpAsyncHttpClientBuilder()
                                .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("<proxy-host>", 8888)))
                                .build();
        // END: readme-sample-createProxyClient
    }
}
