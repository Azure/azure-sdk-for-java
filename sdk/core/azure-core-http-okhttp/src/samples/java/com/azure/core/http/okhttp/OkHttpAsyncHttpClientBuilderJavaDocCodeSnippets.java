// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp;

import com.azure.core.http.HttpClient;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;

/**
 * Code snippets for {@link OkHttpAsyncHttpClientBuilder}
 */
public class OkHttpAsyncHttpClientBuilderJavaDocCodeSnippets {

    private void simpleInstantiation() {

        // BEGIN: com.azure.core.http.okhttp.instantiation-simple
        HttpClient client = new OkHttpAsyncHttpClientBuilder()
                .build();
        // END: com.azure.core.http.okhttp.instantiation-simple
    }

    private void proxySample() {

        // BEGIN: com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder#setProxy
        final String proxyHost = "<proxy-host>"; // e.g. localhost
        final int proxyPort = 9999; // Proxy port
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyHost, proxyPort));
        HttpClient client = new OkHttpAsyncHttpClientBuilder()
                .setProxy(proxy)
                .build();
        // END: com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder#setProxy

    }

    private void proxyBasicAuthenticationSample() {

        // BEGIN: com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder#setProxyAuthenticator
        final String proxyHost = "<proxy-host>"; // e.g. localhost
        final int proxyPort = 9999; // Proxy port
        final String proxyUser = "<proxy-user>";
        final String proxyPassword = "<proxy-password>";
        //
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyHost, proxyPort));
        HttpClient client = new OkHttpAsyncHttpClientBuilder()
                .setProxy(proxy)
                .setProxyAuthenticator((route, response) -> {
                    String credential = Credentials.basic(proxyUser, proxyPassword);
                    return response.request().newBuilder()
                            .header("Proxy-Authorization", credential)
                            .build();
                })
                .build();
        // END: com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder#setProxyAuthenticator

    }

    private void connectionTimeoutSample() {

        // BEGIN: com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder#setConnectionTimeout
        final Duration connectionTimeout = Duration.ofSeconds(250); // connection timeout of 250 seconds
        HttpClient client = new OkHttpAsyncHttpClientBuilder()
                .setConnectionTimeout(connectionTimeout)
                .build();
        // END: com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder#setConnectionTimeout

    }

    private void readTimeoutTimeoutSample() {

        // BEGIN: com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder#setConnectionTimeout
        final Duration readTimeout = Duration.ofSeconds(100); // read timeout of 100 seconds
        HttpClient client = new OkHttpAsyncHttpClientBuilder()
                .setReadTimeout(readTimeout)
                .build();
        // END: com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder#setConnectionTimeout

    }

    private void usingExistingHttpClientSample() {

        // BEGIN: com.azure.core.http.okhttp.using-existing-okhttp
        // Create an OkHttpClient with connection timeout of 250 seconds.
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(Duration.ofSeconds(250))
                .build();
        // Use "okHttpClient" instance to create an azure-core HttpClient "client".
        // Both "okHttpClient" and "client" share same underlying resources such as
        // connection pool, thread pool.
        // "client" inherits connection timeout settings and add proxy.
        HttpClient client = new OkHttpAsyncHttpClientBuilder(okHttpClient)
                .setProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("<proxy-host>", 9999)))
                .build();
        // END: com.azure.core.http.okhttp.using-existing-okhttp

    }
}
