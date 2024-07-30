// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.okhttp3;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.ProxyOptions;
import okhttp3.OkHttpClient;

import java.net.InetSocketAddress;
import java.time.Duration;

/**
 * Code snippets for {@link OkHttpHttpClientBuilder}
 */
public class OkHttpHttpClientBuilderJavaDocCodeSnippets {

    /**
     * Code snippet for simple http client instantiation.
     */
    public void simpleInstantiation() {
        // BEGIN: io.clientcore.http.okhttp3.instantiation-simple
        HttpClient client = new OkHttpHttpClientBuilder().build();
        // END: io.clientcore.http.okhttp3.instantiation-simple
    }

    public void proxySample() {
        // BEGIN: io.clientcore.http.okhttp3.OkHttpHttpClientBuilder.proxy#ProxyOptions
        final String proxyHost = "<proxy-host>"; // e.g. localhost
        final int proxyPort = 9999; // Proxy port
        ProxyOptions proxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP,
            new InetSocketAddress(proxyHost, proxyPort));
        HttpClient client = new OkHttpHttpClientBuilder().proxy(proxyOptions).build();
        // END: io.clientcore.http.okhttp3.OkHttpHttpClientBuilder.proxy#ProxyOptions

    }

    public void proxyBasicAuthenticationSample() {

        // BEGIN: io.clientcore.http.okhttp3.OkHttpHttpClientBuilder#setProxyAuthenticator
        final String proxyHost = "<proxy-host>"; // e.g. localhost
        final int proxyPort = 9999; // Proxy port
        final String proxyUser = "<proxy-user>";
        final String proxyPassword = "<proxy-password>";
        //
        ProxyOptions proxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP,
            new InetSocketAddress(proxyHost, proxyPort));
        proxyOptions = proxyOptions.setCredentials(proxyUser, proxyPassword);
        HttpClient client = new OkHttpHttpClientBuilder().proxy(proxyOptions).build();
        // END: io.clientcore.http.okhttp3.OkHttpHttpClientBuilder#setProxyAuthenticator

    }

    public void connectionTimeoutSample() {

        // BEGIN: io.clientcore.http.okhttp3.OkHttpHttpClientBuilder#connectionTimeout
        final Duration connectionTimeout = Duration.ofSeconds(250); // connection timeout of 250 seconds
        HttpClient client = new OkHttpHttpClientBuilder().connectionTimeout(connectionTimeout).build();
        // END: io.clientcore.http.okhttp3.OkHttpHttpClientBuilder#connectionTimeout

    }

    public void readTimeoutSample() {

        // BEGIN: io.clientcore.http.okhttp3.OkHttpHttpClientBuilder#readTimeout
        final Duration readTimeout = Duration.ofSeconds(100); // read timeout of 100 seconds
        HttpClient client = new OkHttpHttpClientBuilder().readTimeout(readTimeout).build();
        // END: io.clientcore.http.okhttp3.OkHttpHttpClientBuilder#readTimeout

    }

    public void usingExistingHttpClientSample() {

        // BEGIN: io.clientcore.http.okhttp3.using-existing-okhttp
        // Create an OkHttpClient with connection timeout of 250 seconds.
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder().connectTimeout(Duration.ofSeconds(250)).build();
        // Use "okHttpClient" instance to create an azure-core HttpClient "client".
        // Both "okHttpClient" and "client" share same underlying resources such as
        // connection pool, thread pool.
        // "client" inherits connection timeout settings and add proxy.
        ProxyOptions proxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP,
            new InetSocketAddress("<proxy-host>", 9999));
        HttpClient client = new OkHttpHttpClientBuilder(okHttpClient).proxy(proxyOptions).build();
        // END: io.clientcore.http.okhttp3.using-existing-okhttp

    }
}
