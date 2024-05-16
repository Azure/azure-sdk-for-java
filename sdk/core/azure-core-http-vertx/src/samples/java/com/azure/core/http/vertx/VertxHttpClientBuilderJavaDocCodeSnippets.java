// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx;

import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;

import java.net.InetSocketAddress;
import java.time.Duration;

/**
 * Code snippets for {@link VertxHttpClientBuilderJavaDocCodeSnippets}
 */
@SuppressWarnings("unused")
public class VertxHttpClientBuilderJavaDocCodeSnippets {

    /**
     * Code snippet for simple http client instantiation.
     */
    public void simpleInstantiation() {
        // BEGIN: com.azure.core.http.vertx.instantiation-simple
        HttpClient client = new VertxAsyncHttpClientBuilder()
                .build();
        // END: com.azure.core.http.vertx.instantiation-simple
    }

    public void proxySample() {
        // BEGIN: com.azure.core.http.vertx.vertxAsyncHttpClientBuilder.proxy#ProxyOptions
        final String proxyHost = "<proxy-host>"; // e.g. localhost
        final int proxyPort = 9999; // Proxy port
        ProxyOptions proxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP,
                new InetSocketAddress(proxyHost, proxyPort));
        HttpClient client = new VertxAsyncHttpClientBuilder()
                .proxy(proxyOptions)
                .build();
        // END: com.azure.core.http.vertx.vertxAsyncHttpClientBuilder.proxy#ProxyOptions

    }

    public void proxyBasicAuthenticationSample() {

        // BEGIN: com.azure.core.http.vertx.vertxAsyncHttpClientBuilder#setProxyAuthenticator
        final String proxyHost = "<proxy-host>"; // e.g. localhost
        final int proxyPort = 9999; // Proxy port
        final String proxyUser = "<proxy-user>";
        final String proxyPassword = "<proxy-password>";
        //
        ProxyOptions proxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP,
                new InetSocketAddress(proxyHost, proxyPort));
        proxyOptions = proxyOptions.setCredentials(proxyUser, proxyPassword);
        HttpClient client = new VertxAsyncHttpClientBuilder()
                .proxy(proxyOptions)
                .build();
        // END: com.azure.core.http.vertx.vertxAsyncHttpClientBuilder#setProxyAuthenticator

    }

    public void timeoutSample() {
        // BEGIN: com.azure.core.http.vertx.VertxAsyncHttpClientBuilder#timeoutSample
        HttpClient client = new VertxAsyncHttpClientBuilder()
            .connectTimeout(Duration.ofSeconds(10)) // Timeout of 10 seconds for establishing a connection
            .writeTimeout(Duration.ofSeconds(100)) // Timeout of 100 seconds when network writing is idle
            .responseTimeout(Duration.ofSeconds(30)) // Timeout of 30 seconds for the server to return a response
            .readTimeout(Duration.ofSeconds(100)) // Timeout of 100 seconds when network reading is idle
            .build();
        // END: com.azure.core.http.vertx.VertxAsyncHttpClientBuilder#timeoutSample

    }
}
