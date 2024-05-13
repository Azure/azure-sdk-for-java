// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.jdk.httpclient;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.ProxyOptions;

import java.net.InetSocketAddress;
import java.time.Duration;

/**
 * Class containing code snippets that will be injected to README.md.
 */
public class ReadmeSamples {

    /**
     * Sample code for creating async JDK HttpClient.
     */
    public void createBasicClient() {
        // BEGIN: readme-sample-createBasicClient
        HttpClient client = new JdkHttpClientBuilder().build();
        // END: readme-sample-createBasicClient
    }

    /**
     * Sample code for create async JDK HttpClient with connection timeout.
     */
    public void createClientWithConnectionTimeout() {
        // BEGIN: readme-sample-createClientWithConnectionTimeout
        HttpClient client = new JdkHttpClientBuilder().connectionTimeout(Duration.ofSeconds(60)).build();
        // END: readme-sample-createClientWithConnectionTimeout
    }

    /**
     * Sample code for creating async JDK HttpClient with proxy.
     */
    public void createProxyClient() {
        // BEGIN: readme-sample-createProxyClient
        HttpClient client = new JdkHttpClientBuilder()
            .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("<proxy-host>", 8888)))
            .build();
        // END: readme-sample-createProxyClient
    }

}
