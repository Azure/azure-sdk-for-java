// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.HttpClient;
import io.netty.channel.nio.NioEventLoopGroup;
import reactor.core.publisher.Mono;
import reactor.netty.tcp.ProxyProvider.Proxy;

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
            .setPort(8080)
            .setWiretap(true)
            .build();
        // END: com.azure.core.http.netty.instantiation-simple
    }

    /**
     * Code snippet for creating http client with fixed thread pool.
     */
    public void fixedThreadPoolSample() {

        // BEGIN: com.azure.core.http.netty.NettyAsyncHttpClientBuilder#NioEventLoopGroup
        int threadCount = 5;
        HttpClient client = new NettyAsyncHttpClientBuilder()
            .setNioEventLoopGroup(new NioEventLoopGroup(threadCount))
            .build();
        // END: com.azure.core.http.netty.NettyAsyncHttpClientBuilder#NioEventLoopGroup
    }

    public void reactorNettyRawConfigurations() {
        // BEGIN: com.azure.core.http.netty.NettyAsyncHttpClientBuilder#reactorNettyConfiguration
        HttpClient client = new NettyAsyncHttpClientBuilder()
            .reactorNettyConfiguration(rawClient -> rawClient
                .secure()
                .wiretap(true)
                .tcpConfiguration(tcpClient -> tcpClient
                    .proxy(typeSpec -> typeSpec
                        .type(Proxy.HTTP)
                        .address(new InetSocketAddress("localhost", 8888)))
                    .handle((in, out) -> Mono.empty()))
                .doOnRequestError((req, e) -> System.err.println(e.getMessage())))
            .build();
        // END: com.azure.core.http.netty.NettyAsyncHttpClientBuilder#reactorNettyConfiguration
    }

}
