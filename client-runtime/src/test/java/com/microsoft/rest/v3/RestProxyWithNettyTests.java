package com.microsoft.rest.v3;

import com.microsoft.rest.v3.http.HttpClient;
import com.microsoft.rest.v3.http.HttpClientConfiguration;
import com.microsoft.rest.v3.http.NettyClient;

public class RestProxyWithNettyTests extends RestProxyTests {
    private static NettyClient.Factory nettyClientFactory = new NettyClient.Factory();

    @Override
    protected HttpClient createHttpClient() {
        return nettyClientFactory.create(new HttpClientConfiguration());
    }
}
