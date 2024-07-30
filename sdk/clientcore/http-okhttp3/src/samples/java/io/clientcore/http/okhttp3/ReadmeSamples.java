// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.okhttp3;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.ProxyOptions;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collections;

/**
 * Class containing code snippets that will be injected to README.md.
 */
public class ReadmeSamples {

    /**
     * Sample code for creating async OkHttp HTTP client.
     */
    public void createBasicClient() {
        // BEGIN: readme-sample-createBasicClient
        HttpClient client = new OkHttpHttpClientBuilder().build();
        // END: readme-sample-createBasicClient
    }

    /**
     * Sample code for creating async OkHttp HTTP client with proxy.
     */
    public void createProxyClient() {
        // BEGIN: readme-sample-createProxyClient
        HttpClient client = new OkHttpHttpClientBuilder()
            .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("<proxy-host>", 8888)))
            .build();
        // END: readme-sample-createProxyClient
    }

    /**
     * Sample code for creating async OkHttp HTTP client that supports both the HTTP/1.1 and HTTP/2 protocols, with
     * HTTP/2 being the preferred protocol.
     */
    public void useHttp2WithConfiguredOkHttpClient() {
        // BEGIN: readme-sample-useHttp2WithConfiguredOkHttpClient
        // Constructs an HttpClient that supports both HTTP/1.1 and HTTP/2 with HTTP/2 being the preferred protocol.
        // This is the default handling for OkHttp.
        HttpClient client = new OkHttpHttpClientBuilder(
            new OkHttpClient.Builder().protocols(Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1)).build())
            .build();
        // END: readme-sample-useHttp2WithConfiguredOkHttpClient
    }

    /**
     * Sample code for creating async OkHttp HTTP client that only supports HTTP/2.
     */
    public void useHttp2OnlyWithConfiguredOkHttpClient() {
        // BEGIN: readme-sample-useHttp2OnlyWithConfiguredOkHttpClient
        // Constructs an HttpClient that only supports HTTP/2.
        HttpClient client = new OkHttpHttpClientBuilder(
            new OkHttpClient.Builder().protocols(Collections.singletonList(Protocol.H2_PRIOR_KNOWLEDGE)).build())
            .build();
        // END: readme-sample-useHttp2OnlyWithConfiguredOkHttpClient
    }
}
