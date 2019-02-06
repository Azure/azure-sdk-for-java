package com.microsoft.azure.v3;

import com.microsoft.rest.v3.http.HttpClient;
import com.microsoft.rest.v3.http.HttpClientConfiguration;
import com.microsoft.rest.v3.http.NettyClient;

public class AzureProxyToRestProxyWithNettyTests extends AzureProxyToRestProxyTests {
    private final NettyClient.Factory nettyClientFactory = new NettyClient.Factory();

    @Override
    protected HttpClient createHttpClient() {
        return nettyClientFactory.create(new HttpClientConfiguration());
    }
}
