// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpProtocolVersion;
import com.azure.core.http.ProxyOptions;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;

import java.net.InetSocketAddress;
import java.time.Duration;
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
        HttpClient client = new OkHttpAsyncHttpClientBuilder().build();
        // END: readme-sample-createBasicClient
    }

    /**
     * Sample code for creating async OkHttp HTTP client with proxy.
     */
    public void createProxyClient() {
        // BEGIN: readme-sample-createProxyClient
        HttpClient client = new OkHttpAsyncHttpClientBuilder()
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
        HttpClient client = new OkHttpAsyncHttpClientBuilder(new OkHttpClient.Builder()
            .protocols(Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1))
            .build())
            .build();
        // END: readme-sample-useHttp2WithConfiguredOkHttpClient
    }

    /**
     * Sample code for creating an OkHttp HTTP client that only supports cleartext HTTP/2.
     */
    public void useHttp2OnlyWithConfiguredOkHttpClient() {
        // BEGIN: readme-sample-useHttp2OnlyWithConfiguredOkHttpClient
        // Constructs a cleartext HTTP/2-only client. HTTPS and HTTP/1.1 fallback are not supported.
        HttpClient client = new OkHttpAsyncHttpClientBuilder(new OkHttpClient.Builder()
            .protocols(Collections.singletonList(Protocol.H2_PRIOR_KNOWLEDGE))
            .build())
            .build();
        // END: readme-sample-useHttp2OnlyWithConfiguredOkHttpClient
    }

    /**
     * Configures HTTP/2 with HTTP/1.1 fallback.
     */
    public void configureHttpVersion() {
        // BEGIN: readme-sample-configureHttpVersion
        HttpClient client = new OkHttpAsyncHttpClientBuilder()
            .maximumHttpVersion(HttpProtocolVersion.HTTP_2)
            .build();
        // END: readme-sample-configureHttpVersion
    }

    /**
     * Configures connection, write, response and read timeouts.
     */
    public void configureTimeouts() {
        // BEGIN: readme-sample-configureTimeouts
        HttpClient client = new OkHttpAsyncHttpClientBuilder()
            .connectionTimeout(Duration.ofSeconds(60))
            .writeTimeout(Duration.ofSeconds(120))
            .responseTimeout(Duration.ofSeconds(60))
            .readTimeout(Duration.ofSeconds(120))
            .build();
        // END: readme-sample-configureTimeouts
    }

    /**
     * Configures an authenticated HTTP proxy.
     */
    public void createAuthenticatedProxyClient() {
        // BEGIN: readme-sample-createAuthenticatedProxyClient
        HttpClient client = new OkHttpAsyncHttpClientBuilder()
            .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("<proxy-host>", 8888))
                .setCredentials("<username>", "<password>"))
            .build();
        // END: readme-sample-createAuthenticatedProxyClient
    }

    /**
     * Configures hosts that bypass the HTTP proxy.
     */
    public void createProxyWithNonProxyHostsClient() {
        // BEGIN: readme-sample-createProxyWithNonProxyHostsClient
        HttpClient client = new OkHttpAsyncHttpClientBuilder()
            .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("<proxy-host>", 8888))
                .setNonProxyHosts("<nonProxyHostRegex>"))
            .build();
        // END: readme-sample-createProxyWithNonProxyHostsClient
    }

    /**
     * Restricts the client to HTTP/1.1.
     */
    public void useHttp1() {
        // BEGIN: readme-sample-useHttp1
        HttpClient client = new OkHttpAsyncHttpClientBuilder()
            .maximumHttpVersion(HttpProtocolVersion.HTTP_1_1)
            .build();
        // END: readme-sample-useHttp1
    }

    /**
     * Wraps an internal OkHttp client with connection retries disabled.
     */
    public void customizeInternalClient() {
        // BEGIN: readme-sample-customizeInternalClient
        OkHttpClient internalClient = new OkHttpClient.Builder()
            .retryOnConnectionFailure(false)
            .build();
        HttpClient client = new OkHttpAsyncHttpClientBuilder(internalClient).build();
        // END: readme-sample-customizeInternalClient
    }
}
