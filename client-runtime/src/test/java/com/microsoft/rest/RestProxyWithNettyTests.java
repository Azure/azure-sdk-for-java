package com.microsoft.rest;

import com.microsoft.rest.http.HttpClient;
import com.microsoft.rest.http.HttpClient.Configuration;
import com.microsoft.rest.http.NettyClient;
import com.microsoft.rest.policy.RequestPolicy.Factory;

import java.util.Collections;

public class RestProxyWithNettyTests extends RestProxyTests {
    private static NettyClient.Factory nettyClientFactory = new NettyClient.Factory();

    @Override
    protected HttpClient createHttpClient() {
        return nettyClientFactory.create(new Configuration(Collections.<Factory>emptyList(), null));
    }
}
