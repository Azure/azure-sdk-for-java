// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp.implementation;

import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder;
import com.azure.core.test.implementation.RestProxyTests;
import java.net.InetSocketAddress;
import org.junit.jupiter.api.Disabled;

@Disabled("Should only be run manually when a local proxy server (e.g. Fiddler) is running")
public class RestProxyWithHttpProxyOkHttpTests extends RestProxyTests {

    @Override
    protected HttpClient createHttpClient() {
        ProxyOptions proxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP,
                new InetSocketAddress("localhost", 8888));
        return new OkHttpAsyncHttpClientBuilder()
                .proxy(proxyOptions)
                .build();
    }
}
