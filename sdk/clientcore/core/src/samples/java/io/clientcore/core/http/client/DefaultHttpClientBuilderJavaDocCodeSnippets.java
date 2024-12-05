// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.client;

import io.clientcore.core.http.models.ProxyOptions;

import java.net.InetSocketAddress;
import java.time.Duration;

/**
 * Code snippets for {@link DefaultHttpClientBuilder}
 */
@SuppressWarnings("unused")
public class DefaultHttpClientBuilderJavaDocCodeSnippets {
    /**
     * Code snippet for simple http client instantiation.
     */
    public void simpleInstantiation() {
        // BEGIN: io.clientcore.core.http.client.instantiation-simple
        HttpClient client = new DefaultHttpClientBuilder()
            .build();
        // END: io.clientcore.core.http.client.instantiation-simple
    }

    public void proxySample() {
        // BEGIN: io.clientcore.core.http.client.DefaultHttpClientBuilder.proxy#ProxyOptions
        final String proxyHost = "<proxy-host>"; // e.g. localhost
        final int proxyPort = 9999; // Proxy port
        ProxyOptions proxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP,
            new InetSocketAddress(proxyHost, proxyPort));
        HttpClient client = new DefaultHttpClientBuilder()
            .proxy(proxyOptions)
            .build();
        // END: io.clientcore.core.http.client.DefaultHttpClientBuilder.proxy#ProxyOptions
    }

    public void proxyBasicAuthenticationSample() {

        // BEGIN: io.clientcore.core.http.client.DefaultHttpClientBuilder#setProxyAuthenticator
        final String proxyHost = "<proxy-host>"; // e.g. localhost
        final int proxyPort = 9999; // Proxy port
        final String proxyUser = "<proxy-user>";
        final String proxyPassword = "<proxy-password>";
        //
        ProxyOptions proxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP,
                new InetSocketAddress(proxyHost, proxyPort));
        proxyOptions = proxyOptions.setCredentials(proxyUser, proxyPassword);
        HttpClient client = new DefaultHttpClientBuilder()
                .proxy(proxyOptions)
                .build();
        // END: io.clientcore.core.http.client.DefaultHttpClientBuilder#setProxyAuthenticator

    }

    public void connectionTimeoutSample() {

        // BEGIN: io.clientcore.core.http.client.DefaultHttpClientBuilder.connectionTimeout#Duration
        HttpClient client = new DefaultHttpClientBuilder()
                .connectionTimeout(Duration.ofSeconds(250)) // connection timeout of 250 seconds
                .build();
        // END: io.clientcore.core.http.client.DefaultHttpClientBuilder.connectionTimeout#Duration

    }
}
