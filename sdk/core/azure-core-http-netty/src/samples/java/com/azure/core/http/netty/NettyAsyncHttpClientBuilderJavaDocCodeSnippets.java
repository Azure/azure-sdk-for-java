// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;
import io.netty.channel.nio.NioEventLoopGroup;

import java.net.InetSocketAddress;

/**
 * Code snippets for {@link NettyAsyncHttpClientBuilder}
 */
public class NettyAsyncHttpClientBuilderJavaDocCodeSnippets {

    /**
     * Code snippet for simple http client instantiation.
     */
    public void simpleInstantiation() {
        // BEGIN: com.azure.core.http.netty.instantiation-simple
        HttpClient client = new NettyAsyncHttpClientBuilder()
            .port(8080)
            .wiretap(true)
            .build();
        // END: com.azure.core.http.netty.instantiation-simple
    }

    /**
     * Code snippet for creating http client with fixed thread pool.
     */
    public void fixedThreadPoolSample() {
        // BEGIN: com.azure.core.http.netty.NettyAsyncHttpClientBuilder#nioEventLoopGroup
        int threadCount = 5;
        HttpClient client = new NettyAsyncHttpClientBuilder()
            .nioEventLoopGroup(new NioEventLoopGroup(threadCount))
            .build();
        // END: com.azure.core.http.netty.NettyAsyncHttpClientBuilder#nioEventLoopGroup
    }

    /**
     * Code snippet for creating http client with proxy.
     */
    public void proxySample() {
        // BEGIN: com.azure.core.http.netty.NettyAsyncHttpClientBuilder#proxy
        HttpClient client = new NettyAsyncHttpClientBuilder()
            .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("<proxy-host>", 8888)))
            .build();
        // END: com.azure.core.http.netty.NettyAsyncHttpClientBuilder#proxy
    }
}
