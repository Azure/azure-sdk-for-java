// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx;

import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;
import io.vertx.core.http.HttpClientOptions;

import java.net.InetSocketAddress;
import java.time.Duration;

/**
 * Class containing code snippets that will be injected to README.md.
 */
public class ReadmeSamples {

    /**
     * Sample code for creating Vert.x HTTP client.
     */
    public void createBasicClient() {
        // BEGIN: readme-sample-createBasicClient
        HttpClient client = new VertxHttpClientBuilder().build();
        // END: readme-sample-createBasicClient
    }

    /**
     * Sample code for create Vert.x HTTP client with connection timeout.
     */
    public void createClientWithConnectionTimeout() {
        // BEGIN: readme-sample-createClientWithConnectionTimeout
        HttpClient client = new VertxHttpClientBuilder().connectTimeout(Duration.ofSeconds(60)).build();
        // END: readme-sample-createClientWithConnectionTimeout
    }

    /**
     * Sample code for creating Vert.x HTTP client with proxy.
     */
    public void createProxyClient() {
        // BEGIN: readme-sample-createProxyClient
        HttpClient client = new VertxHttpClientBuilder()
            .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("<proxy-host>", 8888)))
            .build();
        // END: readme-sample-createProxyClient
    }

    /**
     * Sample for creating a Vert.x HTTP client with a customized max header size.
     * <p>
     * {@code maxHeaderSize} is used to determine the maximum headers size Vert.x can process. The default value is 8192
     * bytes (8KB). If the headers exceed this size, Vert.x will throw an exception. Passing a customized Vert.x
     * HttpClientOptions to the VertxHttpClientBuilder allows you to set a different value for this parameter.
     */
    public void overrideMaxHeaderSize() {
        // BEGIN: readme-sample-customMaxHeaderSize
        // Constructs an HttpClient with a modified max header size.
        // This creates a Vert.x HttpClient with a max headers size of 256 KB.
        // NOTE: If httpClientOptions is set, all other options set in the VertxHttpClientBuilder will be ignored.
        HttpClient httpClient = new VertxHttpClientBuilder()
            .httpClientOptions(new HttpClientOptions().setMaxHeaderSize(256 * 1024))
            .build();
        // END: readme-sample-customMaxHeaderSize
    }
}
