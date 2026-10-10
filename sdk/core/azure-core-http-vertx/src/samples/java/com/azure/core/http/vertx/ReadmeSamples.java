// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpProtocolVersion;
import com.azure.core.http.ProxyOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
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
        // NOTE: Internal options provide connection, read and write timeouts and proxy settings.
        HttpClient httpClient = new VertxHttpClientBuilder()
            .httpClientOptions(new HttpClientOptions().setMaxHeaderSize(256 * 1024))
            .build();
        // END: readme-sample-customMaxHeaderSize
    }

    /**
     * Configures HTTP/2 with HTTP/1.1 fallback.
     */
    public void configureHttpVersion() {
        // BEGIN: readme-sample-configureHttpVersion
        HttpClient client = new VertxHttpClientBuilder()
            .maximumHttpVersion(HttpProtocolVersion.HTTP_2)
            .build();
        // END: readme-sample-configureHttpVersion
    }

    /**
     * Configures connection, write, response and read timeouts.
     */
    public void configureTimeouts() {
        // BEGIN: readme-sample-configureTimeouts
        HttpClient client = new VertxHttpClientBuilder()
            .connectTimeout(Duration.ofSeconds(60))
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
        HttpClient client = new VertxHttpClientBuilder()
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
        HttpClient client = new VertxHttpClientBuilder()
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
        HttpClient client = new VertxHttpClientBuilder()
            .maximumHttpVersion(HttpProtocolVersion.HTTP_1_1)
            .build();
        // END: readme-sample-useHttp1
    }

    /**
     * Creates an HTTP client using a Vert.x instance with a customized event-loop pool.
     */
    public void createClientWithVertxInstance() {
        // BEGIN: readme-sample-createClientWithVertxInstance
        VertxOptions vertxOptions = new VertxOptions()
            .setEventLoopPoolSize(4);
        Vertx vertx = Vertx.vertx(vertxOptions);
        HttpClient client = new VertxHttpClientBuilder()
            .vertx(vertx)
            .build();
        // END: readme-sample-createClientWithVertxInstance
    }

    /**
     * Configures HTTP/2 prior knowledge for cleartext connections.
     */
    public void configureHttp2PriorKnowledge() {
        // BEGIN: readme-sample-configureHttp2PriorKnowledge
        HttpClientOptions clientOptions = new HttpClientOptions()
            .setHttp2ClearTextUpgrade(false);
        HttpClient client = new VertxHttpClientBuilder()
            .httpClientOptions(clientOptions)
            .maximumHttpVersion(HttpProtocolVersion.HTTP_2)
            .build();
        // END: readme-sample-configureHttp2PriorKnowledge
    }
}
