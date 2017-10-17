package com.microsoft.azure;

import com.microsoft.rest.http.HttpClient;
import com.microsoft.rest.http.HttpClient.Configuration;
import com.microsoft.rest.http.NettyClient;
import com.microsoft.rest.policy.RequestPolicy;

import java.util.Collections;

public class AzureProxyToRestProxyWithNettyTests extends AzureProxyToRestProxyTests {
    private final NettyClient.Factory nettyClientFactory = new NettyClient.Factory();

    @Override
    protected HttpClient createHttpClient() {
        return nettyClientFactory.create(new Configuration(Collections.<RequestPolicy.Factory>emptyList(), null));
    }
}
