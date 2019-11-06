// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.test.implementation.RestProxyTests;
import org.junit.jupiter.api.Disabled;

import java.net.InetSocketAddress;

@Disabled("Should only be run manually when a local proxy server (e.g. Fiddler) is running")
public class RestProxyWithHttpProxyNettyTests extends RestProxyTests {

    @Override
    protected HttpClient createHttpClient() {
        InetSocketAddress address = new InetSocketAddress("localhost", 8888);
        return new NettyAsyncHttpClientBuilder().proxy(new ProxyOptions(ProxyOptions.Type.HTTP, address)).build();
    }
}
