// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.ProxyOptions;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

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
        // BEGIN: com.azure.core.http.netty.NettyAsyncHttpClientBuilder#eventLoopGroup
        int threadCount = 5;
        HttpClient client = new NettyAsyncHttpClientBuilder()
            .eventLoopGroup(new NioEventLoopGroup(threadCount))
            .build();
        // END: com.azure.core.http.netty.NettyAsyncHttpClientBuilder#eventLoopGroup
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

    /**
     * Code snippet for creating a new http client based on an existing reactor netty HttpClient.
     * The existing client is configured for netty level logging.
     */
    public void fromExistingReactorNettyClient() {
        // BEGIN: com.azure.core.http.netty.from-existing-http-client
        // Creates a reactor-netty client with netty logging enabled.
        reactor.netty.http.client.HttpClient baseHttpClient = reactor.netty.http.client.HttpClient.create()
            .tcpConfiguration(tcp -> tcp.bootstrap(b -> b.handler(new LoggingHandler(LogLevel.INFO))));
        // Create an HttpClient based on above reactor-netty client and configure EventLoop count.
        HttpClient client = new NettyAsyncHttpClientBuilder(baseHttpClient)
            .eventLoopGroup(new NioEventLoopGroup(5))
            .build();
        // END: com.azure.core.http.netty.from-existing-http-client
    }

    /**
     * Code snippet to demonstrate the use of a Netty based http client that disables buffer copy.
     */
    public void disabledBufferCopyClientSample() {
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost");
        // BEGIN: com.azure.core.http.netty.disabled-buffer-copy
        HttpClient client = new NettyAsyncHttpClientBuilder()
            .port(8080)
            .disableBufferCopy(true)
            .build();

        client.send(httpRequest)
            .flatMapMany(response -> response.getBody())
            .map(byteBuffer -> completeProcessingByteBuffer(byteBuffer))
            .subscribe();
        // END: com.azure.core.http.netty.disabled-buffer-copy
    }

    private int completeProcessingByteBuffer(ByteBuffer byteBuffer) {
        return byteBuffer.remaining();
    }
}
