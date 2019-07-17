// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.ProxyOptions.Type;
import org.junit.jupiter.api.Disabled;

import java.net.InetSocketAddress;

@Disabled("Should only be run manually when a local proxy server (e.g. Fiddler) is running")
public class RestProxyWithHttpProxyNettyTests extends RestProxyTests {

    @Override
    protected HttpClient createHttpClient() {
        InetSocketAddress address = new InetSocketAddress("localhost", 8888);
        return HttpClient.createDefault().proxy(() -> new ProxyOptions(Type.HTTP, address));
    }
}
