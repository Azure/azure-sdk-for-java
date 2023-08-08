// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient;

import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;

import java.net.InetSocketAddress;
import java.time.Duration;

/**
 * Code snippets for {@link JdkHttpClientBuilder}
 */
public class JdkHttpClientBuilderJavaDocCodeSnippets {

    /**
     * Code snippet for simple http client instantiation.
     */
    public void simpleInstantiation() {
        // BEGIN: com.azure.core.http.jdk.httpclient.instantiation-simple
        HttpClient client = new JdkHttpClientBuilder()
                .build();
        // END: com.azure.core.http.jdk.httpclient.instantiation-simple
    }

    public void proxySample() {
        // BEGIN: com.azure.core.http.jdk.httpclient.JdkHttpClientBuilder.proxy#ProxyOptions
        final String proxyHost = "<proxy-host>"; // e.g. localhost
        final int proxyPort = 9999; // Proxy port
        ProxyOptions proxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP,
                new InetSocketAddress(proxyHost, proxyPort));
        HttpClient client = new JdkHttpClientBuilder()
                .proxy(proxyOptions)
                .build();
        // END: com.azure.core.http.jdk.httpclient.JdkHttpClientBuilder.proxy#ProxyOptions

    }

    public void proxyBasicAuthenticationSample() {

        // BEGIN: com.azure.core.http.jdk.httpclient.JdkHttpClientBuilder#setProxyAuthenticator
        final String proxyHost = "<proxy-host>"; // e.g. localhost
        final int proxyPort = 9999; // Proxy port
        final String proxyUser = "<proxy-user>";
        final String proxyPassword = "<proxy-password>";
        //
        ProxyOptions proxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP,
                new InetSocketAddress(proxyHost, proxyPort));
        proxyOptions = proxyOptions.setCredentials(proxyUser, proxyPassword);
        HttpClient client = new JdkHttpClientBuilder()
                .proxy(proxyOptions)
                .build();
        // END: com.azure.core.http.jdk.httpclient.JdkHttpClientBuilder#setProxyAuthenticator

    }

    public void connectionTimeoutSample() {

        // BEGIN: com.azure.core.http.jdk.httpclient.JdkHttpClientBuilder.connectionTimeout#Duration
        HttpClient client = new JdkHttpClientBuilder()
                .connectionTimeout(Duration.ofSeconds(250)) // connection timeout of 250 seconds
                .build();
        // END: com.azure.core.http.jdk.httpclient.JdkHttpClientBuilder.connectionTimeout#Duration

    }
}
