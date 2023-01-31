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
