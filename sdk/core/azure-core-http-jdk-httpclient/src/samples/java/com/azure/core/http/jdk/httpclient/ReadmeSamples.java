// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient;

import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;
import java.net.InetSocketAddress;
import java.time.Duration;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 *
 * Class containing code snippets that will be injected to README.md.
 */
public class ReadmeSamples {

    /**
     * Sample code for creating async JDK HttpClient.
     */
    public void createBasicClient() {
        HttpClient client = new JdkAsyncHttpClientBuilder().build();
    }

    /**
     * Sample code for create async JDK HttpClient with connection timeout.
     */
    public void createClientWithConnectionTimeout() {
        HttpClient client = new JdkAsyncHttpClientBuilder().connectionTimeout(Duration.ofSeconds(60)).build();
    }

    /**
     * Sample code for creating async JDK HttpClient with proxy.
     */
    public void createProxyClient() {
        HttpClient client = new JdkAsyncHttpClientBuilder()
            .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("<proxy-host>", 8888)))
            .build();
    }

}
