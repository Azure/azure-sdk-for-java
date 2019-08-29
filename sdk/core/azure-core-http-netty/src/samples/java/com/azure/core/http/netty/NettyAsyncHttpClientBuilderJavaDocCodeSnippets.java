// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.HttpClient;
import io.netty.channel.nio.NioEventLoopGroup;

/**
 * Code snippets for {@link NettyAsyncHttpClientBuilder}
 */
public class NettyAsyncHttpClientBuilderJavaDocCodeSnippets {

    private void simpleInstantiation() {

        // BEGIN: com.azure.core.http.netty.instantiation-simple
        HttpClient client = new NettyAsyncHttpClientBuilder()
            .port(8080)
            .wiretap(true)
            .build();
        // END: com.azure.core.http.netty.instantiation-simple
    }

    private void fixedThreadPoolSample() {

        // BEGIN: com.azure.core.http.netty.NettyAsyncHttpClientBuilder#NioEventLoopGroup
        int threadCount = 5;
        HttpClient client = new NettyAsyncHttpClientBuilder()
            .nioEventLoopGroup(new NioEventLoopGroup(threadCount))
            .build();
        // END: com.azure.core.http.netty.NettyAsyncHttpClientBuilder#NioEventLoopGroup
    }

}
