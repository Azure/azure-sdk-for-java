// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp.implementation;

import com.azure.core.http.HttpClient;
import com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder;
import com.azure.core.implementation.RestProxyTests;
import org.junit.Ignore;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.Proxy;

@Ignore("Should only be run manually when a local proxy server (e.g. Fiddler) is running")
public class RestProxyWithHttpProxyOkHttpTests extends RestProxyTests {

    @Override
    protected HttpClient createHttpClient() {

        return new OkHttpAsyncHttpClientBuilder()
                .setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8888)))
                .build();
    }

    @Test
    @Override
    @Ignore("OKHttp does not support setting null as header value.")
    public void syncGetRequestWithNullHeader() {
        super.syncGetRequestWithNullHeader();
    }
}
