// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;
import reactor.netty.http.HttpProtocol;

import java.net.InetSocketAddress;

/**
 * Class containing code snippets that will be injected to README.md.
 */
public class ReadmeSamples {

    /**
     * Sample code for creating async Netty HTTP client.
     */
    public void createBasicClient() {
        // BEGIN: readme-sample-createBasicClient
        HttpClient client = new NettyAsyncHttpClientBuilder().build();
        // END: readme-sample-createBasicClient
    }

    /**
     * Sample code for creating async Netty HTTP client with proxy.
     */
    public void createProxyClient() {
        // BEGIN: readme-sample-createProxyClient
        HttpClient client = new NettyAsyncHttpClientBuilder()
            .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("<proxy-host>", 8888)))
            .build();
        // END: readme-sample-createProxyClient
    }

    /**
     * Sample code for creating async Netty HTTP client that supports both the HTTP/1.1 and HTTP/2 protocols, with
     * HTTP/2 being the preferred protocol.
     */
    public void useHttp2WithConfiguredNettyClient() {
        // BEGIN: readme-sample-useHttp2WithConfiguredNettyClient
        // Constructs an HttpClient that supports both HTTP/1.1 and HTTP/2 with HTTP/2 being the preferred protocol.
        HttpClient client = new NettyAsyncHttpClientBuilder(reactor.netty.http.client.HttpClient.create()
            .protocol(HttpProtocol.HTTP11, HttpProtocol.H2))
            .build();
        // END: readme-sample-useHttp2WithConfiguredNettyClient
    }

    /**
     * Sample code for creating async Netty HTTP client that only supports HTTP/2.
     */
    public void useHttp2OnlyWithConfiguredNettyClient() {
        // BEGIN: readme-sample-useHttp2OnlyWithConfiguredNettyClient
        // Constructs an HttpClient that only supports HTTP/2.
        HttpClient client = new NettyAsyncHttpClientBuilder(reactor.netty.http.client.HttpClient.create()
            .protocol(HttpProtocol.H2))
            .build();
        // END: readme-sample-useHttp2OnlyWithConfiguredNettyClient
    }

    /**
     * Sample code for creating an async Netty HTTP client with a customized max chunk size.
     * <p>
     * Max chunk size is used to determine the maximum size of the ByteBuf, later converted to ByteBuffer for use
     * throughout the rest of the SDKs and for compatibility with JDK APIs, returned by Netty. Changing this can
     * positively impact the performance of some APIs such as Storage's download to file methods provided in Blobs,
     * Datalake, and Files.
     */
    @SuppressWarnings("deprecation") // maxChunkSize is deprecated in a future version of Reactor Netty
    public void largerMaxChunkSizeWithConfiguredNettyClient() {
        // BEGIN: readme-sample-customMaxChunkSize
        // Constructs an HttpClient with a modified max chunk size.
        // Max chunk size modifies the maximum size of ByteBufs returned by Netty (later converted to ByteBuffer).
        // Changing the chunk size can positively impact performance of APIs such as Storage's download to file methods
        // provided in azure-storage-blob, azure-storage-file-datalake, and azure-storage-file-shares (32KB - 64KB have
        // shown the most consistent improvement).
        HttpClient httpClient = new NettyAsyncHttpClientBuilder(reactor.netty.http.client.HttpClient.create()
            .httpResponseDecoder(httpResponseDecoderSpec -> httpResponseDecoderSpec.maxChunkSize(64 * 1024)))
            .build();
        // END: readme-sample-customMaxChunkSize
    }
}
