// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpProtocolVersion;
import com.azure.core.http.ProxyOptions;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.ForkJoinPool;

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

    /**
     * Configures HTTP/2 with HTTP/1.1 fallback.
     */
    public void configureHttpVersion() {
        // BEGIN: readme-sample-configureHttpVersion
        HttpClient client = new JdkHttpClientBuilder()
            .maximumHttpVersion(HttpProtocolVersion.HTTP_2)
            .build();
        // END: readme-sample-configureHttpVersion
    }

    /**
     * Configures connection, write, response and read timeouts.
     */
    public void configureTimeouts() {
        // BEGIN: readme-sample-configureTimeouts
        HttpClient client = new JdkHttpClientBuilder()
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
        HttpClient client = new JdkHttpClientBuilder()
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
        HttpClient client = new JdkHttpClientBuilder()
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
        HttpClient client = new JdkHttpClientBuilder()
            .maximumHttpVersion(HttpProtocolVersion.HTTP_1_1)
            .build();
        // END: readme-sample-useHttp1
    }

    /**
     * Wraps an internal JDK builder using an application-selected executor.
     */
    public void customizeInternalBuilder() {
        // BEGIN: readme-sample-customizeInternalBuilder
        java.net.http.HttpClient.Builder internalBuilder = java.net.http.HttpClient.newBuilder()
            .executor(ForkJoinPool.commonPool());
        HttpClient client = new JdkHttpClientBuilder(internalBuilder).build();
        // END: readme-sample-customizeInternalBuilder
    }

}
